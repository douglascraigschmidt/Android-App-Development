package vandy.mooc.uniqueidgen.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import vandy.mooc.uniqueidgen.R;
import vandy.mooc.uniqueidgen.services.UniqueIDGenService;
import vandy.mooc.uniqueidgen.views.GeneratorView;

/**
 * A class that interacts with a user to generate a system-wide unique ID via
 * the UniqueIDGenService, which is a bound service that communicates to this
 * activity via a request messenger.
 */
public class UniqueIDGenActivity
       extends LifecycleLoggingActivity {
    /**
     * Used for debugging.
     */
    private final String TAG = getClass().getName();

    /**
     * Reference to a custom view class that animates a circle along path view
     * widgets that connect activity, service, and 4 thread widgets.
     */
    private GeneratorView mGeneratorView;

    /**
     * Location where the unique ID is displayed.
     */
    private TextView mGuidTextView;

    /**
     * Reference to the request messenger that's implemented in the
     * UniqueIDGenService.
     */
    private Messenger mReqMessengerRef = null;

    /**
     * The ReplyMessenger whose reference is passed to the UniqueIDGenService
     * and used to process replies from the service.
     */
    private Messenger mReplyMessenger;

    /**
     * This ServiceConnection is used to receive a Messenger reference after
     * binding to the UniqueIDGenService using bindService().
     */
    private ServiceConnection mSvcConn = new ServiceConnection() {
            /**
             * Called after the UniqueIDGenService is connected to
             * convey the result returned from onBind().
             */
            public void onServiceConnected(
                                           ComponentName className,
                                           IBinder binder) {
                Log.d(TAG, "ComponentName:" + className);

                // Create a new Messenger that encapsulates the
                // returned IBinder object and store it for later use
                // in mReqMessengerRef.
                mReqMessengerRef = new Messenger(binder);
            }

            /**
             * Called if the Service crashes and is no longer
             * available.  The ServiceConnection will remain bound,
             * but the Service will not respond to any requests.
             */
            public void onServiceDisconnected(ComponentName className) {
                Log.d(TAG, "Service has been disconnected");

                mReqMessengerRef = null;
            }
        };

    /**
     * Method that initializes the Activity when it is first created.
     *
     * @param savedInstanceState Activity's previously frozen state, if there
     *                           was one.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize the superclass.
        super.onCreate(savedInstanceState);

        // Set the activity's view.
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab =
            (FloatingActionButton) findViewById(R.id.play_fab);
        fab.setOnClickListener(this::generateUniqueID);

        // Initialize the views.
        mGuidTextView = (TextView) findViewById(R.id.guid_text_view);
        mGeneratorView = (GeneratorView) findViewById(R.id.generator_view);

        // Initialize the reply messenger.
        mReplyMessenger = 
            new Messenger(new ReplyHandler(this));
    }

    /**
     * Hook method called by Android when this activity becomes visible.
     */
    @Override
    protected void onStart() {
        // Call to super class.
        super.onStart();

        Log.d(TAG, "calling bindService()");
        if (mReqMessengerRef == null) {
            // Bind to the UniqueIDGenService associated with this
            // Intent.
            bindService(UniqueIDGenService.makeIntent(this),
                    mSvcConn,
                    Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Hook method called by Android when this activity becomes invisible.
     */
    @Override
    protected void onStop() {
        // Unbind from the Service.
        unbindService(mSvcConn);

        // Call to super class.
        super.onStop();
    }

    /**
     * Called by Android when the user presses the "Generate Unique ID" button
     * to request a new unique ID via UniqueIDGenService.
     */
    public void generateUniqueID(View view) {
        // Create a request message that indicates the
        // UniqueIdGenService should send the reply back to
        // ReplyHandler encapsulated by the mReplyMessenger.
        Message request = Message.obtain();
        request.replyTo = mReplyMessenger;

        try {
            if (mReqMessengerRef != null) {
                Log.d(TAG, "sending message");

                // Run animation along path from this clicked button to
                // the activity view.
                request.arg1 =
                    mGeneratorView.startAnimation(
                                                  null,
                                                  GeneratorView.START_NODE,
                                                  GeneratorView.ACTIVITY_NODE,
                                                  GeneratorView.SERVICE_NODE);

                // Send request message to UniqueIDGenService.
                //animateInput();
                mReqMessengerRef.send(request);
            } else {
                // Output a warning indicating that the service is
                // not running.
                Log.w(TAG, "Service is not currently running");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives the reply from the UniqueIDGenService containing the
     * unique ID and displays it to the user.
     */
    static class ReplyHandler 
           extends Handler {
        /**
         * Logging tag.
         */
        private static final String TAG = "ReplyHandler";

        /**
         * Reference back to the enclosing activity.
         */
        private UniqueIDGenActivity mUniqueIDGenActivity;
        
        /**
         * Reference to the enclosing GeneratorView.
         */
        private GeneratorView mGeneratorView;

        /**
         * Constructor initializes the fields.
         *
         * @param uniqueIDGenActivity Reference to the enclosing activity.
         */
        public ReplyHandler(UniqueIDGenActivity uniqueIDGenActivity) {
            this.mUniqueIDGenActivity = uniqueIDGenActivity;
            this.mGeneratorView = mUniqueIDGenActivity.mGeneratorView;
        }

        /**
         * Callback to handle the reply from the UniqueIDGenService.
         */
        public void handleMessage(Message reply) {
            int requestId = reply.arg1;
            int pathId = reply.arg2;

            // Get the unique ID encapsulated in reply Message.
            String uniqueID = uniqueID(reply);

            // Process an animation for the service.
            mGeneratorView.addAnimation(requestId, null, pathId);

            // Check if we finally have a result.
            if (!TextUtils.isEmpty(uniqueID)) {
                // Now that we have a result, perform one final
                // animation from the activity to the GUID text view
                // and also register an animation callback so that we
                // can update the text view with the GUID once the
                // animation completes.
                Log.d(TAG, "received unique ID " + uniqueID);
                mGeneratorView.addAnimation
                    (requestId,
                     node -> {
                        if (node == GeneratorView.END_NODE) {
			                // Display the unique ID to the user.
                            mUniqueIDGenActivity.mGuidTextView.setText(uniqueID);
                            mGeneratorView.endAnimation(requestId);
                        }
                    }, GeneratorView.END_NODE);
            }
        }

        /**
         * Extracts the encapsulated unique ID from the reply Message.
         */
        private String uniqueID(Message replyMessage) {
            return replyMessage.getData().getString(UniqueIDGenService.ID);
        }
    }
}

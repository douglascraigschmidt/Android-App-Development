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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import vandy.mooc.uniqueidgen.service.UniqueIDGenService;
import vandy.mooc.uniqueidgen.R;

/**
 * This class interacts with a user to generate a system-wide unique
 * ID via the UniqueIDGenService, which is a bound service.
 */
public class UniqueIDGenActivity 
       extends LifecycleLoggingActivity {
    /**
     * Used for debugging.
     */
    private final String TAG = getClass().getName();

    /**
     * Location where the unique ID is displayed.
     */
    private TextView mOutput;

    /**
     * Reference to the request messenger that's implemented in the
     * UniqueIDGenService.
     */
    private Messenger mReqMessengerRef = null;

    /**
     * The ReplyMessenger whose reference is's passed to the
     * UniqueIDGenService and used to process replies from the
     * service.
     */
    private Messenger mReplyMessenger;

    /**
     * Receives the reply from the UniqueIDGenService containing the
     * unique ID and displays it to the user.
     */
    class ReplyHandler 
          extends Handler {
        /**
         * Callback to handle the reply from the UniqueIDGenService.
         */
        public void handleMessage(Message reply) {
            // Get the unique ID encapsulated in reply Message.
            String uniqueID =
                UniqueIDGenService.uniqueID(reply);

            Log.d(TAG, "received unique ID " + uniqueID);

            // Display the unique ID.
            mOutput.setText(uniqueID);
        }
    }

    /** 
     * This ServiceConnection is used to receive a Messenger reference
     * after binding to the UniqueIDGenService using bindService().
     */
    private ServiceConnection mSvcConn = new ServiceConnection() {
            /**
             * Called after the UniqueIDGenService is connected to
             * convey the result returned from onBind().
             */
            public void onServiceConnected(ComponentName className,
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
                mReqMessengerRef = null;
            }
	};

    /**
     * Method that initializes the Activity when it is first created.
     * 
     * @param savedInstanceState
     *            Activity's previously frozen state, if there was one.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize the superclass.
        super.onCreate(savedInstanceState);

        // Set the activity's view.
        setContentView(R.layout.main);

        // Initialize the views.
        mOutput = (TextView) findViewById(R.id.output);

        // Initialize the reply messenger.
        mReplyMessenger =
	    new Messenger(new ReplyHandler());
    }

    /**
     * Called by Android when the user presses the "Generate Unique
     * ID" button to request a new unique ID via UniqueIDGenService.
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

                // Send request message to UniqueIDGenService.
                mReqMessengerRef.send(request);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook method called by Android when this activity becomes
     * visible.
     */
    @Override
    protected void onStart() {
        // Call to super class.
        super.onStart();

        Log.d(TAG, "calling bindService()");
        if (mReqMessengerRef == null)
            // Bind to the UniqueIDGenService associated with this
            // Intent.
            bindService(UniqueIDGenService.makeIntent(this),
                        mSvcConn,
                        Context.BIND_AUTO_CREATE);
    }

    /**
     * Hook method called by Android when this activity becomes
     * invisible.
     */
    @Override
    protected void onStop() {
        // Unbind from the Service.
        unbindService(mSvcConn);

        // Call to super class.
        super.onStop();
    }
}

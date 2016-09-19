package vandy.mooc.pingpong.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.io.File;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * A main Activity that prompts the user for a count and then plays
 * ping/pong using a statically registered broadcast receiver and
 * dynamically registered broadcast receiver.
 */
public class MainActivity
       extends LifecycleLoggingActivity {
   /**
     * EditText field for entering the desired count of "pings" and
     * "pongs".
     */
    private EditText mCountEditText;

    /**
     * Keeps track of whether a button click from the user is
     * processed or not.  Only one click is processed until the pings
     * and pongs are finished.
     */
    public static boolean mProcessButtonClick = true;

    /**
     * Number of times to send "ping" and "pong" if the user doesn't
     * specify otherwise.
     */
    private final static int mDefaultCount = 3;

    /**
     * Intent sent to the PingReceiver.
     */
    private final static String ACTION_VIEW_PING = 
        "vandy.mooc.action.VIEW_PING";

    /**
     * Intent sent to the PongReceiver.
     */
    private final static String ACTION_VIEW_PONG =
        "vandy.mooc.action.VIEW_PONG";

    /**
     * Dynamically registered broadcast receiver that handles "pings".
     */
    private PingReceiver mPingReceiver;

    /**
     * Reference to the "play" floating action button.
     */
    private FloatingActionButton mSetFab;

    /**
     * Reference to the "set" floating action button.
     */
    private FloatingActionButton mCountFab;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a count.
     */
    private boolean mIsEditTextVisible = false;

    /**
     * Hook method called when a new instance of Activity is
     * created. One time initialization code goes here, e.g., UI
     * snackbar and some class scope variable initialization.
     *
     * @param savedInstanceState
     *            object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Set the default snackbar.
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();
    }

    /**
     * Hook method called after onStart(), just before the activity
     * (re)gains focus.
     */
    @Override
    protected void onResume() {
        // Always call super class for necessary
        // initialization/implementation.
        super.onResume();

        // Call helper method to register a broadcast receiver that
        // will receive "ping" intents.
        registerPingReceiver();
    }

    /**
     * Hook method called when activity is about to lose focus.
     * Release resources that may cause a memory leak.
     */
    @Override
    protected void onPause(){
        // Always call super method.
        super.onPause();

        // Unregister the PingReceiver.
        if (mPingReceiver != null)
            unregisterReceiver(mPingReceiver);
    }

    /**
     * A broadcast receiver that handles "ping" intents.
     */
    public class PingReceiver
           extends BroadcastReceiver {
        /**
         * Number of times to play "ping/pong".
         */
        private final int mMaxCount;

        /**
         * Debugging tag used by the Android logger.
         */
        protected final String TAG =
            getClass().getSimpleName();

        /**
         * Constructor sets the field.
         */
        public PingReceiver(int maxCount) {
            mMaxCount = maxCount;
        }

        /**
         * Hook method called by the Android ActivityManagerService
         * framework after a broadcast has sent.
         *
         * @param context The caller's context.
         * @param ping  An intent containing ping data.
         */
        @Override
        public void onReceive(Context context,
                              Intent ping) {
            // Get the count from the PongReceiver.
            Integer count = ping.getIntExtra("COUNT", 0);

            Log.d(TAG, "onReceive() called with count of "
                  + count.intValue());

            // If we're not done then pop a toast and broadcast a
            // "pong" intent.
            if (count <= mMaxCount) {
                // Inform send that we're "ping'd".
                UiUtils.showToast(context,
                                  "Ping " + count);

                // Send a "pong" intent.
                context.sendBroadcast(makePongIntent(count));
            } else {
                // Inform the user we're done.
                UiUtils.showToast(context,
                                  "Finished playing ping/pong");

                // Allow user input again.
                MainActivity.mProcessButtonClick = true;

                // Unregister the PingReceiver.
                unregisterReceiver(mPingReceiver);
            }
        }

        /**
         * Factory method that makes a "pong" intent with the given @a
         * count as an extra.
         */
        public Intent makePongIntent(int count) {
            return new Intent(ACTION_VIEW_PONG)
                .putExtra("COUNT", count);
        }
    }

    /**
     * A broadcast receiver that handles "pong" intents.
     */
    public static class PongReceiver
            extends BroadcastReceiver {
        /**
         * Debugging tag used by the Android logger.
         */
        protected final String TAG =
            getClass().getSimpleName();

        /**
         * Hook method called by the Android ActivityManagerService
         * framework after a broadcast has been sent.
         *
         * @param context The caller's context.
         * @param data  An intent containing ...
         */
        @Override
        public void onReceive(Context context,
                              Intent data) {
            // Get the count from the PingReceiver.
            Integer count = data.getIntExtra("COUNT", 0);
            Log.d(TAG, "onReceive() called with count of "
                  + count.intValue());

            // Inform send that we're "pong'd".
            UiUtils.showToast(context,
                              "Pong " + count);

            // Broadcast a "ping", incrementing the count by one.
            context.sendBroadcast(makePingIntent(count + 1));
        }

        /**
         * Factory method that makes a "ping" intent with the given @a
         * count as an extra.
         */
        public static Intent makePingIntent(int count) {
            return new Intent(ACTION_VIEW_PING).
                putExtra("COUNT", count);
        }
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Set the EditText that holds the count entered by the user
        // (if any).
        mCountEditText = (EditText) findViewById(R.id.count);

        // Cache floating action button that sets the count.
        mSetFab = (FloatingActionButton) findViewById(R.id.set_fab);

        // Cache floating action button that starts playing ping/pong.
        mCountFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the EditText invisible for animation purposes.
        mCountEditText.setVisibility(View.INVISIBLE);

        // Make the count button invisible for animation purposes.
        mCountFab.setVisibility(View.INVISIBLE);

        // Register a listener to help display "start playing" FAB
        // when the user hits enter.
        mCountEditText.setOnEditorActionListener
            ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.hideKeyboard(MainActivity.this,
                                         mCountEditText.getWindowToken());
                    UiUtils.showFab(mCountFab);
                    return true;
                } else
                    return false;
            });
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the '+' floating action button.
     *
     * @param view The view
     */
    public void setCount(View view) {
        // Check whether the EditText is visible to determine
        // the kind of animations to use.
        if (mIsEditTextVisible) {
            // Hide the EditText using circular reveal animation
            // and set boolean to false.
            UiUtils.hideEditText(mCountEditText);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mSetFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the count FAB.
            UiUtils.hideFab(mCountFab);
        } else {
            // Hide the EditText using circular reveal animation
            // and set boolean to true.
            UiUtils.revealEditText(mCountEditText);
            mIsEditTextVisible = true;
            mCountEditText.requestFocus();

            // Rotate the FAB from '+' to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mSetFab.startAnimation(AnimationUtils.loadAnimation(this,
                                                                animRedId));
        }
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Start Playing" button.
     *
     * @param view
     *            The view.
     */
    public void startPlaying(View view) {
        try {
            // Hide the keyboard.
            UiUtils.hideKeyboard(this,
                                 mCountEditText.getWindowToken());

            // Get the count from the user.
            int count = getCount();

            // Make sure there's a non-0 count.
            if (count > 0) {
                // Ensure there's not already a game in progress.
                if (!mProcessButtonClick)
                    UiUtils.showToast(this,
                                      "Already playing with count of "
                                      + count);
                else {
                    // Disable processing of a button click.
                    mProcessButtonClick = false;

                    // Initialize the PingReceiver.
                    mPingReceiver = new PingReceiver(count);

                    // Dynamically register the PingReceiver.
                    registerPingReceiver();

                    // Create a new "ping" intent with an initial
                    // count of 1 and start playing "ping/pong".
                    mPingReceiver.onReceive(this,
                                            PongReceiver.makePingIntent(1))
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the count based on user input.
     */
    protected int getCount() {
        // Get the text the user typed in the edit text (if anything).
        String userInput = mCountEditText.getText().toString();

        // If the user didn't provide a count then use the default.
        if ("".equals(userInput))
            return mDefaultCount;
        else 
            // Convert the count.
            return Integer.decode(userInput).intValue();
    }

    /**
     * Register the PingReceiver dynamically.
     */
    private void registerPingReceiver() {
        // Create an intent filter for ACTION_VIEW_PING.
        IntentFilter intentFilter = 
            new IntentFilter(ACTION_VIEW_PING);

        // Register the receiver and the intent filter.
        registerReceiver(mPingReceiver,
                         intentFilter);
    }
}

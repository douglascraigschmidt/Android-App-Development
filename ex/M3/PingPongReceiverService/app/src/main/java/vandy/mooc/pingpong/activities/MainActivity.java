package vandy.mooc.pingpong.activities;

import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.receiver.PingReceiver;
import vandy.mooc.pingpong.utils.UiUtils;

import static vandy.mooc.pingpong.R.id.count;

/**
 * MainActivity prompts the user for a count and then plays
 * "ping/pong" by passing intents between a started service and a
 * broadcast receiver that is dynamically registered.
 */
public class MainActivity
       extends LifecycleLoggingActivity {
    /**
     * Number of times to send "ping" and "pong" if the user doesn't specify
     * otherwise.
     */
    private final static int sDEFAULT_COUNT = 3;

    /**
     * EditText field for entering the desired count of "pings" and
     * "pongs".
     */
    private EditText mCountEditText;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a count.
     */
    private boolean mIsEditTextVisible = false;

    /**
     * Reference to the "set" floating action button.
     */
    private FloatingActionButton mSetFab;

    /**
     * Reference to the "start or stop" floating action button.
     */
    private FloatingActionButton mStartOrStopFab;

    /**
     * Status bar notification id that is shared between the ping
     * receiver and the pong service.
     */
    private final static int NOTIFICATION_ID = 1;

    /**
     * Dynamically registered broadcast receiver that handles "ping"
     * intents.
     */
    private PingReceiver mPingReceiver;

    /**
     * Hook method called when a new instance of Activity is
     * created. One time initialization code goes here, e.g., UI
     * snackbar and some class scope variable initialization.
     *
     * @param savedInstanceState object that contains saved state information.
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
     * Initialize the views.
     */
    private void initializeViews() {
        // Set the EditText that holds the count entered by the user
        // (if any).
        mCountEditText = (EditText) findViewById(count);

        // Cache floating action button that sets the count.
        mSetFab = (FloatingActionButton) findViewById(R.id.set_fab);

        // Cache floating action button that starts or stops playing
        // ping/pong.
        mStartOrStopFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the EditText invisible for animation purposes.
        mCountEditText.setVisibility(View.INVISIBLE);

        // Make the count button invisible for animation purposes.
        mStartOrStopFab.setVisibility(View.INVISIBLE);

        // Register a listener to help display "start playing" FAB
        // when the user hits enter.  This listener also sets a
        // default count value if the user enters no value.
        mCountEditText.setOnEditorActionListener
                ((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        UiUtils.hideKeyboard(MainActivity.this,
                                mCountEditText.getWindowToken());
                        if (TextUtils.isEmpty
                                (mCountEditText.getText().toString().trim()))
                            mCountEditText.setText(String.valueOf(sDEFAULT_COUNT));

                        UiUtils.showFab(mStartOrStopFab);
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    /**
     * Hook method called when activity is about to lose
     * focus. Release resources that may cause a memory leak.
     */
    @Override
    protected void onPause() {
        // Always call super method.
        super.onPause();

        if (mPingReceiver != null) {
            // Unregister the PingReceiver.
            unregisterReceiver(mPingReceiver);

            // Update status bar notification to idle.
            UiUtils.updateStatusBar(this,
                                    "Paused",
                                    R.drawable.idle,
                                    NOTIFICATION_ID);
        }
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

        if (mPingReceiver != null) {
            // Call helper method to register a broadcast receiver
            // that will receive "ping" intents.
            registerPingReceiver();

            // Resume game from at the last know iteration.
            mPingReceiver.onReceive
                (this,
                 PingReceiver.makePingIntent(this,
                                             mPingReceiver.getIteration() + 1,
                                             NOTIFICATION_ID));
        }
    }

    /**
     * Register the PingReceiver dynamically.
     */
    private void registerPingReceiver() {
        // Create an intent filter for ACTION_VIEW_PING.
        IntentFilter intentFilter =
                new IntentFilter(PingReceiver.ACTION_VIEW_PING);

        // Register the receiver and the intent filter.
        registerReceiver(mPingReceiver,
                         intentFilter);
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "startOrStopPlaying" button.
     *
     * @param view The view.
     */
    public void startOrStopPlaying(View view) {
        if (mPingReceiver != null) {
            // The receiver only exists while a game is in progress.
            stopPlaying();
        } else {
            // Get the count from the edit view.
            int count = Integer.valueOf(mCountEditText.getText().toString());

            // Make sure there's a count greater than 0.
            if (count <= 0) 
                // Inform the user there's a problem with the input.
                UiUtils.showToast(this,
                                  "Please specify a count value that's > 0");
            else
                startPlaying(count);
        }
    }

    /**
     * Start playing the game for @a count number of "pings" and "pongs".
     */
    private void startPlaying(int count) {
        // Hide the keyboard.
        UiUtils.hideKeyboard(this,
                mCountEditText.getWindowToken());

        // Initialize the PingReceiver.
        mPingReceiver = new PingReceiver(this, count);

        // Dynamically register the PingReceiver.
        registerPingReceiver();

        // Create a new "ping" intent with an initial
        // count of 1 and start playing "ping/pong".
        mPingReceiver.onReceive
                (this,
                 PingReceiver.makePingIntent(this,
                                             1,
                                             NOTIFICATION_ID));

        // Update the start/stop FAB to display a stop icon.
        mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
    }

    /**
     * Called by the PingReceiver when "ping/pong" is done.
     */
    public void stopPlaying() {
        // Unregister the PingReceiver.
        unregisterReceiver(mPingReceiver);

        // Null out the receiver to avoid later problems.
        mPingReceiver = null;

        // Cancel the status bar notification.
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(NOTIFICATION_ID);

        // Reset the start/stop FAB to the play icon.
        mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);
    }

    /**
     * Called by the Android Activity framework when the user clicks the '+'
     * floating action button.
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
            // Only hide the start/stop FAB if a game is not currently in
            // progress.
            if (mPingReceiver == null) {
                UiUtils.hideFab(mStartOrStopFab);
            }
        } else {
            // Reveal the EditText using circular reveal animation and
            // set boolean to true.
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
}

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
import android.widget.Toast;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.receivers.PingReceiver;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * MainActivity prompts the user for a count and then plays
 * "ping/pong" by passing intents between a two broadcast receivers
 * that are statically and dynamically registered.
 */
public class MainActivity
       extends LifecycleLoggingActivity {
   /**
     * EditText field for entering the desired count of "pings" and
     * "pongs".
     */
    private EditText mCountEditText;

    /**
     * Number of times to send "ping" and "pong" if the user doesn't
     * specify otherwise.
     */
    private final static int DEFAULT_COUNT = 3;

    /**
     * Status bar notification id that is shared between ping and pong receivers.
     */
    private final static int NOTIFICATION_ID = 1;

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
     * Reference to the "play" floating action button.
     */
    private FloatingActionButton mStartOrStopFab;

    /**
     * Dynamically registered broadcast receiver that handles "pings".
     */
    private PingReceiver mPingReceiver;

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

        if (mPingReceiver != null) {
            // Call helper method to register a broadcast receiver
            // that will receive "ping" intents.
            registerPingReceiver();
        }
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

        // Cancel the status bar notification.
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(NOTIFICATION_ID);
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

        // Cache floating action button that starts or stops playing ping/pong.
        mStartOrStopFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the EditText invisible for animation purposes.
        mCountEditText.setVisibility(View.INVISIBLE);

        // Make the count button invisible for animation purposes.
        mStartOrStopFab.setVisibility(View.INVISIBLE);

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
                    UiUtils.showFab(mStartOrStopFab);
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
            // Only hide the start/stop FAB if a game is not currently in
            // progress.
            if (mPingReceiver == null) {
                UiUtils.hideFab(mStartOrStopFab);
            }
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
     * @param view The view.
     */
    public void startOrStopPlaying(View view) {
        if (mPingReceiver != null) {
            // The receiver only exists while a game is in progress so .
            stopPlaying();
        } else {
            // Get the count from the edit view.
            int count = getCount();

            // Make sure there's a non-0 count.
            if (count == 0) {
                Toast.makeText(this,
                               "Please specify a non-zero count value",
                               Toast.LENGTH_SHORT).show();
            } else {
                startPlaying(count);
            }
        }
    }

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
        mPingReceiver.onReceive(this,
                                PingReceiver.makePingIntent(
                                        this, 1, NOTIFICATION_ID));

        // Update the start/stop FAB to display a stop icon.
        mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
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
     * Get the count based on user input.
     */
    protected int getCount() {
        // Get the text the user typed in the edit text (if anything).
        String userInput = mCountEditText.getText().toString();

        // If the user didn't provide a count then use the default.
        if (TextUtils.isEmpty(userInput.trim()))
            return DEFAULT_COUNT;
        else
            // Convert the count.
            return Integer.decode(userInput);
    }
}

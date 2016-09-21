package vandy.mooc.pingpong.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.receivers.PingReceiver;
import vandy.mooc.pingpong.receivers.PongReceiver;
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
                    mPingReceiver = new PingReceiver(this, count);

                    // Dynamically register the PingReceiver.
                    registerPingReceiver();

                    // Create a new "ping" intent with an initial
                    // count of 1 and start playing "ping/pong".
                    mPingReceiver.onReceive
                            (this, PingReceiver.makePingIntent(this, 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
     * Called by the PingReceiver when "ping/pong" is done.
     */
    public void stopPlaying() {
        // Allow user input again.
        mProcessButtonClick = true;

        // Unregister the PingReceiver.
        unregisterReceiver(mPingReceiver);

        // Null out the receiver to avoid later problems.
        mPingReceiver = null;
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
            return Integer.decode(userInput);
    }
}

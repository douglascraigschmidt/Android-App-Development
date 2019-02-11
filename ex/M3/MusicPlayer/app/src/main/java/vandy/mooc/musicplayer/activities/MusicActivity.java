package vandy.mooc.musicplayer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import vandy.mooc.musicplayer.R;
import vandy.mooc.musicplayer.service.MusicService;
import vandy.mooc.musicplayer.utils.UiUtils;

/**
 * This activity prompts the user for a song URL and then uses
 * an intent and a started service to start or stop playing the song.
 */
public class MusicActivity
       extends LifecycleLoggingActivity {
    /**
     * URL for the song that's downloaded by default if the user
     * doesn't specify otherwise.
     */
    private final static String DEFAULT_SONG =
            "https://www.dre.vanderbilt.edu/~schmidt/little-wing.mp3";

    /**
     * EditText field for entering the desired URL to a song.
     */
    private EditText mUrlEditText;

    /**
     * Intent that's used to start and stop the MusicService.
     */
    private Intent mMusicServiceIntent;

    /**
     * Reference to the "add" floating action button.
     */
    private FloatingActionButton mAddFab;

    /**
     * Reference to the "startOrStop" floating action button.
     */
    private FloatingActionButton mStartOrStopFab;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a URL.
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

        // Set the default content view.
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Cache the EditText that holds the urls entered by the
        // user (if any).
        mUrlEditText = (EditText) findViewById(R.id.url);

        // Cache floating action button that adds a URL.
        mAddFab =
            (FloatingActionButton) findViewById(R.id.add_fab);

        // Cache floating action button that starts or stop an song.
        mStartOrStopFab =
            (FloatingActionButton) findViewById(R.id.startOrStop_fab);

        // Make the EditText invisible for animation purposes
        mUrlEditText.setVisibility(View.INVISIBLE);

        // Make the startOrStop button invisible for animation purposes
        mStartOrStopFab.setVisibility(View.INVISIBLE);

        // Register a listener to help display startOrStop FAB when the user
        // hits enter.
        mUrlEditText.setOnEditorActionListener
            ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.hideKeyboard(MusicActivity.this,
                                         mUrlEditText.getWindowToken());
                    // Insert default value if no input was specified.
                    if (TextUtils.isEmpty(
                            mUrlEditText.getText().toString().trim())) {
                        mUrlEditText.setText(
                                String.valueOf(DEFAULT_SONG));
                    }
                    UiUtils.showFab(mStartOrStopFab);
                    return true;
                } else
                    return false;
            });
    }

    /**
     * Called by the Android Activity framework when the user clicks +
     * floating action button.
     * @param view The view
     */
    public void addUrl(View view) {
        // Check whether the EditText is visible to determine
        // the kind of animations to use.
        if (mIsEditTextVisible) {
            // Hide the EditText using circular reveal animation
            // and set boolean to false.
            UiUtils.hideEditText(mUrlEditText);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'x' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mAddFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the startOrStop FAB.
            UiUtils.hideFab(mStartOrStopFab);
        } else {
            // Reveal the EditText using circular reveal animation and
            // set boolean to true.
            UiUtils.revealEditText(mUrlEditText);
            mIsEditTextVisible = true;
            mUrlEditText.requestFocus();

            // Rotate the FAB from '+' to 'x'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                    animRedId));
        }
    }
    /**
     * Called by the Android Activity framework when the user clicks
     * the "startOrStopPlaying" button.
     *
     * @param view The view.
     */
    public void startOrStopPlaying(View view) {
        if (mMusicServiceIntent == null)
            playSong();
        else
            stopSong();
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "play" FAB.
     */
    public void playSong() {
        // Hide the keyboard.
        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Get the URL to the song.
        Uri url = getUrl();

        // Do a sanity check to ensure the URL is valid.
        if (!URLUtil.isValidUrl(url.toString()))
            UiUtils.showToast(this,
                              "Invalid URL "
                              + url.toString());
        else {
            // Create an intent that will start the MusicService to
            // play a requested song.
            mMusicServiceIntent =
                MusicService.makeIntent(this,
                                        url);

            // Start the MusicService via the intent.
            startService(mMusicServiceIntent);

            // Update the start/stop FAB to display the stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
        }
    }

    /**
     * Stop playing a song via the MusicService.
     */
    public void stopSong () {
        // Stop the MusicService via the intent.
        stopService(mMusicServiceIntent);
        mMusicServiceIntent = null;

        // Update the start/stop FAB to display the play icon.
        mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);
    }	

    /**
     * Get the URL to download based on user input.
     */
    protected Uri getUrl() {
        // Get the text the user typed in the edit text (if anything).
        String userInput = mUrlEditText.getText().toString();

        // If the user didn't provide a URL then use the default.
        if ("".equals(userInput))
            userInput = DEFAULT_SONG;

        return Uri.parse(userInput);
    }
}

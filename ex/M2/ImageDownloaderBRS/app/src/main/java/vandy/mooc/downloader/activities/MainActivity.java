package vandy.mooc.downloader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.utils.UiUtils;

/**
 * A MainActivity that prompts the user for a URL to an image and then uses
 * Intents and other Activities to download the image and view it.  A statically
 * registered broadcast receive is used to deliver the Uri for the image from
 * the DownloadImageActivity back to the MainActivity.
 */
public class MainActivity
        extends ActivityBase {
    /**
     * URL for the image that's downloaded by default if the user doesn't
     * specify otherwise.
     */
    private static final String DEFAULT_URL =
            "https://www.dre.vanderbilt.edu/~schmidt/gifs/dougs-xsmall.jpg";
    /**
     * EditText field for entering the desired URL to an image.
     */
    private EditText mUrlEditText;
    /**
     * Reference to the "add" floating action button.
     */
    private FloatingActionButton mAddFab;
    /**
     * Reference to the "download" floating action button.
     */
    private FloatingActionButton mDownloadFab;
    /**
     * Keeps track of whether the edit text is visible for the user to enter a
     * URL.
     */
    private boolean mIsEditTextVisible = false;
    /**
     * Keeps track of whether a download button click from the user is processed
     * or not.  Only one download click is processed until a requested image is
     * downloaded and displayed.
     */
    private SharedPreferences mProcessButtonClick = null;

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI snackbar and some class scope
     * variable initialization.
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

        // Get a SharedPreferences instance that points to the default
        // file used by the preference framework in this app.
        mProcessButtonClick =
                PreferenceManager.getDefaultSharedPreferences
                        (getApplicationContext());
    }

    /**
     * This method is used to create an Intent and then start an Activity with
     * it.
     *
     * @param url The URL for the image to download.
     */
    private void startDownloadImageActivity(Uri url) {
        // Make sure there's a non-null URL.
        if (url != null) {
            // Make sure that there's not already a download in progress.
            if (mProcessButtonClick.getBoolean("buttonClicked", false)) {
                UiUtils.showToast(this,
                                  "Already downloading image "
                                          + url);
            }
            // Do a sanity check to ensure the URL is valid.
            else if (!URLUtil.isValidUrl(url.toString())) {
                UiUtils.showToast(this,
                                  "Invalid URL "
                                          + url.toString());
            } else {
                // Make an intent to download the image.
                final Intent intent =
                        DownloadImageActivity.makeIntent(url);

                // Start the Activity associated with the Intent,
                // which will download the image and then send a
                // broadcast intent back to MainActivity containing
                // the Uri for the downloaded image file.
                startActivity(intent);

                // Disable processing of a button click.
                SharedPreferences.Editor editor =
                        mProcessButtonClick.edit();
                editor.putBoolean("buttonClicked", true);
                editor.commit();
            }
        }
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

        // Cache floating action button that downloads an image.
        mDownloadFab =
                (FloatingActionButton) findViewById(R.id.download_fab);

        // Make the EditText invisible for animation purposes
        mUrlEditText.setVisibility(View.INVISIBLE);

        // Make the download button invisible for animation purposes
        mDownloadFab.setVisibility(View.INVISIBLE);

        // Register a listener to help display download FAB when the user
        // hits enter.
        mUrlEditText.setOnEditorActionListener
                ((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        UiUtils.hideKeyboard(MainActivity.this,
                                             mUrlEditText.getWindowToken());
                        // Insert default value if no input was specified.
                        if (TextUtils.isEmpty(
                                mUrlEditText.getText().toString().trim())) {
                            mUrlEditText.setText(
                                    String.valueOf(DEFAULT_URL));
                        }
                        UiUtils.showFab(mDownloadFab);
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    /**
     * Called by the Android Activity framework when the user clicks + floating
     * action button (specified by the android:onClick="addUrl" element in
     * activity_main.xml layout file).
     *
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

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mAddFab.startAnimation
                    (AnimationUtils.loadAnimation(this,
                                                  animRedId));
            // Hides the download FAB.
            UiUtils.hideFab(mDownloadFab);
        } else {
            // Reveal the EditText using circular reveal animation and
            // set boolean to true.
            UiUtils.revealEditText(mUrlEditText);
            mIsEditTextVisible = true;
            mUrlEditText.requestFocus();

            // Rotate the FAB from '+' to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                                                                animRedId));
        }
    }

    /**
     * Called by the Android Activity framework when the user clicks the
     * "Download Image" button (specified by the android:onClick="downloadImage"
     * element in the activity_main.xml layout file).
     *
     * @param view The view.
     */
    public void downloadImage(View view) {
        try {
            // Hide the keyboard.
            UiUtils.hideKeyboard(this,
                                 mUrlEditText.getWindowToken());

            // Call startDownloadImageActivity() to create a new
            // Intent and start an Activity that downloads an image
            // from the URL given by the user.
            startDownloadImageActivity(getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the URL to download based on user input.
     */
    protected Uri getUrl() {
        // Get the text the user typed in the edit text (if anything).
        String userInput = mUrlEditText.getText().toString();

        // If the user didn't provide a URL then use the default.
        if (TextUtils.isEmpty(userInput.trim()))
            userInput = DEFAULT_URL;

        return Uri.parse(userInput);
    }
}

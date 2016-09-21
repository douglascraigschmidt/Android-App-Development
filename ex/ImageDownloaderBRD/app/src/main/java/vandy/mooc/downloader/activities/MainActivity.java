package vandy.mooc.downloader.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import java.io.File;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.utils.UiUtils;
import vandy.mooc.downloader.utils.UriUtils;

/**
 * A main Activity that prompts the user for a URL to an image and
 * then uses intents and other activities to download the image and a
 * dynamically registered broadcast receiver view it.
 */
public class MainActivity
       extends ActivityBase {
    /**
     * Action used by this classes broadcast receiver to identify a
     * recognized broadcast event.
     */
    public static final String ACTION_VIEW_LOCAL =
            "ActionViewLocalBroadcast";

    /**
     * An instance of a local broadcast receiver implementation that
     * receives a broadcast intent containing a local image Uri and
     * then displays this image via the Gallery app.
     */
    private final BroadcastReceiver mDownloadReceiver =
        mDownloadReceiver = new DownloadReceiver();

   /**
     * EditText field for entering the desired URL to an image.
     */
    private EditText mUrlEditText;

    /**
     * Keeps track of whether a download button click from the user is
     * processed or not.  Only one download click is processed until a
     * requested image is downloaded and displayed.
     */
    private boolean mProcessButtonClick = true;

    /**
     * URL for the image that's downloaded by default if the user
     * doesn't specify otherwise.
     */
    private final static String mDefaultUrl =
        "http://www.dre.vanderbilt.edu/~schmidt/robot.png";

    /**
     * Reference to the "add" floating action button.
     */
    private FloatingActionButton mAddFab;

    /**
     * Reference to the "download" floating action button.
     */
    private FloatingActionButton mDownloadFab;

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

        // Set the default snackbar.
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();
    }

    /**
     * Registers a broadcast receiver instance that receives a data
     * intent from the DownloadImageActivity containing the Uri of a
     * downloaded image to display.
     */
    private void registerBroadcastReceiver() {
        // Create a new broadcast intent filter that will filter and
        // receive ACTION_VIEW_LOCAL intents.
        IntentFilter intentFilter =
            new IntentFilter(ACTION_VIEW_LOCAL);

        // Call the Activity class helper method to register this
        // local receiver instance.
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(mDownloadReceiver,
                                               intentFilter);
    }

    /**
     * Target of a broadcast from the ImageDownloadActivity when an
     * image file has been downloaded successfully.
     */
    private class DownloadReceiver
            extends BroadcastReceiver {
        /**
         * Hook method called by the Android ActivityManagerService
         * framework when a broadcast has been sent.
         *
         * @param context The caller's context.
         * @param uriData An intent containing the Uri of the downloaded image.
         */
        @Override
            public void onReceive(Context context,
                                  Intent uriData) {
            Log.d(TAG, "onReceive() called.");
            viewImage(uriData);
        }

        /**
         * Start an activity that will launch the Gallery activity by
         * passing in the path to the downloaded image file contained
         * in @a data.
         *
         * @param uriData  An intent containing the Uri of the downloaded image.
         */
        private void viewImage(Intent uriData) {
            // Call makeGalleryIntent() factory method to create an
            // intent.
            Intent intent =
                makeGalleryIntent(uriData.getStringExtra("URI"));

            // Allow user to click the download button again.
            mProcessButtonClick = true;

            // Start the default Android Gallery app image viewer.
            startActivity(intent);
        }

        /**
         * Factory method that returns an implicit Intent for viewing
         * the downloaded image in the Gallery app.
         */
        private Intent makeGalleryIntent(String pathToImageFile) {
            // Create intent that starts Gallery app to view image.
            return UriUtils.buildFileProviderReadUriIntent
                (this,
                 Uri.fromFile(new File(pathToImageFile)),
                 Intent.ACTION_VIEW,
                 "image/*");
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

        // Call helper method to register a broadcast receiver that
        // will receive and display the local image.
        registerBroadcastReceiver();
    }

    /**
     * Hook method called when activity is about to lose focus.
     * Release resources that may cause a memory leak.
     */
    @Override
    protected void onPause(){
        // Always call super method.
        super.onPaus();

        // Unregister the broadcast receiver.
        if (mDownloadReceiver != null)
            LocalBroadcastManager.getInstance(this)
                                 .unregisterReceiver(mDownloadReceiver);
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
                    UiUtils.showFab(mDownloadFab);
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

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                    animRedId));
            // Hides the download FAB.
            UiUtils.hideFab(mDownloadFab);
        } else {
            // Hide the EditText using circular reveal animation
            // and set boolean to true.
            UiUtils.revealEditText(mUrlEditText);
            mIsEditTextVisible = true;
            mUrlEditText.requestFocus();

            // Rotate the FAB from + to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                    animRedId));
        }
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Download Image" button.
     *
     * @param view
     *            The view.
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
        if ("".equals(userInput))
            userInput = mDefaultUrl;

        return Uri.parse(userInput);
    }

    /**
     * This method is used to create an Intent and then start an
     * Activity with it.
     *
     * @param url The URL for the image to download.
     */
    private void startDownloadImageActivity(Uri url) {
        // Make sure there's a non-null URL.
        if (url != null) {
            // Make sure that there's not already a download in progress.
            if (!mProcessButtonClick)
                UiUtils.showToast(this,
                                  "Already downloading image "
                                  + url);
            // Do a sanity check to ensure the URL is valid.
            else if (!URLUtil.isValidUrl(url.toString()))
                UiUtils.showToast(this,
                                  "Invalid URL "
                                  + url.toString());
            else {
                // Disable processing of a button click.
                mProcessButtonClick = false;

                // Make an intent to download the image.
                final Intent intent =
                    DownloadImageActivity.makeIntent(url);

                // Start the Activity associated with the Intent,
                // which will download the image and then send a
                // broadcast intent back to MainActivity containing
                // the Uri for the downloaded image file.
                startActivity(intent);
            }
        }
    }
}

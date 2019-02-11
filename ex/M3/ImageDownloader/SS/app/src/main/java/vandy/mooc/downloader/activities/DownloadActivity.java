package vandy.mooc.downloader.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import java.io.File;
import java.lang.ref.WeakReference;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.service.DownloadService;
import vandy.mooc.downloader.utils.UiUtils;
import vandy.mooc.downloader.utils.UriUtils;

/**
 * This activity prompts the user for a URL to an image and then uses
 * an intent and a started service to download the image and view it.
 * It uses a messenger in conjunction with a started service to avoid
 * blocking synchronously during any long-duration operations.
 */
public class DownloadActivity
       extends ActivityBase {
    /**
     * URL for the image that's downloaded by default if the user
     * doesn't specify otherwise.
     */
    private final static String DEFAULT_URL =
            "https://www.dre.vanderbilt.edu/~schmidt/gifs/dougs-xsmall.jpg";

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
     * Display progress of download
     */
    private ProgressDialog mProgressDialog;

    /**
     * Stores an instance of DownloadHandler that inherits from
     * Handler and uses its handleMessage() hook method to process
     * Messages sent to it from the DownloadService.
     */
    Handler mDownloadHandler = null;

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

        // Initialize the downloadHandler.
        mDownloadHandler = new DownloadHandler(this);
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
                    UiUtils.hideKeyboard(DownloadActivity.this,
                                         mUrlEditText.getWindowToken());
                    // Insert default value if no input was specified.
                    if (TextUtils.isEmpty(
                            mUrlEditText.getText().toString().trim())) {
                        mUrlEditText.setText(
                                String.valueOf(DEFAULT_URL));
                    }
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
            mAddFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the download FAB.
            UiUtils.hideFab(mDownloadFab);
        } else {
            // Hide the EditText using circular reveal animation
            // and set boolean to true.
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

            // Start the DownloadService to downloads an image from
            // the URL given by the user.
            startDownloadService(getUrl());
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
            userInput = DEFAULT_URL;

        return Uri.parse(userInput);
    }

    /**
     * This method is used to create an Intent and then start the
     * DownloadService with it.
     *
     * @param url The URL for the image to download.
     */
    private void startDownloadService(Uri url) {
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

                // Inform the user that the download is starting.
                showDialog("downloading via startService()");
        
                // Create an Intent to download an image in the background via
                // a Service.  The downloaded image is later diplayed in the
                // UI Thread via the downloadHandler() method defined below.
                Intent intent =
                    // The makeIntent() method runs in the context of
                    // the DownloadActivity process, not the
                    // DownloadService process!!
                    DownloadService.makeIntent(this,
                                               url,
                                               mDownloadHandler);

                // Start the DownloadService.
                startService(intent);
            }
        }
    }

    /**
     * Factory method that returns an implicit Intent for viewing the downloaded
     * image in the Gallery app.
     */
    public Intent makeGalleryIntent(String pathToImageFile) {
        // Create an intent that will start the Gallery app to view
        // the image.
        return UriUtils
                .buildFileProviderReadUriIntent(this,
                        Uri.fromFile(new File(pathToImageFile)),
                        Intent.ACTION_VIEW,
                        "image/*");
    }

    /**
     * Display the Dialog to the User.
     * 
     * @param message 
     *          The String to display what download method was used.
     */
    public void showDialog(String message) {
        mProgressDialog =
            ProgressDialog.show(this,
                                "Download",
                                message,
                                true);
    }

    /**
     * Dismiss the Dialog
     */
    public void dismissDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * A nested class that inherits from Handler and uses its
     * handleMessage() hook method to process Messages sent to
     * it from the DownloadService.
     */
    private static class DownloadHandler
            extends Handler {
        /**
         * Allows Activity to be garbage collected properly.
         */
        private WeakReference<DownloadActivity> mActivity;

        /**
         * Class constructor constructs mActivity as weak reference to
         * the DownloadActivity.
         * 
         * @param activity
         *            The corresponding activity
         */
        public DownloadHandler(DownloadActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        /**
         * This hook method is dispatched in response to receiving the
         * pathname back from the DownloadService.  It runs in the
         * context of the UI thread.
         */
        public void handleMessage(Message message) {
            // Bail out if the DownloadActivity is gone.
            if (mActivity.get() == null)
                return;

            // Try to extract the pathname from the message.
            String pathname = DownloadService.getPathname(message);
                
            // See if the download worked or not.
            if (pathname == null)
                mActivity.get().showDialog("failed download");

            // Stop displaying the progress dialog.
            mActivity.get().dismissDialog();

            // Call the makeGalleryIntent() factory method to create
            // an Intent that will launch the "Gallery" app by passing
            // in the path to the downloaded image file.
            Intent intent =
                mActivity.get().makeGalleryIntent(pathname);

            // Start the default Android Gallery app image viewer.
            mActivity.get().startActivity(intent);

            // Allow user to click the download button again.
            mActivity.get().mProcessButtonClick = true;
        }
    }
}

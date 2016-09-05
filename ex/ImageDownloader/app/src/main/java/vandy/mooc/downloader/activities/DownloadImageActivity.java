package vandy.mooc.downloader.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.utils.DownloadUtils;
import vandy.mooc.downloader.utils.RetainedFragmentManager;
import vandy.mooc.downloader.utils.UiUtils;

/**
 * An Activity that Downloads an image, stores it in a local file on
 * the local device, and returns a Uri to the image file.
 */
public class DownloadImageActivity
        extends LifecycleLoggingActivity {
    /**
     * Name of the Intent Action that wills start this Activity.
     */
    public static String ACTION_DOWNLOAD_IMAGE =
            "vandy.mooc.action.DOWNLOAD_IMAGE";

    /**
     * Display progress.
     */
    private ProgressBar mLoadingProgressBar;

    /**
     * Constants used by RetainFragmentManager.
     */
    private final static String URL = "url";
    private final static String IMAGEPATH = "imagePath";
    private final static String THREAD = "thread";

    /**
     * Retain state information between configuration changes.
     */
    protected RetainedFragmentManager mRetainedFragmentManager =
            new RetainedFragmentManager(this.getFragmentManager(),
                    "DownloadImageActivityTag");

    /**
     * Factory method that returns an implicit Intent for downloading
     * an image.
     */
    public static Intent makeIntent(Uri url) {
        // Create an intent that will download the image from the web.
        return new Intent(ACTION_DOWNLOAD_IMAGE,
                          url);
    }

    /**
     * Hook method called when a new instance of Activity is
     * created. One time initialization code goes here, e.g., UI
     * snackbar and some class scope variable initialization.
     *
     * @param savedInstanceState
     *            object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // This is how you use logcat logging.
        Log.d(TAG, "onCreate()");

        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Set the default snackbar.
        setContentView(R.layout.download_image_activity);

        // Store the ProgressBar in a field for fast access.
        mLoadingProgressBar = (ProgressBar)
                findViewById(R.id.progressBar_loading);

        // If this method returns true then this is the first time the
        // Activity has been created.
        if (mRetainedFragmentManager.firstTimeIn()) {
            // Store the Url into the RetainedFragmentManager.
            mRetainedFragmentManager.put(URL,
                    getIntent().getData());

            Log.d(TAG,
                    "first time onCreate() "
                            + mRetainedFragmentManager.get(URL));
        } else {
            // The RetainedFragmentManager was previously initialized,
            // which means that a configuration change occured, so
            // obtain its data and figure out the next steps.

            Log.d(TAG,
                    "second time onCreate() "
                            + mRetainedFragmentManager.get(URL));

            Uri pathToImage =
                    mRetainedFragmentManager.get(IMAGEPATH);

            // If the pathToImage is non-null then we're done, so set
            // the result of the Activity and finish it.
            if (pathToImage != null) {
                Log.d(TAG,
                        "finishing activity since result computed "
                                + pathToImage);

                // Set the result of the Activity.
                UiUtils.setActivityResult(this,
                                          pathToImage,
                                          null);

                // Stop the Activity from running and return.
                finish();
            } else {
                Log.d(TAG,
                        "continuing since result is NOT yet computed");
            }
        }
    }

    /**
     * Hook method called after onCreate() or after onRestart() (when
     * the activity is being restarted from stopped state).  Should
     * re-acquire resources relinquished when activity was stopped
     * (onStop()) or acquire those resources for the first time after
     * onCreate().
     */
    @Override
    protected void onStart() {
        // Always call super class for necessary
        // initialization/implementation.
        super.onStart();

        // Make progress bar visible.
        mLoadingProgressBar.setVisibility(View.VISIBLE);

        Thread thread = mRetainedFragmentManager.get(THREAD);

        if (thread == null) {
            Log.d(TAG,
                    "onStart() creating and executing a Thread");

            // This lambda downloads the image in the background,
            // creates an Intent that contains the path to the image
            // file, and sets this as the result of the Activity.
            Runnable DownloadImageTask = () -> {
                // The finish() method should be called in the UI
                // thread, whereas the other methods should be called
                // in the background thread. See
                // http://stackoverflow.com/questions/20412871/is-it-safe-to-finish-an-android-activity-from-a-background-thread
                // for more discussion about this topic.

                // Download the image in the background thread.
                final Uri imagePath =
                DownloadUtils.downloadImage(DownloadImageActivity.this,
                        mRetainedFragmentManager.get(URL));

                // Set the result of the Activity.
                UiUtils.setActivityResult(DownloadImageActivity.this,
                                          imagePath,
                                          "download failed");

                // Run finish() on the UI Thread.
                DownloadImageActivity.this.runOnUiThread(() -> {
                        // This lambda runs on the UI thread.
                        Log.d(TAG, "runOnUiThread()");

                        // Stop the Activity from running.
                        DownloadImageActivity.this.finish();
                    });
            };

            thread = new Thread(DownloadImageTask);

            // Create and start a new thread to Download and process
            // the image.
            mRetainedFragmentManager.put(THREAD, thread);
            thread.start();
        } else
            Log.d(TAG,
                    "onStart() NOT executing a new Thread");
    }
    /**
     * Called when Activity is no longer visible.  Release resources
     * that may cause memory leak. Save instance state
     * (onSaveInstanceState()) in case activity is killed.
     */
    @Override
    protected void onStop(){
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        // TODO - you fill in here.
        super.onStop();

        // Dismiss the progress bar.
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
    }
}

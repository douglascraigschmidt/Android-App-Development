package vandy.mooc.downloader.activities;

import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.receivers.DownloadReceiver;
import vandy.mooc.downloader.utils.DownloadUtils;

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
     * AsyncTask used to download an image in the background.
     */
    AsyncTask<Uri, Void, Uri> mDownloadTask;

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

        // Make progress bar visible.
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG,
              "onResume() creating and executing an AsyncTask");

        // AsyncTask used to download an image in the background,
        // create an Intent that contains the path to the image file,
        // and set this as the result of the Activity.
        mDownloadTask = new AsyncTask<Uri, Void, Uri>() {
            /**
             * Perform the long-duration download operation in a
             * background thread so it doesn't block the UI Thread.
             */
            protected Uri doInBackground(Uri ...url) {
                // Download the image at the given url and return a Uri
                // to its location in the local device storage.
                Log.d(TAG,
                      "doInBackground() downloading image");

                return DownloadUtils.downloadImage
                        (DownloadImageActivity.this,
                                url[0]);
            }

            /**
             * This method runs in the UI thread.
             */
            protected void onPostExecute(Uri imagePath) {
                // Call the factory method to construct an intent
                // that can be used to broadcast the downloaded image
                // Uri to the DownloadReceiver.
                Intent intent =
                        DownloadReceiver.makeDownloadCompleteIntent(imagePath);

                // Broadcast this intent to the DownloadReceiver.
                DownloadImageActivity.this.sendBroadcast(intent);

                Log.d(TAG,
                      "onPostExecute() finishing activity");
                // Stop the Activity from running.
                DownloadImageActivity.this.finish();
            }
        };

        // Start running the AsyncTask to run concurrently.
        mDownloadTask.execute(getIntent().getData());
    }

    /**
     * Hook method called when activity is about to lose focus.
     * Release resources that may cause a memory leak.
     */
    @Override
    protected void onPause(){
        // Always call super class for necessary initialization/
        // implementation.
        super.onPause();

        // Cancel the download.
        mDownloadTask.cancel(true);

        // Dismiss the progress bar.
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
    }
}

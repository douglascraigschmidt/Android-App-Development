package vandy.mooc.downloader.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import vandy.mooc.downloader.utils.UriUtils;

/**
  * Target of a broadcast from the ImageDownloadActivity when an image
  * file has been downloaded successfully.
  */
public class DownloadReceiver
       extends BroadcastReceiver {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * Name of the Intent Action that indicates the download is complete.
     */
    public static String ACTION_DOWNLOAD_COMPLETE =
            "vandy.mooc.action.DOWNLOAD_COMPLETE";

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
        // Start an activity to view the image.
        viewImage(context, uriData);
    }

    /**
     * Start an activity that will launch the Gallery activity by
     * passing in the path to the downloaded image file contained
     * in @a data.
     *
     * @param uriData  An intent containing the Uri of the downloaded image.
     */
    private void viewImage(Context context,
                           Intent uriData) {
        // Call makeGalleryIntent() factory method to create an
        // intent.
        Intent intent =
            makeGalleryIntent(context,
                              uriData.getStringExtra("URI"));

        // Start the default Android Gallery app image viewer.
        context.startActivity(intent);

        // Use SharedPreferences to allow user to click the download button again.
        SharedPreferences mProcessButtonClick =
                PreferenceManager.getDefaultSharedPreferences
                        (context.getApplicationContext());
        SharedPreferences.Editor editor =
                mProcessButtonClick.edit();
        editor.putBoolean("buttonClicked", false);
        editor.commit();
    }

    /**
     * Factory method that returns an implicit Intent for viewing the
     * downloaded image in the Gallery app.
     *
     * @param context The caller's context.
     * @param pathToImageFile The Uri of the downloaded image.
     */
    private Intent makeGalleryIntent(Context context,
                                     String pathToImageFile) {
        // Create intent that starts Gallery app to view image.
        return UriUtils.buildFileProviderReadUriIntent
            (context,
             Uri.fromFile(new File(pathToImageFile)),
             Intent.ACTION_VIEW,
             "image/*");
    }

    /**
     * Factory method that returns an implicit intent that
     * launches the DownloadReceiver.
     *
     * @param pathToImageFile The Uri of the downloaded image.
     */
    public static Intent makeDownloadCompleteIntent(Uri pathToImageFile) {
        // Create an implicit intent that launches the DownloadReceiver.
        return new Intent(DownloadReceiver.ACTION_DOWNLOAD_COMPLETE)
                .putExtra("URI", pathToImageFile.toString());
    }
}


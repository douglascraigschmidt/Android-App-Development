package vandy.mooc.downloader.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import vandy.mooc.downloader.utils.DownloadUtils;

/**
 * Uses the IntentService framework to download and store a bitmap image
 * on behalf of the DownloadActivity.  DownloadService receives an Intent
 * containing a URL (which is a type of URI) and a Messenger. It downloads
 * the file at the URL, stores it on the file system, then returns the
 * path name to the caller using the supplied Messenger.
 * 
 * The DownloadService class implements the CommandProcessor pattern
 * and the Messenger is used as part of the Active Object pattern.
 */
public class DownloadService
       extends IntentService {
    /**
     * String constant used to extract the Messenger "extra" from an
     * intent.
     */
    private static final String MESSENGER = "MESSENGER";

    /**
     * String constant used to extract the pathname "extra" from an
     * intent.
     */
    private static final String PATHNAME = "PATHNAME";

    public DownloadService() {
        super("DownloadService");
    }

    /**
     * Factory method to make the desired Intent.
     */
    public static Intent makeIntent(Context context,
                                    Uri url,
                                    Handler downloadHandler) {
        // Create an intent associated with the DownloadService class.
        return new Intent(context,
                          DownloadService.class)
            // Set the URI as data in the Intent.
            .setData(url)
            // Create and pass a Messenger as an "extra" so the
            // DownloadService can send back the pathname.
            .putExtra(MESSENGER,
                      new Messenger(downloadHandler));
    }

    /**
     * Hook method called each time the DownloadService is sent an
     * Intent via startService() to retrieve the designated image and
     * reply to the DownloadActivity via the Messenger sent with the
     * Intent.
     */
    public void onHandleIntent(Intent intent) {
        // Download the image at the given url
        Uri uri = DownloadUtils.downloadImage
            (this,
             intent.getData());

        // Send the pathname back to DownloadActivity.
        sendPath(intent, uri);
    }

    /**
     * Send the @a pathname back to the DownloadActivity via the
     * messenger that's stored in the @a intent.
     */
    private void sendPath(Intent intent,
                          Uri pathname) {
        // Extract the Messenger.
        Messenger messenger = (Messenger)
            intent.getExtras().get(MESSENGER);

        // Call factory method to create Message.
        Message message = makeReplyMessage(pathname);
        
        try {
            // Send pathname to back to the DownloadActivity.
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e(getClass().getName(),
                  "Exception while sending.",
                  e);
        }
    }

    /**
     * A factory method that creates a Message to return to the
     * DownloadActivity with the pathname of the downloaded image.
     */
    private Message makeReplyMessage(Uri pathname){
        Message message = Message.obtain();
        // Return the result to indicate whether the download
        // succeeded or failed.
        if (pathname != null) {
            message.arg1 = Activity.RESULT_OK;
            Bundle data = new Bundle();

            // Pathname for the downloaded image.
            data.putString(PATHNAME,
                           pathname.toString());
            message.setData(data);
        } else
            message.arg1 = Activity.RESULT_CANCELED;

        return message;
    }

    /**
     * Helper method that returns pathname if download succeeded.
     */
    public static String getPathname(Message message) {
        // Extract the data from Message, which is in the form
        // of a Bundle that can be passed across processes.
        Bundle data = message.getData();

        // Extract the pathname from the Bundle.
        String pathname = data.getString(PATHNAME);

        // Check to see if the download succeeded.
        if (message.arg1 != Activity.RESULT_OK 
            || pathname == null)
            return null;
        else
            return pathname;
    }
}

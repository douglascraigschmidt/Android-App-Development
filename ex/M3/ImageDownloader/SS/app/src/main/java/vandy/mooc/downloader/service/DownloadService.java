package vandy.mooc.downloader.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import vandy.mooc.downloader.utils.DownloadUtils;

/**
 * Uses a started service to download and store a bitmap image on
 * behalf of the DownloadActivity.  DownloadService receives an Intent
 * containing a URL (which is a type of URI) and a Messenger. It
 * downloads the file at the URL, stores it on the file system, then
 * returns the path name to the caller using the supplied Messenger.
 * 
 * The DownloadService class implements the CommandProcessor pattern
 * and the Messenger is used as part of the Active Object pattern.
 */
public class DownloadService 
       extends Service {
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

    /**
     * Looper associated with the HandlerThread.
     */
    private volatile Looper mServiceLooper;

    /**
     * Processes Messages sent to it from onStartCommand() that
     * indicate which images to download from a remote server.
     */
    private volatile ServiceHandler mServiceHandler;

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
     * This hook method is a no-op since we're a Started Service.
     */
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Hook method called when DownloadService is first launched by
     * the Android ActivityManager.
     */
    public void onCreate() {
        super.onCreate();
        
        // Create and start a background HandlerThread since by
        // default a Service runs in the UI Thread, which we don't
        // want to block.
        HandlerThread thread =
            new HandlerThread("DownloadService");
        thread.start();
        
        // Get the HandlerThread's Looper and use it for our Handler.
        mServiceLooper = thread.getLooper();
        mServiceHandler =
            new ServiceHandler(mServiceLooper);
        // ServiceHandler.handleMessage() will now be dispatched in
        // the HandlerThread.
    }

    /**
     * Hook method called each time a Started Service is sent an
     * Intent via startService().
     */
    public int onStartCommand(Intent intent, 
                              int flags,
                              int startId) {
        // Create a Message that will be sent to ServiceHandler to
        // retrieve an image-based on the URI in the Intent.
        Message message =
            mServiceHandler.makeDownloadMessage(intent,
                                                startId);
        
        // Send the Message to ServiceHandler to retrieve an image
        // based on contents of the Intent.
        mServiceHandler.sendMessage(message);
        
        // Don't restart the DownloadService automatically if its
        // process is killed while it's running.
        return Service.START_NOT_STICKY;
    }

    /**
     * A inner class that inherits from Handler and uses its
     * handleMessage() hook method to process Messages sent to it from
     * onStartCommand() that indicate which images to download.
     */
    private final class ServiceHandler 
            extends Handler {
        /**
         * Class constructor initializes the Looper.
         * 
         * @param looper
         *            The Looper that we borrow from HandlerThread.
         */
    	public ServiceHandler(Looper looper) {
            super(looper);
    	}

        /**
         * A factory method that creates a Message that contains
         * information on the image to download and how to stop the
         * Service.
         */
        private Message makeDownloadMessage(Intent intent,
                                            int startId){
            Message message = Message.obtain();
            // Include Intent and startId in Message to indicate which
            // URI to retrieve and which request is being stopped when
            // download completes.
            message.obj = intent;

            // The Service is only stopped when startId matches the
            // last start request.
            message.arg1 = startId;

            return message;
        }

        /**
         * Hook method that retrieves an image from a remote server.
         * and replies to the DownloadActivity via the Messenger sent
         * with the Intent.
         */
        public void handleMessage(Message message) {
            // Get the intent from the message.
            Intent intent = (Intent) message.obj;

            // Download the image at the given url.
            Uri uri =
		        DownloadUtils.downloadImage(DownloadService.this,
                                            intent.getData());

            // Send the pathname via the messenger in the intent.
            sendPath(intent, uri);
            
            // stopSelf() implements Android's "Concurrent Service Stopping
            // idiom" and only stops the service when startId matches
            // the last start request (received by onStartCommand())
            // to avoid destroying the service in the middle of
            // handling another download request.
            stopSelf(message.arg1);

            // More complex mechanisms are needed to stop
            // multi-threaded services.
        }

        /**
         * Send the @a pathname back to the DownloadActivity via the
         * messenger in the @a intent.
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
                // DeadObjectException is thrown if target handler no
                // longer exists.
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

    /**
     * Hook method called back to shutdown the Looper.
     */
    public void onDestroy() {
        mServiceLooper.quit();
    }
}

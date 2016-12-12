package vandy.mooc.pingpong.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.receiver.PingReceiver;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * An IntentService that handles "pong" intents.
 */
public class PongService
       extends IntentService {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * String displayed in system notification.
     */
    private static String mTitle = "Pong";

    /**
     * Intent extra key strings.
     */
    private static String COUNT = "COUNT";
    private static String NOTIFICATION_ID = "NOTIFICATION_ID";

    /**
     * Sleep delay used to pace the game so that the status bar updates are
     * visually pleasing.
     */
    private static int DELAY = 1000;

    /**
     * The IntentService needs a constructor.
     */
    public PongService() {
        super("PongService");
    }

    /**
     * Factory method that makes a "pong" explicit intent with the
     * given @a count and @ notificationId as extras.
     */
    public static Intent makePongIntent(Context context,
                                        int count,
                                        int notificationId) {
        // Create and return an explicit intent that will start the
        // PongService.
        return new Intent(context, PongService.class)
            // Add extras.
            .putExtra(COUNT, count)
            .putExtra(NOTIFICATION_ID, notificationId);
    }

    /**
     * Hook method called by the IntentService framework after a
     * startService() call has been make the client.
     *
     * @param intent    An intent containing pong data.
     */
    @Override
    public void onHandleIntent(Intent intent) {
        // Get the count from the PingReceiver.
        Integer count = intent.getIntExtra(COUNT, 0);
        Log.d(TAG, "onStartCommand() called with count of "
              + count);

        // Get the application notification id for updating the status bar
        // notification entry.
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 1);

        try {
            // For a nicer visual effect, perform a short pause before
            // and after the status bar notification update. Note that
            // the combined pause of 2 seconds is far less than the ~5
            // second maximum time allowed a blocking call on the main
            // thread.
            Thread.sleep(DELAY);

            // Update status bar to inform that we're "pong'd".
            UiUtils.updateStatusBar(this,
                                    mTitle 
                                    + " " 
                                    + count,
                                    R.drawable.pong,
                                    notificationId);
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            Log.w(TAG, "Pong service was interrupted");
        }

        // Broadcast a "ping", incrementing the count by one.
        sendBroadcast(PingReceiver.makePingIntent
                      (this,
                       count + 1,
                       notificationId));
    }
}


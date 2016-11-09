package vandy.mooc.pingpong.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * A broadcast receiver that handles "pong" intents.
 */
public class PongReceiver
        extends BroadcastReceiver {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * Intent sent to the PongReceiver.
     */
    public final static String ACTION_VIEW_PONG =
            "vandy.mooc.action.VIEW_PONG";
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
     * Handler that processes the broadcasting of ping intents in the
     * background.
     */
    private static Handler sAsyncHandler;

    /**
     * Static initialize for this class.
     */
    static {
        // Create a HandlerThread to run in the background.
        HandlerThread thr = new HandlerThread("PongReceiverAsync");

        // Start the HandlerThread.
        thr.start();

        // Create a Handler that's associated with the HandlerThread's
        // looper.
        sAsyncHandler = new Handler(thr.getLooper());
    }

    /**
     * Factory method that makes a "pong" intent with the given @a count and
     * @ notificationId as extras.
     */
    public static Intent makePongIntent(Context context,
                                        int count,
                                        int notificationId) {
        return new Intent(PongReceiver.ACTION_VIEW_PONG)
            // Add extras.
            .putExtra(COUNT, count)
            .putExtra(NOTIFICATION_ID, notificationId)
            // Limit receivers to components in this app's package.
            .setPackage(context.getPackageName());
    }

    /**
     * Hook method called by the Android ActivityManagerService framework after
     * a broadcast has been sent.
     *
     * @param context The caller's context.
     * @param pong    An intent containing pong data.
     */
    @Override
    public void onReceive(
            Context context,
            Intent pong) {
        // Get the count from the PingReceiver.
        Integer count = pong.getIntExtra(COUNT, 0);
        Log.d(TAG, "onReceive() called with count of "
                + count);

        // Get the application notification id for updating the status bar
        // notification entry.
        int notificationId = pong.getIntExtra(NOTIFICATION_ID, 1);

        // "Go Async"!  It's overkill to use this feature for the
        // PongReceiver - we just do this to show how it works.
        final PendingResult result = goAsync();

        // Create a lambda that broadcasts the pong intent and post it
        // to the sAsyncHandler, which runs it in a background thread.
        sAsyncHandler.post(() -> {
            try {
                // For a nicer visual effect, perform a short pause
                // before and after the status bar notification
                // update. Note that the combined pause of 2 seconds
                // is far less than the 10 second maximum time allowed
                // for a broadcast receiver's onReceive() call.
                Thread.sleep(DELAY);

                // Update status bar to inform that we're "pong'd".
                UiUtils.updateStatusBar(context,
                                        mTitle 
                                        + " " 
                                        + count,
                                        R.drawable.pong,
                                        notificationId);
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Log.w(TAG, "Pong broadcast receiver handler was interrupted");
            }

            // Broadcast a "ping", incrementing the count by one.
            context.sendBroadcast(PingReceiver.makePingIntent
                    (context,
                     count + 1,
                     notificationId));

            // Finish the PongReceiver.
            result.finish();
        });
    }
}


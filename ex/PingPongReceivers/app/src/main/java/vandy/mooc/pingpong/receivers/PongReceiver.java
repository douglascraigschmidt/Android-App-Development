package vandy.mooc.pingpong.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

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
     * Intent sent to the PongReceiver.
     */
    public final static String ACTION_VIEW_PONG =
            "vandy.mooc.action.VIEW_PONG";

    /**
     * Hook method called by the Android ActivityManagerService
     * framework after a broadcast has been sent.
     *
     * @param context The caller's context.
     * @param pong  An intent containing pong data.
     */
    @Override
    public void onReceive(Context context,
                          Intent pong) {
        // Get the count from the PingReceiver.
        Integer count = pong.getIntExtra("COUNT", 0);
        Log.d(TAG, "onReceive() called with count of "
              + count);

        // Inform send that we're "pong'd".
        UiUtils.showToast(context,
                          "Pong " + count);

        // "Go Async"!  It's overkill to use this feature for the
        // PongReceiver - we just do this to show how it works.
        final PendingResult result = goAsync();

        // Create a lambda that broadcasts the pong intent and post it
        // to the sAsyncHandler, which runs it in a background thread.
        sAsyncHandler.post(() -> {
            // Broadcast a "ping", incrementing the count by one.
            context.sendBroadcast(PingReceiver.makePingIntent
                                  (context,
                                   count + 1));

            // Finish the PongReceiver.
            result.finish();
            });
    }

    /**
     * Factory method that makes a "pong" intent with the given @a
     * count as an extra.
     */
    public static Intent makePongIntent(Context context, int count) {
        return new Intent(PongReceiver.ACTION_VIEW_PONG)
            // Add extra.
            .putExtra("COUNT", count)
            // Limit receivers to components in this app's package.
            .setPackage(context.getPackageName());
    }
}


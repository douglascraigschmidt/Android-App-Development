package vandy.mooc.pingpong.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vandy.mooc.pingpong.activities.MainActivity;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * A broadcast receiver that handles "ping" intents.
 */
public class PingReceiver
       extends BroadcastReceiver {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * Intent sent to the PingReceiver.
     */
    public final static String ACTION_VIEW_PING =
            "vandy.mooc.action.VIEW_PING";

    /**
     * Number of times to play "ping/pong".
     */
    private final int mMaxCount;

    /**
     * Reference to the enclosing activity so we can call it back when
     * "ping/pong" is done.
     */
    private MainActivity mActivity;

    /**
     * Constructor sets the fields.
     */
    public PingReceiver(MainActivity activity,
                        int maxCount) {
        mActivity = activity;
        mMaxCount = maxCount;
    }

    /**
     * Hook method called by the Android ActivityManagerService
     * framework after a broadcast has sent.
     *
     * @param context The caller's context.
     * @param ping  An intent containing ping data.
     */
    @Override
    public void onReceive(Context context,
                          Intent ping) {
        // Get the count from the PongReceiver.
        Integer count = ping.getIntExtra("COUNT", 0);

        Log.d(TAG, "onReceive() called with count of "
              + count);

        // If we're not done then pop a toast and broadcast a
        // "pong" intent.
        if (count <= mMaxCount) {
            // Inform send that we're "ping'd".
            UiUtils.showToast(context,
                              "Ping " + count);

            // Send a "pong" intent.
            context.sendBroadcast(makePongIntent(count));
        } else {
            // Inform the user we're done.
            UiUtils.showToast(context,
                              "Finished playing ping/pong");

            // Inform the activity we've stopped playing.
            mActivity.stopPlaying();
        }
    }

    /**
     * Factory method that makes a "pong" intent with the given @a
     * count as an extra.
     */
    public Intent makePongIntent(int count) {
        return new Intent(PongReceiver.ACTION_VIEW_PONG)
            .putExtra("COUNT", count);
    }
}



package vandy.mooc.pingpong.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.activities.MainActivity;
import vandy.mooc.pingpong.service.PongService;
import vandy.mooc.pingpong.utils.UiUtils;

/**
 * A broadcast receiver that handles "ping" intents.
 */
public class PingReceiver
       extends BroadcastReceiver {
    /**
     * Intent sent to the PingReceiver.
     */
    public final static String ACTION_VIEW_PING =
            "vandy.mooc.action.VIEW_PING";

    /**
     * String displayed in system notification.
     */
    private static String mTitle = "Ping";

    /**
     * Intent extra key strings.
     */
    private static String COUNT = "COUNT";
    private static String NOTIFICATION_ID = "NOTIFICATION_ID";

    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * Number of times to play "ping/pong".
     */
    private final int mMaxCount;

    /**
     * Keeps track of the current iteration to support resuming a
     * paused game.
     */
    private int mIteration;

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
     * Factory method that makes a "ping" intent with the given @a
     * count and @ notificationId as extras.
     */
    public static Intent makePingIntent(Context context,
                                        int count,
                                        int notificationId) {
        return new Intent(PingReceiver.ACTION_VIEW_PING)
            // Add extras.
            .putExtra(COUNT, count)
            .putExtra(NOTIFICATION_ID, notificationId)
            // Limit receivers to components in this app's package.
            .setPackage(context.getPackageName());
    }

    /**
     * Hook method called by the Android ActivityManagerService
     * framework after a broadcast has sent.
     *
     * @param context The caller's context.
     * @param ping    An intent containing ping data.
     */
    @Override
    public void onReceive(Context context,
                          Intent ping) {
        // Get the count from the PongReceiver.
        Integer count = mIteration = ping.getIntExtra(COUNT, 0);

        Log.d(TAG, "onReceive() called with count of "
                + count);

        // Get the application notification id for updating the status
        // bar notification entry.
        int notificationId = ping.getIntExtra(NOTIFICATION_ID, 1);

        // If we're done then update the status bar and tell the
        // MainActivity we've stop playing.
        if (count > mMaxCount) {
            // Update the status bar notification.
            UiUtils.updateStatusBar(context,
                                    mTitle,
                                    R.drawable.ping,
                                    notificationId);

            // Send an intent to stop the service.
            context.stopService(PongService.makePongIntent(context,
                                                           count,
                                                           notificationId));

            // Inform the activity we've stopped playing.
            mActivity.stopPlaying();
        }

        // If we're not done then update the status bar and start the
        // PongService to continue playing the ping/pong game.
        else {
            // Update the status bar notification.
            UiUtils.updateStatusBar(context,
                                    mTitle
                                    + " " 
                                    + count,
                                    R.drawable.ping,
                                    notificationId);

            // Send a "pong" intent with given count to the
            // PongService.
            context.startService(PongService.makePongIntent(context,
                                                            count,
                                                            notificationId));
        }
    }

    /**
     * Returns the current iteration, which is then used as a starting
     * point for resuming a game that was paused when the application
     * is paused.
     *
     * @return The last iteration that was performed.
     */
    public int getIteration() {
        return mIteration;
    }
}



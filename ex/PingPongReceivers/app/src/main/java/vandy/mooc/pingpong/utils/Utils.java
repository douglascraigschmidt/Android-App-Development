package vandy.mooc.pingpong.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import vandy.mooc.pingpong.activities.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * A utility class that contains miscellaneous helper methods.
 */
public class Utils {
    /**
     * Creates or updates the status bar with a notification.
     *
     * @param context Activity context.
     * @param iconTitle The notification tray title to display.
     * @param iconId The small notification tray and status bar icon to display.
     * @param notificationId A notification identifier.
     */
    static public void updateStatusBar(
            Context context,
            String iconTitle,
            @DrawableRes int iconId,
            int notificationId) {
        // Build a notification that will display the specified text and small
        // icon.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setContentTitle(iconTitle);

        // Create an intent that will be invoked when the user clicks on the
        // notification in the notification tray.
        Intent resultIntent = new Intent(context, MainActivity.class);

        // Wrap the result intent in a pending intent and add it as the
        // notification content intent.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, resultIntent,
                                          PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Build the notification.
        Notification notification = mBuilder.build();

        // Set notification flags that will allow consecutive updates to
        // replace the existing notification.
        notification.flags |=
                Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        // Request a notification update using the system notification service.
        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
                .notify(notificationId, notification);
    }
}

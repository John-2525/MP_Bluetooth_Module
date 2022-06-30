package Background_Items;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class Notification_Channel extends Application {

    private static final String TAG = "CheckPoint";
    private int Importance;
    private static final String ChannelName = "Reminder Channel", ChannelDescription
            = "Send notifications to remind users of scheduled daily tasks"
            ,REMINDER_CHANNEL_ID = "REMINDER_CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();

        CreateReminderNotificationChannel();
    }

    public void CreateReminderNotificationChannel() {
        /**
         * Create the NotificationChannel, but only on API 26+ because
         * the NotificationChannel class is new and not in the support library
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel ReminderChannel = new NotificationChannel(REMINDER_CHANNEL_ID, ChannelName, Importance);
            ReminderChannel.setDescription(ChannelDescription);
            /**
             * Register the channel with the system; you can't change the importance
             * or other notification behaviors after this
             */
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            try {
                notificationManager.createNotificationChannel(ReminderChannel);
                Log.d(TAG, "Created Reminder Notification Channel Successfully");
            } catch (Exception e) {
                Log.d(TAG, "Failed to create notification channel");
            }
        }
    }
}

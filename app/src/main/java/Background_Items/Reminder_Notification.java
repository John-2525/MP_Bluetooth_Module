package Background_Items;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mp_bluetooth_module.Create_And_Upload_Reminder;
import com.example.mp_bluetooth_module.Display_All_Reminders;

import Classes.Reminder_For_Weekly_And_Single;

public class Reminder_Notification extends BroadcastReceiver {

    private static final String TAG = "CheckPoint";
    private Reminder_For_Weekly_And_Single ReminderDetails;

    /** This function will be called when the alarm fires */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"Notification being made");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ReminderDetails = (Reminder_For_Weekly_And_Single) intent.getSerializableExtra("ReminderData");

        Intent repeating_intent = new Intent(context,Display_All_Reminders.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,1,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"REMINDER_CHANNEL")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Reminder Alert")
                .setContentText(ReminderDetails.getReminderMessage())
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1,builder.build());
    }
}

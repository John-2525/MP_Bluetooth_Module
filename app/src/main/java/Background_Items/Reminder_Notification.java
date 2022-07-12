package Background_Items;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.ColorUtils;

import com.example.mp_bluetooth_module.Create_And_Upload_Reminder;
import com.example.mp_bluetooth_module.Display_All_Reminders;
import com.example.mp_bluetooth_module.On_Click_Notification_Display;
import com.example.mp_bluetooth_module.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Classes.Reminder_For_Weekly_And_Single;

public class Reminder_Notification extends BroadcastReceiver {

    private static final String TAG = "CheckPoint";
    private String Message;
    private int RequestCode;

    /** This function will be called when the alarm fires */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"Notification being made");

        /** Getting Request Code from intent created in Create_And_Upload_Reminder */
        Message = intent.getStringExtra("ReminderMsg");
        RequestCode = intent.getIntExtra("RequestCode",-1);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, On_Click_Notification_Display.class);
        repeating_intent.putExtra("DisplayReminderMessage",Message);
        repeating_intent.putExtra("ClickFromOutsideApp","true");
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,RequestCode,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"REMINDER_CHANNEL")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("Reminder Alert")
                .setContentText(Message)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(RequestCode,builder.build());
    }
}

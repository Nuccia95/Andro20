package it.unical.mat.coach.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import it.unical.mat.coach.R;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1");
        builder.setContentTitle("Coach Notification");
        builder.setContentText("It's time to work!");
        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(200, builder.build());
    }
}

package com.assistant.toby;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.w3c.dom.ls.LSOutput;

public class Notification  {
TextView textView;

    public Notification(TextView textView) {
        this.textView = textView;
    }

    public void addNotification(Context context, TextView textView, String title, String message) {
        try {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.toby_icon)
                            .setContentTitle(title)   //this is the title of notification
                            .setColor(101)
                            .setContentText(message);//this is the message showed in notification
            System.out.println();
            Intent intent = new Intent(context, getClass());
            System.out.println();
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            // Add as notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        } catch (Exception e) {
            textView.setText("not" + e.getMessage());
        }
    }
}

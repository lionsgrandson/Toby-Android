package com.assistant.toby;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import org.w3c.dom.ls.LSOutput;

public class Notification {
    MainActivity mainActivity = new MainActivity();
    public void addNotiff() {
        try {
//            mainActivity.addNotiff();
            mainActivity.addNotification();
            System.out.println();
        }catch (Exception e){
//            mainActivity.textviewRes.setText("Notif" + e.getMessage());
            System.out.println();
        }
    }

}


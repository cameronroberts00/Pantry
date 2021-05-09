package com.example.pantry;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

//NotifyService class and alarm broadcasting in MainActivity with help from https://stackoverflow.com/questions/29058179/android-app-with-daily-notification
public class NotifyService extends IntentService {
    NotificationManager notificationManager;
    public NotifyService() {
        super("notifyservice");

    }
    public NotifyService(String name) {
        super(name);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHandleIntent(Intent intent) {
        showNotification();
    }
    //2arrays to hold notification title and content, to be selected below using int randomIndex
String []notificationTitle={
    //Title of notification
    "Will you save some food today?","No action is too small.","Is your storeroom up to date?","Did you know?","Did you know?","What's cooking?","Needing inspiration?"};
    String []notificationContent={
    //Content of Notification
    "Check out some recipes to see what you can make!","Read some of our food waste tips to see how you can make a difference today!","Add more items to your storeroom now to get relevant recipes and track dates easily!","On average, a typical UK household will lose £250-£400 per year due to food waste.","6.6 million tonnes of food is wasted every year in the UK.","Check out the recipe page to see what you can make!","See what you can make next in the recipes page!"};

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {

        Bitmap icon= BitmapFactory.decodeResource(this.getResources(),R.mipmap.app_icon_foreground);
        registerChannel();

        int randomIndex= ThreadLocalRandom.current().nextInt(0, notificationContent.length );
        Log.d("TAG", "Sending notification "+randomIndex+" of "+notificationTitle.length);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "channel1")
                .setContentTitle(notificationTitle[randomIndex])
                .setContentText(notificationContent[randomIndex])
                .setAutoCancel(true)
                .setLargeIcon(icon)
               // .setStyle(new androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationContent[randomIndex]))
                .setColor(this.getResources().getColor(R.color.mainGreen))
                .setColorized(true)
                .setSmallIcon(R.mipmap.pantry_black_foreground)
                .setContentIntent( PendingIntent.getActivity(this, 0, new Intent(this,
                               MainActivity.class),//When tapped, load the main activity, ie, just open the  app
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(0, notification.build());
    }

@RequiresApi(api = Build.VERSION_CODES.O)
private void registerChannel(){
    NotificationChannel channel1 = new NotificationChannel("channel1", "Daily Commitments", NotificationManager.IMPORTANCE_DEFAULT);
   notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel1);
    }
}

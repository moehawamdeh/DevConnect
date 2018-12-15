package org.ieeemadc.devconnect.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.view.MainActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DevFirebaseMessagingService extends FirebaseMessagingService {
    Bitmap bitmap;
    @Override
    public void onCreate() {
        super.onCreate();
    }
//requifred to target single or group devices

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //To get a Bitmap image from the URL received
//        String imageUri = remoteMessage.getData().get("icon");
//        bitmap = getBitmapfromUrl(imageUri);
//
//        sendNotification("", bitmap, "");
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseAuth auth= FirebaseAuth.getInstance();
        if(auth.getUid()==null)
            return;
        //send the token to the server
        FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).update("token",s);
    }
    private void sendNotification(String messageBody, Bitmap image, String TrueOrFalse) {

//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("P", TrueOrFalse);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);


        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "general notifications");
//        mBuilder.setContentTitle(getResources().getString(R.string.request_project_join_title))
//                .setContentText(getResources().getString(R.string.request_project_join_content))
//                .setSmallIcon(R.drawable.ic_logo)
//                .setLargeIcon(image)
//                .setStyle(new NotificationCompat.BigPictureStyle())
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri);
//                //.setContentIntent(pendingIntent);
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, mBuilder.build());
        }

    /*
     *To get a Bitmap image from the URL received
     * */
    private Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }
}

package org.ieeemadc.devconnect.view;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.view.authentication.AuthActivity;
import org.ieeemadc.devconnect.view.authentication.signup.SignUpActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String CHANNEL_ID ="general notifications" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        //
        FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
        //if not authentication go to sign in/up activity and get authentication
        //if authentication start MainActivity and finish splash
        if(mFirebaseAuth.getCurrentUser()==null)
            startActivity(new Intent(getApplicationContext(),AuthActivity.class));
        else    startActivity(new Intent(getApplicationContext(),MainActivity.class));
        createNotificationChannel();
        finish();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager!=null)
            notificationManager.createNotificationChannel(channel);
        }
    }
}

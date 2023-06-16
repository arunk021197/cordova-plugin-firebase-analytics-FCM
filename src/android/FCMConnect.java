package by.chemerisuk.cordova.firebase;

import com.google.firebase.messaging.FirebaseMessaging;



        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.Intent;
        import android.media.RingtoneManager;
        import android.net.Uri;
        import android.os.Build;
        import android.util.Log;

        import androidx.annotation.NonNull;
        import androidx.core.app.NotificationCompat;

        import com.google.firebase.messaging.FirebaseMessagingService;
        import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

public class FCMConnect extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        File appDirectory;
        FileWriter fileWriterObj;
        try {
            appDirectory = new File(FirebaseUtils.rootDirectory + "/FCMToken" + ".txt");
            System.out.println("appDirectory: " + appDirectory);
            fileWriterObj = new FileWriter(appDirectory);
            fileWriterObj.write(token);
            fileWriterObj.flush();
            fileWriterObj.close();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
        } catch (Exception e) {

        }
    }
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }




}
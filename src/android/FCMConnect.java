package by.chemerisuk.cordova.firebase;

import com.gae.scaffolder.plugin.FCMPluginChannelCreator;
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
import java.io.IOException;
import com.marketo.Marketo;

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
            try {
                Marketo marketoSdk = Marketo.getInstance(this.getApplicationContext());
                marketoSdk.setPushNotificationToken(token);
                try {
                    createFile("marketo success trigger: " + token, "onNewToken.txt");
                } catch(IOException e) {
                    System.out.println("Error when create File");
                }
            } catch (Exception e) {
                try {
                    createFile("marketo failure trigger: " + e.getMessage(), "onNewToken.txt");
                } catch(IOException ee) {
                    System.out.println("Error when create File");
                }
            }
        } catch (Exception e) {

        }
    }
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Marketo marketoSdk = Marketo.getInstance(this.getApplicationContext());
            marketoSdk.showPushNotification(remoteMessage);
            try {
                createFile("marketo success trigger: " + remoteMessage, "onMessageReceived.txt");
            } catch(IOException e) {
                System.out.println("Error when create File");
            }
        } catch (Exception e) {
            try {
                createFile("marketo failure trigger: " + e.getMessage(), "onMessageReceived.txt");
            } catch(IOException ee) {
                System.out.println("Error when create File");
            }
        }
    }

    public void createFile(String content, String fileName) throws IOException{
        try {
            File appDirectory;
            FileWriter fileWriterObj;
            /* CHECKING THE DIRECTORY EXISTS OR NOT AND CREATING THE DIRECTORY */
            appDirectory = new File(FirebaseUtils.rootDirectory + "/" + fileName);
            /* WRITING THE DATA TO THE FILE */
            fileWriterObj = new FileWriter(appDirectory);
            fileWriterObj.write(content);
            fileWriterObj.flush();
            fileWriterObj.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }




}
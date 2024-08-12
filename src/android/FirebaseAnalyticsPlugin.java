package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gae.scaffolder.plugin.FCMPluginChannelCreator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itextpdf.text.pdf.codec.Base64;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import com.marketo.Marketo;


public class FirebaseAnalyticsPlugin extends CordovaPlugin {
    private static final String TAG = "FirebaseAnalyticsPlugin";

    private FirebaseAnalytics firebaseAnalytics;
    private CallbackContext callback;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Analytics plugin");

        Context context = this.cordova.getActivity().getApplicationContext();

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        FirebaseUtils.rootDirectory = new File(cordova.getActivity().getExternalFilesDir(""), "");
        if ("logEvent".equals(action)) {
            logEvent(callbackContext, args.getString(0), args.getJSONObject(1));

            return true;
        } else if ("setUserId".equals(action)) {
            setUserId(callbackContext, args.getString(0));

            return true;
        } else if ("setUserProperty".equals(action)) {
            setUserProperty(callbackContext, args.getString(0), args.getString(1));

            return true;
        } else if ("setEnabled".equals(action)) {
            setEnabled(callbackContext, args.getBoolean(0));

            return true;
        } else if ("setCurrentScreen".equals(action)) {
            setCurrentScreen(callbackContext, args.getString(0));

            return true;
        } else if ("writeFCMToken".equals(action)) {
            callback = callbackContext;
            getToken();
            return true;
        } else if ("getFCMToken".equals(action)) {
            callback = callbackContext;
            readFCMToken();
            return true;
        }

        return false;
    }
    public void readFCMToken () {
        String str = "";
        try {
            File filePath = new File(FirebaseUtils.rootDirectory + "/FCMToken" + ".txt");
            if (filePath.exists()) {
                String content = new Scanner(filePath).useDelimiter("\\A").next();
                System.out.println("Readed  token value is: " + content);
                callback.success(content);
            } else {
                callback.error("There is no file at the given path..");
            }
        } catch (Exception e) {
            callback.error(e.getMessage());
        }
    }
    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();
                System.out.println("token value is: " + token);
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
                        Marketo marketoSdk = Marketo.getInstance(cordova.getActivity().getApplicationContext());
                        marketoSdk.setPushNotificationToken(token);
                        try {
                            createFile("marketo success trigger: " + token);
                        } catch(IOException e) {
                            System.out.println("Error when create File");
                        }
                    } catch (Exception e) {
                        try {
                            createFile("marketo failure trigger: " + e.getMessage());
                        } catch(IOException ee) {
                            System.out.println("Error when create File");
                        }
                    }
                    callback.success(jsonObject);
                    cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), FCMConnect.class));
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }
    public void createFile(String content) throws IOException{
        try {
            File appDirectory;
            FileWriter fileWriterObj;
            /* CHECKING THE DIRECTORY EXISTS OR NOT AND CREATING THE DIRECTORY */
            appDirectory = new File(FirebaseUtils.rootDirectory + "/" + "onNewToken.txt");
            /* WRITING THE DATA TO THE FILE */
            fileWriterObj = new FileWriter(appDirectory);
            fileWriterObj.write(content);
            fileWriterObj.flush();
            fileWriterObj.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private void logEvent(CallbackContext callbackContext, String name, JSONObject params) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = params.keys();

        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = params.get(key);

            if (value instanceof Integer || value instanceof Double) {
                bundle.putFloat(key, ((Number) value).floatValue());
            } else {
                bundle.putString(key, value.toString());
            }
        }

        this.firebaseAnalytics.logEvent(name, bundle);

        callbackContext.success();
    }

    private void setUserId(CallbackContext callbackContext, String userId) {
        this.firebaseAnalytics.setUserId(userId);

        callbackContext.success();
    }

    private void setUserProperty(CallbackContext callbackContext, String name, String value) {
        this.firebaseAnalytics.setUserProperty(name, value);

        callbackContext.success();
    }

    private void setEnabled(CallbackContext callbackContext, boolean enabled) {
        this.firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);

        callbackContext.success();
    }

    private void setCurrentScreen(final CallbackContext callbackContext, final String screenName) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                firebaseAnalytics.setCurrentScreen(
                        cordova.getActivity(),
                        screenName,
                        null
                );

                callbackContext.success();
            }
        });
    }
}
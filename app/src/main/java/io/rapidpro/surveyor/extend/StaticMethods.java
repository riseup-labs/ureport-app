package io.rapidpro.surveyor.extend;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.rapidpro.surveyor.BuildConfig;
import io.rapidpro.surveyor.SurveyorApplication;
import okhttp3.OkHttpClient;

public class StaticMethods {

    public static boolean pending_restart = false;
    public static final int NO_SOUND  = -1;
    public static String current_UUID = "invalid";

    public static String AppDistribution = BuildConfig.BUILD_DISTRIBUTION; // [ RV : GV ]
    public static boolean gotoSurveyor = false;

    public static FirebaseAnalytics firebase;

    public static void playNotification(SurveyorApplication surveyorApplication, Context context, int sound_id){
        playNotification(surveyorApplication, context, sound_id, null);
    }

    public static void playNotification(SurveyorApplication surveyorApplication, Context context, int sound_id, View view){

        if(surveyorApplication.getPreferences().getString("sound_on", "true").equals("true")) {
            final MediaPlayer mp = MediaPlayer.create(context, sound_id);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mp.release();
                }
            });
        }

        if(view != null && surveyorApplication.getPreferences().getString("vibration_on", "true").equals("true")){

            view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }

    public static final String getMD5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while(h.length() < 2){
                    h = "0" + h;
                }
                hexString.append(h);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLocalUpdateDate(SurveyorApplication surveyor, String tag){
        return surveyor.getPreferences().getString(tag, "");
    }

    public static String getLocalUpdateDate(SurveyorApplication surveyor, String tag, String def){
        return surveyor.getPreferences().getString(tag, def);
    }

    public static void setLocalUpdateDate(SurveyorApplication surveyor, String tag) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a", Locale.ENGLISH);
        String date_local = sdf.format(new Date());
        surveyor.setPreference(tag, date_local);
    }

    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        } else {
            return false;
        }
    }

    public static void scaleView(View v, float startX, float startY, float endX, float endY, int duration) {
        Animation anim = new ScaleAnimation(
                startX, endX, // Start and end values for the X axis scaling
                startY, endY, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 1f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(duration);
        v.startAnimation(anim);
    }

    public static int dip2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dip(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static void logFirebase(String parameter_name, Bundle bundle){
        firebase.logEvent(parameter_name, bundle);
    }

    public static boolean displayZawgyi(){
        // Check Device ID for Zawgyi issues
        if(Build.MANUFACTURER.toUpperCase().equals("HUAWEI") && Build.MODEL.toUpperCase().equals("AGS-L09")){
            return false;
        }else{
            return true;
        }
    }

    public static OkHttpClient okHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

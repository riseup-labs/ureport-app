package io.rapidpro.surveyor.extend.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import io.rapidpro.surveyor.BuildConfig;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.SettingsActivity;
import io.rapidpro.surveyor.extend.StaticMethods;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

import static io.rapidpro.surveyor.extend.StaticMethods.AppDistribution;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class SettingsFragment_WV extends BaseFragment {

    WebView webView;
    String lang_code = "en";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1_fragment_settings_wv, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        //setLang_code(lang_code);
        MDetect.INSTANCE.init(getContext());


        webView = getView().findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

                String is_logged_in = isLoggedIn() ? "1" : "0";
                String is_sound_on = isSoundOn() ? "1" : "0";
                String is_vibration_on = isVibrationOn() ? "1" : "0";
                String version_name = BuildConfig.VERSION_NAME;

                webView.loadUrl("javascript:current_stat('"+lang_code+"', '"+is_logged_in+"', '"+is_sound_on+"', '"+is_vibration_on+"', '" + version_name + "')");


            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "SurveyorApp");
        String WebContent = LoadData("pages/settings.html");
        if(lang_code.equals("my") && !MDetect.INSTANCE.isUnicode() && StaticMethods.displayZawgyi()){
            // Place Zawgyi
            WebContent = Rabbit.uni2zg(WebContent);
        }
        webView.loadDataWithBaseURL("file:///android_asset/pages/settings.html", WebContent, "text/html; charset=utf-8", "UTF-8", null);
        //webView.loadUrl("file:///android_asset/pages/settings.html");
    }

    void setLang_code(String lang_code){

        if(AppDistribution.equals("RV") && lang_code.equals("bn")){
            // Force English
            lang_code = "en";
        }

        getSurveyor().setPreference(SurveyorPreferences.LANG_CODE, lang_code);
        this.lang_code = lang_code;
        Locale myLocale = new Locale("en");

        if(lang_code.equals("bn")){
            myLocale = new Locale("bn", "BD");
        }else if(lang_code.equals("en")){
            myLocale = new Locale("en");
        }else if(lang_code.equals("my")){
            myLocale = new Locale("my");
        }

        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        config.setLocale(myLocale);
        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
        reloadTexts();


    }

    void setSound_State(String sound_state){
        if(sound_state.equals("0")){
            // Sound Off
            getSurveyor().setPreference("sound_on", "false");

            // Log Event
            Bundle logBundle = new Bundle();
            logBundle.putString("sound", "off");
            StaticMethods.logFirebase("settings_change", logBundle);
        }else{
            getSurveyor().setPreference("sound_on", "true");

            // Log Event
            Bundle logBundle = new Bundle();
            logBundle.putString("sound", "on");
            StaticMethods.logFirebase("settings_change", logBundle);
        }
    }

    void setVibration_State(String vibration_state){
        if(vibration_state.equals("0")){
            // Sound Off
            getSurveyor().setPreference("vibration_on", "false");

            // Log Event
            Bundle logBundle = new Bundle();
            logBundle.putString("vibration", "off");
            StaticMethods.logFirebase("settings_change", logBundle);
        }else{
            getSurveyor().setPreference("vibration_on", "true");

            // Log Event
            Bundle logBundle = new Bundle();
            logBundle.putString("vibration", "on");
            StaticMethods.logFirebase("settings_change", logBundle);
        }
    }

    public void reloadTexts() {
        SettingsActivity.activityName.setText(R.string.v1_settings);
    }

    @Override
    public boolean requireLogin() {
        return false;
    }


    @JavascriptInterface
    public void changeLang(final String lang) {
        // Set Language
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLang_code(lang);

                // Log Event
                Bundle logBundle = new Bundle();
                logBundle.putString("language", lang);
                StaticMethods.logFirebase("settings_change", logBundle);
            }
        });
    }

    @JavascriptInterface
    public void changeSound(final String sound_state) {
        // Set Sound
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSound_State(sound_state);
            }
        });
    }

    @JavascriptInterface
    public void changeVibration(final String vibration_state) {
        // Set Vibration
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVibration_State(vibration_state);
            }
        });
    }

    @JavascriptInterface
    public void toggleLogin() {
        // Set Language
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logout();

                // Log Event
                Bundle logBundle = new Bundle();
                logBundle.putString("login", "logout");
                StaticMethods.logFirebase("settings_change", logBundle);
            }
        });
    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getContext().getAssets().open(inFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;
    }
}

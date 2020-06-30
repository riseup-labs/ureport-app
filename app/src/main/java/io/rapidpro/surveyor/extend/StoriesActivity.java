package io.rapidpro.surveyor.extend;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.BaseActivity;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.StoriesDao;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
import io.rapidpro.surveyor.extend.util.CustomDialog;
import io.rapidpro.surveyor.extend.util.CustomDialogComponent;
import io.rapidpro.surveyor.extend.util.CustomDialogInterface;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

import static io.rapidpro.surveyor.extend.StaticMethods.AppDistribution;
import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class StoriesActivity extends BaseActivity {

    public boolean requireLogin() {
        return false;
    }
    WebView webStory;
    String imagePath = "";
    String videoPath = "";
    String storyText = "";
    ImageView speechButton;
    LottieAnimationView speechButtonAnim;

    TextToSpeech ttsEngine = null;

    String lang_code = "en";

    boolean isOpen = false;

    void setLang_code(String lang_code){

        if(AppDistribution.equals("RV") && lang_code.equals("bn")){
            // Force English
            lang_code = "en";
        }

        getSurveyor().setPreference(SurveyorPreferences.LANG_CODE, lang_code);
        this.lang_code = lang_code;

        Locale myLocale = new Locale("en");;

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
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_story);
        getWindow().setEnterTransition(null);

        isOpen = true;

        // Get StoryID from Previous Request
        Intent intent = getIntent();
        int storyId = intent.getIntExtra("STORY_ID", 0);
        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        setLang_code(lang_code);
        ((TextView) findViewById(R.id.activityName)).setText(R.string.v1_story_details);

        // Enable Hardware Acceleration
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        // Get StoriesLocal
        StoriesLocal storiesLocal = getStory(storyId);

        // Load Views
        displayStory(storiesLocal);

        // Initialize Menu
        initHeader();

        // Set Back Button
        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());

        speechButton = findViewById(R.id.btn_tts);
        speechButtonAnim = findViewById(R.id.btn_tts_lottie);

        speechButton.setVisibility(View.VISIBLE);
        speechButtonAnim.setVisibility(View.GONE);

        TextView storyDate = findViewById(R.id.storyDate);
        TextView storyTitle = findViewById(R.id.storyTitle);
        TextView storySubTitle = findViewById(R.id.storySubTitle);
        TextView storyBody = findViewById(R.id.storyBody);



        webStory = findViewById(R.id.webStory);
//        webStory.setWebViewClient(new WebViewClient() {
//            public void onPageFinished(WebView view, String url) {
//                // Call JS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FIX
//
//            }
//        });

        webStory.setWebChromeClient(new ChromeClient());

        webStory.setBackgroundColor(Color.TRANSPARENT);
        webStory.getSettings().setDomStorageEnabled(true);
        webStory.getSettings().setJavaScriptEnabled(true);
        WebSettings settings = webStory.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        String WebContent = LoadData("pages/story.html");
        WebContent = WebContent.replace("#DateData", storyDate.getText().toString());
        WebContent = WebContent.replace("#ImageFile", imagePath);

        if(lang_code.equals("my")){
            // Place Zawgyi
            String burmese_title = "";
            String burmese_body = "";

            if(!MDetect.INSTANCE.isUnicode() && StaticMethods.displayZawgyi()){
                burmese_title = Rabbit.uni2zg(storiesLocal.getTitle_my());
                burmese_body = Rabbit.uni2zg(storiesLocal.getSubtitle_my() + "<br>" +storiesLocal.getBody_my().replace("\r\n", "<br>"));
            }else{
                burmese_title = storiesLocal.getTitle_my();
                burmese_body = storiesLocal.getSubtitle_my() + "<br>" +storiesLocal.getBody_my().replace("\r\n", "<br>");
            }

            WebContent = WebContent.replace("#TitleData", burmese_title);
            WebContent = WebContent.replace("#TextData", burmese_body);
        }else{
            WebContent = WebContent.replace("#TitleData", storyTitle.getText().toString());
            WebContent = WebContent.replace("#TextData", storySubTitle.getText().toString() + "<br>" + storyBody.getText().toString().replace("\r\n", "<br>"));
        }

        if(AppDistribution.equals("RV")){
            String burmese_title = storiesLocal.getTitle_my();
            String burmese_body = storiesLocal.getSubtitle_my() + "<br>" +storiesLocal.getBody_my().replace("\r\n", "<br>");

            if (!MDetect.INSTANCE.isUnicode() && StaticMethods.displayZawgyi()){
                burmese_title = Rabbit.uni2zg(burmese_title);
                burmese_body = Rabbit.uni2zg(burmese_body);
            }

            WebContent = WebContent.replace("#Title_Lang_1", storiesLocal.getTitle_en());
            WebContent = WebContent.replace("#Title_Lang_2", burmese_title);
            WebContent = WebContent.replace("#Body_Lang_1", storiesLocal.getSubtitle_en() + "<br>" + storiesLocal.getBody_en().replace("\r\n", "<br>"));
            WebContent = WebContent.replace("#Body_Lang_2", burmese_body);
            WebContent = WebContent.replace("::lang_2_key::", "Burmese");
            WebContent = WebContent.replace("::lang_2_text::", "ဗမာ");
        }else{
            WebContent = WebContent.replace("#Title_Lang_1", storiesLocal.getTitle_en());
            WebContent = WebContent.replace("#Title_Lang_2", storiesLocal.getTitle_bn());
            WebContent = WebContent.replace("#Title_Lang_3", storiesLocal.getTitle_my());
            WebContent = WebContent.replace("#Body_Lang_1", storiesLocal.getSubtitle_en() + "<br>" + storiesLocal.getBody_en().replace("\r\n", "<br>"));
            WebContent = WebContent.replace("#Body_Lang_2", storiesLocal.getSubtitle_bn() + "<br>" +storiesLocal.getBody_bn().replace("\r\n", "<br>"));
            WebContent = WebContent.replace("#Body_Lang_3", storiesLocal.getSubtitle_my() + "<br>" +storiesLocal.getBody_my().replace("\r\n", "<br>"));

            // Enable Bangla
            WebContent = WebContent.replace("<!-- HideBN ", "");
            WebContent = WebContent.replace(" HideBN  !-->", "");
            WebContent = WebContent.replace(" HideBN  !-->", "");
            WebContent = WebContent.replace("// HideBN ", "");

        }

        WebContent = WebContent.replace("::current_lang::", lang_code);
        WebContent = WebContent.replace("::video_path::", videoPath);

        File file = new File(videoPath);

        if(file.exists()){
            double bytes = file.length();
            Log.d("bytes", String.valueOf(bytes));
        }

        if(lang_code.equals("my")){
            ttsEngine = new TextToSpeech(this, status -> {
                if(status != TextToSpeech.ERROR) {
                    ttsEngine.setLanguage(Locale.UK);
                    ttsInit();
                }
            }, "org.saomaicenter.myanmartts");
            WebContent = WebContent.replace("::spliter::", "။");
        }else if(lang_code.equals("en")){
            ttsEngine = new TextToSpeech(this, status -> {
                if(status != TextToSpeech.ERROR) {
                    ttsEngine.setLanguage(Locale.UK);
                    ttsInit();
                }
            }, "com.google.android.tts");
            WebContent = WebContent.replace("::spliter::", ".");
        }else if(lang_code.equals("bn") && AppDistribution.equals("GV")) {
            ttsEngine = new TextToSpeech(this, status -> {
                if(status != TextToSpeech.ERROR) {
                    ttsEngine.setLanguage(new Locale("bn"));
                    ttsInit();
                }
            }, "com.google.android.tts");
            WebContent = WebContent.replace("::spliter::", "।");
        }

        //webStory.loadDataWithBaseURL(null, WebContent, "text/html; charset=utf-8", "UTF-8", null);
        webStory.loadDataWithBaseURL("file:///android_asset/pages/story.html", WebContent, "text/html; charset=utf-8", "UTF-8", null);
        webStory.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webStory.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webStory.addJavascriptInterface(this, "SurveyorStory");

        findViewById(R.id.btn_tts).setOnClickListener(view -> toggleSound());

        findViewById(R.id.btn_tts_lottie).setOnClickListener(view -> toggleSound());

        // Log Event
        Bundle logBundle = new Bundle();
        logBundle.putInt("story_id", storyId);
        StaticMethods.logFirebase("story_view", logBundle);

    }

    public void reloadTTS() {
        if(lang_code.equals("my")){
            ttsEngine = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status){
                    if(status != TextToSpeech.ERROR) {
                        ttsEngine.setLanguage(Locale.UK);
                        ttsInit();
                    }
                }
            }, "org.saomaicenter.myanmartts");
        }else if(lang_code.equals("en")){
            ttsEngine = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status){
                    if(status != TextToSpeech.ERROR) {
                        ttsEngine.setLanguage(Locale.UK);
                        ttsInit();
                    }
                }
            }, "com.google.android.tts");
        }else if(lang_code.equals("bn")) {
            ttsEngine = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status){
                    if(status != TextToSpeech.ERROR) {
                        ttsEngine.setLanguage(new Locale("bn"));
                        ttsInit();
                    }
                }
            }, "com.google.android.tts");
        }
    }

    public void toggleSound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ttsEngine.isSpeaking()){
                    ttsEngine.stop();
                    speechButton.setVisibility(View.VISIBLE);
                    speechButtonAnim.setVisibility(View.GONE);
                    speechButtonAnim.pauseAnimation();

                    // Log Event
                    Bundle logBundle = new Bundle();
                    logBundle.putString("action", "stop_speech");
                    StaticMethods.logFirebase("story_action", logBundle);
                }else{
                    webStory.loadUrl("javascript:vueApp.playNow()");
                    speechButton.setVisibility(View.GONE);
                    speechButtonAnim.setVisibility(View.VISIBLE);
                    speechButtonAnim.playAnimation();

                    // Log Event
                    Bundle logBundle = new Bundle();
                    logBundle.putString("action", "play_speech");
                    StaticMethods.logFirebase("story_action", logBundle);
                }
            }
        });


    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(isOpen){ isOpen = false; }else{ return; }
        ttsEngine.stop();
        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, findViewById(R.id.backButton));
        super.onBackPressed();
    }

    void ttsInit() {
        ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onDone(String utteranceId)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webStory.loadUrl("javascript:vueApp.playNext()");
                        speechButton.setVisibility(View.VISIBLE);
                        speechButtonAnim.setVisibility(View.GONE);
                        speechButtonAnim.pauseAnimation();
                    }
                });
            }

            @Override
            public void onError(String utteranceId)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speechButton.setVisibility(View.GONE);
                        speechButtonAnim.setVisibility(View.VISIBLE);
                        speechButtonAnim.pauseAnimation();
                    }
                });
            }

            @Override
            public void onStart(String utteranceId)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speechButton.setVisibility(View.GONE);
                        speechButtonAnim.setVisibility(View.VISIBLE);
                        speechButtonAnim.playAnimation();
                    }
                });
            }
        });
    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getAssets().open(inFile);
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

    int BN_CHECK_CODE = 12132;
    int LANG_CHECK_CODE = 12311;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BN_CHECK_CODE) {
            ArrayList<String> availableLanguages = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);

            if(availableLanguages == null){
                return;
            }

            if (!availableLanguages.contains("bn")) {

                if(ttsEngine.isSpeaking()) {
                    ttsEngine.stop();

                    new Handler().postDelayed(() -> {
                        runOnUiThread(() -> {
                            speechButton.setVisibility(View.VISIBLE);
                            speechButtonAnim.setVisibility(View.GONE);
                            speechButtonAnim.pauseAnimation();
                        });
                    }, 250);
                }

                // Show Prompt to Download
                new CustomDialog(this).displayCustomDialog(new CustomDialogComponent()
                                .setSubTextVisible(View.GONE)
                                .setMainText(getString(R.string.v1_install_tts_bn))
                                .setButtonYes("Download Now")
                                .setButtonNo("Cancel"),
                        new CustomDialogInterface() {
                            @Override
                            public void retry() {
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                            }

                            @Override
                            public void cancel() {
                                // None
                            }
                        });

                return;
            }
        }

        if(requestCode == LANG_CHECK_CODE){
            ArrayList<String> availableLanguages = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);

            if(availableLanguages == null){
                return;
            }

            if(availableLanguages.isEmpty()){
                // Show Prompt to Download
                new CustomDialog(this).displayCustomDialog(new CustomDialogComponent()
                                .setSubTextVisible(View.GONE)
                                .setMainText(getString(R.string.v1_install_tts_en))
                                .setButtonYes("Download Now")
                                .setButtonNo("Cancel"),
                        new CustomDialogInterface() {
                            @Override
                            public void retry() {
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                            }

                            @Override
                            public void cancel() {
                                // None
                            }
                        });

                return;
            }
        }
    }

    boolean myCheck = false;
    boolean bnCheck = false;
    boolean enCheck = false;

    @JavascriptInterface
    public void ttsData(String tts_text) {

//        if(lang_code.equals("bn") && !bnCheck){
//            Intent checkIntent = new Intent();
//            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//            startActivityForResult(checkIntent, BN_CHECK_CODE);
//            bnCheck = true;
//        }

        if(lang_code.equals("my") && !isPackageExisted("org.saomaicenter.myanmartts")){
            //myCheck = true;
            // TTS Engine Does Not Exists
            if(ttsEngine.isSpeaking()) {
                ttsEngine.stop();
            }

            // Show Prompt to Download
            new CustomDialog(this).displayCustomDialog(new CustomDialogComponent()
                            .setSubTextVisible(View.GONE)
                            .setMainText(getString(R.string.v1_install_tts_my))
                            .setButtonYes("OK")
                            .setButtonNo("Cancel"),
                    new CustomDialogInterface() {
                        @Override
                        public void retry() {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.saomaicenter.myanmartts")));
                        }

                        @Override
                        public void cancel() {
                            // None
                        }
                    });

            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    speechButton.setVisibility(View.VISIBLE);
                    speechButtonAnim.setVisibility(View.GONE);
                    speechButtonAnim.pauseAnimation();
                });
            }, 250);

            return;
        }

        String utterance_id = getMD5(tts_text);
        ttsEngine.speak(tts_text, TextToSpeech.QUEUE_FLUSH, null, utterance_id);
    }

    @JavascriptInterface
    public void reloadLang(String lang) {
        this.lang_code = lang;
        if(ttsEngine.isSpeaking()) {
            ttsEngine.stop();

            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    speechButton.setVisibility(View.VISIBLE);
                    speechButtonAnim.setVisibility(View.GONE);
                    speechButtonAnim.pauseAnimation();
                });
            }, 250);

        }
        reloadTTS();
    }

    @JavascriptInterface
    public void stopTTS() {
        if(ttsEngine.isSpeaking()) {
            ttsEngine.stop();

            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    speechButton.setVisibility(View.VISIBLE);
                    speechButtonAnim.setVisibility(View.GONE);
                    speechButtonAnim.pauseAnimation();
                });
            }, 100);
        }
        reloadTTS();
    }

    // Init Menu and Header
    void initHeader(){
        menuItems.add(new PowerMenuItem(getString(R.string.action_bug_report)));
        menuItems.add(new PowerMenuItem(getString(R.string.action_settings)));
        menuItems.add(new PowerMenuItem(getString(R.string.action_logout)));
        initHeaderBar();
    }

    // Init Database Object and Return StoriesLocal
    StoriesLocal getStory(int id){
        AppDatabase database = databaseConnection.getDatabase(StoriesActivity.this);
        StoriesDao storiesDao = database.getStories();
        return storiesDao.getStoryById(id);
    }

    // Display StoriesLocal into Views
    void displayStory(StoriesLocal storiesLocal){

        TextView storyAuthorName = findViewById(R.id.storyAuthorName);
        CircleImageView storyAuthorImage = findViewById(R.id.storyAuthorImage);
        TextView storyDate = findViewById(R.id.storyDate);
        ImageView storyImage = findViewById(R.id.storyContentImage);
        TextView storyTitle = findViewById(R.id.storyTitle);
        TextView storySubTitle = findViewById(R.id.storySubTitle);
        TextView storyBody = findViewById(R.id.storyBody);

        // Assign Values to View
        storyAuthorName.setText(storiesLocal.getAuthor());
        storyDate.setText(storiesLocal.getCreated_at());
        storySubTitle.setVisibility(View.GONE);

        if(lang_code.equals("bn")){
            storyTitle.setText(storiesLocal.getTitle_bn());
            storyBody.setText(storiesLocal.getBody_bn());
            if(storySubTitle != null){
                storySubTitle.setText(storiesLocal.getSubtitle_bn());
            }
        }else if(lang_code.equals("my")){
            storyTitle.setText(storiesLocal.getTitle_my());
            storyBody.setText(storiesLocal.getBody_my());
            if(storySubTitle != null){
                storySubTitle.setText(storiesLocal.getSubtitle_my());
            }
        } else {
            storyTitle.setText(storiesLocal.getTitle_en());
            storyBody.setText(storiesLocal.getBody_en());

            if(storySubTitle != null){
                storySubTitle.setText(storiesLocal.getSubtitle_en());
            }
        }

//        Glide.with(StoriesActivity.this)
//                .load(storiesLocal.getAuthor_image())
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .centerCrop()
//                .into(storyAuthorImage);

        if(storiesLocal.getContent_image() != null){
            if(!storiesLocal.getContent_image().equals("")){
                String imageURL = storiesLocal.getContent_image();
                String file_path = this.getFilesDir() + "/story_image_" + getMD5(imageURL);
                imagePath = file_path;
            }else{
                imagePath = "file:///android_asset/images/no-image.png";
            }


//            Glide.with(StoriesActivity.this)
//                    .load(file_path)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .fitCenter()
//                    .into(storyImage);
        }

        if(storiesLocal.getStory_video() != null){
            String videoURL = storiesLocal.getStory_video();
            if(!videoURL.equals("")){
                videoPath = this.getFilesDir() + "/story_video_" + getMD5(videoURL) +  "#t=0.2";
            }else{
                videoPath = "";
            }

        }
    }




    // Extend ChromeClient
    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        ChromeClient() {

        }

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }

            return BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.v1_splash);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }



}

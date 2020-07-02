package io.rapidpro.surveyor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.github.florent37.viewanimator.ViewAnimator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rapidpro.surveyor.activity.BaseActivity;
import io.rapidpro.surveyor.activity.LoginActivity;
import io.rapidpro.surveyor.activity.OrgChooseActivity;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.extend.api.ApkApi;
import io.rapidpro.surveyor.extend.api.StoriesApi;
import io.rapidpro.surveyor.extend.api.SurveyorApi;
import io.rapidpro.surveyor.extend.api.UreportApi;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.StoriesDao;
import io.rapidpro.surveyor.extend.entity.dao.SurveyorDao;
import io.rapidpro.surveyor.extend.entity.dao.UReportDao;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;
import io.rapidpro.surveyor.extend.entity.model.apk_version;
import io.rapidpro.surveyor.extend.entity.model.story_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_data;
import io.rapidpro.surveyor.extend.entity.model.surveyor_api;
import io.rapidpro.surveyor.extend.entity.model.ureport_api;
import me.myatminsoe.mdetect.MDetect;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class SplashActivity extends BaseActivity {


    private static AppDatabase database;
    private static Context context;

    boolean is_story_loading = false;
    boolean is_story_loading_2 = false;
    boolean is_ureport_loading = false;
    boolean is_ureport_loading_2 = false;
    boolean is_content_loading = false;

    boolean halt_goto_next = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_splash);

        database = databaseConnection.getDatabase(this);
        context = this;

        MDetect.INSTANCE.init(this);

        // Animate Objects
        animateObjects();

        // Check Update
        checkAPK_Update();

        // Copy Data
        // new ProcessData().execute();

        // Add Firebase Analytics
        StaticMethods.firebase = FirebaseAnalytics.getInstance(this);

        // Log Event
        Bundle logBundle = new Bundle();
        logBundle.putString("activity", "Splash");
        StaticMethods.logFirebase("app_event", logBundle);
    }


    /**
     * Animates the component when launching
     */
    public void animateObjects() {

        ViewAnimator
                .animate(findViewById(R.id.splash_top))
                .translationY(-500, 0)
                .alpha(0, 1)
                .decelerate()
                .duration(500)
                .start();

        ViewAnimator
                .animate(findViewById(R.id.splash_bottom_left))
                .translationX(-500, 0)
                .alpha(0, 1)
                .decelerate()
                .duration(750)
                .start();

        ViewAnimator
                .animate(findViewById(R.id.splash_bottom_right))
                .translationX(500, 0)
                .alpha(0, 1)
                .decelerate()
                .duration(750)
                .start();

        ViewAnimator
                .animate(findViewById(R.id.splash_logo))
                .alpha(0, 1)
                .decelerate()
                .duration(1000)
                .start();

        ViewAnimator
                .animate(findViewById(R.id.appName))
                .alpha(0, 1)
                .decelerate()
                .duration(1250)
                .start();

        ViewAnimator
                .animate(findViewById(R.id.spin_kit))
                .alpha(0, 1)
                .decelerate()
                .duration(1500)
                .start();
    }

    void showUpdate_Dialog() {
        playNotification(getSurveyor(), context, R.raw.button_click_yes);
        final Dialog dialog4 = new Dialog(context);
        dialog4.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog4.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog4.setContentView(R.layout.v1_dialog_ui);
        dialog4.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) dialog4.findViewById(R.id.textMainText)).setText(R.string.v1_new_version);
        ((TextView) dialog4.findViewById(R.id.button_yes_text)).setText("Ok");
        ((TextView) dialog4.findViewById(R.id.button_no_text)).setText("Cancel");

        dialog4.findViewById(R.id.button_yes).setOnClickListener(view -> {
            playNotification(getSurveyor(), context, R.raw.button_click_yes, view);
            dialog4.dismiss();

            try {
                String file_url = getSurveyor().getPreferences().getString("apk_url", "");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(file_url)));
                finish();
            } catch (Exception e) {
                //
            }

        });

        dialog4.findViewById(R.id.button_no).setOnClickListener(view -> {
            playNotification(getSurveyor(), context, R.raw.button_click_no, view);
            dialog4.dismiss();
            finish();
        });

        dialog4.setCanceledOnTouchOutside(false);
        dialog4.show();
    }

    void checkAPK_Update() {
        String baseURL;
        if(StaticMethods.AppDistribution.equals("GV")){
            baseURL = StoriesApi.BASE_URL_GV;
        }else{
            baseURL = StoriesApi.BASE_URL_RV;
        }

        // Check U-Report Offline Update
        OkHttpClient okHttpClient0 = StaticMethods.okHttpClient();

        Retrofit retrofit0 = new Retrofit.Builder().baseUrl(baseURL).client(okHttpClient0).addConverterFactory(GsonConverterFactory.create()).build();

        ApkApi apkApi = retrofit0.create(ApkApi.class);
        Call<apk_version> apk_versionCall = apkApi.getVersion();
        apk_versionCall.enqueue(new Callback<apk_version>() {

            @Override
            public void onResponse(Call<apk_version> call, Response<apk_version> response) {

                apk_version apkVersion;

                if(response.body() != null){
                    apkVersion = response.body();
                }else{
                    checkUpdaterLogic();
                    return;
                }

                getSurveyor().setPreference("apk_version_code", apkVersion.getVersion_code());
                getSurveyor().setPreference("apk_version_name", apkVersion.getVersion_name());
                getSurveyor().setPreference("apk_is_mandatory", apkVersion.getIs_mandatory());
                getSurveyor().setPreference("apk_url", apkVersion.getFile_url());
                getSurveyor().setPreference("apk_status", apkVersion.getStatus());

                checkUpdaterLogic();

            }

            @Override
            public void onFailure(Call<apk_version> call, Throwable t) {
                checkUpdaterLogic();
            }

        });
    }

    void checkUpdaterLogic(){
        // Failed to Check : Check if mandatory update is pending
        int currentVersion = BuildConfig.VERSION_CODE;
        int availableVersion = getSurveyor().getPreferences().getInt("apk_version_code", 0);
        int isMandatory = getSurveyor().getPreferences().getInt("apk_is_mandatory", 0);
        int apkStatus = getSurveyor().getPreferences().getInt("apk_status", 0);

        if(availableVersion > currentVersion && apkStatus == 1){
            if(isMandatory == 0){
                // No Mandatory Update Detected: Continue Update
                new ProcessData().execute();
            }else{
                // Mandatory Update Required: Show Dialog and Prevent Launch
                showUpdate_Dialog();
                // Halt Background Process
                halt_goto_next = true;
            }
        }else{
            // Update Not Available or Not Required
            new ProcessData().execute();
        }
    }

    /**
     * Background Service which runs to download / sync
     * Stories / uReport / Offline Surveyor results from it's
     * server.
     *
     * Also this service downloads the images / videos that
     * are attached to the story.
     */
    private class ProcessData extends AsyncTask<String, Void, String> {

        Retrofit retrofit;
        Gson gson;

        StoriesApi storiesApi;
        StoriesDao storiesDao;
        Call<story_api> story_apiCall;
        List<StoriesLocal> localStories;

        UReportDao uReportDao;
        UreportApi uApi;
        Call<ureport_api> call_ureport;

        SurveyorDao surveyorDao;
        SurveyorApi surveyorApi;
        Call<surveyor_api> call_surveyor;


        @Override
        protected String doInBackground(String... params) {

            String baseURL;
            if(StaticMethods.AppDistribution.equals("GV")){
                baseURL = StoriesApi.BASE_URL_GV;
            }else{
                baseURL = StoriesApi.BASE_URL_RV;
            }

            // Connect Database
            gson = new Gson();

            // Update Last Updated
            String is_installed = getSurveyor().getPreferences().getString("is_installed_20", "false");

            is_story_loading = true;
            if(!is_installed.equals("true")){
                //copyData();

                // Copy Story Data
                storiesDao = database.getStories();
                Type storyLocalType = new TypeToken<story_api>() { }.getType();
                String storyStringData = getJsonFromAssets(getApplicationContext(), "data/story.json");
                story_api storyAPI = gson.fromJson(storyStringData, storyLocalType);
                String story_new_last_update = storyAPI.getLast_updated();
                String story_last_update = getSurveyor().getPreferences().getString("story_date", "");

                if(!story_new_last_update.equals(story_last_update)){
                    List<StoriesLocal> storiesLocals = storyAPI.getData();
                    for(StoriesLocal s: storiesLocals) {
                        if(storiesDao.doesStoryExists(s.getId()) > 0){
                            // Old Story: update
                            s.primaryKey = storiesDao.getStory_pKey(s.getId());
                            storiesDao.update(s);
                        }else{
                            // New Story: insert
                            storiesDao.insert(s);
                        }

                        // Get Image
                        String file_image = s.getContent_image();
                        String file_path = "story_image_" + getMD5(file_image);
                        String asset_path = "data/story_image/" + file_path;
                        String file_path_full = getApplicationContext().getFilesDir().getPath() + "/story_image_" + getMD5(file_image);

                        if(!file_image.equals("") && isAssetExists(asset_path)){
                            try {
                                copyFileFromAssets(getApplicationContext(), asset_path, file_path_full);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Get Video
                        String file_video = s.getStory_video();
                        String video_path = "story_video_" + getMD5(file_video);
                        String v_asset_path = "data/story_video/" + video_path;
                        String video_path_full = getApplicationContext().getFilesDir().getPath() + "/story_video_" + getMD5(file_video);

                        if(!file_video.equals("") && isAssetExists(v_asset_path)){
                            try {
                                copyFileFromAssets(getApplicationContext(), v_asset_path, video_path_full);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Copy U-Report Data
                UReportDao uReportDao = database.getUReports();
                Type ureportLocalType = new TypeToken<ureport_api>() { }.getType();
                String ureportStringData = getJsonFromAssets(context, "data/ureport.json");
                ureport_api ureportAPI = gson.fromJson(ureportStringData, ureportLocalType);
                String ureport_new_last_update = ureportAPI.getLast_updated();
                String ureport_last_update = getSurveyor().getPreferences().getString("ureport_date", "");

                if(!ureport_new_last_update.equals(ureport_last_update)){
                    List<UReportLocal> ureports = ureportAPI.getData();
                    for(UReportLocal x: ureports){
                        if(uReportDao.doesUReportExists(x.getUreport_id()) > 0){
                            // Old UReport: update
                            x.primaryKey = uReportDao.getUreport_pKey(x.getUreport_id());
                            uReportDao.update(x);
                        }else{
                            // New UReport: insert
                            uReportDao.insert(x);
                        }
                    }
                }

                // Copy Offline U-Report Data
                surveyorDao = database.getSurveyor();
                Type surveyorLocalType = new TypeToken<surveyor_api>() { }.getType();
                String surveyorStringData = getJsonFromAssets(context, "data/surveyor.json");
                surveyor_api surveyorAPI = gson.fromJson(surveyorStringData, surveyorLocalType);
                String surveyor_new_last_update = surveyorAPI.getLast_updated();
                String surveyor_last_update = getSurveyor().getPreferences().getString("surveyor_date", "");


                if(!surveyor_new_last_update.equals(surveyor_last_update)){
                    List<SurveyorLocal> ureports = surveyorAPI.getData();
                    for(SurveyorLocal x: ureports){
                        if(surveyorDao.doesSurveyExists(x.getFlow_id()) > 0){
                            // Old UReport: update
                            x.primaryKey = surveyorDao.getSurvey_pKey(x.getFlow_id());
                            surveyorDao.update(x);
                        }else{
                            // New UReport: insert
                            surveyorDao.insert(x);
                        }
                    }
                }

                if(StaticMethods.getLocalUpdateDate(getSurveyor(), "ureport_offline_last_updated_local").equals("")){
                    StaticMethods.setLocalUpdateDate(getSurveyor(), "ureport_offline_last_updated_local");
                }

                getSurveyor().setPreference("is_installed_20", "true");
            }
            is_story_loading = false;

            // Check Update
            // Check Story Update
            // Download Stories
            String last_updated = getSurveyor().getPreferences().getString("story_date", "");
            storiesDao = database.getStories();



            OkHttpClient okHttpClient1 = StaticMethods.okHttpClient();

            Retrofit retrofit1 = new Retrofit.Builder().baseUrl(baseURL).client(okHttpClient1).addConverterFactory(GsonConverterFactory.create()).build();

            storiesApi = retrofit1.create(StoriesApi.class);
            story_apiCall = storiesApi.getStories(last_updated, 100);
            is_story_loading = true;
            story_apiCall.enqueue(new Callback<story_api>() {
                @Override
                public void onResponse(Call<story_api> call, Response<story_api> response) {

                    Log.d("UR_Story_Data", response.body().getLast_updated());

                    is_story_loading = false;

                    if(response.body() != null){

                        if(response.body().getData().size() == 0){
                            return;
                        }

                        String new_last_updated = response.body().getLast_updated();
                        List<StoriesLocal> storiesLocals = response.body().getData();

                        //new Thread(() -> {
                        for (StoriesLocal s : storiesLocals) {
                            if (storiesDao.doesStoryExists(s.getId()) > 0) {
                                // Old Story: update
                                s.primaryKey = storiesDao.getStory_pKey(s.getId());
                                storiesDao.update(s);
                            } else {
                                // New Story: insert
                                storiesDao.insert(s);
                            }
                        }
                        //}).start();

                        // Update Last Updated
                        getSurveyor().setPreference("story_date", new_last_updated);

                        //new Thread(() -> {
                        // Download Images
                        localStories = storiesDao.getStoriesList();

                        for (StoriesLocal s : localStories) {
                            // Download Story Image
                            if (s.getContent_image() == null) {
                                continue;
                            }
                            if (s.getContent_image().equals("")) {
                                continue;
                            }
                            Context context = getApplicationContext();
                            String imageURL = s.getContent_image();

                            if (imageURL.equals("")) {
                                continue;
                            }

                            final String file_path = "story_image_" + getMD5(imageURL);
                            final String file_path_full = context.getFilesDir() + "/story_image_" + getMD5(imageURL);
                            File file = new File(file_path_full);
                            if (file.exists()) {
                                if (file.length() > 100 * 1024) {
                                    // File with At Least 100 KB Data Exists: skip
                                    continue;
                                }
                            }

                            is_content_loading = true;
                            getURL(imageURL, new okhttp3.Callback() {
                                @Override
                                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                    InputStream inputStream = response.body().byteStream();
                                    try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                        int read;
                                        while ((read = inputStream.read(buffer)) != -1) {
                                            output.write(buffer, 0, read);
                                        }
                                        output.flush();
                                    }
                                    inputStream.close();
                                    is_content_loading = false;
                                }

                                @Override
                                public void onFailure(okhttp3.Call call, IOException e) {
                                    is_content_loading = false;
                                }
                            });

                        }

                        for (StoriesLocal s : localStories) {
                            // Download Story Video
                            if (s.getStory_video() == null) {
                                continue;
                            }
                            if (s.getStory_video().equals("")) {
                                continue;
                            }

                            Context context = getApplicationContext();
                            String videoURL = s.getStory_video();

                            if (videoURL.equals("")) {
                                continue;
                            }

                            final String file_path = "story_video_" + getMD5(videoURL);
                            String file_path_full = context.getFilesDir() + "/story_video_" + getMD5(videoURL);
                            File file = new File(file_path_full);
                            if (file.exists()) {
                                if (file.length() > 100 * 1024) {
                                    // File with At Least 50 KB Data Exists: skip
                                    continue;
                                }
                            }

                            is_content_loading = true;
                            getURL(videoURL, new okhttp3.Callback() {

                                @Override
                                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                    InputStream inputStream = response.body().byteStream();
                                    try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                        int read;
                                        while ((read = inputStream.read(buffer)) != -1) {
                                            output.write(buffer, 0, read);
                                        }
                                        output.flush();
                                    }
                                    inputStream.close();
                                    is_content_loading = false;
                                }

                                @Override
                                public void onFailure(okhttp3.Call call, IOException e) {
                                    is_content_loading = false;
                                }

                            });

                        }
                        //}).start();

                    }
                }

                @Override
                public void onFailure(Call<story_api> call, Throwable t) {
                    Log.d("UR_Story_Data", "Error Response");
                    is_story_loading = false;
                }
            });

            // Check For Deleted Story
            OkHttpClient okHttpClient2 = StaticMethods.okHttpClient();

            Retrofit retrofit2 = new Retrofit.Builder().baseUrl(baseURL).client(okHttpClient2).addConverterFactory(GsonConverterFactory.create()).build();

            final String story_delete_last_update = getSurveyor().getPreferences().getString("story_delete_last_update", "");
            StoriesApi storiesDeleteApi = retrofit2.create(StoriesApi.class);
            Call<story_delete_api> storyDelete_apiCall = storiesDeleteApi.getDeletedStories(story_delete_last_update);
            is_story_loading_2 = true;
            storyDelete_apiCall.enqueue(new Callback<story_delete_api>() {

                @Override
                public void onResponse(Call<story_delete_api> call, Response<story_delete_api> response) {

                    Log.d("UR_Story_Delete", response.body().getLast_updated());

                    if(response.body().getData() == null){
                        is_story_loading_2 = false;
                        return;
                    }

                    List<story_delete_data> deleted_stories = response.body().getData();

                    for(story_delete_data deleted: deleted_stories) {

                        //new Thread(() -> {
                        if(storiesDao.doesStoryExists(deleted.getStory_id()) > 0){
                            // Story Deleted on Server
                            storiesDao.deleteFromStoryById(deleted.getStory_id());
                        }
                        //}).start();
                    }

                    // Update Story Delete Last_Update
                    getSurveyor().setPreference("story_delete_last_update", response.body().getLast_updated());
                    is_story_loading_2 = false;
                }

                @Override
                public void onFailure(Call<story_delete_api> call, Throwable t) {
                    Log.d("UR_Story_Delete", "Error Response");
                    is_story_loading_2 = false;
                }

            });

            // Check Missing Image / Video
            localStories = storiesDao.getStoriesList();

            for (StoriesLocal s : localStories) {
                // Download Story Image
                if (s.getContent_image() == null) {
                    continue;
                }
                if (s.getContent_image().equals("")) {
                    continue;
                }
                Context context = getApplicationContext();
                String imageURL = s.getContent_image();

                if(imageURL.equals("")){
                    continue;
                }

                final String file_path = "story_image_" + getMD5(imageURL);
                final String file_path_full = context.getFilesDir() + "/story_image_" + getMD5(imageURL);
                File file = new File(file_path_full);
                if (file.exists()) {
                    if (file.length() > 100 * 1024) {
                        // File with At Least 50 KB Data Exists: skip
                        continue;
                    }
                }

                is_content_loading = true;
                getURL(imageURL, new okhttp3.Callback() {
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        InputStream inputStream = response.body().byteStream();
                        try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        }
                        inputStream.close();
                        is_content_loading = false;
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        is_content_loading = false;
                    }
                });
            }

            // Download Video
            for (StoriesLocal s : localStories) {
                // Download Story Video
                if (s.getStory_video() == null) {
                    continue;
                }
                if (s.getStory_video().equals("")) {
                    continue;
                }

                Context context = getApplicationContext();
                String videoURL = s.getStory_video();

                if(videoURL.equals("")){
                    continue;
                }

                final String file_path = "story_video_" + getMD5(videoURL);
                final String file_path_full = context.getFilesDir() + "/story_video_" + getMD5(videoURL);
                File file = new File(file_path_full);
                if (file.exists()) {
                    if (file.length() > 100 * 1024) {
                        // File with At Least 50 KB Data Exists: skip
                        continue;
                    }
                }

                is_content_loading = true;
                getURL(videoURL, new okhttp3.Callback() {
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                        InputStream inputStream = response.body().byteStream();
                        try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        }
                        inputStream.close();
                        is_content_loading = false;
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        is_content_loading = false;
                    }
                });
            }


            // Check U-Report Update
            OkHttpClient okHttpClient3 = StaticMethods.okHttpClient();

            Retrofit retrofit3 = new Retrofit.Builder().baseUrl(baseURL).client(okHttpClient3).addConverterFactory(GsonConverterFactory.create()).build();

            String ureport_last_updated = getSurveyor().getPreferences().getString("ureport_date", "");
            uApi = retrofit3.create(UreportApi.class);
            call_ureport = uApi.getUreport(100, 0, ureport_last_updated);
            is_ureport_loading = true;
            call_ureport.enqueue(new Callback<ureport_api>() {
                @Override
                public void onResponse(Call<ureport_api> call, Response<ureport_api> response) {

                    Log.d("UR_UReport_Data", response.body().getLast_updated());

                    ureport_api ureportApi = response.body();

                    if(ureportApi == null){
                        is_ureport_loading = false;
                        return;
                    }

                    // Save Preference
                    String new_last_update = ureportApi.getLast_updated();
                    getSurveyor().setPreference("ureport_date", new_last_update);

                    if(ureportApi.getData().size() == 0){
                        is_ureport_loading = false;
                        return;
                    }

                    List<UReportLocal> ureports = ureportApi.getData();
                    uReportDao = database.getUReports();

                    for(UReportLocal x: ureports){

                        //new Thread(() -> {
                        if (uReportDao.doesUReportExists(x.getUreport_id()) > 0) {
                            // Old UReport: update
                            x.primaryKey = uReportDao.getUreport_pKey(x.getUreport_id());
                            uReportDao.update(x);
                        } else {
                            // New UReport: insert
                            uReportDao.insert(x);
                        }
                        //}).start();

                    }

                    is_ureport_loading = false;
                }

                @Override
                public void onFailure(Call<ureport_api> call, Throwable t) {
                    Log.d("UR_UReport_Data", "Error Response");
                    is_ureport_loading = false;
                }
            });


            // Check U-Report Offline Update
            OkHttpClient okHttpClient4 = StaticMethods.okHttpClient();

            Retrofit retrofit4 = new Retrofit.Builder().baseUrl(baseURL).client(okHttpClient4).addConverterFactory(GsonConverterFactory.create()).build();

            surveyorDao = database.getSurveyor();
            String surveyor_last_updated = getSurveyor().getPreferences().getString("surveyor_date", "");
            surveyorApi = retrofit4.create(SurveyorApi.class);
            call_surveyor = surveyorApi.getSurveyor(100, 0, surveyor_last_updated);
            is_ureport_loading_2 = true;
            Log.d("UR_Surveyor_Data", "Requested");
            call_surveyor.enqueue(new Callback<surveyor_api>() {
                @Override
                public void onResponse(Call<surveyor_api> call, Response<surveyor_api> response) {
                    Log.d("UR_Surveyor_Data", response.body().getLast_updated());

                    surveyor_api surveyorApi = response.body();

                    if(surveyorApi == null){
                        is_ureport_loading_2 = false;
                        return;
                    }

                    // Save Preference
                    String new_last_update = surveyorApi.getLast_updated();
                    getSurveyor().setPreference("surveyor_date", new_last_update);
                    StaticMethods.setLocalUpdateDate(getSurveyor(), "ureport_offline_last_updated_local");

                    if(surveyorApi.getData().size() == 0){
                        is_ureport_loading_2 = false;
                        return;
                    }

                    List<SurveyorLocal> surveyorLocals = surveyorApi.getData();

                    for(SurveyorLocal x: surveyorLocals){

                        //new Thread(() -> {
                        if (surveyorDao.doesSurveyExists(x.getFlow_id()) > 0) {
                            // Old UReport: update
                            x.primaryKey = surveyorDao.getSurvey_pKey(x.getFlow_id());
                            surveyorDao.update(x);
                        } else {
                            // New UReport: insert
                            surveyorDao.insert(x);
                        }
                        //}).start();

                    }

                    is_ureport_loading_2 = false;

                }

                @Override
                public void onFailure(Call<surveyor_api> call, Throwable t) {
                    Log.d("UR_Surveyor_Data", "Error Response");
                    is_ureport_loading_2 = false;
                }
            });

            return "splash";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.matches("splash")){
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    checkerLoop();
                }, 1000);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        /**
         * Get JSON data from Assets
         *
         * @param context
         * @param fileName
         * @return JSON String
         */
        String getJsonFromAssets(Context context, String fileName) {
            String jsonString;
            try {
                InputStream is = context.getAssets().open(fileName);

                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                jsonString = new String(buffer, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return jsonString;
        }

        /**
         * Checks if asset exists
         *
         * @param pathInAssetsDir
         * @return true / false whether file exists or not
         */
        boolean isAssetExists(String pathInAssetsDir){
            AssetManager assetManager = context.getResources().getAssets();
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open(pathInAssetsDir);
                if(null != inputStream ) {
                    return true;
                }
            }  catch(Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }


        /**
         * Copies file form Asset to Private Storage
         *
         * @param context
         * @param file
         * @param dest
         * @throws Exception
         */
        void copyFileFromAssets(Context context, String file, String dest) throws Exception {
            InputStream in = null;
            OutputStream fout = null;
            int count = 0;

            try
            {
                in = context.getAssets().open(file);
                fout = new FileOutputStream(new File(dest));

                byte data[] = new byte[1024];
                while ((count = in.read(data, 0, 1024)) != -1)
                {
                    fout.write(data, 0, count);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (in != null)
                {
                    try {
                        in.close();
                    } catch (IOException e)
                    {
                    }
                }
                if (fout != null)
                {
                    try {
                        fout.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        OkHttpClient client = StaticMethods.okHttpClient();

        /**
         * Simplified GET request call for OKHTTP
         * @param url
         * @param callback
         * @return
         */
        okhttp3.Call getURL(String url, okhttp3.Callback callback) {
            Log.d("UR_getURL", url);
            Request request = new Request.Builder().url(url).build();
            okhttp3.Call call = client.newCall(request);
            call.enqueue(callback);
            return call;
        }

        int nextTick = 0;

        Handler checkerHandler =  new Handler();
        Runnable checkerRunnable = new Runnable() {
            public void run() {
                checkerLoop();
            }
        };

        void checkerLoop() {

            // Reset Timer if Anything is Loading
            if(is_story_loading || is_story_loading_2 || is_content_loading || is_ureport_loading || is_ureport_loading_2){
                nextTick = 0;
                checkerHandler.postDelayed(checkerRunnable, 1000);
                Log.d("UR_Ticker", is_story_loading + " " + is_story_loading_2 + " " + is_content_loading + " " + is_ureport_loading + " " + is_ureport_loading_2);
            } else {
                if (nextTick < 4) {
                    nextTick++;
                    checkerHandler.postDelayed(checkerRunnable, 500);
                } else {
                    checkerHandler.removeCallbacks(checkerRunnable);
                    gotoNext();
                }
            }
        }
    }





    /**
     * Goto Next Activity
     */
    public void gotoNext() {
        Intent intent;

        if (!isLoggedIn() && !halt_goto_next) {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }else{
            intent = new Intent(SplashActivity.this, OrgChooseActivity.class);
        }


        Fade fade = new Fade();
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        startActivity(intent);
        finish();
    }

    boolean lockBack = false;
    @Override
    public void onBackPressed() {

        if(lockBack){return;}

        final Dialog exitDialog = new Dialog(context);
        exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        exitDialog.setContentView(R.layout.v1_dialog_ui);
        exitDialog.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) exitDialog.findViewById(R.id.textMainText)).setText("Would you like to exit the application?");
        ((TextView) exitDialog.findViewById(R.id.button_yes_text)).setText("Yes");
        ((TextView) exitDialog.findViewById(R.id.button_no_text)).setText("No");

        exitDialog.findViewById(R.id.button_yes).setOnClickListener(view1 -> {
            finish();
            exitDialog.dismiss();
            lockBack = false;
        });

        exitDialog.findViewById(R.id.button_no).setOnClickListener(view2 -> {
            exitDialog.dismiss();
            lockBack = false;
        });

        exitDialog.setOnDismissListener(dialogInterface -> {
            lockBack = false;
        });

        exitDialog.show();
        lockBack = true;

        //super.onBackPressed();
    }

    /**
     * This activity does not require login.
     * It is required and checked by BaseActivity
     * from Surveyor Application
     * @return false
     */
    @Override
    public boolean requireLogin() {
        return false;
    }

}

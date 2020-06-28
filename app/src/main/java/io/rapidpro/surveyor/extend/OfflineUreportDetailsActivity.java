package io.rapidpro.surveyor.extend;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.BaseActivity;
import io.rapidpro.surveyor.extend.adapter.CustomAdapterOpinionlist;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.SurveyorDao;
import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;
import io.rapidpro.surveyor.extend.entity.model.questions;
import io.rapidpro.surveyor.extend.entity.model.results;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class OfflineUreportDetailsActivity extends BaseActivity {

    AppDatabase database;
    SurveyorDao surveyorDao;

    private TextView summaryText;

    String resultName;
    String resultID;
    String resultDate;

    String lang_code = "en";

    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_ureport_offline);
        getWindow().setEnterTransition(null);

        isOpen = true;

        ObjectAnimator.ofFloat(findViewById(R.id.headerLayout), "alpha",  0, 1f).setDuration(1000).start();
        ObjectAnimator.ofFloat(findViewById(R.id.uCard), "translationY", 1000, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(findViewById(R.id.backButton), "translationX", -200, 0).setDuration(1000).start();

        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        Intent intent = getIntent();
        resultName = intent.getExtras().getString("ResultName");
        resultID = intent.getExtras().getString("ResultID");
        resultDate = intent.getExtras().getString("ResultDate");

        ((TextView) findViewById(R.id.activityName)).setText(resultName);

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");

        database = databaseConnection.getDatabase(getApplicationContext());
        surveyorDao = database.getSurveyor();
        SurveyorLocal surveyLocal = surveyorDao.getSurveyByFlowId(resultID);
        String survey_string = "";

        JsonParser parser = new JsonParser();
        JsonObject rootObj = null;

        if(lang_code.equals("my")){
            if(surveyLocal.getMy_pack() != null){
                survey_string = surveyLocal.getMy_pack();
                rootObj = parser.parse(survey_string).getAsJsonObject();
            }
        }else if(lang_code.equals("bn")){
            if(surveyLocal.getBn_pack() != null){
                survey_string = surveyLocal.getBn_pack();
                rootObj = parser.parse(survey_string).getAsJsonObject();
            }
        }else{
            if(surveyLocal.getData_pack() != null){
                survey_string = surveyLocal.getData_pack();
                rootObj = parser.parse(survey_string).getAsJsonObject();
            }
        }

        if(rootObj != null){
            int numSub = 0;

            try {
                numSub = rootObj.
                        getAsJsonArray("questions").
                        get(0).getAsJsonObject().
                        getAsJsonObject("results").
                        get("set").getAsInt();
            } catch (Exception e) {
                //
            }

            int numSup = numSub;

            TextView poll_summary = findViewById(R.id.poll_summary);

            if(numSub == 0){
                poll_summary.setText("");
            }else {

                float responseRate = 0f;
                DecimalFormat df = new DecimalFormat("#.00");

                if (numSub > 0 && numSup > 0) {
                    responseRate = (float) numSub / ((float) numSup / 100);
                    responseRate = Float.valueOf(df.format(responseRate));
                }

                poll_summary.setText(
                        this.getString(R.string.v1_ureport_poll_summary_off)
                                .replace("%sup", String.valueOf(numSub))
                                .replace("%sub", String.valueOf(responseRate))
                );
            }
        }

        WebView webView = findViewById(R.id.webView);

        String WebContent = LoadData("ureport/index.html");;

        WebContent = WebContent.replace("::data_pack::", survey_string);

        //webView.setWebChromeClient(new WebChromeClient());
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");

        webView.loadDataWithBaseURL("file:///android_asset/ureport/index.html", WebContent, "text/html; charset=utf-8", "UTF-8", null);

        summaryText = findViewById(R.id.poll_summary);

        // Log Event
        Bundle logBundle = new Bundle();
        logBundle.putInt("result_id", surveyLocal.getId());
        logBundle.putString("flow_id", surveyLocal.getFlow_id());
        StaticMethods.logFirebase("result_view", logBundle);
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

    @Override
    public void onBackPressed() {
        if(isOpen){ isOpen = false; }else{ return; }
        playNotification(SurveyorApplication.get(), getApplicationContext(), R.raw.button_click_no, findViewById(R.id.backButton));
        super.onBackPressed();
    }

    @Override
    public boolean requireLogin() {
        return false;
    }
}

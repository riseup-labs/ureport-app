package io.rapidpro.surveyor.extend;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.skydoves.powermenu.PowerMenuItem;

import java.util.ArrayList;
import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.BaseActivity;
import io.rapidpro.surveyor.extend.adapter.CustomAdapterOpinionlist;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.UReportDao;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;
import io.rapidpro.surveyor.extend.entity.model.questions;
import io.rapidpro.surveyor.extend.entity.model.results;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class UreportDetailsActivity extends BaseActivity implements CustomAdapterOpinionlist.ItemClickListener {

    CustomAdapterOpinionlist adapter;
    List<questions> questionsList = new ArrayList<>();
    RecyclerView recyclerView;
    AppDatabase database;
    UReportDao uReportDao;

    TextView statisticsT,locationsT,genderT,ageT;
    private TextView summaryText;

    String resultName;
    String resultID;
    String resultDate;

    String lang_code = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_ureport_data_new);

        getWindow().setEnterTransition(null);

        ObjectAnimator.ofFloat(findViewById(R.id.headerLayout), "alpha",  0, 1f).setDuration(1000).start();
        ObjectAnimator.ofFloat(findViewById(R.id.uCard), "translationY", 1000, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(findViewById(R.id.backButton), "translationX", -200, 0).setDuration(1000).start();

        findViewById(R.id.backButton).setOnClickListener(view -> onBackPressed());

        ((TextView) findViewById(R.id.activityName)).setText("U-Report");

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        Intent intent = getIntent();
        resultName = intent.getExtras().getString("ResultName");
        resultID = intent.getExtras().getString("ResultID");
        resultDate = intent.getExtras().getString("ResultDate");

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");

        database = databaseConnection.getDatabase(getApplicationContext());
        uReportDao = database.getUReports();
        UReportLocal uReportLocal = uReportDao.getUReportById(Integer.parseInt(resultID));
        results resultsObj = null;
        Gson gson = new Gson();

        if(lang_code.equals("my")){
            if(uReportLocal.getMy_pack() != null){
                resultsObj = gson.fromJson(uReportLocal.getMy_pack(), results.class);
            }
        }else if(lang_code.equals("bn")){
            if(uReportLocal.getData_pack() != null){
                resultsObj = gson.fromJson(uReportLocal.getData_pack(), results.class);
            }
        }else{
            if(uReportLocal.getEn_pack() != null){
                resultsObj = gson.fromJson(uReportLocal.getEn_pack(), results.class);
            }
        }

        if(resultsObj != null){
            List<questions> questionsTemp = resultsObj.getQuestions();
            for (questions b : questionsTemp) {
                questionsList.add(b);
            }

            ((TextView) findViewById(R.id.activityName)).setText(resultsObj.getTitle());
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
        recyclerView.hasFixedSize();

        summaryText = findViewById(R.id.poll_summary);
        adapter = new CustomAdapterOpinionlist(UreportDetailsActivity.this, questionsList, resultDate, summaryText);

        recyclerView.setAdapter(adapter);
        adapter.setClickListener(UreportDetailsActivity.this);

    }

    @Override
    public void onBackPressed() {
        playNotification(SurveyorApplication.get(), getApplicationContext(), R.raw.button_click_no, findViewById(R.id.backButton));
        super.onBackPressed();
    }


    @Override
    public void onItemClick(View view, int position) {

        switch (view.getId()) {
            case R.id.textViewStatistics:

                View temp = (View) view.getParent();
                View temp2 = (View) temp.getParent();
                statisticsT=temp2.findViewById(R.id.textViewStatistics);
                locationsT=temp2.findViewById(R.id.textViewlocations);
                genderT=temp2.findViewById(R.id.textViewGender);
                ageT=temp2.findViewById(R.id.textViewAge);

                statisticsT.setTextColor(getResources().getColor(R.color.white));
                statisticsT.setBackgroundColor(getResources().getColor(R.color.green2));

                locationsT.setTextColor(getResources().getColor(R.color.green2));
                locationsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                genderT.setTextColor(getResources().getColor(R.color.green2));
                genderT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                ageT.setTextColor(getResources().getColor(R.color.green2));
                ageT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                temp2.findViewById(R.id.layout_statistics).setVisibility(View.VISIBLE);
                temp2.findViewById(R.id.layout_gender).setVisibility(View.GONE);
                temp2.findViewById(R.id.layout_age).setVisibility(View.GONE);
                temp2.findViewById(R.id.layout_location).setVisibility(View.GONE);
                break;
            case R.id.textViewGender:
                View temp3 = (View) view.getParent();
                View temp4 = (View) temp3.getParent();

                statisticsT=temp4.findViewById(R.id.textViewStatistics);
                locationsT=temp4.findViewById(R.id.textViewlocations);
                genderT=temp4.findViewById(R.id.textViewGender);
                ageT=temp4.findViewById(R.id.textViewAge);

                statisticsT.setTextColor(getResources().getColor(R.color.green2));
                statisticsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                locationsT.setTextColor(getResources().getColor(R.color.green2));
                locationsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                genderT.setTextColor(getResources().getColor(R.color.white));
                genderT.setBackgroundColor(getResources().getColor(R.color.green2));

                ageT.setTextColor(getResources().getColor(R.color.green2));
                ageT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                temp4.findViewById(R.id.layout_statistics).setVisibility(View.GONE);
                temp4.findViewById(R.id.layout_gender).setVisibility(View.VISIBLE);
                temp4.findViewById(R.id.layout_age).setVisibility(View.GONE);
                temp4.findViewById(R.id.layout_location).setVisibility(View.GONE);
                break;
            case R.id.textViewAge:
                View temp5 = (View) view.getParent();
                View temp6 = (View) temp5.getParent();

                statisticsT=temp6.findViewById(R.id.textViewStatistics);
                locationsT=temp6.findViewById(R.id.textViewlocations);
                genderT=temp6.findViewById(R.id.textViewGender);
                ageT=temp6.findViewById(R.id.textViewAge);

                statisticsT.setTextColor(getResources().getColor(R.color.green2));
                statisticsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                locationsT.setTextColor(getResources().getColor(R.color.green2));
                locationsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                genderT.setTextColor(getResources().getColor(R.color.green2));
                genderT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                ageT.setTextColor(getResources().getColor(R.color.white));
                ageT.setBackgroundColor(getResources().getColor(R.color.green2));
                temp6.findViewById(R.id.layout_statistics).setVisibility(View.GONE);
                temp6.findViewById(R.id.layout_gender).setVisibility(View.GONE);
                temp6.findViewById(R.id.layout_age).setVisibility(View.VISIBLE);
                temp6.findViewById(R.id.layout_location).setVisibility(View.GONE);
                break;

            case R.id.textViewlocations:
                View temp7 = (View) view.getParent();
                View temp8= (View) temp7.getParent();


                statisticsT=temp8.findViewById(R.id.textViewStatistics);
                locationsT=temp8.findViewById(R.id.textViewlocations);
                genderT=temp8.findViewById(R.id.textViewGender);
                ageT=temp8.findViewById(R.id.textViewAge);

                statisticsT.setTextColor(getResources().getColor(R.color.green2));
                statisticsT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                locationsT.setTextColor(getResources().getColor(R.color.white));
                locationsT.setBackgroundColor(getResources().getColor(R.color.green2));

                genderT.setTextColor(getResources().getColor(R.color.green2));
                genderT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                ageT.setTextColor(getResources().getColor(R.color.green2));
                ageT.setBackgroundColor(getResources().getColor(R.color.colorlightGrey));

                temp8.findViewById(R.id.layout_statistics).setVisibility(View.GONE);
                temp8.findViewById(R.id.layout_gender).setVisibility(View.GONE);
                temp8.findViewById(R.id.layout_age).setVisibility(View.GONE);
                temp8.findViewById(R.id.layout_location).setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean requireLogin() {
        return false;
    }
}

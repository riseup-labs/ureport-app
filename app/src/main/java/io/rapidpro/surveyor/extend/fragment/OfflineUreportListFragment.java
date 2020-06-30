package io.rapidpro.surveyor.extend.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.OfflineUreportDetailsActivity;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.extend.adapter.CustomAdapterPollList;
import io.rapidpro.surveyor.extend.api.SurveyorApi;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.SurveyorDao;
import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;
import io.rapidpro.surveyor.extend.entity.model.surveyor_api;
import io.rapidpro.surveyor.extend.util.CustomDialog;
import io.rapidpro.surveyor.extend.util.CustomDialogInterface;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.rapidpro.surveyor.extend.StaticMethods.isConnected;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class OfflineUreportListFragment extends BaseFragment implements CustomAdapterPollList.ItemClickListener {

    CustomAdapterPollList adapter;
    List<String> pollNameList= new ArrayList<>();
    List<String> pollIDList= new ArrayList<>();
    List<String> pollDateList= new ArrayList<>();
    RecyclerView recyclerView;

    boolean isFirstTime=false;
    boolean connected = false;

    Context context;
    View mainView;

    String lang_code = "en";
    int ColorPosition = 0;

    AppDatabase database;
    Retrofit retrofit;
    SurveyorDao surveyorDao;
    SurveyorApi api;
    Call<surveyor_api> call_surveyor;
    SwipeRefreshLayout surveyorRefresh;
    CustomAdapterPollList.ItemClickListener itemClickListener;

    String category_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1_fragment_ureport_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();
        mainView = getView();

        category_name = getActivity().getIntent().getExtras().getString("ResultName");
        ColorPosition = getActivity().getIntent().getExtras().getInt("ColorPosition");

        // Set Layout Color
        setLayoutBackground(ColorPosition);


        mainView.findViewById(R.id.progressBg).setVisibility(View.GONE);
        mainView.findViewById(R.id.progressBar).setVisibility(View.GONE);


        database = databaseConnection.getDatabase(context);

        String baseURL;

        if(StaticMethods.AppDistribution.equals("GV")){
            baseURL = SurveyorApi.BASE_URL_GV;
        }else{
            baseURL = SurveyorApi.BASE_URL_RV;
        }

        final OkHttpClient okHttpClient = StaticMethods.okHttpClient();

        retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        api = retrofit.create(SurveyorApi.class);

        surveyorDao = database.getSurveyor();
        List<SurveyorLocal> surveyorList = surveyorDao.getSurveys();

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");

        // Convert to Legacy Format
        pollNameList = new ArrayList<>();
        pollIDList = new ArrayList<>();
        pollDateList = new ArrayList<>();
        itemClickListener = this;

        for(SurveyorLocal x: surveyorList){
            JsonParser parser = new JsonParser();
            // Manage Language Here!
            JsonObject rootObj = null;

            if(lang_code.equals("my")){
                if(x.getMy_pack() != null){
                    rootObj = parser.parse(x.getMy_pack()).getAsJsonObject();
                }
            }else if(lang_code.equals("en")){
                if(x.getData_pack() != null){
                    rootObj = parser.parse(x.getData_pack()).getAsJsonObject();
                }
            }else if(lang_code.equals("bn")) {
                if(x.getBn_pack() != null){
                    rootObj = parser.parse(x.getBn_pack()).getAsJsonObject();
                }
            }

            if(rootObj != null){
                pollIDList.add(String.valueOf(x.getFlow_id()));
                pollNameList.add(rootObj.get("title").getAsString());
                pollDateList.add(rootObj.get("poll_date").getAsString());
            }

        }

        if(surveyorList.size() == 0){
            refreshUreport();
        }

        recyclerView = mainView.findViewById(R.id.all_topic_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);

        adapter = new CustomAdapterPollList(getSurveyor(), context, pollNameList, pollIDList, pollDateList, false, ColorPosition);
        adapter.setClickListener(itemClickListener);
        recyclerView.setAdapter(adapter);

        surveyorRefresh = getView().findViewById(R.id.ureportRefreshLayout);
        surveyorRefresh.setOnRefreshListener(() -> {
            playNotification(getSurveyor(), getContext(), R.raw.swipe_sound);
            refreshUreport();
        });
    }

    public void refreshUreport(){
        if(isConnected(getContext())){
            connected = true;
            new LongOperation().execute("");
        }else{
            new CustomDialog(getContext()).displayNoInternetDialog(new CustomDialogInterface() {
                @Override
                public void retry() {
                    refreshUreport();
                }
                @Override
                public void cancel() { }
            });
            if(surveyorRefresh != null){
                surveyorRefresh.setRefreshing(false);}
        }
    }

    public void setLayoutBackground(int ColorPos){
        int px = ColorPos;

        // Override: We can use multi-color
        // But for now we will use single color only.
        px = 2;

        if (px == 0) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(33, 159, 255));
        }
        if (px == 1) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(255, 88, 53));
        }
        if (px == 2) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(235, 190, 65));
        }
        if (px == 3) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(67, 219, 83));
        }
        if (px == 4) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(101, 101, 101));
        }
        if (px == 5) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(28, 161, 162));
        }
        if (px == 6) {
            mainView.findViewById(R.id.bg_color).setBackgroundColor(Color.rgb(7, 132, 183));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    boolean clickLock = false;

    @Override
    public void onItemClick(View view, int position) {
        if(clickLock){
            return;
        }else{
            clickLock = true;
            // Unlock after 2 s
            new Handler().postDelayed(() -> clickLock = false, 1500);
        }

        playNotification(getSurveyor(), getContext(), R.raw.button_click_yes, view);

        Intent intent = new Intent(context, OfflineUreportDetailsActivity.class);
        intent.putExtra("ResultName", "" +adapter.getItem(position));
        intent.putExtra("ResultID", "" + adapter.getId(position));
        intent.putExtra("ResultDate", "" + adapter.getDate(position));
        startActivity(intent);


    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Update Last Updated
            String last_updated = getSurveyor().getPreferences().getString("surveyor_date", "");
            //last_updated = "";

            call_surveyor = api.getSurveyor(1000, 0, last_updated);
            call_surveyor.enqueue(new Callback<surveyor_api>() {
                @Override
                public void onResponse(Call<surveyor_api> call, Response<surveyor_api> response) {
                    surveyor_api surveyorApi = response.body();
                    if(surveyorApi == null){
                        if(surveyorRefresh != null){
                            surveyorRefresh.setRefreshing(false);}
                        return;
                    }
                    if(surveyorApi.getData().size() == 0){
                        if(surveyorRefresh != null){
                            surveyorRefresh.setRefreshing(false);}
                        return;
                    }

                    String new_last_update = surveyorApi.getLast_updated();

                    List<SurveyorLocal> surveyorLocals = surveyorApi.getData();
                    //surveyorDao.deleteAllSurveyor();

                    for(SurveyorLocal x: surveyorLocals){

                        //surveyorDao.insert(x);

                        if(surveyorDao.doesSurveyExists(x.getFlow_id()) > 0){
                            // Old UReport: update
                            x.primaryKey = surveyorDao.getSurvey_pKey(x.getFlow_id());
                            surveyorDao.update(x);
                        }else{
                            // New UReport: insert
                            surveyorDao.insert(x);
                        }

                    }

                    // Save Preference
                    try {
                        getSurveyor().setPreference("surveyor_date", new_last_update);
                    } catch (Exception e){
                        //
                    }

                    // Reload Recycler View
                    pollNameList = new ArrayList<>();
                    pollIDList = new ArrayList<>();
                    pollDateList = new ArrayList<>();

                    List<SurveyorLocal> surveyorLocalsList = surveyorDao.getSurveys();

                    for(SurveyorLocal x: surveyorLocalsList){
                        JsonParser parser = new JsonParser();
                        JsonObject rootObj = null;

                        if(lang_code.equals("my")){
                            if(x.getMy_pack() != null){
                                rootObj = parser.parse(x.getMy_pack()).getAsJsonObject();
                            }
                        }else if(lang_code.equals("en")){
                            if(x.getData_pack() != null){
                                rootObj = parser.parse(x.getData_pack()).getAsJsonObject();
                            }
                        }else if(lang_code.equals("bn")) {
                            if(x.getBn_pack() != null){
                                rootObj = parser.parse(x.getBn_pack()).getAsJsonObject();
                            }
                        }

                        if(rootObj != null){
                            pollIDList.add(String.valueOf(x.getFlow_id()));
                            pollNameList.add(rootObj.get("title").getAsString());
                            pollDateList.add(rootObj.get("poll_date").getAsString());
                        }

                    }

                    try {

                        StaticMethods.setLocalUpdateDate(getSurveyor(), "ureport_offline_last_updated_local");

                        if(adapter != null && recyclerView != null){
                            adapter = new CustomAdapterPollList(getSurveyor(), context, pollNameList, pollIDList, pollDateList, false, ColorPosition);
                            recyclerView.setAdapter(adapter);
                            adapter.setClickListener(itemClickListener);
                            adapter.notifyDataSetChanged();
                        }

                        if(surveyorRefresh != null){
                            surveyorRefresh.setRefreshing(false);
                        }

                        playNotification(getSurveyor(), getContext(), R.raw.sync_complete);
                        adapter.notifyDataSetChanged();

                    }catch(Exception e){
                        //
                    }
                }

                @Override
                public void onFailure(Call<surveyor_api> call, Throwable t) {
                    if(surveyorRefresh != null){
                        surveyorRefresh.setRefreshing(false);}
                }
            });

            return "ureport";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.matches("ureport")){
                final Handler handler = new Handler();
                handler.postDelayed(() -> {

                    if(isFirstTime=true){
                        isFirstTime=false;
                    }

                    if(adapter != null){
                        try{
                            adapter.notifyDataSetChanged();
                        }catch(Exception e){
                            //
                        }
                    }

                }, 100);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    public boolean requireLogin() {
        return false;
    }


}

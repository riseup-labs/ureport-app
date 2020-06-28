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

import java.util.ArrayList;
import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.extend.OfflineUreportDetailsActivity;
import io.rapidpro.surveyor.extend.UreportDetailsActivity;
import io.rapidpro.surveyor.extend.adapter.CustomAdapterPollList;
import io.rapidpro.surveyor.extend.api.UreportApi;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.UReportDao;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;
import io.rapidpro.surveyor.extend.entity.model.ureport_api;
import io.rapidpro.surveyor.extend.util.CustomDialog;
import io.rapidpro.surveyor.extend.util.CustomDialogInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.rapidpro.surveyor.extend.StaticMethods.isConnected;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class UreportListFragment extends BaseFragment implements CustomAdapterPollList.ItemClickListener {

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
    UReportDao uReportDao;
    UreportApi api;
    Call<ureport_api> call_ureport;
    SwipeRefreshLayout ureportRefresh;
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
            baseURL = UreportApi.BASE_URL_GV;
        }else{
            baseURL = UreportApi.BASE_URL_RV;
        }

        retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(UreportApi.class);

        uReportDao = database.getUReports();
        List<UReportLocal> uReportLocalList = uReportDao.getUReportsByCategory(category_name);

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");

        // Convert to Legacy Format
        pollNameList = new ArrayList<>();
        pollIDList = new ArrayList<>();
        pollDateList = new ArrayList<>();
        itemClickListener = this;

        for(UReportLocal x: uReportLocalList){
            JsonParser parser = new JsonParser();
            // Manage Language Here!
            JsonObject rootObj = null;

            if(lang_code.equals("my")){
                if(x.getMy_pack() != null){
                    rootObj = parser.parse(x.getMy_pack()).getAsJsonObject();
                }
            }else if(lang_code.equals("en")){
                if(x.getEn_pack() != null){
                    rootObj = parser.parse(x.getEn_pack()).getAsJsonObject();
                }
            }else if(lang_code.equals("bn")) {
                if(x.getData_pack() != null){
                    rootObj = parser.parse(x.getData_pack()).getAsJsonObject();
                }
            }

            if(rootObj != null){
                pollIDList.add(String.valueOf(x.getId()));
                pollNameList.add(rootObj.get("title").getAsString());
                pollDateList.add(rootObj.get("poll_date").getAsString());
            }

        }

        if(uReportLocalList.size() == 0){
            refreshUreport();
        }

        recyclerView = mainView.findViewById(R.id.all_topic_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);

        adapter = new CustomAdapterPollList(context, pollNameList, pollIDList, pollDateList, false, ColorPosition);
        adapter.setClickListener(itemClickListener);
        recyclerView.setAdapter(adapter);

        ureportRefresh = getView().findViewById(R.id.ureportRefreshLayout);
        ureportRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                playNotification(getSurveyor(), getContext(), R.raw.swipe_sound);
                refreshUreport();
            }
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
            if(ureportRefresh != null){ureportRefresh.setRefreshing(false);}
        }
    }

    public void setLayoutBackground(int ColorPos){
        int px = ColorPos;

        // Override
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

        Intent intent = new Intent(context, UreportDetailsActivity.class);
        intent.putExtra("ResultName", "" +adapter.getItem(position));
        intent.putExtra("ResultID", "" + adapter.getId(position));
        intent.putExtra("ResultDate", "" + adapter.getDate(position));
        startActivity(intent);


    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Update Last Updated
            String last_updated = getSurveyor().getPreferences().getString("ureport_date", "");

            call_ureport = api.getUreport(100, 0, last_updated);
            call_ureport.enqueue(new Callback<ureport_api>() {
                @Override
                public void onResponse(Call<ureport_api> call, Response<ureport_api> response) {
                    ureport_api ureportApi = response.body();
                    if(ureportApi == null){
                        if(ureportRefresh != null){ureportRefresh.setRefreshing(false);}
                        return;
                    }
                    if(ureportApi.getData().size() == 0){
                        if(ureportRefresh != null){ureportRefresh.setRefreshing(false);}
                        return;
                    }

                    String new_last_update = ureportApi.getLast_updated();

                    List<UReportLocal> ureports = ureportApi.getData();

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

                    // Reload Recycler View
                    pollNameList = new ArrayList<>();
                    pollIDList = new ArrayList<>();
                    pollDateList = new ArrayList<>();

                    List<UReportLocal> uReportLocalList = uReportDao.getUReports();

                    for(UReportLocal x: uReportLocalList){
                        JsonParser parser = new JsonParser();
                        JsonObject rootObj = null;

                        if(lang_code.equals("my")){
                            if(x.getMy_pack() != null){
                                rootObj = parser.parse(x.getMy_pack()).getAsJsonObject();
                            }
                        }else{
                            if(x.getEn_pack() != null){
                                rootObj = parser.parse(x.getEn_pack()).getAsJsonObject();
                            }
                        }

                        if(rootObj != null){
                            pollIDList.add(String.valueOf(x.getId()));
                            pollNameList.add(rootObj.get("title").getAsString());
                            pollDateList.add(rootObj.get("poll_date").getAsString());
                        }
                    }

                    if(adapter != null && recyclerView != null) {
                        adapter = new CustomAdapterPollList(context, pollNameList, pollIDList, pollDateList, false, ColorPosition);
                        recyclerView.setAdapter(adapter);
                        adapter.setClickListener(itemClickListener);
                        adapter.notifyDataSetChanged();
                    }

                    // Save Preference

                    try {
                        getSurveyor().setPreference("ureport_date", new_last_update);
                    } catch(Exception e) {
                        //
                    }

                    if(ureportRefresh != null){ureportRefresh.setRefreshing(false);}
                }

                @Override
                public void onFailure(Call<ureport_api> call, Throwable t) {
                    if(ureportRefresh != null){ureportRefresh.setRefreshing(false);}
                }
            });

            return "ureport";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.matches("ureport")){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //mainView.findViewById(R.id.progressBg).setVisibility(View.GONE);
                        //mainView.findViewById(R.id.progressBar).setVisibility(View.GONE);

                        if(isFirstTime=true){
                            isFirstTime=false;
                        }

                        if(adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, 1000);
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

package io.rapidpro.surveyor.extend.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.extend.StoriesListActivity;
import io.rapidpro.surveyor.extend.adapter.CustomAdapterStories;
import io.rapidpro.surveyor.extend.api.StoriesApi;
import io.rapidpro.surveyor.extend.database.AppDatabase;
import io.rapidpro.surveyor.extend.database.databaseConnection;
import io.rapidpro.surveyor.extend.entity.dao.StoriesDao;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
import io.rapidpro.surveyor.extend.entity.model.story_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_data;
import io.rapidpro.surveyor.extend.util.CustomDialog;
import io.rapidpro.surveyor.extend.util.CustomDialogInterface;
import me.myatminsoe.mdetect.MDetect;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class StoriesListFragment extends BaseFragment implements CustomAdapterStories.ItemClickListener {


    AppDatabase database;
    Retrofit retrofit;
    StoriesApi storiesApi;
    StoriesDao storiesDao;
    Call<story_api> story_apiCall;
    List<StoriesLocal> localStories;
    CustomAdapterStories storiesAdapter;
    SwipeRefreshLayout storiesRefresh;
    CustomAdapterStories.ItemClickListener itemClickListener;

    RecyclerView recyclerView;
    String lang_code = "en";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1_fragment_stories, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Disable Strict Mode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        database = databaseConnection.getDatabase(getContext());
        storiesDao = database.getStories();


        localStories = new ArrayList<>();
        localStories = storiesDao.getStoriesList();

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");

        storiesAdapter = new CustomAdapterStories(getContext(), localStories, lang_code);

        recyclerView = getView().findViewById(R.id.storiesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(storiesAdapter);

        itemClickListener = this;
        storiesAdapter.setClickListener(itemClickListener);

        storiesRefresh = getView().findViewById(R.id.storiesRefreshLayout);
        storiesRefresh.setOnRefreshListener(() -> {
            playNotification(getSurveyor(), getContext(), R.raw.swipe_sound);
            updateStories();
        });
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

        StoriesListActivity sla = (StoriesListActivity) getActivity();
        int storyId = localStories.get(position).getId();
        sla.beginTransition(view, storyId);

    }

    public void updateStories() {
        if(StaticMethods.isConnected(getContext())){
            //downloadStories();
            new LongOperation().execute();
        }else{
            new CustomDialog(getContext()).displayNoInternetDialog(new CustomDialogInterface() {
                @Override
                public void retry() {
                    updateStories();
                }
                @Override
                public void cancel() { }
            });
            if(storiesRefresh != null){storiesRefresh.setRefreshing(false);}
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Download Stories
            String last_updated = getSurveyor().getPreferences().getString("story_date", "");

            String baseURL;
            if(StaticMethods.AppDistribution.equals("GV")){
                baseURL = StoriesApi.BASE_URL_GV;
            }else{
                baseURL = StoriesApi.BASE_URL_RV;
            }

            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
            storiesApi = retrofit.create(StoriesApi.class);
            story_apiCall = storiesApi.getStories(last_updated, 100);
            story_apiCall.enqueue(new Callback<story_api>() {
                @Override
                public void onResponse(Call<story_api> call, Response<story_api> response) {
                    if(response.body() != null){


                        String new_last_updated = response.body().getLast_updated();

                        try {
                            playNotification(getSurveyor(), getContext(), R.raw.sync_complete);
                            getSurveyor().setPreference("story_date", new_last_updated);
                        } catch (Exception e) {
                            //
                        }

                        if(response.body().getData().size() == 0){
                            if(storiesRefresh != null){storiesRefresh.setRefreshing(false);}

                            // Refresh Images
                            // new MiniOperation().execute();

                            return;
                        }

                        List<StoriesLocal> storiesLocals = response.body().getData();

                        for(StoriesLocal s: storiesLocals) {
                            if(storiesDao.doesStoryExists(s.getId()) > 0){
                                // Old Story: update
                                s.primaryKey = storiesDao.getStory_pKey(s.getId());
                                storiesDao.update(s);
                            }else{
                                // New Story: insert
                                storiesDao.insert(s);
                            }
                        }

                        // Update Last Updated

                        try {

                            if (storiesRefresh != null) {
                                storiesRefresh.setRefreshing(false);
                            }

                            localStories = storiesDao.getStoriesList();
                            storiesAdapter = new CustomAdapterStories(getContext(), localStories, lang_code);
                            storiesAdapter.setClickListener(itemClickListener);
                            recyclerView.setAdapter(storiesAdapter);
                            storiesAdapter.notifyDataSetChanged();

                        } catch(Exception e) {
                            //
                        }
                    }else{
                        if(storiesRefresh != null){storiesRefresh.setRefreshing(false);}
                        playNotification(getSurveyor(), getContext(), R.raw.sync_complete);
                    }
                }

                @Override
                public void onFailure(Call<story_api> call, Throwable t) {
                    try {
                        if (storiesRefresh != null) {
                            storiesRefresh.setRefreshing(false);
                        }
                        playNotification(getSurveyor(), getContext(), R.raw.sync_complete);
                    } catch (Exception e) {
                        //
                    }
                }
            });

            // Check For Deleted Story
            final String story_delete_last_update = getSurveyor().getPreferences().getString("story_delete_last_update", "");
            StoriesApi storiesDeleteApi = retrofit.create(StoriesApi.class);
            Call<story_delete_api> storyDelete_apiCall = storiesDeleteApi.getDeletedStories(story_delete_last_update);
            storyDelete_apiCall.enqueue(new Callback<story_delete_api>() {

                @Override
                public void onResponse(Call<story_delete_api> call, Response<story_delete_api> response) {
                    if(response.body().getData() == null){
                        return;
                    }

                    List<story_delete_data> deleted_stories = response.body().getData();

                    for(story_delete_data deleted: deleted_stories) {
                        if(storiesDao.doesStoryExists(deleted.getStory_id()) > 0){
                            // Story Deleted on Server
                            storiesDao.deleteFromStoryById(deleted.getStory_id());
                        }
                    }

                    // Update Story Delete Last_Update
                    try {

                        getSurveyor().setPreference("story_delete_last_update", response.body().getLast_updated());

                        localStories = storiesDao.getStoriesList();
                        storiesAdapter = new CustomAdapterStories(getContext(), localStories, lang_code);
                        storiesAdapter.setClickListener(itemClickListener);
                        recyclerView.setAdapter(storiesAdapter);
                        storiesAdapter.notifyDataSetChanged();

                        if (storiesRefresh != null) {
                            storiesRefresh.setRefreshing(false);
                        }

                    } catch(Exception e) {
                        //
                    }

                }

                @Override
                public void onFailure(Call<story_delete_api> call, Throwable t) {

                }

            });

            List<StoriesLocal> storiesLocals = database.getStories().getStoriesList();

            //Collections.reverse(storiesLocals);
            for(StoriesLocal s: storiesLocals){
                try {
                    // Download Story Image
                    if (s.getContent_image() == null) {
                        continue;
                    }
                    if (s.getContent_image().equals("")) {
                        continue;
                    }

                    Context context = getSurveyor().getApplicationContext();
                    String imageURL = s.getContent_image();

                    String file_path = "story_image_" + getMD5(imageURL);
                    String file_path_full = context.getFilesDir() + "/story_image_" + getMD5(imageURL);

                    File file = new File(file_path_full);
                    if (file.exists()) {
                        if (file.length() > 100 * 1024) {
                            // File with At Least 50 KB Data Exists: skip
                            continue;
                        }
                    }

                    OkHttpClient okHttpClient2 = new OkHttpClient();
                    okhttp3.Request okRequest = new okhttp3.Request.Builder().url(imageURL).build();
                    okhttp3.Response okResponse = null;

                    try {
                        okResponse = okHttpClient2.newCall(okRequest).execute();
                        InputStream inputStream = okResponse.body().byteStream();

                        try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        }

                        inputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e1) {
                    //
                }

            }

            for(StoriesLocal s: storiesLocals){
                try {
                    // Download Story Video
                    if (s.getStory_video() == null) {
                        continue;
                    }
                    if (s.getStory_video().equals("")) {
                        continue;
                    }

                    Context context = getSurveyor().getApplicationContext();
                    String videoURL = s.getStory_video();

                    String file_path = "story_video_" + getMD5(videoURL);
                    String file_path_full = context.getFilesDir() + "/story_video_" + getMD5(videoURL);

                    File file = new File(file_path_full);
                    if (file.exists()) {
                        if (file.length() > 100 * 1024) {
                            // File with At Least 50 KB Data Exists: skip
                            continue;
                        }
                    }

                    OkHttpClient okHttpClient3 = new OkHttpClient();
                    okhttp3.Request okRequest = new okhttp3.Request.Builder().url(videoURL).build();
                    okhttp3.Response okResponse = null;

                    try {
                        okResponse = okHttpClient3.newCall(okRequest).execute();
                        InputStream inputStream = okResponse.body().byteStream();

                        try (OutputStream output = context.openFileOutput(file_path, context.MODE_PRIVATE)) {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        }

                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e1) {
                    //
                }
            }

            return "images";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.matches("images")){

                try {
                    localStories = storiesDao.getStoriesList();
                    storiesAdapter = new CustomAdapterStories(getContext(), localStories, lang_code);
                    storiesAdapter.setClickListener(itemClickListener);
                    recyclerView.setAdapter(storiesAdapter);
                    storiesAdapter.notifyDataSetChanged();
                } catch(Exception e) {
                    //
                }

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


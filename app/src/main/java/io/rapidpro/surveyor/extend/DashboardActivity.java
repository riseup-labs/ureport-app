package io.rapidpro.surveyor.extend;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Fade;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.rapidpro.surveyor.BuildConfig;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.BaseActivity;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.extend.adapter.CustomScrollAdapter;
import io.rapidpro.surveyor.extend.adapter.DashboardList_RV;
import io.rapidpro.surveyor.ui.ViewCache;

import static io.rapidpro.surveyor.extend.StaticMethods.AppDistribution;
import static io.rapidpro.surveyor.extend.StaticMethods.gotoSurveyor;
import static io.rapidpro.surveyor.extend.StaticMethods.pending_restart;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class DashboardActivity extends BaseActivity implements CustomScrollAdapter.ClickListener {

    // Initialize
    DiscreteScrollView scrollView;
    List<DashboardList_RV> dashboardLists = new ArrayList<>();
    CustomScrollAdapter scrollAdapter;
    ConstraintLayout btn_stories, btn_opinions, btn_results, btn_settings;
    ImageView updateIcon;
    int selectedButton = 0;
    String orgUUID = "";
    int pending;
    ViewCache cache;
    private Org org;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_dashboard);

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        setLang_code(lang_code);
        orgUUID = getIntent().getStringExtra(SurveyorIntent.EXTRA_ORG_UUID);
        context = this;

        findViews();
        initDashboard();
        cache = getViewCache();

        if(org == null && !orgUUID.equals("")) {
            try {
                org = getSurveyor().getOrgService().get(orgUUID);
            } catch (Exception e) {
                //
            }
        }

        if(org != null) {
            cache.setText(R.id.pendingOpinion, String.valueOf(pending));

            if(pending == 0){
                StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 0f, 0f, 1);
            }else{
                StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 1f, 1f, 1);
            }
        }else{
            StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 0f, 0f, 1);
        }

        if(gotoSurveyor && isLoggedIn()){
            // Fix Background Color
            //findViewById(R.id.root_layout).setBackgroundColor(Color.argb(255,255,247,239));

            new Handler().postDelayed(() -> {
                scrollView.scrollToPosition(2);
                new Handler().postDelayed(() -> {
                    // Simulate Click on Surveyor
                    onItemClick(2, scrollView.getViewHolder(2).itemView);
                    gotoSurveyor = false;
                }, 300);
            }, 10);
        }
        if(gotoSurveyor && !isLoggedIn()){
            // Fix Background Color
            //findViewById(R.id.root_layout).setBackgroundColor(Color.argb(255,255,247,239));

            new Handler().postDelayed(() -> {
                // Just Scroll : No Click
                scrollView.scrollToPosition(2);
                new Handler().postDelayed(() -> {
                    gotoSurveyor = false;
                }, 300);
            }, 10);
        }
    }

    /**
     * findViewById in one place
     */
    void findViews() {
        scrollView = findViewById(R.id.scrollView);
        btn_stories = findViewById(R.id.btn_stories);
        btn_opinions = findViewById(R.id.btn_opinions);
        btn_results = findViewById(R.id.btn_results);
        btn_settings = findViewById(R.id.btn_settings);
        updateIcon = findViewById(R.id.update_icon);
    }

    /**
     * Checks for Pending Submission and Redraws Texts for Langauge Change
     */
    @Override
    public void onResume(){

        getWindow().setEnterTransition(null);

        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        setLang_code(lang_code);

        if(org != null) {
            pending = getSurveyor().getSubmissionService().getCompletedCount(org);
            cache.setText(R.id.pendingOpinion, String.valueOf(pending));

            if(pending == 0){
                StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 0f, 0f, 1);
            }else{
                if(selectedButton == 2){
                    StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 1.2f, 1.2f, 1);
                }else{
                    StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 1f, 1f, 1);
                }
            }
        }else{
            StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 0f, 0f, 0f, 0f, 1);
        }

        // Change Texts
        ((TextView) findViewById(R.id.text_app_name)).setText(R.string.v1_welcome_to_app);
        ((TextView) btn_stories.getViewById(R.id.btn_text)).setText(R.string.v1_stories);
        ((TextView) btn_opinions.getViewById(R.id.btn_text)).setText(R.string.v1_ureport);
        ((TextView) btn_results.getViewById(R.id.btn_text)).setText(R.string.v1_survey);
        ((TextView) btn_settings.getViewById(R.id.btn_text)).setText(R.string.v1_settings);
        ((TextView) btn_stories.getViewById(R.id.btn_text)).setTextColor(Color.rgb(33,33,33));
        dashboardLists.get(0).setName(getString(R.string.v1_stories));
        dashboardLists.get(1).setName(getString(R.string.v1_ureport));
        dashboardLists.get(2).setName(getString(R.string.v1_survey));
        dashboardLists.get(3).setName(getString(R.string.v1_settings));
        //scrollAdapter = new CustomScrollAdapter(dashboardLists);
        scrollAdapter.notifyDataSetChanged();

        super.onResume();
        doRestart();
    }

    /**
     * Checks for Force Restart Signal
     */
    void doRestart() {
        // Restart Activity
        if(pending_restart){
            pending_restart = false;
            this.recreate();
        }
    }

    /**
     * Initialize Dashboard Components
     */
    void initDashboard() {

        dashboardLists.add(new DashboardList_RV(getString(R.string.v1_stories), R.drawable.v1_header_image_stories, R.drawable.v1_bg_shade_stories));
        dashboardLists.add(new DashboardList_RV(getString(R.string.v1_ureport), R.drawable.v1_header_image_results, R.drawable.v1_bg_shade_results));
        dashboardLists.add(new DashboardList_RV(getString(R.string.v1_survey), R.drawable.v1_header_image_opinion, R.drawable.v1_bg_shades_opinions));
        dashboardLists.add(new DashboardList_RV(getString(R.string.v1_settings), R.drawable.v1_header_image_settings, R.drawable.v1_bg_shade_settings));
        //dashboardLists.add(new DashboardList(getString(R.string.v1_settings), "test.json", "animation/test", R.drawable.v1_card_bg_settings, R.drawable.v1_bg_shade_settings));
        scrollAdapter = new CustomScrollAdapter(dashboardLists);
        scrollAdapter.setOnItemClickListener(this);
        scrollView.setAdapter(scrollAdapter);

        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.CENTER) // CENTER is a default one
                .build());

        setButtonClickLisener(btn_stories, 0);
        setButtonClickLisener(btn_opinions, 1);
        setButtonClickLisener(btn_results, 2);
        setButtonClickLisener(btn_settings, 3);

        StaticMethods.scaleView(btn_stories.getViewById(R.id.btn_image), 1f, 1f, 1.2f, 1.2f, 0);
        StaticMethods.scaleView(btn_stories.getViewById(R.id.btn_text), 1f, 1f, 1.2f, 1.2f, 0);

        scrollView.addOnItemChangedListener((viewHolder, i) -> animateButtonClick(i));

        scrollView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScrollStart(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                playNotification(getSurveyor(), getApplicationContext(), R.raw.swipe_sound, viewHolder.itemView);
            }

            @Override
            public void onScrollEnd(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                // Animate Bottom Bars
                setBottomBar(i);
            }

            @Override
            public void onScroll(float v, int i, int i1, @Nullable RecyclerView.ViewHolder viewHolder, @Nullable RecyclerView.ViewHolder t1) {
                //
            }
        });

        // Update Icon
        int currentVersion = BuildConfig.VERSION_CODE;
        int availableVersion = getSurveyor().getPreferences().getInt("apk_version_code", 0);
        int apkStatus = getSurveyor().getPreferences().getInt("apk_status", 0);

        if(availableVersion > currentVersion && apkStatus == 1){
            // New Version Available: Show Download Icon
            updateIcon.setVisibility(View.VISIBLE);
            updateIcon.setOnClickListener(view -> {

                playNotification(getSurveyor(), context, R.raw.button_click_yes, view);

                final Dialog dialog4 = new Dialog(context);
                dialog4.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog4.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog4.setContentView(R.layout.v1_dialog_ui);
                dialog4.findViewById(R.id.textSubText).setVisibility(View.GONE);
                ((TextView) dialog4.findViewById(R.id.textMainText)).setText(R.string.v1_new_version_dashboard);
                ((TextView) dialog4.findViewById(R.id.button_yes_text)).setText("Ok");
                ((TextView) dialog4.findViewById(R.id.button_no_text)).setText("Cancel");

                dialog4.findViewById(R.id.button_yes).setOnClickListener(view1 -> {
                    playNotification(getSurveyor(), context, R.raw.button_click_yes, view1);
                    dialog4.dismiss();

                    try {
                        String file_url = getSurveyor().getPreferences().getString("apk_url", "");
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(file_url)));
                    } catch (Exception e) {
                        //
                    }

                });

                dialog4.findViewById(R.id.button_no).setOnClickListener(view2 -> {
                    playNotification(getSurveyor(), context, R.raw.button_click_no, view2);
                    dialog4.dismiss();
                });

                dialog4.show();


            });

            // Updater Zoom Animation
            updaterAnimation();
        }else{
            updateIcon.setVisibility(View.GONE);
        }

    }

    /**
     * Updater Icon Zoom Animation
     */
    void updaterAnimation() {
        try {
            new Handler().postDelayed(() -> {

                // Zoom In
                StaticMethods.scaleView(
                        findViewById(R.id.update_icon),
                        1.0f, 1.0f,
                        1.2f, 1.2f,
                        250);

                // Zoom Out
                new Handler().postDelayed(() -> {
                    StaticMethods.scaleView(findViewById(R.id.update_icon), 1.2f, 1.2f, 1.0f, 1.0f, 200);
                }, 260);

                updaterAnimation();
            }, 3000);
        } catch(Exception e) {
            //
        }
    }

    /**
     * Top Button Click Listener
     * @param button
     * @param id
     */
    void setButtonClickLisener(ConstraintLayout button, final int id){
        button.setOnClickListener(view -> {
            if(selectedButton == id){
                onItemClick(id, scrollView.getViewHolder(id).itemView);
            }
            scrollView.smoothScrollToPosition(id);
        });
    }

    /**
     * Animate Buttons
     * @param position
     */
    void animateButtonClick(final int position){
        int newColor = 0, prevColor = 0;

        ConstraintLayout selected_button = null, previous_button = null;

        switch(position){
            case 0:
                selected_button = btn_stories; newColor = Color.argb(255,239,255,247); break;
            case 1:
                selected_button = btn_opinions; newColor = Color.argb(255,255,253,241); break;
            case 2:
                selected_button = btn_results; newColor = Color.argb(255,255,247,239); break;
            case 3:
                selected_button = btn_settings; newColor = Color.argb(255,249,253,255); break;
        }
        switch(selectedButton){
            case 0:
                previous_button = btn_stories; prevColor = Color.argb(255,239,255,247); break;
            case 1:
                previous_button = btn_opinions; prevColor = Color.argb(255,255,253,241); break;
            case 2:
                previous_button = btn_results; prevColor = Color.argb(255,255,247,239); break;
            case 3:
                previous_button = btn_settings; prevColor = Color.argb(255,249,253,255); break;
        }

        if(selected_button == null || previous_button == null){return;}

        if(selectedButton == position){ // Same Button or No Click Event
            return;
        }else{
            //new Handler().postDelayed(() -> skipFirst = true, 1000);
        }

        StaticMethods.scaleView(previous_button.getViewById(R.id.btn_image), 1.2f, 1.2f, 1f, 1f, 150);
        StaticMethods.scaleView(previous_button.getViewById(R.id.btn_text), 1.2f, 1.2f, 1f, 1f, 150);

        StaticMethods.scaleView(selected_button.getViewById(R.id.btn_image), 1f, 1f, 1.2f, 1.2f, 300);
        StaticMethods.scaleView(selected_button.getViewById(R.id.btn_text), 1f, 1f, 1.2f, 1.2f, 300);

        // Opinions Number Circle
        if(selected_button == btn_results && pending > 0){
            StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 1f, 1f, 1.2f, 1.2f, 300);
        }

        if(previous_button == btn_results && pending > 0){
            StaticMethods.scaleView(findViewById(R.id.pendingOpinion), 1.2f, 1.2f, 1f, 1f, 150);
        }

        // Change Button Color
        ((TextView) previous_button.getViewById(R.id.btn_text)).setTextColor(Color.rgb(97,97,97));
        ((TextView) selected_button.getViewById(R.id.btn_text)).setTextColor(Color.rgb(33,33,33));


        // Animate Color
        // colorChangeAnimator(prevColor, newColor);
        selectedButton = position;
    }

    boolean clickLock = false;

    /**
     * Start new Activity with Shared Component(s)
     * @param position
     * @param v
     */
    @Override
    public void onItemClick(int position, View v) {

        if(clickLock){
            return;
        }else{
            clickLock = true;
            // Unlock after 2 s
            new Handler().postDelayed(() -> clickLock = false, 1500);
        }

        //ImageView imageSun = findViewById(R.id.image_sun);
        //View bgColor = v.findViewById(R.id.bgColor);

        ImageView cardImage = v.findViewById(R.id.cardImage);
        View bgShade = v.findViewById(R.id.bg_shadow);
        TextView activityName = v.findViewById(R.id.activityName);
        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, v);

        Intent intent = null;

        if(position == 0) {intent = new Intent(DashboardActivity.this, StoriesListActivity.class);}

        if(AppDistribution.equals("RV")){
            if(position == 1) {intent = new Intent(DashboardActivity.this, OfflineUreportListActivity.class);}
        }else{
            if(position == 1) {intent = new Intent(DashboardActivity.this, UreportCategoryActivity.class);}
        }

        if(position == 2) {intent = new Intent(DashboardActivity.this, SurveyorActivity.class);}
        if(position == 3) {intent = new Intent(DashboardActivity.this, SettingsActivity.class);}

        if(position == 2 && !isLoggedIn()){
            gotoSurveyor = true;
            logout();
            return;
        }

        //Pair<View, String> p1 = Pair.create((View)imageSun, imageSun.getTransitionName());
        //Pair<View, String> p2 = Pair.create((View)imageLottie, imageLottie.getTransitionName());
        //Pair<View, String> p3 = Pair.create((View)bgColor, bgColor.getTransitionName());

        Pair<View, String> p1 = Pair.create((View)cardImage, cardImage.getTransitionName());
        Pair<View, String> p2 = Pair.create((View)bgShade, bgShade.getTransitionName());
        Pair<View, String> p3 = Pair.create((View)activityName, activityName.getTransitionName());

        intent.putExtra(SurveyorIntent.EXTRA_ORG_UUID, orgUUID);


        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(DashboardActivity.this, p1, p2, p3);

        Fade fade = new Fade();
        fade.excludeChildren(cardImage, true);
        fade.excludeChildren(bgShade, true);
        fade.excludeChildren(activityName, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        startActivity(intent, options.toBundle());

    }

    String lang_code = "en";

    /**
     * Switch Language Code
     * @param lang_code
     */
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
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    View lastBottomView = null;

    /**
     * Select / Move Bottom Bar in the Dashboard
     * @param num
     */
    public void setBottomBar(int num){

        if(lastBottomView == null){
            lastBottomView = findViewById(R.id.bottom_bar_1);
        }

        View newView = null;

        if(num == 0){newView = findViewById(R.id.bottom_bar_1);}
        if(num == 1){newView = findViewById(R.id.bottom_bar_2);}
        if(num == 2){newView = findViewById(R.id.bottom_bar_3);}
        if(num == 3){newView = findViewById(R.id.bottom_bar_4);}

        int colorGrey = Color.argb(255, 149, 241, 122);
        int colorBlack = Color.argb(255, 0, 0, 0);

        ValueAnimator colorAnim = ObjectAnimator.ofInt(lastBottomView, "backgroundColor", colorBlack, colorGrey);
        colorAnim.setDuration(500);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(0);
        colorAnim.start();

        ValueAnimator colorAnim2 = ObjectAnimator.ofInt(newView, "backgroundColor", colorGrey, colorBlack);
        colorAnim2.setDuration(500);
        colorAnim2.setEvaluator(new ArgbEvaluator());
        colorAnim2.setRepeatCount(0);
        colorAnim2.start();

        lastBottomView = newView;
    }

    /**
     * Change Dashboard Background Color with Animation
     * @param from
     * @param to
     */
    public void colorChangeAnimator(int from, int to){
        if(gotoSurveyor){
            findViewById(R.id.root_layout).setBackgroundColor(Color.argb(255,255,247,239));
            return;
        }

        ValueAnimator colorAnim = ObjectAnimator.ofInt(findViewById(R.id.root_layout), "backgroundColor", from, to);
        colorAnim.setDuration(1000);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(0);
        colorAnim.start();
    }

    boolean lockBack = false;
    @Override
    public void onBackPressed() {

        if(!isLoggedIn()){
            super.onBackPressed();
            return;
        }

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

    @Override
    public boolean requireLogin() {
        return false;
    }
}

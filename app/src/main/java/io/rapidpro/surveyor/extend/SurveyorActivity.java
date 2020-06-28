package io.rapidpro.surveyor.extend;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vdurmont.semver4j.Semver;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.activity.BaseSubmissionsActivity;
import io.rapidpro.surveyor.activity.RunActivity;
import io.rapidpro.surveyor.adapter.FlowListAdapter;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.engine.Engine;
import io.rapidpro.surveyor.extend.util.CustomDialog;
import io.rapidpro.surveyor.extend.util.CustomDialogComponent;
import io.rapidpro.surveyor.extend.util.CustomDialogInterface;
import io.rapidpro.surveyor.fragment.FlowListFragment;
import io.rapidpro.surveyor.task.RefreshOrgTask;
import io.rapidpro.surveyor.ui.BlockingProgress;
import io.rapidpro.surveyor.ui.ViewCache;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class SurveyorActivity extends BaseSubmissionsActivity implements FlowListFragment.Container {

    ViewGroup headerLayout;
    CardView storyList;
    ImageView backButton;
    View bgColor;
    int pending = 0;

    Fragment flowListFragment;
    SwipeRefreshLayout flowlistRefresh;
    FragmentTransaction ft;
    FragmentManager fm;

    Context context;
    String orgUUID = "invalid";
    ViewCache cache;

    private Org org;
    private Dialog confirmRefreshDialog;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_survey_new);
        getWindow().setEnterTransition(null);

        isOpen = true;

        headerLayout = findViewById(R.id.headerLayout);
        storyList = findViewById(R.id.storyList);
        backButton = findViewById(R.id.backButton);
        bgColor = findViewById(R.id.bgColor);
        //flowListFragment = (FlowListFragment) getSupportFragmentManager().findFragmentById(R.id.flowFragment);

        cache = getViewCache();
        //cache.setVisible(R.id.pending_submission, false);
        cache.setText(R.id.button_pending, NumberFormat.getInstance().format(pending));

        if(pending == 0){
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 0f, 0f, 1);
        }else{
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 1f, 1f, 1);
        }

        orgUUID = getIntent().getStringExtra(SurveyorIntent.EXTRA_ORG_UUID);
        refresh();



        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        flowListFragment = new FlowListFragment();
        ft.add(R.id.frameLayout, flowListFragment, "myFragmentTag");
        ft.commit();

        ObjectAnimator.ofFloat(storyList, "alpha",  0, 1f).setDuration(500).start();
        ObjectAnimator.ofFloat(bgColor, "translationY", -500, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(storyList, "translationY", 1000, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(backButton, "translationX", -200, 0).setDuration(1000).start();

        backButton.setOnClickListener(view -> onBackPressed());

        context = getApplicationContext();



        ImageView submit_image = findViewById(R.id.submit_icon);
        submit_image.setOnClickListener(view -> {

            if(StaticMethods.isConnected(this)){
                playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view);
                onActionSubmit(view);
            }else{
                new CustomDialog(this).displayNoInternetDialog(new CustomDialogInterface() {

                    @Override
                    public void retry() {
                        submitToServer();
                    }

                    @Override
                    public void cancel() {
                        // None
                    }
                });
            }


        });

        // 5 Second Timer
        Timer5s();


    }

    public void submitToServer() {
        if(StaticMethods.isConnected(this)){
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes);
            doSubmit();
        }else{
            new CustomDialog(this).displayNoInternetDialog(new CustomDialogInterface() {

                @Override
                public void retry() {
                    submitToServer();
                }

                @Override
                public void cancel() {
                    // None
                }
            });
        }
    }

    void Timer5s() {
        try {
            new Handler().postDelayed(() -> {

                pending = getSurveyor().getSubmissionService().getCompletedCount(getOrg());
                //cache.setVisible(R.id.pending_submission, pending > 0);

                if(pending == 0){
                    StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 0f, 0f, 1);
                }else{
                    StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 1f, 1f, 1);
                }

                if (pending > 0) {
                    // Zoom In
                    StaticMethods.scaleView(
                            cache.getView(R.id.pending_submission),
                            1.0f, 1.0f,
                            1.2f, 1.2f,
                            250);

                    // Zoom Out
                    new Handler().postDelayed(() -> {
                        StaticMethods.scaleView(cache.getView(R.id.pending_submission), 1.2f, 1.2f, 1.0f, 1.0f, 200);
                        }, 260);

                }

                Timer5s();
            }, 3000);
        } catch(Exception e) {
            //
        }
    }

    @Override
    public void onBackPressed() {
        if(isOpen){ isOpen = false; }else{ return; }
        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, findViewById(R.id.backButton));
        headerLayout.setBackgroundColor(Color.parseColor("#00000000"));
        ObjectAnimator.ofFloat(storyList, "alpha",  1f, 0).setDuration(750).start();
        ObjectAnimator.ofFloat(bgColor, "translationY", 0, -500).setDuration(500).start();
        ObjectAnimator.ofFloat(storyList, "translationY", 0, 1000).setDuration(750).start();
        ObjectAnimator.ofFloat(backButton, "translationX", 0, -200).setDuration(1000).start();
        super.onBackPressed();
    }




    public void refreshFlows(){
        if(StaticMethods.isConnected(this)){
            confirmRefreshOrg(R.string.confirm_org_refresh);
            if(flowlistRefresh != null){flowlistRefresh.setRefreshing(false);}
        }else{
            new CustomDialog(this).displayNoInternetDialog(new CustomDialogInterface() {

                @Override
                public void retry() {
                    refreshFlows();
                }

                @Override
                public void cancel() {
                    // None
                    ((SwipeRefreshLayout) flowListFragment.getView().findViewById(R.id.flowRefreshLayout)).setRefreshing(false);
                }
            });

            if(flowlistRefresh != null){flowlistRefresh.setRefreshing(false);}
        }
    }

    private boolean skipFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
        //refresh();


        pending = getSurveyor().getSubmissionService().getCompletedCount(getOrg());
        //cache.setVisible(R.id.pending_submission, pending > 0);
        cache.setText(R.id.button_pending, String.valueOf(pending));

        if(pending == 0){
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 0f, 0f, 1);
        }else{
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 1f, 1f, 1);
        }

        if(!skipFirst){
//            fm = getSupportFragmentManager();
//            ft = fm.beginTransaction();
//            flowListFragment = new FlowListFragment();
//            ft.add(R.id.frameLayout, flowListFragment, "myFragmentTag");
//            ft.commit();


            Fragment frg = null;
            frg = getSupportFragmentManager().findFragmentByTag("myFragmentTag");
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        }

        skipFirst = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (confirmRefreshDialog != null) {
            confirmRefreshDialog.dismiss();
        }
    }

    protected void promptToUpgrade() {

        final Dialog dialog1 = new Dialog(this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1.setContentView(R.layout.v1_dialog_ui);
        dialog1.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) dialog1.findViewById(R.id.textMainText)).setText(R.string.unsupported_version);
        ((TextView) dialog1.findViewById(R.id.button_yes_text)).setText("Yes");
        ((TextView) dialog1.findViewById(R.id.button_no_text)).setText("No");

        dialog1.findViewById(R.id.button_yes).setOnClickListener(view -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view);
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=io.rapidpro.surveyor")));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.rapidpro.surveyor")));
            }

            dialog1.dismiss();
        });

        dialog1.findViewById(R.id.button_no).setOnClickListener(view -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view);
            dialog1.dismiss();
        });

        dialog1.show();

    }

    protected void refresh() {
        if(orgUUID.equals("invalid")){return;} // Not Logged In

        if (org == null) {
            try {
                org = getSurveyor().getOrgService().get(orgUUID);
            } catch (Exception e) {
                Logger.e("Unable to load org", e);
                showBugReportDialog();
                finish();
                return;
            }
        }

        setTitle(org.getName());

        FlowListAdapter adapter = (FlowListAdapter) getViewCache().getListViewAdapter(android.R.id.list);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        pending = getSurveyor().getSubmissionService().getCompletedCount(getOrg());
        //cache.setVisible(R.id.pending_submission, pending > 0);
        cache.setText(R.id.button_pending, NumberFormat.getInstance().format(pending));

        if(pending == 0){
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 0f, 0f, 1);
        }else{
            StaticMethods.scaleView(cache.getView(R.id.pending_submission), 0f, 0f, 1f, 1f, 1);
        }

        if (confirmRefreshDialog == null) {
            if (!org.hasAssets()) {
                // if this org doesn't have downloaded assets, ask the user if we can download them now


                if(StaticMethods.isConnected(this)){
                    confirmRefreshOrg(R.string.confirm_org_download);
                }else{
                    new CustomDialog(this).displayNoInternetDialog(new CustomDialogInterface() {

                        @Override
                        public void retry() {
                            refresh();
                            //confirmRefreshOrg(R.string.confirm_org_download);
                        }

                        @Override
                        public void cancel() {
                            // None
                        }
                    });
                }


            } else {
                for (Flow flow : org.getFlows()) {
                    if (!Engine.isSpecVersionSupported(flow.getSpecVersion())) {
                        Logger.w("Found flow " + flow.getUuid() + " with unsupported version " + flow.getSpecVersion());

                        Semver flowVersion = new Semver(flow.getSpecVersion(), Semver.SemverType.LOOSE);
                        if (flowVersion.isGreaterThan(Engine.currentSpecVersion())) {
                            // if this flow is a major version ahead of us... user needs to upgrade the app
                            promptToUpgrade();
                            break;
                        } else {
                            // if it is a major version behind, they should refresh the assets


                            if(StaticMethods.isConnected(this)){
                                confirmRefreshOrg(R.string.confirm_org_download);
                            }else{
                                new CustomDialog(this).displayNoInternetDialog(new CustomDialogInterface() {

                                    @Override
                                    public void retry() {
                                        confirmRefreshOrg(R.string.confirm_org_refresh_old);
                                    }

                                    @Override
                                    public void cancel() {
                                        // None
                                    }
                                });
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_org, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onActionRefresh(MenuItem item) {
        confirmRefreshOrg(R.string.confirm_org_refresh);
    }

    public void confirmRefreshOrg(int msgId) {

        new CustomDialog(this).displayCustomDialog(new CustomDialogComponent()
                        .setSubTextVisible(View.GONE)
                        .setMainText(getString(msgId))
                        .setButtonYes("Yes")
                        .setButtonNo("No"),
                new CustomDialogInterface() {
                    @Override
                    public void retry() {
                        doRefresh();
                    }

                    @Override
                    public void cancel() {
                        ((SwipeRefreshLayout) flowListFragment.getView().findViewById(R.id.flowRefreshLayout)).setRefreshing(false);
                    }
                });

    }

    RefreshOrgTask rotx;

    private void doRefresh() {
        final BlockingProgress progressModal = new BlockingProgress(SurveyorActivity.this, R.string.one_moment, R.string.refresh_org);
        progressModal.setOnDismissListener(dialogInterface -> {
            ((SwipeRefreshLayout) flowListFragment.getView().findViewById(R.id.flowRefreshLayout)).setRefreshing(false);
            rotx.cancel(true);
        });
        progressModal.show();



        rotx = new RefreshOrgTask(new RefreshOrgTask.Listener() {
            @Override
            public void onProgress(int percent) {
                progressModal.setProgress(percent);
            }
            public void onMessage(String message) {
                runOnUiThread(() -> progressModal.setMessage(message));
            }

            @Override
            public void onComplete() {
                try {
                    playNotification(getSurveyor(), getApplicationContext(), R.raw.sync_complete);

                    StaticMethods.setLocalUpdateDate(getSurveyor(), "surveyor_last_updated_local");

                } catch (Exception e){
                    //
                }
                refresh();
                progressModal.dismiss();
                ((SwipeRefreshLayout) flowListFragment.getView().findViewById(R.id.flowRefreshLayout)).setRefreshing(false);
            }

            @Override
            public void onFailure() {
                progressModal.dismiss();
                Toast.makeText(SurveyorActivity.this, getString(R.string.error_org_refresh), Toast.LENGTH_SHORT).show();
            }
        });
        rotx.execute(getOrg());
    }

    /**
     * @see BaseSubmissionsActivity#getPendingSubmissions()
     */
    @Override
    protected List<Submission> getPendingSubmissions() {
        return getSurveyor().getSubmissionService().getCompleted(getOrg());
    }

    @Override
    public Org getOrg() {
        return org;
    }

    /**
     * @see FlowListFragment.Container#getListItems()
     */
    @Override
    public List<Flow> getListItems() {
        return getOrg().getFlows();
    }

    /**
     * @see FlowListFragment.Container#onItemClick(Flow)
     */

    boolean clickLock = false;

    @Override
    public void onItemClick(Flow flow) {

        if(clickLock){
            return;
        }else{
            clickLock = true;
            // Unlock after 2 s
            new Handler().postDelayed(() -> clickLock = false, 1000);
        }

        final Dialog dialog3 = new Dialog(this);
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog3.setContentView(R.layout.v1_dialog_ui);

        String questionString = " Questions";
        if (flow.getQuestionCount() == 1) {
            questionString = " Question";
        }
        NumberFormat nf = NumberFormat.getInstance();
        dialog3.findViewById(R.id.img_start).setVisibility(View.VISIBLE);
        dialog3.findViewById(R.id.img_cancel).setVisibility(View.VISIBLE);
        ((TextView) dialog3.findViewById(R.id.textMainText)).setText(flow.getName());
        ((TextView) dialog3.findViewById(R.id.textSubText)).setText(
                nf.format(flow.getQuestionCount()) + questionString + " " + "(v" + nf.format(flow.getRevision()) + ")");

        final Flow dialog_flow = flow;

        dialog3.findViewById(R.id.button_yes).setOnClickListener(view -> {

            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view);
            Intent intent = new Intent(context, RunActivity.class);
            intent.putExtra(SurveyorIntent.EXTRA_ORG_UUID, getOrg().getUuid());
            intent.putExtra(SurveyorIntent.EXTRA_FLOW_UUID, dialog_flow.getUuid());
            startActivity(intent);
            dialog3.dismiss();
        });

        dialog3.findViewById(R.id.button_no).setOnClickListener(view -> {

            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view);
            dialog3.dismiss();
        });

        dialog3.show();

    }

}

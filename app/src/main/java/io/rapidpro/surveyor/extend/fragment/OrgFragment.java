package io.rapidpro.surveyor.extend.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.vdurmont.semver4j.Semver;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.activity.FlowActivity;
import io.rapidpro.surveyor.adapter.FlowListAdapter;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.engine.Engine;
import io.rapidpro.surveyor.fragment.FlowListFragment;
import io.rapidpro.surveyor.task.RefreshOrgTask;
import io.rapidpro.surveyor.ui.BlockingProgress;
import io.rapidpro.surveyor.ui.ViewCache;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

/**
 * Home screen for an org - shows available flows and pending submissions
 */
public class OrgFragment extends BaseFragment implements FlowListFragment.Container {

    private Org org;
    private AlertDialog confirmRefreshDialog;

    Activity activity;
    Context context;
    View mainView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_org, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        context = getActivity();
        mainView = getView();

        refresh();

        if (savedInstanceState == null) {
            Fragment fragment = new FlowListFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (confirmRefreshDialog != null) {
            confirmRefreshDialog.dismiss();
        }
    }

    protected void promptToUpgrade() {

        final Dialog dialog4 = new Dialog(context);
        dialog4.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog4.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog4.setContentView(R.layout.v1_dialog_ui);
        dialog4.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) dialog4.findViewById(R.id.textMainText)).setText(R.string.unsupported_version);
        ((TextView) dialog4.findViewById(R.id.button_yes_text)).setText("Yes");
        ((TextView) dialog4.findViewById(R.id.button_no_text)).setText("No");

        dialog4.findViewById(R.id.button_yes).setOnClickListener(view -> {
            playNotification(getSurveyor(), getContext(), R.raw.button_click_yes, view);
            dialog4.dismiss();

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=io.rapidpro.surveyor")));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.rapidpro.surveyor")));
            }

        });

        dialog4.findViewById(R.id.button_no).setOnClickListener(view -> {
            playNotification(getSurveyor(), getContext(), R.raw.button_click_no, view);
            dialog4.dismiss();
        });

        dialog4.show();
    }

    protected void refresh() {
        if (org == null) {
            String orgUUID = activity.getIntent().getStringExtra(SurveyorIntent.EXTRA_ORG_UUID);
            try {
                org = getSurveyor().getOrgService().get(orgUUID);
            } catch (Exception e) {
                Logger.e("Unable to load org", e);
                showBugReportDialog();
                //finish();
                return;
            }
        }

        //setTitle(org.getName());
        FlowListAdapter adapter = (FlowListAdapter) getViewCache().getListViewAdapter(android.R.id.list);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        int pending = getSurveyor().getSubmissionService().getCompletedCount(getOrg());

        ViewCache cache = getViewCache();
        cache.setVisible(R.id.pending_submission, pending > 0);
        cache.setButtonText(R.id.button_pending, NumberFormat.getInstance().format(pending));

        if (confirmRefreshDialog == null) {
            if (!org.hasAssets()) {
                // if this org doesn't have downloaded assets, ask the user if we can download them now
                confirmRefreshOrg(R.string.confirm_org_download);
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
                            confirmRefreshOrg(R.string.confirm_org_refresh_old);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void showBugReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.confirm_bug_report))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sendBugReport();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void sendBugReport() {
        try {
            Uri outputUri = getSurveyor().generateLogDump();

            ShareCompat.IntentBuilder.from(activity)
                    .setType("message/rfc822")
                    .addEmailTo(getString(R.string.support_email))
                    .setSubject("Surveyor Bug Report")
                    .setText("Please include what you were doing prior to sending this report and specific details on the error you encountered.")
                    .setStream(outputUri)
                    .setChooserTitle("Send Email")
                    .startChooser();

        } catch (IOException e) {
            Logger.e("Failed to generate bug report", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onActionRefresh(MenuItem item) {
        confirmRefreshOrg(R.string.confirm_org_refresh);
    }

    public void confirmRefreshOrg(int msgId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        confirmRefreshDialog = builder.setMessage(getString(msgId))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        doRefresh();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create();

        confirmRefreshDialog.show();
    }

    private void doRefresh() {
        final BlockingProgress progressModal = new BlockingProgress(context, R.string.one_moment, R.string.refresh_org);
        progressModal.show();

        new RefreshOrgTask(new RefreshOrgTask.Listener() {
            @Override
            public void onProgress(int percent) {
                progressModal.setProgress(percent);
            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onComplete() {
                refresh();

                progressModal.dismiss();
            }

            @Override
            public void onFailure() {
                progressModal.dismiss();

                Toast.makeText(context, getString(R.string.error_org_refresh), Toast.LENGTH_SHORT).show();
            }
        }).execute(getOrg());
    }

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
    @Override
    public void onItemClick(Flow flow) {
        Intent intent = new Intent(context, FlowActivity.class);
        intent.putExtra(SurveyorIntent.EXTRA_ORG_UUID, getOrg().getUuid());
        intent.putExtra(SurveyorIntent.EXTRA_FLOW_UUID, flow.getUuid());
        startActivity(intent);
    }
}

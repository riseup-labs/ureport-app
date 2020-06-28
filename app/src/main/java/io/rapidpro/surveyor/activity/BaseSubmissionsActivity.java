package io.rapidpro.surveyor.activity;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.task.SubmitSubmissionsTask;
import io.rapidpro.surveyor.ui.BlockingProgress;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

/**
 * Base for activities that have submissions ((org and flow views)
 */
public abstract class BaseSubmissionsActivity extends BaseActivity {

    /**
     * User has clicked a submit button
     *
     * @param view the button
     */
    public void onActionSubmit(View view) {

        final Dialog dialog7 = new Dialog(this);
        dialog7.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog7.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog7.setContentView(R.layout.v1_dialog_ui);
        dialog7.findViewById(R.id.textSubText).setVisibility(View.GONE);
        ((TextView) dialog7.findViewById(R.id.textMainText)).setText(getString(R.string.confirm_send_submissions));
        ((TextView) dialog7.findViewById(R.id.button_yes_text)).setText("Yes");
        ((TextView) dialog7.findViewById(R.id.button_no_text)).setText("No");

        dialog7.findViewById(R.id.button_yes).setOnClickListener(view1 -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view1);
            dialog7.dismiss();
            doSubmit();
        });

        dialog7.findViewById(R.id.button_no).setOnClickListener(view12 -> {
            playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view12);
            dialog7.dismiss();
        });

        dialog7.show();
    }



    /**
     * Does the actual invoking of the submissions task
     */
    public void doSubmit() {
        final BlockingProgress progressModal = new BlockingProgress(this, R.string.one_moment, R.string.submit_body);
        progressModal.show();

        final List<Submission> pending = getPendingSubmissions();
        final Submission[] asArray = pending.toArray(new Submission[0]);
        final Resources res = getResources();

        SubmitSubmissionsTask task = new SubmitSubmissionsTask(new SubmitSubmissionsTask.Listener() {
            @Override
            public void onProgress(int percent) {
                progressModal.setProgress(percent);
            }

            @Override
            public void onComplete(int total) {
                refresh();

                progressModal.dismiss();
                playNotification(getSurveyor(), getApplicationContext(), R.raw.sync_complete);

                CharSequence toast = res.getQuantityString(R.plurals.submissions_sent, total, total);
                Toast.makeText(BaseSubmissionsActivity.this, toast, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int numFailed) {
                progressModal.dismiss();

                Toast.makeText(BaseSubmissionsActivity.this, getString(R.string.error_submissions_send), Toast.LENGTH_SHORT).show();
            }
        });

        task.execute(asArray);
    }

    protected abstract List<Submission> getPendingSubmissions();

    protected abstract Org getOrg();

    protected abstract void refresh();
}

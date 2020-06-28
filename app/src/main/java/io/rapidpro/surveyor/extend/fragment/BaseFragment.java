package io.rapidpro.surveyor.extend.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import com.greysonparrelli.permiso.Permiso;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.LoginActivity;
import io.rapidpro.surveyor.activity.OrgChooseActivity;
import io.rapidpro.surveyor.ui.ViewCache;

import static io.rapidpro.surveyor.extend.StaticMethods.gotoSurveyor;

/**
 * This is an Fragment based implementation of functionality of
 * BaseActivity from Surveyor Application.
 */
public abstract class BaseFragment extends Fragment {

    private ViewCache m_viewCache;

    @Override
    public void onCreate(Bundle bundle) {
        Logger.d("Creating " + getClass().getSimpleName());

        super.onCreate(bundle);

        // if we're on an activity that requires a logged in user, and we aren't, redirect to login activity
        if (requireLogin() && !isLoggedIn()) {
            logout();
        }
    }

    public SurveyorApplication getSurveyor() {
        return (SurveyorApplication) getActivity().getApplication();
    }

    public void changeTitle(String title){
        ((TextView) getActivity().findViewById(R.id.header_text)).setText(title);
    }

    /**
     * Whether this activity requires the user to be logged in
     *
     * @return true if activity requires login
     */
    public boolean requireLogin() {
        return true;
    }

    /**
     * Logs in a user for the given orgs
     */
    public void login(String email, Set<String> orgUUIDs) {
        Logger.d("Logging in as " + email + " with access to orgs " + TextUtils.join(",", orgUUIDs));

        // save email which we'll need for submissions later
        getSurveyor().setPreference(SurveyorPreferences.AUTH_USERNAME, email);
        getSurveyor().setPreference(SurveyorPreferences.PREV_USERNAME, email);
        getSurveyor().setPreference(SurveyorPreferences.AUTH_ORGS, orgUUIDs);

        // let the user pick an org...
        startActivity(new Intent(getContext(), OrgChooseActivity.class));

        // we don't want to go back to the view that sent us here (i.e. login or create account)
    }

    /**
     * Logs the user out and returns them to the login page
     */
    protected void logout() {
        logout(-1);
    }

    /**
     * Logs the user out and returns them to the login page showing the given error string
     */
    protected void logout(int errorResId) {
        Logger.d("Logging out with error " + errorResId);

        getSurveyor().setPreference("ORG_UUID", "");
        getSurveyor().clearPreference(SurveyorPreferences.AUTH_USERNAME);
        getSurveyor().setPreference(SurveyorPreferences.AUTH_ORGS, Collections.<String>emptySet());

//        try {
//            getSurveyor().clearSubmissions();
//        } catch (IOException e) {
//            Logger.e("Unable to clear submissions", e);
//        }

        Intent intent = new Intent(getContext(), LoginActivity.class);


        // clear the activity stack
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (errorResId != -1) {
            intent.putExtra(SurveyorIntent.EXTRA_ERROR, getString(errorResId));
        }

        startActivity(intent);
        //getActivity().overridePendingTransition(0,0);
    }

    public void showBugReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

            ShareCompat.IntentBuilder.from(getActivity())
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

    public ViewCache getViewCache() {
        if (m_viewCache == null) {
            m_viewCache = new ViewCache(getContext(), getView());
        }
        return m_viewCache;
    }

    /**
     * Gets the currently authenticated username
     *
     * @return the username/email
     */
    protected String getUsername() {
        return getPreferences().getString(SurveyorPreferences.AUTH_USERNAME, null);
    }

    /**
     * Checks whether we are currently authenticated
     *
     * @return truer if we are authenticated
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(getUsername());
    }

    /**
     * Checkes whether sound is enabled in settings.
     * @return true if sound is enabled, false otherwise
     */
    protected  boolean isSoundOn() {
        String sound_state = getSurveyor().getPreferences().getString("sound_on", "true");
        if(sound_state.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Checkes whether vibration is enabled in settings.
     * @return true if vibration is enabled, false otherwise
     */
    protected  boolean isVibrationOn() {
        String vibration_state = getSurveyor().getPreferences().getString("vibration_on", "true");
        if(vibration_state.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Gets the preferences for this application
     *
     * @return the preferences
     */
    public SharedPreferences getPreferences() {
        return getSurveyor().getPreferences();
    }

    public AlertDialog showAlert(int title, int body) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(body)
                .setIcon(android.R.drawable.ic_dialog_alert).create();

        dialog.show();
        return dialog;
    }

    protected void showToast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public void showRationaleDialog(int body, Permiso.IOnRationaleProvided callback) {
        Permiso.getInstance().showRationaleInDialog(getString(R.string.title_permissions), getString(body), null, callback);
    }

}

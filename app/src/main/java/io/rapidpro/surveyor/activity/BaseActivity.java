package io.rapidpro.surveyor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;

import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.IOException;
import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.rapidpro.surveyor.BuildConfig;
import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.ui.ViewCache;

import static io.rapidpro.surveyor.extend.StaticMethods.gotoSurveyor;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

/**
 * All activities for the SurveyorApplication app extend this base activity which provides convenience methods
 * for things like authentication etc.
 */
public abstract class BaseActivity extends PermisoActivity {

    private ViewCache m_viewCache;

    public List<PowerMenuItem> menuItems;
    public PowerMenu powerMenu;
    ConstraintLayout menu_view;


    public void initHeaderBar(){

        powerMenu = new PowerMenu.Builder(this)
                .addItemList(menuItems)
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(this, R.color.black))
                .setTextGravity(Gravity.LEFT)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                //.setSelectedMenuColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build();

        menu_view = findViewById(R.id.menu_view);
        menu_view.setOnClickListener(view -> powerMenu.showAsDropDown(view));

        if(findViewById(R.id.back_button) != null){
            findViewById(R.id.back_button).setOnClickListener(view -> {
                playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, view);
                onBackPressed();
            });
        }
    }


    public void changeTitle(String title){
        ((TextView) findViewById(R.id.activityName)).setText(title);
    }


    /**
     * @see android.app.Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle bundle) {
        Logger.d("Creating " + getClass().getSimpleName());

        menuItems = new ArrayList<>();

        // so that espresso tests always have an unlocked screen
        if (BuildConfig.DEBUG) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        super.onCreate(bundle);

        // make new activity come in from right
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

        // if we're on an activity that requires a logged in user, and we aren't, redirect to login activity
        if (requireLogin() && !isLoggedIn()) {
            logout();
        }
    }

    public OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {

            if(item.getTitle().equals(getString(R.string.action_bug_report))){
                // Bug Report
                sendBugReport();
            }

            if(item.getTitle().equals(getString(R.string.action_logout))){
                // Bug Report
                logout();
            }

            if(item.getTitle().equals(getString(R.string.action_settings))){
                // Bug Report
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }

            powerMenu.dismiss();
        }
    };

    /**
     * @see android.app.Activity#onCreateOptionsMenu(Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // show the settings menu in debug mode
        if (BuildConfig.DEBUG) {
            MenuItem menuItem = menu.findItem(R.id.action_settings);
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
        }

        // show logout action if we're logged in
        if (isLoggedIn()) {
            MenuItem menuItem = menu.findItem(R.id.action_logout);
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
        }

        return true;
    }

    /**
     * User clicked "Settings" menu option
     *
     * @param item the menu item
     */
    public void onActionSettings(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * User clicked "Logout" menu option
     *
     * @param item the menu item
     */
    public void onActionLogout(MenuItem item) {
        logout();
    }

    /**
     * User clicked "Bug Report" menu option
     *
     * @param item the menu item
     */
    public void onActionBugReport(MenuItem item) {
        sendBugReport();
    }

    /**
     * Gets the instance of the application
     *
     * @return the application
     */
    public SurveyorApplication getSurveyor() {
        return (SurveyorApplication) getApplication();
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
        startActivity(new Intent(this, OrgChooseActivity.class));

        // play sound

        playNotification(getSurveyor(), getApplicationContext(), R.raw.sync_complete);

        // we don't want to go back to the view that sent us here (i.e. login or create account)
        finish();
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

        StaticMethods.current_UUID = "invalid";
        getSurveyor().clearPreference(SurveyorPreferences.AUTH_USERNAME);
        getSurveyor().setPreference(SurveyorPreferences.AUTH_ORGS, Collections.<String>emptySet());

//        try {
//            getSurveyor().clearSubmissions();
//        } catch (IOException e) {
//            Logger.e("Unable to clear submissions", e);
//        }

        Intent intent = new Intent(this, LoginActivity.class);

        // clear the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (errorResId != -1) {
            intent.putExtra(SurveyorIntent.EXTRA_ERROR, getString(errorResId));
        }
        startActivity(intent);
        //overridePendingTransition(0, 0);
        finish();
    }

    public void showBugReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public void sendBugReport() {
        try {
            Uri outputUri = getSurveyor().generateLogDump();

            ShareCompat.IntentBuilder.from(this)
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
            m_viewCache = new ViewCache(this, findViewById(android.R.id.content));
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
     * Gets the preferences for this application
     *
     * @return the preferences
     */
    public SharedPreferences getPreferences() {
        return getSurveyor().getPreferences();
    }

    public AlertDialog showAlert(int title, int body) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body)
                .setIcon(android.R.drawable.ic_dialog_alert).create();

        dialog.show();
        return dialog;
    }

    protected void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public void showRationaleDialog(int body, Permiso.IOnRationaleProvided callback) {
        Permiso.getInstance().showRationaleInDialog(getString(R.string.title_permissions), getString(body), null, callback);
    }

}

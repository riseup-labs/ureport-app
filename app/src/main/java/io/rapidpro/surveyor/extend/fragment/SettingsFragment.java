package io.rapidpro.surveyor.extend.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorPreferences;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

/**
 * Previous implementation of Settings fragment used by the application.
 * Replaced by: SettingsFragment_WV
 * Scheduled for Deletion.
 */
public class SettingsFragment extends BaseFragment {

    String lang_code = "en";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v1_fragment_settings, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lang_code = getSurveyor().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        setLang_code(lang_code);

        if(!isLoggedIn()){
            ((TextView) getView().findViewById(R.id.textView_logout)).setText("Login");
        }

        getView().findViewById(R.id.logout_button).setOnClickListener(view13 -> logout());

        getView().findViewById(R.id.btn_english).setOnClickListener(view12 -> {
            if(lang_code.equals("en")){return;}
            setLang_code("en");
            playNotification(getSurveyor(), getContext(), R.raw.setting_button_change, view12);
        });

        getView().findViewById(R.id.btn_burmese).setOnClickListener(view1 -> {
            if(lang_code.equals("my")){return;}
            setLang_code("my");
            playNotification(getSurveyor(), getContext(), R.raw.setting_button_change, view1);
        });
    }

    void setLang_code(String lang_code){
        getSurveyor().setPreference(SurveyorPreferences.LANG_CODE, lang_code);
        this.lang_code = lang_code;
        if(lang_code.equals("my")){
            getView().findViewById(R.id.btn_english).setBackground(getResources().getDrawable(R.drawable.v3_dialog_button_black));
            getView().findViewById(R.id.btn_burmese).setBackground(getResources().getDrawable(R.drawable.v3_dialog_button_magenta));
        }else{
            getView().findViewById(R.id.btn_english).setBackground(getResources().getDrawable(R.drawable.v3_dialog_button_magenta));
            getView().findViewById(R.id.btn_burmese).setBackground(getResources().getDrawable(R.drawable.v3_dialog_button_black));
        }
    }

    @Override
    public boolean requireLogin() {
        return false;
    }

}

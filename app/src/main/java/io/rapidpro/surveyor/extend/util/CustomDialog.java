package io.rapidpro.surveyor.extend.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;

import static io.rapidpro.surveyor.extend.StaticMethods.NO_SOUND;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class CustomDialog  {

    Context context;

    public CustomDialog(Context context){
        this.context = context;
    }

    public void displayNoInternetDialog(final CustomDialogInterface customDialogInterface) {

        // Create Dialog Window
        final Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.v1_dialog_custom_layout);

        // Retry Button
        ConstraintLayout retry_button = dialog.findViewById(R.id.dialog_retry_button);
        retry_button.setOnClickListener(view -> {
            playNotification(SurveyorApplication.get(), context, R.raw.button_click_yes, view);
            customDialogInterface.retry();
            dialog.dismiss();
        });

        // Cancel Button
        ConstraintLayout cancel_button = dialog.findViewById(R.id.dialog_cancel_button);
        cancel_button.setOnClickListener(view -> {
            playNotification(SurveyorApplication.get(), context, R.raw.button_click_no, view);
            customDialogInterface.cancel();
            dialog.dismiss();
        });

        dialog.show();
        playNotification(SurveyorApplication.get(), context, R.raw.no_internet_alert);

    }

    public void displayCustomDialog(final CustomDialogInterface customDialogInterface){
        displayCustomDialog(new CustomDialogComponent(), customDialogInterface);
    }

    public void displayCustomDialog(final CustomDialogComponent customDialogComponent, final CustomDialogInterface customDialogInterface){

        // Create Dialog Window
        final Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.v1_dialog_ui);

        dialog.findViewById(R.id.textMainText).setVisibility(customDialogComponent.getMainTextVisible());
        dialog.findViewById(R.id.textSubText).setVisibility(customDialogComponent.getSubTextVisible());
        dialog.findViewById(R.id.button_yes).setVisibility(customDialogComponent.getButtonYesVisible());
        dialog.findViewById(R.id.button_no).setVisibility(customDialogComponent.getButtonNoVisible());
        //dialog.findViewById(R.id.v3_dialog_icon).setVisibility(customDialogComponent.getImageIconVisible());

        dialog.findViewById(R.id.img_start).setVisibility(customDialogComponent.getButtonIconVisible());
        dialog.findViewById(R.id.img_cancel).setVisibility(customDialogComponent.getButtonIconVisible());

        ((TextView) dialog.findViewById(R.id.textMainText)).setText(customDialogComponent.getMainText());
        ((TextView) dialog.findViewById(R.id.textSubText)).setText(customDialogComponent.getSubText());
        ((TextView) dialog.findViewById(R.id.button_yes_text)).setText(customDialogComponent.getButtonYes());
        ((TextView) dialog.findViewById(R.id.button_no_text)).setText(customDialogComponent.getButtonNo());
        //((ImageView) dialog.findViewById(R.id.v3_dialog_icon)).setImageResource(customDialogComponent.getImageIcon());

        // Retry Button
        ConstraintLayout retry_button = dialog.findViewById(R.id.button_yes);
        retry_button.setOnClickListener(view -> {
            if(customDialogComponent.getDialogNoSound() != NO_SOUND){
                playNotification(SurveyorApplication.get(), context, customDialogComponent.getDialogYesSound(), view);
            }
            customDialogInterface.retry();
            dialog.dismiss();
        });

        // Cancel Button
        ConstraintLayout cancel_button = dialog.findViewById(R.id.button_no);
        cancel_button.setOnClickListener(view -> {
            if(customDialogComponent.getDialogNoSound() != NO_SOUND){
                playNotification(SurveyorApplication.get(), context, customDialogComponent.getDialogNoSound(), view);
            }
            customDialogInterface.cancel();
            dialog.dismiss();
        });

        dialog.show();

        if(customDialogComponent.getDialogOpenSound() != NO_SOUND){
            playNotification(SurveyorApplication.get(), context, customDialogComponent.getDialogOpenSound());
        }

    }

}

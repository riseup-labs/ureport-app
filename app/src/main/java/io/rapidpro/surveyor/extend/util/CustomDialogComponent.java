package io.rapidpro.surveyor.extend.util;

import android.view.View;

import io.rapidpro.surveyor.R;


public class CustomDialogComponent {

    private int mainTextVisible = View.VISIBLE;
    private int subTextVisible = View.VISIBLE;
    private int buttonYesVisible = View.VISIBLE;
    private int buttonNoVisible = View.VISIBLE;
    private int imageIconVisible = View.VISIBLE;
    private int buttonIconVisible = View.GONE;

    private String mainText = "";
    private String subText = "";
    private String buttonYes = "Yes";
    private String buttonNo = "No";

    private int dialogOpenSound = R.raw.no_internet_alert;
    private int dialogYesSound = R.raw.button_click_yes;
    private int dialogNoSound = R.raw.button_click_no;

    private int imageIcon = R.drawable.v3_icon_network;

    public int getButtonIconVisible() {
        return buttonIconVisible;
    }

    public void setButtonIconVisible(int buttonIconVisible) {
        this.buttonIconVisible = buttonIconVisible;
    }

    public int getMainTextVisible() {
        return mainTextVisible;
    }

    public int getImageIconVisible() {
        return imageIconVisible;
    }

    public CustomDialogComponent setImageIconVisible(int imageIconVisible) {
        this.imageIconVisible = imageIconVisible;
        return this;
    }

    public int getImageIcon() {
        return imageIcon;
    }

    public CustomDialogComponent setImageIcon(int imageIcon) {
        this.imageIcon = imageIcon;
        return this;
    }

    public CustomDialogComponent setMainTextVisible(int mainTextVisible) {
        this.mainTextVisible = mainTextVisible;
        return this;
    }

    public int getSubTextVisible() {
        return subTextVisible;
    }

    public CustomDialogComponent setSubTextVisible(int subTextVisible) {
        this.subTextVisible = subTextVisible;
        return this;
    }

    public int getButtonYesVisible() {
        return buttonYesVisible;
    }

    public CustomDialogComponent setButtonYesVisible(int buttonYesVisible) {
        this.buttonYesVisible = buttonYesVisible;
        return this;
    }

    public int getButtonNoVisible() {
        return buttonNoVisible;
    }

    public CustomDialogComponent setButtonNoVisible(int buttonNoVisible) {
        this.buttonNoVisible = buttonNoVisible;
        return this;
    }

    public String getMainText() {
        return mainText;
    }

    public CustomDialogComponent setMainText(String mainText) {
        this.mainText = mainText;
        return this;
    }

    public String getSubText() {
        return subText;
    }

    public CustomDialogComponent setSubText(String subText) {
        this.subText = subText;
        return this;
    }

    public String getButtonYes() {
        return buttonYes;
    }

    public CustomDialogComponent setButtonYes(String buttonYes) {
        this.buttonYes = buttonYes;
        return this;
    }

    public String getButtonNo() {
        return buttonNo;
    }

    public CustomDialogComponent setButtonNo(String buttonNo) {
        this.buttonNo = buttonNo;
        return this;
    }

    public int getDialogOpenSound() {
        return dialogOpenSound;
    }

    public CustomDialogComponent setDialogOpenSound(int dialogOpenSound) {
        this.dialogOpenSound = dialogOpenSound;
        return this;
    }

    public int getDialogYesSound() {
        return dialogYesSound;
    }

    public CustomDialogComponent setDialogYesSound(int dialogYesSound) {
        this.dialogYesSound = dialogYesSound;
        return this;
    }

    public int getDialogNoSound() {
        return dialogNoSound;
    }

    public CustomDialogComponent setDialogNoSound(int dialogNoSound) {
        this.dialogNoSound = dialogNoSound;
        return this;
    }
}

package io.rapidpro.surveyor.extend.adapter;

/**
 * Custom Adapter Dashboard Cards
 * Used in DashboardActivity
 */

public class DashboardList_RV {

    String name;
    int cardImage;
    int bgShadow;

    public DashboardList_RV(String name, int cardImage, int bgShadow) {
        this.name = name;
        this.cardImage = cardImage;
        this.bgShadow = bgShadow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCardImage() {
        return cardImage;
    }

    public void setCardImage(int cardImage) {
        this.cardImage = cardImage;
    }

    public int getBgShadow() {
        return bgShadow;
    }

    public void setBgShadow(int bgShadow) {
        this.bgShadow = bgShadow;
    }
}

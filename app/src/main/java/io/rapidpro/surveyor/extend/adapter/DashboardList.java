package io.rapidpro.surveyor.extend.adapter;

public class DashboardList {

    String name;
    String animationName;
    String animationPath;
    int bgColor;
    int bgShadow;

    public DashboardList(String name, String animationName, String animationPath, int bgColor, int bgShadow) {
        this.name = name;
        this.animationName = animationName;
        this.animationPath = animationPath;
        this.bgColor = bgColor;
        this.bgShadow = bgShadow;
    }

    public String getName() {
        return name;
    }

    public DashboardList setName(String name) {
        this.name = name;
        return this;
    }

    public String getAnimationName() {
        return animationName;
    }

    public DashboardList setAnimationName(String animationName) {
        this.animationName = animationName;
        return this;
    }

    public String getAnimationPath() {
        return animationPath;
    }

    public DashboardList setAnimationPath(String animationPath) {
        this.animationPath = animationPath;
        return this;
    }

    public int getBgColor() {
        return bgColor;
    }

    public DashboardList setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public int getBgShadow() {
        return bgShadow;
    }

    public DashboardList setBgShadow(int bgShadow) {
        this.bgShadow = bgShadow;
        return this;
    }
}

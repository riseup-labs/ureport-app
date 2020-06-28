package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class results_by_location {
    String open_ended;
    int set;
    int unset;
    String boundary;
    String label;
    List<io.rapidpro.surveyor.extend.entity.model.categories> categories;

    public results_by_location() {
    }

    public String getOpen_ended() {
        return open_ended;
    }

    public void setOpen_ended(String open_ended) {
        this.open_ended = open_ended;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getUnset() {
        return unset;
    }

    public void setUnset(int unset) {
        this.unset = unset;
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<io.rapidpro.surveyor.extend.entity.model.categories> getCategories() {
        return categories;
    }

    public void setCategories(List<io.rapidpro.surveyor.extend.entity.model.categories> categories) {
        this.categories = categories;
    }
}

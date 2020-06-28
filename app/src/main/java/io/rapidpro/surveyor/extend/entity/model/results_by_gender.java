package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class results_by_gender {
    int set;
    int unset;
    String label;
    List<io.rapidpro.surveyor.extend.entity.model.categories> categories;

    public results_by_gender() {
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

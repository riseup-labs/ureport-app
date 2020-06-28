package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
public class story_api {

    List<StoriesLocal> data;
    String last_updated;

    public story_api(List<StoriesLocal> data, String last_updated) {
        this.data = data;
        this.last_updated = last_updated;
    }

    public List<StoriesLocal> getData() {
        return data;
    }

    public void setData(List<StoriesLocal> data) {
        this.data = data;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }
}

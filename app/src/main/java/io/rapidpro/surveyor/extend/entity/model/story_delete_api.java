package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class story_delete_api {

    List<story_delete_data> data;
    String last_updated;

    public story_delete_api(List<story_delete_data> data, String last_updated) {
        this.data = data;
        this.last_updated = last_updated;
    }

    public List<story_delete_data> getData() {
        return data;
    }

    public void setData(List<story_delete_data> data) {
        this.data = data;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }
}

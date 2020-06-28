package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.SurveyorLocal;
import io.rapidpro.surveyor.extend.entity.local.UReportLocal;

public class surveyor_api {

    List<SurveyorLocal> data;
    String last_updated;

    public surveyor_api(List<SurveyorLocal> data, String last_updated) {
        this.data = data;
        this.last_updated = last_updated;
    }

    public List<SurveyorLocal> getData() {
        return data;
    }

    public void setData(List<SurveyorLocal> data) {
        this.data = data;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }
}

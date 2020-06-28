package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.UReportLocal;

public class ureport_api {

    List<UReportLocal> data;
    String last_updated;

    public ureport_api(List<UReportLocal> data, String last_updated) {
        this.data = data;
        this.last_updated = last_updated;
    }

    public List<UReportLocal> getData() {
        return data;
    }

    public void setData(List<UReportLocal> data) {
        this.data = data;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }
}

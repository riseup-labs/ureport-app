package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class results {
    int id;
    String flow_uuid;
    String title;
    int org;
    io.rapidpro.surveyor.extend.entity.model.category category;
    String created_on;
    List<io.rapidpro.surveyor.extend.entity.model.questions> questions;



    public results() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFlow_uuid() {
        return flow_uuid;
    }

    public void setFlow_uuid(String flow_uuid) {
        this.flow_uuid = flow_uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrg() {
        return org;
    }

    public void setOrg(int org) {
        this.org = org;
    }

    public io.rapidpro.surveyor.extend.entity.model.category getCategory() {
        return category;
    }

    public void setCategory(io.rapidpro.surveyor.extend.entity.model.category category) {
        this.category = category;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public List<io.rapidpro.surveyor.extend.entity.model.questions> getQuestions() {
        return questions;
    }

    public void setQuestions(List<io.rapidpro.surveyor.extend.entity.model.questions> questions) {
        this.questions = questions;
    }
}

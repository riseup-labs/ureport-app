package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class questions {
    int id;
    String ruleset_uuid;
    String title;
    results2 results;
    List<io.rapidpro.surveyor.extend.entity.model.results_by_age> results_by_age;
    List<results_by_gender> results_by_gender;
    List<io.rapidpro.surveyor.extend.entity.model.results_by_location> results_by_location;

    public questions() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRuleset_uuid() {
        return ruleset_uuid;
    }

    public void setRuleset_uuid(String ruleset_uuid) {
        this.ruleset_uuid = ruleset_uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public results2 getResults() {
        return results;
    }

    public void setResults(results2 results) {
        this.results = results;
    }

    public List<io.rapidpro.surveyor.extend.entity.model.results_by_age> getResults_by_age() {
        return results_by_age;
    }

    public void setResults_by_age(List<io.rapidpro.surveyor.extend.entity.model.results_by_age> results_by_age) {
        this.results_by_age = results_by_age;
    }

    public List<results_by_gender> getResults_by_gender() {
        return results_by_gender;
    }

    public void setResults_by_gender(List<results_by_gender> results_by_gender) {
        this.results_by_gender = results_by_gender;
    }

    public List<io.rapidpro.surveyor.extend.entity.model.results_by_location> getResults_by_location() {
        return results_by_location;
    }

    public void setResults_by_location(List<io.rapidpro.surveyor.extend.entity.model.results_by_location> results_by_location) {
        this.results_by_location = results_by_location;
    }
}

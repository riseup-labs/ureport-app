package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

public class polls {
    int count;
    String next;
    String previous;
    List<io.rapidpro.surveyor.extend.entity.model.results> results;

    public polls() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<io.rapidpro.surveyor.extend.entity.model.results> getResults() {
        return results;
    }

    public void setResults(List<io.rapidpro.surveyor.extend.entity.model.results> results) {
        this.results = results;
    }
}

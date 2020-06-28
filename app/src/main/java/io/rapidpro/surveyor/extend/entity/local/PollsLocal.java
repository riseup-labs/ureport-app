package io.rapidpro.surveyor.extend.entity.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.ResultsTypeConverter;
import io.rapidpro.surveyor.extend.entity.model.results;

@Entity(tableName = "polls")
public class PollsLocal {
    @PrimaryKey(autoGenerate = true)
    public int primaryKey;
    int count;
    String next;
    String previous;

    @TypeConverters(ResultsTypeConverter.class)
    List<results> results;

    public PollsLocal() {
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

    public List<results> getResults() {
        return results;
    }

    public void setResults(List<results> results) {
        this.results = results;
    }
}

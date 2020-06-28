package io.rapidpro.surveyor.extend.entity;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import io.rapidpro.surveyor.extend.entity.model.results;

public class ResultsTypeConverter {

    static Gson gson = new Gson();

    @TypeConverter
    public static List<results> stringToSomeObjectList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<results>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(List<results> someObjects) {
        return gson.toJson(someObjects);
    }
}

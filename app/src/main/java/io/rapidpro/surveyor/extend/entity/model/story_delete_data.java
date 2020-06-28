package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;

public class story_delete_data {

    int story_id;

    public story_delete_data(int story_id) {
        this.story_id = story_id;
    }

    public int getStory_id() {
        return story_id;
    }

    public void setStory_id(int story_id) {
        this.story_id = story_id;
    }
}

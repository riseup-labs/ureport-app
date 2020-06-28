package io.rapidpro.surveyor.extend.entity.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stories")
public class StoriesLocal {
    @PrimaryKey(autoGenerate = true)
    public int primaryKey;
    int id;
    String title_bn;
    String title_en;
    String title_my;
    String subtitle_bn;
    String subtitle_en;
    String subtitle_my;
    String body_bn;
    String body_en;
    String body_my;
    String content_image;
    String story_video;
    String author;
    String author_image;
    int status;
    String created_at;
    String updated_at;

    public StoriesLocal(int primaryKey, int id, String title_bn, String title_en, String title_my, String subtitle_bn, String subtitle_en, String subtitle_my, String body_bn, String body_en, String body_my, String content_image, String story_video, String author, String author_image, int status, String created_at, String updated_at) {
        this.primaryKey = primaryKey;
        this.id = id;
        this.title_bn = title_bn;
        this.title_en = title_en;
        this.title_my = title_my;
        this.subtitle_bn = subtitle_bn;
        this.subtitle_en = subtitle_en;
        this.subtitle_my = subtitle_my;
        this.body_bn = body_bn;
        this.body_en = body_en;
        this.body_my = body_my;
        this.content_image = content_image;
        this.story_video = story_video;
        this.author = author;
        this.author_image = author_image;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle_bn() {
        return title_bn;
    }

    public void setTitle_bn(String title_bn) {
        this.title_bn = title_bn;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getTitle_my() {
        return title_my;
    }

    public void setTitle_my(String title_my) {
        this.title_my = title_my;
    }

    public String getSubtitle_bn() {
        return subtitle_bn;
    }

    public void setSubtitle_bn(String subtitle_bn) {
        this.subtitle_bn = subtitle_bn;
    }

    public String getSubtitle_en() {
        return subtitle_en;
    }

    public void setSubtitle_en(String subtitle_en) {
        this.subtitle_en = subtitle_en;
    }

    public String getSubtitle_my() {
        return subtitle_my;
    }

    public void setSubtitle_my(String subtitle_my) {
        this.subtitle_my = subtitle_my;
    }

    public String getBody_bn() {
        return body_bn;
    }

    public void setBody_bn(String body_bn) {
        this.body_bn = body_bn;
    }

    public String getBody_en() {
        return body_en;
    }

    public void setBody_en(String body_en) {
        this.body_en = body_en;
    }

    public String getBody_my() {
        return body_my;
    }

    public void setBody_my(String body_my) {
        this.body_my = body_my;
    }

    public String getContent_image() {
        return content_image;
    }

    public void setContent_image(String content_image) {
        this.content_image = content_image;
    }

    public String getStory_video() {
        return story_video;
    }

    public void setStory_video(String story_video) {
        this.story_video = story_video;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor_image() {
        return author_image;
    }

    public void setAuthor_image(String author_image) {
        this.author_image = author_image;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}

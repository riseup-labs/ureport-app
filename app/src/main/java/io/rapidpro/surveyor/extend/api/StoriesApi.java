package io.rapidpro.surveyor.extend.api;

import io.rapidpro.surveyor.extend.entity.model.story_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StoriesApi {

    // Base URL for General Version
    String BASE_URL_GV = "https://ureport-offline-global.unicefbangladesh.org/api/";

    // Base URL for Non-General Version
    String BASE_URL_RV = "https://ureport-offline.unicefbangladesh.org/api/";

    /**
     * Loads stories from server
     * @param last_updated
     * @param limit
     * @return Stories as story_api
     */
    @GET("story")
    Call<story_api> getStories(@Query("last_updated") String last_updated, @Query("limit") int limit);

    /**
     * Get story delete log from server.
     * This is used to sync deletion locally.
     * @param last_updated
     * @return id's of deleted contents
     */
    @GET("log")
    Call<story_delete_api> getDeletedStories(@Query("last_updated") String last_updated);
}

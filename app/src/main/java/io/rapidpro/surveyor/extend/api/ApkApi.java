package io.rapidpro.surveyor.extend.api;

import io.rapidpro.surveyor.extend.entity.model.apk_version;
import io.rapidpro.surveyor.extend.entity.model.story_api;
import io.rapidpro.surveyor.extend.entity.model.story_delete_api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApkApi {

    // Base URL for General Version
    String BASE_URL_GV = "https://ureport-offline-global.unicefbangladesh.org/api/";

    // Base URL for Non-General Version
    String BASE_URL_RV = "https://ureport-offline.unicefbangladesh.org/api/";

    /**
     * Loads APK Version information from server
     * @return Stories as story_api
     */
    @GET("apk_version")
    Call<apk_version> getVersion();

}

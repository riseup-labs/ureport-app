package io.rapidpro.surveyor.extend.api;

import io.rapidpro.surveyor.extend.entity.model.surveyor_api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SurveyorApi {

    // Base URL for General Version
    String BASE_URL_GV = "https://ureport-offline-global.unicefbangladesh.org/api/";

    // Base URL for Non-General Version
    String BASE_URL_RV = "https://ureport-offline.unicefbangladesh.org/api/";

    /**
     * This API gets Results from Offline Surveys into U-Report format
     * @param limit
     * @param offset
     * @param last_updated
     * @return Results from Offline Surveyor as surveyor_api
     */
    @GET("surveyor")
    Call<surveyor_api> getSurveyor(@Query("limit") int limit, @Query("offset") int offset, @Query("last_updated") String last_updated);

}

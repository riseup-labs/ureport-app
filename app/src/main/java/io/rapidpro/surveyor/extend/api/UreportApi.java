package io.rapidpro.surveyor.extend.api;

import io.rapidpro.surveyor.extend.entity.model.polls;
import io.rapidpro.surveyor.extend.entity.model.ureport_api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UreportApi {

    // Base URL for General Version
    String BASE_URL_GV = "https://ureport-offline-global.unicefbangladesh.org/api/";

    // Base URL for Non-General Version
    String BASE_URL_RV = "https://ureport-offline.unicefbangladesh.org/api/";

    /**
     * This API fetches U-Report data from server.
     * In this implementation we use our server as a medium
     * to implement translation functionality into U-Rpeort data.
     * @param limit
     * @param offset
     * @param last_updated
     * @return U-report data as ureport_api
     */
    @GET("ureport")
    Call<ureport_api> getUreport(@Query("limit") int limit, @Query("offset") int offset, @Query("last_updated") String last_updated);

}

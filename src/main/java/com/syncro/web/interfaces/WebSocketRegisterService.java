package com.syncro.web.interfaces;

import com.syncro.web.RegisterServiceResponse;
import com.syncro.web.ServiceRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
/**
 * interface for Retrofit
 */
public interface WebSocketRegisterService {
    @POST("api/joinTeam")
    Call<RegisterServiceResponse> registerService(@Body ServiceRequest serviceRequest);

    @POST("api/makeTeam")
    Call<RegisterServiceResponse> makeTeamService(@Body ServiceRequest serviceRequest);
}

package com.syncro.web.interfaces;

import com.syncro.web.RegisterServiceResponse;
import com.syncro.web.ServiceRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WebSocketRegisterService {
    @POST("endpoint/joinTeam")
    Call<RegisterServiceResponse> registerService(@Body ServiceRequest serviceRequest);
}

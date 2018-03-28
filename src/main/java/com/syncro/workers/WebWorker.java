package com.syncro.workers;

import com.fasterxml.uuid.Generators;
import com.syncro.persistence.AppProps;
import com.syncro.web.RegisterServiceResponse;
import com.syncro.web.ServiceRequest;
import com.syncro.web.interfaces.WebSocketRegisterService;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.UUID;
import java.util.logging.Logger;

public class WebWorker extends Worker {

    private static final Logger LOGGER = Logger.getLogger(WebWorker.class.getName());

    public WebWorker(){
        onStart();
    }

    private void onStart(){

        AppProps appProps = AppProps.getInstance();

        UUID uuid = null;

        if(StringUtils.isEmpty(appProps.getProperty("uuid",""))) {
            uuid = Generators.randomBasedGenerator().generate();
            appProps.setProperty("uuid",uuid.toString());
            appProps.preserve();
        } else {
            uuid = UUID.fromString(appProps.getProperty("uuid",""));

        }
        String endPoint = appProps.getProperty("server_endpoint","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WebSocketRegisterService service = retrofit.create(WebSocketRegisterService.class);

        Call<RegisterServiceResponse> call = service.registerService(new ServiceRequest()
                .withTeam("myteam").withMacId(uuid.toString()));

        call.enqueue(new Callback<RegisterServiceResponse>() {
            @Override
            public void onResponse(Call<RegisterServiceResponse> call, Response<RegisterServiceResponse> response) {
                LOGGER.info("Started "+response.raw());
                if(response.isSuccessful()){

                }


            }

            @Override
            public void onFailure(Call<RegisterServiceResponse> call, Throwable t) {
                System.out.println("Failed");
            }
        });


    }
}

package com.syncro.workers;

import com.fasterxml.uuid.Generators;
import com.syncro.persistence.AppProps;
import com.syncro.resources.Constants;
import com.syncro.resources.events.UIEvent;
import com.syncro.views.FileExplorer;
import com.syncro.web.RegisterServiceResponse;
import com.syncro.web.ServiceRequest;
import com.syncro.web.WebSocketHandler;
import com.syncro.web.interfaces.WebSocketRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebWorker extends Worker {

    private static final Logger LOGGER = Logger.getLogger(WebWorker.class.getName());

    private static WebWorker webWorker;

    private WebSocketHandler webSocketHandler;

    public static WebWorker getInstance(){
        if(webWorker == null) {
            webWorker = new WebWorker();

            EventBus.getDefault().register(FileExplorer.getInstance());

            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
            executor.schedule(() -> {EventBus.getDefault().post(new UIEvent("1"));}, 2, TimeUnit.SECONDS);
            executor.schedule(() -> {EventBus.getDefault().post(new UIEvent("0"));}, 4, TimeUnit.SECONDS);

            webWorker.onStart();
        }
        return webWorker;
    }

    private WebWorker(){

    }

    private void onStart(){

        AppProps appProps = AppProps.getInstance();

        UUID uuid = null;

        boolean uuidPresent = true;

        if(StringUtils.isEmpty(appProps.getProperty("uuid",""))) {
            uuid = Generators.randomBasedGenerator().generate();
            appProps.setProperty("uuid",uuid.toString());
            appProps.preserve();
            uuidPresent = false;
        } else {
            uuid = UUID.fromString(appProps.getProperty("uuid",""));

        }
        String endPoint = appProps.getProperty("server_endpoint","");
        if(StringUtils.isNotEmpty(endPoint))
            endPoint = Constants.HTTP + endPoint;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WebSocketRegisterService service = retrofit.create(WebSocketRegisterService.class);


        Call<RegisterServiceResponse> call = null;

        if(!uuidPresent)
            call = service.makeTeamService(new ServiceRequest()
                .withTeam("myteam").withMacId(uuid.toString()));
        else
            call = service.registerService(new ServiceRequest()
                .withTeam("myteam").withMacId(uuid.toString()));

        call.enqueue(new Callback<RegisterServiceResponse>() {
            @Override
            public void onResponse(Call<RegisterServiceResponse> call, Response<RegisterServiceResponse> response) {
                LOGGER.info("Started "+response.raw());
                if(response.isSuccessful()){
                    createSocketConnection();
                }

            }

            @Override
            public void onFailure(Call<RegisterServiceResponse> call, Throwable t) {
                System.out.println("Failed");
                try {
                    Thread.sleep(Constants.SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onStart();
            }
        });


    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    private void createSocketConnection(){

        AppProps appProps = AppProps.getInstance();
        String endPoint = appProps.getProperty("server_ws_endpoint","");
        if(StringUtils.isNotEmpty(endPoint))
            endPoint = Constants.WS + endPoint;
        try {
            webSocketHandler = new WebSocketHandler(new URI(endPoint));
            webSocketHandler.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}

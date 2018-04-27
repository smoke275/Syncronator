package com.syncro.workers;

import com.fasterxml.uuid.Generators;
import com.google.common.io.Files;
import com.syncro.persistence.AppProps;
import com.syncro.persistence.Folder;
import com.syncro.resources.Constants;
import com.syncro.transfer.FileServer;
import com.syncro.views.FileExplorer;
import com.syncro.views.FolderView;
import com.syncro.web.RegisterServiceResponse;
import com.syncro.web.ServiceRequest;
import com.syncro.web.WebSocketHandler;
import com.syncro.web.handlers.SyncSocket;
import com.syncro.web.interfaces.WebSocketRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebWorker extends Worker {

    private static final Logger LOGGER = Logger.getLogger(WebWorker.class.getName());

    private static WebWorker webWorker;

    private WebSocketHandler webSocketHandler;

    private SyncSocket socketIO;

    public static WebWorker getInstance(){
        if(webWorker == null) {
            webWorker = new WebWorker();

            EventBus.getDefault().register(FileExplorer.getInstance());

            /*final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
            executor.schedule(() -> {EventBus.getDefault().post(new UIEvent("1"));}, 2, TimeUnit.SECONDS);
            executor.schedule(() -> {EventBus.getDefault().post(new UIEvent("0"));}, 4, TimeUnit.SECONDS);*/

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

        LOGGER.info("UUID ::"+uuid);
        String endPoint = appProps.getProperty("server_endpoint","");
        if(StringUtils.isNotEmpty(endPoint))
            endPoint = Constants.HTTP + endPoint;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WebSocketRegisterService service = retrofit.create(WebSocketRegisterService.class);


        Call<RegisterServiceResponse> call = null;

        call = service.registerService(new ServiceRequest()
                .withTeam("myteam").withMacId(uuid.toString()));

        call.enqueue(new Callback<RegisterServiceResponse>() {
            @Override
            public void onResponse(Call<RegisterServiceResponse> call, Response<RegisterServiceResponse> response) {
                LOGGER.info("Started "+response.raw());
                if(response.isSuccessful()){
                    createSocketIOConnection();
                } else FileExplorer.getInstance().setMode(FileExplorer.INACTIVE);

            }

            @Override
            public void onFailure(Call<RegisterServiceResponse> call, Throwable t) {
                System.out.println("Failed");
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(() -> onStart(),
                        Constants.SLEEP_TIME, TimeUnit.SECONDS);
                FileExplorer.getInstance().setMode(FileExplorer.OFFLINE);
            }
        });


    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    private void createWebSocketConnection(){

        AppProps appProps = AppProps.getInstance();
        String endPoint = appProps.getProperty("server_ws_endpoint","");
        if(StringUtils.isNotEmpty(endPoint))
            endPoint = Constants.WS + endPoint;
        LOGGER.info(endPoint);
        try {
            webSocketHandler = new WebSocketHandler(new URI(endPoint));
            webSocketHandler.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public SyncSocket getSocketIO() {
        return socketIO;
    }

    private void createSocketIOConnection(){

        AppProps appProps = AppProps.getInstance();
        String endPoint = appProps.getProperty("server_ws_endpoint","");
        if(StringUtils.isNotEmpty(endPoint))
            endPoint = Constants.HTTP + endPoint;
        LOGGER.info(endPoint);
        try {

            socketIO = new SyncSocket(new URI(endPoint));
            socketIO.connect();
            socketIO.onStartup();

            /*Message message = new Message()
                    .withType("send")
                    .withData(new Data()
                            .withTo("id")
                            .withFrom(appProps.getProperty("uuid",""))
                            .withMessage("xxx"));

            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
            Gson gson = new Gson();
            executor.schedule(() -> {
                socketIO.emit(MessageGetJson.EVENT,
                        gson.toJson(messageGetJson));
                LOGGER.info("Sent");
            }, 2, TimeUnit.SECONDS);*/

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public void broadcastJson(String json){
        AppProps appProps = AppProps.getInstance();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mac_id",appProps.getProperty("uuid",""));
            jsonObject.put("version",1);
            jsonObject.put("json",new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketIO.emit(SyncSocket.PUT_JSON,jsonObject.toString());
    }

    public void refreshJson(){
        socketIO.onStartup();
    }

    public void requestFile(String uuid, String fileName, com.syncro.transfer.callbacks.Callback callback){
        JSONObject jsonObject = new JSONObject();
        AppProps appProps = AppProps.getInstance();
        Folder folder = FileExplorer.readFileView();
        String filePath = appProps.getProperty("drive_location","")
                + File.separator + FolderView.getFileUrl(folder,fileName);
        try {
            Files.createParentDirs(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jsonObject.put("mac_id",uuid);
            JSONObject dataObject = new JSONObject();
            dataObject.put("requestedFile",fileName);
            jsonObject.put("pass_message",dataObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketIO.emit(SyncSocket.P2P,jsonObject.toString());
        Executors.newSingleThreadExecutor().execute(() -> {
            FileServer fileServer =
                    new FileServer(appProps.getProperty("server_relay_endpoint",""),
                            Constants.DESTINATION_PORT,
                            filePath,
                            callback);
        });
    }
}

package com.syncro.web.handlers;

import com.google.gson.Gson;
import com.syncro.persistence.AppProps;
import com.syncro.resources.events.JSONUpdate;
import com.syncro.resources.events.UIEvent;
import com.syncro.transfer.MessageGetJson;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This is trying to emulate the WebSocket Structure
 */
public class SyncSocket {

    private static final Logger LOGGER = Logger.getLogger(SyncSocket.class.getName());
    public static final String JSON_EVENT = "json";
    public static final String JSON_UPDATE = "update_json";


    private Socket socket;

    public SyncSocket(URI uri) throws URISyntaxException {
        socket = IO.socket(uri.toString());
        LOGGER.info("Get Socket ::"+uri.toString());
        socket.on(Socket.EVENT_CONNECT, args -> {
            LOGGER.info("On connect");
        }).on(Socket.EVENT_MESSAGE,args -> {
            LOGGER.info("On message :: " +args[0]);
        }).on(Socket.EVENT_DISCONNECT,args -> {
            LOGGER.info("On disconnect");
        }).on(JSON_EVENT,args -> {
            LOGGER.info("JSON ::"+args[0]);
            JSONObject obj = (JSONObject)args[0];
            JSONObject jsonString = null;
            try {
                jsonString = obj.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            EventBus.getDefault().post(new JSONUpdate(jsonString.toString()));
        }).on(JSON_UPDATE,args -> {
            LOGGER.info("JSON ::"+args[0]);
            JSONObject obj = (JSONObject)args[0];
            JSONObject jsonString = null;
            try {
                jsonString = obj.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            EventBus.getDefault().post(new JSONUpdate(jsonString.toString()));
        });
    }

    public void connect(){
        socket.connect();
        LOGGER.info("Connection initiated");
    }

    public void send(Object... objects) {
        LOGGER.info("SEND :: "+objects[0]);
        JSONObject jsonObject = new JSONObject(objects[0]);
        socket.send(jsonObject);
    }

    public void emit(String event, Object... objects){
        LOGGER.info("EMIT :: "+objects[0]);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject((String)objects[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit(event, jsonObject);
    }

    public void onStartup(){
        AppProps appProps = AppProps.getInstance();
        MessageGetJson messageGetJson = new MessageGetJson()
                .withMacId(appProps.getProperty("uuid",""));
        Gson gson = new Gson();
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.schedule(() -> {
            emit(MessageGetJson.EVENT,
                    gson.toJson(messageGetJson));
            LOGGER.info("Sent");
        }, 2, TimeUnit.SECONDS);
    }

}

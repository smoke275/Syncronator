package com.syncro.web.handlers;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * This is trying to emulate the WebSocket Structure
 */
public class SyncSocket {

    private static final Logger LOGGER = Logger.getLogger(SyncSocket.class.getName());

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
        });
    }

    public void connect(){
        socket.connect();
        LOGGER.info("Connection initiated");
    }

    public void send(Object... objects) {
        socket.send(objects);
    }

    public void emit(String event, Object... objects){
        socket.emit(event, objects);
    }


}

package com.syncro.transfer;

import com.syncro.transfer.callbacks.Callback;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FileClient{
    private static final Logger LOGGER = Logger.getLogger(FileClient.class.getName());
    private Socket socket;
    private Callback callback;

    public FileClient(String host, int port, String fileName, Callback callback) {
        this.callback = callback;
        try {
            socket = new Socket(host, port);
            sendFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendFile(String file) throws IOException {


        int fileSize = (int)(new File(file)).length();

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(fileSize);
        LOGGER.info("Sent fileSize ="+fileSize);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fileInputStream.read(buffer) > 0) {
            dataOutputStream.write(buffer);
        }

        fileInputStream.close();
        dataOutputStream.close();

        if(callback!=null)
            Executors.newCachedThreadPool().execute(callback);
    }

}
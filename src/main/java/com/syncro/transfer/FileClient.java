package com.syncro.transfer;

import com.syncro.transfer.callbacks.Callback;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class FileClient implements Callback{

    private Socket socket;

    public FileClient(String host, int port, String fileName) {
        try {
            socket = new Socket(host, port);
            sendFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String file) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fileInputStream.read(buffer) > 0) {
            dataOutputStream.write(buffer);
        }

        fileInputStream.close();
        dataOutputStream.close();
    }

}
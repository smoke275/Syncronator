package com.syncro.transfer;

import com.syncro.transfer.callbacks.Callback;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
/**
 * Class for file transfer from sender
 */
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


        /*try {
            CryptoUtils.encrypt(Constants.CRYPTO_KEY, new File(file), new File(file));
        } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }*/

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
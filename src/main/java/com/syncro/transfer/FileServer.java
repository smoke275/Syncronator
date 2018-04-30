package com.syncro.transfer;

import com.syncro.transfer.callbacks.Callback;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Class for file transfer to receiver
 */
public class FileServer{
    private static final Logger LOGGER = Logger.getLogger(FileServer.class.getName());
    private Socket ss;
    private int fileSize;
    private int remaining;
    private int totalRead;
    private Callback callback;
    public FileServer(String host, int port, String fileName, Callback callback) {
        this.callback = callback;
        try {
            ss = new Socket(host, port);
            saveFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getTotalRead() {
        return totalRead;
    }

    private void saveFile(String fileName) throws IOException {


        DataInputStream dataInputStream = new DataInputStream(ss.getInputStream());
        this.fileSize = dataInputStream.readInt();
        LOGGER.info("fileSize ="+fileSize);
        LOGGER.info("Connected = "+ss.isConnected());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        byte[] buffer = new byte[4096];

        int read = 0;
        totalRead = 0;
        remaining = fileSize;
        while((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            fileOutputStream.write(buffer, 0, read);
        }

        fileOutputStream.close();

        /*try {
            CryptoUtils.decrypt(Constants.CRYPTO_KEY, new File(file), new File(file));
        } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }*/

        if(callback!=null)
            Executors.newCachedThreadPool().execute(callback);

    }
}
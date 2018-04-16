package com.syncro.transfer;

import com.syncro.transfer.callbacks.Callback;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class FileServer implements Callback{

    private Socket ss;
    private int fileSize;
    private int remaining;
    private int totalRead;
    public FileServer(String host, int port, String fileName, int fileSize) {
        this.fileSize = fileSize;
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
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        byte[] buffer = new byte[4096];

        int read = 0;
        totalRead = 0;
        remaining = fileSize;
        while((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fileOutputStream.write(buffer, 0, read);
        }

        fileOutputStream.close();

    }

}
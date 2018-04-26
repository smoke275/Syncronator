package com.syncro;

import com.syncro.resources.Constants;
import com.syncro.views.ApplicationSystemTray;
import com.syncro.views.FileExplorer;
import com.syncro.views.FolderView;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.Scanner;

public class Main {
    public static void main(String args[]){
        System.out.println("Initiating...");
        if(!isFileshipAlreadyRunning()){
            System.out.println("Fileship already running");
            System.out.println("Another instance of this application is already running.  Exiting.");
            System.exit(0);
        }
        ApplicationSystemTray.getInstance().init();
        FileExplorer.invokeFileManager();

        System.out.println("Running......");
    }

    private static boolean isFileshipAlreadyRunning() {
        /*InputStream stream = Main.class.getResourceAsStream("/lock/lock.dat");
        System.out.println(stream != null);
        stream = Main.class.getClassLoader().getResourceAsStream("lock.dat");
        System.out.println(stream != null);
        File file = new File(System.getProperty("user.dir"));
        System.out.println(Constants.getResource("abc"));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        // socket concept is shown at http://www.rbgrn.net/content/43-java-single-application-instance
        // but this one is really great
/*        try {
            URL lockFile = FolderView.class.getResource("/lock/lock.dat");

            final File file = new File(lockFile.getFile());
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            //file.delete();
                        } catch (Exception e) {
                            //log.error("Unable to remove lock file: " + lockFile, e);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            // log.error("Unable to create and/or lock file: " + lockFile, e);
            e.printStackTrace();
        }
        return false;*/
        return true;
    }
}

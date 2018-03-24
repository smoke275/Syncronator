package com.syncro;

import com.syncro.views.ApplicationSystemTray;
import com.syncro.views.FileExplorer;

public class Main {
    public static void main(String args[]){
        System.out.println("Initiating...");
        ApplicationSystemTray.getInstance().init();
        FileExplorer.invokeFileManager();

        System.out.println("Running......");
    }
}

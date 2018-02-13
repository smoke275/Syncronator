package views;

import workers.Worker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class ApplicationSystemTray {

    TrayIcon processTrayIcon = null;

    private static ApplicationSystemTray applicationSystemTray;

    private ApplicationSystemTray(){ }

    public static ApplicationSystemTray getInstance(){
        if(applicationSystemTray==null){
            applicationSystemTray = new ApplicationSystemTray();
        }
        return applicationSystemTray;
    }

    public void init(){
        try {
            applicationSystemTray.createAndAddApplicationToSystemTray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        applicationSystemTray.startProcess();
    }

    /**
     * This method creates the AWT items and add it to the System tray.
     *
     * @throws IOException
     */
    private void createAndAddApplicationToSystemTray() throws IOException {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        Locale locale = new Locale("en", "US");
        ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);

        final PopupMenu popup = new PopupMenu();
        URL url = ApplicationSystemTray.class.getResource("/images/syncronator_icon.png");
        BufferedImage imageIcon = null;
        try {
            imageIcon = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final TrayIcon trayIcon = new TrayIcon(imageIcon, labels.getString("system_tray_tooltip"));
        this.processTrayIcon = trayIcon;
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem(labels.getString("about_label"));
        MenuItem launchApplication = new MenuItem(labels.getString("launch_application_label"));


        MenuItem exitItem = new MenuItem(labels.getString("exit_label"));

        // Add components to popup menu
        popup.add(launchApplication);
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        // Add listener to trayIcon.
        trayIcon.addActionListener(e -> FileExplorer.invokeFileManager());

        // Add listener to aboutItem.
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
                labels.getString("about_message")));

        launchApplication.addActionListener(e -> FileExplorer.invokeFileManager());


        // Add listener to exitItem.
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            System.exit(0);
        });
    }

    private void startProcess() {

        Thread thread = new Thread(() -> new Worker());
        thread.start();
    }
}
package com.syncro.views;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syncro.persistence.AppProps;
import com.syncro.persistence.Folder;
import com.syncro.resources.Constants;
import com.syncro.resources.events.JSONUpdate;
import com.syncro.resources.events.RegisterLater;
import com.syncro.resources.events.UIEvent;
import com.syncro.web.handlers.SyncSocket;
import com.syncro.workers.WebWorker;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.awt.EventQueue.invokeLater;

/**
 * FileView explorer class creates basic view of the filesystem
 */
public class FileExplorer extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(FileExplorer.class.getName());

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int OFFLINE = 2;

    private static FileExplorer fileExplorer;
    private  JScrollPane scrollPane;
    private JPanel jPanel;
    private JMenuBar jMenuBar;
    private DropTarget dropTarget;
    private Folder rootFolderView;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JLabel statusImage;
    private int mode = ACTIVE;
    private Stack<com.syncro.persistence.Folder> navigationStack = null;
    private MouseListener glassPaneListener = null;

    public static FileExplorer getInstance(){
        if(fileExplorer == null) {
            Locale locale = new Locale("en", "US");
            ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);
            fileExplorer = new FileExplorer(labels.getString("app_name"));
            URL url = FileExplorer.class.getResource("/images/syncronator_icon.png");
            BufferedImage imageIcon = null;
            try {
                imageIcon = ImageIO.read(url);
                fileExplorer.setIconImage(imageIcon);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return fileExplorer;
    }

    private FileExplorer(String title){
        super(title);
    }

    public static void invokeFileManager(){
            invokeLater(() -> {
                FileExplorer.getInstance().initUI();
            });



    }

    public int getMode() {
        return mode;
    }

    public void run(RegisterLater registerLater){
        registerLater.doRegister();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageFromWebSocket(UIEvent uiEvent) {
        if(uiEvent.getMessage().equals(SyncSocket.ACTIVE)){
            LOGGER.info("Inactive");
            setInactiveState();
        }
    }

    private void setInactiveState(){
        getGlassPane().setVisible(true);
        setMode(INACTIVE);
        glassPaneListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Active");
                setActiveState();
                WebWorker.getInstance().broadcastActive();
            }
        };
        getGlassPane().addMouseListener(glassPaneListener);
    }

    private void setActiveState(){
        getGlassPane().setVisible(false);
        setMode(ACTIVE);
        getGlassPane().removeMouseListener(glassPaneListener);
        invokeLater(() -> initUI());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onUpdateJsonFromWebSocket(JSONUpdate jsonUpdate) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // parse json string to object
        LOGGER.info("json update ::"+jsonUpdate.getJson());
        com.syncro.persistence.Folder folderTree =
                gson.fromJson(jsonUpdate.getJson(), com.syncro.persistence.Folder.class);
        saveFileView(folderTree,false);
        invokeLater(() -> {
            LOGGER.info("Current ::"+navigationStack.peek().getName());
            initUI();
            setInactiveState();
        });

    }

    public void initUI() {

        getContentPane().removeAll();
        getContentPane().revalidate();
        getContentPane().repaint();

        navigationStack = new Stack<>();
        rootFolderView = readFileView();
        drawWith(rootFolderView);

        getScrollablePanel(jPanel);
        this.setJMenuBar(getExplorerMenuBar());

        this.add(getStatusBar(), BorderLayout.SOUTH);

        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        setConfigurations();

        getContentPane().revalidate();
        getContentPane().repaint();

        if(!isDataValid()){
            Locale locale = new Locale("en", "US");
            ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);
            JOptionPane.showMessageDialog(this,labels.getString("error_msg_wrong_directory"),
                    labels.getString("error_title_wrong_directory"), JOptionPane.ERROR_MESSAGE);


            AppProps appProps = AppProps.getInstance();
            String currentLocation = null;
            boolean isChoosingNecessary= true;
            JFileChooser jFileChooser = null;
            if(new File(appProps.getProperty("drive_location","")).
                    isDirectory()){
                currentLocation = appProps.getProperty("drive_location","");
                isChoosingNecessary = false;
            }
            jFileChooser = new JFileChooser(currentLocation);

            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jFileChooser.setMultiSelectionEnabled(false);
            jFileChooser.setDialogTitle(labels.getString("option_pane_entry_message_for_setting_dir"));
            Integer opt = -1;
            String result = null;

            while(opt!=JFileChooser.OPEN_DIALOG){
                opt = jFileChooser.showOpenDialog(this);
            }
            result = jFileChooser.getSelectedFile().getAbsolutePath();

            appProps.setProperty("drive_location",result);
            try {
                appProps.store(new FileWriter(appProps.getUrl().getPath()),
                        labels.getString("comment_properties_file"));
                //URL urlOriginal = FolderView.class.getResource("/dist/filesystem/file_view");
                URL urlOriginal = Constants.getResource("file_view");
                URL urlDefault = FolderView.class.getResource("/file_system/default_file_view");
                Files.move(new File(urlDefault.getPath()),new File(urlOriginal.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            invokeLater(() -> {
                FileExplorer.getInstance().initUI();
            });
        }

        /*getGlassPane().setVisible(true);
        getGlassPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Active");
                getGlassPane().setVisible(false);
                setMode(INACTIVE);
                final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
                executor.schedule(() -> {
                            getGlassPane().setVisible(true);
                            setMode(ACTIVE);
                        },
                        3, TimeUnit.SECONDS);
            }
        });*/
    }

    private JPanel getStatusBar(){
        if(statusPanel==null){
            statusPanel = new JPanel();
            statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
            statusPanel.setPreferredSize(new Dimension(this.getWidth(), 16));
            statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
            statusImage = new JLabel(new ImageIcon(getStatusImageIcon()));
            statusLabel = new JLabel("status");
            statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
            statusImage.setHorizontalAlignment(SwingConstants.LEFT);
            statusPanel.add(statusImage);
            statusPanel.add(statusLabel);
        }
        return statusPanel;
    }
    private static BufferedImage activeIcon;
    private static BufferedImage inactiveIcon;
    private static BufferedImage offlineIcon;

    private static BufferedImage getStatusImageIcon() {
        if (activeIcon == null) {
            try {
                URL url = FolderView.class.getResource("/images/active_img.png");
                activeIcon = Constants.resizeImage(ImageIO.read(url),0.35);
                url = FolderView.class.getResource("/images/offline_img.png");
                offlineIcon = Constants.resizeImage(ImageIO.read(url),0.35);
                url = FolderView.class.getResource("/images/inactive_img.png");
                inactiveIcon = Constants.resizeImage(ImageIO.read(url),0.35);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        switch (FileExplorer.getInstance().getMode()){
            case FileExplorer.ACTIVE: return activeIcon;
            case FileExplorer.INACTIVE: return inactiveIcon;
            case FileExplorer.OFFLINE: return offlineIcon;
            default: return offlineIcon;
        }
    }

    public void setMode(int mode){
        this.mode = mode;
        Locale locale = new Locale("en", "US");
        ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);
        switch (FileExplorer.getInstance().getMode()){
            case FileExplorer.ACTIVE: {
                statusLabel.setText(labels.getString("active_label"));
            } break;
            case FileExplorer.INACTIVE: {
                statusLabel.setText(labels.getString("inactive_label"));
            } break;
            case FileExplorer.OFFLINE: {
                statusLabel.setText(labels.getString("offline_label"));
            } break;
            default: {
                statusLabel.setText(labels.getString("offline_label"));
                this.mode = 2;
            }
        }
        statusImage.setIcon(new ImageIcon(getStatusImageIcon()));
        drawWith(navigationStack.peek());
    }


    private boolean isDataValid(){
        AppProps appProps = AppProps.getInstance();
        if(new File(appProps.getProperty("drive_location","")).isDirectory())
            return true;
        else
            return false;

    }

    private void drawWith(com.syncro.persistence.Folder folderView){
        navigationStack.push(folderView);
        getMainPanel().removeAll();
        if(!folderView.getName().equals(FolderView.ROOT)){
            FolderView folderViewTemp = FolderView.getNewInstance(FolderView.NAVIGATE_UP);
            if(getMode()!=FileExplorer.INACTIVE) folderViewTemp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2){
                        navigationStack.pop();
                        drawWith(navigationStack.pop());
                    }
                }
            });
            getMainPanel().add(folderViewTemp);
        }
        JPopupMenu popup = new JPopupMenu();
        JMenuItem contextMenu = new JMenuItem("Delete");
        popup.add(contextMenu);
        for(com.syncro.persistence.Folder folder:folderView.getFolders()){
            FolderView folderViewTemp = FolderView.getNewInstance(folder.getName());
            if(getMode()!=FileExplorer.INACTIVE) {
                folderViewTemp.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getClickCount()==2){
                            drawWith(folder);
                        }
                    }
                });
                folderViewTemp.setComponentPopupMenu(popup);
            }
            getMainPanel().add(folderViewTemp);
        }

        for(com.syncro.persistence.File file:folderView.getFiles()){
            FileView fileViewTemp = FileView.getNewInstance(file.getName());
            if(getMode()!=FileExplorer.INACTIVE) {
                fileViewTemp.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getClickCount()==2){
                            LOGGER.info(file.getName());

                            if (Desktop.isDesktopSupported()) {
                                try {
                                    AppProps appProps = AppProps.getInstance();
                                    String nodeId = file.getLocation().split(":")[0];
                                    final String actualLocation = file.getLocation().split(":")[1];
                                    if(nodeId.equals(appProps.getProperty("uuid",""))){
                                        String createLocation = appProps.getProperty("drive_location","")
                                                + File.separator + actualLocation;
                                        File myFile = new File(createLocation);
                                        Desktop.getDesktop().open(myFile);
                                    }else{
                                        try{
                                            showProgressBar();
                                        } catch (Exception e1){
                                            e1.printStackTrace();
                                        }
                                        WebWorker.getInstance().requestFile(nodeId,
                                                file.getName(),() ->{
                                                    LOGGER.info("received File");
                                                    String createLocation =
                                                            appProps.getProperty("drive_location","")
                                                                    + File.separator + actualLocation;
                                                    File myFile = new File(createLocation);
                                                    try {
                                                        Desktop.getDesktop().open(myFile);
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                    invokeLater(() -> {
                                                        String newLocation =
                                                                appProps.getProperty("uuid","")+
                                                                        ":"+
                                                                        file.getLocation().split(":")[1];
                                                        file.setLocation(newLocation);
                                                        saveFileView(rootFolderView,true);
                                                    });
                                                });
                                    }
                                } catch (IOException ex) {
                                    // no application registered for PDFs
                                }
                            }
                        }
                    }
                });
                fileViewTemp.setComponentPopupMenu(popup);
            }
            getMainPanel().add(fileViewTemp);
        }
        getMainPanel().revalidate();
        getMainPanel().repaint();

    }

    private void setConfigurations(){
        dropTarget = new DropTarget(this, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) { }

            public void dragExit(DropTargetEvent dte) { }

            public void dragOver(DropTargetDragEvent dtde) { }

            public void dropActionChanged(DropTargetDragEvent dtde) { }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        //System.out.println("Possible flavor: " + flavors[i].getMimeType());
                        if (flavors[i].isFlavorJavaFileListType()) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY);

                            java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);

                            AppProps appProps = AppProps.getInstance();
                            StringBuilder directoryName = new StringBuilder();

                            directoryName.append(appProps.getProperty("drive_location",""));
                            ListIterator<Folder> namesIterator = navigationStack.listIterator();

                            // Traversing elements
                            while(namesIterator.hasNext()){
                                String folder_name = namesIterator.next().getName();
                                if(!folder_name.equals(FolderView.ROOT))
                                    directoryName.append(File.separator+folder_name);
                            }

                            for (int j = 0; j < list.size(); j++) {
                                com.syncro.persistence.File file = new com.syncro.persistence.File();
                                final File fileSource = (File)list.get(j);
                                file.setName(fileSource.getName());
                                file.setLocation(directoryName+
                                        File.separator+fileSource.getName());
                                final File fileDestination = new File(file.getLocation());

                                //adding source computer id
                                String relative
                                        = new File(appProps.getProperty("drive_location",""))
                                        .toURI()
                                        .relativize(new File(file.getLocation())
                                                .toURI()).getPath();
                                file.setLocation(appProps.getProperty("uuid","")+":"+relative);

                                Folder source = navigationStack.pop();
                                invokeLater(() -> {
                                    source.getFiles().add(file);
                                    LOGGER.info(rootFolderView.getName());
                                    drawWith(source);
                                    saveFileView(rootFolderView,true);
                                    try {
                                        Files.copy(fileSource,fileDestination);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            dtde.dropComplete(true);
                            return;
                        }
                    }
                    LOGGER.info("Drop failed: " + dtde);
                    dtde.rejectDrop();
                } catch (Exception e) {
                     e.printStackTrace();
                    dtde.rejectDrop();
                }
            }
        });
        this.setSize(1000,500);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());
        this.setResizable(false);
    }

    private JScrollPane getScrollablePanel(JPanel jPanel){
        if(scrollPane == null)
            scrollPane = new JScrollPane(jPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel getMainPanel(){
        if(jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new WrapLayout(FlowLayout.LEFT));
        }
        return jPanel;
    }


    private JMenuBar getExplorerMenuBar() {

        if(jMenuBar==null){
            jMenuBar = new JMenuBar();

            Locale locale = new Locale("en", "US");
            ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);

            JMenu file = new JMenu(labels.getString("menu_item1"));
            file.setMnemonic(KeyEvent.VK_F);

            JMenuItem eMenuItem = new JMenuItem(labels.getString("submenu_item1"));
            eMenuItem.setMnemonic(KeyEvent.VK_E);
            eMenuItem.setToolTipText(labels.getString("submenu_item1_tooltip"));
            eMenuItem.addActionListener((ActionEvent event) -> {
                this.dispose();
            });

            JMenuItem dDriveLocation = new JMenuItem(labels.getString("submenu_item2"));
            dDriveLocation.setMnemonic(KeyEvent.VK_D);
            dDriveLocation.setToolTipText(labels.getString("submenu_item2_tooltip"));
            dDriveLocation.addActionListener((ActionEvent event) -> {

                AppProps appProps = AppProps.getInstance();
                String currentLocation = null;
                JFileChooser jFileChooser = null;
                if(new File(appProps.getProperty("drive_location","")).
                        isDirectory()){
                    currentLocation = appProps.getProperty("drive_location","");
                }
                jFileChooser = new JFileChooser(currentLocation);

                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jFileChooser.setMultiSelectionEnabled(false);
                jFileChooser.setDialogTitle(labels.getString("option_pane_message"));
                Integer opt = jFileChooser.showOpenDialog(this);
                String result = null;

                if(opt==JFileChooser.OPEN_DIALOG)
                    result = jFileChooser.getSelectedFile().getAbsolutePath();

                if(!Strings.isNullOrEmpty(result) &&
                        new File(result).isDirectory()){
                    if(!appProps.getProperty("drive_location","")
                            .equals(result)){ // the input is actually different
                        appProps.setProperty("drive_location",result);
                        try {
                            appProps.store(new FileWriter(appProps.getUrl().getPath()),
                                    labels.getString("comment_properties_file"));
                            //URL urlOriginal = FolderView.class.getResource("/dist/filesystem/file_view");
                            URL urlOriginal = Constants.getResource("file_view");

                            /**this part needs to change because the files in the system
                             * need to be shifted first so assuming that the fileystem cannot be shifted
                            **/
                            /*Folder folder = new Folder();
                            folder.setName(FolderView.ROOT);
                            folder.setFiles(new LinkedList<>());
                            folder.setFolders(new LinkedList<>());
                            saveFileView(folder,true);*/

                            invokeLater(() -> {
                                FileExplorer.getInstance().initUI();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    JOptionPane.showMessageDialog(this,labels.getString("error_msg_wrong_directory"),
                            labels.getString("error_title_wrong_directory"), JOptionPane.ERROR_MESSAGE);
                }
            });

            JMenuItem nNewFolder = new JMenuItem(labels.getString("submenu_item3"));
            nNewFolder.setMnemonic(KeyEvent.VK_N);
            nNewFolder.setToolTipText(labels.getString("submenu_item3_tooltip"));
            nNewFolder.addActionListener((ActionEvent event) -> {
                String result = JOptionPane.showInputDialog(this, labels.getString("new_folder_option_pane_message"),
                        labels.getString("new_folder"));
                if (!Strings.isNullOrEmpty(result)){
                    Folder folder = new Folder();
                    folder.setName(result);
                    folder.setFiles(new LinkedList<>());
                    folder.setFolders(new LinkedList<>());
                    StringBuilder directoryName = new StringBuilder();

                    AppProps appProps = AppProps.getInstance();

                    directoryName.append(appProps.getProperty("drive_location",""));
                    ListIterator<Folder> namesIterator = navigationStack.listIterator();

                    // Traversing elements
                    while(namesIterator.hasNext()){
                        String folder_name = namesIterator.next().getName();
                        if(!folder_name.equals(FolderView.ROOT))
                            directoryName.append(File.separator+folder_name);
                    }

                    directoryName.append(File.separator+result+File.separator+"dummyFile.txt");

                    Folder source = navigationStack.pop();

                    source.getFolders().add(folder);
                    invokeLater(() -> {
                        drawWith(source);
                        LOGGER.info(rootFolderView.getName());
                        saveFileView(rootFolderView,true);
                        try {
                            Files.createParentDirs(new File(directoryName.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            file.add(nNewFolder);
            file.add(dDriveLocation);
            file.add(eMenuItem);

            jMenuBar.add(file);
        }
        return jMenuBar;
    }

    public static Folder readFileView(){
        //URL url = FolderView.class.getResource("/dist/filesystem/file_view");
        URL url = Constants.getResource("file_view");
        LOGGER.info("url for reading ::"+url);
        // read JSON file data as String
        String fileData = null;
        try {
            fileData = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json string to object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // parse json string to object
        com.syncro.persistence.Folder folderTree = gson.fromJson(fileData, com.syncro.persistence.Folder.class);
        return folderTree;
    }

    public static Boolean saveFileView(Folder folder, boolean withBroadCast){
        //URL url = FolderView.class.getResource("/dist/filesystem/file_view");

        URL url = Constants.getResource("file_view");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonInString = gson.toJson(folder);

        if(withBroadCast){
            gson = new GsonBuilder().create();
            String broadCastJsonInString = gson.toJson(folder);
            WebWorker.getInstance().broadcastJson(broadCastJsonInString);
        }

        Reader initialReader = new StringReader(jsonInString);

        File targetFile = new File(url.getPath());
        try {
            com.google.common.io.Files.touch(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        CharSink charSink = com.google.common.io.Files.
                asCharSink(targetFile, Charset.defaultCharset());
        try {
            charSink.writeFrom(initialReader);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            initialReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void showProgressBar(){
        final JDialog dlg = new JDialog(getInstance(), "Progress Dialog", true);
        JProgressBar dpb = new JProgressBar(0, 100);
        dlg.add(BorderLayout.CENTER, dpb);
        dlg.setUndecorated(true);
        dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(300, 40);
        dlg.setLocationRelativeTo(getInstance());

        new Thread(()->{
            for (int i = 0; i <= 100; i++) {
                dpb.setValue(i);
                if(dpb.getValue() == 100){
                    dlg.setVisible(false);

                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        dlg.setVisible(true);
    }
}

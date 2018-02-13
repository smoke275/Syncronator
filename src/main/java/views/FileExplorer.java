package views;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import persistence.Folder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static java.awt.EventQueue.invokeLater;

/**
 * FileView explorer class creates basic view of the filesystem
 */
public class FileExplorer extends JFrame {

    private static FileExplorer fileExplorer;
    private  JScrollPane scrollPane;
    private JPanel jPanel;
    private JMenuBar jMenuBar;
    private DropTarget dropTarget;
    private Folder rootFolderView;
    private Stack<persistence.Folder> navigationStack = null;

    private static FileExplorer getInstance(){
        if(fileExplorer == null) {
            Locale locale = new Locale("en", "US");
            ResourceBundle labels = ResourceBundle.getBundle("strings/string", locale);
            fileExplorer = new FileExplorer(labels.getString("app_name"));
        }
        return fileExplorer;
    }

    private FileExplorer(String title){
        super(title);
    }

    private void initUI() {
        navigationStack = new Stack<>();
        rootFolderView = readFileView();
        drawWith(rootFolderView);

        getScrollablePanel(jPanel);
        this.setJMenuBar(getExplorerMenuBar());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        setConfigurations();
    }

    private void drawWith(persistence.Folder folderView){
        navigationStack.push(folderView);
        getMainPanel().removeAll();
        if(!folderView.getName().equals(FolderView.ROOT)){
            FolderView folderViewTemp = FolderView.getNewInstance(FolderView.NAVIGATE_UP);
            folderViewTemp.addMouseListener(new MouseAdapter() {
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
        for(persistence.Folder folder:folderView.getFolders()){
            FolderView folderViewTemp = FolderView.getNewInstance(folder.getName());
            folderViewTemp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2){
                        drawWith(folder);
                    }
                }
            });
            getMainPanel().add(folderViewTemp);
        }

        for(persistence.File file:folderView.getFiles()){
            getMainPanel().add(FileView.getNewInstance(file.getName()));
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

                            Properties appProps = new Properties();
                            URL url = FileExplorer.class.getResource("/properties/editable.properties");
                            try {
                                appProps.load(url.openStream());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            StringBuilder directoryName = new StringBuilder();
                            directoryName.append(appProps.getProperty("drive_location",""));
                            ListIterator<Folder> namesIterator = navigationStack.listIterator();

                            // Traversing elements
                            while(namesIterator.hasNext()){
                                String folder_name = namesIterator.next().getName();
                                if(!folder_name.equals(FolderView.ROOT))
                                    directoryName.append("\\"+folder_name);
                            }

                            for (int j = 0; j < list.size(); j++) {
                                persistence.File file = new persistence.File();
                                final File fileSource = (File)list.get(i);
                                file.setName(fileSource.getName());
                                file.setLocation(directoryName+
                                        "\\"+fileSource.getName());
                                final File fileDestination = new File(file.getLocation());
                                Folder source = navigationStack.pop();
                                invokeLater(() -> {
                                    source.getFiles().add(file);
                                    drawWith(source);
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
                    System.out.println("Drop failed: " + dtde);
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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    public static void main(String args[]){
        invokeLater(() -> {
            FileExplorer.getInstance().initUI();
        });
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
                System.exit(0);
            });

            JMenuItem dDriveLocation = new JMenuItem(labels.getString("submenu_item2"));
            dDriveLocation.setMnemonic(KeyEvent.VK_D);
            dDriveLocation.setToolTipText(labels.getString("submenu_item2_tooltip"));
            dDriveLocation.addActionListener((ActionEvent event) -> {
                Properties appProps = new Properties();
                URL url = FileExplorer.class.getResource("/properties/editable.properties");
                try {
                    appProps.load(url.openStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String result = JOptionPane.showInputDialog(this, labels.getString("option_pane_message"),
                        appProps.getProperty("drive_location",""));
                if(!Strings.isNullOrEmpty(result))
                    appProps.setProperty("drive_location",result);
                try {
                    appProps.store(new FileWriter(url.getPath()), labels.getString("comment_properties_file"));
                } catch (IOException e) {
                    e.printStackTrace();
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

                    Properties appProps = new Properties();
                    URL url = FileExplorer.class.getResource("/properties/editable.properties");
                    try {
                        appProps.load(url.openStream());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    directoryName.append(appProps.getProperty("drive_location",""));
                    ListIterator<Folder> namesIterator = navigationStack.listIterator();

                    // Traversing elements
                    while(namesIterator.hasNext()){
                        String folder_name = namesIterator.next().getName();
                        if(!folder_name.equals(FolderView.ROOT))
                            directoryName.append("\\"+folder_name);
                    }

                    directoryName.append("\\"+result+"\\dummyFile.txt");

                    Folder source = navigationStack.pop();
                    source.getFolders().add(folder);
                    invokeLater(() -> {
                        drawWith(source);
                        saveFileView(source);
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
        URL url = FolderView.class.getResource("/file_system/file_view");
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
        persistence.Folder folderTree = gson.fromJson(fileData, persistence.Folder.class);
        return folderTree;
    }

    public static Boolean saveFileView(Folder folder){
        URL url = FolderView.class.getResource("/file_system/file_view");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonInString = gson.toJson(folder);

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
}

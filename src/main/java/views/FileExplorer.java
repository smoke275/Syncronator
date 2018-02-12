package views;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Stack;

import static java.awt.EventQueue.invokeLater;

/**
 * File explorer class creates basic view of the filesystem
 */
public class FileExplorer extends JFrame{

    private static FileExplorer fileExplorer;
    private  JScrollPane scrollPane;
    private JPanel jPanel;
    private JMenuBar jMenuBar;
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
        persistence.Folder rootFolderView = readFileView();
        drawWith(rootFolderView);


        getScrollablePanel(jPanel);
        this.setJMenuBar(getExplorerMenuBar());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        setConfigurations();
    }

    private void drawWith(persistence.Folder folderView){
        navigationStack.push(folderView);
        getMainPanel().removeAll();
        if(!folderView.getName().equals(Folder.ROOT)){
            Folder folderTemp = Folder.getNewInstance(Folder.NAVIGATE_UP);
            folderTemp.addMouseListener(new MouseAdapter() {
                public persistence.Folder state = folderView;
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2){
                        navigationStack.pop();
                        drawWith(navigationStack.peek());
                        System.out.println(navigationStack.peek().getName());
                    }
                }
            });
            getMainPanel().add(folderTemp);
        }
        for(persistence.Folder folder:folderView.getFolders()){
            Folder folderTemp = Folder.getNewInstance(folder.getName());
            folderTemp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount()==2){
                        drawWith(folder);
                    }
                }
            });
            getMainPanel().add(folderTemp);
        }

        for(persistence.File file:folderView.getFiles()){
            getMainPanel().add(File.getNewInstance(file.getName()));
        }
        getMainPanel().revalidate();
        getMainPanel().repaint();

    }

    private void setConfigurations(){
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
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
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
            //ImageIcon icon = new ImageIcon("exit.png");

            JMenu file = new JMenu("File");
            file.setMnemonic(KeyEvent.VK_F);

            JMenuItem eMenuItem = new JMenuItem("Exit");
            eMenuItem.setMnemonic(KeyEvent.VK_E);
            eMenuItem.setToolTipText("Exit application");
            eMenuItem.addActionListener((ActionEvent event) -> {
                System.exit(0);
            });

            file.add(eMenuItem);

            jMenuBar.add(file);
        }
        return jMenuBar;
    }

    public static persistence.Folder readFileView(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        URL url = Folder.class.getResource("/file_system/file_view");
        // read JSON file data as String
        String fileData = null;
        try {
            fileData = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json string to object
        persistence.Folder folderTree = gson.fromJson(fileData, persistence.Folder.class);
        return folderTree;
    }
}

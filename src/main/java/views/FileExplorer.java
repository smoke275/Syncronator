package views;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.awt.EventQueue.invokeLater;

public class FileExplorer extends JFrame{

    private static FileExplorer fileExplorer;
    private  JScrollPane scrollPane;
    private JPanel jPanel;

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


        for (int counter = 0; counter<19;counter++)
            getMainPanel().add(Folder.getNewInstance());
        for (int counter = 0; counter<20;counter++)
            getMainPanel().add(File.getNewInstance());


        getScrollablePanel(jPanel);

        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

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

    FileExplorer(){
        initUI();
    }
}

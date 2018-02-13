package views;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class FolderView extends JLabel {
    public final static String ROOT = "root";
    public final static String NAVIGATE_UP = "\u21A9";
    private static BufferedImage imageIcon;
    private persistence.Folder folderStructure = null;
    public static FolderView getNewInstance() {
        return new FolderView();
    }

    public static FolderView getNewInstance(String name) {
        return new FolderView(name);
    }
    public static FolderView getNewInstance(String name, persistence.Folder folderStructure) {
        return new FolderView(name, folderStructure);
    }

    private static BufferedImage getImageIcon() {
        if (imageIcon == null) {
            imageIcon = null;
            try {
                URL url = FolderView.class.getResource("/images/folder_image2.png");
                imageIcon = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageIcon;
    }
    private FolderView(){
        this("FolderView");
    }
    private FolderView(String name) {
        this(name, null);
    }
    private FolderView(String name, persistence.Folder folderStructure) {
        super(new ImageIcon(getImageIcon()));
        this.folderStructure = folderStructure;
        setText(name);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }
}
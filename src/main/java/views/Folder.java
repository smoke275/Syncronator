package views;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Folder extends JLabel {
    public final static String ROOT = "root";
    public final static String NAVIGATE_UP = "Go back";
    private static BufferedImage imageIcon;
    private persistence.Folder folderStructure = null;
    public static Folder getNewInstance() {
        return new Folder();
    }

    public static Folder getNewInstance(String name) {
        return new Folder(name);
    }
    public static Folder getNewInstance(String name, persistence.Folder folderStructure) {
        return new Folder(name, folderStructure);
    }

    private static BufferedImage getImageIcon() {
        if (imageIcon == null) {
            imageIcon = null;
            try {
                URL url = Folder.class.getResource("/images/folder_image2.png");
                imageIcon = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageIcon;
    }
    private Folder(){
        this("Folder");
    }
    private Folder(String name) {
        this(name, null);
    }
    private Folder(String name, persistence.Folder folderStructure) {
        super(new ImageIcon(getImageIcon()));
        this.folderStructure = folderStructure;
        setText(name);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }
}

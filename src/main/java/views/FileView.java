package views;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class FileView extends JLabel {
    private static BufferedImage imageIcon;
    public static FileView getNewInstance() {
        return new FileView();
    }
    public static FileView getNewInstance(String name) {
        return new FileView(name);
    }

    private static BufferedImage getImageIcon() {
        if (imageIcon == null) {
            imageIcon = null;
            try {
                URL url = FolderView.class.getResource("/images/file_image2.png");
                imageIcon = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageIcon;
    }

    private FileView(){
        this("FileView");
    }
    private FileView(String name) {
        super(new ImageIcon(getImageIcon()));
        setText(name);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }
}

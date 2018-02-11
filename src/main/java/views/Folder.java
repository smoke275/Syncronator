package views;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Folder extends JLabel {
    private static BufferedImage imageIcon;
    public static Folder getNewInstance() {
        return new Folder();
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

    private Folder() {
        super(new ImageIcon(getImageIcon()));
        setText("Folder");
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }
}

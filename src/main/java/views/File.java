package views;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class File extends JLabel {
    private static BufferedImage imageIcon;
    public static File getNewInstance() {
        return new File();
    }
    public static File getNewInstance(String name) {
        return new File(name);
    }

    private static BufferedImage getImageIcon() {
        if (imageIcon == null) {
            imageIcon = null;
            try {
                URL url = Folder.class.getResource("/images/file_image2.png");
                imageIcon = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageIcon;
    }

    private File(){
        this("File");
    }
    private File(String name) {
        super(new ImageIcon(getImageIcon()));
        setText(name);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }
}

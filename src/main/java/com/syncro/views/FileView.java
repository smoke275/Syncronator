package com.syncro.views;


import com.syncro.resources.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class FileView extends JLabel {
    private static BufferedImage imageIcon;
    private static BufferedImage disabledImageIcon;
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
                Image grayImage = GrayFilter.createDisabledImage(imageIcon);
                disabledImageIcon = Constants.toBufferedImage(grayImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(FileExplorer.getInstance().getMode()==FileExplorer.INACTIVE)
            return disabledImageIcon;
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

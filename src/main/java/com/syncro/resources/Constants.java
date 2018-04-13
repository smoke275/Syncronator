package com.syncro.resources;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Constants {
    public static final String HTTP = "http://";
    public static final String WS = "ws://";
    public static final int SLEEP_TIME = 100000;

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, double scale){
        int IMG_WIDTH = (int)(originalImage.getWidth()*scale);
        int IMG_HEIGHT = (int)(originalImage.getHeight()*scale);
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();

        return resizedImage;
    }

}

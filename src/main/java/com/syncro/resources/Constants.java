package com.syncro.resources;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

public class Constants {
    private static final Logger LOGGER = Logger.getLogger(Constants.class.getName());
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

    public static URL getResource(String fileName){
        File root = new File(System.getProperty("user.dir"));
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root.getParentFile(), null, recursive);

            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName)) {
                    LOGGER.info("CONFIG :: "+file.toURI().toURL());
                    return file.toURI().toURL();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

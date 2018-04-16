package com.syncro;

import javax.swing.*;
import java.awt.*;

public class Test {
    public static void main(String[] args) {
        JFrame parentFrame = new JFrame();
        parentFrame.setSize(500, 150);
        JLabel jl = new JLabel();
        jl.setText("Count : 0");

        parentFrame.add(BorderLayout.CENTER, jl);
        parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        parentFrame.setVisible(true);

        final JDialog dlg = new JDialog(parentFrame, "Progress Dialog", true);
        JProgressBar dpb = new JProgressBar(0, 100);
        dlg.add(BorderLayout.CENTER, dpb);
        dlg.setUndecorated(true);
        dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(300, 40);
        dlg.setLocationRelativeTo(parentFrame);

        Thread t = new Thread(() -> dlg.setVisible(true));
        t.start();
        for (int i = 0; i <= 100; i++) {
            jl.setText("Count : " + i);
            dpb.setValue(i);
            if(dpb.getValue() == 100){
                dlg.setVisible(false);
                System.exit(0);

            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        dlg.setVisible(true);
    }
}
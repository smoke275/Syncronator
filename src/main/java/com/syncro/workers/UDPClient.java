package com.syncro.workers;

// Java program to illustrate Client side
// Implementation using DatagramSocket

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class UDPClient
{
    public static void main(String args[]) throws IOException
    {
        Scanner sc = new Scanner(System.in);

        // Step 1:Create the socket object for
        // carrying the data.
        MulticastSocket ds = new MulticastSocket();
        ds.setReuseAddress(true);
        //InetAddress ip = InetAddress.getByName("localhost");
        InetAddress ip = InetAddress.getByName("ec2-18-188-60-165.us-east-2.compute.amazonaws.com");
        byte buf[] = null;

        // loop while user not enters "bye"
        while (true)
        {
            String inp = sc.nextLine();

            // convert the String input into the byte array.
            buf = inp.getBytes();

            // Step 2 : Create the datagramPacket for sending
            // the data.
            DatagramPacket DpSend =
                    new DatagramPacket(buf, buf.length, ip, 5000);

            // Step 3 : invoke the send call to actually send
            // the data.
            ds.send(DpSend);

            // break the loop if user enters "bye"
            if (inp.equals("bye"))
                break;
        }
    }
}

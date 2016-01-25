package fr.valentinporchet.romeo;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TTClientThread implements Runnable {

    private boolean connected = false;
    private String SERVER_ADDRESS = "192.168.1.1"; // default value
    private int SERVER_PORT = 8181;
    private Socket socket;
    private InetAddress serverAddr;
    private TTData mDataToSend;

    public TTClientThread(TTData data, String serverIP) {
        mDataToSend = data;
        SERVER_ADDRESS = serverIP;
    }

    @Override
    public void run() {
        try {
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            Log.d("TTClientActivity", "C: Connecting...");
            socket = new Socket(serverAddr, SERVER_PORT);
            connected = true;

            while (connected) {
                try {
                    Log.d("TTClientActivity", "C: Sending data.");
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(mDataToSend);
                    out.close();
                    connected = false;
                    Log.d("TTClientActivity", "C: Sent.");
                } catch (Exception e) {
                    Log.e("TTClientActivity", "S: Error", e);
                }
            }
        } catch (Exception e) {
            Log.e("TTClientActivity", "C: Error", e);
            connected = false;
        }
    }


    /*public void setServerIP(String serverIP) {
        SERVER_ADDRESS = serverIP;

        try {
            socket.close();
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            socket = new Socket(serverAddr, SERVER_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}

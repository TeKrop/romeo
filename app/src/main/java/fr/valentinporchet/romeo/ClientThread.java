package fr.valentinporchet.romeo;

import android.util.Log;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread implements Runnable {

    private boolean connected = false;
    private String SERVER_ADDRESS = "192.168.1.1"; // default value
    private int SERVER_PORT = 8080;
    private Socket socket;
    private InetAddress serverAddr;
    private ArrayList<TouchData> mDataToSend;

    public ClientThread(ArrayList<TouchData> touchData, String serverIP) {
        mDataToSend = touchData;
        SERVER_ADDRESS = serverIP;
    }

    @Override
    public void run() {
        try {
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            Log.d("ClientActivity", "C: Connecting...");
            socket = new Socket(serverAddr, SERVER_PORT);
            connected = true;

            while (connected) {
                try {
                    Log.d("ClientActivity", "C: Sending data.");
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(mDataToSend);
                    out.close();
                    Log.d("ClientActivity", "C: Sent.");
                    connected = false; // we then close the connection
                } catch (Exception e) {
                    Log.e("ClientActivity", "S: Error", e);
                }
            }

            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            connected = false;
        }
    }
}

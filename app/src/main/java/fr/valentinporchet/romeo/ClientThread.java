package fr.valentinporchet.romeo;

import android.app.Activity;
import android.util.Log;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread implements Runnable {

    private static int SOCKET_TIMEOUT = 5000;
    private boolean connected = false;
    private String SERVER_ADDRESS = "192.168.1.1"; // default value
    private int SERVER_PORT = 8080;
    private Socket socket;
    private InetAddress serverAddr;
    private ArrayList<TouchData> mDataToSend;
    private MainActivity mainActivity;

    public ClientThread(ArrayList<TouchData> touchData, String serverIP, MainActivity mainActivity) {
        mDataToSend = touchData;
        SERVER_ADDRESS = serverIP;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        try {
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            Log.d("ClientActivity", "C: Connecting...");
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), SOCKET_TIMEOUT);
            connected = true;

            while (connected) {
                try {
                    Log.d("ClientActivity", "C: Sending data.");
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(mDataToSend);
                    out.close();
                    Log.d("ClientActivity", "C: Sent.");
                    mainActivity.setStatus("Sent");
                    connected = false; // we then close the connection
                } catch (Exception e) {
                    mainActivity.setStatus("Error");
                    Log.e("ClientActivity", "S: Error", e);
                }
            }

            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            mainActivity.setStatus("Error");
            connected = false;
        }
    }
}

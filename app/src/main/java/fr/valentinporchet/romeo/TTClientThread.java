package fr.valentinporchet.romeo;

import android.util.Log;

import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class TTClientThread implements Runnable {

    private static int SOCKET_TIMEOUT = 5000;
    private boolean connected = false;
    private String SERVER_ADDRESS = "192.168.1.1"; // default value
    private int SERVER_PORT = 8181;
    private Socket socket;
    private InetAddress serverAddr;
    private ArrayList<TTData> mDataToSend;

    public TTClientThread(ArrayList<TTData> data, String serverIP) {
        mDataToSend = data;
        SERVER_ADDRESS = serverIP;
    }

    @Override
    public void run() {
        try {
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            Log.d("TTClientActivity", "C: Connecting to ..." + SERVER_ADDRESS);
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), SOCKET_TIMEOUT);
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
}

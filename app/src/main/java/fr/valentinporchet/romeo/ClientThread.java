package fr.valentinporchet.romeo;

import android.util.Log;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread implements Runnable {

    private boolean connected = false;
    private String SERVER_ADDRESS = "192.168.1.45";
    private int SERVER_PORT = 8080;
    private Socket socket;
    private InetAddress serverAddr;

    @Override
    public void run() {
        try {
            serverAddr = InetAddress.getByName(SERVER_ADDRESS);
            Log.d("ClientActivity", "C: Connecting...");
            socket = new Socket(serverAddr, SERVER_PORT);
            connected = true;

            while (connected) {
                /*try {
                    Log.d("ClientActivity", "C: Sending command.");
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true);
                    // where you issue the commands
                    out.println("Hey Server!");
                    Log.d("ClientActivity", "C: Sent.");
                } catch (Exception e) {
                    Log.e("ClientActivity", "S: Error", e);
                }*/
            }

            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
            connected = false;
        }
    }

    public void send(ArrayList<TouchData> touchData) {
        try {
            Log.d("ClientActivity", "C: Sending command.");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            // where you issue the commands
            out.writeObject(touchData);
            out.close();
            Log.d("ClientActivity", "C: Sent.");
        } catch (Exception e) {
            Log.e("ClientActivity", "S: Error", e);
        }
    }
}

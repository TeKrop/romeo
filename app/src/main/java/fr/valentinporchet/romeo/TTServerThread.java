package fr.valentinporchet.romeo;

import android.os.Handler;
import android.util.Log;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class TTServerThread implements Runnable {
    private String SERVER_ADDRESS = getLocalAddress();
    private int SERVER_PORT = 8181;
    private Handler handler = new Handler();
    private ServerSocket mServerSocket;
    private ArrayList<TTData> mReceived;
    private TouchThroughView mTouchView;

    public TTServerThread(ServerSocket serverSocket, TouchThroughView touchView) {
        super();
        mServerSocket = serverSocket;
        mTouchView = touchView;
    }

    @Override
    public void run() {
        try {
            if (SERVER_ADDRESS != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ServerHandler", "Listening on IP: " + SERVER_ADDRESS);
                    }
                });
                mServerSocket = new ServerSocket(SERVER_PORT);
                while (true) { // server will always be running
                    // listen for incoming clients
                    Socket client = mServerSocket.accept();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("ServerHandler", "Connected.");
                        }
                    });

                    try {
                        Log.v("ServerHandler", "Waiting...");
                        ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                        mReceived = (ArrayList<TTData>) in.readObject();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("ServerHandler", "New data received ! Adding the dot...");
                                // we launch the mReceived animation
                                mTouchView.getOtherPosition(mReceived);
                            }
                        });
                        in.close();
                    } catch (Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("ServerHandler", "Oops. Connection interrupted. Please reconnect your phones.");
                            }
                        });
                        e.printStackTrace();
                    }
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ServerHandler", "Couldn't detect internet connection.");
                    }
                });
            }
        } catch (Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("ServerHandler", "Error");
                }
            });
            e.printStackTrace();
        }
    }

    /**
     * Method used to get the local IP address
     * @return String with the local IP address
     */
    private String getLocalAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }
}

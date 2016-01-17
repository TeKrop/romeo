package fr.valentinporchet.prototypephil;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerThread implements Runnable {
    private boolean connected = false;
    private String SERVER_ADDRESS = getLocalAddress();
    private int SERVER_PORT = 8080;
    private Handler handler = new Handler();
    private ServerSocket serverSocket;

    public ServerThread(ServerSocket serverSocket) {
        super();
        this.serverSocket = serverSocket;
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
                serverSocket = new ServerSocket(SERVER_PORT);
                while (true) {
                    // listen for incoming clients
                    Socket client = serverSocket.accept();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("ServerHandler", "Connected.");
                        }
                    });

                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            Log.d("ServerActivity", line);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // do whatever you want to the front end
                                    // this is where you can be creative
                                }
                            });
                        }
                        break;
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

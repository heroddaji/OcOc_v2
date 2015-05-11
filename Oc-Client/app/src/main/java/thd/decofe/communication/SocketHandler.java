package thd.decofe.communication;


import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import thd.decofe.algorithm.Algorithm;
import thd.decofe.ococclient.Helper;

public class SocketHandler {

    private final String TAG = this.getClass().toString();
    protected int port = 4321;
    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected int SOCKKET_TIMEOUT = 5000;
    private String host = "163.180.116.93";


    public SocketHandler() {

    }

    /*
    This setup is means for this service to be a server, and process incoming requests from others
     */
    public void startServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        new Thread(
                                new ServerRunnable(serverSocket.accept()))
                                .start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "ex", e);
                }
            }
        }).start();
    }

    public boolean isServerAvailable() {
        if (serverSocket == null || serverSocket.isClosed())
            return false;
        return true;
    }

    public void invokeMethodThroughSocket(WifiP2pInfo info, List<String> offloadValues) {
        try {
            clientSocket = new Socket();
            Log.i(TAG, "open client socket");
            host = info.groupOwnerAddress.getHostAddress();
            //host = "163.180.116.93";
            clientSocket.bind(null);
            clientSocket.connect(new InetSocketAddress(host, port), SOCKKET_TIMEOUT);


            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            for (String line : offloadValues) {
                out.println(line);
            }

            String response;
            while (!(response = in.readLine()).equals("finish")) {
                Log.i(TAG, "Server response:" + response);
                Helper.showToast(response);
            }

            clientSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "ex", e);
        }

    }

    public class ServerRunnable implements Runnable {

        Socket mSocket;

        public ServerRunnable(Socket client) {

            mSocket = client;
        }

        @Override
        public void run() {

            System.out.println("server: client connected");

            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(mSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                out.println("server alive");
            } catch (IOException e) {
                Log.e(TAG, "ex", e);
            }
            Map<String, String> values = new HashMap<String, String>();

            String request = "";
            StringTokenizer st;

            try {

                while (!(request = in.readLine()).equals("finish")) {

                    // System.out.println(request);
                    st = new StringTokenizer(request, ":");

                    int index = 0;

                    while (st.hasMoreElements()) {
                        String key = (String) st.nextElement();
                        String value = (String) st.nextElement();
                        System.out.println(++index + " server: key:" + key + " value:" + value);
                        values.put(key, value);
                    }
                    out.println("server received request");

                }

                //execute code
                if (request.equals("finish")) {
                    Class c = null;
                    try {
                        c = Class.forName(values.get("class").replace("class", "").trim());

                        //Class c = Class.forName(values.get("class"));
                        Object t = c.newInstance();
                        Method m = c.getMethod(values.get("method"), Integer.TYPE);
                        m.setAccessible(true);
                        String res = (String) m.invoke(t, Integer.parseInt(values.get("params")));
                        out.println("server offload result:" + res);
                        out.println("finish");
                    } catch (Exception e) {
                        Log.e(TAG, "ex", e);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "ex", e);
            }

        }
    }
}

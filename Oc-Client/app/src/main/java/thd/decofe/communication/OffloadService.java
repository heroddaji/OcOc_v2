package thd.decofe.communication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import thd.decofe.algorithm.Algorithm;
import thd.decofe.ococclient.Helper;
import thd.decofe.ococclient.OcocApplication;
import thd.decofe.ococclient.IOffloadAidlInterface;
import thd.decofe.ococclient.IOffloadAidlInterfaceCallback;

public class OffloadService extends Service  {
    private static final int SOCKKET_TIMEOUT = 5000;
    String TAG = "OffloadService";
    IntentFilter mIntentFilter = new IntentFilter();
    WifiP2pManager mManager = null;
    WifiP2pManager.Channel mChannel = null;
    WifiP2pBroadcastReceiver mWifiRecv = null;
    List<IOffloadAidlInterfaceCallback> mCallbacks = new ArrayList<>();
    private List<WifiP2pDevice> mPeers = new ArrayList();
    WifiP2pInfo mWifiP2pInfo = null;
    private WifiP2pDevice mMyWifiP2pDevice = null;

    SocketHandler socketHandler = new SocketHandler();


    IOffloadAidlInterface.Stub mAidlStubBinder = new IOffloadAidlInterface.Stub() {


        @Override
        public void invokeOffLoadMethod(final List<String> offloadValues) throws RemoteException {


            new Thread(new Runnable() {
                @Override
                public void run() {
                  WifiP2pInfo info = ((OcocApplication)getApplicationContext()).getWifiP2pInfo();

                  socketHandler.invokeMethodThroughSocket(info, offloadValues);

                }
            }).start();
        }

        @Override
        public void discoverPeers() throws RemoteException
        {
            if (mManager == null)
                return ;

            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener()
            {
                @Override
                public void onSuccess()
                {
                    Helper.showToast("Discovery Initiated");
                }

                @Override
                public void onFailure(int reason)
                {
                    Log.i(TAG,"discoverPeers - onFailure with reason:"+reason);
                }
            });
        }

        @Override
        public boolean connectToPeer(WifiP2pDevice device) throws RemoteException {

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            config.groupOwnerIntent = 0; //set 0 to not become a groupOwner
            mManager.connect(mChannel,config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    //goto WifiP2pBroadcaseReceiver
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(OffloadService.this, "Connect failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return true;
        }

        @Override
        public void registerCallback(IOffloadAidlInterfaceCallback cb){
            if (cb != null)
                mCallbacks.add(cb);
        }

        @Override
        public void unregisterCallback(IOffloadAidlInterfaceCallback cb){
            if (cb != null)
                mCallbacks.remove(cb);
        }

        public void disconnect(){
            if(mManager != null && mChannel != null){
                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if(group != null && mManager != null && mChannel != null){
                            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(int reason) {

                                }
                            });
                        }
                    }
                });
            }
        }

        @Override
        public void createSocketServer(){
            //SERVER MODE
            if(socketHandler.isServerAvailable() == false){
                socketHandler.startServer();
            }
        }

    };

    public void callBack(int value) {
        for(IOffloadAidlInterfaceCallback cb: mCallbacks){
            try {
                cb.notify(value);
            } catch (RemoteException e) {
                Log.e(TAG,"ex",e);
            }
        }
    }


    public OffloadService() {
        //REGISTER WIFIP2P
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAidlStubBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mWifiRecv = new WifiP2pBroadcastReceiver(mManager, mChannel,this);
        registerReceiver(mWifiRecv, mIntentFilter);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiRecv);
    }




    public void setMyDevice(WifiP2pDevice device) {
        mMyWifiP2pDevice = device;
        ((OcocApplication)getApplicationContext()).setMyDevice(mMyWifiP2pDevice);
    }
}


package thd.decofe.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import thd.decofe.ococclient.OcocApplication;

public class WifiP2pBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.ConnectionInfoListener,WifiP2pManager.PeerListListener {

    String TAG = this.getClass().toString();

    Activity mActivity;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    List mPeers = new ArrayList();
    OffloadService mService;
    NetworkInfo mNetworkInfo = null ;
    WifiP2pInfo mWifiP2pInfo = null;
    WifiP2pGroup mWifiP2pGroup= null;
    WifiP2pDevice mWifiP2pDevice= null;


    public WifiP2pBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, OffloadService service) {

        mManager = manager;
        mChannel = channel;
        mService = service;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.i(TAG,"Wifi Direct mode is enabled");
            }else{
                Log.i(TAG,"Wifi Direct mode is disable");
            }

            Log.i(TAG, "P2P state changed - " + state);

        }

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
            WifiP2pDeviceList list = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
            Log.i(TAG, "new peerList info:" + (list == null ? "null" : list.getDeviceList().toString()));

            assert (mManager != null);
            mManager.requestPeers(mChannel, this);
        }

        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //connect to other device
            Log.i(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");



            if (mManager == null)
            {
                Log.i(TAG, "mManager is null, return");
                return;
            }


            mNetworkInfo= intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            mWifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            mWifiP2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

            Log.i(TAG, "updated network info:" + (mNetworkInfo == null? "null": mNetworkInfo.toString()));
            Log.i(TAG, "updated wifiP2pInfo:" + (mWifiP2pInfo == null? "null": mWifiP2pInfo.toString()));
            Log.i(TAG, "updated wifiP2pGroup:" + (mWifiP2pGroup == null? "null": mWifiP2pGroup.toString()));

            if(mNetworkInfo.isConnected()){
                //now connected, find group owner IP
                Toast.makeText(mService,"Connected",Toast.LENGTH_LONG).show();
                mManager.requestConnectionInfo(mChannel,this);

            }
            else{
                //disconnected from p2p
                //clear something
                Toast.makeText(mService,"Disconnected",Toast.LENGTH_LONG).show();
            }
        }

        if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            mWifiP2pDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.i(TAG, "updated device info:" + mWifiP2pDevice.toString());
            //update device name in global app
            mService.setMyDevice(mWifiP2pDevice);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        mWifiP2pInfo = info;
        OcocApplication.getInstance().setWifiP2pInfo(mWifiP2pInfo);

        mService.callBack(Constants.CALLBACK_DEVICE_CONNECTED);

        //if this device is groupowner, start the servermode, otherwise show the main activity for client
        if (mWifiP2pInfo.isGroupOwner){
            mService.callBack(Constants.CALLBACK_GROUPFORMED_OWNER);
        }
        if( mWifiP2pInfo.groupFormed && !mWifiP2pInfo.isGroupOwner){
            mService.callBack(Constants.CALLBACK_GROUPFORMED_NOTOWNER);
        }

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        mPeers.clear();
        mPeers.addAll(peers.getDeviceList());
        OcocApplication.getInstance().setPeers(mPeers);

        //callback to MainActivity to start PeersActivity
        if(mPeers.size() > 0 )
            mService.callBack(Constants.CALLBACK_START_PEERS_ACTIVITY);
    }
}

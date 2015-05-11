package thd.decofe.ococclient;

import android.app.Application;
import android.content.res.Configuration;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This application will pierce through your heart
 */
public class OcocApplication extends Application{

    String TAG = this.getClass().toString();
    private static OcocApplication singleton;

    private List<WifiP2pDevice> mPeers = new ArrayList<>();
    private WifiP2pDevice mMyDevice = null;
    private WifiP2pInfo mWifiP2pInfo = null;

    public static OcocApplication getInstance(){
        return singleton;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate(){

        super.onTerminate();
    }


    public List<WifiP2pDevice> getPeers() {
        return mPeers;
    }

    public void setPeers(List<WifiP2pDevice> mPeers) {
        this.mPeers = mPeers;
    }

    public WifiP2pDevice getMyDevice() {
        return mMyDevice;
    }

    public void setMyDevice(WifiP2pDevice mMyDevice) {
        this.mMyDevice = mMyDevice;
    }

    public WifiP2pInfo getWifiP2pInfo() {
        return mWifiP2pInfo;
    }

    public void setWifiP2pInfo(WifiP2pInfo mWifiP2pInfo) {
        this.mWifiP2pInfo = mWifiP2pInfo;
    }
}

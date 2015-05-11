// IOffloadAidlInterface.aidl
package thd.decofe.ococclient;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pConfig;
import thd.decofe.ococclient.IOffloadAidlInterfaceCallback;

// Declare any non-default types here with import statements

interface IOffloadAidlInterface {
    void invokeOffLoadMethod(in List<String> offloadValues);
    void discoverPeers();
    void disconnect();
    boolean connectToPeer(in WifiP2pDevice device);

    void registerCallback(IOffloadAidlInterfaceCallback cb);
    void unregisterCallback(IOffloadAidlInterfaceCallback cb);

    void createSocketServer();


}

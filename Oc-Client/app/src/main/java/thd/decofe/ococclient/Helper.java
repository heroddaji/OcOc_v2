package thd.decofe.ococclient;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.lang.reflect.Method;

import static xdroid.toaster.Toaster.toast;
import static xdroid.toaster.Toaster.toastLong;

public class Helper {
    private final static String TAG = "Helper" ;

    public static void showToast(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                toast(message);
            }
        }).start();
    }

    public static void toggleWifiP2pSetting(boolean enable) {
        String disValue = "disableP2p";
        String enValue = "enableP2p";
        String value = enable ? enValue : disValue;
        WifiP2pManager manager = (WifiP2pManager) OcocApplication.getInstance().getSystemService(OcocApplication.getInstance().WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(OcocApplication.getInstance(), OcocApplication.getInstance().getMainLooper(), null);
        //Method method1 = manager.getClass().getMethod("enableP2p", Channel.class);
        Method method1 = null;
        try {
            method1 = manager.getClass().getMethod(value, WifiP2pManager.Channel.class);
            method1.invoke(manager, channel);
        } catch (Exception e) {
            Log.e(TAG, "ex", e);
        }

    }
}

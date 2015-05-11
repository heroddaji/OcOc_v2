package thd.decofe.ococclient;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import thd.decofe.communication.OffloadService;


public abstract  class BaseActivity extends ActionBarActivity {
    String TAG = getClass().toString();
    protected IOffloadAidlInterface mBinder = null;
    protected IOffloadAidlInterfaceCallback mCallback = null;
    ServiceConnection mServiceConnection;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCallback = new IOffloadAidlInterfaceCallback.Stub() {
            @Override
            public void notify(int value) throws RemoteException {

            }
        };

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = IOffloadAidlInterface.Stub.asInterface(service);
                try {
                    mBinder.registerCallback(mCallback);
                } catch (RemoteException e) {
                    Log.e(TAG, "ex", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBinder = null;
            }
        };

        isBound = getApplicationContext().bindService(new Intent(getApplicationContext(),OffloadService.class), mServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isBound){
            getApplicationContext().unbindService(mServiceConnection);
        }
    }
}

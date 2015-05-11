package thd.decofe.ococclient;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.IllegalFormatCodePointException;

import thd.decofe.communication.Constants;
import thd.decofe.communication.OffloadService;

public class PeersActivity extends BaseActivity implements PeerListFragment.OnFragmentInteractionListener, PeerDetailFragment.OnFragmentInteractionListener, PeerListFragment.DeviceActionListener {

    private final String TAG = this.getClass().toString();
    private PeerListFragment.OnFragmentInteractionListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);

        mCallback = new IOffloadAidlInterfaceCallback.Stub() {
            @Override
            public void notify(int value) throws RemoteException {
                if (value == Constants.CALLBACK_DEVICE_CONNECTED) {
                    //update group owner
                    PeerDetailFragment fragment = (PeerDetailFragment) getFragmentManager().findFragmentById(R.id.fragmentPeerDetail);
                    if (fragment != null) {
                        fragment.updateWhenWifiInfoAvailable();
                    }
                }

                if (value == Constants.CALLBACK_GROUPFORMED_NOTOWNER) {
                    finish();
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_peer_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast toast = Toast.makeText(this, "Wheeee!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        PeerDetailFragment fragment = (PeerDetailFragment) getFragmentManager().findFragmentById(R.id.fragmentPeerDetail);
        fragment.showDetails(device);
    }


    @Override
    public void connect(WifiP2pDevice device) {
        try {
            mBinder.connectToPeer(device);
        } catch (RemoteException e) {
            Log.e(TAG, "ex", e);
        }
    }

    @Override
    public void disconnect() {
        try {
           Helper.toggleWifiP2pSetting(false);
        } catch (Exception e) {
            Log.e(TAG, "ex", e);
        }
    }


}

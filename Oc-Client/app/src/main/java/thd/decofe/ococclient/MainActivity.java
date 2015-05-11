package thd.decofe.ococclient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import thd.decofe.algorithm.Algorithm;
import thd.decofe.communication.Constants;


public class MainActivity extends BaseActivity {
    private String TAG = this.getClass().toString();
    ProgressDialog progressDialog = null;
    TextView textViewLogger;


    private void startProgressDialog(String whatAreUDoing) {
        progressDialog = ProgressDialog.show(this, "Press back to cancel", whatAreUDoing, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }

    private void stopProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        textViewLogger = (TextView) findViewById(R.id.textViewLogger);

        //handle callbacks
        mCallback = new IOffloadAidlInterfaceCallback.Stub() {
            @Override
            public void notify(int value) throws RemoteException {
                stopProgressDialog();
                if (value == Constants.CALLBACK_FOUND_PEERS) {
                    Log.i(TAG, "found peers");
                }

                if (value == Constants.CALLBACK_DEVICE_CONNECTED) {

                }

                if (value == Constants.CALLBACK_START_PEERS_ACTIVITY) {
                    Intent peersIntent = new Intent(MainActivity.this, PeersActivity.class);
                    peersIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(peersIntent);
                }

                if (value == Constants.CALLBACK_GROUPFORMED_OWNER) {
                    //SERVER MODE
                    showNotification(true);
                    Intent intent = new Intent(MainActivity.this, ServerModeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }

                if (value == Constants.CALLBACK_GROUPFORMED_NOTOWNER) {
                    //CLIENT MODE
                    showNotification(false);
                }


                if (value == Constants.CALLBACK_CANNOT_CONNECT_SERVER) {
                    Helper.showToast("No fog devices to offload");
                }
            }
        };


        Button button = (Button) findViewById(R.id.buttonBubbleSort);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBubbleSort(v);
            }
        });

        button = (Button) findViewById(R.id.buttonFibonacci);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLongFibonacci(v);
            }
        });

        button = (Button) findViewById(R.id.buttonRBubbleSort);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               callOffloadLongBubbleSort();
            }
        });
        button = (Button) findViewById(R.id.buttonRFibonacci);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callOffloadLongFibonacci();
            }
        });

        button = (Button) findViewById(R.id.btnDisWifiP2p);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Helper.toggleWifiP2pSetting(false);
            }
        });

    }

    private void showNotification(boolean isServer) {
        String content = "Client mode";
        Intent resultIntent = new Intent(this, MainActivity.class);
        if(isServer == true){
            content = "Server mode";
            resultIntent = new Intent(this, ServerModeActivity.class);
        }

        NotificationCompat.Builder mNotiBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_vibration_white_24dp)
                .setContentTitle(content)
                .setContentText(content);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mNotiBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int mId = 1;
        mNotificationManager.notify(mId, mNotiBuilder.build());

    }

    public void callOffloadLongFibonacci(){
        try {
            Log.i(TAG, "click Remote bubblesort");
            List<String> offloadValues = new ArrayList<String>();
            offloadValues.add("class:" + Algorithm.class.getName());


            //offloadValues.add("method longBubbleSort");
            offloadValues.add("method:" + Algorithm.class.getMethod("longBubbleSort", Integer.TYPE).getName());

            EditText text = (EditText) findViewById(R.id.editTextBubleSort);
            offloadValues.add("params:" + text.getText().toString());
            offloadValues.add("finish");

            mBinder.invokeOffLoadMethod(offloadValues);


        } catch (Exception e) {
            Log.e(TAG, "ex", e);
        }
    }

    public void callOffloadLongBubbleSort(){
        try {
            Log.i(TAG, "click Remote bubblesort");
            List<String> offloadValues = new ArrayList<String>();
            offloadValues.add("class:" + Algorithm.class.getName());


            //offloadValues.add("method longBubbleSort");
            offloadValues.add("method:" + Algorithm.class.getMethod("longBubbleSort", Integer.TYPE).getName());

            EditText text = (EditText) findViewById(R.id.editTextBubleSort);
            offloadValues.add("params:" + text.getText().toString());
            offloadValues.add("finish");

            mBinder.invokeOffLoadMethod(offloadValues);


        } catch (Exception e) {
            Log.e(TAG, "ex", e);
        }
    }

    public void callLongFibonacci(View view) {
        EditText editTextFibonacci = (EditText) findViewById(R.id.editTextFibonacci);
        final int textFibonacciValue = Integer.parseInt(editTextFibonacci.getText().toString());
        startProgressDialog("Calculating...");
        Runnable runnable;
        runnable = new Runnable() {
            @Override
            public void run() {
                final String result = new Algorithm().longFibonacci(textFibonacciValue);

                textViewLogger.post(new Runnable() {
                    @Override
                    public void run() {
                        stopProgressDialog();
                        textViewLogger.setText(String.valueOf(result));
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    public void callBubbleSort(View view) {
        EditText editTextBubleSort = (EditText) findViewById(R.id.editTextBubleSort);
        final int bubbleSize = Integer.parseInt(editTextBubleSort.getText().toString());
        startProgressDialog("Calculating...");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String result = new Algorithm().longBubbleSort(bubbleSize);

                textViewLogger.post(new Runnable() {
                    @Override
                    public void run() {
                        stopProgressDialog();
                        textViewLogger.setText(String.valueOf(result));
                    }
                });
            }
        };

        new Thread(runnable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
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

        if (id == R.id.atn_direct_discover) {

            stopProgressDialog();
            startProgressDialog("Finding Wifi Direct surrounded peers ...");

            try {
                Helper.toggleWifiP2pSetting(true);
                mBinder.discoverPeers();
            } catch (Exception e) {
                Log.e(TAG, "ex", e);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    public void onPause() {
        super.onPause();

    }


}

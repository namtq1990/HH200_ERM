

package android.HH100.Dialog;

import android.HH100.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;


public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;


    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayList<String> temp = new ArrayList<>();
    private ArrayList<String> temp1 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);


        setResult(Activity.RESULT_CANCELED);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);


        ListView pairedListView =  (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);


        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);


        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> temp1 = new ArrayList<>();

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().matches("HH.*") | device.getName().matches("BP.*") | device.getName().matches("VM.*"))
                    //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    temp.add(device.getName() + "\n" + device.getAddress());
            }

            for (int i = 0; i < temp.size(); i++) {
                if(!temp1.contains(temp.get(i))) {
                    temp1.add(temp.get(i).toString());
                    mPairedDevicesArrayAdapter.add(temp.get(i).toString());
                }
            }
        }


        if(pairedDevices.size() == 0){
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }


        this.unregisterReceiver(mReceiver);
    }


    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");


        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);


        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);


        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }


        mBtAdapter.startDiscovery();
    }


    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            mBtAdapter.cancelDiscovery();


            String info = ((TextView) v).getText().toString();


            if(!info.equals(getResources().getText(R.string.none_paired).toString()))
            {
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }


        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            //   if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {


             /*   byte[] pinBytes = BluetoothDevice.convertPinToBytes("1234");
                device.setPin(pinBytes);*/


              //  mNewDevicesArrayAdapter = new ArrayAdapter<String>(DeviceListActivity.this, R.layout.device_name);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                   // mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    if(device.getName()!=null){
                        if(device.getName().matches("HH.*") | device.getName().matches("BP.*") | device.getName().matches("VM.*")) {
                           // mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                            temp.add(device.getName() + "\n" + device.getAddress());
                        }
                    }

                    for (int i = 0; i < temp.size(); i++) {
                        if(!temp1.contains(temp.get(i))) {
                            temp1.add(temp.get(i).toString());
                            mNewDevicesArrayAdapter.add(temp.get(i).toString());
                        }
                    }

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    //mNewDevicesArrayAdapter.add(noDevices);
                }
            }


        }
    };


}

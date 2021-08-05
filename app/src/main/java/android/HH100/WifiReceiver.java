package android.HH100;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action)
        {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info != null && info.isConnected())
                {
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    int ipAddress = wifiInfo.getIpAddress();
                    Log.i("tuanpa", wifiInfo.getIpAddress() + "" );

                    final String formattedIpAddress = String.format("%d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));

                    Log.i("tuanpa", formattedIpAddress);

                    Intent i = new Intent(context, TCPServerService.class);
                    context.startService(i);

                    MainActivity.wifiConnected = true;
                }
                break;

            case ConnectivityManager.CONNECTIVITY_ACTION:
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected())
                {
                    Log.i("tuanpa", "Disconnected wifi" );
                    Intent in = new Intent(context, TCPServerService.class);
                    context.stopService(in);

                    MainActivity.wifiConnected = false;
                }
                break;
        }
    }
}

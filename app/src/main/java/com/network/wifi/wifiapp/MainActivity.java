package com.network.wifi.wifiapp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private RecyclerView rv;
    private Button btnScan;
    private ArrayList<String> arrayList = new ArrayList<>();
    private MyAdapter adapter;
    private int signalLevel;


    private static final int rqACCESS_COARSE_LOCATION = 110 , REQUEST_ACCESS_FINE_LOCATION = 111, rqCHANGE_NETWORK_STATE = 112, rqCHANGE_WIFI_STATE=113, rqWRITE_SETTINGS=114;


    public static ArrayList<String> SSidarray = new ArrayList<>();

    private final String[] necessaryPermissions = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            //Manifest.permission.NEARBY_WIFI_DEVICES,
           // Manifest.permission.WRITE_SETTINGS

    };


    private static final int PERMISSION_ALL = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);


        btnScan = findViewById(R.id.btnscan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if(!wifiEnabled){
            wifiManager.setWifiEnabled(true);
        }
        rv = findViewById(R.id.rvWifiList);

        adapter = new MyAdapter(this, arrayList, wifiManager);
        rv.setAdapter(adapter);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(lm);

       if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, necessaryPermissions, PERMISSION_ALL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                boolean permission = Settings.System.canWrite(this);
                if (!permission) {
                    permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;

                    if (!permission) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 42);
                    }
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, 42);

                    permission = ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;

                    if (!permission) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, 222);
                    }

                }

            }

        }

        if (checkWifiPermission()) {
            Toast.makeText(MainActivity.this, "NearBy Device Permission Already Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestWifiPermission(2023);
        }
    }


    private boolean hasPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String perm : necessaryPermissions){
           if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
               listPermissionsNeeded. add (perm) ;
           }
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat. requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_ALL);
            return false;
        }
        return true;

//



//
//
//
//
//
////
//        }
//
//        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==PERMISSION_ALL) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                scanWifi();
            } else {
                Toast.makeText(this, "This app needs Location and Write permissions to work without any problems. so please grant these permissions!", Toast.LENGTH_SHORT).show();
            }

        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode == 2023) {
                Toast.makeText(this, "NearBy Scan Permission Granted Successfully", Toast.LENGTH_SHORT).show();
            }
        }
//
//
//
//
//
//    }

//        if (grantResults.length > 0){
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    scanWifi();
//
//            } else {
//                showMessage("Permissions is not granted");
//            }
//
//
//        }
//        if (requestCode == PERMISSION_ALL) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    public void scanWifi() {
        arrayList.clear();
        adapter.notifyDataSetChanged();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        showMessage("Scanning...");
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return;
                }

                List<ScanResult> mScanResults = wifiManager.getScanResults();

                for (ScanResult scanResult : mScanResults) {
                    signalLevel = wifiManager.calculateSignalLevel(scanResult.level, 5);
                    SSidarray.add(scanResult.SSID);

                    arrayList.add(scanResult.SSID + "-" + signalStrength(signalLevel));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String signalStrength(int signalLevel) {
        String result = "";
        switch (signalLevel) {
            case 0:
                result = "Very Low";
                break;
            case 1:
                result = "Low";
                break;
            case 2:
                result = "Medium";
                break;
            case 3:
                result = "High";
                break;
            case 4:
                result = "Very High";
                break;
        }
        return result;
    }

    public  boolean checkWifiPermission() { //true if GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public  boolean requestWifiPermission(int requestId) {
        boolean isGranted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
        }

        if (!isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.NEARBY_WIFI_DEVICES,}, requestId);
            }

        }
        return isGranted;
    }
}
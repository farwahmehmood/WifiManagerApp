package com.network.wifi.wifiapp;

import static android.content.Context.WIFI_SERVICE;
import static android.graphics.Insets.add;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Build;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context con;
    private ArrayList<String> arrayL;

    EditText pass;
    WifiManager mainWifiObj;
    Button dialogButton;
    //String ssid, Password;
    ConnectivityManager.NetworkCallback networkCallback;


    public MyAdapter(Context context, ArrayList<String> arrayL, WifiManager wm) {
        inflater = LayoutInflater.from(context);
        this.con = context;
        this.arrayL = arrayL;
        mainWifiObj = wm;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.rv_item, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.row.setText(arrayL.get(i));
    }

    @Override
    public int getItemCount() {
        return arrayL.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row=itemView.findViewById(R.id.tvRow);

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // selected item
                    String ssid = row.getText().toString();
                    String currentString =ssid;
                    String[] separatessid = currentString.split("-");
                    connectToWifi(separatessid[0]);
                    Toast.makeText(con, "Wifi SSID : " + ssid, Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(con);

        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID1);

         dialogButton = (Button) dialog.findViewById(R.id.okButton);
        pass = (EditText) dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);
        mainWifiObj = (WifiManager) con.getApplicationContext().getSystemService(WIFI_SERVICE);


        WifiInfo currentConnectionInfo1= mainWifiObj.getConnectionInfo();
        String dis=currentConnectionInfo1.getSSID();
        String temp;
        temp="\""+ wifiSSID +"\"";

        if(dis.equals(temp)){
            // we're already connected to this AP, nothing to do.
            dialogButton.setText("Disconnect");
            pass.setVisibility(View.GONE);

            Toast.makeText(con, "Already connected to network", Toast.LENGTH_SHORT).show();
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
    public void onClick(View v) {

                    //mainWifiObj.disconnect();

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && networkCallback != null) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                        dialog.dismiss();
                    } else {
                        // For devices below Android 10, use WifiManager to disconnect
                    int i=currentConnectionInfo1.getNetworkId();
                   boolean b;
                   b= mainWifiObj.disableNetwork(i);
                    mainWifiObj.disconnect();
                    mainWifiObj.saveConfiguration();
                    dialog.dismiss();
                    }
                }
            });
            //return;

        }

        else {
            // if button is clicked, connect to the network;
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String checkPassword = pass.getText().toString();
                    if (TextUtils.isEmpty(checkPassword)){
                        pass.setError("Please Enter Password!");
                    }else {
                        finallyConnect(checkPassword, wifiSSID);
                        dialog.dismiss();
                    }

                }
            });
       }

        dialog.show();
    }

//////////////////this method needs changes main problem is here w eneed to cover old android versions as well as new to connnect to wifi through ssid and password///////////////////
    private void finallyConnect(String networkPass, String networkSSID) {

        WifiInfo currentConnectionInfo1= mainWifiObj.getConnectionInfo();
        int ii=currentConnectionInfo1.getNetworkId();

        final String TAG = "WifiConnector";

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//
//            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
//                    .setSsid(networkSSID)
//            .setWpa2Passphrase(networkPass)
//            .build();
//
//            NetworkRequest request = new NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//
//            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .setNetworkSpecifier(specifier).build();
//         networkCallback = new ConnectivityManager.NetworkCallback() {
//
//             @Override
//             public void onAvailable(@NonNull Network network) {
//
////                 if(ii!=-1){
////                     mainWifiObj.disableNetwork(ii);
////                     mainWifiObj.disconnect();
////                     mainWifiObj.saveConfiguration();
////
////
////                 }
//
////                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                     // To make sure that requests don't go over mobile data
////                     connectivityManager.bindProcessToNetwork(network);
////                 } else {
////                     connectivityManager.setProcessDefaultNetwork(network);
////                 }
//                // connectivityManager.bindProcessToNetwork(network);
//                 Toast.makeText(con,"Connecting to: " + networkSSID,Toast.LENGTH_SHORT).show();
//                 //mainWifiObj.saveConfiguration();
//
//             }
//
//             @Override
//             public void onUnavailable() {
//                 // This is to stop the looping request for OnePlus & Xiaomi models
//                 //connectivityManager.bindProcessToNetwork(null);
//                 //connectivityManager.unregisterNetworkCallback(networkCallback);
//                 // Here you can have a fallback option to show a 'Please connect manually' page with an Intent to the Wifi settings
//                 Toast.makeText(con,"Error...",Toast.LENGTH_SHORT).show();
//
//             }
//         };
//            ConnectivityManager connectivityManager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            connectivityManager.requestNetwork(request, networkCallback);
//
//
//
//// Release the request when done.
//            //connectivityManager.unregisterNetworkCallback(networkCallback);
//
//
//
//
//
//
//        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final WifiNetworkSuggestion suggestion1 =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(networkSSID)
                            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                            .build();

             WifiNetworkSuggestion suggestion2 =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(networkSSID)
                            .setWpa2Passphrase(networkPass)
                            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                            .build();

            final WifiNetworkSuggestion suggestion3 =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(networkSSID)
                            .setWpa3Passphrase(networkPass)
                            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                            .build();

            final PasspointConfiguration passpointConfig = new PasspointConfiguration();
// configure passpointConfig to include a valid Passpoint configuration
            final WifiNetworkSuggestion suggestion4 ;

                     suggestion4 =
                        new WifiNetworkSuggestion.Builder()
                                .setPasspointConfig(passpointConfig)
                                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                                .build();


            final List<WifiNetworkSuggestion> suggestionsList = new ArrayList<WifiNetworkSuggestion>();


            suggestionsList.add(suggestion1);
            suggestionsList.add(suggestion2);
            suggestionsList.add(suggestion3);
            suggestionsList.add(suggestion4);




            final WifiManager wifiManager =
                    (WifiManager) con.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            final int status = wifiManager.addNetworkSuggestions(suggestionsList);
            if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
// do error handling here…
                Toast.makeText(con, "Error: "+status,Toast.LENGTH_SHORT).show();
            }

// Optional (Wait for post connection broadcast to one of your suggestions)
            final IntentFilter intentFilter =
                    new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!intent.getAction().equals(
                            WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                        return;
                    }
                    Toast.makeText(con, "Connecting to : "+networkSSID,Toast.LENGTH_SHORT).show();

                    // do post connect processing here...
                }
            };
            con.registerReceiver(broadcastReceiver, intentFilter);
        }
     else {

        mainWifiObj = (WifiManager) con.getApplicationContext().getSystemService(WIFI_SERVICE);
            if (mainWifiObj != null) {

                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = networkSSID;
                conf.preSharedKey = "\""+ networkPass +"\"";
                //conf.wepKeys[0] = "\"" + networkPass + "\"";
                //conf.wepTxKeyIndex = 0;
                //conf.hiddenSSID = true;
                //conf.hiddenSSID = true;
                conf.status = WifiConfiguration.Status.ENABLED;
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                int netId = mainWifiObj.addNetwork(conf);

                if (netId == -1) {
                    Toast.makeText(con, "Failed to add network, Incorrect Password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mainWifiObj.disconnect();
                int previd= currentConnectionInfo1.getNetworkId();
                if(previd!=-1){
                    //mainWifiObj.disconnect();
                    boolean b;
                    b= mainWifiObj.disableNetwork(previd);
                    mainWifiObj.disconnect();
                   // mainWifiObj.removeNetwork(previd);
                }
               
                mainWifiObj.enableNetwork(netId, true);
                mainWifiObj.reconnect();
                mainWifiObj.saveConfiguration();
                Toast.makeText(con, "Connecting to WiFi network: " + networkSSID, Toast.LENGTH_SHORT).show();




            }
        }


    }



}


package com.digiapp.jilmusic.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.digiapp.jilmusic.utils.Core;
import com.digiapp.jilmusic.utils.NetworkHelper;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction()=="android.net.conn.CONNECTIVITY_CHANGE") {
            if (NetworkHelper.isOnline(context)) {
                Core.checkSubscription();
            }
        }
    }
}

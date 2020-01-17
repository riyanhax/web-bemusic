package com.digiapp.jilmusic.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import org.greenrobot.eventbus.EventBus;

public class SMSListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    EventBus.getDefault().post(pdus);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}

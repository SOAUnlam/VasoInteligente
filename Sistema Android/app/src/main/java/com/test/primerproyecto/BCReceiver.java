package com.test.primerproyecto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BCReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("EjemploBroadcas", "Se conecto el auricular papaaaa");

    }
}

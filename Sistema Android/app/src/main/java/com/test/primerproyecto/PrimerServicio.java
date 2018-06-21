package com.test.primerproyecto;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class PrimerServicio extends Service {

    MediaPlayer reproductor;

    public PrimerServicio()
    {
    }

    public void onCreate()
    {
        Toast.makeText(this,"Servicio creado", Toast.LENGTH_SHORT).show();
        reproductor = MediaPlayer.create(this, R.raw.imperial);
        reproductor.setLooping(true);
    }

    @Override
    //Despues de onCreate automaticamente se ejecuta a OnstartCommand,
    //idArranque: Es el id del servicio a ejecutar
    public int onStartCommand(Intent intenc, int flags, int idArranque)
    {
        Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();
        reproductor.start();
        return START_STICKY;
    }

    @Override
    //On destroy se invoca cuando se ejcuta stopService
    public void onDestroy()
    {
        Toast.makeText(this,"Servicio detenido",Toast.LENGTH_SHORT).show();
        reproductor.stop();
    }

    @Nullable
    @Override
    //Se invoca a este metodo cuando ejecuta bindService
    public IBinder onBind(Intent intencion)
    {
        Toast.makeText(this,"EjecutandoOnbinnd",Toast.LENGTH_SHORT).show();
        return null;
    }


}

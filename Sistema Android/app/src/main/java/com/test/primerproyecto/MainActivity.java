package com.test.primerproyecto;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DispositivosBT";

    public static ListView lvListaDispositivos;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String PESO = "peso";
    public static String ESTATURA = "estatura";
    public static String GENERO = "peso";

    //Variables Bluetooth
    private BluetoothAdapter btAdapter;
    private BTListAdapter btDispositivosLista;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_bt);
    }

    @Override
    public void onResume()
    {
        super.onResume();



        /* OBTENGO DISPOSITIVO BLUETOOTH POR DEFECTO */
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        /* SI NO HAY DISPOSITIVO */
        if(btAdapter == null)
        {
            Toast.makeText(getBaseContext(), "Su celular no posee dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else
        {
            /* SI HAY DISPOSITIVO */
            if (btAdapter.isEnabled())
            {
                Log.d(TAG, "Bluetooth Activado");
            }
            else
            {
                /* SOLICITUD PARA ACTIVAR BLUETOOTH */
                Intent btIntentRequest = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btIntentRequest, 1);

            }
        }

        /*OBTENGO LISTA DE DISPOSITIVOS ENCONTRADOS */
        ArrayList<BTDevice> listaDispositivos = new ArrayList<BTDevice>();
        BTListAdapter btDispositivosLista = new BTListAdapter(this,listaDispositivos);

        /*MUESTRO DISPOSITIVOS ENCONTRADOS */
        lvListaDispositivos = (ListView) findViewById(R.id.IdLista);
        lvListaDispositivos.setAdapter(btDispositivosLista);
        lvListaDispositivos.setOnItemClickListener(mDeviceClickListener);


        // Obtiene el adaptador local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set <BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                btDispositivosLista.add(new BTDevice(device.getName(),device.getAddress()) );
            }
        }
    }

    /* EVENTO ON CLICK DE LA LISTA */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3)
        {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista
            RelativeLayout rlContainer = ((RelativeLayout)v);
            TextView tvNombre = rlContainer.findViewById(R.id.nombre);
            TextView tvMAC = rlContainer.findViewById(R.id.mac);
            String address = tvMAC.getText().toString();

            // Realiza un intent para iniciar la siguiente actividad
            Intent intentPantallaPrincipal = new Intent(MainActivity.this, ParamActivity.class);//<-<- PARTE A MODIFICAR >->->

            //Le pasa a la actividad PantallaPrincipal la direccion del dispositivo BT
            intentPantallaPrincipal.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intentPantallaPrincipal);
        }
    };


}
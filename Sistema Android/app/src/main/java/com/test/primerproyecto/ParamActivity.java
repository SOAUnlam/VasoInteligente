package com.test.primerproyecto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.content.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ParamActivity extends AppCompatActivity implements View.OnClickListener
{

    private EditText txtEstatura;
    private EditText txtPeso;
    private Button btnCalcular,btnArrancar,btnDetener,btnThread;
    private Button btnLlamar;
    private Integer idThread=0;
    private RadioButton rbMasculino;
    private RadioButton rbFemenino;

    //------------------ BLUETOOTH -----------------------------------
    //Button IdEncender, IdApagar,
    //Button IdDesconectar;
    TextView IdBufferIn;

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private BTThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //--------------------------------------------------



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        txtEstatura = (EditText)findViewById(R.id.txtEstatura);
        txtPeso = (EditText)findViewById(R.id.txtPeso);
        btnCalcular = (Button)findViewById(R.id.btnCalcular);
        rbMasculino=(RadioButton)findViewById(R.id.rbMasculino);
        rbFemenino=(RadioButton)findViewById(R.id.rbFemenino);

        btnCalcular.setOnClickListener(this);
        rbMasculino.setOnClickListener(this);
        rbFemenino.setOnClickListener(this);
        // --------- BLUETOOTH -----------

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String readMessage = (String) msg.obj;
                String[] separated = readMessage.split(";");
                Log.i("INFORMACION",separated[0]);

                if (separated[0].trim().equalsIgnoreCase("RESPUESTA"))
                {
                    Log.i("INFORMACION","INGRESA");
                    Log.i("INFORMACION",separated[1].trim());
                    Intent ns = new Intent(ParamActivity.this,ActivityDos.class);
                    ns.putExtra("RESULTADO",separated[1].trim());
                    startActivity(ns);
                }
            }

        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    @Override
    public void onResume()
    {
        super.onResume();

        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();

        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->

        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }

        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
                Toast.makeText(getBaseContext(), "No es posible conectarse" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            catch (IOException ex)
            {
            }

        }

        MyConexionBT = new BTThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            btSocket.close();
        }
        catch (IOException e2)
        {

        }
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT()
    {

        if(btAdapter==null)
        {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (!btAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class BTThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream ;

        public BTThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                Toast.makeText(getBaseContext(), "Problema al abrir el socket: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true)
            {
                try
                {
                    if(mmInStream.available()>0)
                    {
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);

                        // Envia los datos obtenidos hacia el evento via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    }
                    else
                    {
                        SystemClock.sleep(500);
                    }



                }
                catch (IOException e)
                {
                    Toast.makeText(getBaseContext(), "Error en la lectura", Toast.LENGTH_LONG).show();
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try
            {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                Log.i("INFO","Error escribiendo" + e.getMessage());
                //si no es posible enviar datos se cierra la conexión
                finish();
            }
        }
    }


    //Crea la clase que permite crear el evento de conexion

    public void onClick(View v)
    {

        //Se determina que componente genero un evento
        switch (v.getId())
        {
            case R.id.btnCalcular:

            String genero = "";
            if(rbMasculino.isChecked())
            {
                genero = "M";
            }
            else
            {
                genero = "F";
            }

            MyConexionBT.write(genero + ";" + txtPeso.getText());

            break;

            case R.id.rbFemenino:

                    rbFemenino.setChecked(true);
                    rbMasculino.setChecked(false);

                break;

            case R.id.rbMasculino:

                rbFemenino.setChecked(false);
                rbMasculino.setChecked(true);

                break;

            default:
                Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
        }


    }
}




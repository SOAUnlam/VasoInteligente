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
    private Button btnCalcular;
    private RadioButton rbMasculino;
    private RadioButton rbFemenino;

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
    private static String macAddress = null;



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



        bluetoothIn = new Handler()
        {
            public void handleMessage(android.os.Message MensajeObject)
            {
                String Mensaje = (String) MensajeObject.obj;
                String[] Respuesta = Mensaje.split(";");

                if (Respuesta[0].trim().equalsIgnoreCase("RESPUESTA"))
                {
                    Log.i("MILIGRAMOS",Respuesta[1].trim());
                    Log.i("TEMPERATURA",Respuesta[2].trim());
                    Log.i("VOLUMEN",Respuesta[3].trim());
                    Log.i("CCUBICOS",Respuesta[4].trim());
                    Intent intentResultado = new Intent(ParamActivity.this,ResultActivity.class);
                    intentResultado.putExtra("MILIGRAMOS",Respuesta[1].trim());
                    intentResultado.putExtra("TEMPERATURA",Respuesta[2].trim());
                    intentResultado.putExtra("VOLUMEN",Respuesta[3].trim());
                    intentResultado.putExtra("CCUBICOS",Respuesta[4].trim());
                    startActivity(intentResultado);
                }
            }

        };


        btAdapter = BluetoothAdapter.getDefaultAdapter();

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

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    @Override
    public void onResume()
    {
        super.onResume();

        /*OBTENGO EL DISPOSITIVO SELECCIONADO */
        Intent intentMAC = getIntent();
        macAddress = intentMAC.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice BTDevice = btAdapter.getRemoteDevice(macAddress);

        /* CREO EL SOCKET PARA ENVIAR LOS DATOS  ME CONECTO E INICIO UN HILO PARA ESCRIBIR Y ESCUCHAR*/
        try
        {
            btSocket = createBluetoothSocket(BTDevice);
        }
        catch (IOException e)
        {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }

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
                Toast.makeText(getBaseContext(), "No fue posible cerrar el socket" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


        try
        {
            MyConexionBT = new BTThread(btSocket);
            MyConexionBT.start();
        }
        catch (Exception e)
        {
                Toast.makeText(getBaseContext(), "Error iniciar el servicio de comunicacion" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            btSocket.close();
        }
        catch (Exception e)
        {

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

                        /*ENVIO LA RESPUESTA AL HANDLER PARA QUE MANEJE EL MENSAJE*/
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    }
                    else
                    {
                        SystemClock.sleep(1000);
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




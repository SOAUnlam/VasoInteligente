package com.test.primerproyecto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class ParamActivity extends AppCompatActivity  implements SensorEventListener,View.OnClickListener
{

    /* CONTROLES DE LA VISTA */
    private EditText txtEstatura;
    private EditText txtPeso;
    private Button btnCalcular;
    private RadioButton rbMasculino;
    private RadioButton rbFemenino;
    private long LastUpdate = 0;


    /*SENSORES ANDROID*/
    private SensorManager mSensorManager;
    private final static float ACC = 15;
    private static final int SENSOR_SENSITIVITY = 4;


    /* VARIABLES BLUETOOTH */
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String macAddress = null;

    /*HILO PARA SOCKET CON BLUETOOTH */
    private BTThread MyConexionBT;


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




    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    /*ESCUCHA Y ENVIO DE DATOS AL SISTEMA EMBEBIDO DEPENDIENDO DEL SENSOR*/
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType())
            {
                case Sensor.TYPE_PROXIMITY:
                    if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY)
                    {
                        Log.i("SENSOR","PROXIMIDAD");
                        MyConexionBT.write("SENSOR;   PROXIMIDAD   ");
                    }


                    break;

                case Sensor.TYPE_LIGHT:
                    float[] values1 = event.values;
                    if ((Math.abs(values1[0]) > 800))
                    {
                        Log.i("SENSOR","LUMINOSIDAD");
                        MyConexionBT.write("SENSOR;  LUMINOSIDAD  ");
                    }
                    break;

                case Sensor.TYPE_ACCELEROMETER:


                    float[] values = event.values;

                        if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
                        {
                            Log.i("SENSOR","ACELEROMETRO");
                            MyConexionBT.write("SENSOR;      SHAKE     ;");
                        }

                    break;
            }
        }

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void registerSenser()
    {
        boolean done;
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSenser()
    {
        mSensorManager.unregisterListener(this);
        Log.i("sensor", "unregister");
    }


    @Override
    public void onResume()
    {
        super.onResume();

        /* HABILITAR BLUETOOTH */
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


        /*SERVICIO DE SENSORES*/
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        /*REGISTRO ESCUCHA DE SENSORES */
        registerSenser();



        ConexionBluetooth();
    }

    public void ConexionBluetooth()
    {
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
            unregisterSenser();
            btSocket.close();
        }
        catch (IOException e2)
        {
            //insert code to deal with this
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
                mmOutStream.flush();
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Log.i("PROBLEMA",e.getMessage());
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




package com.test.primerproyecto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
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

public class PantallaPrincipal extends AppCompatActivity implements View.OnClickListener {

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
    private ConnectedThread MyConexionBT;
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
        btnArrancar = (Button)findViewById(R.id.btnArrancar);
        btnDetener = (Button)findViewById(R.id.btnDetener);
        btnThread =  (Button)findViewById(R.id.btnThread);
        rbMasculino=(RadioButton)findViewById(R.id.rbMasculino);
        rbFemenino=(RadioButton)findViewById(R.id.rbFemenino);




        /*btnCalcular.setOnClickListener(this);
        btnArrancar.setOnClickListener(this);
        btnDetener.setOnClickListener(this);
        btnThread.setOnClickListener(this);
        rbMasculino.setOnClickListener(this);
        rbFemenino.setOnClickListener(this);*/



        // --------- BLUETOOTH -----------

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        IdBufferIn.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        //-------------

        // Configuracion onClick listeners para los botones
        // para indicar que se realizara cuando se detecte
        // el evento de Click
        //IdEncender.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v)
        //   {
        //        MyConexionBT.write("1");
        //    }
        //});

        btnCalcular.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    MyConexionBT.write("80");
                }
        });



        //IdApagar.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        MyConexionBT.write("0");
        //    }
        //});

        /*IdDesconectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btSocket!=null)
                {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
            }
        });*/
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    private void  ejecutarThread()
    {
        final Handler mHandler = new Handler();

        //Se crea un Thread implementado la interfaz Runnable para poder interactuar con este
        new Thread(new Runnable() {
            @Override
            //El metodo run se utiliza debido a que se debe implentar al utilizar la interfaz Runnable
            public void run () {
                // Perform long-running task here
                // (like audio buffering).
                // you may want to update some progress
                // bar every second, so use handler:

                //Aca, en esta seccion, se ejecuta el thread secundario creado
                idThread++;

                //Se utiliza un Handler que permite comunicar al thread creado con la cola de mensjes del hilo
                //principal de la interfaz grafica, internamente mediante un looper.
                //En otras palabras mediante un Handler el Thread seundario puede modifcar la interfaz grafica
                Runnable refresh = new Runnable() {
                    @Override
                    public void run()
                    {
                        // make operation on UI - on example
                        // on progress bar.

                        Toast.makeText(getApplicationContext(),"Ejecutando Thread Numero:"+idThread.toString(),Toast.LENGTH_LONG).show();
                    }
                };
                //Al utilizar desde el Thread secundario el post del Handler , se invocara automaticamente el
                //  metodo run de la intefaz Runnable reefresh. El cual permite modificar la Interfaz GUI desde dicho metodo.
                //NOTA: Lo que se ejecuta en el ambito de post, No se ejecuta en el thread secundario,
                //      sino en la interfaz grafica
                mHandler.post(refresh);

                Log.i("INFO","EjecutaNDO THREAD!!!!!!!!!!!!!!!");
                //Ejecutando una espera activa por cada thread creado para comprobar que la Interfaz grafica no se congela,
                //sigue activa. Continua respondiendo a los eventos de los usuarios
                while(true);
            }
        }).start();
    }

    public void onClick(View v)
    {

        Intent intent=new Intent(PantallaPrincipal.this, PrimerServicio.class);

        //Se determina que componente genero un evento
        switch (v.getId())
        {
            case R.id.btnCalcular:
            Intent sendIntent = new Intent(PantallaPrincipal.this,ActivityDos.class);
            sendIntent.putExtra("estatura",txtEstatura.getText().toString());
            sendIntent.putExtra("peso",txtPeso.getText().toString());

            if(rbMasculino.isChecked())
            {
                sendIntent.putExtra("sexo","Masculino");
            }
            else
            {
                sendIntent.putExtra("sexo","Femenino");
            }

            startActivity(sendIntent);
            break;

            case R.id.rbFemenino:

                    rbFemenino.setChecked(true);
                    rbMasculino.setChecked(false);

                break;

            case R.id.rbMasculino:

                rbFemenino.setChecked(false);
                rbMasculino.setChecked(true);

                break;

            //Si se presiono el Boton Encender
            case R.id.btnArrancar:
                //Inicio el servicio
                startService(intent);
                break;

            //Si se presiono el Boton Apagar
            case R.id.btnDetener:
            //detengo el servicio
            stopService(intent);
            break;

            case R.id.btnThread:
                //detengo el servicio
                ejecutarThread();
                break;

            default:
                Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
        }


    }
}

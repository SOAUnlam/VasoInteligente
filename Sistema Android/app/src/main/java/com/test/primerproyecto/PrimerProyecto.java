package com.test.primerproyecto;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.content.*;

import java.net.URI;
import java.util.EventListener;

public class PrimerProyecto extends AppCompatActivity implements View.OnClickListener {

    private EditText txtEstatura;
    private EditText txtPeso;
    private Button btnCalcular,btnArrancar,btnDetener,btnThread;
    private Button btnLlamar;
    private Integer idThread=0;
    private RadioButton rbMasculino;
    private RadioButton rbFemenino;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primer_proyecto);

        txtEstatura = (EditText)findViewById(R.id.txtEstatura);
        txtPeso = (EditText)findViewById(R.id.txtPeso);
        btnCalcular = (Button)findViewById(R.id.btnCalcular);
        btnArrancar = (Button)findViewById(R.id.btnArrancar);
        btnDetener = (Button)findViewById(R.id.btnDetener);
        btnThread =  (Button)findViewById(R.id.btnThread);
        rbMasculino=(RadioButton)findViewById(R.id.rbMasculino);
        rbFemenino=(RadioButton)findViewById(R.id.rbFemenino);




        btnCalcular.setOnClickListener(this);
        btnArrancar.setOnClickListener(this);
        btnDetener.setOnClickListener(this);
        btnThread.setOnClickListener(this);
        rbMasculino.setOnClickListener(this);
        rbFemenino.setOnClickListener(this);
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

        Intent intent=new Intent(PrimerProyecto.this, PrimerServicio.class);

        //Se determina que componente genero un evento
        switch (v.getId())
        {
            case R.id.btnCalcular:
            Intent sendIntent = new Intent(PrimerProyecto.this,ActivityDos.class);
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

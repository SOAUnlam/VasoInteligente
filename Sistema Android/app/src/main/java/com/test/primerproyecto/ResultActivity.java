package com.test.primerproyecto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    private Bundle Resutado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView txtTemperatura = (TextView) findViewById(R.id.txtTemperatura);
        TextView txtMiligramos = (TextView) findViewById(R.id.txtMiligramos);
        TextView txtGraduacion = (TextView) findViewById(R.id.txtGraduacion);

        Resutado = getIntent().getExtras();
        String temperatura = Resutado.getString("TEMPERATURA");
        String miligramos = Resutado.getString("MILIGRAMOS");
        String volumen = Resutado.getString("VOLUMEN");
        String ccubicos = Resutado.getString("CCUBICOS");

        txtTemperatura.append(temperatura + " °C");
        txtMiligramos.append(miligramos + " mg");
        txtGraduacion.append(volumen + " ° / " + ccubicos + " ml");


        Button btnSalir = (Button) findViewById(R.id.btnSalir);

        btnSalir.setOnClickListener(this);
    }

    public void onClick(View v)
    {

        //Se determina que componente genero un evento
        if(v.getId() == R.id.btnSalir)
        {
            Intent i = new Intent(this,SplashActivity.class);
            i.putExtra("EXIT",true);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed()
    {

    }

}

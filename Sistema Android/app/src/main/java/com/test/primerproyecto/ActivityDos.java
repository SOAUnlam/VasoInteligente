package com.test.primerproyecto;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ActivityDos extends AppCompatActivity {

    private Bundle bundle;
    private TextView txtResultado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dos);
        txtResultado = (TextView)findViewById(R.id.txtResultado);
        /*

        String peso = bundle.getString("peso");
        String estatura = bundle.getString("estatura");
        String sexo = bundle.getString("sexo");
*/
        bundle = getIntent().getExtras();
        String resultado = bundle.getString("RESULTADO");
        txtResultado.append("GRADUACION: " + resultado);

    }

}

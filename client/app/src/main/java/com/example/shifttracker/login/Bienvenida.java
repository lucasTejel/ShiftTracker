package com.example.shifttracker.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.shifttracker.PrincipalAplicacion;
import com.example.shifttracker.R;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Usuario;

import java.io.Serializable;

public class Bienvenida extends AppCompatActivity implements Serializable {

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        //Se conecta al servidor, crea los sockets y flujos
        Controlador.conectarAServidor(this);

        //Si se ha iniciado sesión, iniciar automáticamente
        inicioSesionAutomatico();
    }

    //Si existen datos en SharedPreferences, se extraen y se incia la sesión automáticamente
    public void inicioSesionAutomatico() {
        String[] credenciales = Controlador.extraerCredencialesUsuario(this);

        //Habla con el servidor
        Usuario usuario = null;
        if (!credenciales[0].equals("") && !credenciales[1].equals("")) {
            usuario = Controlador.iniciarSesion(this, credenciales[0], credenciales[1]);
        }
        if (usuario != null) {
            Controlador.guardarDatosUsuario(this, usuario);
            nuevaActividad(PrincipalAplicacion.class);
        }
    }

    //Establece el evento OnClick en el botón registrarse
    public void onClickRegistrarse(View view) {
        nuevaActividad(Registrarse.class);
    }

    //Establece el evento OnClick en el botón iniciar sesión
    public void onClickIniciarSesion(View view) {
        nuevaActividad(IniciarSesion.class);
    }

    //Inicia una nueva actividad dada una clase
    public void nuevaActividad(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ActivityBienvenida", this);
        intencion.putExtras(bundle);
        startActivity(intencion);
    }

}
package com.example.shifttracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.shifttracker.PrincipalAplicacion;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.R;
import com.example.shifttracker.pojo.Usuario;
import com.google.android.material.textfield.TextInputEditText;

public class IniciarSesion extends AppCompatActivity {

    private transient TextInputEditText textInputEditTextEmail;
    private transient TextInputEditText textInputEditTextContrasena;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        establecerVistas();
    }

    //Establece las vistas de la actividad
    public void establecerVistas() {
        textInputEditTextEmail = findViewById(R.id.actIniciarSesionTextInputEditTextEmail);
        textInputEditTextContrasena = findViewById(R.id.actIniciarSesionTextInputEditTextContrasena);
    }

    //Establece el evento OnClick en el botón iniciar sesión
    public void onClickIniciarSesion(View view) {
        //Habla con el servidor
        Usuario usuario = enviarInicioSesion();
        if (usuario != null) {
            Controlador.guardarDatosUsuario(this, usuario);
            nuevaActividad(PrincipalAplicacion.class);
        }
    }

    //Extrae los datos escritos por el usuario y los envia al servidor
    public Usuario enviarInicioSesion() {
        String email = textInputEditTextEmail.getText().toString();
        String contrasena = textInputEditTextContrasena.getText().toString();

        return Controlador.iniciarSesion(this, email, contrasena);
    }

    //Inicia una nueva actividad dada una clase
    public void nuevaActividad(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        startActivity(intencion);
    }


}
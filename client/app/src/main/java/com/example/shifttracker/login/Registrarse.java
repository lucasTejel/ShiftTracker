package com.example.shifttracker.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.shifttracker.R;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;

public class Registrarse extends AppCompatActivity implements Serializable {

    private transient TextInputEditText textInputEditTextUsuario;
    private transient TextInputEditText textInputEditTextEmail;
    private transient TextInputEditText textInputEditTextTelefono;
    private transient TextInputEditText textInputEditTextContrasena;
    private transient RadioGroup radioGroupTipoUsuario;
    private transient RadioButton radioButtonEmpleadoAlumno;
    private transient RadioButton radioButtonAdministrador;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        establecerVistas();
        establecerOnCheckedChangeListener();
    }

    //Establece las vistas de la actividad
    public void establecerVistas() {
        textInputEditTextUsuario = findViewById(R.id.actRegistrarseTextInputEditTextUsuario);
        textInputEditTextEmail = findViewById(R.id.actRegistrarseTextInputEditTextEmail);
        textInputEditTextTelefono = findViewById(R.id.actRegistrarseTextInputEditTextTelefono);
        textInputEditTextContrasena = findViewById(R.id.actRegistrarseTextInputEditTextContrasena);
        radioGroupTipoUsuario = findViewById(R.id.actRegistrarseRadioGroupTipoUsuario);
        radioButtonEmpleadoAlumno = findViewById(R.id.actRegistrarseRadioButtonEmpleadoAlumno);
        radioButtonAdministrador = findViewById(R.id.actRegistrarseRadioButtonAdministrador);
    }

    //Establece el evento OnCheckedChange en el RadioGroup para seleccionar el tipo de usuario
    public void establecerOnCheckedChangeListener() {
        radioGroupTipoUsuario.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.actRegistrarseRadioButtonEmpleadoAlumno) {
                    radioButtonEmpleadoAlumno.setTextColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonEmpleadoAlumno.setBackgroundColor(getResources().getColor(R.color.white, null));
                    radioButtonAdministrador.setTextColor(getResources().getColor(R.color.white, null));
                    radioButtonAdministrador.setBackground(getDrawable(R.drawable.bordes_blancos));
                }
                else {
                    radioButtonAdministrador.setTextColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonAdministrador.setBackgroundColor(getResources().getColor(R.color.white, null));
                    radioButtonEmpleadoAlumno.setTextColor(getResources().getColor(R.color.white, null));
                    radioButtonEmpleadoAlumno.setBackground(getDrawable(R.drawable.bordes_blancos));
                }
            }
        });
    }

    //Establece el evento OnClick en el botón registrarse
    public void onClickRegistrarse(View view) {
        if (enviarRegistro()) {
            nuevaActividad(IniciarSesion.class);
        }
    }

    //Extrae los datos escritos por el usuario y los envia al servidor
    public boolean enviarRegistro() {
        String nombre = textInputEditTextUsuario.getText().toString();
        String email = textInputEditTextEmail.getText().toString();
        String telefono = textInputEditTextTelefono.getText().toString();
        String contrasena = textInputEditTextContrasena.getText().toString();
        boolean administrador = radioButtonAdministrador.isChecked();

        return Controlador.registrarse(this, nombre, email, telefono, contrasena, administrador);
    }

    //Inicia una nueva actividad dada una clase
    public void nuevaActividad(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ActivityBienvenida", recogerBienvenidaBundle());
        bundle.putSerializable("ActivityRegistrarse", this);

        intencion.putExtras(bundle);
        
        startActivity(intencion);
    }

    //Recoge del bundle el objeto ActividadPrincipal
    public Bienvenida recogerBienvenidaBundle() {
        Bundle datos = this.getIntent().getExtras();
        return (Bienvenida) datos.getSerializable("ActivityBienvenida");
    }
}
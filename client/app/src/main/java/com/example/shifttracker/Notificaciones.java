package com.example.shifttracker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Notificacion;

import java.util.ArrayList;
import java.util.List;

public class Notificaciones extends AppCompatActivity {

    private List<Notificacion> listaNotificacionesConsultada;
    private TextView textViewListadoNotificaciones;
    private ListView listaNotificaciones;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        establecerVistas();
        establecerInformacion();
    }

    //Establece las vistas de la actividad
    public void establecerVistas() {
        textViewListadoNotificaciones = findViewById(R.id.actNotificacionesTextViewNotificaciones);
        listaNotificaciones = findViewById(R.id.actNotificacionesListViewNotificaciones);
    }

    //Establece la información en las vistas
    public void establecerInformacion() {
        listaNotificacionesConsultada = Controlador.cargarNotificaciones(this);
        establecerListaNotificaciones(listaNotificacionesConsultada);
    }

    //Coloca la lista de notificaciones, si existen
    public void establecerListaNotificaciones(List<Notificacion> listaNotificacionesConsultada) {
        if (listaNotificacionesConsultada.size() <= 0 ||  listaNotificacionesConsultada == null) {
            textViewListadoNotificaciones.setText(getString(R.string.listado_notificaciones_vacio));
        }
        else {
            String descripcion;
            String nombreUsuario;
            String nombreGrupo;
            List<String> listaNotificacionesDescripciones = new ArrayList<>();
            for (Notificacion notificacion : listaNotificacionesConsultada) {
                descripcion = notificacion.getDescripcion();
                nombreUsuario = descripcion.substring(descripcion.indexOf("nombreUsuario=") + "nombreUsuario=".length(),
                        descripcion.indexOf(";nombreGrupo="));
                nombreGrupo = descripcion.substring(descripcion.indexOf("nombreGrupo=") + "nombreGrupo=".length(),
                        descripcion.indexOf(";;"));
                listaNotificacionesDescripciones.add("[ " +
                        notificacion.getFechaHoraRecibo().toString().replace(".0", "") + " ] "
                        + nombreGrupo + getString(R.string.notificacion_nueva_asistencia) + nombreUsuario);
            }

            ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaNotificacionesDescripciones);
            listaNotificaciones.setAdapter(adaptador);
        }
    }
}
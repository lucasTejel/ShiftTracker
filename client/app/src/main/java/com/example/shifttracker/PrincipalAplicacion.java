package com.example.shifttracker;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.grupo.PrincipalGrupo;
import com.example.shifttracker.login.Bienvenida;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioAdministrador;
import com.example.shifttracker.pojo.UsuarioComun;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrincipalAplicacion extends AppCompatActivity implements Serializable {

    private List<Grupo> listaGruposConsultada;
    private transient ListView listaGrupos;
    private transient TextView textViewNombreUsuario;
    private transient TextView textViewCargoEstado;
    private transient ImageView imageViewEditarCargo;
    private transient ImageView imageViewNotificaciones;
    private transient TextView textViewListadoGrupos;
    private transient Button buttonNuevoGrupo;
    private transient Dialog ventanaDialogo;
    private transient ImageView imageViewBotonCerrar;
    private transient TextInputEditText textInputEditText;
    private transient TextInputEditText textInputEditText2;
    private transient Button buttonAceptar;
    private transient CheckBox[] checkBoxesDiasSemana;
    private transient TextView textViewInicio;
    private transient TextView textViewFin;
    private transient TextView textViewHoraInicio;
    private transient TextView textViewHoraFin;
    private transient TextView textViewMensaje;
    private boolean administrador;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_aplicacion);


        establecerVistas();
        establecerInformacion();

        administrador = Controlador.extraerEsAdministrador(this);

        establecerOnBackPressedCallBack();
        establecerOnItemClickListener();
        if (administrador) {
            establecerOnItemLongClickListener();
        }

        if (!administrador){
            eliminarVistasUsuarioComun();
        }
    }

    //En caso de no ser administrador, elimina las vistas correspondientes.
    public void eliminarVistasUsuarioComun() {
        imageViewEditarCargo.setVisibility(View.INVISIBLE);
        imageViewNotificaciones.setVisibility(View.INVISIBLE);
        buttonNuevoGrupo.setVisibility(View.INVISIBLE);
    }

    //Establece las vistas de la actividad
    public void establecerVistas() {
        //Establecer la información en el layout de la actividad
        textViewNombreUsuario = findViewById(R.id.actPrincipalTextViewNombreUsuario);
        textViewCargoEstado = findViewById(R.id.actPrincipalTextViewCargoEstado);
        imageViewEditarCargo = findViewById(R.id.actPrincipalImageViewEditarCargo);
        imageViewNotificaciones = findViewById(R.id.actPrincipalImageViewNotificaciones);
        textViewListadoGrupos = findViewById(R.id.actPrincipalTextViewListadoGrupos);
        listaGrupos = findViewById(R.id.actPrincipalListViewGrupos);
        buttonNuevoGrupo = findViewById(R.id.actPrincipalButtonNuevoGrupo);
    }

    //Establece la información en las vistas
    public void establecerInformacion() {
        Map<String, Object> objetosAdjuntos = Controlador.cargarPrincipalAplicacion(this);
        if (objetosAdjuntos != null) {
            Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
            establecerUsuario(usuario);

            listaGruposConsultada = (List<Grupo>) objetosAdjuntos.get("listaGrupos");
            establecerListaGrupos(listaGruposConsultada);
        }
    }

    //Al pulsar atrás, sale de la aplicación, manteniendose esta en segundo plano
    public void establecerOnBackPressedCallBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    //Establece el evento OnItemClick en los items de la lista
    public void establecerOnItemClickListener() {
        //Al pulsar, se entra en el grupo
        listaGrupos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Grupo grupo = listaGruposConsultada.get(position);
                nuevaActividadGrupo(PrincipalGrupo.class, grupo);
            }
        });
    }

    //Establece el evento OnItemLongClick en los items de la lista
    public void establecerOnItemLongClickListener() {
        //Al mantener pulsado, se elimina el grupo
        listaGrupos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                @LayoutRes int layoutResID = R.layout.win_dialog_confirmar;
                ventanaDialogo = new Dialog(view.getContext());
                abrirVentanaDialogo(layoutResID);
                establecerVistasEliminarGrupo(position);
                return true;
            }
        });
    }

    //Establece la información del usuario en la cabecera de la actividad
    public void establecerUsuario(Usuario usuario) {
        textViewNombreUsuario.setText(usuario.getNombreUsuario());
        if (usuario instanceof UsuarioComun) {
            UsuarioComun usuarioComun = (UsuarioComun) usuario;
            if (usuarioComun.getEstado()) {
                textViewCargoEstado.setText(getString(R.string.esta_presente));
            }
            else {
                textViewCargoEstado.setText(getString(R.string.no_esta_presente));
            }
        }
        else if (usuario instanceof UsuarioAdministrador) {
            UsuarioAdministrador usuarioAdministrador = (UsuarioAdministrador) usuario;
            if (usuarioAdministrador.getCargo() != null) {
                textViewCargoEstado.setText(usuarioAdministrador.getCargo());
            }
            else {
                textViewCargoEstado.setText(getString(R.string.sin_cargo));
            }
        }
    }

    //Coloca la lista de grupos, si existen
    public void establecerListaGrupos(List<Grupo> listaGruposConsultada) {
        if (listaGruposConsultada.size() <= 0) {
            textViewListadoGrupos.setText(getString(R.string.listado_grupos_vacio));
        }
        else {
            List<String> listaGruposNombres = new ArrayList<>();
            for (Grupo grupo : listaGruposConsultada) {
                listaGruposNombres.add(grupo.getNombre());
            }

            ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaGruposNombres);
            listaGrupos.setAdapter(adaptador);
        }
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo de editar nombre usuario
    public void onClickEditarNombreUsuario(View view) {
        @LayoutRes int layoutResID = R.layout.win_dialog_editar_nombre_usuario;
        ventanaDialogo = new Dialog(this);
        abrirVentanaDialogo(layoutResID);
        establecerVistasEditarNombreUsuario();
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo de editar cargo
    public void onClickEditarCargo(View view) {
        @LayoutRes int layoutResID = R.layout.win_dialog_editar_cargo;
        ventanaDialogo = new Dialog(this);
        abrirVentanaDialogo(layoutResID);
        establecerVistasEditarCargo();
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo de nuevo grupo
    public void onClickNuevoGrupo(View view) {
        @LayoutRes int layoutResID = R.layout.win_dialog_nuevo_grupo;
        ventanaDialogo = new Dialog(this);
        abrirVentanaDialogo(layoutResID);
        establecerVistasNuevoGrupo();
    }

    //Establece las vistas de la ventana nuevo grupo
    public void establecerVistasEditarNombreUsuario() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogEditarNombreUsuarioImageViewCerrar);
        textInputEditText = ventanaDialogo.findViewById(R.id.actIniciarSesionTextInputEditTextEmail);
        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogEditarNombreUsuarioButtonAceptar);
        establecerEditarNombreUsuarioOnClickListener();
    }

    //Establece el evento OnClick del boton aceptar de la ventana editar nombre usuario
    public void establecerEditarNombreUsuarioOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idUsuario = Controlador.extraerIdUsuario(PrincipalAplicacion.this);
                String nombreUsuario = textInputEditText.getText().toString();
                Usuario usuario = new UsuarioComun(idUsuario, nombreUsuario);
                nombreUsuario = Controlador.editarNombreUsuario(PrincipalAplicacion.this, usuario);
                if (nombreUsuario != null) {
                    textViewNombreUsuario.setText(nombreUsuario);
                    ventanaDialogo.dismiss();
                }
            }
        });
    }

    //Establece las vistas de la ventana editar cargo
    public void establecerVistasEditarCargo() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogEditarCargoImageViewCerrar);
        textInputEditText = ventanaDialogo.findViewById(R.id.winDialogEditarCargoTextInputEditTextUsuario);
        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogEditarCargoButtonAceptar);
        establecerEditarCargoOnClickListener();
    }

    //Establece el evento OnClick del boton aceptar de la ventana editar cargo
    public void establecerEditarCargoOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idUsuario = Controlador.extraerIdUsuario(PrincipalAplicacion.this);
                String cargo = textInputEditText.getText().toString();
                UsuarioAdministrador usuario = new UsuarioAdministrador(idUsuario, cargo);
                cargo = Controlador.editarCargo(PrincipalAplicacion.this, usuario);
                if (cargo != null) {
                    textViewCargoEstado.setText(cargo);
                    ventanaDialogo.dismiss();
                }
            }
        });
    }

    //Establece las vistas de la ventana nuevo grupo
    public void establecerVistasNuevoGrupo() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoImageViewCerrar);
        textInputEditText = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextInputEditTextNombre);
        textInputEditText2 = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextInputEditTextDescripcion);
        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoButtonAceptar);

        checkBoxesDiasSemana = new CheckBox[7];
        checkBoxesDiasSemana[0] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxLun);
        checkBoxesDiasSemana[1] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxMar);
        checkBoxesDiasSemana[2] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxMie);
        checkBoxesDiasSemana[3] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxJue);
        checkBoxesDiasSemana[4] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxVie);
        checkBoxesDiasSemana[5] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxSab);
        checkBoxesDiasSemana[6] = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoCheckBoxDom);
        for (CheckBox checkBoxDiaSemana : checkBoxesDiasSemana) {
            establecerOnCheckedChangeListener(checkBoxDiaSemana);
        }

        textViewInicio = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextViewInicio);
        textViewFin = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextViewFin);
        textViewHoraInicio = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextViewInicioHora);
        textViewHoraFin = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextViewFinHora);

        establecerNuevoGrupoOnClickListener();
    }

    //Establece el evento OnClick del boton aceptar de la ventana nuevo grupo
    public void establecerNuevoGrupoOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = textInputEditText.getText().toString();
                String descripcion = textInputEditText2.getText().toString();
                String horario = extraerHorarioNuevoGrupo();
                int idUsuarioAdministrador = Controlador.extraerIdUsuario(PrincipalAplicacion.this);
                Grupo grupoNuevo = new Grupo(nombre, descripcion, horario, idUsuarioAdministrador);
                grupoNuevo = Controlador.anadirGrupo(PrincipalAplicacion.this, grupoNuevo);
                if (grupoNuevo != null) {
                    listaGruposConsultada.add(grupoNuevo);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) listaGrupos.getAdapter();
                    adapter.add(grupoNuevo.getNombre());
                    adapter.notifyDataSetChanged();
                    ventanaDialogo.dismiss();
                }

            }
        });
    }

    //Devuelve un String con el formato deseado de horario
    public String extraerHorarioNuevoGrupo() {
        String[] diasSemana = {"L", "M", "X", "J", "V", "S", "D"};

        String horario = "";
        int primerDia = 0;
        int contador = 0;
        int ultimoDia = 0;
        for (int i = 0; i < checkBoxesDiasSemana.length; i++) {
            primerDia = -1;
            contador = -1;
            ultimoDia = -1;
            if (checkBoxesDiasSemana[i].isChecked()) {
                primerDia = i;
                contador = i;
                while (ultimoDia == -1) {
                    if (contador != 6  && checkBoxesDiasSemana[contador + 1].isChecked()) {
                        contador++;
                    }
                    else {
                        ultimoDia = contador;
                        if (horario.length() > 0) {
                            horario += ", ";
                        }

                        if (primerDia != ultimoDia) {
                            horario += diasSemana[primerDia] + "-" + diasSemana[ultimoDia];
                        }
                        else {
                            horario += diasSemana[ultimoDia];
                        }
                        i = ultimoDia + 1;

                    }
                }
                ultimoDia = -1;
            }

        }

        if (horario.length() <= 0) {
            horario += diasSemana[0] + "-" + diasSemana[diasSemana.length - 1];
        }

        return horario + "; " + textViewHoraInicio.getText().toString() + "-" + textViewHoraFin.getText().toString();
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo eliminar grupo
    public void establecerVistasEliminarGrupo(int posicion) {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogConfirmarImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view);
            }
        });

        textViewMensaje = ventanaDialogo.findViewById(R.id.winDialogConfirmarTextViewMensaje);
        textViewMensaje.setText(getString(R.string.eliminar_grupo));

        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogConfirmarButtonAceptar);
        establecerEliminarGrupoOnClickListener(posicion);
    }

    //Establece el evento OnClick del boton aceptar de la ventana nuevo grupo
    public void establecerEliminarGrupoOnClickListener(int posicion) {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Grupo grupo = listaGruposConsultada.get(posicion);
                if (Controlador.eliminarGrupo(PrincipalAplicacion.this, grupo)) {
                    listaGruposConsultada.remove(grupo);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) listaGrupos.getAdapter();
                    adapter.remove(grupo.getNombre());
                    adapter.notifyDataSetChanged();
                    ventanaDialogo.dismiss();
                }
            }
        });
    }

    //Establece el evento OnCheckedChange en los checkboxes de los días de la semana en la
    //ventana nuevo grupo
    public void establecerOnCheckedChangeListener(CheckBox checkBoxDiaSemana) {
        checkBoxDiaSemana.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setTextColor(getResources().getColor(R.color.blue_app, null));
                    buttonView.setBackground(getDrawable(R.drawable.fondo_redondo_blanco));
                }
                else {
                    buttonView.setTextColor(getResources().getColor(R.color.white, null));
                    buttonView.setBackground(getDrawable(R.drawable.bordes_blancos_redondo));
                }
            }
        });
    }

    //Abre una ventana de diálogo con el layout del parámetro
    public void abrirVentanaDialogo(@LayoutRes int layoutResID) {
        ventanaDialogo.setContentView(layoutResID);
        ventanaDialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ventanaDialogo.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ventanaDialogo.show();
    }

    //Establece el evento OnClick para iniciar la actividad Notificaciones
    public void onClickNotificaciones(View view) {
        nuevaActividad(Notificaciones.class);
    }

    //Establece el evento OnClick para cerrar la sesión y volver a la actividad de bienvenida
    public void onClickCerrarSesion(View view) {
        Controlador.eliminarDatosUsuario(this);
        Intent intent = new Intent(getApplicationContext(), Bienvenida.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //Desplega un TimePickerDialog para seleccionar la hora
    public void desplegarTimePicker(View vista) {
        // Código para desplegar TimePicker
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.Picker, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (vista == textViewInicio || vista == textViewHoraInicio) {
                    textViewHoraInicio.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                }
                else {
                    textViewHoraFin.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                }
            }
        }, 0, 0, true);

        timePickerDialog.show();
    }

    //Establece el evento OnClick en el botón de cerrar X
    public void onClickCerrar(View view) {
        ventanaDialogo.dismiss();
    }

    //Inicia una nueva actividad dada una clase
    public void nuevaActividad(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        startActivity(intencion);
    }

    //Inicia una nueva actividad pasando en un bundle el grupo, dada una clase
    public void nuevaActividadGrupo(Class<?> clase, Grupo grupo) {
        Intent intencion = new Intent(this, clase);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Grupo", grupo);
        bundle.putSerializable("ActivityPrincipalAplicacion", this);
        intencion.putExtras(bundle);
        startActivity(intencion);
    }
}
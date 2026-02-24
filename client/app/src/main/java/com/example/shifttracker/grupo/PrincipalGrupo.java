package com.example.shifttracker.grupo;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.shifttracker.NfcAcceso;
import com.example.shifttracker.Notificaciones;
import com.example.shifttracker.PrincipalAplicacion;
import com.example.shifttracker.R;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Grupo;
import com.google.android.material.textfield.TextInputEditText;

public class PrincipalGrupo extends AppCompatActivity {

    private Grupo grupo;
    private TextView textViewNombre;
    private TextView textViewDescripcion;
    private TextView textViewHorario;
    private TextView textViewNfc;
    private ImageView imageViewEditarNombre;
    private ImageView imageViewEditarDescripcion;
    private ImageView imageViewNotificaciones;
    private Dialog ventanaDialogo;
    private ImageView imageViewBotonCerrar;
    private TextInputEditText textInputEditTextEditar;
    private Button buttonAceptar;

    private RadioGroup radioGroupFragmentos;
    private RadioButton radioButtonMiembros;
    private RadioButton radioButtonCalendario;
    private FragmentContainerView fragmentoMiembros;
    private FragmentContainerView fragmentoCalendario;
    private boolean administrador;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_grupo);

        establecerOnBackPressedCallBack();
        establecerVistas();
        establecerInformacion();

        establecerOnCheckedChangeListener();

        administrador = Controlador.extraerEsAdministrador(this);
        if (!administrador){
            eliminarVistasUsuarioComun();
        }
    }

    //En caso de no ser administrador, elimina las vistas correspondientes.
    public void eliminarVistasUsuarioComun() {
        imageViewEditarNombre.setVisibility(View.INVISIBLE);
        imageViewEditarDescripcion.setVisibility(View.INVISIBLE);
        imageViewNotificaciones.setVisibility(View.INVISIBLE);
        textViewNfc.setText(getString(R.string.nueva_asistencia));
    }

    //Al volver atrás, se inicia una nueva actividad PrincipalAplicacion, para que cargue los nuevos datos
    public void establecerOnBackPressedCallBack() {
        //Al pulsar atrás, elimina la anterior actividad PrincipalAplicacion y ejecuta una nueva
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                PrincipalAplicacion principalAplicacion = recogerPrincipalAplicacionaBundle();
                principalAplicacion.finish();
                nuevaActividad(PrincipalAplicacion.class);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    //Establece las vistas de la actividad
    public void establecerVistas() {
        textViewNombre = findViewById(R.id.actPrincipalGrupoTextViewNombreGrupo);
        textViewDescripcion = findViewById(R.id.actPrincipalGrupoTextViewDescripcion);
        textViewHorario = findViewById(R.id.actPrincipalGrupoTextViewHorario);
        imageViewEditarNombre = findViewById(R.id.actPrincipalGrupoImageViewEditarNombreGrupo);
        imageViewEditarDescripcion = findViewById(R.id.actPrincipalGrupoImageViewEditarDescripcion);
        imageViewNotificaciones = findViewById(R.id.actPrincipalGrupoImageViewNotificaciones);
        textViewNfc = findViewById(R.id.actPrincipalGrupoTextViewRfidAnadir);

        radioGroupFragmentos = findViewById(R.id.actPrincipalGrupoRadioGroupFragmentos);
        radioButtonMiembros = findViewById(R.id.actPrincipalGrupoRadioButtonMiembros);
        radioButtonCalendario = findViewById(R.id.actPrincipalGrupoRadioButtonCalendario);

        fragmentoMiembros = findViewById(R.id.actPrincipalGrupoFragmentContainerViewMiembros);
        fragmentoCalendario = findViewById(R.id.actPrincipalGrupoFragmentContainerViewCalendario);
    }

    //Establece la información en las vistas
    public void establecerInformacion() {
        grupo = recogerGrupoBundle();

        textViewNombre.setText(grupo.getNombre());
        textViewDescripcion.setText(grupo.getDescripcion());
        textViewHorario.setText(grupo.getHorario());
    }

    //Establece el evento OnCheckedChange en el RadioGroup para seleccionar el fragmento a visualizar
    public void establecerOnCheckedChangeListener() {
        radioGroupFragmentos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.actPrincipalGrupoRadioButtonMiembros) {
                    radioButtonMiembros.setTextColor(getResources().getColor(R.color.white, null));
                    radioButtonMiembros.setBackgroundColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonCalendario.setTextColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonCalendario.setBackground(getDrawable(R.drawable.bordes_azules));

                    fragmentoCalendario.setVisibility(View.GONE);
                    fragmentoMiembros.setVisibility(View.VISIBLE);
                }
                else {
                    radioButtonCalendario.setTextColor(getResources().getColor(R.color.white, null));
                    radioButtonCalendario.setBackgroundColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonMiembros.setTextColor(getResources().getColor(R.color.blue_app, null));
                    radioButtonMiembros.setBackground(getDrawable(R.drawable.bordes_azules));

                    fragmentoMiembros.setVisibility(View.GONE);
                    fragmentoCalendario.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Recoge el objeto Grupo del bundle
    public Grupo recogerGrupoBundle() {
        Bundle datos = this.getIntent().getExtras();
        return (Grupo) datos.getSerializable("Grupo");
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo de editar nombre grupo
    public void onClickEditarNombreGrupo(View view) {
        @LayoutRes int layoutResID = R.layout.win_dialog_editar_nombre_grupo;
        ventanaDialogo = new Dialog(this);
        abrirVentanaDialogo(layoutResID);
        establecerVistasEditarNombreGrupo();
    }

    //Establece el evento OnClick para iniciar la ventana de diálogo de editar descripción grupo
    public void onClickEditarDescripcionGrupo(View view) {
        @LayoutRes int layoutResID = R.layout.win_dialog_editar_descripcion_grupo;
        ventanaDialogo = new Dialog(this);
        abrirVentanaDialogo(layoutResID);
        establecerVistasEditarDescripcion();
    }

    //Establece las vistas de la ventana editar nombre grupo
    public void establecerVistasEditarNombreGrupo() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogEditarNombreGrupoImageViewCerrar);
        textInputEditTextEditar = ventanaDialogo.findViewById(R.id.winDialogNuevoGrupoTextInputEditTextNombre);
        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogEditarNombreGrupoButtonAceptar);
        establecerEditarNombreGrupoOnClickListener();
    }

    //Establece el evento OnClick para del botón aceptar de la ventana de diálogo editar nombre grupo
    public void establecerEditarNombreGrupoOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idGrupo = grupo.getIdGrupo();
                String nombreGrupo = textInputEditTextEditar.getText().toString();
                Grupo grupoNuevo = new Grupo(idGrupo, nombreGrupo);
                nombreGrupo = Controlador.editarNombreGrupo(PrincipalGrupo.this, grupoNuevo);
                if (nombreGrupo != null) {
                    textViewNombre.setText(nombreGrupo);
                    ventanaDialogo.dismiss();
                }
            }
        });
    }

    //Establece las vistas de la ventana editar descripcion grupo
    public void establecerVistasEditarDescripcion() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogEditarDescripcionGrupoImageViewCerrar);
        textInputEditTextEditar = ventanaDialogo.findViewById(R.id.winDialogEditarDescripcionGrupoTextInputEditTextGrupo);
        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogEditarDescripcionGrupoButtonAceptar);
        establecerEditarDescripcionGrupoOnClickListener();
    }

    //Establece el evento OnClick para del botón aceptar de la ventana de diálogo editar descripción grupo
    public void establecerEditarDescripcionGrupoOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idGrupo = grupo.getIdGrupo();
                String descripcionGrupo = textInputEditTextEditar.getText().toString();
                Grupo grupoNuevo = new Grupo(descripcionGrupo, idGrupo);
                descripcionGrupo = Controlador.editarDescripcionGrupo(PrincipalGrupo.this, grupoNuevo);
                if (descripcionGrupo != null) {
                    textViewDescripcion.setText(descripcionGrupo);
                    ventanaDialogo.dismiss();
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

    //Establece el evento OnClick para iniciar la actividad NfcAcceso
    public void onClickNfc(View view) {
        nuevaActividadGrupo(NfcAcceso.class);
    }

    //Establece el evento OnClick en el botón de cerrar X
    public void onClickCerrar(View view) {
        ventanaDialogo.dismiss();
    }

    //Recoge el objeto de la actividad PrincipalAplicacion del bundle
    public PrincipalAplicacion recogerPrincipalAplicacionaBundle() {
        Bundle datos = this.getIntent().getExtras();
        return (PrincipalAplicacion) datos.getSerializable("ActivityPrincipalAplicacion");
    }

    //Inicia una nueva actividad dada una clase
    public void nuevaActividad(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        startActivity(intencion);
    }

    //Inicia una nueva actividad pasando en un bundle el grupo, dada una clase
    public void nuevaActividadGrupo(Class<?> clase) {
        Intent intencion = new Intent(this, clase);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Grupo", grupo);
        intencion.putExtras(bundle);
        startActivity(intencion);
    }

    public Grupo getGrupo() {
        return grupo;
    }
}
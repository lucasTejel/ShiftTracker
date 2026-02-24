package com.example.shifttracker.grupo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.shifttracker.R;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.PeriodoFestivo;
import com.example.shifttracker.pojo.PeriodoVacacional;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioAdministrador;
import com.example.shifttracker.pojo.UsuarioComun;
import com.google.android.material.textfield.TextInputEditText;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class FragmentoMiembros extends Fragment {

    // Los parámetros de inicialización del fragmento
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View vista;


    private List<Usuario> listaMiembrosConsultada;
    private Grupo grupo;
    private TextView textViewListadoMiembros;
    private ListView listaMiembros;
    private Button buttonNuevoMiembro;
    private Dialog ventanaDialogo;
    private Dialog ventanaDialogo2;
    private ImageView imageViewBotonCerrar;
    private TextInputEditText textInputEditTextAnadir;
    private Button buttonAceptar;
    private TextView textViewNombreUsuario;
    private TextView textViewEmail;
    private TextView textViewTelefono;
    private TextView textViewCargoEstado;
    private Button buttonEliminarMiembro;
    private Button buttonNuevaAsistencia;
    private TextView textViewFecha;
    private TextView textViewEstablecerFecha;
    private TextView textViewHora;
    private TextView textViewEstablecerHora;
    private Button buttonCalendario;
    private MaterialCalendarView materialCalendarView;
    private Button buttonAnadirVacaciones;
    private TextView textViewInicio;
    private TextView textViewFechaInicio;
    private TextView textViewFin;
    private TextView textViewFechaFin;
    private boolean administrador;

    public FragmentoMiembros() {
        // Se requiere un constructor vacío
    }

    // Crea una nueva instancia del fragmento usando los parámetros proporcionados
    public static FragmentoMiembros newInstance(String param1, String param2) {
        FragmentoMiembros fragment = new FragmentoMiembros();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //Se ejecuta al crear la vista del fragmento
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Carga el layout para este fragmento
        vista = inflater.inflate(R.layout.fragment_miembros, container, false);

        administrador = Controlador.extraerEsAdministrador(getActivity());

        establecerVistas();
        establecerInformacion();

        establecerOnItemClickListener();
        if (administrador) {
            establecerOnItemLongClickListener();
        }
        establecerOnClickListener();

        if (!administrador){
            eliminarVistasUsuarioComun();
        }

        return vista;
    }

    //En caso de no ser administrador, elimina las vistas correspondientes.
    public void eliminarVistasUsuarioComun() {
        buttonNuevoMiembro.setVisibility(View.INVISIBLE);
    }

    //Establece las vistas del fragmento
    public void establecerVistas() {
        textViewListadoMiembros = vista.findViewById(R.id.actPrincipalGrupoTextViewListadoMiembros);
        listaMiembros = vista.findViewById(R.id.actPrincipalGrupoListViewMiembros);
        buttonNuevoMiembro = vista.findViewById(R.id.actPrincipalGrupoButtonNuevoMiembro);
    }

    //Establece la información en las vistas
    public void establecerInformacion() {
        PrincipalGrupo actividad = (PrincipalGrupo) getActivity();
        grupo = actividad.getGrupo();
        int idGrupo = grupo.getIdGrupo();

        listaMiembrosConsultada = Controlador.cargarListaMiembrosGrupo(actividad, idGrupo);
        establecerListaMiembros(listaMiembrosConsultada);
    }

    //Establece el evento OnItemClick en los items de la lista
    public void establecerOnItemClickListener() {
        listaMiembros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @LayoutRes int layoutResID = R.layout.win_dialog_ver_miembro;
                ventanaDialogo = new Dialog(view.getContext());
                abrirVentanaDialogo(layoutResID, ventanaDialogo);
                establecerVistasVerMiembro();
                establecerInformacionVerMiembro(position);
            }
        });
    }

    //Establece el evento OnItemLongClick en los items de la lista
    public void establecerOnItemLongClickListener() {
        listaMiembros.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    @LayoutRes int layoutResID = R.layout.win_dialog_opciones_miembros;
                    ventanaDialogo = new Dialog(view.getContext());
                    abrirVentanaDialogo(layoutResID, ventanaDialogo);
                    establecerVistasOpcionesMiembros(position);
                }
                else {
                    Controlador.mostrarMensajeToast(getActivity(), getString(R.string.error_no_modificar_administrador));
                }
                return true;
            }
        });
    }

    //Establece el evento OnClick en el botón de nuevo miembro
    public void establecerOnClickListener() {
        buttonNuevoMiembro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @LayoutRes int layoutResID = R.layout.win_dialog_nuevo_miembro;
                ventanaDialogo = new Dialog(getContext());
                abrirVentanaDialogo(layoutResID, ventanaDialogo);
                establecerVistasNuevoMiembro();
            }
        });
    }

    //Coloca la lista de miembros, si existen
    public void establecerListaMiembros(List<Usuario> listaMiembrosConsultada) {
        if (listaMiembrosConsultada.size() <= 0) {
            textViewListadoMiembros.setText(getString(R.string.listado_miembros_vacio));
        }
        else {
            List<String> listaMiembrosNombres = new ArrayList<>();
            for (Usuario usuario : listaMiembrosConsultada) {
                listaMiembrosNombres.add(usuario.getNombreUsuario());
            }

            ArrayAdapter<String> adaptador = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listaMiembrosNombres);
            listaMiembros.setAdapter(adaptador);
        }
    }

    //Abre una ventana de diálogo con el layout del parámetro
    public void abrirVentanaDialogo(@LayoutRes int layoutResID, Dialog ventanaDialogo) {
        ventanaDialogo.setContentView(layoutResID);
        ventanaDialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ventanaDialogo.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ventanaDialogo.show();
    }

    //Establece las vistas del botón nuevo miembro
    public void establecerVistasNuevoMiembro() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogNuevoMiembroImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo);
            }
        });

        textInputEditTextAnadir = ventanaDialogo.findViewById(R.id.winDialogNuevoMiembroTextInputEditTextMiembro);

        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogNuevoMiembroButtonAceptar);
        establecerNuevoMiembroOnClickListener();
    }

    //Establece el evento OnClick en el botón aceptar de la ventana nuevo miembro
    public void establecerNuevoMiembroOnClickListener() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = textInputEditTextAnadir.getText().toString();
                UsuarioComun usuario = new UsuarioComun(email);
                Usuario usuarioRecibido = Controlador.anadirMiembroGrupo(getActivity(), usuario, grupo);
                if (usuarioRecibido != null) {
                    listaMiembrosConsultada.add(usuarioRecibido);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) listaMiembros.getAdapter();
                    adapter.add(usuarioRecibido.getNombreUsuario());
                    adapter.notifyDataSetChanged();
                    ventanaDialogo.dismiss();
                }

            }
        });
    }

    //Establece las vistas de la ventana ver miembro
    public void establecerVistasVerMiembro() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogVerMiembroImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo);
            }
        });

        textViewNombreUsuario = ventanaDialogo.findViewById(R.id.winDialogVerMiembroTextViewNombreUsuario);
        textViewEmail = ventanaDialogo.findViewById(R.id.winDialogVerMiembroTextViewEmail);
        textViewTelefono = ventanaDialogo.findViewById(R.id.winDialogVerMiembroTextViewTelefono);
        textViewCargoEstado = ventanaDialogo.findViewById(R.id.winDialogVerMiembroTextViewCargoEstado);
    }

    //Establece la información de la ventana ver miembro
    public void establecerInformacionVerMiembro(int posicion) {
        Usuario usuario = listaMiembrosConsultada.get(posicion);
        textViewNombreUsuario.setText(usuario.getNombreUsuario());
        textViewEmail.setText(usuario.getEmail());
        textViewTelefono.setText(usuario.getTelefono());

        if (usuario instanceof UsuarioAdministrador) {
            UsuarioAdministrador usuarioAdministrador = (UsuarioAdministrador) usuario;
            if (usuarioAdministrador.getCargo() != null) {
                textViewCargoEstado.setText(usuarioAdministrador.getCargo());
            }
            else {
                textViewCargoEstado.setText(getString(R.string.sin_cargo));
            }
        }
        else {
            UsuarioComun usuarioComun = (UsuarioComun) usuario;
            if (usuarioComun.getEstado()) {
                textViewCargoEstado.setText(getString(R.string.esta_presente));
            }
            else {
                textViewCargoEstado.setText(getString(R.string.no_esta_presente));
            }
        }
    }

    //Establece las vistas de la ventana opciones miembros
    public void establecerVistasOpcionesMiembros(int posicion) {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogOpcionesMiembrosImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo);
            }
        });

        buttonEliminarMiembro = ventanaDialogo.findViewById(R.id.winDialogOpcionesMiembrosButtonEliminarMiembro);
        establecerEliminarMiembroOnClickListener(posicion);

        buttonNuevaAsistencia = ventanaDialogo.findViewById(R.id.winDialogOpcionesMiembrosButtonNuevaAsistencia);
        establecerNuevaAsistenciaOnClickListener(posicion);

        buttonCalendario = ventanaDialogo.findViewById(R.id.winDialogOpcionesMiembrosButtonCalendario);
        establecerCalendarioOnClickListener(posicion);

        buttonAnadirVacaciones = ventanaDialogo.findViewById(R.id.winDialogOpcionesMiembrosButtonAnadirVacaciones);
        establecerAnadirVacacionesOnClickListener(posicion);
    }

    //Establece el evento OnClick en el botón eliminar miembro de la ventana opciones miembros
    public void establecerEliminarMiembroOnClickListener(int posicion) {
        buttonEliminarMiembro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Usuario usuario = listaMiembrosConsultada.get(posicion);
                if (Controlador.eliminarMiembroGrupo(getActivity(), usuario, grupo)) {
                    listaMiembrosConsultada.remove(usuario);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) listaMiembros.getAdapter();
                    adapter.remove(usuario.getNombreUsuario());
                    adapter.notifyDataSetChanged();
                    ventanaDialogo.dismiss();
                }
            }
        });
    }

    //Establece el evento OnClick en el botón nueva asistencia de la ventana opciones miembros
    public void establecerNuevaAsistenciaOnClickListener(int posicion) {
        buttonNuevaAsistencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @LayoutRes int layoutResID = R.layout.win_dialog_nueva_asistencia;
                ventanaDialogo2 = new Dialog(getContext());
                abrirVentanaDialogo(layoutResID, ventanaDialogo2);
                establecerVistasNuevaAsistencia(posicion);
            }
        });
    }

    //Establece el evento OnClick en el botón calendario de la ventana opciones miembros
    public void establecerCalendarioOnClickListener(int posicion) {
        buttonCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @LayoutRes int layoutResID = R.layout.win_dialog_calendario;
                ventanaDialogo2 = new Dialog(getContext());
                abrirVentanaDialogo(layoutResID, ventanaDialogo2);
                establecerVistasCalendario();
                establecerInformacionCalendario(posicion);
            }
        });
    }

    //Establece el evento OnClick en el botón añadir vacaciones de la ventana opciones miembros
    public void establecerAnadirVacacionesOnClickListener(int posicion) {
        buttonAnadirVacaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @LayoutRes int layoutResID = R.layout.win_dialog_anadir_vacaciones;
                ventanaDialogo2 = new Dialog(getContext());
                abrirVentanaDialogo(layoutResID, ventanaDialogo2);
                establecerVistasAnadirVacaciones(posicion);
            }
        });
    }

    //Establece las vistas de la ventana nueva asistencia
    public void establecerVistasNuevaAsistencia(int posicion) {
        imageViewBotonCerrar = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo2);
            }
        });

        textViewFecha = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaTextViewFecha);
        textViewEstablecerFecha = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaTextViewEstablecerFecha);
        textViewEstablecerFecha.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        textViewHora = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaTextViewHora);
        textViewEstablecerHora = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaTextViewEstablecerHora);
        textViewEstablecerHora.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));

        textViewFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewEstablecerFecha);
            }
        });
        textViewEstablecerFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewEstablecerFecha);
            }
        });
        textViewHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarTimePicker(textViewEstablecerHora);
            }
        });
        textViewEstablecerHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarTimePicker(textViewEstablecerHora);
            }
        });

        buttonAceptar = ventanaDialogo2.findViewById(R.id.winDialogNuevaAsistenciaButtonAceptar);
        establecerOnClickListenerNuevaAsistencia(posicion);
    }

    //Establece el evento OnClick en el botón aceptar de la ventana nueva asistencia
    public void establecerOnClickListenerNuevaAsistencia(int posicion) {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String fechaHoraString = textViewEstablecerFecha.getText().toString() + " " + textViewEstablecerHora.getText().toString();
                    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date fechaHora = formato.parse(fechaHoraString);
                    Timestamp timestamp = new Timestamp(fechaHora.getTime());

                    Usuario usuario = listaMiembrosConsultada.get(posicion);
                    if (Controlador.anadirAsistencia(getActivity(), usuario, grupo, timestamp)) {
                        ventanaDialogo.dismiss();
                        ventanaDialogo2.dismiss();
                    }
                } catch (ParseException e) {
                    Controlador.mostrarMensajeToast(getActivity(), getString(R.string.error_formato_fecha_hora));
                    e.printStackTrace();
                }
            }
        });
    }

    //Establece las vistas de la ventana calendario
    public void establecerVistasCalendario() {
        imageViewBotonCerrar = ventanaDialogo2.findViewById(R.id.winDialogCalendarioImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo2);
            }
        });

        materialCalendarView = ventanaDialogo2.findViewById(R.id.winDialogCalendarioMaterialCalendarView);
    }

    //Establece la información de la ventana calendario
    public void establecerInformacionCalendario(int posicion) {
        PrincipalGrupo actividad = (PrincipalGrupo) getActivity();
        grupo = actividad.getGrupo();
        Usuario usuario = listaMiembrosConsultada.get(posicion);

        Map<String, Object> mapaPeriodos = Controlador.cargarCalendarioMiembro(actividad, usuario, grupo);


        for (Map.Entry<String, Object> entrada : mapaPeriodos.entrySet()) {
            Object listaPeriodos = entrada.getValue();
            List<LocalDate> listadoDiasLocalDate;
            List<CalendarDay> listaDiasCalendarDay;

            if (entrada.getKey().equals("listaAsistencias")) {
                //Establecer lista de días de asistencias
                List<Timestamp> listaTimestamp = (List<Timestamp>) listaPeriodos;
                listadoDiasLocalDate = new ArrayList<>();
                for (Timestamp timestamp : listaTimestamp) {
                    Date fecha = new Date(timestamp.getTime());
                    listadoDiasLocalDate.add(fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }

                listaDiasCalendarDay = generarListaCalendarDay(listadoDiasLocalDate);
                pintarEnCalendario(listaDiasCalendarDay, R.drawable.fondo_redondo_verde);
            }
            else if (entrada.getKey().equals("listaPeriodosVacacionales")) {
                //Establecer lista de días vacacionales entre inicio y fin de cada periodo
                List<PeriodoVacacional> listaPeriodosVacacionales = (List<PeriodoVacacional>) listaPeriodos;
                LocalDate fechaActual;
                LocalDate fechaFin;
                listadoDiasLocalDate = new ArrayList<>();
                for (PeriodoVacacional periodoVacacional : listaPeriodosVacacionales) {
                    fechaActual = periodoVacacional.getFechaInicio();
                    fechaFin = periodoVacacional.getFechaFin();
                    while (!fechaActual.isAfter(fechaFin)) {
                        listadoDiasLocalDate.add(fechaActual);
                        fechaActual = fechaActual.plusDays(1);
                    }
                }

                listaDiasCalendarDay = generarListaCalendarDay(listadoDiasLocalDate);
                pintarEnCalendario(listaDiasCalendarDay, R.drawable.fondo_redondo_naranja);
            }
            else if (entrada.getKey().equals("listaPeriodosFestivos")) {
                //Establecer lista de días festivos entre inicio y fin de cada periodo
                List<PeriodoFestivo> listaPeriodosFestivos = (List<PeriodoFestivo>) listaPeriodos;
                LocalDate fechaActual;
                LocalDate fechaFin;
                listadoDiasLocalDate = new ArrayList<>();
                for (PeriodoFestivo periodoFestivo : listaPeriodosFestivos) {
                    fechaActual = periodoFestivo.getFechaInicio();
                    fechaFin = periodoFestivo.getFechaFin();
                    while (!fechaActual.isAfter(fechaFin)) {
                        listadoDiasLocalDate.add(fechaActual);
                        fechaActual = fechaActual.plusDays(1);
                    }
                }

                listaDiasCalendarDay = generarListaCalendarDay(listadoDiasLocalDate);
                pintarEnCalendario(listaDiasCalendarDay, R.drawable.fondo_redondo_azul);
            }

        }
    }

    //Genera una lista de CalendarDay a partir de una de LocalDate
    public List<CalendarDay> generarListaCalendarDay(List<LocalDate> listaLocalDate) {
        //Crear lista de CalendarDay a partir de lista de LocalDate
        CalendarDay calendarDay;
        List<CalendarDay> listaDiasCalendarDay = new ArrayList<>();
        for (LocalDate localDate : listaLocalDate) {
            int ano = localDate.getYear();
            int mes = localDate.getMonthValue() - 1;
            int dia = localDate.getDayOfMonth();

            calendarDay = CalendarDay.from(ano, mes, dia);
            listaDiasCalendarDay.add(calendarDay);
        }

        return listaDiasCalendarDay;
    }

    //Pinta en el calendario los días en la lista de CalendarDay en el color que se pasa por parámetro
    public void pintarEnCalendario(List<CalendarDay> listaDiasCalendarDay, @DrawableRes int color) {
        //Pintar los días que se encuentren en la lista de CalendarDay
        materialCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar calendario = day.getCalendar();
                int diaSemana = calendario.get(Calendar.DAY_OF_WEEK);
                return listaDiasCalendarDay.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), color));
            }
        });
    }

    //Establece las vistas de la ventana añadir vacaciones
    public void establecerVistasAnadirVacaciones(int posicion) {
        imageViewBotonCerrar = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view, ventanaDialogo2);
            }
        });

        textViewInicio = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesTextViewInicio);
        textViewFechaInicio = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesTextViewInicioFecha);
        textViewFechaInicio.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        textViewFin = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesTextViewFin);
        textViewFechaFin = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesTextViewFinFecha);
        textViewFechaFin.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        textViewInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewFechaInicio);
            }
        });
        textViewFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewFechaInicio);
            }
        });
        textViewFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewFechaFin);
            }
        });
        textViewFechaFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(textViewFechaFin);
            }
        });

        buttonAceptar = ventanaDialogo2.findViewById(R.id.winDialogAnadirVacacionesButtonAceptar);
        establecerOnClickListenerAnadirVacaciones(posicion);
    }

    //Establece el evento OnClick en el botón aceptar de la ventana añadir vacaciones
    public void establecerOnClickListenerAnadirVacaciones(int posicion) {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDate fechaInicio;
                LocalDate fechaFin;
                fechaInicio = LocalDate.parse(textViewFechaInicio.getText().toString());
                fechaFin = LocalDate.parse(textViewFechaFin.getText().toString());

                int comparacion = fechaInicio.compareTo(fechaFin);
                if (comparacion <= 0) {
                    Usuario usuario = listaMiembrosConsultada.get(posicion);
                    PeriodoVacacional periodoVacacional = new PeriodoVacacional(usuario.getIdUsuario(), grupo.getIdGrupo(), fechaInicio, fechaFin);
                    if (Controlador.anadirVacaciones(getActivity(), periodoVacacional)) {
                        ventanaDialogo.dismiss();
                        ventanaDialogo2.dismiss();
                    }
                }
                else {
                    Controlador.mostrarMensajeToast(getActivity(), getString(R.string.error_fecha_inicio_antes_que_fin));
                }
            }
        });
    }

    //Desplega un DatePickerDialog para seleccionar la fecha
    public void desplegarDatePicker(TextView vista) {
        // Código para desplegar DatePicker
        GregorianCalendar fechaHoy = new GregorianCalendar();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.Picker, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                vista.setText(String.format("%04d", year) + "-"
                                                + String.format("%02d", month + 1) + "-"
                                                + String.format("%02d", dayOfMonth));
            }
        }, fechaHoy.get(Calendar.YEAR), fechaHoy.get(Calendar.MONTH), fechaHoy.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    //Desplega un TimePickerDialog para seleccionar la hora
    public void desplegarTimePicker(TextView vista) {
        // Código para desplegar TimePicker
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.Picker, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                vista.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00");
            }
        }, 0, 0, true);

        timePickerDialog.show();
    }

    //Establece el evento OnClick en el botón de cerrar X
    public void onClickCerrar(View view, Dialog ventanaDialogo) {
        ventanaDialogo.dismiss();
    }
}
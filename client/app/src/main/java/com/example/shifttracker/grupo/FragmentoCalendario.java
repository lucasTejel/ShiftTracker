package com.example.shifttracker.grupo;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shifttracker.R;
import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.PeriodoFestivo;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FragmentoCalendario extends Fragment {

    // Los parámetros de inicialización del fragmento
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private View vista;

    private Grupo grupo;
    private TextView textViewCalendario;
    private MaterialCalendarView materialCalendarView;
    private Button buttonNuevoFestivo;
    private Dialog ventanaDialogo;
    private ImageView imageViewBotonCerrar;
    private Button buttonAceptar;
    private TextView textViewInicio;
    private TextView textViewFechaInicio;
    private TextView textViewFin;
    private TextView textViewFechaFin;
    private boolean administrador;

    public FragmentoCalendario() {
        // Se requiere un constructor vacío
    }

    // Crea una nueva instancia del fragmento usando los parámetros proporcionados
    public static FragmentoCalendario newInstance(String param1, String param2) {
        FragmentoCalendario fragment = new FragmentoCalendario();
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
        vista = inflater.inflate(R.layout.fragment_calendario, container, false);

        establecerVistas();
        establecerInformacion();

        establecerOnClickListener();

        administrador = Controlador.extraerEsAdministrador(getActivity());
        if (!administrador){
            eliminarVistasUsuarioComun();
        }

        return vista;
    }

    //En caso de no ser administrador, elimina las vistas correspondientes.
    public void eliminarVistasUsuarioComun() {
        buttonNuevoFestivo.setVisibility(View.INVISIBLE);
    }

    //Establece las vistas del fragmento
    public void establecerVistas() {
        textViewCalendario = vista.findViewById(R.id.actPrincipalGrupoTextViewCalendario);
        materialCalendarView = vista.findViewById(R.id.winDialogCalendarioMaterialCalendarView);
        buttonNuevoFestivo = vista.findViewById(R.id.actPrincipalGrupoButtonNuevoFestivo);
    }

    //Establece la información en las vistas
    public void establecerInformacion() {
        PrincipalGrupo actividad = (PrincipalGrupo) getActivity();
        grupo = actividad.getGrupo();
        int idGrupo = grupo.getIdGrupo();

        List<PeriodoFestivo> listaPeriodosFestivos = Controlador.cargarCalendarioGrupo(actividad, idGrupo);

        //Establecer lista de días festivos entre inicio y fin de cada periodo
        LocalDate fechaActual;
        LocalDate fechaFin;
        List<LocalDate> listadoDiasFestivos = new ArrayList<>();
        for (PeriodoFestivo festivo : listaPeriodosFestivos) {
            fechaActual = festivo.getFechaInicio();
            fechaFin = festivo.getFechaFin();
            while (!fechaActual.isAfter(fechaFin)) {
                listadoDiasFestivos.add(fechaActual);
                fechaActual = fechaActual.plusDays(1);
            }
        }

        List<CalendarDay> listaDiasCalendarDay = generarListaCalendarDay(listadoDiasFestivos);
        pintarEnCalendario(listaDiasCalendarDay, R.drawable.fondo_redondo_azul);
    }

    //Establece el evento OnClick en el botón de nuevo festivo
    public void establecerOnClickListener() {
        buttonNuevoFestivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @LayoutRes int layoutResID = R.layout.win_dialog_nuevo_festivo;
                ventanaDialogo = new Dialog(getContext());
                abrirVentanaDialogo(layoutResID);
                establecerVistasNuevoFestivo();
            }
        });
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
                return listaDiasCalendarDay.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), color));
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

    //Establece las vistas del botón nuevo festivo
    public void establecerVistasNuevoFestivo() {
        imageViewBotonCerrar = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoImageViewCerrar);
        imageViewBotonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCerrar(view);
            }
        });

        textViewInicio = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoTextViewInicio);
        textViewFechaInicio = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoTextViewInicioFecha);
        textViewFechaInicio.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        textViewFin = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoTextViewFin);
        textViewFechaFin = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoTextViewFinFecha);
        textViewFechaFin.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        textViewInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(view);
            }
        });
        textViewFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(view);
            }
        });
        textViewFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(view);
            }
        });
        textViewFechaFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desplegarDatePicker(view);
            }
        });


        buttonAceptar = ventanaDialogo.findViewById(R.id.winDialogNuevoFestivoButtonAceptar);
        establecerOnClickListenerNuevoFestivo();
    }

    //Establece el evento OnClick en el botón aceptar de la ventana nuevo festivo
    public void establecerOnClickListenerNuevoFestivo() {
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDate fechaInicio;
                LocalDate fechaFin;
                fechaInicio = LocalDate.parse(textViewFechaInicio.getText().toString());
                fechaFin = LocalDate.parse(textViewFechaFin.getText().toString());

                int comparacion = fechaInicio.compareTo(fechaFin);
                if (comparacion <= 0) {
                    PeriodoFestivo periodoFestivo = new PeriodoFestivo(grupo.getIdGrupo(), fechaInicio, fechaFin);
                    if (Controlador.anadirFestivo(getActivity(), periodoFestivo)) {

                        //Establecer lista de días festivos entre inicio y fin del periodo
                        LocalDate fechaActual;
                        List<LocalDate> listadoDiasFestivos = new ArrayList<>();
                        fechaActual = periodoFestivo.getFechaInicio();
                        fechaFin = periodoFestivo.getFechaFin();
                        while (!fechaActual.isAfter(fechaFin)) {
                            listadoDiasFestivos.add(fechaActual);
                            fechaActual = fechaActual.plusDays(1);
                        }
                        List<CalendarDay> listaDiasCalendarDay = generarListaCalendarDay(listadoDiasFestivos);
                        pintarEnCalendario(listaDiasCalendarDay, R.drawable.fondo_redondo_azul);

                        ventanaDialogo.dismiss();
                    }
                }
                else {
                    Controlador.mostrarMensajeToast(getActivity(), getString(R.string.error_fecha_inicio_antes_que_fin));
                }
            }
        });
    }

    //Desplega un DatePickerDialog para seleccionar la fecha
    public void desplegarDatePicker(View vista) {
        // Código para desplegar DatePicker
        GregorianCalendar fechaHoy = new GregorianCalendar();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.Picker, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (vista == textViewInicio || vista == textViewFechaInicio) {
                    textViewFechaInicio.setText(String.format("%04d", year) + "-"
                            + String.format("%02d", month + 1) + "-"
                            + String.format("%02d", dayOfMonth));
                }
                else {
                    textViewFechaFin.setText(String.format("%04d", year) + "-"
                            + String.format("%02d", month + 1) + "-"
                            + String.format("%02d", dayOfMonth));
                }
            }
        }, fechaHoy.get(Calendar.YEAR), fechaHoy.get(Calendar.MONTH), fechaHoy.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    //Establece el evento OnClick en el botón de cerrar X
    public void onClickCerrar(View view) {
        ventanaDialogo.dismiss();
    }
}
package com.example.shifttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.shifttracker.conexion_servidor.Controlador;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioComun;

import java.io.IOException;

public class NfcAcceso extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private Grupo grupo;
    private ImageView imagen;
    private AnimationDrawable animationDrawable;

    private RadioGroup radioGroupVincular;
    private RadioButton radioButtonVincular;
    private RadioButton radioButtonDesvincular;

    private NfcAdapter nfcAdapter;
    private boolean administrador;

    //Se ejecuta al iniciar la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_acceso);

        comprobarNfc();
        // Verificar si el NFC está habilitado
        NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();
        solicitarActivarNfc();

        grupo = recogerGrupoBundle();
        administrador = Controlador.extraerEsAdministrador(this);

        establecerAnimacion();
        establecerRadioGroup();

        if (!administrador){
            eliminarVistasUsuarioComun();
        }
    }

    //Si el dispositivo no soporta NFC, muestra un mensaje indicandolo
    public void comprobarNfc() {
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            //El dispositivo no tiene NFC, maneja esta situación adecuadamente
            Controlador.mostrarMensajeToast(this, getString(R.string.no_tiene_nfc));
            finish();
        }
    }

    //El NFC no está habilitado, solicitar activación
    public void solicitarActivarNfc() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            //El NFC no está habilitado, solicitar activación
            Controlador.mostrarMensajeToast(this, getString(R.string.activar_nfc));
            Intent nfcSettings = new Intent(Settings.Panel.ACTION_NFC);
            startActivity(nfcSettings);
            finish();
        }
    }

    //En caso de no ser administrador, elimina las vistas correspondientes.
    public void eliminarVistasUsuarioComun() {
        radioGroupVincular.setVisibility(View.INVISIBLE);
    }

    //Recoge el objeto Grupo del bundle
    public Grupo recogerGrupoBundle() {
        Bundle datos = this.getIntent().getExtras();
        return (Grupo) datos.getSerializable("Grupo");
    }

    //Establece la animación de NFC
    public void establecerAnimacion() {
        imagen = findViewById(R.id.actNfcImageViewGif);
        imagen.setImageResource(R.drawable.anim_nfc);
        animationDrawable = (AnimationDrawable) imagen.getDrawable();
        animationDrawable.start();
    }

    //Establece las vistas del radioGroup y el evento OnCheckedChange
    public void establecerRadioGroup() {
        radioGroupVincular = findViewById(R.id.actNfcRadioGroupVincular);
        radioButtonVincular = findViewById(R.id.actNfcRadioButtonVincular);
        radioButtonDesvincular = findViewById(R.id.actNfcRadioButtonDesvincular);
        if (administrador) {
            radioGroupVincular.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.actNfcRadioButtonVincular) {
                        radioButtonVincular.setTextColor(getResources().getColor(R.color.white, null));
                        radioButtonVincular.setBackgroundColor(getResources().getColor(R.color.blue_app, null));
                        radioButtonDesvincular.setTextColor(getResources().getColor(R.color.blue_app, null));
                        radioButtonDesvincular.setBackground(getDrawable(R.drawable.bordes_azules));
                    } else {
                        radioButtonDesvincular.setTextColor(getResources().getColor(R.color.white, null));
                        radioButtonDesvincular.setBackgroundColor(getResources().getColor(R.color.blue_app, null));
                        radioButtonVincular.setTextColor(getResources().getColor(R.color.blue_app, null));
                        radioButtonVincular.setBackground(getDrawable(R.drawable.bordes_azules));
                    }
                }
            });
        }
        else {
            radioGroupVincular.setVisibility(View.GONE);
        }
    }


    //Este método se ejecuta en otro hilo cuando se descubre una etiqueta
    //No puede interactuar directamente con UI Thread.
    //Se utiliza el método `runOnUiThread` para cambiar la UI desde este método
    @Override
    public void onTagDiscovered(Tag tag) {
        //Reproduce sonido para indicar etiqueta detectada
        reproducirSonido();

        //Leer y o escribir en Tag aquí a la clase de tipo de Tecnología Tag apropiada.
        //En este ejemplo la etiqueta debe ser un Tipo de Tecnología Ndef
        Ndef ndef = Ndef.get(tag);

        //Comprueba que es una etiqueta compatible con Ndef
        if (ndef != null) {
            try {
                if (administrador) {
                    //Escribir en etiqueta
                    if (radioButtonVincular.isChecked()) {
                        //Vincular etiqueta al grupo
                        escribirEnEtiqueta(ndef, "id_grupo=" + grupo.getIdGrupo());
                    } else {
                        //Vaciar etiqueta
                        escribirEnEtiqueta(ndef, "");
                    }
                }
                else {
                    //Leer etiqueta
                    String contenidoEtiqueta = leerEtiqueta(ndef);
                    if (contenidoEtiqueta.endsWith(String.valueOf(grupo.getIdGrupo()))) {
                        //Extraer usuario al que se le añade la asistencia
                        Object[] idYNombreUsuario = Controlador.extraerIdYNombreUsuario(this);
                        int idUsuario = (Integer) idYNombreUsuario[0];
                        String nombreusuario = (String) idYNombreUsuario[1];
                        Usuario usuario = new UsuarioComun(idUsuario, nombreusuario);

                        if (Controlador.anadirAsistencia(this, usuario, grupo, null)) {
                            runOnUiThread(() -> {
                                imagen.setImageResource(R.drawable.anim_success);
                                animationDrawable.setOneShot(true);
                                ejecutarAnimacion();
                            });
                        }
                        else {
                            runOnUiThread(() -> {
                                imagen.setImageResource(R.drawable.anim_error);
                                ejecutarAnimacion();

                                Controlador.mostrarMensajeToast(this, getString(R.string.error));
                            });
                        }
                    }
                    else {
                        runOnUiThread(() -> {
                            imagen.setImageResource(R.drawable.anim_error);
                            ejecutarAnimacion();

                            Controlador.mostrarMensajeToast(this, getString(R.string.error_etiqueta_no_configurada));
                        });

                    }
                }

            }
            catch (FormatException e) {
                //Si el mensaje NDEF a escribir está mal formado
                runOnUiThread(() -> {
                    e.printStackTrace();
                    imagen.setImageResource(R.drawable.anim_error);
                    ejecutarAnimacion();

                    Controlador.mostrarMensajeToast(this, getString(R.string.error));
                });
            }
            catch (TagLostException e) {
                //La etiqueta queda fuera de alcance antes de finalizar el proceso
                runOnUiThread(() -> {
                    e.printStackTrace();
                    imagen.setImageResource(R.drawable.anim_error);
                    ejecutarAnimacion();

                    Controlador.mostrarMensajeToast(this, getString(R.string.error));
                });
            }
            catch (IOException e) {
                runOnUiThread(() -> {
                    e.printStackTrace();
                    imagen.setImageResource(R.drawable.anim_error);
                    ejecutarAnimacion();

                    Controlador.mostrarMensajeToast(this, getString(R.string.error));
                });
            }
            catch (Exception e) {
                runOnUiThread(() -> {
                    e.printStackTrace();
                    imagen.setImageResource(R.drawable.anim_error);
                    ejecutarAnimacion();

                    Controlador.mostrarMensajeToast(this, getString(R.string.error));
                });
            }
            finally {
                try {
                    //Desactiva las operaciones de E/S a la etiqueta desde este objeto TagTechnology y libera recursos.
                    ndef.close();
                }
                catch (IOException e) {
                    runOnUiThread(() -> {
                        e.printStackTrace();

                        imagen.setImageResource(R.drawable.anim_error);
                        ejecutarAnimacion();
                    });
                }
            }

        }
    }

    //Reproduce un sonido al acercar la etiqueta NFC
    public void reproducirSonido() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                notification);
        r.play();
    }

    //Ejecuta la animación de éxito o error
    public void ejecutarAnimacion() {
        animationDrawable = (AnimationDrawable) imagen.getDrawable();
        animationDrawable.start();
    }

    //Lee la etiqueta
    //Como no activamos el NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
    //Podemos obtener el mensaje Ndef en caché que el sistema lee por nosotros.
    public String leerEtiqueta(Ndef ndef) {
        String texto;

        NdefMessage mNdefMessage = ndef.getCachedNdefMessage();
        texto = new String(mNdefMessage.getRecords()[0].getPayload());
        return texto;
    }

    //Escribe el mensaje pasado por parámetro en la etiqueta
    public boolean escribirEnEtiqueta(Ndef ndef, String mensaje) throws FormatException, IOException {
        //Si queremos escribir un mensaje Ndef
        //Crear un registro Ndef
        NdefRecord ndefRecord = NdefRecord.createTextRecord("en", mensaje);

        //Añadir a un NdefMessage
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
        ndef.connect();
        ndef.writeNdefMessage(ndefMessage);

        //Éxito si llega hasta aquí
        runOnUiThread(() -> {
            imagen.setImageResource(R.drawable.anim_success);
            animationDrawable.setOneShot(true);
            ejecutarAnimacion();
        });

        return true;
    }

    //Se llama cuando la actividad se vuelve visible para el usuario y se coloca en primer plano.
    //Se debe iniciar cualquier recurso o servicio que deba estar activo mientras la actividad esté en primer plano.
    //En este caso, se activa el ReaderModej para varias teconologías NFC.
    @Override
    protected void onResume() {
        super.onResume();

        if(nfcAdapter!= null) {
            Bundle options = new Bundle();
            //Solución para algunas implementaciones de firmware Nfc defectuosas que sondean la tarjeta demasiado rápido.
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            //Activar ReaderMode para todos los tipos de tarjeta y desactivar los sonidos de la plataforma
            nfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    //Se llama cuando la actividad está a punto de pasar al segundo plano y pierde el foco del usuario.
    //Se deben realizar operaciones que deben ser guardadas o suspendidas, como guardar los cambios
    //de datos en una base de datos o liberar recursos que no son necesarios mientras la actividad no está visible.
    //En este caso, se desactiva el ReaderMode.
    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdapter!= null)
            nfcAdapter.disableReaderMode(this);
    }

}
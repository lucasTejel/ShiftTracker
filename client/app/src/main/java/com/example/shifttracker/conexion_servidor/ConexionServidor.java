package com.example.shifttracker.conexion_servidor;

import android.app.Activity;

import com.example.shifttracker.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConexionServidor {

    private static String servidor = "192.168.232.117";
    private static int puerto = 6126;

    private static Socket socket;
    private static ObjectInputStream flujoEntradaObjeto;
    private static ObjectOutputStream flujoSalidaObjeto;

    private static Activity actividadProcedencia;

    //Realiza una conexión con un socket en segundo plano y
    //establece los flujos de entrada y salida del socket.
    public static Callable<Void> conectar = new Callable<Void>() {
        public Void call() {

            //Realizamos la conexión con el socket en segundo plano
            try {
                socket = new Socket(servidor, puerto);

                flujoEntradaObjeto = new ObjectInputStream(socket.getInputStream());
                flujoSalidaObjeto = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Controlador.mostrarMensajeToast(actividadProcedencia, actividadProcedencia.getString(R.string.error_conexion));
            }
            return null;
        }
    };

    //Cierra los flujos de salida y entrada del objeto, y luego cierra el socket.
    public static void desconectar() {
        try {
            flujoSalidaObjeto.close();
            flujoEntradaObjeto.close();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Recibe un objeto de tipo EnvioSocket desde el servidor.
    //Se establece un tiempo de espera de 10 segundos para la recepción del objeto.
    public static Callable<EnvioSocket> recibirDeServidor = new Callable<EnvioSocket>() {
        public EnvioSocket call() {
            EnvioSocket reciboSocket = null;

            try {
                socket.setSoTimeout(10000);
                reciboSocket = (EnvioSocket) flujoEntradaObjeto.readObject();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return reciboSocket;
        }
    };

    private static EnvioSocket envioSocket = null;
    //Envía un objeto EnvioSocket al servidor a través del flujo de salida del objeto.
    public static Callable<Void> enviarAServidor = new Callable<Void>() {
        public Void call() {
            try {
                flujoSalidaObjeto.writeObject(envioSocket);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    };

    public static void setActividadProcedencia(Activity actividadProcedencia) {
        ConexionServidor.actividadProcedencia = actividadProcedencia;
    }

    public static void setEnvioSocket(EnvioSocket envioSocket) {
        ConexionServidor.envioSocket = envioSocket;
    }
}

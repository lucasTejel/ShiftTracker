package com.example.shifttracker.conexion_servidor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shifttracker.R;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Notificacion;
import com.example.shifttracker.pojo.PeriodoFestivo;
import com.example.shifttracker.pojo.PeriodoVacacional;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioAdministrador;
import com.example.shifttracker.pojo.UsuarioComun;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controlador {

    static ExecutorService executor = Executors.newSingleThreadExecutor();

    //Ejecuta el método "conectar" en segundo plano utilizando un executor y espera a que se complete.
    //En caso de excepciones, muestra un mensaje de error.
    public static void conectarAServidor(Activity actividad) {
        try {
            ConexionServidor.setActividadProcedencia(actividad);
            Future<Void> resultado = executor.submit(ConexionServidor.conectar);
            resultado.get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            Controlador.mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            Controlador.mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            Controlador.mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
    }

    //Valida los datos de registro de un usuario (nombre de usuario, email y teléfono).
    //Si los datos son correctos, enviarlos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un valor booleano indicando si los datos de registro son correctos o no.
    public static boolean registrarse(Activity actividad, String nombreUsuario, String email,
                                   String telefono, String contrasena, boolean administrador) {
        boolean datosCorrectos = true;

        //Comprobar nombre de usuario bien formado
        nombreUsuario = nombreUsuario.replaceAll("\\s+", " ").trim();
        if (! nombreUsuarioBienFormado(nombreUsuario)) {
            datosCorrectos = false;
            mostrarMensajeToast(actividad, actividad.getString(R.string.error_nombre_usuario));
        }

        //Comprobar email bien formado
        if (! emailBienFormado(email)) {
            datosCorrectos = false;
            mostrarMensajeToast(actividad, actividad.getString(R.string.error_email));
        }

        //Comprobar telefono bien formado
        if (! telefonoBienFormado(telefono)) {
            datosCorrectos = false;
            mostrarMensajeToast(actividad, actividad.getString(R.string.error_telefono));
        }


        if (datosCorrectos) {

            try {
                //Crear usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;

                if (administrador) {
                    UsuarioAdministrador usuarioAdministrador = new UsuarioAdministrador(nombreUsuario, email, telefono, contrasena);
                    objetosAdjuntos.put("usuario", usuarioAdministrador);
                    envioSocket = new EnvioSocket("/registrarse", objetosAdjuntos);
                }
                else {
                    UsuarioComun usuarioComun = new UsuarioComun(nombreUsuario, email, telefono, contrasena);
                    objetosAdjuntos.put("usuario", usuarioComun);
                    envioSocket = new EnvioSocket("/registrarse", objetosAdjuntos);
                }

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();


                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);
                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    datosCorrectos = false;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("contrasenaInvalida")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error_contrasena));
                    }
                    else if (mensajeError.equals("yaExisteUsuario")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error_ya_existe_usuario));
                    }
                    else if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                }
                else if (reciboSocket.getTipo().equals("/exito")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_registro));
                }

            }
            catch (ExecutionException e) {
                e.printStackTrace();
                mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            }
            catch (Exception e) {
                e.printStackTrace();
                mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            }

        }

        return datosCorrectos;
    }

    //Inicia sesión de un usuario utilizando un email y contraseña. Crea un objeto de tipo Usuario con los datos.
    //Si los datos son correctos, enviarlos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve el Usuario con todos sus datos.
    public static Usuario iniciarSesion(Activity actividad, String email, String contrasena) {
        Usuario usuario = null;

        try {
            //Crear usuario y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;

            Usuario usuarioComun = new UsuarioComun(email, contrasena);
            objetosAdjuntos.put("usuario", usuarioComun);
            envioSocket = new EnvioSocket("/iniciarSesion", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();


            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);
            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("datosIncorrectos")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_datos_incorrectos));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                usuario = (Usuario) reciboSocket.getObjetosAdjuntos().get("usuario");
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return usuario;
    }

    //Obtiene el id_usuario que tiene la sesión iniciada y lo envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve el mapa con el usuario y la lista de grupos del usuario.
    public static Map<String, Object> cargarPrincipalAplicacion(Activity actividad) {
        int idUsuario = extraerIdUsuario(actividad);
        Usuario usuario;
        Map<String, Object> objetosAdjuntosRecibidos = null;

        try {
            //Crear usuario y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            usuario = new UsuarioComun(idUsuario);
            objetosAdjuntos.put("usuario", usuario);

            envioSocket = new EnvioSocket("/cargarPrincipalAplicacion", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);
            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("noExisteUsuario")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_no_existe_usuario));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                objetosAdjuntosRecibidos = reciboSocket.getObjetosAdjuntos();
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return objetosAdjuntosRecibidos;
    }

    //Recibe como parámetro el id_grupo y lo envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve la lista de miembros de ese grupo.
    public static List<Usuario> cargarListaMiembrosGrupo(Activity actividad, int idGrupo) {
        List<Usuario> listaMiembrosConsultada = null;

        try {
            //Crear grupo y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            Grupo grupo = new Grupo(idGrupo);
            objetosAdjuntos.put("grupo", grupo);

            envioSocket = new EnvioSocket("/cargarListaMiembrosGrupo", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("noExisteGrupo")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_no_existe_grupo));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                objetosAdjuntos = reciboSocket.getObjetosAdjuntos();
                listaMiembrosConsultada = (List<Usuario>) objetosAdjuntos.get("listaMiembros");
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return listaMiembrosConsultada;
    }

    //Recibe como parámetro el id_grupo y lo envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve la lista de periodos festivos de ese grupo para poder construir el calendario.
    public static List<PeriodoFestivo> cargarCalendarioGrupo(Activity actividad, int idGrupo) {
        List<PeriodoFestivo> listaPeriodosFestivos = null;

        try {
            //Crear grupo y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            Grupo grupo = new Grupo(idGrupo);
            objetosAdjuntos.put("grupo", grupo);

            envioSocket = new EnvioSocket("/cargarCalendarioGrupo", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("noExistenFestivos")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_no_existe_festivo));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                objetosAdjuntos = reciboSocket.getObjetosAdjuntos();
                listaPeriodosFestivos = (List<PeriodoFestivo>) objetosAdjuntos.get("listaPeriodosFestivos");
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return listaPeriodosFestivos;
    }

    //Obtiene el id_usuario que tiene la sesión iniciada y lo envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve la lista de notificaciones de ese usuario.
    public static List<Notificacion> cargarNotificaciones(Activity actividad) {
        int idUsuario = extraerIdUsuario(actividad);
        Usuario usuario;
        Map<String, Object> objetosAdjuntosRecibidos;
        List<Notificacion> listaNotificaciones = null;

        try {
            //Crear usuario y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            usuario = new UsuarioComun(idUsuario);
            objetosAdjuntos.put("usuario", usuario);

            envioSocket = new EnvioSocket("/cargarNotificaciones", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);
            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("noExisteUsuario")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_no_existe_usuario));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                objetosAdjuntosRecibidos = reciboSocket.getObjetosAdjuntos();
                listaNotificaciones = (List<Notificacion>) objetosAdjuntosRecibidos.get("listaNotificaciones");
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return listaNotificaciones;
    }

    //Recibe por parámetro los objetos Usuario, Grupo y Timestamp que son los datos de la asistencia.
    //Si Timestamp es nulo, significa que la asistencia se ha realizado con NFC y se usará la fecha y hora actuales.
    //Envía al servidor los datos a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un booleano indicando si el proceso ha resultado exitoso no.
    public static boolean anadirAsistencia(Activity actividad, Usuario usuario, Grupo grupo, Timestamp fechaHoraInicio) {
        boolean exito = false;

        try {
            //Añadir grupo y usuario y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            objetosAdjuntos.put("grupo", grupo);
            objetosAdjuntos.put("usuario", usuario);
            objetosAdjuntos.put("fechaHoraInicio", fechaHoraInicio);

            envioSocket = new EnvioSocket("/anadirAsistencia", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                exito = true;
                mostrarMensajeToast(actividad, actividad.getString(R.string.exito_asistencia_anadida));
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return exito;
    }

    //Recibe por parámetro el objeto Usuario a editar. Lo valida y envía al servidor los datos a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un String con el nuevo nombre de usuario.
    public static String editarNombreUsuario(Activity actividad, Usuario usuario) {
        boolean datosCorrectos = true;
        String nombreUsuario;

        try {
            //Comprobar nombre de usuario bien formado
            nombreUsuario = usuario.getNombreUsuario();
            nombreUsuario = nombreUsuario.replaceAll("\\s+", " ").trim();
            if (! nombreUsuarioBienFormado(nombreUsuario)) {
                datosCorrectos = false;
                nombreUsuario = null;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_nombre_usuario));
            }

            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                usuario.setNombreUsuario(nombreUsuario);
                objetosAdjuntos.put("usuario", usuario);

                envioSocket = new EnvioSocket("/editarNombreUsuario", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    nombreUsuario = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("nombreUsuarioYaExiste")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error_nombre_usuario_ya_existe));
                    }
                    else if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    nombreUsuario = usuario.getNombreUsuario();
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_nombre_usuario_editado));
                }
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreUsuario = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreUsuario = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreUsuario = null;
        }

        return nombreUsuario;
    }

    //Recibe por parámetro el objeto UsuarioAdministrador a editar. Lo valida y envía al servidor los datos a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un String con el nuevo cargo de usuario.
    public static String editarCargo(Activity actividad, UsuarioAdministrador usuario) {
        boolean datosCorrectos = true;
        String cargo;

        try {
            //Comprobar cargo bien formado
            cargo = usuario.getCargo();
            cargo = cargo.replaceAll("\\s+", " ").trim();
            if (! cargoDescripcionBienFormado(cargo)) {
                datosCorrectos = false;
                cargo = null;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_cargo));
            }

            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                usuario.setCargo(cargo);
                objetosAdjuntos.put("usuario", usuario);

                envioSocket = new EnvioSocket("/editarCargo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    cargo = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    cargo = usuario.getCargo();
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_cargo_editado));
                }
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            cargo = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            cargo = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            cargo = null;
        }

        return cargo;
    }

    //Recibe por parámetro el objeto Grupo a editar. Lo valida y envía al servidor los datos a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un String con el nuevo nombre de grupo.
    public static String editarNombreGrupo(Activity actividad, Grupo grupo) {
        boolean datosCorrectos = true;
        String nombreGrupo;

        try {
            //Comprobar nombre de grupo bien formado
            nombreGrupo = grupo.getNombre();
            nombreGrupo = nombreGrupo.replaceAll("\\s+", " ").trim();
            if (! nombreGrupoBienFormado(nombreGrupo)) {
                datosCorrectos = false;
                nombreGrupo = null;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_nombre_grupo));
            }

            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                grupo.setNombre(nombreGrupo);
                objetosAdjuntos.put("grupo", grupo);

                envioSocket = new EnvioSocket("/editarNombreGrupo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    nombreGrupo = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    nombreGrupo = grupo.getNombre();
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_nombre_grupo_editado));
                }
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreGrupo = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreGrupo = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            nombreGrupo = null;
        }

        return nombreGrupo;
    }

    //Recibe por parámetro el objeto Grupo a editar. La valida y envía al servidor los datos a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un String con la nueva descripción de grupo.
    public static String editarDescripcionGrupo(Activity actividad, Grupo grupo) {
        boolean datosCorrectos = true;
        String descripcionGrupo;

        try {
            //Comprobar descripcion de grupo bien formada
            descripcionGrupo = grupo.getDescripcion();
            descripcionGrupo = descripcionGrupo.replaceAll("\\s+", " ").trim();
            if (! cargoDescripcionBienFormado(descripcionGrupo)) {
                datosCorrectos = false;
                descripcionGrupo = null;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_descripcion));
            }


            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                grupo.setDescripcion(descripcionGrupo);
                objetosAdjuntos.put("grupo", grupo);

                envioSocket = new EnvioSocket("/editarDescripcionGrupo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    descripcionGrupo = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    descripcionGrupo = grupo.getDescripcion();
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_descripcion_grupo_editado));
                }
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            descripcionGrupo = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            descripcionGrupo = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            descripcionGrupo = null;
        }

        return descripcionGrupo;
    }

    //Recibe por parámetro el objeto Grupo a añadir. Valida sus datos y los envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un objeto Grupo con el nuevo grupo añadido.
    public static Grupo anadirGrupo(Activity actividad, Grupo grupo) {
        boolean datosCorrectos = true;
        Grupo grupoRecibido = null;

        try {
            //Comprobar nombre de grupo bien formado
            String nombreGrupo = grupo.getNombre();
            nombreGrupo = nombreGrupo.replaceAll("\\s+", " ").trim();
            if (! nombreGrupoBienFormado(nombreGrupo)) {
                datosCorrectos = false;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_nombre_grupo));
            }

            //Comprobar descripcion de grupo bien formada
            String descripcionGrupo = grupo.getDescripcion();
            descripcionGrupo = descripcionGrupo.replaceAll("\\s+", " ").trim();
            if (! cargoDescripcionBienFormado(descripcionGrupo)) {
                datosCorrectos = false;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_descripcion));
            }


            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                grupo.setNombre(nombreGrupo);
                grupo.setDescripcion(descripcionGrupo);
                objetosAdjuntos.put("grupo", grupo);

                envioSocket = new EnvioSocket("/anadirGrupo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    grupoRecibido = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    grupoRecibido = (Grupo) reciboSocket.getObjetosAdjuntos().get("grupo");
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_grupo_creado));
                }

            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            grupoRecibido = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            grupoRecibido = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            grupoRecibido = null;
        }

        return grupoRecibido;
    }

    //Recibe por parámetro los objetos Usuario a añadir y Grupo al que añadir. Valida el email y
    //envía los datos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un objeto Usuario con el nuevo miembro añadido.
    public static Usuario anadirMiembroGrupo(Activity actividad, Usuario usuario, Grupo grupo) {
        boolean datosCorrectos = true;
        Usuario usuarioRecibido = null;

        try {
            //Comprobar email bien formado
            if (! emailBienFormado(usuario.getEmail())) {
                datosCorrectos = false;
                mostrarMensajeToast(actividad, actividad.getString(R.string.error_email));
            }

            if (datosCorrectos) {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                objetosAdjuntos.put("grupo", grupo);
                objetosAdjuntos.put("usuario", usuario);

                envioSocket = new EnvioSocket("/anadirMiembroGrupo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    usuarioRecibido = null;
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    switch (mensajeError) {
                        case "noExisteUsuario":
                            mostrarMensajeToast(actividad, actividad.getString(R.string.error_no_existe_usuario));
                            break;
                        case "esUsuarioAdministrador":
                            mostrarMensajeToast(actividad, actividad.getString(R.string.error_usuario_administrador));
                            break;
                        case "yaExisteUsuarioEnGrupo":
                            mostrarMensajeToast(actividad, actividad.getString(R.string.error_ya_existe_usuario_en_grupo));
                            break;
                        case "error":
                            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                            break;
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    usuarioRecibido = (Usuario) reciboSocket.getObjetosAdjuntos().get("usuario");
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_miembro_anadido));
                }

            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            usuarioRecibido = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            usuarioRecibido = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
            usuarioRecibido = null;
        }

        return usuarioRecibido;
    }

    //Recibe por parámetro el objeto Grupo a eliminar. Envía los datos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un booleando que indica si el proceso se ha completado correctamente.
    public static boolean eliminarGrupo(Activity actividad, Grupo grupo) {
        boolean exito = false;

        try {
                //Añadir usuario y enviar por socket
                Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
                EnvioSocket envioSocket;
                objetosAdjuntos.put("grupo", grupo);

                envioSocket = new EnvioSocket("/eliminarGrupo", objetosAdjuntos);

                ConexionServidor.setEnvioSocket(envioSocket);
                Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
                resultadoEnvio.get();

                //Recibir respuesta del servidor y hacer lo necesario
                Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

                EnvioSocket reciboSocket = resultado.get();

                if (reciboSocket.getTipo().equals("/error")) {
                    String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                    if (mensajeError.equals("error")) {
                        mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                    }
                } else if (reciboSocket.getTipo().equals("/exito")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.exito_grupo_eliminado));
                    exito = true;
                }
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return exito;
    }

    //Recibe por parámetro los objetos Usuario a eliminar y Grupo del que eliminar. Envía los datos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un booleando que indica si el proceso se ha completado correctamente.
    public static boolean eliminarMiembroGrupo(Activity actividad, Usuario usuario, Grupo grupo) {
        boolean exito = false;

        try {
            //Añadir usuario y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            objetosAdjuntos.put("usuario", usuario);
            objetosAdjuntos.put("grupo", grupo);

            envioSocket = new EnvioSocket("/eliminarMiembroGrupo", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            } else if (reciboSocket.getTipo().equals("/exito")) {
                mostrarMensajeToast(actividad, actividad.getString(R.string.exito_miembro_eliminado));
                exito = true;
            }
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return exito;
    }

    //Recibe por parámetro el objeto PeriodoVacacional con los datos a añadir. Envía los datos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un booleando que indica si el proceso se ha completado correctamente.
    public static boolean anadirVacaciones(Activity actividad, PeriodoVacacional periodoVacacional) {
        boolean exito = false;

        try {
            //Añadir periodo vacacional y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            objetosAdjuntos.put("periodoVacacional", periodoVacacional);

            envioSocket = new EnvioSocket("/anadirVacaciones", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("periodoVacacionalSolapado")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_periodo_solapado));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                exito = true;
                mostrarMensajeToast(actividad, actividad.getString(R.string.exito_vacaciones_anadidas));
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return exito;
    }

    //Recibe como parámetro los objetos Usuario y Grupo a consultar y lo envía al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un mapa con las listas de asistencias, periodos festivos y periodos vacacionales
    //de ese grupo y miembro para poder construir el calendario.
    public static Map<String, Object> cargarCalendarioMiembro(Activity actividad, Usuario usuario, Grupo grupo) {
        Map<String, Object> mapaPeriodos = null;

        try {
            //Añadir usuario y grupo y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            objetosAdjuntos.put("usuario", usuario);
            objetosAdjuntos.put("grupo", grupo);

            envioSocket = new EnvioSocket("/cargarCalendarioMiembro", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("errorCargarCalendario")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_cargar_calendario));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                objetosAdjuntos = reciboSocket.getObjetosAdjuntos();
                mapaPeriodos = (Map<String, Object>) objetosAdjuntos.get("mapaPeriodos");
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return mapaPeriodos;
    }

    //Recibe por parámetro el objeto PeriodoFestivo con los datos a añadir. Envía los datos al servidor a través de un socket.
    //Luego, espera recibir una respuesta del servidor y si este devuelve errores, se muestra el mensaje de error.
    //Si ocurren excepciones durante el proceso, tambien muestra el mensaje de error.
    //Finalmente, el método devuelve un booleando que indica si el proceso se ha completado correctamente.
    public static boolean anadirFestivo(Activity actividad, PeriodoFestivo periodoFestivo) {
        boolean exito = false;

        try {
            //Añadir periodo festivo y enviar por socket
            Map<String, Object> objetosAdjuntos = new LinkedHashMap<String, Object>();
            EnvioSocket envioSocket;
            objetosAdjuntos.put("periodoFestivo", periodoFestivo);

            envioSocket = new EnvioSocket("/anadirFestivo", objetosAdjuntos);

            ConexionServidor.setEnvioSocket(envioSocket);
            Future<Void> resultadoEnvio = executor.submit(ConexionServidor.enviarAServidor);
            resultadoEnvio.get();

            //Recibir respuesta del servidor y hacer lo necesario
            Future<EnvioSocket> resultado = executor.submit(ConexionServidor.recibirDeServidor);

            EnvioSocket reciboSocket = resultado.get();

            if (reciboSocket.getTipo().equals("/error")) {
                String mensajeError = (String) reciboSocket.getObjetosAdjuntos().get("mensajeError");

                if (mensajeError.equals("periodoFestivoSolapado")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error_periodo_solapado));
                }
                else if (mensajeError.equals("error")) {
                    mostrarMensajeToast(actividad, actividad.getString(R.string.error));
                }
            }
            else if (reciboSocket.getTipo().equals("/exito")) {
                exito = true;
                mostrarMensajeToast(actividad, actividad.getString(R.string.exito_festivo_anadido));
            }

        }
        catch (ExecutionException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }
        catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeToast(actividad, actividad.getString(R.string.error));
        }

        return exito;
    }

    //Verifica si un nombre de usuario tiene una longitud válida, entre 3 y 16 carácteres.
    //Devuelve true o false si cumple o no con esta condición.
    public static boolean nombreUsuarioBienFormado(String nombreUsuario) {
        return nombreUsuario.length() >= 3 && nombreUsuario.length() <= 16;
    }

    //Comprueba si el correo electrónico tiene un formato válido.
    //Devuelve true o false si cumple o no con esta condición.
    public static boolean emailBienFormado(String email) {
        String expRegularEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(expRegularEmail);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    //Comprueba si el telefono tiene un formato válido.
    //Devuelve true o false si cumple o no con esta condición.
    public static boolean telefonoBienFormado(String telefono) {
        String expRegularEmail = "^(?:(?:\\+|00)34[-\\s]?)?[6-9][\\d]{8}$";
        Pattern pattern = Pattern.compile(expRegularEmail);
        Matcher matcher = pattern.matcher(telefono);

        return matcher.matches();
    }

    //Verifica si la descripcion tiene una longitud válida, entre 1 y 24 carácteres.
    //Devuelve true o false si cumple o no con esta condición.
    public static boolean cargoDescripcionBienFormado(String cargoDescripcion) {
        return cargoDescripcion.length() >= 1 && cargoDescripcion.length() <= 24;
    }

    //Verifica si un nombre de grupo tiene una longitud válida, entre 3 y 16 carácteres.
    //Devuelve true o false si cumple o no con esta condición.
    public static boolean nombreGrupoBienFormado(String nombreGrupo) {
        return nombreGrupo.length() >= 3 && nombreGrupo.length() <= 16;
    }

    //Guarda los datos de un usuario en las preferencias compartidas (SharedPreferences).
    public static void guardarDatosUsuario(Activity actividad, Usuario usuario) {
        SharedPreferences sharedPreferences = actividad.getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id_usuario", usuario.getIdUsuario());
        editor.putString("nombre", usuario.getNombreUsuario());
        editor.putString("email", usuario.getEmail());
        editor.putString("telefono", usuario.getTelefono());
        editor.putString("contrasena", usuario.getContrasena());
        if (usuario instanceof UsuarioAdministrador) {
            editor.putBoolean("administrador", true);
        }
        else {
            editor.putBoolean("administrador", false);
        }
        editor.apply();
    }

    //Elimina los datos de un usuario de las preferencias compartidas (SharedPreferences).
    public static void eliminarDatosUsuario(Activity actividad) {
        SharedPreferences sharedPreferences = actividad.getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //Extrae el id_usuario de las preferencias compartidas (SharedPreferences).
    public static int extraerIdUsuario(Activity activity) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(activity.getApplicationContext()).getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("id_usuario",0);
    }

    //Extrae el id_usuario y nombre de las preferencias compartidas (SharedPreferences).
    public static Object[] extraerIdYNombreUsuario(Activity activity) {
        Object[] datos = new Object[2];
        SharedPreferences sharedPreferences = Objects.requireNonNull(activity.getApplicationContext()).getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        datos[0] = (Integer) sharedPreferences.getInt("id_usuario",0);
        datos[1] = (String) sharedPreferences.getString("nombre","");
        return datos;
    }

    //Extrae el booleano administrador de las preferencias compartidas (SharedPreferences).
    public static boolean extraerEsAdministrador(Activity activity) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(activity.getApplicationContext()).getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("administrador",false);
    }

    //Extrae el email y contrasena de las preferencias compartidas (SharedPreferences).
    public static String[] extraerCredencialesUsuario(Activity activity) {
        String[] credenciales = new String[2];
        SharedPreferences sharedPreferences = Objects.requireNonNull(activity.getApplicationContext()).getSharedPreferences("datosUsuario", Context.MODE_PRIVATE);
        credenciales[0] = sharedPreferences.getString("email","");
        credenciales[1] = sharedPreferences.getString("contrasena","");

        return credenciales;
    }

    //Muestra un mensaje personalizado en forma de Toast en una actividad
    //Crea un Toast personalizado utilizando un diseño definido en el archivo "toast_personalizado.xml".
    //El mensaje pasado como parámetro se muestra en el TextView dentro del Toast.
    //El Toast se muestra en la esquina superior derecha de la pantalla.
    public static void mostrarMensajeToast(Activity actividad, String mensaje) {
        actividad.runOnUiThread(() -> {
            LayoutInflater inflater = actividad.getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_personalizado, actividad.findViewById(R.id.toastPersonalizadoLayout));

            Toast toast = new Toast(actividad);
            toast.setGravity(Gravity.RIGHT | Gravity.TOP, 0, 200);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            TextView mensajeToast = layout.findViewById(R.id.toastPersonalizadoTextViewMensaje);
            mensajeToast.setText(mensaje);
            toast.show();
        });
    }

}

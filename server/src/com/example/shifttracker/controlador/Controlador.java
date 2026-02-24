package com.example.shifttracker.controlador;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.shifttracker.conexion_servidor.EnvioSocket;
import com.example.shifttracker.dao.Dao;
import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Notificacion;
import com.example.shifttracker.pojo.PeriodoFestivo;
import com.example.shifttracker.pojo.PeriodoVacacional;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioAdministrador;
import com.example.shifttracker.pojo.UsuarioComun;

public class Controlador {
	
	//Registra un usuario en la base de datos.
	//Verifica si la contraseña es válida y comprueba si los datos del usuario ya existen en la base de datos.
	//Si todo es correcto, se consulta el máximo id_usuario para asignar el nuevo id_usuario y se inserta el usuario en las tablas.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket registrarse(Map <String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		List<Usuario> listaUsuarios = new ArrayList<Usuario>();
		
		Dao dao = new Dao();
		try {
			
			//Comprobar si la contraseña es válida
			if (esContrasenaValida(usuario.getContrasena())) {
				
				//Comprobar que los datos no existan en la base de datos
				listaUsuarios = dao.consultarUsuarioPorDatos(usuario);
				if (listaUsuarios.size() != 0) {
					objetosAdjuntosRetorno.put("mensajeError", "yaExisteUsuario");
					envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
				}
				//Si todo es correcto, se consulta el máximo id para asignar el nuevo id
				//Despues, se inserta el usuario en la tabla usuario y en usuarios_comunes o usuarios_administradores
				else {
					int nuevoIdUsuario = dao.obtenerMaxIdUsuario() + 1;
					usuario.setIdUsuario(nuevoIdUsuario);
					dao.insertarUsuario(usuario);
					envioSocket = new EnvioSocket("/exito", null);
				}
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "contrasenaInvalida");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Verifica si una contraseña es válida.
	//Recibe una contraseña como parámetro y la compara con una expresión regular que requiere
	//al menos 8 caracteres, incluyendo al menos una letra y un dígito, sin espacios en blanco.
	//Devuelve un valor booleano que indica si la contraseña cumple con los criterios establecidos.
	public boolean esContrasenaValida(String contrasena) {
		String expresionRegular = "^(?=.*\\d)(?=.*[a-zA-Z])(?!.*\\s).{8,}$";
		
		Pattern pattern = Pattern.compile(expresionRegular);
        Matcher matcher = pattern.matcher(contrasena);
        return matcher.matches();
	}
	
	//Realiza el inicio de sesión de un usuario. Comprueba si existe un usuario con el email y contraseña proporcionados
	//en la base de datos. Si el usuario existe, se devuelve la información del usuario consultado.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket iniciarSesion(Map <String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Usuario usuarioConsultado;
		
		Dao dao = new Dao();
		try {
			
			//Comprobar si existe el usuario
			usuarioConsultado = dao.consultarUsuarioPorEmailYContrasena(usuario);
			if (usuarioConsultado != null) {
				objetosAdjuntosRetorno.put("usuario", usuarioConsultado);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "datosIncorrectos");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Consulta los datos del usuario y una lista de los grupos asociados, y devuelve un objeto con esta información.
	//Si el usuario no existe, se devuelve un mensaje de error.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket cargarPrincipalAplicacion(Map <String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Usuario usuarioConsultado;
		List<Grupo> listaGrupos;
		
		Dao dao = new Dao();
		try {
			
			//Consultar los datos del usuario
			usuarioConsultado = dao.consultarUsuarioPorId(usuario);
			
			//Consultar los grupos del usuario consultado
			listaGrupos = dao.consultarGruposPorUsuario(usuarioConsultado);
			
			
			if (usuarioConsultado != null) {
				objetosAdjuntosRetorno.put("usuario", usuarioConsultado);
				objetosAdjuntosRetorno.put("listaGrupos", listaGrupos);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "noExisteUsuario");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Consulta los miembros de un grupo y devuelve un objeto con la lista de usuarios.
	//Si el grupo no existe, se devuelve un mensaje de error.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket cargarListaMiembrosGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		List<Usuario> listaUsuarios;
		
		Dao dao = new Dao();
		try {
			
			//Consultar los miembros
			listaUsuarios = dao.consultarUsuariosPorGrupo(grupo);
			
			if (listaUsuarios != null) {
				objetosAdjuntosRetorno.put("listaMiembros", listaUsuarios);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "noExisteGrupo");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Consulta los periodos festivos de un grupo y devuelve un objeto con la lista de periodos festivos.
	//Si no existen periodos festivos, se devuelve un mensaje de error.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket cargarCalendarioGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		List<PeriodoFestivo> listaPeriodosFestivos;
		
		Dao dao = new Dao();
		try {
			
			//Consultar los miembros
			listaPeriodosFestivos = dao.consultarPeriodosFestivosPorGrupo(grupo);
			
			if (listaPeriodosFestivos != null) {
				objetosAdjuntosRetorno.put("listaPeriodosFestivos", listaPeriodosFestivos);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "noExistenFestivos");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Consulta las notificaciones de un usuario y devuelve un objeto con la lista de notificaciones.
	//Si no existen notificaciones para el usuario, se devuelve un mensaje de error.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket cargarNotificaciones(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		List<Notificacion> listaNotificaciones;
		
		Dao dao = new Dao();
		try {
			
			//Consultar los miembros
			listaNotificaciones = dao.consultarNotificaciones(usuario);
			
			if (listaNotificaciones != null) {
				objetosAdjuntosRetorno.put("listaNotificaciones", listaNotificaciones);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "noExisteUsuario");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Registra una asistencia para un usuario en un grupo específico,
	//utilizando la fecha y hora de inicio proporcionadas.
	//También actualiza el estado del usuario a "false".
	//Devuelve un objeto con la respuesta de éxito o un mensaje de error en caso de excepciones.
	public EnvioSocket anadirAsistencia(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		
		Object objeto = objetosAdjuntos.get("fechaHoraInicio");
		Timestamp fechaHoraInicio = null;
		if (objeto != null && objeto instanceof Timestamp) {
		    fechaHoraInicio = (Timestamp) objeto;
		}
		
		Dao dao = new Dao();
		try {
			dao.insertarAsistencia(usuario, grupo, fechaHoraInicio);
			
			UsuarioComun usuarioComun = (UsuarioComun) usuario;
			dao.actualizarEstado(usuarioComun, false);
			envioSocket = new EnvioSocket("/exito", null);
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Permite cambiar el nombre de usuario de un usuario en la base de datos.
	//Verifica si el nuevo nombre de usuario ya existe en la base de datos.
	//Si no existe, actualiza el nombre de usuario y devuelve un objeto con la respuesta de éxito.
	//Si el nuevo nombre de usuario ya existe, devuelve un mensaje de error indicando
	//que el nombre de usuario ya está en uso. En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket editarNombreUsuario(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		List<Usuario> listaUsuarios;
		Dao dao = new Dao();
		
		try {
			usuario.setEmail("");
			usuario.setTelefono("");
			listaUsuarios = dao.consultarUsuarioPorDatos(usuario);
			if (listaUsuarios.size() <= 0) {
				dao.actualizarNombreUsuario(usuario);
				envioSocket = new EnvioSocket("/exito", null);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "nombreUsuarioYaExiste");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Permite actualizar el cargo de un usuario administrador en la base de datos.
	//Realiza la actualización del cargo y devuelve un objeto con la respuesta de éxito.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket editarCargo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		UsuarioAdministrador usuario = (UsuarioAdministrador) objetosAdjuntos.get("usuario");
		Dao dao = new Dao();
		
		try {
			dao.actualizarCargo(usuario);
			envioSocket = new EnvioSocket("/exito", null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Actualiza el nombre de un grupo en la base de datos.
	//Realiza la actualización del nombre del grupo y devuelve un objeto con la respuesta de éxito.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket editarNombreGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		Dao dao = new Dao();
		
		try {
			dao.actualizarNombreGrupo(grupo);
			envioSocket = new EnvioSocket("/exito", null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Actualiza la descripción de un grupo en la base de datos.
	//Realiza la actualización de la descripción del grupo y devuelve un objeto con la respuesta de éxito.
	//En caso de excepciones, devuelve un mensaje de error genérico.
	public EnvioSocket editarDescripcionGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		Dao dao = new Dao();
		
		try {
			dao.actualizarDescripcionGrupo(grupo);
			envioSocket = new EnvioSocket("/exito", null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Añade un nuevo grupo a la base de datos. Primero, se obtiene el máximo id_grupo + 1 para asignar el nuevo id_grupo.
	//Luego, se inserta el grupo en la base de datos y se crea un objeto grupo como adjunto para enviar como respuesta de éxito.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket anadirGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		
		Dao dao = new Dao();
		try {
			//Si todo es correcto, se consulta el máximo id para asignar el nuevo id
			//Despues, se inserta el grupo
			int nuevoIdGrupo = dao.obtenerMaxIdGrupo() + 1;
			grupo.setIdGrupo(nuevoIdGrupo);
			dao.insertarGrupo(grupo);
			
			objetosAdjuntosRetorno.put("grupo", grupo);
			envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Añade un nuevo miembro a un grupo existente. Se consultan los datos del usuario a través de
	//su nombre de usuario y teléfono, y si el usuario existe, se verifica si es un administrador
	//o si ya es miembro del grupo. En función de estos casos, se retorna un mensaje de error correspondiente.
	//Si el usuario cumple con los requisitos, se inserta como miembro del grupo y se devuelve un objeto
	//con el usuario consultado como adjunto en caso de éxito. En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket anadirMiembroGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		UsuarioComun usuarioComunConsultado = null;
		
		List<Usuario> listaUsuarios;
		Dao dao = new Dao();
		try {
			usuario.setNombreUsuario("");
			usuario.setTelefono("");
			listaUsuarios = dao.consultarUsuarioPorDatos(usuario);
			if (listaUsuarios.size() > 0) {
				Usuario usuarioConsultado = listaUsuarios.get(0);
				if (dao.obtenerEsUsuarioAdministrador(usuarioConsultado)) {
					objetosAdjuntosRetorno.put("mensajeError", "esUsuarioAdministrador");
					envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
				}
				else {
					if (dao.obtenerExisteMiembroEnGrupo(usuarioConsultado, grupo)) {
						objetosAdjuntosRetorno.put("mensajeError", "yaExisteUsuarioEnGrupo");
						envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
					}
					else {
						dao.insertarUsuarioEnGrupo(usuarioConsultado, grupo);
						usuarioComunConsultado = dao.consultarUsuarioComunCompleto(usuarioConsultado);
						objetosAdjuntosRetorno.put("usuario", usuarioComunConsultado);
						envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
					}
				}
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "noExisteUsuario");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Finaliza un grupo existente. Se toma el grupo proporcionado como parámetro y se establece
	//la fecha actual como la fecha de finalización del grupo en la base de datos.
	//Después de finalizar el grupo, se devuelve un éxito.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket eliminarGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		
		
		Dao dao = new Dao();
		try {
			//Se inserta la fecha actual como el fin del grupo
			dao.finalizarGrupo(grupo);
			
			envioSocket = new EnvioSocket("/exito", null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Elimina un miembro existente en un grupo. Se toma el usuario y el grupo proporcionado
	//como parámetro y se elimina de la base de datos.
	//Después de eliminarlo, se devuelve un éxito.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket eliminarMiembroGrupo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		
		
		Dao dao = new Dao();
		try {
			//Se elimina la fila de la tabal usuarios_grupos
			//que relaciones al miembro con el grupo
			dao.eliminarMiembroGrupo(usuario, grupo);
			
			envioSocket = new EnvioSocket("/exito", null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Añade un nuevo periodo de vacaciones. Se toma el periodo vacacional
	//como parámetro con el usuario y el grupo y se añade de la base de datos.
	//Después de añadirlo, se devuelve un éxito, aunque si se solapa con otro periodo, devuelve un mensaje de error.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket anadirVacaciones(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		PeriodoVacacional periodoVacacional = (PeriodoVacacional) objetosAdjuntos.get("periodoVacacional");
		List<PeriodoVacacional> listaPeriodosFestivos;
		
		Dao dao = new Dao();
		try {
			//Comprueba en primer lugar que no exista el periodo insertado
			listaPeriodosFestivos = dao.consultarPeriodosVacacionalesPorDatos(periodoVacacional);
			
			if (listaPeriodosFestivos.size() <= 0) {
				dao.insertarPeriodoVacacional(periodoVacacional);
				envioSocket = new EnvioSocket("/exito", null);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "periodoVacacionalSolapado");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Devuelve el calendario de un miembro en un grupo, consultando las asistencias, periodos vacacionales y festivos.
	//Si las consultas son exitosas, se devuelve un objeto con los datos.
	//En caso de errores o consultas sin resultados, se devuelve un objeto con un mensaje de error.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket cargarCalendarioMiembro(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		Usuario usuario = (Usuario) objetosAdjuntos.get("usuario");
		Grupo grupo = (Grupo) objetosAdjuntos.get("grupo");
		List<Timestamp> listaAsistencias;
		List<PeriodoVacacional> listaPeriodosVacacionales;
		List<PeriodoFestivo> listaPeriodosFestivos;
		
		Dao dao = new Dao();
		try {
			//Consultar las asistencias
			listaAsistencias = dao.consultarAsistenciasPorGrupoYMiembro(usuario, grupo);
			
			//Consultar las vacaciones
			listaPeriodosVacacionales = dao.consultarPeriodosVacacionalesPorGrupoYMiembro(usuario, grupo);
			
			//Consultar los festivos
			listaPeriodosFestivos = dao.consultarPeriodosFestivosPorGrupo(grupo);
			
			if (listaAsistencias != null && listaPeriodosVacacionales != null && listaPeriodosFestivos != null) {
				Map<String, Object> mapaPeriodos = new LinkedHashMap<String, Object>();
				mapaPeriodos.put("listaAsistencias", listaAsistencias);
				mapaPeriodos.put("listaPeriodosVacacionales", listaPeriodosVacacionales);
				mapaPeriodos.put("listaPeriodosFestivos", listaPeriodosFestivos);
				objetosAdjuntosRetorno.put("mapaPeriodos", mapaPeriodos);
				envioSocket = new EnvioSocket("/exito", objetosAdjuntosRetorno);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "errorCargarCalendario");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		} catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Añade un nuevo periodo festivo. Se toma el periodo festivo
	//como parámetro con el grupo y se añade de la base de datos.
	//Después de añadirlo, se devuelve un éxito, aunque si se solapa con otro periodo, devuelve un mensaje de error.
	//En caso de excepciones, se devuelve un mensaje de error genérico.
	public EnvioSocket anadirFestivo(Map<String, Object> objetosAdjuntos) {
		EnvioSocket envioSocket;
		Map <String, Object> objetosAdjuntosRetorno = new LinkedHashMap<>();
		PeriodoFestivo periodoFestivo = (PeriodoFestivo) objetosAdjuntos.get("periodoFestivo");
		List<PeriodoFestivo> listaPeriodosFestivos;
		
		Dao dao = new Dao();
		try {
			//Comprueba en primer lugar que no exista el periodo insertado
			listaPeriodosFestivos = dao.consultarPeriodosFestivosPorDatos(periodoFestivo);
			
			if (listaPeriodosFestivos.size() <= 0) {
				dao.insertarPeriodoFestivo(periodoFestivo);
				envioSocket = new EnvioSocket("/exito", null);
			}
			else {
				objetosAdjuntosRetorno.put("mensajeError", "periodoFestivoSolapado");
				envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			objetosAdjuntosRetorno.put("mensajeError", "error");
			envioSocket = new EnvioSocket("/error", objetosAdjuntosRetorno);
		}
		
		return envioSocket;
	}
	
	//Verifica la actualización del estado para cada usuario en la base de datos.
	//Se consulta la lista de usuarios comunes y, para cada usuario, se obtiene la última asistencia registrada.
	//Si existe una última asistencia y no coincide con la fecha actual, se establece el estado como falso.
	//Si no hay una última asistencia registrada, también se establece el estado como falso.
	//Luego, se actualiza el estado en la base de datos utilizando el objeto Dao.
	public void comprobarActualizacionEstado() {
		boolean colocarEstadoFalso = false;
		
		Dao dao = new Dao();
		try {
			List<UsuarioComun> listaUsuarios = dao.consultarTodosUsuariosComunes();
			
			for (UsuarioComun usuarioComun : listaUsuarios) {
				colocarEstadoFalso = false;
				
				//Crear LocalDateTime con la ultima asistencia
				LocalDateTime ultimaAsistencia = dao.consultarTiempoUltimaAsistencia(usuarioComun);
				if (ultimaAsistencia != null) {
					//Crear LocalDate con el dia de hoy
					LocalDate hoy = LocalDate.now();
					
					//Extraer hora y minutos actuales
					if (ultimaAsistencia.getDayOfYear() != hoy.getDayOfYear()) {
						colocarEstadoFalso = true;
					}
				}
				else {
					colocarEstadoFalso = true;
				}
				
				if (colocarEstadoFalso) {
					dao.actualizarEstado(usuarioComun, colocarEstadoFalso);
				}
			}
			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

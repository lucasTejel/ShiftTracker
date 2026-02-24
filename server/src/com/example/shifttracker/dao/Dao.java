package com.example.shifttracker.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.shifttracker.pojo.Grupo;
import com.example.shifttracker.pojo.Notificacion;
import com.example.shifttracker.pojo.PeriodoFestivo;
import com.example.shifttracker.pojo.PeriodoVacacional;
import com.example.shifttracker.pojo.Usuario;
import com.example.shifttracker.pojo.UsuarioAdministrador;
import com.example.shifttracker.pojo.UsuarioComun;

public class Dao {
	private static final String NOMBRE_CONTROLADOR_MYSQL = "com.mysql.cj.jdbc.Driver";
	private static final String NOMBRE_BD = "//localhost/shift_tracker";
	private static final String URL_SQLITE_BD = "jdbc:mysql:" + NOMBRE_BD;
	private static final String USER = "root";
	private static final String PASSWORD = "root";
	
	//Consulta usuarios por datos como el nombre, email o teléfono en la base de datos y devuelve una lista de usuarios encontrados.
	public List<Usuario> consultarUsuarioPorDatos(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<Usuario> listaUsuarios = new ArrayList<Usuario>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT id_usuario, "
							+ "nombre, "
							+ "email, "
							+ "telefono "
							+ "FROM usuarios "
							+ "WHERE nombre = '" + usuario.getNombreUsuario() + "' "
							+ "OR email = '" + usuario.getEmail() + "' "
							+ "OR telefono = '" + usuario.getTelefono() + "'";
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
		while (resultados.next()) {
			Usuario usuarioConsultado;
			
			if (usuario instanceof UsuarioAdministrador) {
				usuarioConsultado = new UsuarioAdministrador(resultados.getInt("id_usuario"),
															resultados.getString("nombre"),
											                resultados.getString("email"),
											                resultados.getString("telefono"));
			}
			else {
				usuarioConsultado = new UsuarioComun(resultados.getInt("id_usuario"), 
													resultados.getString("nombre"),
													resultados.getString("email"),
									                resultados.getString("telefono"));
			}
			
			listaUsuarios.add(usuarioConsultado);
		}
		resultados.close();
		sentencia.close();
		
		return listaUsuarios;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Obtiene el valor máximo del campo "id_usuario" en la tabla de usuarios de la base de datos y lo devuelve como un entero.
	public int obtenerMaxIdUsuario() throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int maxIdUsuario = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT MAX(id_usuario) AS maximo "
							+ "FROM usuarios";
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
		if (resultados.next()) {
			maxIdUsuario = resultados.getInt("maximo");
		}
		resultados.close();
		sentencia.close();
		
		return maxIdUsuario;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//inserta un nuevo usuario en una base de datos, incluyendo su información como nombre, email, teléfono y contraseña.
	//Además, si el usuario es un administrador, también se inserta un registro en la tabla de usuarios administradores o
	//si es un usuario común, se inserta un registro en la tabla de usuarios comunes.
	//Devuelve el número de filas insertadas en la tabla de usuarios.
	public int insertarUsuario(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Inserta el nuevo usuario en la tabla usuarios
			String sentenciaSqlUsuarios = "INSERT INTO usuarios "
										+ "(id_usuario, nombre, email, telefono, contrasena) "
										+ "VALUES ("
										+ usuario.getIdUsuario() + ", '"
										+ usuario.getNombreUsuario() + "', '"
										+ usuario.getEmail() + "', '"
										+ usuario.getTelefono() + "', '"
										+ usuario.getContrasena() + "')";
			
			System.out.println("\n" + sentenciaSqlUsuarios);
			Statement sentenciaUsuarios = conexion.createStatement();
			filasInsertadas = sentenciaUsuarios.executeUpdate(sentenciaSqlUsuarios);
			
			//En caso de ser usuario administrador se insertará un nuevo registro en la tabla usuarios_administradores
			if (usuario instanceof UsuarioAdministrador) {
				String sentenciaSqlUsuariosAdministradores = "INSERT INTO usuarios_administradores "
															+ "(id_usuario) "
															+ "VALUES ("
															+ usuario.getIdUsuario() + ")";
				
				System.out.println("\n" + sentenciaSqlUsuariosAdministradores);
				Statement sentenciaUsuariosAdministradores = conexion.createStatement();
				sentenciaUsuariosAdministradores.executeUpdate(sentenciaSqlUsuariosAdministradores);
			}
			//En caso de ser usuario comun se insertará un nuevo registro en la tabla usuarios_comunes
			else {
				String sentenciaSqlUsuariosComunes = "INSERT INTO usuarios_comunes "
													+ "(id_usuario) "
													+ "VALUES ("
													+ usuario.getIdUsuario() + ")";
				
				System.out.println("\n" + sentenciaSqlUsuariosComunes);
				Statement sentenciaUsuariosComunes = conexion.createStatement();
				sentenciaUsuariosComunes.executeUpdate(sentenciaSqlUsuariosComunes);
			}
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//Consulta un usuario en la base de datos utilizando su email y contraseña.
	//Realiza una unión entre las tablas de usuarios, usuarios_administradores y usuarios_comunes.
	//Devuelve el usuario consultado si se encuentra en la base de datos, de lo contrario, devuelve null.
	public Usuario consultarUsuarioPorEmailYContrasena(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		Usuario usuarioConsultado = null;
 		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT * "
								+ "FROM usuarios "
								+ "LEFT JOIN usuarios_administradores USING (id_usuario) "
								+ "LEFT JOIN usuarios_comunes USING (id_usuario) "
								+ "WHERE email = '" + usuario.getEmail() + "' "
								+ "AND contrasena = '" + usuario.getContrasena() + "'";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
			if (resultados.next()) {
				boolean estadoNulo;
				//Vemos si el estado es nulo
				resultados.getBoolean("estado");
				estadoNulo = resultados.wasNull();
				
				if (estadoNulo) {
					usuarioConsultado = new UsuarioAdministrador(resultados.getInt("id_usuario"),
																resultados.getString("nombre"),
												                resultados.getString("email"),
												                resultados.getString("telefono"),
												                resultados.getString("contrasena"));
				}
				else {
					usuarioConsultado = new UsuarioComun(resultados.getInt("id_usuario"),
														resultados.getString("nombre"),
										                resultados.getString("email"),
										                resultados.getString("telefono"),
										                resultados.getString("contrasena"));
				}
			}
			resultados.close();
			sentencia.close();
			
			return usuarioConsultado;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	
	//Consulta un usuario en la base de datos utilizando id_usuario.
	//Realiza una unión entre las tablas de usuarios, usuarios_administradores y usuarios_comunes.
	//Devuelve el usuario consultado si se encuentra en la base de datos, de lo contrario, devuelve null.
	public Usuario consultarUsuarioPorId(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		Usuario usuarioConsultado = null;
 		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT * "
								+ "FROM usuarios "
								+ "LEFT JOIN usuarios_administradores USING (id_usuario) "
								+ "LEFT JOIN usuarios_comunes USING (id_usuario) "
								+ "WHERE id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			boolean estado;
			boolean estadoNulo;
			if (resultados.next()) {
				//Vemos si el estado es nulo
				estado = resultados.getBoolean("estado");
				estadoNulo = resultados.wasNull();
				
				//Si el estado es nulo, es usuario administrador
				if (estadoNulo) {
					usuarioConsultado = new UsuarioAdministrador(resultados.getString("nombre"),
												                resultados.getString("email"),
												                resultados.getString("telefono"),
												                resultados.getString("cargo"),
												                resultados.getInt("id_usuario"));
				}
				else {
					usuarioConsultado = new UsuarioComun(resultados.getString("nombre"),
										                resultados.getString("email"),
										                resultados.getString("telefono"),
										                estado,
										                resultados.getInt("id_usuario"));
				}
			}
			resultados.close();
			sentencia.close();
			
			return usuarioConsultado;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	
	//Consulta grupos en la base de datos según el tipo de usuario proporcionado (UsuarioComun o UsuarioAdministrador).
	//Si es un UsuarioComun, se realiza una unión con la tabla usuarios_grupos y se filtra por id_usuario.
	//Si es un UsuarioAdministrador, se filtra por el id_usuario_administrador.
	//Además, se excluyen los grupos que hayan finalizado (fecha_fin) o cuya fecha de finalización sea anterior a la fecha actual.
	//Devuelve una lista de grupos encontrados.
	public List<Grupo> consultarGruposPorUsuario(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<Grupo> listaGrupos = new ArrayList<Grupo>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT id_grupo, "
							+ "nombre, "
							+ "descripcion, "
							+ "horario, "
							+ "fecha_inicio, "
							+ "id_usuario_administrador "
							+ "FROM grupos ";
		if (usuario instanceof UsuarioComun) {
			sentenciaSql += "JOIN usuarios_grupos USING (id_grupo) "
						+ "WHERE id_usuario = " + usuario.getIdUsuario();
		}
		else if (usuario instanceof UsuarioAdministrador) {
			sentenciaSql += "WHERE id_usuario_administrador = " + usuario.getIdUsuario();
		}
		
		sentenciaSql += " AND (fecha_fin IS NULL OR fecha_fin > CURRENT_DATE)";
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);
		
		Grupo grupoConsultado;
		while (resultados.next()) {
			grupoConsultado = new Grupo(resultados.getInt("id_grupo"),
						                 resultados.getString("nombre"),
						                 resultados.getString("descripcion"),
						                 resultados.getString("horario"),
						                 resultados.getDate("fecha_inicio").toLocalDate(),
						                 resultados.getInt("id_usuario_administrador"));
			
			listaGrupos.add(grupoConsultado);
		}
		resultados.close();
		sentencia.close();
		
		return listaGrupos;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Consulta los usuarios que son miembros de un grupo específico en la base de datos.
	//Realiza una unión entre las tablas de usuarios, usuarios_administradores, usuarios_comunes y usuarios_grupos.
	//Filtra los usuarios según el id_grupo proporcionado. Devuelve una lista de usuarios que son miembros del grupo.
	//De esta manera, el primer usuario de la lista es el administrador y los siguientes el resto de usuarios.
	public List<Usuario> consultarUsuariosPorGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<Usuario> listaMiembros = new ArrayList<Usuario>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT id_usuario, "
								+ "usuarios.nombre, "
								+ "email, "
								+ "telefono, "
								+ "cargo, "
								+ "null as estado "
								+ "FROM usuarios "
								+ "JOIN usuarios_administradores USING (id_usuario) "
								+ "JOIN grupos ON (usuarios_administradores.id_usuario = grupos.id_usuario_administrador) "
								+ "WHERE id_grupo = " + grupo.getIdGrupo()
								+ " UNION "
								+ "SELECT id_usuario, "
								+ "usuarios.nombre, "
								+ "email, "
								+ "telefono, "
								+ "null as cargo, "
								+ "estado "
								+ "FROM usuarios "
								+ "JOIN usuarios_comunes USING (id_usuario) "
								+ "JOIN usuarios_grupos USING (id_usuario) "
								+ "WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			if (resultados.next()) {
				UsuarioAdministrador usuarioAdministradorConsultado;
				usuarioAdministradorConsultado = new UsuarioAdministrador(resultados.getInt("id_usuario"),
																		resultados.getString("nombre"),
																		resultados.getString("email"),
																		resultados.getString("telefono"));
				usuarioAdministradorConsultado.setCargo(resultados.getString("cargo"));
				listaMiembros.add(usuarioAdministradorConsultado);
			}
			
			UsuarioComun usuarioComunConsultado;
			while (resultados.next()) {
				usuarioComunConsultado = new UsuarioComun(resultados.getInt("id_usuario"),
														resultados.getString("nombre"),
														resultados.getString("email"),
														resultados.getString("telefono"));
				usuarioComunConsultado.setEstado(resultados.getBoolean("estado"));
				
				listaMiembros.add(usuarioComunConsultado);
			}
			resultados.close();
			sentencia.close();

			return listaMiembros;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Consulta y devuelve un objeto UsuarioComun completo (incluyendo todos los atributos) de la base de datos,
	//utilizando id_usuario proporcionado como filtro. Realiza una combinación de las tablas "usuarios" y "usuarios_comunes"
	//para obtener los datos del usuario común específico.
	public UsuarioComun consultarUsuarioComunCompleto(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		UsuarioComun usuarioConsultado = null;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT id_usuario, "
								+ "nombre, "
								+ "email, "
								+ "telefono, "
								+ "estado "
								+ "FROM usuarios "
								+ "JOIN usuarios_comunes USING (id_usuario) "
								+ "WHERE id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);

			if (resultados.next()) {
				usuarioConsultado = new UsuarioComun(resultados.getInt("id_usuario"),
													resultados.getString("nombre"),
													resultados.getString("email"),
													resultados.getString("telefono"),
													resultados.getBoolean("estado"));
				
			}
			resultados.close();
			sentencia.close();

			return usuarioConsultado;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Consulta y devuelve una lista de timestamps que representan las asistencias de un usuario a un grupo específico.
	//Utiliza id_usuario e id_grupo proporcionados como filtros en la consulta.
	//La consulta se realiza en la tabla "asistencias" y recupera la columna "fecha_hora_entrada".
	//La lista resultante contiene todos los registros de asistencia del usuario al grupo.
	public List<Timestamp> consultarAsistenciasPorGrupoYMiembro(Usuario usuario, Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<Timestamp> listaAsistencias = new ArrayList<Timestamp>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT fecha_hora_entrada "
								+ "FROM asistencias "
								+ "WHERE id_grupo = " + grupo.getIdGrupo()
								+ " AND id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			Timestamp asistenciaConsultada;
			while (resultados.next()) {
				asistenciaConsultada = resultados.getTimestamp("fecha_hora_entrada");
				
				listaAsistencias.add(asistenciaConsultada);
			}
			resultados.close();
			sentencia.close();
			
			return listaAsistencias;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Realiza una consulta a una base de datos para obtener los periodos vacacionales de un usuario específico en un grupo determinado.
	//Devuelve la lista consultada de periodos vacacionales.
	public List<PeriodoVacacional> consultarPeriodosVacacionalesPorGrupoYMiembro(Usuario usuario, Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<PeriodoVacacional> listaPeriodosVacacionales = new ArrayList<PeriodoVacacional>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT fecha_inicio, "
								+ "fecha_fin "
								+ "FROM periodos_vacacionales "
								+ "WHERE id_grupo = " + grupo.getIdGrupo()
								+ " AND id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			PeriodoVacacional periodoConsultado;
			while (resultados.next()) {
				periodoConsultado = new PeriodoVacacional(resultados.getDate("fecha_inicio").toLocalDate(),
													resultados.getDate("fecha_fin").toLocalDate());
				
				listaPeriodosVacacionales.add(periodoConsultado);
			}
			resultados.close();
			sentencia.close();
			
			return listaPeriodosVacacionales;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Realiza una consulta a una base de datos para obtener los periodos festivos de un grupo determinado.
	//Devuelve la lista consultada de periodos festivos.
	public List<PeriodoFestivo> consultarPeriodosFestivosPorGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<PeriodoFestivo> listaPeriodosFestivos = new ArrayList<PeriodoFestivo>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT fecha_inicio, "
								+ "fecha_fin "
								+ "FROM periodos_festivos "
								+ "WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			PeriodoFestivo periodoConsultado;
			while (resultados.next()) {
				periodoConsultado = new PeriodoFestivo(resultados.getDate("fecha_inicio").toLocalDate(),
													resultados.getDate("fecha_fin").toLocalDate());
				
				listaPeriodosFestivos.add(periodoConsultado);
			}
			resultados.close();
			sentencia.close();
			
			return listaPeriodosFestivos;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Realiza una consulta a una base de datos para obtener las notificaciones asociadas a un usuario específico.
	//Devuelve la lista consultada de notificiaciones.
	public List<Notificacion> consultarNotificaciones(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<Notificacion> listaNotificaciones = new ArrayList<Notificacion>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT * "
								+ "FROM notificaciones "
								+ "WHERE id_usuario = " + usuario.getIdUsuario()
								+ " ORDER BY fecha_hora_recibo DESC";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			Notificacion notificacion;
			while (resultados.next()) {
				notificacion = new Notificacion(resultados.getInt("id_usuario"),
												resultados.getTimestamp("fecha_hora_recibo"),
												resultados.getString("descripcion"));
				
				listaNotificaciones.add(notificacion);
			}
			resultados.close();
			sentencia.close();
			
			return listaNotificaciones;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Realiza la inserción de una nueva asistencia en la base de datos, asociada a un usuario y un grupo específicos, con una fecha y hora de inicio.
	//Además, en caso de que la asistencia sea con NFC, se inserta una notificación correspondiente a la asistencia.
	//Devuelve el número de filas insertadas en la tabla "asistencias".
	public int insertarAsistencia(Usuario usuario, Grupo grupo, Timestamp fechaHoraInicio) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String tiempoInsertar;
			if (fechaHoraInicio == null) {
				tiempoInsertar = "CURRENT_TIMESTAMP";
			}
			else {
				tiempoInsertar = "'" + fechaHoraInicio + "'";
			}
			
			conexion.setAutoCommit(false);
			
			//Inserta la nueva asistencia en la tabla asistencias
			String sentenciaSqlAsistencia = "INSERT INTO asistencias "
										+ "(id_usuario, id_grupo, fecha_hora_entrada) "
										+ "VALUES ("
										+ usuario.getIdUsuario() + ", '"
										+ grupo.getIdGrupo() + "', "
										+ tiempoInsertar + ")";
			
			System.out.println("\n" + sentenciaSqlAsistencia);
			Statement sentenciaAsistencia = conexion.createStatement();
			filasInsertadas = sentenciaAsistencia.executeUpdate(sentenciaSqlAsistencia);
			
			//Inserta la notificación correspondiente a la asistencia
			//Solo lo hace en caso de que la asistencia haya sido con NFC
			if (fechaHoraInicio == null) {
				String descripcion = "nombreUsuario=" + usuario.getNombreUsuario() + ";nombreGrupo=" + grupo.getNombre() + ";;";
				String sentenciaSqlNotificaciones = "INSERT INTO notificaciones "
													+ "(id_usuario, fecha_hora_recibo, descripcion) "
													+ "VALUES ("
													+ grupo.getIdUsuarioAdministrador()
													+ ", CURRENT_TIMESTAMP, '"
													+ descripcion + "')";
				
				System.out.println("\n" + sentenciaSqlNotificaciones);
				Statement sentenciaNotificaciones = conexion.createStatement();
				filasInsertadas = sentenciaNotificaciones.executeUpdate(sentenciaSqlNotificaciones);
			}
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//Actualiza el nombre de usuario en la base de datos para un usuario específico.
	//Devuelve el número de filas actualizadas en la tabla "usuarios".
	public int actualizarNombreUsuario(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza el nombre de usuario
			String sentenciaSql = "UPDATE usuarios "
										+ "SET nombre = '" + usuario.getNombreUsuario()
										+ "' WHERE id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaNombreUsuario = conexion.createStatement();
			filasActualizadas = sentenciaNombreUsuario.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
	
	//Actualiza el cargo de un usuario administrador en la base de datos.
	//Devuelve el número de filas actualizadas en la tabla "usuarios_administradores".
	public int actualizarCargo(UsuarioAdministrador usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza el cargo de usuario administrador
			String sentenciaSql = "UPDATE usuarios_administradores "
										+ "SET cargo = '" + usuario.getCargo()
										+ "' WHERE id_usuario = " + usuario.getIdUsuario();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaCargo = conexion.createStatement();
			filasActualizadas = sentenciaCargo.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
	
	//Actualiza el nombre de un grupo en la base de datos.
	//Devuelve el número de filas actualizadas en la tabla "grupos".
	public int actualizarNombreGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza el nombre del grupo
			String sentenciaSql = "UPDATE grupos "
										+ "SET nombre = '" + grupo.getNombre()
										+ "' WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaAsistencia = conexion.createStatement();
			filasActualizadas = sentenciaAsistencia.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
	
	//Actualiza la descripción de un grupo en la base de datos.
	//Devuelve el número de filas actualizadas en la tabla "grupos".
	public int actualizarDescripcionGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza la descripción del grupo
			String sentenciaSql = "UPDATE grupos "
								+ "SET descripcion = '" + grupo.getDescripcion()
								+ "' WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaDescripcion = conexion.createStatement();
			filasActualizadas = sentenciaDescripcion.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
	
	//Realiza una consulta a la base de datos para obtener el valor máximo del campo "id_grupo" en la tabla "grupos".
	//Devuelve el valor máximo encontrado.
	public int obtenerMaxIdGrupo() throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int maxIdUsuario = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT MAX(id_grupo) AS maximo "
							+ "FROM grupos";
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
		if (resultados.next()) {
			maxIdUsuario = resultados.getInt("maximo");
		}
		resultados.close();
		sentencia.close();
		
		return maxIdUsuario;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Inserta un nuevo grupo en la base de datos, con los datos proporcionados en el objeto "grupo".
	//Devuelve el número de filas insertadas en la tabla "grupos".
	public int insertarGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Inserta el nuevo grupo en la tabla grupos
			String sentenciaSql = "INSERT INTO grupos "
										+ "(id_grupo, nombre, descripcion, horario, "
										+ "fecha_inicio, fecha_fin, id_usuario_administrador) "
										+ "VALUES ("
										+ grupo.getIdGrupo() + ", '"
										+ grupo.getNombre() + "', '"
										+ grupo.getDescripcion() + "', '"
										+ grupo.getHorario()
										+ "', CURRENT_DATE"
										+ ", null, "
										+ grupo.getIdUsuarioAdministrador() + ")";

			System.out.println("\n" + sentenciaSql);
			Statement sentenciaUsuarios = conexion.createStatement();
			filasInsertadas = sentenciaUsuarios.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//Verifica si un usuario está asociado a un grupo específico en la base de datos.
	//Devuelve un valor booleano indicando si el usuario es miembro del grupo o no.
	public boolean obtenerExisteMiembroEnGrupo(Usuario usuario, Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		boolean existe = true;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT * "
							+ "FROM usuarios_grupos "
							+ "WHERE id_usuario = " + usuario.getIdUsuario()
							+ " AND id_grupo = " + grupo.getIdGrupo();
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
		if (! resultados.next()) {
			existe = false;
		}
		resultados.close();
		sentencia.close();
		
		return existe;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Verifica si un usuario es un usuario administrador en la base de datos.
	//Devuelve un valor booleano indicando si el usuario es administrador o no.
	public boolean obtenerEsUsuarioAdministrador(Usuario usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		boolean esAdministrador = false;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
		
		String sentenciaSql = "SELECT * "
							+ "FROM usuarios_administradores "
							+ "WHERE id_usuario = " + usuario.getIdUsuario();
		
		System.out.println("\n" + sentenciaSql);
		Statement sentencia = conexion.createStatement();
		ResultSet resultados = sentencia.executeQuery(sentenciaSql);	
		if (resultados.next()) {
			esAdministrador = true;
		}
		resultados.close();
		sentencia.close();
		
		return esAdministrador;
		
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Insertar un usuario en un grupo en la base de datos. Recibe como parámetros un objeto Usuario y un objeto Grupo.
	//Devuelve el número de filas insertadas en la tabla "usuarios_grupos".
	public int insertarUsuarioEnGrupo(Usuario usuario, Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Inserta el nuevo grupo en la tabla grupos
			String sentenciaSql = "INSERT INTO usuarios_grupos "
										+ "(id_usuario, id_grupo) "
										+ "VALUES ("
										+ usuario.getIdUsuario() + ", " + grupo.getIdGrupo() + ")";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaUsuarios = conexion.createStatement();
			filasInsertadas = sentenciaUsuarios.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//Actualiza la fecha de finalización de un grupo en la base de datos.
	//Recibe como parámetro un objeto Grupo y retorna el número de filas actualizadas en la tabla "grupos",
	//que indica si la operación fue exitosa. La fecha de finalización se establece como la fecha actual utilizando "CURRENT_DATE".
	public int finalizarGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza la descripción del grupo
			String sentenciaSql = "UPDATE grupos "
									+ "SET fecha_fin = CURRENT_DATE "
									+ "WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaGrupo = conexion.createStatement();
			filasActualizadas = sentenciaGrupo.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
	
	//Elimina un usuario de un grupo en la base de datos. Recibe como parámetros un objeto Usuario y un objeto Grupo que son los filtros.
	//Devuelve el número de filas eliminadas en la tabla "usuarios_grupos", que indica si la operación fue exitosa. 
	public int eliminarMiembroGrupo(Usuario usuario, Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasEliminadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			//Actualiza la descripción del grupo
			String sentenciaSql = "DELETE FROM usuarios_grupos "
									+ "WHERE id_usuario = " + usuario.getIdUsuario()
									+ " AND id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaMiembro = conexion.createStatement();
			filasEliminadas = sentenciaMiembro.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasEliminadas;
	}
	
	//Realizar una consulta a la base de datos para obtener los periodos vacacionales que coincidan con los datos proporcionados.
	//Utilizando estos datos, se construye una consulta SQL que selecciona los periodos vacacionales que cumplan con los criterios establecidos.
	//Si se proporcionan fechas de inicio y fin, se realiza una validación para asegurar que los periodos consultados no se solapen con estas fechas.
	//Los resultados se almacenan en una lista de objetos "PeriodoVacacional" y se devuelve como resultado de la función.
	public List<PeriodoVacacional> consultarPeriodosVacacionalesPorDatos(PeriodoVacacional periodoVacacional) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<PeriodoVacacional> listaPeriodosVacacionales = new ArrayList<PeriodoVacacional>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			
			String sentenciaSql = "SELECT * "
					+ "FROM periodos_vacacionales "
					+ "WHERE id_usuario = " + periodoVacacional.getIdUsuario()
					+ " AND id_grupo = " + periodoVacacional.getIdGrupo();
			if (periodoVacacional.getFechaInicio() != null && periodoVacacional.getFechaFin() != null) {	
				sentenciaSql += " AND NOT((fecha_inicio > '" + periodoVacacional.getFechaInicio()
							+ "' AND fecha_inicio > '" + periodoVacacional.getFechaFin()
							+ "') OR (fecha_fin < '" + periodoVacacional.getFechaInicio()
							+ "' AND fecha_fin < '" + periodoVacacional.getFechaFin() + "'))";
			}
			
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			PeriodoVacacional periodoConsultado;
			while (resultados.next()) {
				periodoConsultado = new PeriodoVacacional(resultados.getInt("id_usuario"),
														resultados.getInt("id_grupo"),
														resultados.getDate("fecha_inicio").toLocalDate(),
														resultados.getDate("fecha_fin").toLocalDate());
				
				listaPeriodosVacacionales.add(periodoConsultado);
			}
			resultados.close();
			sentencia.close();
			
			return listaPeriodosVacacionales;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Inserta un nuevo periodo vacacional en la tabla periodos_vacacionales,
	//especificando el id del usuario y el id del grupo, junto con las fechas de inicio y fin del periodo.
	//Devuelve el número de filas insertadas en la tabla "periodos_vacacionales"
	public int insertarPeriodoVacacional(PeriodoVacacional periodoVacacional) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			conexion.setAutoCommit(false);
			
			//Inserta el nuevo periodo vacacional en la tabla periodos_vacacionales
			String sentenciaSql = "INSERT INTO periodos_vacacionales "
											+ "(id_usuario, id_grupo, fecha_inicio, fecha_fin) "
											+ "VALUES ("
											+ periodoVacacional.getIdUsuario() + ", "
											+ periodoVacacional.getIdGrupo() + ", '"
											+ periodoVacacional.getFechaInicio() + "', '"
											+ periodoVacacional.getFechaFin() + "')";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaPeriodoVacacional = conexion.createStatement();
			filasInsertadas = sentenciaPeriodoVacacional.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//C los periodos festivos de un grupo específico, filtrando por fecha de inicio y fin si se especifican.
	//Devuelve una lista de objetos PeriodoFestivo que contienen el id del grupo, la fecha de inicio y la fecha de fin de cada periodo festivo consultado.
	public List<PeriodoFestivo> consultarPeriodosFestivosPorDatos(PeriodoFestivo periodoFestivo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<PeriodoFestivo> listaPeriodosFestivos = new ArrayList<PeriodoFestivo>();
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			
			String sentenciaSql = "SELECT * "
					+ "FROM periodos_festivos "
					+ "WHERE id_grupo = " + periodoFestivo.getIdGrupo();
			if (periodoFestivo.getFechaInicio() != null && periodoFestivo.getFechaFin() != null) {	
				sentenciaSql += " AND NOT((fecha_inicio > '" + periodoFestivo.getFechaInicio()
							+ "' AND fecha_inicio > '" + periodoFestivo.getFechaFin()
							+ "') OR (fecha_fin < '" + periodoFestivo.getFechaInicio()
							+ "' AND fecha_fin < '" + periodoFestivo.getFechaFin() + "'))";
			}
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			PeriodoFestivo periodoConsultado;
			while (resultados.next()) {
				periodoConsultado = new PeriodoFestivo(resultados.getInt("id_grupo"),
														resultados.getDate("fecha_inicio").toLocalDate(),
														resultados.getDate("fecha_fin").toLocalDate());
				
				listaPeriodosFestivos.add(periodoConsultado);
			}
			resultados.close();
			sentencia.close();
			
			return listaPeriodosFestivos;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//inserta un nuevo periodo festivo en la tabla periodos_festivos,
	//especificando el id del grupo y las fechas de inicio y fin del periodo.
	//Devuelve el número de filas insertadas en la tabla.
	public int insertarPeriodoFestivo(PeriodoFestivo periodoFestivo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasInsertadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			conexion.setAutoCommit(false);
			
			//Inserta el nuevo periodo vacacional en la tabla periodos_vacacionales
			String sentenciaSql = "INSERT INTO periodos_festivos "
											+ "(id_grupo, fecha_inicio, fecha_fin) "
											+ "VALUES ("
											+ periodoFestivo.getIdGrupo() + ", '"
											+ periodoFestivo.getFechaInicio() + "', '"
											+ periodoFestivo.getFechaFin() + "')";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentenciaPeriodoFestivo = conexion.createStatement();
			filasInsertadas = sentenciaPeriodoFestivo.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasInsertadas;
	}
	
	//Consulta todos los usuarios comunes de la base de datos.
	//Devuelve una lista de objetos UsuarioComun que contienen el id del usuario, nombre, email,
	//teléfono y estado de cada usuario común consultado.
	public List<UsuarioComun> consultarTodosUsuariosComunes() throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		List<UsuarioComun> listaUsuariosComunes = new ArrayList<UsuarioComun>();
		UsuarioComun usuarioConsultado = null;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT id_usuario, "
								+ "nombre, "
								+ "email, "
								+ "telefono, "
								+ "estado "
								+ "FROM usuarios "
								+ "JOIN usuarios_comunes USING (id_usuario)";
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);

			while (resultados.next()) {
				usuarioConsultado = new UsuarioComun(resultados.getInt("id_usuario"),
													resultados.getString("nombre"),
													resultados.getString("email"),
													resultados.getString("telefono"),
													resultados.getBoolean("estado"));
				listaUsuariosComunes.add(usuarioConsultado);
			}
			resultados.close();
			sentencia.close();

			return listaUsuariosComunes;
			
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
	}
	
	//Consulta la hora de finalización de un grupo específico.
	///Devuelve un arreglo de enteros de tamaño 2, donde el primer elemento representa la hora
	//y el segundo elemento representa los minutos de la hora de finalización del grupo consultado.
	public int[] consultarHoraFinGrupo(Grupo grupo) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int[] hora = new int[2];
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT SUBSTRING(horario, length(horario) - 4, 2) AS hora_fin, "
								+ "SUBSTRING(horario, length(horario) - 1, 2) AS minutos_fin "
								+ "FROM grupos "
								+ "WHERE id_grupo = " + grupo.getIdGrupo();
			
			System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			if (resultados.next()) {
				hora[0] = Integer.parseInt(resultados.getString("hora_fin"));
				hora[1] = Integer.parseInt(resultados.getString("minutos_fin"));
			}
			resultados.close();
			sentencia.close();
			
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
		
		return hora;
	}
	
	//consulta el tiempo de la última asistencia registrada para un usuario común.
	//Devuelve un objeto LocalDateTime que representa el momento de la última entrada
	//registrada en la tabla de asistencias para el usuario consultado.
	public LocalDateTime consultarTiempoUltimaAsistencia(UsuarioComun usuario) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		LocalDateTime tiempoUltimaAsistencia = null;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			
			String sentenciaSql = "SELECT fecha_hora_entrada "
								+ "FROM asistencias "
								+ "WHERE id_usuario = " + usuario.getIdUsuario()
								+ " ORDER BY fecha_hora_entrada DESC";
			
			//System.out.println("\n" + sentenciaSql);
			Statement sentencia = conexion.createStatement();
			ResultSet resultados = sentencia.executeQuery(sentenciaSql);
			
			if (resultados.next()) {
				tiempoUltimaAsistencia = resultados.getTimestamp("fecha_hora_entrada").toLocalDateTime();
			}
			resultados.close();
			sentencia.close();
			
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		finally {
			if (conexion != null) {
				conexion.close();
			}
		}
		
		return tiempoUltimaAsistencia;
	}
	
	//Actualiza el estado de un usuario común en la tabla usuarios_comunes.
	//Recibe un objeto UsuarioComun y un valor booleano (estadoFalso).
	//Si el estado es falso, el estado se establece en 0; de lo contrario, se establece en 1.
	//Devuelve el número de filas actualizadas en la tabla.
	public int actualizarEstado(UsuarioComun usuario, boolean estadoFalso) throws ClassNotFoundException, SQLException {
		Connection conexion = null;
		int filasActualizadas = 0;
		try {
			Class.forName(NOMBRE_CONTROLADOR_MYSQL);
			conexion = DriverManager.getConnection(URL_SQLITE_BD, USER, PASSWORD);
			conexion.setAutoCommit(false);
			
			int estadoNuevo;
			if (estadoFalso) {
				estadoNuevo = 0;
			}
			else {
				estadoNuevo = 1;
			}
			
			//Actualiza el estado de un usuario comun
			String sentenciaSql = "UPDATE usuarios_comunes "
										+ "SET estado = " + estadoNuevo
										+ " WHERE id_usuario = " + usuario.getIdUsuario();
			
			//System.out.println("\n" + sentenciaSql);
			Statement sentenciaAsistencia = conexion.createStatement();
			filasActualizadas = sentenciaAsistencia.executeUpdate(sentenciaSql);
			
			conexion.commit();
		}
		catch (SQLException e) {
			if (conexion != null) {
				conexion.rollback();
			}
			throw e;
		}
		finally {
			try {
				if (conexion != null) {
					conexion.close();
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return filasActualizadas;
	}
}


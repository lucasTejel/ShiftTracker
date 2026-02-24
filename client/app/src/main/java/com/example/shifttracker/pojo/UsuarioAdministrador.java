package com.example.shifttracker.pojo;

import java.io.Serializable;

public class UsuarioAdministrador extends Usuario implements Serializable {
	private String cargo;

	public UsuarioAdministrador(String nombreUsuario, String email, String telefono, String contrasena, String cargo) {
		super(nombreUsuario, email, telefono, contrasena);
		this.cargo = cargo;
	}

	public UsuarioAdministrador(String nombreUsuario, String email, String telefono, String cargo, int idUsuario) {
		super(idUsuario, nombreUsuario, email, telefono);
		this.cargo = cargo;
	}

	public UsuarioAdministrador(int idUsuario, String nombreUsuario, String email, String telefono) {
		super(idUsuario, nombreUsuario, email, telefono);
	}

	public UsuarioAdministrador(int idUsuario, String nombreUsuario, String email, String telefono, String contrasena) {
		super(idUsuario, nombreUsuario, email, telefono, contrasena);
	}

	public UsuarioAdministrador(String nombreUsuario, String email, String telefono, String contrasena) {
		super(nombreUsuario, email, telefono, contrasena);
	}

	public UsuarioAdministrador(String nombreUsuario, String email, String telefono) {
		super(nombreUsuario, email, telefono);
	}

	public UsuarioAdministrador(int idUsuario, String cargo) {
		super(idUsuario);
		this.cargo = cargo;
	}

	public UsuarioAdministrador(int idUsuario) {
		super(idUsuario);
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

}

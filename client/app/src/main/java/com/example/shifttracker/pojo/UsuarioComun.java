package com.example.shifttracker.pojo;

import java.io.Serializable;

public class UsuarioComun extends Usuario implements Serializable {
	private boolean estado;

	public UsuarioComun(int idUsuario, String nombreUsuario, String email, String telefono, boolean estado) {
		super(idUsuario, nombreUsuario, email, telefono);
		this.estado = estado;
	}

	public UsuarioComun(String nombreUsuario, String email, String telefono, String contrasena, boolean estado) {
		super(nombreUsuario, email, telefono, contrasena);
		this.estado = estado;
	}

	public UsuarioComun(String nombreUsuario, String email, String telefono, boolean estado, int idUsuario) {
		super(idUsuario, nombreUsuario, email, telefono);
		this.estado = estado;
	}

	public UsuarioComun(int idUsuario, String nombreUsuario, String email, String telefono, String contrasena) {
		super(idUsuario, nombreUsuario, email, telefono, contrasena);
	}

	public UsuarioComun(String nombreUsuario, String email, String telefono, String contrasena) {
		super(nombreUsuario, email, telefono, contrasena);
	}

	public UsuarioComun(int idUsuario, String nombreUsuario, String email, String telefono) {
		super(idUsuario, nombreUsuario, email, telefono);
	}

	public UsuarioComun(String nombreUsuario, String email, String telefono) {
		super(nombreUsuario, email, telefono);
	}

	public UsuarioComun(String email, String contrasena) {
		super(email, contrasena);
	}

	public UsuarioComun(int idUsuario, String nombreUsuario) {
		super(idUsuario, nombreUsuario);
	}

	public UsuarioComun(String email) {
		super(email);
	}

	public UsuarioComun(int idUsuario) {
		super(idUsuario);
	}

	public boolean getEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

}

package com.example.shifttracker.pojo;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
	private int idUsuario;
	private String nombreUsuario;
	private String email;
	private String telefono;
	private String contrasena;

	public Usuario(int idUsuario, String nombreUsuario, String email, String telefono, String contrasena) {
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
		this.email = email;
		this.telefono = telefono;
		this.contrasena = contrasena;
	}

	public Usuario(int idUsuario, String nombreUsuario, String email, String telefono) {
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
		this.email = email;
		this.telefono = telefono;
	}

	public Usuario(String nombreUsuario, String email, String telefono, String contrasena) {
		this.nombreUsuario = nombreUsuario;
		this.email = email;
		this.telefono = telefono;
		this.contrasena = contrasena;
	}

	public Usuario(String nombreUsuario, String email, String telefono) {
		this.nombreUsuario = nombreUsuario;
		this.email = email;
		this.telefono = telefono;
	}

	public Usuario(String email, String contrasena) {
		this.email = email;
		this.contrasena = contrasena;
	}

	public Usuario(int idUsuario, String nombreUsuario) {
		this.idUsuario = idUsuario;
		this.nombreUsuario = nombreUsuario;
	}

	public Usuario(String email) {
		this.email = email;
	}

	public Usuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}
}

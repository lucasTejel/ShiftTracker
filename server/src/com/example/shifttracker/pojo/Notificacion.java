package com.example.shifttracker.pojo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Notificacion implements Serializable {
	private int idUsuario;
	private Timestamp fechaHoraRecibo;
	private String descripcion;

	public Notificacion(int idUsuario, Timestamp fechaHoraRecibo, String descripcion) {
		this.idUsuario = idUsuario;
		this.fechaHoraRecibo = fechaHoraRecibo;
		this.descripcion = descripcion;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public Timestamp getFechaHoraRecibo() {
		return fechaHoraRecibo;
	}

	public String getDescripcion() {
		return descripcion;
	}

}

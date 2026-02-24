package com.example.shifttracker.pojo;

import java.io.Serializable;
import java.time.LocalDate;

public class PeriodoVacacional implements Serializable {

	private int idUsuario;
	private int idGrupo;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;

	public PeriodoVacacional(int idUsuario, int idGrupo, LocalDate fechaInicio, LocalDate fechaFin) {
		this.idUsuario = idUsuario;
		this.idGrupo = idGrupo;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public PeriodoVacacional(int idUsuario, int idGrupo) {
		this.idUsuario = idUsuario;
		this.idGrupo = idGrupo;
	}
	
	public PeriodoVacacional(LocalDate fechaInicio, LocalDate fechaFin) {
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}
}

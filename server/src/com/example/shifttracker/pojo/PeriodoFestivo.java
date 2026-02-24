package com.example.shifttracker.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class PeriodoFestivo implements Serializable {
	private int idGrupo;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	
	public PeriodoFestivo(int idGrupo, LocalDate fechaInicio, LocalDate fechaFin) {
		this.idGrupo = idGrupo;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public PeriodoFestivo(LocalDate fechaInicio, LocalDate fechaFin) {
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public PeriodoFestivo(int idGrupo) {
		this.idGrupo = idGrupo;
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

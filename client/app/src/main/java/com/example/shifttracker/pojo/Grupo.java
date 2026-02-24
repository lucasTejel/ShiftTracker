package com.example.shifttracker.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Grupo implements Serializable {
	private int idGrupo;
	private String nombre;
	private String descripcion;
	private String horario;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private int idUsuarioAdministrador;

	public Grupo(int idGrupo, String nombre, String descripcion, String horario, LocalDate fechaInicio, LocalDate fechaFin, int idUsuarioAdministrador) {
		this.idGrupo = idGrupo;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.horario = horario;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.idUsuarioAdministrador = idUsuarioAdministrador;
	}

	public Grupo(int idGrupo, String nombre, String descripcion, String horario, LocalDate fechaInicio, int idUsuarioAdministrador) {
		this.idGrupo = idGrupo;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.horario = horario;
		this.fechaInicio = fechaInicio;
		this.idUsuarioAdministrador = idUsuarioAdministrador;
	}

	public Grupo(String nombre, String descripcion, String horario, int idUsuarioAdministrador) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.horario = horario;
		this.idUsuarioAdministrador = idUsuarioAdministrador;
	}

	public Grupo(int idGrupo, String nombre) {
		this.idGrupo = idGrupo;
		this.nombre = nombre;
	}

	public Grupo(String descripcion, int idGrupo) {
		this.idGrupo = idGrupo;
		this.descripcion = descripcion;
	}

	public Grupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}

	public int getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
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

	public int getIdUsuarioAdministrador() {
		return idUsuarioAdministrador;
	}

	public void setIdUsuarioAdministrador(int idUsuarioAdministrador) {
		this.idUsuarioAdministrador = idUsuarioAdministrador;
	}

}

package com.example.shifttracker.conexion_servidor;

import java.io.Serializable;
import java.util.Map;

public class EnvioSocket implements Serializable {
	private String tipo;
	Map <String, Object> objetosAdjuntos;
	
	public EnvioSocket(String tipo, Map<String, Object> objetosAdjuntos) {
		this.tipo = tipo;
		this.objetosAdjuntos = objetosAdjuntos;
	}

	public String getTipo() {
		return tipo;
	}


	public Map<String, Object> getObjetosAdjuntos() {
		return objetosAdjuntos;
	}
	
}

package com.example.shifttracker.conexion_servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import com.example.shifttracker.controlador.Controlador;

public class ClienteHilo extends Thread {

	//Atributos
	private ObjectInputStream flujoEntradaObjeto;
	private ObjectOutputStream flujoSalidaObjeto;
	private Socket cliente = null;
	
	private ServerEscuchaCliente hiloEscucha;
	private boolean continuaEscucha = true;

	//Constructor
	public ClienteHilo(Socket socket) {
		try {
			this.cliente = socket;
			flujoSalidaObjeto = new ObjectOutputStream(socket.getOutputStream()); 
			flujoEntradaObjeto = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	//Tarea a realizar con el cliente
	@Override
	public void run() {
		
		hiloEscucha = new ServerEscuchaCliente();
		hiloEscucha.start();

	}
	
	
	//Escucha constantemente al cliente para recibir recibos en objetos EnvioSocket de clientes y gestionarlos
	class ServerEscuchaCliente extends Thread {
		@Override
		public void run() {
			
			while (continuaEscucha) {
				try {
					EnvioSocket reciboCliente = (EnvioSocket) flujoEntradaObjeto.readObject();
					
					//Gestion de peticiones
					gestionarRecibos(reciboCliente);
					
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
				}
				catch (IOException e) {
					continuaEscucha = false;
				}
			}
		}
		
		//Recibe un objeto EnvioSocket que contiene un tipo de recibo y objetos adjuntos.
		//A partir del tipo de recibo, se ejecuta una serie de casos en un switch para llamar a
		//diferentes métodos del controlador y enviar el resultado al cliente con un nuevo objeto EnvioSocket.
		//Si el tipo de recibo no coincide con ningún caso, se muestra un mensaje de error.
		public void gestionarRecibos(EnvioSocket recibo) {
			String tipoRecibo = recibo.getTipo();
			Map <String, Object> objetosAdjuntos = recibo.getObjetosAdjuntos();
			Controlador controlador = new Controlador();
			
			switch (tipoRecibo) {
				case "/registrarse":
					enviarObjeto(controlador.registrarse(objetosAdjuntos));
					break;
					
				case "/iniciarSesion":
					enviarObjeto(controlador.iniciarSesion(objetosAdjuntos));
					break;
					
				case "/cargarPrincipalAplicacion":
					controlador.comprobarActualizacionEstado();
					enviarObjeto(controlador.cargarPrincipalAplicacion(objetosAdjuntos));
					break;
					
				case "/cargarListaMiembrosGrupo":
					controlador.comprobarActualizacionEstado();
					enviarObjeto(controlador.cargarListaMiembrosGrupo(objetosAdjuntos));
					break;
					
				case "/cargarCalendarioGrupo":
					enviarObjeto(controlador.cargarCalendarioGrupo(objetosAdjuntos));
					break;
					
				case "/cargarNotificaciones":
					enviarObjeto(controlador.cargarNotificaciones(objetosAdjuntos));
					break;
					
				case "/anadirAsistencia":
					enviarObjeto(controlador.anadirAsistencia(objetosAdjuntos));
					break;
					
				case "/editarNombreUsuario":
					enviarObjeto(controlador.editarNombreUsuario(objetosAdjuntos));
					break;
					
				case "/editarCargo":
					enviarObjeto(controlador.editarCargo(objetosAdjuntos));
					break;
					
				case "/editarNombreGrupo":
					enviarObjeto(controlador.editarNombreGrupo(objetosAdjuntos));
					break;
					
				case "/editarDescripcionGrupo":
					enviarObjeto(controlador.editarDescripcionGrupo(objetosAdjuntos));
					break;
					
				case "/anadirGrupo":
					enviarObjeto(controlador.anadirGrupo(objetosAdjuntos));
					break;
					
				case "/anadirMiembroGrupo":
					controlador.comprobarActualizacionEstado();
					enviarObjeto(controlador.anadirMiembroGrupo(objetosAdjuntos));
					break;
					
				case "/eliminarGrupo":
					enviarObjeto(controlador.eliminarGrupo(objetosAdjuntos));
					break;
					
				case "/eliminarMiembroGrupo":
					enviarObjeto(controlador.eliminarMiembroGrupo(objetosAdjuntos));
					break;
					
				case "/anadirVacaciones":
					enviarObjeto(controlador.anadirVacaciones(objetosAdjuntos));
					break;
					
				case "/cargarCalendarioMiembro":
					enviarObjeto(controlador.cargarCalendarioMiembro(objetosAdjuntos));
					break;
					
				case "/anadirFestivo":
					enviarObjeto(controlador.anadirFestivo(objetosAdjuntos));
					break;
					
				default:
					System.err.println("ERROR!!! Recibo no reconocido --> " + tipoRecibo);
			}
		}
		
	}
	
	//Envia al cliente un objeto EnvioSocket
	public void enviarObjeto(EnvioSocket envioSocket) {
		try {
			flujoSalidaObjeto.writeObject(envioSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Cierra los flujos y sockets
	public void disconnect() {
		try {
			continuaEscucha = false;
			flujoSalidaObjeto.close();
			flujoEntradaObjeto.close();

			cliente.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("\n -- Cliente desconectado --");
	}
	
	
	public void setContinuaEscucha(boolean continuaEscucha) {
		this.continuaEscucha = continuaEscucha;
	}
	
}
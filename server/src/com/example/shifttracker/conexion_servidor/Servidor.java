package com.example.shifttracker.conexion_servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;


public class Servidor {
	
	//Atributos en el servidor
	private ServerSocket servidorSocket;
	private int puerto = 6126;
	private Map<String, ClienteHilo> clientesConectados = new TreeMap<String, ClienteHilo>();
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Servidor servidor = new Servidor();
		try {
			servidor.iniciarServidor();
			servidor.aceptarClientes(); //Acepta constantemente clientes
		}
		finally {
			servidor.desconectar();
		}
	}
	
	//Acepta y maneja la conexión de nuevos clientes de forma continua.
	//Cada vez que se establece una conexión, se crea un nuevo hilo para el cliente y se guarda en un mapa.
	//Si el cliente ya está conectado, se finaliza el hilo existente y se crea uno nuevo.
	public void aceptarClientes() {
		Socket clienteSocket = null;
		
		//Siempre se aceptan nuevos clientes
		while(true) {
			//Espera cliente
			try {
				clienteSocket = servidorSocket.accept();
				
				//Si el cliente ya se ha conectado, finaliza el hilo y vuelve a conectar
				if (clientesConectados.containsKey(clienteSocket.getInetAddress().toString())) {
					clientesConectados.get(clienteSocket.getInetAddress().toString()).setContinuaEscucha(false);
				}
				
				//Crea nuevo hilo jugador y lo inicia
				ClienteHilo clienteHilo = new ClienteHilo(clienteSocket); 
				clienteHilo.start();
				clientesConectados.put(clienteSocket.getInetAddress().toString(), clienteHilo);
				System.out.println("\nNuevo Cliente: " + clienteSocket.getInetAddress() + "/" + clienteSocket.getPort() + "\n");
				
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Desconecta el servidor y todos los clientes
	public void desconectar() {
		try {
			for (Entry<String, ClienteHilo> entrada : clientesConectados.entrySet()) {
				ClienteHilo clienteSocket = entrada.getValue();
				clienteSocket.disconnect();
			}
			servidorSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Inicia el servidor en el puerto asignado
	public void iniciarServidor() {
		try {
			servidorSocket = new ServerSocket(puerto);
			System.out.println(" --- Servidor iniciado ---");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package exampleUDP;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class UDPEchoServer implements Runnable{
	public static final int DEFAULT_PORT = 7;
	private final int bufferSize; //In bytes
	private final int port;
	private final Logger logger = Logger.getLogger(UDPEchoServer.class.getCanonicalName());
	private volatile boolean isShutDown = false;
	public UDPEchoServer (int port, int bufferSize) {
		this.bufferSize = bufferSize;
		this.port = port;
	}
	public UDPEchoServer (int port) {
		this(port, 8192);
	}
	
	public UDPEchoServer() {
		this(DEFAULT_PORT);
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[bufferSize];
		try (DatagramSocket socket = new DatagramSocket(port)) {
			socket.setSoTimeout(10000); //Check every ten seconds for shutdown
			while (true) {
				if (isShutDown) return;
				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
				try {
					socket.receive(incoming);
					this.respond(socket, incoming);
				} catch (SocketTimeoutException ex) {
					if (isShutDown) return;
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Could not bind to port: " + port, ex);
				}
					
				}
			} catch (SocketException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	
	public void shutDown() {
		this.isShutDown = true;
	}
	
	public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
		DatagramPacket outgoing = new DatagramPacket(packet.getData(), packet.getLength(), 
				packet.getAddress(), packet.getPort());
		socket.send(outgoing);	
	}
	public static void main (String[] args) {
		UDPEchoServer server= new UDPEchoServer();
		Thread t = new Thread(server);
		t.start();
	}
	}
	
	



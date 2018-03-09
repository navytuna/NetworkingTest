import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.sun.corba.se.spi.activation.Server;
import com.sun.security.ntlm.Client;

/**
 * The class {@code UsageExample} shows the usage of {@link Server} and {@link Client}. This examples uses
 * {@link Thread#sleep(long)} to ensure every segment is executed because quickly starting and closing causes some
 * segments to not execute.
 *
 * @version 1.0
 * @see Server
 * @see Client
 */
public class UsageExample {
	public static String host;
    public static int port;
    public static ServerSocket server;
    public static Socket client;
    public static Socket connection;
    public static DataOutputStream clientOut;
    public static DataInputStream clientIn;
    public static DataOutputStream serverOut;
    public static DataInputStream serverIn;
    public static String message;
	
	public static void main(String[] args) throws Exception {
        serverStartup();
        run();
        serverShutdown();
    }
	
	public static void serverStartup() throws Exception{
		host = "localhost";
        port = 10430;
		server = new ServerSocket(port, 50, InetAddress.getByName(host));
        System.out.println("Server started.");
        client = new Socket(host, port);
        System.out.println("Connecting to server...");
        connection = server.accept();
        System.out.println("Connection established.");
        clientOut = new DataOutputStream(client.getOutputStream());
        clientIn = new DataInputStream(client.getInputStream());
        serverOut = new DataOutputStream(connection.getOutputStream());
        serverIn = new DataInputStream(connection.getInputStream());
        System.out.println("Communication is ready.");
	}
	
	public static void run() throws Exception{
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.println("Input a message");
			if(sc.hasNextLine()) {message = sc.nextLine();}else {break;}
			byte[] messageOut = message.getBytes();
			clientOut.writeInt(messageOut.length);
			clientOut.write(messageOut);
			clientOut.flush();
			System.out.println("Message sent to server: " + new String(messageOut));
 
			serverHandling();
		}
		sc.close();
	}
	
	public static void serverShutdown() throws Exception{
		clientOut.close();
        serverOut.close();
        client.close();
        clientIn.close();
        System.out.println("Connections closed.");
        server.close();
        System.out.println("Server terminated.");
	}
	
	public static byte[] getBytes(Object obj) throws java.io.IOException{
	      ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	      ObjectOutputStream oos = new ObjectOutputStream(bos); 
	      oos.writeObject(obj);
	      oos.flush(); 
	      oos.close();
	      bos.flush();
	      bos.close();
	      byte [] data = bos.toByteArray();
	      return data;
	  }
	
	public static void serverHandling(){
		int length = serverIn.readInt();
		if (length > 0) {
			byte[] messageIn = new byte[length];
			serverIn.readFully(messageIn, 0, messageIn.length);
			System.out.println("Message received from client: " + new String(messageIn));
		}
	}
}

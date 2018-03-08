import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * The class {@code Server} represents a server end-point in a network. {@code Server} once bound to a certain IP
 * address and port, establishes connections with clients and is able to communicate with them or disconnect them.
 * 
 * This class is threadsafe.
 * @version 1.0
 * @see Client
 * @see Connection
 */

public class Server implements Runnable {
    private ServerSocket server;
    private List<Connection> connections;
    private Thread thread;
 
    private final Object connectionsLock = new Object();
 
    /**
     * Constructs a {@code Server} that interacts with clients on the specified host name and port with the specified
     * requested maximum length of a queue of incoming clients.
     *
     * @param host Host address to use.
     * @param port Port number to use.
     * @param backlog Requested maximum length of the queue of incoming clients.
     * @throws NetworkException If error occurs while starting a server.
     */
    public Server(String host, int port, int backlog) throws NetworkException {
        try {
            server = new ServerSocket(port, backlog, InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            throw new NetworkException("Host name could not be resolved: " + host, e);
        } catch (IllegalArgumentException e) {
            throw new NetworkException("Port number needs to be between 0 and 65535 (inclusive): " + port);
        } catch (IOException e) {
            throw new NetworkException("Server could not be started.", e);
        }
        connections = Collections.synchronizedList(new ArrayList<>());
        thread = new Thread(this);
        thread.start();
    }
 
    /**
     * Constructs a {@code Server} that interacts with clients on the specified host name and port.
     *
     * @param host Host address to bind.
     * @param port Port number to bind.
     * @throws NetworkException If errors occurs while starting a server.
     */
    public Server(String host, int port) throws NetworkException {
        this(host, port, 50);
    }
 
    /**
     * Listens for, accepts and registers incoming connections from clients.
     */
    @Override
    public void run() {
        while (!server.isClosed()) {
            try {
                connections.add(new Connection(server.accept()));
            } catch (SocketException e) {
                if (!e.getMessage().equals("Socket closed")) {
                    e.printStackTrace();
                }
            } catch (NetworkException | IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    /**
     * Sends data to all registered clients.
     *
     * @param data Data to send.
     * @throws IllegalStateException If writing data is attempted when server is offline.
     * @throws IllegalArgumentException If data to send is null.
     */
    public void broadcast(Object data) {
        if (server.isClosed()) {
            throw new IllegalStateException("Data not sent, server is offline.");
        }
        if (data == null) {
            throw new IllegalArgumentException("null data");
        }
 
        synchronized (connectionsLock) {
            for (Connection connection : connections) {
                try {
                    connection.send(data);
                    System.out.println("Data sent to client successfully.");
                } catch (NetworkException e) {
                    e.printStackTrace();
                }
            }
        }
    }
 
    /**
     * Sends a disconnection message and disconnects specified client.
     *
     * @param connection Client to disconnect.
     * @throws NetworkException If error occurs while closing connection.
     */
    public void disconnect(Connection connection) throws NetworkException {
        if (connections.remove(connection)) {
            connection.close();
        }
    }
 
    /**
     * Sends a disconnection message to all clients, disconnects them and terminates the server.
     */
    public void close() throws NetworkException {
        synchronized (connectionsLock) {
            for (Connection connection : connections) {
                try {
                    connection.close();
                } catch (NetworkException e) {
                    e.printStackTrace();
                }
            }
        }
        connections.clear();
 
        try {
            server.close();
        } catch (IOException e) {
            throw new NetworkException("Error while closing server.");
        } finally {
            thread.interrupt();
        }
    }
 
    /**
     * Returns whether or not the server is online.
     *
     * @return True if server is online. False, otherwise.
     */
    public boolean isOnline() {
        return !server.isClosed();
    }
 
    /**
     * Returns an array of registered clients.
     */
    public Connection[] getConnections() {
        synchronized (connectionsLock) {
            return connections.toArray(new Connection[connections.size()]);
        }
    }
}
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
 
/**
 * The class {@code Client} represents a client end-point in a network. {@code Client}, once connected to a certain
 * server, is guaranteed to only be able to communicate with the server. Whether or not other clients receive the data
 * depends on the server implementation.
 * <br><br>
 * This class is threadsafe.
 *
 * @version 1.0
 * @see Server
 * @see Connection
 */
public class Client {
    private Connection connection;
 
    /**
     * Constructs a {@code Client} connected to the server on the specified host and port.
     *
     * @param host Host address to bind.
     * @param port Port number to bind.
     * @throws NetworkException If error occurs while starting a server.
     */
    public Client(String host, int port) throws NetworkException {
        try {
            connection = new Connection(new Socket(host, port));
        } catch (UnknownHostException e) {
            throw new NetworkException("Host name could not be resolved: " + host, e);
        } catch (IllegalArgumentException e) {
            throw new NetworkException("Port number needs to be between 0 and 65535 (inclusive): " + port);
        } catch (IOException e) {
            throw new NetworkException("Server could not be started.", e);
        }
    }
 
    /**
     * Sends data to the other party.
     *
     * @param data Data to send.
     * @throws NetworkException If writing to output stream fails.
     * @throws IllegalStateException If writing data is attempted when connection is closed.
     * @throws IllegalArgumentException If data to send is null.
     * @throws UnsupportedOperationException If unsupported data type is attempted to be sent.
     */
    public void send(Object data) throws NetworkException {
        connection.send(data);
    }
 
    /**
     * Sends a disconnection message to, and closes connection with, the server.
     */
    public void close() throws NetworkException {
        connection.close();
    }
 
    /**
     * Returns whether or not the client is connected to the server.
     *
     * @return True if client is connected. False, otherwise.
     */
    public boolean isOnline() {
        return connection.isConnected();
    }
 
    /**
     * Returns the {@link Connection} instance of the client.
     */
    public Connection getConnection() {
        return connection;
    }
}
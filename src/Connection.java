import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


public class Connection implements Runnable {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread thread;
 
    private final Object writeLock = new Object();
    private final Object readLock = new Object();
 
    /**
     * Constructs {@code Connection} using streams of a specified {@link Socket}.
     *
     * @param socket Socket to fetch the streams from.
     */
    public Connection(Socket socket) throws NetworkException {
        if (socket == null) {
            throw new IllegalArgumentException("null socket");
        }
 
        this.socket = socket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new NetworkException("Could not access output stream.", e);
        }
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new NetworkException("Could not access input stream.", e);
        }
        thread = new Thread(this);
        thread.start();
    }
 
    /**
     * Reads messages while connection with the other party is alive.
     */
    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                int identifier;
                byte[] bytes;
                synchronized (readLock) {
                    identifier = in.readInt();
                    int length = in.readInt();
                    if (length > 0) {
                        bytes = new byte[length];
                        in.readFully(bytes, 0, bytes.length);
                    } else {
                        continue;
                    }
                }
                switch (identifier) {
                    case Identifier.INTERNAL:
                        String command = new String(bytes);
                        if (command.equals("disconnect")) {
                            if (!socket.isClosed()) {
                                System.out.println("Disconnection packet received.");
                                try {
                                    close();
                                } catch (NetworkException e) {
                                    return;
                                }
                            }
                        }
                        break;
                    case Identifier.TEXT:
                        System.out.println("Message received: " + new String(bytes));
                        break;
                    default:
                        System.out.println("Unrecognized data received.");
                }
            } catch (SocketException e) {
                if (!e.getMessage().equals("Socket closed")) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (socket.isClosed()) {
            throw new IllegalStateException("Data not sent, connection is closed.");
        }
        if (data == null) {
            throw new IllegalArgumentException("null data");
        }
 
        int identifier;
        byte[] bytes;
        if (data instanceof String) {
            identifier = Identifier.TEXT;
            bytes = ((String) data).getBytes();
        } else {
            throw new UnsupportedOperationException("Unsupported data type: " + data.getClass());
        }
        try {
            synchronized (writeLock) {
                out.writeInt(identifier);
                out.writeInt(bytes.length);
                out.write(bytes);
                out.flush();
            }
        } catch (IOException e) {
            throw new NetworkException("Data could not be sent.", e);
        }
    }
 
    /**
     * Sends a disconnection message to, and closes connection with, the other party.
     */
    public void close() throws NetworkException {
        if (socket.isClosed()) {
            throw new IllegalStateException("Connection is already closed.");
        }
 
        try {
            byte[] message = "disconnect".getBytes();
            synchronized (writeLock) {
                out.writeInt(Identifier.INTERNAL);
                out.writeInt(message.length);
                out.write(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Disconnection message could not be sent.");
        }
 
        try {
            synchronized (writeLock) {
                out.close();
            }
        } catch (IOException e) {
            throw new NetworkException("Error while closing connection.", e);
        } finally {
            thread.interrupt();
        }
    }
 
    /**
     * Returns whether or not the connection to the other party is alive.
     *
     * @return True if connection is alive. False, otherwise.
     */
    public boolean isConnected() {
        return !socket.isClosed();
    }
}
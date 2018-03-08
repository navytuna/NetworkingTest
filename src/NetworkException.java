/**
 * The class {@code NetworkException} indicates an error related to network.
 */
public class NetworkException extends Exception {
    /**
     * Constructs a {@code NetworkException} with {@code null} as its message.
     */
    public NetworkException() {
    }
 
    /**
     * Constructs a {@code NetworkException} with the specified message.
     *
     * @param message A message to describe error.
     */
    public NetworkException(String message) {
        super(message);
    }
 
    /**
     * Constructs a {@code NetworkException} with the specified message and cause.
     *
     * @param message A message to describe error.
     * @param cause A cause of error.
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
 
    /**
     * Constructs a {@code NetworkException} with the specified cause.
     *
     * @param cause A cause of error.
     */
    public NetworkException(Throwable cause) {
        super(cause);
    }
}
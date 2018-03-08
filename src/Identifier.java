/**
 * The class {@code Identifier} contains constants used by {@link Connection} for serializing and deserializing the data
 * sent over the network.
 *
 * @version 1.0
 * @see Connection
 */
public final class Identifier {
    /**
     * Identifier for internal messages.
     */
    public static final int INTERNAL = 1;
    /**
     * Identifier for textual messages.
     */
    public static final int TEXT = 2;
}
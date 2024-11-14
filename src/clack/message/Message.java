package clack.message;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Abstract base class for Messages exchanged between Clack
 * clients and servers. The base class keeps track of the user
 * that created the message (username), the type of the message
 * (a value from MsgTypeEnum), and a timestamp (an Instant given
 * by "now()" at the moment a message is created).
 */
public class Message implements Serializable {
    private final MsgTypeEnum msgTypeEnum;
    private final Instant timestamp;
    private final String username;

    /**
     * Initializes the base fields of a Message object. The
     * timestamp is not a parameter -- it is given by a call
     * to Instant.now().
     *
     * @param username name of user who created this message.
     * @param msgTypeEnum the type of this message (MsgTypeEnum value).
     */
    public Message(String username, MsgTypeEnum msgTypeEnum) {
        this.msgTypeEnum = msgTypeEnum;
        this.timestamp = Instant.now();
        this.username = username;
    }

    /**
     * Gets the MsgTypeEnum of a Message object.
     *
     * @return the MsgTypeEnum of this Message.
     */
    public MsgTypeEnum getMsgType() {
        return msgTypeEnum;
    }

    /**
     * Gets the timestamp of this Message.
     *
     * @return an Instant denoting the time of this
     * Message's creation.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the name of the user that created this Message.
     *
     * @return the name of the user that created this Message.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns a string representation of this Message.
     *
     * @return a string representation of this Message.
     */
    @Override
    public String toString() {
        return "Message{" +
                "msgTypeEnum=" + msgTypeEnum +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                '}';
    }

    /**
     * Determines if this Message is equal to some other value.
     * Note: it is unlikely that two Message objects (or
     * objects of subclasses of Message) will ever test equal,
     * given the precision of the timestamp fields.
     *
     * @param o the other value.
     * @return true iff o is of the same type as this Message,
     *  and all fields are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return msgTypeEnum == message.msgTypeEnum
                && Objects.equals(timestamp, message.timestamp)
                && Objects.equals(username, message.username);
    }

    /**
     * Returns the hashCode of this Message object.
     *
     * @return the hashCode of this Message.
     */
    @Override
    public int hashCode() {
        return Objects.hash(msgTypeEnum, timestamp, username);
    }
}

package clack.message;

/**
 * Class that signifies the user wishes to log out. Objects of
 * this class do not carry any fields beyond that of the Message
 * base class.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class LogoutMessage extends Message {
    /**
     * Constructs a LogoutMessage object.
     *
     * @param username name of user who created this message.
     */
    public LogoutMessage(String username) {
        super(username, MsgTypeEnum.LOGOUT);
    }

    /**
     * Returns a string representation of this LogoutMessage.
     *
     * @return a string representation of this LogoutMessage.
     */
    @Override
    public String toString() {
        return "LogoutMessage{" + super.toString() + "}";
    }
}

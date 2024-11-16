package clack.message;

/**
 * Class that signifies the user wishes help information. Objects of
 * this class do not carry any fields beyond that of the Message
 * base class.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class HelpMessage extends Message {

    /**
     * Constructs a Help message.
     *
     * @param username name of user who created this message.
     */
    public HelpMessage(String username) {
        super(username, MsgTypeEnum.HELP);
    }

    /**
     * Returns a string representation of this HelpMessage.
     *
     * @return a string representation of this HelpMessage.
     */
    @Override
    public String toString() {
        return "HelpMessage{" + super.toString() + "}";
    }
}

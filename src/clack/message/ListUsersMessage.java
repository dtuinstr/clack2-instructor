package clack.message;

/**
 * Class that signifies the user wishes a list of all other logged-in
 * users. Objects of this class do not carry any fields beyond that
 * of the Message base class.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class ListUsersMessage extends Message {
    /**
     * Constructs a ListUsers message.
     *
     * @param username name of user who created this message.
     */
    public ListUsersMessage(String username) {
        super(username, MsgTypeEnum.LISTUSERS);
    }

    /**
     * Returns a string representation of this ListUsersMessage.
     *
     * @return a string representation of this ListUsersMessage.
     */
    @Override
    public String toString() {
        return "ListUsersMessage{" + super.toString() + "}";
    }
}

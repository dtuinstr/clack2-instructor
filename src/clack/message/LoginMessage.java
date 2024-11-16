package clack.message;

/**
 * Class that signifies the user wishes to log in. Objects of
 * this class carry an additional password field.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class LoginMessage extends Message {

    private final String password;

    /**
     * Constructs a LoginMessage object.
     *
     * @param username name of user who created this message.
     * @param password user-supplied password.
     */
    public LoginMessage(String username, String password) {
        super(username, MsgTypeEnum.LOGIN);
        this.password = password;
    }

    /**
     * Returns the password.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns a string representation of this LoginMessage.
     *
     * @return a string representation of this LoginMessage.
     */
    @Override
    public String toString() {
        return "LoginMessage{" + super.toString()
                + ", password='" + "*".repeat(password.length())
                + "'}";
    }
}

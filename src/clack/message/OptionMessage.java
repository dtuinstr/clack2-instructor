package clack.message;

/**
 * Class that signifies the user wishes to set an option value, or
 * query the current value of an option. Objects of this class carry
 * an indication of which option is at issue, and what that option's
 * new value should be. The option at issue is indicated with an
 * OptionEnum.
 * <p>
 * Values of options are carried as Strings, but the server
 * may convert them to a more suitable type when storing them (for
 * example, a CIPHER_ENABLE value might be stored as a boolean). The
 * server should reply to such an OptionMessage by setting the option
 * to the new value, then sending the client a TextMessage verifying
 * the new value has been set.
 * <p>
 * If a client sends an OptionMessage with an empty string, or null,
 * as the value for an option, the server should take this as a query
 * for the current value of the option, and send the client a
 * TextMessage with the value.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class OptionMessage extends Message {
    private final OptionEnum option;
    private final String value;

    /**
     * Constructs an OptionMessage.
     *
     * @param username name of user who created this message.
     * @param option   the option to set or query.
     * @param value    if empty or null, an indication that this
     *                 message is requesting the current option
     *                 value; otherwise the new value for the option.
     */
    public OptionMessage(String username, OptionEnum option,
                         String value) {
        super(username, MsgTypeEnum.OPTION);
        this.option = option;
        this.value = value;
    }

    /**
     * Returns the message's option.
     *
     * @return the message's option.
     */
    public OptionEnum getOption() {
        return option;
    }

    /**
     * Returns the setting for the option (which may be null).
     *
     * @return the setting for the option.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "OptionMessage{"
                + super.toString()
                + ", option=" + option
                + ", value='" + value + '\''
                + "}";
    }
}

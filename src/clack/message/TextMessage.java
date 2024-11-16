package clack.message;

/**
 * Class that carries a text string. Objects of this class carry a
 * field containing the text.
 * <p>
 * This class does not override equals() and hashCode(). Given the
 * precision of the timestamp it is unlikely that any two objects of
 * any Message subclass will ever test equal, or have equal hashCodes.
 */
public class TextMessage extends Message {
    private final String text;

    /**
     * Constructs a TextMessage.
     *
     * @param username name of user who created this message.
     */
    public TextMessage(String username, String text) {
        super(username, MsgTypeEnum.TEXT);
        this.text = text;
    }

    /**
     * Returns the text of the message.
     *
     * @return the text of the message.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns this TextMessage as a string.
     *
     * @return this TextMessage as a string.
     */
    @Override
    public String toString() {
        return "TextMessage{"
                + super.toString()
                + ", text='" + text + "'"
                + "}";
    }
}

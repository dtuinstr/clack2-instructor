package clack.endpoint;

import clack.cipher.CharacterCipher;
import clack.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * This is a simple server class for exchanging Message objects
 * with a client. The exchange is conversational, that is, one
 * side sends a Message, then waits for a reply Message from the
 * other side, then sends another Message, waits for a reply,
 * and so on.
 * <p>
 * To begin a conversation, a client connects to the server
 * and waits for the server to send the first Message.
 * <p>
 * The conversation ends when the client sends a LogoutMessage.
 * The server replies with a last TextMessage, closes the
 * connection, and waits for a new connection.
 */
public class Server
{
    public static final String DEFAULT_SERVERNAME = "server";

    // For strings sent to client.
    private static final String GREETING =
            "Server listening. Type 'login <password>' to continue.";
    private static final String LOGIN_BAD =
            "Invalid username/password. Type 'login <password>' to continue.";
    private static final String LOGIN_OK =
            "Login successful. 'logout' to exit, 'help' for help.";
    private static final String GOOD_BYE =
            "Server closing connection, good-bye.";
    private static final String HELP_STR =  """
            HELP
            LIST USERS
            LOGIN
            LOGOUT
            OPTION optionName {optionSetting}
            SEND FILE filepath {AS filename}
            
            Any other entry is sent as a text message.
            Commands are case-insensitive. {...} denotes optional portion.""";

    // Object variables.
    private final int port;
    private final String serverName;
    private final boolean SHOW_TRAFFIC = true;      // FOR DEBUGGING
    private final Properties options;
    private CharacterCipher cipher = null;
    private boolean cipherEnabled = false;

    /**
     * Creates a server for exchanging Message objects.
     *
     * @param port       the port to listen on.
     * @param serverName the name to use when constructing Message objects.
     * @throws IllegalArgumentException if port not in range [1024, 49151].
     */
    public Server(int port, String serverName)
            throws IllegalArgumentException
    {
        if (port < 1024 || port > 49151) {
            throw new IllegalArgumentException("Port " + port
                    + " not in range 1024-49151.");
        }
        this.port = port;
        this.serverName = serverName;
        this.options = new Properties();
        for (OptionEnum oe : OptionEnum.values()) {
            options.setProperty(oe.toString(), "");
        }
    }

    /**
     * Creates a server for exchanging Message objects, using the
     * default servername (Server.DEFAULT_SERVERNAME).
     *
     * @param port the port to listen on.
     * @throws IllegalArgumentException if port not in range [1024, 49151].
     */
    public Server(int port)
    {
        this(port, DEFAULT_SERVERNAME);
    }

    /**
     * Checks password for validity for a given username.
     *
     * @param password the password to check.
     * @return true iff password is valid for the username.
     */
    private boolean passwordValid(String username, String password)
    {
        StringBuilder sb = new StringBuilder(password);
        String pwReverse = new String(sb.reverse());
        return pwReverse.equals(username);
    }

    /**
     * Starts this server, listening on the port it was
     * constructed with.
     *
     * @throws IOException if ServerSocket creation, connection
     *                     acceptance, wrapping, or IO fails.
     */
    public void start()
            throws IOException, ClassNotFoundException
    {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server starting on port " + port + ".");
            System.out.println("Ctrl + C to exit.");
            while (true) {
                try (   // Wait for connection, then build streams.
                        Socket clientSocket = serverSocket.accept();
                        ObjectInputStream inObj =
                                new ObjectInputStream(clientSocket.getInputStream());
                        ObjectOutputStream outObj =
                                new ObjectOutputStream(clientSocket.getOutputStream());
                ) {
                    // Connection made. Request login.
                    userLogin(inObj, outObj);

                    // Login successful. Converse with client.
                    converse(inObj, outObj);

                    // They asked to log out, and our last part of
                    // the conversation was a good-bye message.
                    inObj.close();
                    outObj.close();
                    clientSocket.close();

                    System.out.println("=== Terminating connection. ===");
                }   // Streams and socket ensured closed by try-with-resources.
            }   // end while(true)
        }   // Server socket closed by try-with-resources.
    }


    /**
     * Repeatedly prompt user for password, returns only when
     * user sends a LoginMessage with valid username and password.
     * This acts as a gatekeeper to the main conversation.
     *
     * @param inObj ObjectInputStream from which to read Messages.
     * @param outObj ObjectOutputStream on which our replies are sent.
     * @throws IOException if stream read/write fails.
     * @throws ClassNotFoundException if inObj produces an object of
     * unknown class.
     */
    private void userLogin(ObjectInputStream inObj, ObjectOutputStream outObj)
            throws IOException, ClassNotFoundException
    {
        Message inMsg, outMsg;

        outMsg = new TextMessage(serverName, GREETING);
        outObj.writeObject(outMsg);
        outObj.flush();
        while (true) {
            if (SHOW_TRAFFIC) {
                System.out.println("=> " + outMsg);
            }
            inMsg = (Message) inObj.readObject();
            if (inMsg.getMsgType() == MsgTypeEnum.LOGIN) {
                LoginMessage msg = (LoginMessage) inMsg;
                if (passwordValid(msg.getUsername(), msg.getPassword())) {
                    // only way out of this method.
                    outMsg = new TextMessage(serverName, LOGIN_OK);
                    outObj.writeObject(outMsg);
                    outObj.flush();
                    return;
                }
            }
            outMsg = new TextMessage(serverName, LOGIN_BAD);
            outObj.writeObject(outMsg);
            outMsg = new TextMessage(serverName, GREETING);
            outObj.flush();
        }
    }

    /**
     * Converse with a logged-in user.
     * @param inObj ObjectInputStream to read Messages from.
     * @param outObj ObjectOutputStream to write Message to.
     */
    private void converse(ObjectInputStream inObj, ObjectOutputStream outObj)
            throws IOException, ClassNotFoundException
    {
        Message inMsg, outMsg;
        do {
            // Read a message
            inMsg = (Message) inObj.readObject();
            if (SHOW_TRAFFIC) {
                System.out.println("<= " + inMsg);
            }

            // Process the received message
            outMsg = switch (inMsg.getMsgType()) {
                case FILE -> inMsg;     // just turn the FileMessage around.
                case HELP -> new TextMessage(serverName, HELP_STR);
                case LISTUSERS -> new TextMessage(serverName, "LISTUSERS requested");
                case LOGIN -> new TextMessage(serverName, "Already logged in.");
                case LOGOUT -> new TextMessage(serverName, GOOD_BYE);
                case OPTION -> handleOptionMsg((OptionMessage) inMsg);
                case TEXT -> new TextMessage(serverName, "TEXT: '"
                        + ((TextMessage) inMsg).getText() + "'");
            };

            outObj.writeObject(outMsg);
            outObj.flush();
            if (SHOW_TRAFFIC) {
                System.out.println("=> " + outMsg);
            }
        } while (inMsg.getMsgType() != MsgTypeEnum.LOGOUT);
    }

    /**
     * Queries or sets an option. If getValue() is null or empty,
     * returns a TextMessage showing the current value of the
     * requested option (the option is given by getOption()).
     * If getValue() is anything else, sets the requested option
     * to that value and returns a TextMessage showing the
     * option's new value.
     *
     * @param oMsg an OptionMessage.
     * @return a TextMessage showing the option's setting at
     * the moment this method returns.
     */
    private TextMessage handleOptionMsg(OptionMessage oMsg)
    {
        OptionEnum opt = oMsg.getOption();
        String optStr = opt.toString();
        String val = oMsg.getValue();

        if (val != null) {
            options.setProperty(optStr, val);
            resetCipher();
        }

        String reply = "option " + optStr
                + ": '" + options.getProperty(optStr) + "'";

        return new TextMessage(serverName, reply);
    }

    // TODO Implement this.
    private void resetCipher()
    {

    }
}

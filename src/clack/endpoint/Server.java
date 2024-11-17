package clack.endpoint;

import clack.cipher.CharacterCipher;
import clack.message.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static clack.message.OptionEnum.CIPHER_ENABLE;
import static clack.message.OptionEnum.CIPHER_NAME;

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
            "Invalid username/password.";
    private static final String LOGIN_OK =
            "Login successful. 'logout' to exit, 'help' for help.";
    private static final String GOOD_BYE =
            "Server closing connection, good-bye.";
    private static final String HELP_STR =      // TODO fill in HELP_STR
            "This is the Help.";

    // Object variables.
    private final int port;
    private final String serverName;
    private final boolean SHOW_TRAFFIC = true;      // FOR DEBUGGING
    private String optionCipherKey = null;
    private String optionCipherName = null;
    private String optionCipherEnable = null;
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
            throw new IllegalArgumentException(
                    "Port " + port + " not in range 1024-49151.");
        }
        this.port = port;
        this.serverName = serverName;
    }

    /**
     * Creates a server for exchanging Message objects, using the
     * default servername (Server.DEFAULT_SERVERNAME).
     *
     * @param port       the port to listen on.
     * @throws IllegalArgumentException if port not in range [1024, 49151].
     */
    public Server(int port) {
        this(port, DEFAULT_SERVERNAME);
    }

    /**
     * Checks password for validity for a given username.
     *
     * @param password the password to check.
     * @return true iff password is valid for the username.
     */
    private boolean passwordValid(String username, String password) {
       StringBuffer sb = new StringBuffer(password);
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
    public void start() throws IOException, ClassNotFoundException
    {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server starting on port " + port + ".");
            System.out.println("Ctrl + C to exit.");
            while(true) {
                try (
                        // Wait for connection.
                        Socket clientSocket = serverSocket.accept();

                        // Build streams on the socket.
                        ObjectInputStream inObj =
                                new ObjectInputStream(clientSocket.getInputStream());
                        ObjectOutputStream outObj =
                                new ObjectOutputStream(clientSocket.getOutputStream());
                ) {
                    Message inMsg;
                    Message outMsg;

                    // Connection made. Request login.
                    String password;
                    do {
                        outMsg = new TextMessage(serverName, GREETING);
                        outObj.writeObject(outMsg);
                        outObj.flush();
                        if (SHOW_TRAFFIC) {
                            System.out.println("=> " + outMsg);
                        }
                        inMsg = (Message) inObj.readObject();
                        if (inMsg.getMsgType() != MsgTypeEnum.LOGIN) {
                            outMsg = new TextMessage(serverName, LOGIN_BAD);
                            outObj.writeObject(outMsg);
                            outObj.flush();
                            break;
                        }
                    } while (passwordValid(inMsg.getUsername(),
                            ((LoginMessage) inMsg).getPassword()));
                    outMsg = new TextMessage(serverName,LOGIN_OK);
                    outObj.writeObject(outMsg);
                    outObj.flush();

                    // Login successful. Converse with client.
                    do {
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
                            case TEXT -> new TextMessage(serverName,
                                    "TEXT: '" + ((TextMessage) inMsg).getText() + "'");
                        };

                        outObj.writeObject(outMsg);
                        outObj.flush();
                        if (SHOW_TRAFFIC) {
                            System.out.println("=> " + outMsg);
                        }
                    } while (inMsg.getMsgType() != MsgTypeEnum.LOGOUT);

                    System.out.println("=== Terminating connection. ===");
                }   // Streams and socket closed by try-with-resources.
            }   // end while(true)
        }   // Server socket closed by try-with-resources.
    }

    /**
     * Set or query an option. If an option is set, call a
     * method to change the state of the server to meet the
     * option settings. A query is indicated by an OptionMessage
     * with a null or empty
     * @param oMsg an OptionMessage with a query or new setting.
     * @return
     */
    private TextMessage handleOptionMsg(OptionMessage oMsg) {
        OptionEnum opt = oMsg.getOption();
        String val = oMsg.getValue();
        String reply = null;

        reply = switch (opt) {
            case CIPHER_KEY -> {
                if (!val.isEmpty()) {
                    optionCipherKey = val;
                    reCipher();
                }
                yield "option CIPHER_KEY: '" + optionCipherKey + "'";
            }
            case CIPHER_NAME -> {
                if (!val.isEmpty()) {
                    optionCipherName = val;
                    reCipher();
                }
                yield "option CIPHER_NAME: '" + optionCipherName + "'";
            }
            case CIPHER_ENABLE -> {
                if (!val.isEmpty()) {
                    optionCipherEnable = val;
                    reCipher();
                }
                yield "option CIPHER_ENABLE: '" + optionCipherEnable + "'";
            }
        };  // end switch

        return new TextMessage(serverName, reply);
    }

    // TODO Implement this.
    private void reCipher() {

    }
}

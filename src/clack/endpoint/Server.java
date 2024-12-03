package clack.endpoint;

import clack.cipher.CipherManager;
import clack.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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
    private static final String HELP_STR = """
            HELP
            LIST USERS
            LOGIN
            LOGOUT
            OPTION CIPHER_ENABLE { TRUE | YES | ON | 1 | FALSE | NO | OFF | 0 }
            OPTION CIPHER_KEY    { new_key }
            OPTION CIPHER_NAME   { CAESAR_CIPHER | NULL_CIPHER | PLAYFAIR_CIPHER
                                   | PSEUDO_ONE_TIME_PAD | VIGNERE_CIPHER }
            SEND FILE filepath {AS filename}
            
            Any other entry is sent as a text message.
            Commands are case-insensitive. {...} denotes optional portion.""";

    // Object variables.
    private final int port;
    private final String serverName;
    private final boolean SHOW_TRAFFIC = true;      // FOR DEBUGGING
    private final CipherManager cipherMgr;

    /**
     * Creates a server for exchanging Message objects.
     *
     * @param port       the port to listen on.
     * @param serverName the name to use when constructing Message objects.
     * @throws IllegalArgumentException if port not in range [1024, 49151].
     * @throws NoSuchAlgorithmException if cipher is PseudoOneTimePad and
     *                                  the SHA1PRNG implementation cannot be found.
     */
    public Server(int port, String serverName)
            throws IllegalArgumentException, NoSuchAlgorithmException
    {
        if (port < 1024 || port > 49151) {
            throw new IllegalArgumentException("Port " + port
                    + " not in range 1024-49151.");
        }
        this.port = port;
        this.serverName = serverName;
        this.cipherMgr = new CipherManager();
    }

    /**
     * Creates a server for exchanging Message objects, using the
     * default servername (Server.DEFAULT_SERVERNAME).
     *
     * @param port the port to listen on.
     * @throws IllegalArgumentException if port not in range [1024, 49151].
     * @throws NoSuchAlgorithmException if cipher is PseudoOneTimePad and
     *                                  the SHA1PRNG implementation cannot be found.
     */
    public Server(int port)
            throws NoSuchAlgorithmException
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
                Socket clientSocket = serverSocket.accept();
                System.out.println("Opening session on port "
                        + clientSocket.getPort());
                Thread thread = new Thread(() ->
                        session(clientSocket));
                thread.start();
            }   // end while(true)
        }   // Server socket closed by try-with-resources.
    }

    /**
     * Encapsulate a clack session, from stream creation to use logout.
     * @param socket the socket to use to create streams
     */
    private void session(Socket socket)
    {
        int port = socket.getPort();
        System.out.println("=== Opening session on port " + port + " ===");

        try (
                ObjectOutputStream outObj =
                        new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inObj =
                        new ObjectInputStream(socket.getInputStream());
        ) {
            userLogin(inObj, outObj);
            converse(inObj, outObj);
            System.out.println("=== Terminating session on port "
                    + port
                    + " ===");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("=== ABNORMAL TERMINATION on port "
                    + port
                    + " ===\n"
                    + e);
        } // Object streams closed by try-with-resources.
    }

/**
     * Repeatedly prompt user for password, returns only when
     * user sends a LoginMessage with valid username and password.
     * This acts as a gatekeeper to the main conversation.
     *
     * @param inObj  ObjectInputStream from which to read Messages.
     * @param outObj ObjectOutputStream on which our replies are sent.
     * @throws IOException            if stream read/write fails.
     * @throws ClassNotFoundException if inObj produces an object of
     *                                unknown class.
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
     *
     * @param inObj  ObjectInputStream to read Messages from.
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
            try {
                outMsg = switch (inMsg.getMsgType()) {
                    case FILE -> inMsg;     // just turn the FileMessage around.
                    case HELP -> new TextMessage(serverName, HELP_STR);
                    case LISTUSERS -> new TextMessage(serverName, "LISTUSERS requested");
                    case LOGIN -> new TextMessage(serverName, "Already logged in.");
                    case LOGOUT -> new TextMessage(serverName, GOOD_BYE);
                    case OPTION -> new TextMessage(serverName,
                            cipherMgr.process((OptionMessage) inMsg));
                    case TEXT -> new TextMessage(serverName, "TEXT: '"
                            + ((TextMessage) inMsg).getText() + "'");
                };
            } catch (Exception e) {
                outMsg = new TextMessage(serverName, "ERROR: " + e.getMessage());
            }

            outObj.writeObject(outMsg);
            outObj.flush();
            if (SHOW_TRAFFIC) {
                System.out.println("=> " + outMsg);
            }
        } while (inMsg.getMsgType() != MsgTypeEnum.LOGOUT);
    }
}

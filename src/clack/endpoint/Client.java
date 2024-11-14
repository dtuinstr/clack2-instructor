package clack.endpoint;

import clack.message.*;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This is a simple client class for exchanging Message objects
 * with a server. The exchange is conversational, that is, one
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
public class Client {
    public static final String DEFAULT_USERNAME = "client";

    private final String hostname;
    private final int port;
    private final String prompt;
    private final String username;

    /**
     * Creates a client for exchanging Message objects.
     *
     * @param hostname the hostname of the server.
     * @param port     the service's port on the server.
     * @param username username to include in Messages.
     * @throws IllegalArgumentException if port not in range [1-49151]
     */
    public Client(String hostname, int port, String username) {
        if (port < 1 || port > 49151) {
            throw new IllegalArgumentException(
                    "Port " + port + " not in range 1 - 49151.");
        }
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.prompt = "hostname:" + port + "> ";
    }

    /**
     * Creates a client for exchanging Message objects, using the
     * default username (Client.DEFAULT_USERNAME).
     *
     * @param hostname the hostname of the server.
     * @param port     the service's port on the server.
     * @throws IllegalArgumentException if port not in range [1-49151]
     */
    public Client(String hostname, int port) {
        this(hostname, port, DEFAULT_USERNAME);
    }

    /**
     * Starts this client, connecting to the server and port that
     * it was given when constructed.
     *
     * @throws UnknownHostException if hostname is not resolvable.
     * @throws IOException          if socket creation, wrapping, or IO fails.
     */
    public void start() throws UnknownHostException, IOException, ClassNotFoundException {
        System.out.println("Attempting connection to " + hostname + ":" + port);
        Scanner keyboard = new Scanner(System.in);

        try (
                Socket socket = new Socket(hostname, port);
                ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outObj = new ObjectOutputStream(socket.getOutputStream());
        ) {
            String userInput;
            Message inMsg;
            Message outMsg;

            // Take turns talking. Server goes first.
            do {
                // Get server message and show it to user.
                inMsg = (Message) inObj.readObject();
                System.out.println(switch (inMsg.getMsgType()) {
                    case FILE -> "FILE message received:" + inMsg;
                    case TEXT -> ((TextMessage) inMsg).getText();
                    default -> "UNEXPECTED MESSAGE: " + inMsg;
                });

                // Get user input
                String[] tokens;
                do {
                    System.out.print(prompt);
                    userInput = keyboard.nextLine();
                    tokens = userInput.trim().split("\\s+");
                } while (tokens.length == 0);
                // DEBUG
                // System.out.println("tokens: " + Arrays.toString(tokens));

                // Construct Message based on user input and send it to server.
                switch (tokens[0].toUpperCase()) {
                    case "SEND" -> {
                        if (tokens.length == 3
                                && tokens[1].equalsIgnoreCase("FILE")) {
                            outMsg = new FileMessage(username, tokens[2], tokens[2]);
                        } else if (tokens.length == 5
                                && tokens[1].equalsIgnoreCase("FILE")
                                && tokens[3].equalsIgnoreCase("AS")) {
                            outMsg = new FileMessage(username, tokens[2], tokens[4]);
                        } else {
                            outMsg = new TextMessage(username, userInput);
                        }
                    }
                    case "HELP" -> outMsg = new HelpMessage(username);
                    case "LIST" -> {
                        if (tokens.length > 1
                                && tokens[1].equalsIgnoreCase("USERS")) {
                            outMsg = new ListUsersMessage(username);
                        } else {
                            outMsg = new TextMessage(username, userInput);
                        }
                    }
                    case "LOGIN" -> {
                        if (tokens.length > 1) {
                            outMsg = new LoginMessage(username, tokens[1]);
                        } else {
                            outMsg = new TextMessage(username, userInput);
                        }
                    }
                    case "LOGOUT" -> outMsg = new LogoutMessage(username);
                    case "OPTION" -> {
                        if (tokens.length == 3) {
                            // What option is being set/queried?
                            OptionEnum option = switch (
                                    tokens[1].toUpperCase()) {
                                case "CIPHER_KEY" -> OptionEnum.CIPHER_KEY;
                                case "CIPHER_NAME" -> OptionEnum.CIPHER_NAME;
                                case "CIPHER_ENABLE" -> OptionEnum.CIPHER_ENABLE;
                                default -> null;    // Not a valid option.
                            };
                            // Build appropriate message for that option.
                            if (option != null) {
                                outMsg = new OptionMessage(username,
                                        option, tokens[2]);
                            } else {    // Not a valid option.
                                outMsg = new TextMessage(username, userInput);
                            }
                        } else {    // Not a valid OptionMessage
                            outMsg = new TextMessage(username, userInput);
                        }
                    }
                    default ->
                            // unrecognized token
                            outMsg = new TextMessage(username, userInput);
                }
                outObj.writeObject(outMsg);
                outObj.flush();
            } while (outMsg.getMsgType() != MsgTypeEnum.LOGOUT);

            // Get server's closing reply and show it to user.
            inMsg = (Message) inObj.readObject();
            System.out.println(
                    switch (inMsg.getMsgType()) {
                        case TEXT -> ((TextMessage) inMsg).getText();
                        default -> "UNEXPECTED CLOSING MESSAGE: "
                                + inMsg;
                    });
        }   // Streams and sockets closed by try-with-resources

        System.out.println("Connection to " + hostname + ":" + port
                + " closed, exiting.");
    }
}

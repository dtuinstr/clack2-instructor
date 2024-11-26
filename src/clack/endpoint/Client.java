package clack.endpoint;

import clack.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

import static clack.message.MsgTypeEnum.LOGOUT;

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
public class Client
{
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
    public Client(String hostname, int port, String username)
            throws NoSuchAlgorithmException
    {
        ClientGUI cGUI = new ClientGUI();

        if (port < 1 || port > 49151) {
            throw new IllegalArgumentException(
                    "Port " + port + " not in range 1 - 49151.");
        }
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.prompt = hostname + ":" + port + "> ";
    }

    /**
     * Creates a client for exchanging Message objects, using the
     * default username (Client.DEFAULT_USERNAME).
     *
     * @param hostname the hostname of the server.
     * @param port     the service's port on the server.
     * @throws IllegalArgumentException if port not in range [1-49151]
     */
    public Client(String hostname, int port)
            throws NoSuchAlgorithmException
    {
        this(hostname, port, DEFAULT_USERNAME);
    }

    /**
     * Starts this client, connecting to the server and port that
     * it was given when constructed.
     *
     * @throws UnknownHostException if hostname is not resolvable.
     * @throws IOException          if socket creation, wrapping, or IO fails.
     */
    public void start()
            throws UnknownHostException, IOException, ClassNotFoundException
    {
        System.out.println("Attempting connection to " + hostname + ":" + port);
        Scanner keyboard = new Scanner(System.in);

        try (
                Socket socket = new Socket(hostname, port);
                ObjectOutputStream outObj =
                        new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inObj =
                        new ObjectInputStream(socket.getInputStream());
        ) {
            String userInput;
            Message inMsg;
            Message outMsg;

            // Server talks first after connection made.
            inMsg = (Message) inObj.readObject();
            System.out.println(switch (inMsg.getMsgType()) {
                case FILE -> "FILE message received:" + inMsg;
                case TEXT -> ((TextMessage) inMsg).getText();
                default -> "UNEXPECTED MESSAGE: " + inMsg;
            });

            // Conversation: user gives command, server replies.
            do {
                do { // get valid Message object from user input.
                    // Get user input. Loop on whitespace.
                    String[] tokens;
                    do {
                        System.out.print(prompt);
                        userInput = keyboard.nextLine();
                        tokens = userInput.trim().split("\\s+");
                    } while (tokens.length == 0 || tokens[0].isEmpty());

                    // Construct Message based on input and send it to server.
                    // The build...() methods throw IllegalArgumentException
                    // if a message cannot be built.
                    // 'command' is first token, 'args' a copy of all the rest.
                    String command = tokens[0].toUpperCase();
                    String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                    try {
                        outMsg = switch (command) {
                            case "SEND" -> buildFileMessage(args);
                            case "HELP" -> buildHelpMessage(args);
                            case "LIST" -> buildListUsersMessage(args);
                            case "LOGIN" -> buildLoginMessage(args);
                            case "LOGOUT" -> buildLogoutMessage(args);
                            case "OPTION" -> buildOptionMessage(args);
                            default -> new TextMessage("username", userInput);
                        };
                    } catch (IllegalArgumentException e) {
                        if (command.equals("SEND")) { // syntax or IO exception
                            System.out.println("SEND FILE problem: "
                                    + e.getMessage());
                            outMsg = null;
                        } else {
                            outMsg = new TextMessage("username", userInput);
                        }
                    }
                } while (outMsg == null);

                outObj.writeObject(outMsg);
                outObj.flush();

                // Server's reply.
                inMsg = (Message) inObj.readObject();
                System.out.println(switch (inMsg.getMsgType()) {
                    case FILE -> "FILE message received:" + inMsg;
                    case TEXT -> ((TextMessage) inMsg).getText();
                    default -> "UNEXPECTED MESSAGE: " + inMsg;
                });
            } while (outMsg.getMsgType() != LOGOUT);
        }   // Streams and sockets closed by try-with-resources

        System.out.println("Connection to " + hostname + ":" + port
                + " closed, exiting.");
    }

    /**
     * Attempts to build a FileMessage, with contents retrieved from
     * a fileReadPath given in an array of tokens. The args array
     * must conform to either the following formats (case-insensitive):
     * <ul>
     *   <li>{"FILE", fileReadPath}</li>
     * * <li>{"FILE", fileReadPath, "AS", fileSaveAsName}</li>
     * </ul>
     * If the args is anything else, null is returned. The FileMessage
     * object returned by this method is created by calling the
     * FileMessage(username, fileReadPath) constructor if only a
     * fileReadPath is given, or FileMessage(username, fileReadPath,
     * fileSaveAsName) if a fileSaveAsName is also given.
     *
     * @param args tokenized user input.
     * @return a FileMessage, or null.
     * @throws IllegalArgumentException if args is null, or the
     * file at fileReadPath is not readable.
     */
    private FileMessage buildFileMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        try {
            if (args.length == 2
                    && args[0].equalsIgnoreCase("FILE")) {
                return new FileMessage(username, args[1]);
            }
            if (args.length == 4
                    && args[0].equalsIgnoreCase("FILE")
                    && args[2].equalsIgnoreCase("AS")) {
                return new FileMessage(username, args[1], args[3]);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "File not found or not readable");
        }
        throw new IllegalArgumentException("Invalid SEND FILE syntax");
    }

    /**
     * Builds a HelpMessage if 'args' is not null. If args
     * is null (it should never be), returns null.
     *
     * @param args tokenized user input (presently ignored).
     * @return a HelpMessage, or null.
     */
    private HelpMessage buildHelpMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        return new HelpMessage(username);
    }

    /**
     * Builds a ListUsersMessage, if args equals {"LIST", "USER", ...}.
     * Otherwise, returns null.
     *
     * @param args tokenized user input.
     * @return a ListUsersMessage, or null.
     */
    private ListUsersMessage buildListUsersMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        if (args.length > 0
                && args[0].equalsIgnoreCase("USERS")) {
            return new ListUsersMessage(username);
        }
        throw new IllegalArgumentException("Invalid LIST USERS syntax");
    }

    /**
     * Builds a LoginMessage, if the user has supplied a password.
     * If the user has not, returns null.
     *
     * @param args tokenized user input.
     * @return a LoginMessage, or null.
     */
    private LoginMessage buildLoginMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        if (args.length > 0) {
            return new LoginMessage(username, args[0]);
        }
        throw new IllegalArgumentException("Invalid LOGIN syntax");
    }

    /**
     * Builds a LogoutMessage, if args is not null. If it is,
     * returns null.
     *
     * @param args tokenized user input (presently ignored).
     * @return a LogoutMessage, or null.
     */
    private LogoutMessage buildLogoutMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        return new LogoutMessage(username);
    }

    /**
     * Builds an OptionMessage. If user input is not syntactically
     * correct for an OptionMessage, returns null. The args array
     * must be one of
     * <ul>
     *     <li>{optionEnum}</li>
     *     <li>{optionEnum, string}</li>
     * </ul>
     * In the first case, builds an OptionMessage that queries for
     * the server's current value of the option. In the second case
     * builds an OptionMessage that causes the server to set the
     * value of the option.
     *
     * @param args tokenized user input.
     * @return an OptionMessage, or null.
     */
    private OptionMessage buildOptionMessage(String[] args)
    {
        if (args == null) {
            throw new IllegalArgumentException("null args array");
        }
        // args must have either 1 or 2 elements.
        if (args.length == 0 || args.length > 2) {
            throw new IllegalArgumentException("Invalid OPTION syntax");
        }
        OptionEnum option = switch (args[0].toUpperCase()) {
            case "CIPHER_KEY" -> OptionEnum.CIPHER_KEY;
            case "CIPHER_NAME" -> OptionEnum.CIPHER_NAME;
            case "CIPHER_ENABLE" -> OptionEnum.CIPHER_ENABLE;
            default -> null;    // Not a valid option.
        };
        if (option == null) {
            throw new IllegalArgumentException("Invalid OPTION syntax");
        } else if (args.length == 1) {
            return new OptionMessage(username, option, null);
        } else {
            return new OptionMessage(username, option, args[1]);
        }
    }

}

package no.ntnu;

import no.ntnu.message.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles the logic of one particular client connection
 */
public class ClientHandler extends Thread {
    private static final String CMD_PUBLIC_MESSAGE = "msg";
    private static final String CMD_PRIVATE_MESSAGE = "privmsg";
    private static final String CMD_MSG_OK = "msgok";
    private static final String CMD_HELP = "help";
    private static final String CMD_LOGIN = "login";
    private static final String CMD_LOGIN_OK = "loginok";
    private static final String CMD_USERS = "users";

    private static final String ERR_NOT_SUPPORTED = "cmderr command not supported";
    private static final String ERR_USERNAME_TAKEN = "loginerr username already in use";
    private static final String ERR_INCORRECT_USERNAME = "loginerr incorrect username format";
    private static final String ERR_INCORRECT_RECIPIENT = "msgerr incorrect recipient";
    private static final String ERR_UNAUTHORIZED = "msgerr unauthorized";

    private final Socket socket;
    private final Server server;
    private boolean needToRun = true;
    private final BufferedReader inFromClient;
    private final PrintWriter outToClient;
    private String username;
    // This flag will be set to true once the user logs in with a valid username
    private boolean loggedIn = false;
    // Incremented by 1 for each user
    private static int userCounter = 1;

    /**
     * ClientHandler constructor
     *
     * @param clientSocket Socket for this particular client
     * @param server       The main server class which manages all the connections
     */
    public ClientHandler(Socket clientSocket, Server server) {
        this.socket = clientSocket;
        this.server = server;
        this.inFromClient = createInputStreamReader();
        this.outToClient = createOutputStreamWriter();
        this.username = generateUniqueUsername();
    }

    /**
     * Generate a unique username
     *
     * @return a unique username
     */
    private String generateUniqueUsername() {
        String username = "user" + (userCounter++);
        // Make sure the generated username does not collide with any of the other users
        while (!server.isUsernameAvailable(username)) {
            username = "user" + (userCounter++);
        }
        return username;
    }

    /**
     * Create buffered input stream reader for the socket
     *
     * @return The input stream reader or null on error
     */
    private BufferedReader createInputStreamReader() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            Server.log("Could not setup the input stream: " + e.getMessage());
        }
        return reader;
    }

    /**
     * Create writer which can be used to send data to the client (to the socket)
     *
     * @return The output-stream writer or null on error
     */
    private PrintWriter createOutputStreamWriter() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            Server.log("Could not setup the output stream: " + e.getMessage());
        }
        return writer;
    }

    /**
     * Handle the conversation according to the protocol
     */
    public void run() {
        while (needToRun) {
            Message message = readClientMessage();
            if (message != null) {
                Server.log(getId() + ": " + message);
                switch (message.getCommand()) {
                    case CMD_PUBLIC_MESSAGE:
                        handlePublicMessage(message.getArguments());
                        break;
                    case CMD_PRIVATE_MESSAGE:
                        forwardPrivateMessage(message);
                        break;
                    case CMD_HELP:
                        send("supported msg help");
                        break;
                    case CMD_LOGIN:
                        handleLogin(message.getArguments());
                        break;
                    case CMD_USERS:
                        send("users " + server.getActiveUsernames());
                        break;
                    default:
                        send(ERR_NOT_SUPPORTED);
                }
            } else {
                Server.log("Error while reading client input, probably socket is closed, exiting...");
                needToRun = false;
            }
        }
        Server.log("Done processing client");
        closeSocket();
        server.removeClientHandler(this);
    }

    /**
     * Try to log in with the given username. Send response to the client.
     *
     * @param username The username to use for this client
     */
    private void handleLogin(String username) {
        if (isAlphaNumeric(username)) {
            if (server.isUsernameAvailable(username)) {
                this.username = username;
                loggedIn = true;
                send(CMD_LOGIN_OK);
            } else {
                send(ERR_USERNAME_TAKEN);
            }
        } else {
            send(ERR_INCORRECT_USERNAME);
        }
    }

    /**
     * Check if the given string contains only alphanumeric characters
     * Function taken from: https://www.techiedelight.com/check-string-contains-alphanumeric-characters-java/
     *
     * @param s The string to check
     * @return True if the string contains alphanumerics only, false if it contains any other characters
     */
    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]*$");
    }

    /**
     * Forward the message to all other clients, except this one who sent it.
     * Send also a response to the sender, according to the protocol.
     *
     * @param message The message to forward
     */
    private void handlePublicMessage(String message) {
        String forwardedMessage = CMD_PUBLIC_MESSAGE + " " + username + " " + message;
        int recipientCount = server.forwardToAllClientsExcept(forwardedMessage, this);
        send(CMD_MSG_OK + " " + recipientCount);
    }

    /**
     * Forward a private message to necessary recipient
     *
     * @param m The received message
     */
    private void forwardPrivateMessage(Message m) {
        if (!isLoggedIn()) {
            send(ERR_UNAUTHORIZED);
            return;
        }

        // Split the arguments into recipient and message
        String arguments = m.getArguments();
        if (arguments != null) {
            String[] parts = arguments.split(" ", 2);
            if (parts.length == 2) {
                String recipient = parts[0];
                String message = CMD_PRIVATE_MESSAGE + " " + recipient + " " + parts[1];
                if (server.forwardPrivateMessage(recipient, message)) {
                    send(CMD_MSG_OK + " 1");
                } else {
                    send(ERR_INCORRECT_RECIPIENT);
                }
            } else {
                send(ERR_NOT_SUPPORTED);
            }
        } else {
            send(ERR_NOT_SUPPORTED);
        }
    }

    /**
     * Return true if this client has logged in with a proper username
     *
     * @return True if logged in, false if not
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Read one message from the client (from the socket)
     *
     * @return The message or null on error
     */
    private Message readClientMessage() {
        String receivedInputLine = null;
        try {
            receivedInputLine = inFromClient.readLine();
        } catch (IOException e) {
            Server.log("Error while reading the socket input: " + e.getMessage());
        }
        return Message.createFromInput(receivedInputLine);
    }

    /**
     * Send a message to the client. Newline appended automatically
     *
     * @param message The message to send
     */
    public void send(String message) {
        outToClient.println(message);
    }

    /**
     * Close socket connection for this client
     */
    private void closeSocket() {
        Server.log("Closing client socket...");
        try {
            socket.close();
        } catch (IOException e) {
            Server.log("Error while closing a client socket: " + e.getMessage());
        }
        Server.log("Client socket closed");
    }

    /**
     * Check if this client has the provided username
     *
     * @param username The username to check
     * @return true if this client has the given username, false if it doesn't
     */
    public boolean hasUsername(String username) {
        return this.username.equals(username);
    }

    /**
     * Return the username of the current user
     *
     * @return The username for this client
     */
    public String getUsername() {
        return username;
    }
}

package NtnuChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.net.SocketException;

/**
 * Handle connected chat client.
 * Used as part of assignment A4 in DataKomm course.
 */
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private String clientId;
    private final Server server;
    private boolean authenticated;
    private final PrintWriter pw;
    private Mode mode;
    private final Queue<String> inbox;
    private static final int INBOX_SIZE = 10; // How many messages to keep in the inbox

    /**
     * @param server       the chat server.
     * @param clientSocket the client socket.
     */
    public ClientHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = "Anonymous" + GlobalCounter.getNumber();
        this.authenticated = false;
        pw = new PrintWriter(clientSocket.getOutputStream(), true);
        this.mode = Mode.ASYNC;
        this.inbox = new ArrayDeque<>();
    }

    /**
     * Get a unique ID for this client
     * @return
     */
    public int getClientId() {
        return (int) Thread.currentThread().getId();
    }

    /**
     * Handle client connection. This method is entry point when a new thread
     * is started
     */
    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;

            try {
                // Read from socket until it closes.
                while ((line = input.readLine()) != null) {
                    handleIncomingMessage(line);
                }
                // Socket closed on the client side, force closing the server side as well
                clientSocket.close();
            } catch (SocketException ex) {
                System.out.println("Socket exception for client " + getClientId() + ": " + ex.getMessage());
            }
        } catch (IOException ex) {
            System.out.println("I/O Exception for client " + getClientId() + ": " + ex.getMessage());
        }

        // Socket has been closed. Remove user from connectedClients.
        try {
            System.out.println("Trying to close socket for client " + getClientId() + "...");
            clientSocket.close();
            System.out.println("Socket closed for client " + getClientId());
        } catch (IOException e) {
            System.out.println("Could not close socket for client " + getClientId());
        }
        server.removeConnectedClient(getClientId());
    }

    /**
     * Parse incoming message.
     *
     * @param msg incoming message
     */
    private void handleIncomingMessage(String msg) {
        msg = msg.trim(); // Remove trailing spaces
        if (msg.startsWith("msg ")) { // Public chat message
            // Broadcast message.
            String message = msg.substring(4);
            int recipientCount = broadcast(String.format(ServerResponse.MSG, clientId, message));
            send(String.format(ServerResponse.MSG_MSG_OK, recipientCount));
        } else if (msg.equals("help")) { // Supported commands
            // Send list of supported commands.
            send(ServerResponse.MSG_SUPPORTED);
        } else if (msg.startsWith("login ")) { // Login
            String username = msg.substring(6);
            login(username);
        } else if (msg.equals("users")) { // Get online user listing
            users();
        } else if (msg.equals("sync")) { // Get online user listing
            setMode(Mode.SYNC);
        } else if (msg.equals("async")) { // Get online user listing
            setMode(Mode.ASYNC);
        } else if (msg.equals("inbox")) { // Get online user listing
            reportInbox();
        } else if (msg.equals("joke")) { // Get online user listing
            send(String.format(ServerResponse.MSG_JOKE, Jokes.getRandomJoke()));
        } else if (msg.startsWith("privmsg ")) { // Send private message
            String[] parts = msg.trim().split(" ");
            if (parts.length >= 3) {
                String recipient = parts[1].trim();
                String message = msg.substring(8 + recipient.length() + 1).trim(); // The length is privmsg + recipient + space

                if (recipient.length() > 0 && message.length() > 0) {
                    sendPrivateMessage(recipient, message);
                }
            }
        } else {
            // Command not supported
            send(ServerResponse.MSG_ERR);
        }
    }

    /**
     * Set the conversation mode
     * @param mode
     */
    private void setMode(Mode mode) {
        System.out.println(String.format("Setting mode to %s for client %s", mode.toString(), getId()));
        this.mode = mode;
        send("modeok");
    }

    /**
     * Send the content of inbox to the user
     */
    private void reportInbox() {
        send(String.format(ServerResponse.MSG_INBOX, inbox.size())); // Inbox size
        // Send the messages from the inbox
        for (String message: inbox) {
            send(message);
        }
        inbox.clear();
    }

    /**
     * Send user list to client.
     */
    private void users() {
        send(String.format(ServerResponse.MSG_USERS, server.getUsernames()));
    }

    /**
     * Send a private message.
     *
     * @param recipient username to send message to.
     * @param message   message to send.
     */
    private void sendPrivateMessage(String recipient, String message) {
        if (authenticated) {
            ClientHandler recipientFound = server.getClientByUsername(recipient);
            if (recipientFound != null) {
                // Send message to recipient
                recipientFound.enqueueMessage(String.format(ServerResponse.MSG_PRIVMSG, clientId, message));
                send(String.format(ServerResponse.MSG_MSG_OK, 1)); // Message sent to 1 recipient
            } else {
                // Could not find recipient - send error message to client.
                send(ServerResponse.MSG_ERR_PRIVMSG_RECIPIENT);
            }
        } else {
            // Client not authorized.
            send(ServerResponse.MSG_ERR_PRIVMSG_UNAUTHORIZED);
        }
    }

    /**
     * Authenticate a new username.
     *
     * @param username username to authenticate.
     */
    private void login(String username) {
        boolean isUsernameAvailable = true;

        ClientHandler existingClient = server.getClientByUsername(username);
        if (existingClient != null) {
            // Someone has that username already
            isUsernameAvailable = false;
        }

        // Check if username is only made out of letters and numbers.
        if (username.matches("[a-zA-Z0-9]*")) {
            if (isUsernameAvailable) {
                // Change username.
                clientId = username;
                authenticated = true;

                // Send loginok to client.
                send(ServerResponse.MSG_LOGIN_OK);
            } else {
                // Send loginerr to client.
                send(String.format(ServerResponse.MSG_LOGIN_ERR, "username already in use"));
            }
        } else {
            send(String.format(ServerResponse.MSG_LOGIN_ERR, "incorrect username format"));
        }
    }

    /**
     * Get the client username.
     *
     * @return clientId
     */
    public String getUsername() {
        return clientId;
    }

    /**
     * Send message to client.
     *
     * @param msg the message to send to the client.
     */
    public void send(String msg) {
        pw.println(msg);
    }

    /**
     * Send message to all clients.
     *
     * @param msg the message to send to all clients.
     * @return The number of messages sent (how many recipients got the message)
     */
    public int broadcast(String msg) {
        for (Map.Entry<Integer, ClientHandler> entry : server.getConnectedClients().entrySet()) {
            // Don't send message to this client
            if (entry.getKey() != this.getId()) {
                ClientHandler clientHandler = entry.getValue();
                clientHandler.enqueueMessage(msg);
            }
        }
        return server.getConnectedClients().size() - 1;
    }

    /**
     * Enqueue the message in the clients inbox (if we are in synchronous mode) or send it to the client immediately
     * @param message Message for the clients inbox
     */
    private void enqueueMessage(String message) {
        if (mode == Mode.SYNC) {
            addMessageToInbox(message);
        } else {
            send(message);
        }
    }

    private void addMessageToInbox(String message) {
        System.out.println(String.format("Add message to inbox #%s: %s", getId(), message));
        if (inbox.size() >= INBOX_SIZE) {
            // Inbox full, remove the oldest message
            System.out.println("Inbox full, removing oldest message");
            inbox.poll();
        }
        inbox.add(message);
    }
}

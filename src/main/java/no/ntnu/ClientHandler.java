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

    private static final String ERR_NOT_SUPPORTED = "cmderr command not supported";

    private final Socket socket;
    private final Server server;
    private boolean needToRun = true;
    private final BufferedReader inFromClient;
    private final PrintWriter outToClient;
    private final String username;
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
        return "user" + (userCounter++);
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
                        // Forward the message (with username) to all other clients, except this one
                        String forwardedMessage = CMD_PUBLIC_MESSAGE + " " + username + " " + message.getArguments();
                        server.forwardToAllClientsExcept(forwardedMessage, this);
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
}

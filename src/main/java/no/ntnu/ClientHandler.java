package no.ntnu;

import java.io.IOException;
import java.net.Socket;

/**
 * Handles the logic of one particular client connection
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final Server server;

    /**
     * Constructor
     *
     * @param clientSocket Socket for this particular client
     * @param server The main server class which manages all the connections
     */
    public ClientHandler(Socket clientSocket, Server server) {
        this.socket = clientSocket;
        this.server = server;
    }

    /**
     * Handle the conversation according to the protocol
     */
    public void run() {
        Server.log("Emulating a long operation...");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            Server.log("Processing interrupted");
        }
        Server.log("Done processing client");
        closeSocket();
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
        server.removeClientHandler(this);
    }
}

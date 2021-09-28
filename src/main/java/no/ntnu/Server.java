package no.ntnu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class for the TCP chat server. Accepts new client connections, hands each new client over to a
 * separate ClientHandler. Maintains list of active connections.
 */
public class Server {
    private final static int TCP_PORT = 1300; // TCP port to listen to
    private final List<ClientHandler> clientHandlers = new LinkedList<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
        log("Server exiting...");
    }

    /**
     * Start the TCP chat server
     */
    private void run() {
        ServerSocket welcomeSocket = openWelcomingSocket();
        if (welcomeSocket != null) {
            while (true) {
                Socket clientSocket = acceptNextClient(welcomeSocket);
                if (clientSocket != null) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientHandler.start();
                    storeClientHandler(clientHandler);
                }
            }
        }
    }

    /**
     * Store client handler in the register
     *
     * @param clientHandler The client handler thread
     */
    private void storeClientHandler(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    /**
     * Remove client handler from the register
     *
     * @param clientHandler The client handler thread
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    /**
     * Open a server socket which listens on the predefined TCP port
     *
     * @return the welcoming server socket, null on error
     */
    private ServerSocket openWelcomingSocket() {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(TCP_PORT);
            log("Server listening on port " + TCP_PORT);
        } catch (IOException e) {
            log("Could not open a listening socket: " + e.getMessage());
        }
        return welcomeSocket;
    }

    /**
     * Block execution until the next client connects
     *
     * @param welcomeSocket The listening server socket
     * @return Socket for the newly connected client, null on error
     */
    private Socket acceptNextClient(ServerSocket welcomeSocket) {
        Socket clientSocket = null;
        try {
            clientSocket = welcomeSocket.accept();
            log("New client connected from " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            log("Failed to accept a client connection: " + e.getMessage());
        }
        return clientSocket;
    }

    /**
     * Log a message to standard output
     */
    public static void log(String message) {
        System.out.println(message);
    }

    /**
     * Forward a message to all client sockets, except the socket belonging to the original sender
     *
     * @param message The message to forward
     * @param sender  The original sender who will not receive this message
     */
    public void forwardToAllClientsExcept(String message, ClientHandler sender) {
        for (ClientHandler c : clientHandlers) {
            if (c != sender) {
                c.send(message);
            }
        }
    }

    /**
     * Check if a given username is already available or a logged-in users has already taken it
     *
     * @param username The username to check
     * @return True if username available, false if someone already uses it
     */
    public boolean isUsernameAvailable(String username) {
        boolean usernameAvailable = true;
        Iterator<ClientHandler> it = clientHandlers.iterator();
        while (usernameAvailable && it.hasNext()) {
            ClientHandler clientHandler = it.next();
            usernameAvailable = !clientHandler.hasUsername(username);
        }
        return usernameAvailable;
    }
}

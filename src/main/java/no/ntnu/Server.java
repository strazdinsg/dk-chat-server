package no.ntnu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main class for the TCP chat server. Accepts new client connections, hands each new client over to a
 * separate ClientHandler. Maintains list of active connections.
 */
public class Server {
    private final static int TCP_PORT = 1300; // TCP port to listen to

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
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandler.start();
                }
            }
        }
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
}

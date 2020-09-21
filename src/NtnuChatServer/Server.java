package NtnuChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat server networking.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Server {
    private ServerSocket serverSocket;
    private final Map<Integer, ClientHandler> connectedClients;

    public Server() {
        connectedClients = new HashMap<>();
    }

    /**
     * Start TCP server
     *
     * @param port the TCP server port
     */
    public void start(int port) {
        try {
            // Start listening on port
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Chat server started on port " + port);

        while (true) {
            try {
                // Wait for an incoming connection.
                Socket incomingSocket = serverSocket.accept();


                // Handle new connection
                ClientHandler clientHandler = new ClientHandler(this, incomingSocket);

                // Start thread
                clientHandler.start();

                // Add thread to connectedClients hashtable
                int clientHandlerThreadId = (int) clientHandler.getId();
                System.out.println("New connection accepted, client ID = " + clientHandlerThreadId);
                connectedClients.put(clientHandlerThreadId, clientHandler);

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    /**
     * Get currently connected clients in a map format where the key is the client thread ID and the value
     * is ClientHandler object for that client.
     *
     * @return The connected clients
     */
    public Map<Integer, ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    /**
     * Find a client handber by the username (login_
     *
     * @param username Username of the client of interest
     * @return ClientHandler or null if none found
     */
    public ClientHandler getClientByUsername(String username) {
        ClientHandler desiredClient = null;
        Iterator<ClientHandler> it = connectedClients.values().iterator();
        while (desiredClient == null && it.hasNext()) {
            ClientHandler c = it.next();
            if (c.getUsername().equals(username)) {
                desiredClient = c;
            }
        }
        return desiredClient;
    }

    /**
     * Remove ClientHandler from connectedClients.
     */
    public void removeConnectedClient(int client) {
        System.out.println("Removing client " + client);
        connectedClients.remove(client);
    }

    /**
     * Return a list of connected users
     *
     * @return Usernames of connected clients, separated by space
     */
    public String getUsernames() {
        List<String> usernames = new LinkedList<>();
        for (ClientHandler client : connectedClients.values()) {
            usernames.add(client.getUsername());
        }

        return String.join(" ", usernames);
    }
}

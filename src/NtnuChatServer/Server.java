package NtnuChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat server networking.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Server {
    private ServerSocket serverSocket;
    private final Map<Integer, ClientHandler> connectedClients;
    
    public Server(){
        connectedClients = new HashMap<>();
    }
    
    /**
     * Start TCP server
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
        
        while(true){
            try {            
                // Wait for an incoming connection.
                Socket incomingSocket = serverSocket.accept();
            
                System.out.println("New connection accepted.");
            
                // Handle new connection
                ClientHandler clientHandler = new ClientHandler(this, incomingSocket);
                
                // Start thread
                clientHandler.start();
                
                // Add thread to connectedClients hashtable
                int clientHandlerThreadId = (int)clientHandler.getId();
                connectedClients.put(clientHandlerThreadId, clientHandler);
            
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            
    /**
     * Get all connected users. Maps client thread ID to ClientHandler object
     * @return connectedClients
     */
    public Map<Integer, ClientHandler> getConnectedClients(){
        return connectedClients;
    }
    
    /**
     * Remove ClientHandler from connectedClients.
     */
    public void removeConnectedClient(int client){
        connectedClients.remove(client);
    }
}

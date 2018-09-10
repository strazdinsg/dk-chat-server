package NtnuChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat server networking.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Server {
    private ServerSocket serverSocket;
    private HashMap<Integer, ClientHandler> connectedClients;
    
    /**
     * @param port the server port
     */
    public Server(int port){
        connectedClients = new HashMap<Integer, ClientHandler>();
        
        try {
            // Start listening on port
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
     * Get all connected users.
     * @return connectedClients
     */
    public HashMap<Integer, ClientHandler> getConnectedClients(){
        return connectedClients;
    }
}

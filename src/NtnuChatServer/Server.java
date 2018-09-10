package NtnuChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat server networking.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Server {
    private ServerSocket serverSocket;
    
    /**
     * @param port the server port
     */
    public Server(int port){
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
                handleIncomingConnection(incomingSocket);
            
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Handle new client connections
     * @param socket the incoming socket connection
     */
    private void handleIncomingConnection(Socket socket)
    {
        try {
            // 10 second delay
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

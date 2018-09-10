package NtnuChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle connected chat client.
 * Used as part of assignment A4 in DataKomm course.
 */
public class ClientHandler extends Thread {
    private Socket clientSocket;
    
    /**
     * @param clientSocket the client socket.
     */
    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    
    /**
     * Handle client connection.
     */
    public void run(){
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
            String line;
            
            // Read from socket until it closes.
            while ((line = input.readLine()) != null){
                System.out.println("Thread ID: " + this.getId());
                System.out.println("  Message: " + line);
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

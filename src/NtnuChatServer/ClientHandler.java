package NtnuChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle connected chat client.
 * Used as part of assignment A4 in DataKomm course.
 */
public class ClientHandler extends Thread {
    private Socket clientSocket;
    private String clientId;
    private Server server;
    
    /**
     * @param server the chat server.
     * @param clientSocket the client socket.
     */
    public ClientHandler(Server server, Socket clientSocket){
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = "Anonymous" + GlobalCounter.getNumber();
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
                
                // Check if client is trying to send a message.
                if (line.startsWith("msg ")) {
                    // Broadcast message.
                    String message = line.substring(4);
                    broadcast("msg " + clientId + " " + message + "\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Send message to client.
     * @param msg the message to send to the client.
     */
    public void send(String msg) {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.println(msg);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Send message to all clients.
     * @param msg the message to send to all clients.
     */
    public void broadcast(String msg) {
        Map<Integer, ClientHandler> map = server.getConnectedClients();
        for (Map.Entry<Integer, ClientHandler> client : map.entrySet()) {
            // Don't send message to this client
            if (client.getKey() != this.getId())
            {
                client.getValue().send(msg);
            }
        }
    }
}

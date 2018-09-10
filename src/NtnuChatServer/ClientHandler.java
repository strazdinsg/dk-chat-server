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
                handleIncomingMessage(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Parse incoming message.
     * @param msg incoming message
     */
    private void handleIncomingMessage(String msg) {
        if (msg.startsWith("msg ")) { // Public chat message
            // Broadcast message.
            String message = msg.substring(4);
            broadcast(String.format(ServerResponse.MSG, clientId, message));
        }
        if (msg.trim().equals("help")) { // Supported commands
            // Send list of supported commands.
            send(ServerResponse.MSG_SUPPORTED);
        }
        else {
            // Command not supported
            send(ServerResponse.MSG_ERR);
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
            if (client.getKey() != this.getId()) {
                client.getValue().send(msg);
            }
        }
    }
}

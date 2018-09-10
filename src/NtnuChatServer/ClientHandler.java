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
    private boolean authenticated;
    
    /**
     * @param server the chat server.
     * @param clientSocket the client socket.
     */
    public ClientHandler(Server server, Socket clientSocket){
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = "Anonymous" + GlobalCounter.getNumber();
        this.authenticated = false;
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
        else
        if (msg.trim().equals("help")) { // Supported commands
            // Send list of supported commands.
            send(ServerResponse.MSG_SUPPORTED);
        }
        else
        if (msg.startsWith("login ")) { // Login 
            String username = msg.substring(6);
            login(username);
        }
        else
        if (msg.trim().equals("users")) { // Get online user listing 
            users();
        }
        else
        if (msg.startsWith("privmsg ")) { // Send private message 
            if (msg.trim().split(" ").length >= 3) {
                String recipient = msg.split(" ")[1].trim();
                String message = msg.substring(8 + recipient.length() + 1).trim(); // The length is privmsg + recipient + space
                
                if (recipient.length() > 0 && message.length() > 0){
                    sendPrivateMessage(recipient, message);
                }
            }
        }
        else {
            // Command not supported
            send(ServerResponse.MSG_ERR);
        }
    }
    
    /**
     * Send a private message.
     * @param recipient username to send message to.
     * @param message message to send.
     */
    private void sendPrivateMessage(String recipient, String message) {
        if (authenticated)
        {
            ClientHandler recipientFound = null;
            
            Map<Integer, ClientHandler> map = server.getConnectedClients();
            for (Map.Entry<Integer, ClientHandler> client : map.entrySet()) {
                if (client.getValue().getUsername().equals(recipient)) {
                    // Found username in connectedClients list.
                    recipientFound = client.getValue();
                }
            }
            
            if (recipientFound != null) {
                // Send message to recipient
                recipientFound.send(String.format(ServerResponse.MSG_PRIVMSG, clientId, message));
            }
            else {
                // Could not find recipient - send error message to client.
                send(ServerResponse.MSG_ERR_PRIVMSG_RECIPIENT);
            }
        }
        else {
            // Client not authorized.
            send(ServerResponse.MSG_ERR_PRIVMSG_UNAUTHORIZED);
        }
    }
    
    /**
     * Authenticate a new username.
     * @param username username to authenticate.
     */
    private void login(String username) {
        boolean isUsernameAvailable = true;
        
        Map<Integer, ClientHandler> map = server.getConnectedClients();
        for (Map.Entry<Integer, ClientHandler> client : map.entrySet()) {
            if (client.getValue().getUsername().equals(username)) {
                // Username is taken.
                isUsernameAvailable = false;
            }
        }
        
        // Check if username is only made out of letters and numbers.
        if (username.matches("[a-zA-Z0-9]*")){
            if (isUsernameAvailable) {
                // Change username.
                clientId = username;
                authenticated = true;
                
                // Send loginok to client.
                send(ServerResponse.MSG_LOGIN_OK);
            }
            else {
                // Send loginerr to client.
                send(String.format(ServerResponse.MSG_LOGIN_ERR, "username already in use"));
            }
        }
        else {
            send(String.format(ServerResponse.MSG_LOGIN_ERR, "incorrect username format"));
        }
    }
    
    /**
     * Send user list to client.
     */
    private void users()
    {
        String returnUsers = "";
        
        Map<Integer, ClientHandler> map = server.getConnectedClients();
        for (Map.Entry<Integer, ClientHandler> client : map.entrySet()) {
            // Add username to list.
            returnUsers += client.getValue().getUsername() + " ";
        }
        
        // Send user list to client.
        send(String.format(ServerResponse.MSG_USERS, returnUsers.trim()));
    }
    
    /**
     * Get the client username.
     * @return clientId
     */
    public String getUsername()
    {
        return clientId;
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

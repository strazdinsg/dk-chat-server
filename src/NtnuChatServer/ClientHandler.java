package NtnuChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketException;

/**
 * Handle connected chat client.
 * Used as part of assignment A4 in DataKomm course.
 */
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private String clientId;
    private final Server server;
    private boolean authenticated;
    private final PrintWriter pw;
    
    /**
     * @param server the chat server.
     * @param clientSocket the client socket.
     */
    public ClientHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = "Anonymous" + GlobalCounter.getNumber();
        this.authenticated = false;
        pw = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    
    /**
     * Handle client connection. This method is entry point when a new thread 
     * is started
     */
    @Override
    public void run(){
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
            String line;
            
            try {
                // Read from socket until it closes.
                while ((line = input.readLine()) != null){
                    handleIncomingMessage(line);
                }
                // Socket closed on the client side, force closing the server side as well
                clientSocket.close();
            }
            catch (SocketException ex)
            {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Socket has been closed. Remove user from connectedClients.
        server.removeConnectedClient((int)Thread.currentThread().getId());
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
            String[] parts = msg.trim().split(" ");
            if (parts.length >= 3) {
                String recipient = parts[1].trim();
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
     * Send user list to client.
     */
    private void users() {
        send(String.format(ServerResponse.MSG_USERS, server.getUsernames()));
    }

    /**
     * Send a private message.
     * @param recipient username to send message to.
     * @param message message to send.
     */
    private void sendPrivateMessage(String recipient, String message) {
        if (authenticated)
        {
            ClientHandler recipientFound = server.getClientByUsername(recipient);
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

        ClientHandler existingClient = server.getClientByUsername(username);
        if (existingClient != null) {
            // Someone has that username already
            isUsernameAvailable = false;
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
        pw.println(msg);
    }
    
    /**
     * Send message to all clients.
     * @param msg the message to send to all clients.
     */
    public void broadcast(String msg) {
        for (Map.Entry<Integer, ClientHandler> entry : server.getConnectedClients().entrySet()) {
            // Don't send message to this client
            if (entry.getKey() != this.getId()) {
                ClientHandler clientHandler = entry.getValue();
                clientHandler.send(msg);
            }
        }
    }
}

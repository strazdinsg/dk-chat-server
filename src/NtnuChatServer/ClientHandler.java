package NtnuChatServer;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle connected chat client.
 * Used as part of assignment A4 in DataKomm course.
 */
public class ClientHandler {
    public ClientHandler(){
        try {
            // 10 second delay to simulate 
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

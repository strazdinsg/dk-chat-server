package NtnuChatServer;

/**
 * Main class. TCP Server example.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Start a new server on port 1300
        Server server = new Server();
        server.start(1300);
    }
}

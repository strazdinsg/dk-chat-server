package NtnuChatServer;

/**
 * Main class. TCP Server example.
 * Used as part of assignment A4 in DataKomm course.
 */
public class Main {
    // Default TCP Port to use
    private static final int DEFAULT_PORT = 1300;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Start a new server on a given port. Use 1300 as default port
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port specified: " + args[0]);
                port = DEFAULT_PORT;
            }
        }
        Server server = new Server();
        server.start(port);
    }
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main class for the TCP chat server. Accepts new client connections, hands each new client over to a
 * separate ClientHandler. Maintains list of active connections.
 */
public class Server {
    private final static int TCP_PORT = 1300; // TCP port to listen to

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
        log("Server exiting...");
    }

    /**
     * Start the TCP chat server
     */
    private void run() {
        try {
            ServerSocket welcomeSocket = new ServerSocket(TCP_PORT);
            log("Server listening on port " + TCP_PORT);

            Socket clientSocket = welcomeSocket.accept();
            log("New client connected from " + clientSocket.getRemoteSocketAddress());

            log("Closing client socket...");
            clientSocket.close();
            log("Client socket closed");
        } catch (IOException e) {
            log("Could not open a listening socket: " + e.getMessage());
        }
    }

    /**
     * Log a message to standard output
     */
    private static void log(String message) {
        System.out.println(message);
    }
}

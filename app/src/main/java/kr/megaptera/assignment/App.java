package kr.megaptera.assignment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerError;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    // Only in memory
    private final static String host = "localhost";
    private static int port;
    private static long count = 1;

    void run() throws IOException {
        port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen

        // Make a server
        ServerSocket listener = new ServerSocket(port, 0);
        try {
            while (true) {
                // 2. Accept
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
                bw.write("Socket created\n\n");
                Socket socket = listener.accept();


                // Close socket

                socket.close();

                bw.write("\nSocket closed...\n==================\n\n");
                bw.flush();
            }
        } catch (ServerError e) {
            listener.close();
        }
    }

}
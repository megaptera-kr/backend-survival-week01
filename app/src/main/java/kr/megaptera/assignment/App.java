package kr.megaptera.assignment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerError;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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

                // 3. Request

                InputStream inputStream = socket.getInputStream();
                byte[] bytes = new byte[1_000_000];
                int size = inputStream.read(bytes);
                String message = new String(bytes, 0, size);
                bw.write(message + "------ REQUEST ------\n\n");
                bw.flush();

                StringTokenizer st = new StringTokenizer(message, "\n");
                String[] firstLine = st.nextToken().split(" ");
                String method = firstLine[0];
                String[] path = firstLine[1].split("/");

                boolean hasBody = false;

                while (st.hasMoreElements()) {
                    if (st.nextToken().trim().length() == 0) {
                        hasBody = true;
                        break;
                    }
                }

                StringBuffer sb = new StringBuffer();

                while (hasBody && st.hasMoreElements()) {
                    sb.append(st.nextToken() + "\n");
                }

                String requestBody = sb.toString();

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
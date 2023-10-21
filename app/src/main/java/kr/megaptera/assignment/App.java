package kr.megaptera.assignment;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(8080, 0);

        System.out.println("Listen!");

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            System.out.println("Accept!");

            // 3. Request

            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();
            System.out.println(charBuffer.toString());

            // 4. Response
            String body = "Hello, World!";
            byte[] bytes = body.getBytes();
            String message = "" +
                    "HTTP/1.1 200 OK\n"+
                    "Content-Type : text/html; charset=UTF-8\n"+
                    "Content-Length: " + bytes.length + "\n"+
                    "\n"+
                    body;

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. close
            socket.close();
        }
    }

}

package kr.megaptera.assignment;

import java.io.*;
import java.net.ServerSocket;
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
        int backlog = 0;

        Map<Long, String> tasks = new HashMap<>();

        ServerSocket serverSocket = new ServerSocket(port, backlog);

        while (true) {
            System.out.println("Listen");

            var clientSocket = serverSocket.accept();
            System.out.println("Accept");

            Reader clientReader = new InputStreamReader(clientSocket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            clientReader.read(charBuffer);

            charBuffer.flip();

            String requestMessage = charBuffer.toString();

            // Read first line.


            System.out.println("Request done");

            String body = "Hello, world!";
            byte[] bytes = body.getBytes();
            String responseMessage = "" +
                    "HTTP/1.1 200 OK\n" +
                    "Content-Type: text/html; charset=UTF-8\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "\n" +
                    body;

            System.out.println("Process done");

            Writer clientWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            clientWriter.write(responseMessage);
            System.out.println("Response done");

            // 4. Close
            clientWriter.flush();
            clientReader.close();
            clientSocket.close();
            System.out.println("Close");
        }
    }

}

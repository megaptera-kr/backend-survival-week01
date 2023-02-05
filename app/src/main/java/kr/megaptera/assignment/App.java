package kr.megaptera.assignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
        ServerSocket listener = new ServerSocket(8080,0);
        System.out.println("Listen!");
        // 2. Accept

        while (true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Readable reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();
            System.out.println(charBuffer.toString());
            String headerData = charBuffer.toString();

            String[] split = headerData.split("\n");


            // 4. Response
            String body = "This is body";
            byte[] bytes = body.getBytes();
            String message = ""
                    + "HTTP/1.1 200 OK\n"
                    + "Content-type: text/html; charset=UTF-8\n"
                    + "Content-Length: " + bytes.length + "\n\n" + body;

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
            socket.close();
        }
    }

}

package kr.megaptera.assignment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);
        System.out.println("Listen");

        while(true) {
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            InputStream inputStream = socket.getInputStream();
            Reader reader = new InputStreamReader(inputStream);


            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();

            // 4. Response
            int charBufferLength = charBuffer.toString().split("\n").length;
            String taskStr = charBuffer.toString().split("\n")[charBufferLength-1];
            String method = charBuffer.toString().split("\n")[0].split("/")[0];

            if(taskStr.compareTo("") == 1) taskStr = "{}";

            String message = "";

            if(method.compareTo("GET") == 1) {
                String body = taskStr;
                byte[] bytes = body.getBytes();

                message = "" +
                        "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + bytes.length + "\n" +
                        "Content-Type: text/html;charset=UTF-8\n" +
                        "Host: localhost:8080" +
                        "\n" +
                        JsonParser.parseString(body);
            }
            else if(method.compareTo("POST") == 1) {

            }
            else if(method.compareTo("PATCH") == 1) {

            }
            else {

            }

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. Close
            socket.close();

        }
    }

}

package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
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

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);

        // 2. Accept
        Socket socket = listener.accept();



        // 3. Request
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);

        reader.read(charBuffer);
        charBuffer.flip();

        String[] parts = charBuffer.toString().split(" ");
        String httpMethod = parts[0];
        String path = parts[1];



        // 4. Response
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        int statusCode = 0;
        String statusText = null;

        if(httpMethod.equals("GET") && path.equals("/tasks")){ // 목록 얻기
            statusCode = 200;
            statusText = "OK";
        }

        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + statusCode + " " + statusText + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: localhost:" + port + "\n" +
                "\n" +
                body;

        writer.write(message);
        writer.flush();


        socket.close();
    }

}

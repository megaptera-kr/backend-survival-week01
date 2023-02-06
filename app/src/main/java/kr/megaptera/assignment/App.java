package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static Long taskId = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private String getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();
    }

    private String getResponse(String request, Map<Long, String> tasks) {
        String httpMethod = request.split(" ")[0];
        String path = request.split(" ")[1];
        int statusCode = 0;
        String body = "";

        return createMessage(statusCode, body);
    }

    private String createMessage(int statusCode, String body) {
        return  "" +
                "HTTP/1.1 " + statusCode + " OK\n" +
                "Content-Length: " + body.getBytes().length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                body;
    }

    private void run() throws IOException {
        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(8080, 0);
        System.out.println("Listen!");

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            String request = getRequest(socket);

            // 4. Response
            String response = getResponse(request, tasks);

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(response);
            writer.flush();

            // 5. Close
            socket.close();
        }
    }
}

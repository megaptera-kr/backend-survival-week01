package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();
        Long taskId = 1L;

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            // 3. Request
            String request = charBuffer.toString();
            String[] requestMessage = request.split(" ");

            String requestMethod = requestMessage[0];
            String path = requestMessage[1];
            int code = 0;
            String statusMessage = "";
            String response = "";

            String[] requestLines = request.split("\n");
            String requestBody = requestLines[requestLines.length - 1];

            JsonElement element = JsonParser.parseString(requestBody);

            // 4. Response
            if (requestMethod.equals("GET")) {
                code = 200;
                response = new Gson().toJson(tasks);
            }

            if (requestMethod.equals("POST")) {
                if (requestBody.isBlank()) {
                    code = 400;
                }
                if (!requestBody.isBlank()) {
                    code = 201;

                    String task = element.getAsJsonObject().get("task").getAsString();
                    tasks.put(taskId++, task);

                    response = new Gson().toJson(tasks);
                }
            }

            if (requestMethod.equals("PATCH")) {
                if (requestBody.isBlank()) {
                    code = 400;
                }

                if (!requestBody.isBlank()) {
                    Long modifyingTaskId = Long.valueOf(path.substring(path.length() - 1));

                    if (!tasks.containsKey(modifyingTaskId)) {
                        code = 404;
                    }

                    if (tasks.containsKey(modifyingTaskId)) {
                        code = 200;
                        tasks.put(modifyingTaskId, element.getAsJsonObject().get("task").getAsString());
                        response = new Gson().toJson(tasks);
                    }
                }
            }

            if (requestMethod.equals("DELETE")) {
                Long deletingTaskId = Long.valueOf(path.substring(path.length() - 1));

                if (!tasks.containsKey(deletingTaskId)) {
                    code = 404;
                }

                if (tasks.containsKey(deletingTaskId)) {
                    code = 200;
                    tasks.remove(deletingTaskId);
                    response = new Gson().toJson(tasks);
                }
            }

            switch (code) {
                case 200 -> statusMessage = "OK";
                case 201 -> statusMessage = "Created";
                case 400 -> statusMessage = "Bad Request";
                case 404 -> statusMessage = "Not Found";
            }

            byte[] bytes = response.getBytes();
            String responseMessage = "" +
                "HTTP/1.1 " + code + " " + statusMessage + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: localhost:8080\n" +
                "\n" +
                response +
                "";

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(responseMessage);
            writer.flush();

            socket.close();
        }
    }

}

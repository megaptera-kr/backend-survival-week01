package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();


            // 3. Request
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);

            reader.read(charBuffer);
            charBuffer.flip();
            String data = charBuffer.toString();

            String[] lines = data.split("\n");
            String[] startLineParts = lines[0].split(" ");

            String method = startLineParts[0];
            String path = startLineParts[1];


            // 4. Response
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

            int statusCode = 0;
            String statusText = null;
            String responseBody = null;
            int bytesLength = 0;


            if (method.equals("GET") && path.equals("/tasks")) { // 목록 얻기
                responseBody = new Gson().toJson(tasks);
                bytesLength = responseBody.getBytes().length;

                statusCode = 200;
                statusText = "OK";

            } else if (method.equals("POST") && path.equals("/tasks")) { // 생성하기
                String requestBody = data.split("\n\r")[1];

                if (requestBody.equals("\n")) { // body data가 없을 경우
                    responseBody = "\n";
                    bytesLength = 0;

                    statusCode = 400;
                    statusText = "Bad Request";


                } else {
                    JsonElement jsonElement = JsonParser.parseString(requestBody);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    String task = jsonObject.get("task").getAsString();

                    tasks.put(taskId, task);
                    taskId++;

                    responseBody = new Gson().toJson(tasks);
                    bytesLength = responseBody.getBytes().length;

                    statusCode = 201;
                    statusText = "Created";
                }

            } else if (method.equals("PATCH") && path.contains("/tasks/")) { // 수정하기
                String requestBody = data.split("\n\r")[1];
                long requestId = Long.parseLong(path.split("/")[2]);

                JsonElement jsonElement = JsonParser.parseString(requestBody);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                String task = jsonObject.get("task").getAsString();

                tasks.put(requestId, task);

                responseBody = new Gson().toJson(tasks);
                bytesLength = responseBody.getBytes().length;

                statusCode = 200;
                statusText = "OK";
            }


            String message = "" +
                    "HTTP/1.1 " + statusCode + " " + statusText + "\n" +
                    "Content-Length: " + bytesLength + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Host: localhost:" + port + "\n" +
                    "\n" +
                    responseBody;

            writer.write(message);
            writer.flush();


            socket.close();
        }
    }
}

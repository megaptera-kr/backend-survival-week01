package kr.megaptera.assignment;

import com.google.gson.Gson;
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
            String responseBody = "\n";
            int bytesLength = 0;

            Gson gson = new Gson();


            if (method.equals("GET") && path.equals("/tasks")) { // 목록 얻기
                responseBody = gson.toJson(tasks);
                bytesLength = responseBody.getBytes().length;

                statusCode = 200;
                statusText = "OK";


            } else if (method.equals("POST") && path.equals("/tasks")) { // 생성하기
                String requestBody = data.split("\n\r")[1];

                if (requestBody.equals("\n")) { // body data가 없을 경우
                    statusCode = 400;
                    statusText = "Bad Request";

                } else {
                    String task = JsonParser.parseString(requestBody).getAsJsonObject().get("task").getAsString();

                    tasks.put(taskId, task);
                    taskId++;

                    responseBody = gson.toJson(tasks);
                    bytesLength = responseBody.getBytes().length;

                    statusCode = 201;
                    statusText = "Created";
                }


            } else if (method.equals("PATCH") && path.contains("/tasks/")) { // 수정하기
                String requestBody = data.split("\n\r")[1];
                long requestId = Long.parseLong(path.split("/")[2]);

                if (!(tasks.containsKey(requestId))) { // 존재하지 않는 id로 요청할 경우
                    statusCode = 404;
                    statusText = "Not Found";

                } else if (requestBody.equals("\n")) { // body data가 없을 경우
                    statusCode = 400;
                    statusText = "Bad Request";

                } else {
                    String task = JsonParser.parseString(requestBody).getAsJsonObject().get("task").getAsString();

                    tasks.put(requestId, task);

                    responseBody = gson.toJson(tasks);
                    bytesLength = responseBody.getBytes().length;

                    statusCode = 200;
                    statusText = "OK";
                }


            } else if (method.equals("DELETE") && path.contains("/tasks/")) { // 삭제하기
                long requestId = Long.parseLong(path.split("/")[2]);

                if (!(tasks.containsKey(requestId))) { // 존재하지 않는 id로 요청할 경우
                    statusCode = 404;
                    statusText = "Not Found";

                } else {
                    tasks.remove(requestId);

                    responseBody = gson.toJson(tasks);
                    bytesLength = responseBody.getBytes().length;

                    statusCode = 200;
                    statusText = "OK";
                }
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

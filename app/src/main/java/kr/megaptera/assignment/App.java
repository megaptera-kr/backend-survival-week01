package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 소켓을 사용한 HTTP 서버
public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    // 필요 모듈정리 : ServerSocket, Socket
    private void run() throws IOException {
        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port, 0);
        Map<Long, String> tasks = new HashMap<>();
        Long mapIndex = 0L;

        while (true) {
            // 2. Accept
            Socket socket = serverSocket.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String[] arr = charBuffer.toString().split("\\s");

            switch (arr[0]) {
                case "GET":
                    getRequest(arr[1], socket, tasks);
                    break;
                case "POST":
                    mapIndex++;
                    postRequest(arr[1], socket, tasks, charBuffer, mapIndex);
                    break;
                case "PATCH":
                    break;
                case "DELETE":
                    break;
            }

            socket.close();
        }

    }

    private void postRequest(String path, Socket socket, Map<Long, String> tasks, CharBuffer charBuffer, Long mapIndex) throws IOException {
        if (path.equals("/tasks")) {
            Pattern pattern = Pattern.compile("\\{.*\\}");
            Matcher matcher = pattern.matcher(charBuffer.toString());
            if (matcher.find()) {
                String jsonBody = matcher.group();
                Gson gson = new Gson();
                JsonElement jsonElement = gson.fromJson(jsonBody, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String task = jsonObject.get("task").getAsString();
                tasks.put(mapIndex, task);
                responseBody(socket, tasks, "POST");
            }
        }
    }

    private void getRequest(String path, Socket socket, Map<Long, String> tasks) throws IOException {
        if (path.equals("/tasks")) {
            responseBody(socket, tasks, "GET");
        }
    }

    private void responseBody(Socket socket, Map<Long, String> tasks, String httpMethod) throws IOException {
        // 4. Response
        String body = new Gson().toJson(tasks);
        String message = "";
        byte[] bytes = body.getBytes();

        if (httpMethod.equals("GET")) {
            message = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json; charset=UTF-8
                    Content-Length:""" + bytes.length + "\n" + "\n" + body;
        } else if (httpMethod.equals("POST")) {
            message = """
                    HTTP/1.1 201 Created
                    Content-Type: application/json; charset=UTF-8
                    Content-Length:""" + bytes.length + "\n" + "\n" + body;
        }


        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}

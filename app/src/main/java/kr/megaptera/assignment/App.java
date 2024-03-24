package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParser.*;

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

            System.out.println(charBuffer.toString());



            // 4. Response
            String[] requestInfo = charBuffer.toString().split("\n");
            String method = requestInfo[0];

            int charBufferLength = charBuffer.toString().split("\n").length;
            String taskStr = charBuffer.toString().split("\n")[charBufferLength-1];

            String message = "";

            String body = taskStr;

            byte[] bytes = body.getBytes();

            if(method.startsWith("GET /tasks")) {
                if(taskStr.compareTo("") == 1) body = "{}";
                message = "" +
                        "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + bytes.length + "\n" +
                        "Content-Type: text/html;charset=UTF-8\n" +
                        "Host: localhost:8080\n" +
                        "\n" +
                        JsonParser.parseString(body);
            } else if (method.startsWith("POST /tasks")) {
                if(taskStr.compareTo("") == 1) {
                    message = "" +
                            "HTTP/1.1 400 Bad Request\n" +
                            "Content-Length: 0" + "\n" +
                            "Content-Type: text/html;charset=UTF-8\n" +
                            "Host: localhost:8080\n" +
                            "\n" +
                            "\n";
                }
                else {
                    JsonElement taskJson = JsonParser.parseString(taskStr);
                    String taskName = taskJson.getAsJsonObject().get("task").getAsString();
                    Long taskId = (long)(tasks.size() + 1);
                    tasks.put(taskId, taskName);

                    Gson gson = new Gson();
                    String jsonStr = gson.toJson(tasks);
//                    MapToJson(tasks);
                    System.out.println("bfjgbgkjbjgbk====> " + JsonParser.parseString(jsonStr));
                    message = "" +
                            "HTTP/1.1 201 Created\n" +
                            "Content-Length: " + bytes.length + "\n" +
                            "Content-Type: application/json; charset=UTF-8\n" +
                            "Host: localhost:8080\n" +
                            "\n" +
                            JsonParser.parseString(jsonStr);

                }
            }
//            String method = charBuffer.toString().split("\n")[0].split("/")[0];
//
//

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. Close
            socket.close();

        }
    }

    private String MapToJson(Map<Long, String> tasks){
        Gson gson = new Gson();
        for(int i = 0; i < tasks.size(); i++) {
            long key = (long)(i+1);
            String val = tasks.get(key);

        }
        return "";
    }

}

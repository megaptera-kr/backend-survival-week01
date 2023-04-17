package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
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
        Long number = 0L;
        String status = "";

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.
        // 1. Listen
        ServerSocket listner = new ServerSocket(port, 0);
        while (true) {
            // 2. Accept
            try (Socket socket = listner.accept()) {

                // 3. Request
                String request = createRequest(socket);
                String[] line = request.split("\n");
                String method = request.split("\n")[0];
                if (method.contains("GET")) {
                    status = "200 OK";
                } else if (method.contains("POST")) {
//                    System.out.println("포스트 매핑 접속");
                    if (line[line.length - 1].contains("task")) {
                        String todo = getTodo(line);
//                        System.out.println("투두: " + todo);
                        tasks.put(number += 1L,
                                todo);
                        status = "201 Create";
                    } else {
                        status = "400 Bad Request";
                    }
                } else if (method.contains("PATCH")) {
//                    System.out.println("패치 매핑 접속");
                    Long num = Long.valueOf(method.split("/")[2].trim().split("H")[0].trim());
                    if (tasks.get(num) != null) {
                        if (line[line.length - 1].contains("task")) {
                            String todo = getTodo(line);
                            tasks.replace(num, todo);
                            status = "200 OK";
                        } else {
                            status = "400 Bad Request";
                        }
                    } else {
                        status = "404 Not Found";
                    }
                } else if (method.contains("DELETE")) {
//                    System.out.println("딜리트 매핑 접속");
                    Long num = Long.valueOf(method.split("/")[2].trim().split("H")[0].trim());
                    if (tasks.get(num) != null) {
                        tasks.remove(num);
                        status = "200 OK";
                    } else {
                        status = "404 Not Found";
                    }
                }
                // 4. Response
//                System.out.println("맵 스트링 : " + tasks.toString());
                String json = new Gson().toJson(tasks);
//                System.out.println("제이슨 변환 : " + json);
                byte[] bytes = new Gson().toJson(tasks).getBytes();
                if (status.contains("200") || status.contains("201")) {
                    String message = "" +
                            "HTTP/1.1 " + status + "\n" +
                            "Content-Length:" + bytes.length + "\n" +
                            "Content-Type: application/json; charset=UTF-8\n" +
                            line[1] + "\n" +
                            "\n" +

                            json;
                    Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    writer.write(message);
                    writer.flush();
                    writer.close();
                } else {

                    String message = "" +
                            "HTTP/1.1 " + status + "\n" +
                            "Content-Length:" +  "0 \n" +
                            "Content-Type: application/json; charset=UTF-8\n" +
                            line[1] + "\n" +
                            "\n";
                    Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    writer.write(message);
                    writer.flush();
                    writer.close();
                }
            }
        }
    }

    private static String getTodo(String[] line) {
        String data = line[line.length -1];
        JsonElement jsonElement = JsonParser.parseString(data);
        JsonObject object = jsonElement.getAsJsonObject();
        String todo = object.get("task").getAsString();
        return todo;
    }

    private static String createRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        String request = charBuffer.toString();
        return request;
    }
}

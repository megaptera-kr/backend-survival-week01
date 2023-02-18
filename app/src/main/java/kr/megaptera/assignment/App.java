package kr.megaptera.assignment;


import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {
    public static List<String> methods = List.of("GET", "POST", "PATCH", "PUT", "DELETE");
    public static Long idx = 1L;
    public static void main(String[] args) throws IOException {

private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();
        // 1. Listen
        ServerSocket listener = new ServerSocket(port,0);
        System.out.println("Listen!");

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Readable reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String headerData = charBuffer.toString();
            List<String> lines = headerData.lines().collect(Collectors.toList());
            RequestHeader request = setHeader(lines);

            request.setBody(extractBody(lines));
            String message;
            if (methods.contains(request.getMethod())) {
                message = createMessageByMethod(request, tasks);
            } else {
                throw new RuntimeException("잘못된 method 요청입니다.");
            }

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
            socket.close();
        }
    }

    private RequestHeader setHeader(List<String> lines) {
        String firstLine = lines.get(0);
        RequestHeader requestHeader = new RequestHeader();
        String[] s = firstLine.split(" ");
        String[] split = s[1].split("/");
        requestHeader.setMethod(s[0]);
        requestHeader.setPath(s[1]);
        if(split.length > 2) {
            requestHeader.setKey(Long.parseLong(split[2]));
        }
        requestHeader.setProtocol(s[2].replaceAll("\r", ""));
        return requestHeader;
    }

    public static Long generateIdx(){
        return idx++;
    }

    private String parsePayload(RequestHeader requestHeader) {
        JsonElement jsonElement = JsonParser.parseString(requestHeader.getBody());
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        return asJsonObject.get("task").getAsString();
    }

    private String createMessageByMethod(RequestHeader requestHeader, Map<Long, String> tasks) {
        String message = "";
        Gson gson = new Gson();
        String method = requestHeader.getMethod();

        switch (method) {
            case "GET" -> {
                String statusCode = "200 OK";
                String serializeString = gson.toJson(tasks);
                byte[] bytes = serializeString.getBytes();
                message += requestHeader.getProtocol() + " " + statusCode + "\n"
                        + "Content-Length: " + bytes.length + "\n"
                        + "Content-type: text/html; charset=UTF-8\n\n" +
                        serializeString;
                System.out.println("get message : " + message);
                return message;
            }
            case "POST" -> {
                String statusCode = "201 Created";
                if (Strings.isNullOrEmpty(requestHeader.getBody())) {
                    statusCode = "400 Bad Request";
                } else {
                    String task = parsePayload(requestHeader);
                    tasks.put(generateIdx(), task);
                }
                String serializeString = gson.toJson(tasks);
                byte[] bytes = serializeString.getBytes();
                message += requestHeader.getProtocol() + " " + statusCode + "\n"
                        + "Content-Length: " + bytes.length + "\n"
                        + "Content-type: text/html; charset=UTF-8\n\n" +
                        serializeString;
                System.out.println("post message : " + message + " body : " + requestHeader.getBody());
                return message;
            }
            case "PATCH" -> {
                Long key = requestHeader.getKey();
                String value = tasks.get(key);
                String statusCode = "200 OK";
                if (Strings.isNullOrEmpty(value)) {
                    statusCode = "404 Not Found";
                } else if (Strings.isNullOrEmpty(requestHeader.getBody())) {
                    statusCode = "400 Bad Request";
                } else {
                    String task = parsePayload(requestHeader);
                    tasks.put(key, task);
                }
                String serializeString = gson.toJson(tasks);
                byte[] bytes = serializeString.getBytes();
                message += requestHeader.getProtocol() + " " + statusCode + "\n"
                        + "Content-Length: " + bytes.length + "\n"
                        + "Content-type: text/html; charset=UTF-8\n\n" +
                        serializeString;
                System.out.println("patch message : " + message + " body : " + requestHeader.getBody());
                return message;
            }
            case "DELETE" -> {
                String statusCode = "200 OK";
                if (Strings.isNullOrEmpty(tasks.get(requestHeader.getKey()))) {
                    statusCode = "404 Not Found";
                } else {
                    tasks.remove(requestHeader.getKey());
                }
                message += requestHeader.getProtocol() + " " + statusCode + "\n"
                        + "Content-type: text/html; charset=UTF-8\n\n";
                System.out.println("delete message : " + message+ " body : " + requestHeader.getBody());
                return message;
            }
            default -> throw new RuntimeException("잘못된 method 요청입니다.");
        }
    }

    private String extractBody(List<String> lines) {
        int i = lines.indexOf("");
        StringBuilder sb = new StringBuilder();
        for(int j = i; j < lines.size(); j++){
            sb.append(lines.get(j));
        }
        return sb.toString();
    }
}

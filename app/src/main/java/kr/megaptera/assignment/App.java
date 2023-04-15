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
enum HttpMethod {
    GET, POST, PATCH, DELETE
}

public class App {
    private final int ZERO = 0;
    private final String BODY_JSON_REGEX = "\\{.*\\}";
    private final String OK_STATUS_MESSAGE = """
            HTTP/1.1 200 OK
            Content-Type: application/json; charset=UTF-8
            Content-Length:""";
    private final String CREATE_STATUS_MESSAGE = """
            HTTP/1.1 201 Created
            Content-Type: application/json; charset=UTF-8
            Content-Length:""";
    private final String BAD_REQUEST_MESSAGE = """
            HTTP/1.1 400 Bad Request
            Content-Type: application/json; charset=UTF-8
            Content-Length:""" + ZERO + "\n" + "\n";
    private final String NOT_FOUND_MESSAGE = """
            HTTP/1.1 404 Not Found
            Content-Type: application/json; charset=UTF-8
            Content-Length:""" + ZERO + "\n" + "\n";

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

            mapIndex = routingHandler(arr, socket, tasks, mapIndex, charBuffer);

            System.out.println("socket close");
            socket.close();
        }
    }

    private void deleteRequest(String path, Socket socket, Map<Long, String> tasks) throws IOException {
        String[] pathParsing = pathParameterParsing(path);
        if (pathParsing[1].equals("tasks")) {
            Long pathParameter = Long.valueOf(pathParsing[pathParsing.length - 1]);
            boolean isCheckKey = tasks.containsKey(pathParameter);
            if (!isCheckKey) {
                notFoundResponse(socket);
                return;
            }
            tasks.remove(pathParameter);
            responseBody(socket, tasks, HttpMethod.DELETE);
        }
    }

    private void patchRequest(String path, Socket socket, Map<Long, String> tasks, CharBuffer charBuffer) throws IOException {
        String[] pathParsing = pathParameterParsing(path);
        if (pathParsing[1].equals("tasks")) {
            Matcher matcher = requestBodyJsonParser(charBuffer);
            if (matcher.find()) {
                Long pathParameter = Long.valueOf(pathParsing[pathParsing.length - 1]);
                boolean isCheckKey = tasks.containsKey(pathParameter);
                if (!isCheckKey) {
                    notFoundResponse(socket);
                    return;
                }
                tasks.put(pathParameter, jsonDataExtract(matcher));
                responseBody(socket, tasks, HttpMethod.PATCH);
                return;
            }
            badRequestResponse(socket);
        }
    }

    private Long postRequest(String path, Socket socket, Map<Long, String> tasks, CharBuffer charBuffer, Long mapIndex) throws IOException {
        if (path.equals("/tasks")) {
            Matcher matcher = requestBodyJsonParser(charBuffer);
            if (matcher.find()) {
                tasks.put(++mapIndex, jsonDataExtract(matcher));
                responseBody(socket, tasks, HttpMethod.POST);
                return mapIndex;
            }
            badRequestResponse(socket);
        }
        return mapIndex;
    }

    private void getRequest(String path, Socket socket, Map<Long, String> tasks) throws IOException {
        if (path.equals("/tasks")) {
            responseBody(socket, tasks, HttpMethod.GET);
        }
    }

    private void responseBody(Socket socket, Map<Long, String> tasks, HttpMethod httpMethod) throws IOException {
        // 4. Response
        String body = new Gson().toJson(tasks);
        String message = "";
        byte[] bytes = body.getBytes();

        if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.PATCH || httpMethod == HttpMethod.DELETE) {
            message = OK_STATUS_MESSAGE + bytes.length + "\n" + "\n" + body;
        } else if (httpMethod == HttpMethod.POST) {
            message = CREATE_STATUS_MESSAGE + bytes.length + "\n" + "\n" + body;
        }
        bufferWrite(message, socket);
    }

    private void badRequestResponse(Socket socket) throws IOException {
        bufferWrite(BAD_REQUEST_MESSAGE, socket);
    }

    private void notFoundResponse(Socket socket) throws IOException {
        bufferWrite(NOT_FOUND_MESSAGE, socket);
    }

    private void bufferWrite(String message, Socket socket) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private Long routingHandler(String[] arr, Socket socket, Map<Long, String> tasks, Long mapIndex, CharBuffer charBuffer) throws IOException {
        switch (arr[0]) {
            case "GET":
                getRequest(arr[1], socket, tasks);
                break;
            case "POST":
                mapIndex = postRequest(arr[1], socket, tasks, charBuffer, mapIndex);
                break;
            case "PATCH":
                patchRequest(arr[1], socket, tasks, charBuffer);
                break;
            case "DELETE":
                deleteRequest(arr[1], socket, tasks);
                break;
        }
        return mapIndex;
    }

    private Matcher requestBodyJsonParser(CharBuffer charBuffer) {
        Pattern pattern = Pattern.compile(BODY_JSON_REGEX);
        Matcher matcher = pattern.matcher(charBuffer.toString());
        return matcher;
    }

    private String jsonDataExtract(Matcher matcher) {
        String jsonBody = matcher.group();
        JsonElement jsonElement = new Gson().fromJson(jsonBody, JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String task = jsonObject.get("task").getAsString();
        return task;
    }

    private String[] pathParameterParsing(String path) {
        String[] pathParsing = path.split("/");
        return pathParsing;
    }
}

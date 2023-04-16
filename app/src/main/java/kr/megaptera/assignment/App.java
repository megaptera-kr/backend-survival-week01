package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final Map<Long, String> todoList = new HashMap<>();
    private static Long sequence = 0L;

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.run();
    }

    private void run() throws Exception {
        int port = 8080;


        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen

        ServerSocket listener = new ServerSocket(port, 0);

        System.out.println("Listen!");
        while (true) {
            // 2. Accept

            Socket socket = listener.accept();

            System.out.println("Accept!");

            // 3. Request
            String requestMessage = getRequestMessage(socket);
            System.out.println(requestMessage);


            // 4. Response
            String httpMethod = checkHttpMethod(requestMessage);
            sendRequestMessage(socket, createResponse(requestMessage, httpMethod));

            // 5. close

            socket.close();
        }

    }

    private String createResponse(String requestMessage, String httpMethod) throws Exception {
        if (httpMethod == "GET") {
            String body = getMapToJson();
            return createResponseMessage(body, "200 OK");

        } else if (httpMethod == "POST") {
            String jsonBracket = getJsonBracketFromHttpRequestMessage(requestMessage);

            String todoValue = getJsonValueAsString(jsonBracket);

            if (todoValue == null) {
                return createResponseMessage("", "400 Bad Request");
            }
            todoList.put(++sequence, todoValue);

            String body = getMapToJson();
            return createResponseMessage(body, "201 Created");
        } else if (httpMethod == "PATCH") {
            String requestMessages = getFirstLineOfRequestMessage(requestMessage);
            String[] split = requestMessages.split("/");
            Long id = Long.valueOf(split[split.length - 1].trim());
            if (!todoList.containsKey(id)) {
                return createResponseMessage("", "404 Not Found");
            }

            String jsonBracket = getJsonBracketFromHttpRequestMessage(requestMessage);

            String todoValue = getJsonValueAsString(jsonBracket);

            if (todoValue == null) {
                return createResponseMessage("", "400 Bad Request");
            }

            todoList.put(id, todoValue);

            String body = getMapToJson();
            return createResponseMessage(body, "200 OK");

        } else if (httpMethod == "DELETE") {
            String requestMessages = getFirstLineOfRequestMessage(requestMessage);
            String[] split = requestMessages.split("/");
            Long id = Long.valueOf(split[split.length - 1].trim());
            if (!todoList.containsKey(id)) {
                return createResponseMessage("", "404 Not Found");
            }

            todoList.remove(id);

            String body = getMapToJson();
            return createResponseMessage(body, "200 OK");

        }
        return "";
    }

    private String getJsonValueAsString(String jsonBracket) {


        try {
            JsonElement element = JsonParser.parseString(jsonBracket);
            return element.getAsJsonObject().get("task").getAsString();
        } catch (Exception e) {
            return null;
        }


    }

    private static String getFirstLineOfRequestMessage(String requestMessage) {
        return requestMessage.split("HTTP/1.1")[0];
    }

    private String checkHttpMethod(String requestMessage) {
        if (isGetMethod(requestMessage))
            return "GET";
        else if (isPostMethod(requestMessage))
            return "POST";
        else if (isPatchMethod(requestMessage))
            return "PATCH";
        else if (isDeleteMethod(requestMessage))
            return "DELETE";

        throw new NullPointerException("invalid HTTP METHOD REQUEST");
    }

    private static boolean isDeleteMethod(String requestMessage) {
        return requestMessage.startsWith("DELETE");
    }

    private static boolean isPatchMethod(String requestMessage) {
        return requestMessage.startsWith("PATCH");
    }

    private static boolean isPostMethod(String requestMessage) {
        return requestMessage.startsWith("POST");
    }

    private static boolean isGetMethod(String requestMessage) {
        return requestMessage.startsWith("GET");
    }

    private String getJsonBracketFromHttpRequestMessage(String requestMessage) {
        String[] requestMessageTokens = getSplitRequestMessageByEnter(requestMessage);
        return getJsonBracket(requestMessageTokens);
    }

    private static String getJsonBracket(String[] requestMessageTokens) {
        return requestMessageTokens[requestMessageTokens.length - 1];
    }

    private static String[] getSplitRequestMessageByEnter(String requestMessage) {
        return requestMessage.split("\n");
    }

    private String getMapToJson() {
        return new Gson().toJson(todoList);
    }

    private static void sendRequestMessage(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }


    private String getRequestMessage(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);

        charBuffer.flip();

        String requestMessage = charBuffer.toString();

        return requestMessage;
    }

    String createResponseMessage(String body, String statusCode) {
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: localhost:8080\n" +
                "\n" +
                body;
        return message;
    }

//    String createResponseMessageWithoutBody(String statusCode) {
//        String message = "" +
//                "HTTP/1.1 " + statusCode + "\n" +
//                "Content-Length: " + 0 + "\n" +
//                "Content-Type: application/json; charset=UTF-8\n" +
//                "Host: localhost:8080\n" +
//                "\n";
//
//        return message;
//    }


}

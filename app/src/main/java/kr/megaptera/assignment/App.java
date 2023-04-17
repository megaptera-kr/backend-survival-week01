package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class App {
//    public static Long newId = 0L;

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
        System.out.println("Listen!");

        // 2. Accept
        while(true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();
//            System.out.println(charBuffer.toString());

            // 4. Response
            String message = getResponseMsg(tasks, charBuffer.toString());

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
        }
    }

    private String getResponseMsg(Map param, String msg) {
        String body = getRequestTypeMsg(param, msg);
        return body;
    }

    private String getRequestTypeMsg(Map param, String msg) {
//        System.out.println(msg);
//        String type = msg.substring(0, msg.indexOf("HTTP"));

        if (msg.contains("GET")) {
            return taskProcess(param, msg, "GET");
        } else if (msg.contains("POST")) {
            return taskProcess(param, msg, "POST");
        } else if (msg.contains("PATCH")) {
            return taskProcess(param, msg, "PATCH");
        } else if (msg.contains("DELETE")) {
            return taskProcess(param, msg, "DELETE");
        }

        return getMsg("", "400 Bad Request");
    }

    private String taskProcess(Map param, String msg, String method) {
        String content = new Gson().toJson(param);
        String returnVal = "";

        if(method.equals("GET")) {
            returnVal = getMsg(content, "200 OK");
        } else if(method.equals("POST")) {
            String task = getPayload(msg, "task");
            if (task.equals("")) {
                return getMsg("", "400 Bad Request");
            }

            Long maxKey = makeNewId(param);
            param.put(maxKey+1, task);
            content = new Gson().toJson(param);

            returnVal = getMsg(content, "201 Created");
        } else if (method.equals("PATCH")) {
            Long id = getId(msg);
            String task = getPayload(msg, "task");
            if (!param.containsKey(id)) {
                System.out.println("here!");
                return getMsg("", "404 Not Found");
            }
            if (task.equals("")) {
                return  getMsg("", "400 Bad Request");
            }
            param.put(id, task);
            content = new Gson().toJson(param);

            return getMsg(content, "200 OK");
        } else if (method.equals("DELETE")) {
            Long id = getId(msg);
            if (!param.containsKey(id)) {
                return getMsg("", "404 Not Found");
            }
            param.remove(id);
            content = new Gson().toJson(param);

            return getMsg(content, "200 OK");
        }

//        System.out.println(returnVal);

        return returnVal;

    }

    private String getMsg(String body, String status) {
        byte[] bytes = body.getBytes();

        return "HTTP/1.1 " + status + "\n" +
                "Host: localhost:8080\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                body;
    }

    private String getPayload(String msg, String key) {
        String[] lists = msg.split("\n");
        String bodyStr = lists[lists.length - 1];

        try {
            JsonObject jObj = (JsonParser.parseString(bodyStr)).getAsJsonObject();
            return jObj.get(key).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private Long getId(String method) {
        String[] parts = method.split("/");
        return Long.parseLong(parts[2].replace("HTTP", "").trim());
    }

    private Long makeNewId(Map param) {
        Map.Entry<Long, Integer> maxEntry = null;

        Set<Map.Entry<Long, Integer>> entrySet = param.entrySet();
        for (Map.Entry<Long, Integer> entry : entrySet) {
            maxEntry = entry;
        }

        Long maxKey = Long.valueOf(1);
        if(maxEntry != null) {
            maxKey = maxEntry.getKey();
        }

        return maxKey;
    }

}

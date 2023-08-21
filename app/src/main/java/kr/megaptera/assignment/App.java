package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    Long seq = 1L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();
        // 1. Listen
        ServerSocket listener = new ServerSocket(8080);
        System.out.println("** Listen!");

        // 2. Accept
        while (true) {
            Socket socket = listener.accept();
            System.out.println("** Accept!");

            // 3. Request
            String request = readRequest(socket);
            System.out.println("** request: " + request + "**");

            String message = "";

            if (request.contains("GET")) {
                message = read("200 OK", tasks);
            }
            else if(request.contains("POST")) {
                if(!request.contains("{"))
                    message = makeMessage("","400 Bad Request");
                else
                    message = save("201 Created", tasks, request);
            }
            else if(request.contains("PATCH")) {
                Long key = getKey(request);

                if(!request.contains("{"))
                    message = makeMessage("","400 Bad Request");
                else if(!tasks.containsKey(key))
                    message = makeMessage("","404 Not Found");
                else
                    message = update("200 OK", tasks, request, key);
            }
            else if(request.contains("DELETE")) {
                Long key = getKey(request);

                if(!tasks.containsKey(key))
                    message = makeMessage("","404 Not Found");
                else
                    message = delete("200 OK", tasks, key);
            }

            // 4. Response
            writeResponse(socket, message);

            // 5. Close
            socket.close();
        }
    }

    private String delete(String status, Map<Long, String> tasks, Long key) {
        //delete
        tasks.remove(key);
        String body = new Gson().toJson(tasks);
        return makeMessage(body, status);
    }

    private String update(String status, Map<Long, String> tasks, String request, Long key){
        JsonObject jsonObject = getJsonObject(request);

        //update
        tasks.put(key,jsonObject.get("task").getAsString());
        String body = new Gson().toJson(tasks);
        return makeMessage(body, status);
    }

    private String save(String status, Map<Long, String> tasks, String request){
        JsonObject jsonObject = getJsonObject(request);

        //save
        tasks.put(seq++,jsonObject.get("task").getAsString());
        String body = new Gson().toJson(tasks);
        return makeMessage(body, status);
    }

    private String read(String status, Map<Long, String> tasks) {
        String body = new Gson().toJson(tasks);
        return makeMessage(body, status);
    }

    private String makeMessage(String body, String status) {
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + status + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "\n" +
                body;
        return message;
    }

    private Long getKey(String request) {
        String[] parts = request.split("/");
        Long key;
        if(parts[2].substring(0,1).trim().equals("-"))
            key = -Long.parseLong(parts[2].substring(1,2));
        else
            key = Long.parseLong(parts[2].substring(0,1));
        return key;
    }

    private String readRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();
    }

    private JsonObject getJsonObject(String request) {
        String content = request.substring(request.indexOf("{"));
        JsonElement jsonElement = JsonParser.parseString(content);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;
    }

    private void writeResponse(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
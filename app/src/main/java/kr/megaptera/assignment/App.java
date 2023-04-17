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
    public static Long newId = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        // 포트 열어주기
        ServerSocket listener = new ServerSocket(8080, 0);

        System.out.println("Listen!");


        // 무한 루프
        while (true) {
            // 2. Accept
            // 클라이언트 대기
            Socket socket =  listener.accept();

            System.out.println("Accept!");

            // 3. Request
            String request = getRequest(socket);

            System.out.println("request: " + request);

            // 4. Response
            String message = process(tasks, request);
            writeMessage(socket, message);
        }


    }

    // 3. Request
    // 서버입장에서는 요청을 받는 것이기 때문에 read를 먼저해준다. 즉, 먼저 읽어줘야한다.
    private String getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000); // 1,000,000개의 문자를 저장할 수 있는 버퍼를 생성
        reader.read(charBuffer);

        charBuffer.flip(); // flip() : 버퍼 포지션(position)과 limit을 조정하여 읽기(read) 모드에서 쓰기(write) 모드로 전환합니다.

        return charBuffer.toString();
    }

    // HTTP 요청(request) 문자열에서 HTTP 요청 메소드를 추출하는 메소드
    private String getRequstMethod(String request) {
        return request.substring(0, request.indexOf("HTTP"));
    }

    // getRequstMethod에서 추출한 요청 메소드를 이용해 해당 응답을 보내준다.
    private String process(Map<Long, String> tasks, String request) {
        String method = getRequstMethod(request);

        // startsWith : 문자열이 특정 문자열 또는 문자열 접두사(prefix)로 시작하는지를 검사하는 메소드
        if (method.startsWith("GET /tasks")){
            return processGetTask(tasks);
        }

        if(method.startsWith("POST /tasks")){
            return processPostTask(tasks, request);
        }

        if (method.startsWith("PATCH /tasks/")){
            return processPatchTask(tasks, request, method);
        }

        if (method.startsWith("DELETE /tasks/")){
            return processDeleteTask(tasks, method);
        }

        return generateMessage("", "400 Bad Request");
    }

    private String processDeleteTask(Map<Long, String> tasks, String method) {
        Long id = parseTaskId(method);

        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }
        tasks.remove(id);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");

    }

    private String processPatchTask(Map<Long, String> tasks, String request, String method) {
        Long id = parseTaskId(method);

        if (!tasks.containsKey(id)){
            return generateMessage("", "404 Not Found");
        }

        String task = parsePayload(request, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(id, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    // POST /tasks/2일때 2를 return
    private Long parseTaskId(String method) {
        String[] parts = method.split("/");

        return Long.parseLong(parts[2].trim());
    }

    private String processPostTask(Map<Long, String> tasks, String request) {
        String task = parsePayload(request, "task");

        if(task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(generateTaskId(), task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "201 Created");
    }


    // tasks Long 부분 값 증가
    private Long generateTaskId() {
        newId += 1;
        return newId;
    }


    // HTTP 요청(request) 문자열에서 특정 값(value) 가져오기
    private String parsePayload(String request, String value) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];

        try{
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(value).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    // Get
    private String processGetTask(Map<Long, String> tasks) {
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }


    // Get 응답메시지
    private String generateMessage(String body, String statusCode) {
        byte[] bytes = body.getBytes();

        return "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Host: localhost:8080\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                body;
    }

    // 메세지 보내기
    private void writeMessage(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }
}

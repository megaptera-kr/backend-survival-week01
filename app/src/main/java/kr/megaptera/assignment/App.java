package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static int index = 1;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Integer, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(8080, 0);
        System.out.println("Connecting....to port " + port);

        while (true) {
            // 2. Accept - 클라이언트 접속 대기
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request - 요청값 받기
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();
            String request = charBuffer.toString();

            // 4. Response - 응답값 보내기
            String methodType = request.substring(0, request.indexOf("HTTP"));
            System.out.println("methodType: " + methodType);
            String responseMessage = selectTodoMethod(methodType, tasks, request);

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(responseMessage);
            writer.flush();

            // 5. close - 연결 종료
            socket.close();
        }
    }

    /**
     * methodType에 따라서 처리할 작업을 선택한다.
     */
    private String selectTodoMethod(String methodType, Map<Integer, String> tasks, String request) {
        String responseMessage = "";
        if (methodType.startsWith("GET /tasks ")) {
            responseMessage = getTask(tasks);
        } else if (methodType.startsWith("POST /tasks ")) {
            responseMessage = postTask(tasks, request);
        } else if (methodType.startsWith("PATCH /tasks/")) {
            responseMessage = patchTask(tasks, request);
        } else if (methodType.startsWith("DELETE /tasks/")) {
            responseMessage = deleteTask(tasks, methodType);
        } else {
            generateMessage("", "404 Not Found");
        }
        return responseMessage;
    }

    /**
     *  TODOLIST의 전체 할 일 목록을 가져온다.
     */
    private String getTask(Map<Integer, String> tasks) {

        if(!tasks.isEmpty()) {
            return generateMessage(convertToJson(tasks), "200 OK");
        } else {
            return generateMessage("", "404 Not Found");
        }
    }

    /**
     * TODOLIST에 새로운 할 일을 추가한다.
     */
    private String postTask(Map<Integer, String> tasks, String request) {
        String task = getParsedRequest(request.toString());

        if(task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(index, task);
        index ++;
        return generateMessage(convertToJson(tasks), "201 Created");
    }

    /**
     * TODOLIST의 할 일을 수정한다.
     */
    private String patchTask(Map<Integer, String> tasks, String request) {

        String id = getParsedRequestPathId(request.toString());
        if(!tasks.containsKey(id.toString())) {
            return generateMessage("", "404 Not Found");
        }

        String task = getParsedRequest(request.toString());
        if(!task.equals("")) {
            tasks.put(Integer.parseInt(id), task);
            return generateMessage(convertToJson(tasks), "200 OK");
        } else {
            return generateMessage("", "400 Bad Request");
        }
    }

    /**
     * TODOLIST의 할 일을 삭제한다.
     */
    private String deleteTask(Map<Integer, String> tasks , String request) {
        String id = getParsedRequestPathId(request.toString());
        if(!tasks.containsKey(id.toString())) {
            return generateMessage("", "404 Not Found");
        }

        tasks.remove(id);
        return generateMessage(convertToJson(tasks), "200 OK");
    }

    /**
     * 할 일의 작업을 정규화로 파싱해서 가져온다.
     */
    private String getParsedRequest(String request) {
        Pattern pattern = Pattern.compile("\"task\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 수정하려고하는 할 일의 ID를 정규화로 파싱해서 가져온다.
     */
    private String getParsedRequestPathId(String request) {
        Pattern pattern = Pattern.compile("/tasks/(\\d+)");
        Matcher matcher = pattern.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String convertToJson(Map<Integer, String> tasks) {
        String jsonContent = new Gson().toJson(tasks);
        return jsonContent;
    }

    /**
     * 응답메시지를 생성한다.
     */
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
}
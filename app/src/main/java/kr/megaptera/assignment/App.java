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
import java.util.Set;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        Map<Long, String> tasks = new HashMap<>();
        long tasksIndex = 1;

        // TODO: 요구사항에 맞게 과제를 진행해주세요.
        while (true) {
            // 1. Listen
            ServerSocket listener = new ServerSocket(8080, 0);

            // 2. Accept; 클라이언트 요청이 오면 요청소켓을 리턴
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String s = charBuffer.toString();
            String[] split = s.split("\n");

            // 시작라인 파싱
            String[] startLine = split[0].split(" ");

            String httpMethod = startLine[0].trim().toUpperCase();
            String httpURL = startLine[1].trim();
            String httpRequestBody = "";

            // 바디 파싱
            boolean httpBodyFlag = false;
            for (String str : split) {
                if (str.trim().equals("")) {
                    httpBodyFlag = true;
                }
                if (httpBodyFlag) {
                    httpRequestBody = str;
                }
            }
            JsonElement httpRequestBodyJson = JsonParser.parseString(httpRequestBody);

            // 4. Response
            String httpVersion = "HTTP/1.1";
            String message = "";

            if (httpMethod.equals("GET")) {
                // 단순 목록 조회
                message = """
                        HTTP/1.1 200 OK

                        """ + new Gson().toJson(tasks) + "\n";
            } else if (httpMethod.equals("POST")) {
                // 바디가 비었을 경우
                if (httpRequestBodyJson.isJsonNull()) {
                    message = """
                            HTTP/1.1 400 Bad Request
                                                
                            """;
                } else {
                    // 생성 후 목록 반환
                    tasks.put(tasksIndex, httpRequestBodyJson.getAsJsonObject().get("task").getAsString());
                    tasksIndex++;

                    message = """
                            HTTP/1.1 201 Created
                                                
                            """ + new Gson().toJson(tasks) + "\n";
                }
            } else if (httpMethod.equals("PATCH")) {

                long itemId = Long.parseLong(httpURL.split("/")[2]);
                Set<Long> keySet = tasks.keySet();
                // 바디가 비었을 경우
                if (httpRequestBodyJson.isJsonNull()) {
                    message = """
                            HTTP/1.1 400 Bad Request
                                                
                            """;
                } else {
                    if (keySet.contains(itemId)) {
                        // 존재하는 아이디로 요청하면 업데이트
                        tasks.replace(itemId, httpRequestBodyJson.getAsJsonObject().get("task").getAsString());
                        tasksIndex++;

                        message = """
                                HTTP/1.1 200 OK
                                                    
                                """ + new Gson().toJson(tasks) + "\n";
                    } else {
                        // 없는 아이디로 요청하면 에러
                        message = """
                                HTTP/1.1 404 Not Found
                                                    
                                """;
                    }
                }
            } else if (httpMethod.equals("DELETE")) {
                long itemId = Long.parseLong(httpURL.split("/")[2]);
                Set<Long> keySet = tasks.keySet();

                if (keySet.contains(itemId)) {
                    // 존재하는 아이디로 요청하면 삭제
                    tasks.remove(itemId);
                    tasksIndex++;

                    message = """
                            HTTP/1.1 200 OK
                                                
                            """ + new Gson().toJson(tasks) + "\n";
                } else {
                    // 없는 아이디로 요청하면 에러
                    message = """
                            HTTP/1.1 404 Not Found
                                                
                            """;
                }

            }

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. Close
            socket.close();
            listener.close();
        }

    }
}

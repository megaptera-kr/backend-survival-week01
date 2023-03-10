package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.*;

public class App {
    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private long seq;
    App() {
        this.seq = 0;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public void updateSeq() {
        this.seq += 1;
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);
        System.out.println("listen");

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("accept");

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1500);
            reader.read(charBuffer);
            charBuffer.flip();

            String clientRequest = charBuffer.toString();
            Map parsedRequestMap;
            String body = "";

            try {
                parsedRequestMap = parsedRequest(clientRequest);
                body = parsedRequestMap.get("Body").toString();
            } catch(ArrayIndexOutOfBoundsException e) { // 잘못된 요청(파라미터 오류)
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                String responseMessage = makeResponseMessage("404", "Not Found");
                responseMessage += "Content-Length: " + 0 + "\n";
                responseMessage += "\n";

                writer.write(responseMessage);
                writer.flush();
                socket.close();
                continue;
            }

            String requestTask = "";
            Boolean isBodyEmpty = body.trim().isEmpty();
            if(!isBodyEmpty) {
                JsonElement jsonBodyElement = JsonParser.parseString(body);
                JsonObject jsonBodyObject = jsonBodyElement.getAsJsonObject();

                requestTask = jsonBodyObject.get("task").getAsString(); // string task value
            }

            // 4. Response
            String responseMessage = "";
            String method = parsedRequestMap.get("Method").toString();

            Boolean hasUpdateSeq = !parsedRequestMap.get("UpdateSeq").toString().trim().isEmpty();
            String updateSeqToString = hasUpdateSeq ? parsedRequestMap.get("UpdateSeq").toString() : "";

            if(method.equals("GET")) {
                responseMessage += makeResponseMessage("200", "OK");
            }

            if(method.equals("POST")) {
                responseMessage += makeResponseMessage("201", "Created");
                updateSeq();
                tasks.put(getSeq(), requestTask);
            }

            if(method.equals("PATCH")) {
                try {
                    responseMessage += makeResponseMessage("200", "OK");
                    Long updateSeq = Long.parseLong(updateSeqToString);
                    String task = tasks.get(updateSeq);

                    if(task == null) {
                        responseMessage = makeResponseMessage("404", "Not Found");
                    } else { // 불필요한 동작을 막는다.
                        tasks.replace(updateSeq, requestTask);
                    }
                } catch(NullPointerException e) { // updateSeq가 없을 경우
                    responseMessage = "HTTP/1.1 400 Bad Request\n";
                }
            }

            if(method.equals("DELETE")) {
                try {
                    responseMessage += makeResponseMessage("200", "OK");
                    Long updateSeq = Long.parseLong(updateSeqToString);

                    String task = tasks.get(updateSeq);
                    if(task == null) { // task 가 없을 경우는 잘못 요청한 경우이고 컴파일 오류가 나므로 처리해준다.
                        responseMessage = makeResponseMessage("404", "Not Found");
                    } else {
                        tasks.remove(updateSeq);
                    }
                } catch(NullPointerException e) { // updateSeq가 없을 경우
                    responseMessage = makeResponseMessage("400", "Bad Request");
                }
            }

            if(responseMessage.isEmpty()) {
                // 잘못된 method 요청
            }

            if((method.equals("POST") || method.equals("PATCH")) && isBodyEmpty) {
                responseMessage = makeResponseMessage("400", "Bad Request");
            }

            String sendJsonTask = new Gson().toJson(tasks);
            Boolean isBadRequest = responseMessage.contains("400") || responseMessage.contains("404");
            int contentByteSize = isBadRequest ? 0 : sendJsonTask.getBytes().length;

            responseMessage += "Content-Length: " + contentByteSize + "\n";
            responseMessage += "Content-Type: application/json; charset=UTF-8\n";
            responseMessage += "Host: localhost:8080\n";
            responseMessage += "\n";

            if(!isBadRequest) {
                responseMessage += sendJsonTask; // body
                responseMessage += "\n";
            }

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(responseMessage);
            writer.flush();

            // 5. close
            socket.close();
        }
    }
    public Map parsedRequest(String clientRequest) {
        String[] decomposedRequests = clientRequest.split("\n");
        Map<String, String> requestMap = new HashMap<>();

        Boolean hasBody = false;

        for(String request : decomposedRequests) {
            try {
                String[] decomposedRequest = {};
                String updateSeq = "";
                Boolean hasSeparator = request.contains(":");

                if(request.length() == 1) { // 이후 Body
                    hasBody = true;
                }

                if(hasBody) {
                    requestMap.put("Body", request);
                } else if(hasSeparator) {
                    decomposedRequest = request.split(":");
                    requestMap.put(decomposedRequest[0], decomposedRequest[1]);
                } else { // 규칙성이 없으므로 첫 라인으로 취급
                    decomposedRequest = request.split(" ");
                    String method = decomposedRequest[0];
                    String url = decomposedRequest[1];

                    String[] decomposedUrl = url.split("/");

                    if(decomposedUrl.length >= 3) {
                        updateSeq = decomposedUrl[2];
                    }

                    requestMap.put("Method", method);
                    requestMap.put("Url", url);
                    requestMap.put("UpdateSeq", updateSeq);
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                System.out.println("잘못된 데이터 요청");
                throw e;
            }
        }
        return requestMap;
    }

    public String makeResponseMessage(String code, String message) {
        return "HTTP/1.1" + " " + code + " " +message + "\n";
    }
}

package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        while (true) {

            // 1. Listen
            try (ServerSocket listener = new ServerSocket(port)) {

                // 2. Accept

                Socket socket = listener.accept();

                // 3. Request

                Reader reader = new InputStreamReader(socket.getInputStream());

                CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
                reader.read(charBuffer);

                charBuffer.flip();

//                System.out.println("@@@@@@new request@@@@@@@ \n");
//                System.out.println(charBuffer);

                RequestContent requestContent = requestParser(charBuffer.toString());

                ResponseContent responseContent;

                if (requestContent.method.equals("GET")) {
                    responseContent = getRouter(requestContent, tasks);
                } else if (requestContent.method.equals("POST")) {
                    responseContent = postRouter(requestContent, tasks);
                } else if (requestContent.method.equals("PATCH")) {
                    responseContent = patchRouter(requestContent, tasks);
                } else if (requestContent.method.equals("DELETE")) {
                    responseContent = deleteRouter(requestContent, tasks);
                } else {
                    responseContent = throwNotFound();
                }


                // 4. Response

                String message = makeMessage(responseContent);

//                System.out.println("@@@@@@@@@@@@@start");
//                System.out.println(message);
//                System.out.println("@@@@@@@@@@@@@end");
//                System.out.println("now tasks " + new Gson().toJson(tasks) + "\n");

                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(message);
                writer.flush();
            }

        }

    }

    private class RequestContent {
        private String method;

        private String path;

        private String body;

        public RequestContent(String method, String path, String body) {
            this.method = method;
            this.path = path;
            this.body = body;
        }
    }

    private class ResponseContent {

        private String statusCode;

        private int contentLength;

        private String body;

        public ResponseContent(String statusCode, int contentLength, String body) {
            this.statusCode = statusCode;
            this.contentLength = contentLength;
            this.body = body;
        }
    }

    private class BodyContent {
        private Long key;

        private String value;

        public BodyContent(Long key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private RequestContent requestParser(String request) {
        String[] split = request.split("\n");

        String method = null;
        String path = null;

        boolean isBody = false;

        StringBuilder bodyBuilder = new StringBuilder();

        for (int i=0; i<split.length; i++) {
            if (i==0) {
                String[] first = split[i].split(" ");
                method = first[0];
                path = first[1];
            }
            if(isBody) {
                bodyBuilder.append(split[i]).append("\n");
            }
            if (split[i].isBlank()) {
                isBody = true;
            }
        }

        return new RequestContent(method, path, bodyBuilder.toString());
    }

    private ResponseContent getRouter(RequestContent requestContent, Map<Long, String> tasks) {
        if (requestContent.path.equals("/tasks")) {
            return getTasksService(tasks);
        }

        return throwNotFound();
    }

    private ResponseContent getTasksService(Map<Long, String> tasks) {

        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        int contentLength = bytes.length;

        return new ResponseContent("200 OK", contentLength, body);
    }

    private ResponseContent postRouter(RequestContent requestContent, Map<Long, String> tasks) {

        if (requestContent.path.equals("/tasks")) {
            return postTasksService(requestContent, tasks);
        }

        return throwNotFound();
    }

    private ResponseContent postTasksService(RequestContent requestContent, Map<Long, String> tasks) {

        if (requestContent.body.isBlank()) {
            return throwBadRequest();
        }
        BodyContent content = bodyParser(requestContent.body, tasks.size());
        tasks.put(content.key, content.value);

        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        int contentLength = bytes.length;

        return new ResponseContent("201 Created", contentLength, body);
    }

    private BodyContent bodyParser(String body, int length) {
        JsonElement jsonElement = JsonParser.parseString(body);
        JsonObject object = jsonElement.getAsJsonObject();
        Object task = object.get("task");
        String taskString = task.toString().replace("\"", "");

        return new BodyContent((long) (length), taskString);
    }

    private ResponseContent patchRouter(RequestContent requestContent, Map<Long, String> tasks) {
        String patchTasks = "/tasks/\\d+";
        if (Pattern.matches(patchTasks, requestContent.path)) {
            return patchTasksService(requestContent, tasks);
        }
        return throwNotFound();
    }

    private ResponseContent patchTasksService(RequestContent requestContent, Map<Long, String> tasks) {

        if (requestContent.body.isBlank()) {
            return throwBadRequest();
        }

        String[] split = requestContent.path.split("/");
        String key = split[2];
        Long keyLong = Long.parseLong(key);
        if (!tasks.containsKey(keyLong)) {
            return throwNotFound();
        }

        BodyContent content = bodyParser(requestContent.body, tasks.size());

        tasks.put(keyLong, content.value);

        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        int contentLength = bytes.length;

        return new ResponseContent("200 OK", contentLength, body);
    }

    private ResponseContent deleteRouter(RequestContent requestContent, Map<Long, String> tasks) {
        String patchTasks = "/tasks/\\d+";
        if (Pattern.matches(patchTasks, requestContent.path)) {
            return deleteTasksService(requestContent, tasks);
        }
        return throwNotFound();
    }

    private ResponseContent deleteTasksService(RequestContent requestContent, Map<Long, String> tasks) {

        String[] split = requestContent.path.split("/");
        String key = split[2];
        Long keyLong = Long.parseLong(key);
        if (!tasks.containsKey(keyLong)) {
            return throwNotFound();
        }

        tasks.remove(keyLong);

        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        int contentLength = bytes.length;

        return new ResponseContent("200 OK", contentLength, body);
    }

    private ResponseContent throwBadRequest() {
        return new ResponseContent("400 Bad Request", 0, "");
    }

    private ResponseContent throwNotFound() {
        return new ResponseContent("404 Not Found", 0, "");

    }


    private String makeMessage(ResponseContent responseContent) {
        return """
                HTTP/1.1 %s
                Content-Type: application/json; charset=UTF-8
                Content-Length: %s
                Host: localhost:8080
                
                %s
                """.formatted(responseContent.statusCode,
                    responseContent.contentLength,
                    responseContent.body);
    }
}

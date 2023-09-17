package kr.megaptera.assignment;

import java.util.HashMap;
import java.util.Map;

public class App {
    private static int id = 1;
    private static final Gson gson = new Gson();
    private static Map<Integer, String> todos = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException{
        App app = new App();
        todos.put(id++, new String("study"));
        todos.put(id++, new String("play"));
        app.run();
    }
    private void run() throws IOException {
        //1. Listen
        ServerSocket listener = new ServerSocket(8080, 0);
        System.out.println("Listen!");
        //backlog 0을 넣어 여러개의 요청 자동처리
        //I/O에서 기달리는걸 BLCOKING.. 네틍워크,ACCEPT없으면 영원히 기다림 -> 멀티스레드,비동기,이벤트 기반 처리 필요
        //2. Aceept
        while(true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");
            //3. Request -> 처리 -> Response
            Reader reader = new InputStreamReader(socket.getInputStream(),"UTF-8");
            CharBuffer charbuffer = CharBuffer.allocate(1_000_0000);
            reader.read(charbuffer);
            charbuffer.flip();
            String request = charbuffer.toString();
            //Parsing Request Header
            String[] requestLines = request.split("\n");
            for (String line : requestLines) {
                System.out.println(line);
            }
            String[] firstHeader = requestLines[0].split(" ");
            String httpMethod = firstHeader[0];
            String path = firstHeader[1];

            // Extract request body
            String[] parts = request.split("\r\n\r\n");
            String requestBody = parts.length > 1 ? parts[1] : "";

            System.out.println(requestBody);

            //4. Response
            String body = "Hello, world!";
            String status = "200 OK";
            String response = "";

            switch (httpMethod) {
                case "GET":
                    if (path.equals("/tasks")) {
                        response = handleGetTasks();
                    }
                    break;
                case "POST":
                    if (path.equals("/tasks")) {
                        status = handlePostTasks(requestBody);
                        if(status.contains("201") || status.contains("200")) {
                            response = gson.toJson(todos);;
                        }
                    }
                    break;
                case "PATCH":
                    if (path.startsWith("/tasks/")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        status = handlePatchTasks(id, requestBody);
                        response = gson.toJson(todos);;
                    }
                    break;
                case "DELETE":
                    if (path.startsWith("/tasks/")) {
                        int id = Integer.parseInt(path.split("/")[2]);
                        status = handleDeleteTasks(id);
                        response = gson.toJson(todos);;
                    }
                    break;
            }

            System.out.println(status.substring(0,3));
            if(status.substring(0, 1).equals("4") || status.substring(0, 1).equals("5")) {
                response = "";
            }

            byte[] bytes = response.getBytes();
            String message = "" +
                    "HTTP/1.1 " + status + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "\n" +
                    response;

            Writer writer = new OutputStreamWriter(socket.getOutputStream(),"UTF-8");
            writer.write(message);
            writer.flush();
        }
    }
    private String handleGetTasks() {
        return gson.toJson(todos);
    }
    private String handlePostTasks(String body) {
        if(body == null || body.isEmpty()) {
            return "400 Bad Request"; // 'task' 키가 없거나 값이 기본 타입이 아닌 경우
        }
        // JSON 문자열을 Java 객체로 변환
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        // 'task' 키의 값을 얻습니다.
        JsonElement task = jsonObject.get("task");
        if (task == null || !task.isJsonPrimitive()) {
            return "400 Bad Request"; // 'task' 키가 없거나 값이 기본 타입이 아닌 경우
        }
        String taskValue = task.getAsString();
        // 얻어진 값을 todos Map에 저장
        todos.put(id, taskValue);
        return "201 Created";
    }

    private String handlePatchTasks(int id , String body) {
        if (!todos.containsKey(id)) {
            return "404 Not Found";
        }
        if(body == null || body.isEmpty()) {
            return "400 Bad Request"; // 'task' 키가 없거나 값이 기본 타입이 아닌 경우
        }
        // JSON 문자열을 Java 객체로 변환
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        // 'task' 키의 값을 얻습니다.
        JsonElement task = jsonObject.get("task");
        if (task == null || !task.isJsonPrimitive()) {
            return "400 Bad Request"; // 'task' 키가 없거나 값이 기본 타입이 아닌 경우
        }
        String taskValue = task.getAsString();
        // 얻어진 값을 todos Map에 저장
        todos.put(id, taskValue);
        return "200 OK";
    }

    private String handleDeleteTasks(int id) {
        if (!todos.containsKey(id)) {
            return "404 Not Found";
        }
        todos.remove(id);
        return "200 OK";
    }
}


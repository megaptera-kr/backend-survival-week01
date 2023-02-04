package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class App {
    long count = 0;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        HttpServer httpServer = getHttpServer(port);

        // 2. Accept
        //GET
        httpServer.createContext("/tasks", (exchange) -> {
            // 3. request
            String requestMethod = exchange.getRequestMethod();

            if (requestMethod.equals("GET")) {
                // 4. response
                String tasksToJson = getTasksToJson(tasks);
                sendMessgae(exchange, tasksToJson, 200);

            } else if (requestMethod.equals("POST")) {
                InputStream requestBody = exchange.getRequestBody();
                Reader reader = new InputStreamReader(requestBody);

                CharBuffer buffer = CharBuffer.allocate(1_000_000);

                reader.read(buffer);
                buffer.flip();

                // Body Data 가 없을 경우 예외처리
                if(buffer.toString().equals("")){
                    // 4. response
                    exchange.sendResponseHeaders(400, 0L);

                } else if(!buffer.toString().equals("")){
                    Map bodyMap = gson.fromJson(buffer.toString(), tasks.getClass());
                    tasks.put(++count, (String)bodyMap.get("task"));

                    // 4. response
                    String tasksToJson = getTasksToJson(tasks);
                    sendMessgae(exchange, tasksToJson, 200);
                }

            } else if (requestMethod.equals("PATCH")) {
                // 3-1.

                // 4. response
                String tasksToJson = getTasksToJson(tasks);
                sendMessgae(exchange, tasksToJson, 200);

            } else if (requestMethod.equals("DELETE")) {
                // 3-1.

                // 4. response
                String tasksToJson = getTasksToJson(tasks);
                sendMessgae(exchange, tasksToJson, 200);

            }

        });

        httpServer.start();
    }

    private void sendMessgae(HttpExchange exchange, String tasksToJson, int httpStatusCode) throws IOException {
        byte[] bytes = tasksToJson.getBytes();
        exchange.sendResponseHeaders(httpStatusCode, bytes.length);

        OutputStream outputStream = exchange.getResponseBody();
        Writer writer = new OutputStreamWriter(outputStream);

        writer.write(tasksToJson);
        writer.flush();
    }

    private String getTasksToJson(Map<Long, String> tasks) {

        String tasksToJson = gson.toJson(tasks);
        System.out.println("tasksToJson = " + tasksToJson);
        return tasksToJson;
    }

    private HttpServer getHttpServer(int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(port);
        HttpServer httpServer = HttpServer.create(address, 0);
        return httpServer;
    }

}

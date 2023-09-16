package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    private Map<Integer, String> tasks = new HashMap<>();
    private Integer lastTaskIndex = 1;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private String[] getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);

        reader.read(charBuffer);

        charBuffer.flip();

        String[] requestLines = charBuffer.toString().split("\n");

        return requestLines;
    }

    private void response(Socket socket, String host, String httpVersion, int statusCode, String statusMessage, String body) throws IOException {
        byte[] bytes = body.getBytes();

        String message = httpVersion + " " + statusCode + " " + statusMessage + "\n" +
                         "Content-Type: application/json; charset=UTF-8\n" +
                         "Content-Length: " + bytes.length + "\n" +
                         "Host: " + host + "\n" +
                         "\n" +
                         body;

        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

        writer.write(message);
        writer.flush();
    }

    private String createTasks(String task) {
        this.tasks.put(this.lastTaskIndex, task);

        this.lastTaskIndex += 1;

        return this.getTasks();
    }

    // GET /tasks
    private String getTasks() {
        return new Gson().toJson(this.tasks);
    }

    private String getTask(int taskId) {
        return this.tasks.get(taskId);
    }

    private String patchTask(int taskId, String task) {
        this.tasks.put(taskId, task);
        return new Gson().toJson(this.tasks);
    }

    private String deleteTask(int taskId) {
        this.tasks.remove(taskId);
        return new Gson().toJson(this.tasks);
    }

    private void run() throws IOException {
        int port = 8080;

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);
        System.out.println(String.format("Listen: %d", port));

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            System.out.println("Accept!");

            // 3. Request
            String[] requestLines = this.getRequest(socket);

            // StartLine
            String startLine = requestLines[0].trim();
            String requestMethod = startLine.split(" ")[0];
            String requestFullPath = startLine.split(" ")[1];
            String requestHttpVersion = startLine.split(" ")[2];

            // Header
            Map<String, String> headers = new HashMap<>();
            String host;
            String contentLength;

            // Body
            String bodyLine;

            int bodyLineIndex = 1;

            while (!requestLines[bodyLineIndex].equals("\r")) {
                String headerLine = requestLines[bodyLineIndex];
                headers.put(headerLine.split(":")[0].toLowerCase(), headerLine.split(":")[1].trim());
                bodyLineIndex += 1;
            }

            host = headers.get("host");
            contentLength = headers.get("content-length");

            int statusCode;
            String statusMessage;
            String responseBody;
            try {
                int taskId;
                String task;

                String requestTask;

                switch (requestMethod) {
                    case "POST":
                        if (contentLength.equals("0")) {
                            statusCode = HttpsURLConnection.HTTP_BAD_REQUEST;
                            statusMessage = "BAD REQUEST";
                            responseBody = "";
                        } else {
                            requestTask = JsonParser.parseString(requestLines[bodyLineIndex + 1]).getAsJsonObject().get("task").getAsString();

                            statusCode = HttpsURLConnection.HTTP_CREATED;
                            statusMessage = "CREATED";
                            responseBody = this.createTasks(requestTask);
                        }
                        break;
                    case "GET":
                        statusCode = HttpsURLConnection.HTTP_OK;
                        statusMessage = "OK";
                        responseBody = this.getTasks();
                        break;
                    case "PATCH":
                        taskId = Integer.parseInt(requestFullPath.split("/")[2]);
                        task = this.getTask(taskId);
                        if (task == null) {
                            statusCode = HttpsURLConnection.HTTP_NOT_FOUND;
                            statusMessage = "NOT FOUND";
                            responseBody = "";
                        } else if (contentLength.equals("0")) {
                            statusCode = HttpsURLConnection.HTTP_BAD_REQUEST;
                            statusMessage = "BAD REQUEST";
                            responseBody = "";
                        } else {
                            requestTask = JsonParser.parseString(requestLines[bodyLineIndex + 1]).getAsJsonObject().get("task").getAsString();

                            statusCode = HttpsURLConnection.HTTP_OK;
                            statusMessage = "OK";
                            responseBody = this.patchTask(taskId, requestTask);
                        }
                        break;
                    case "DELETE":
                        taskId = Integer.parseInt(requestFullPath.split("/")[2]);
                        task = this.getTask(taskId);
                        if (task == null) {
                            statusCode = HttpsURLConnection.HTTP_NOT_FOUND;
                            statusMessage = "NOT FOUND";
                            responseBody = "";
                        } else {
                            statusCode = HttpsURLConnection.HTTP_OK;
                            statusMessage = "OK";
                            responseBody = this.deleteTask(taskId);
                        }
                        break;
                    default:
                        statusCode = HttpsURLConnection.HTTP_NOT_FOUND;
                        statusMessage = "NOT FOUND";
                        responseBody = "NOT FOUND";
                }

                // 4. Response
                this.response(socket, host, requestHttpVersion, statusCode, statusMessage, responseBody);// 4. Response
            } catch (Exception e) {
                statusCode = HttpsURLConnection.HTTP_INTERNAL_ERROR;
                statusMessage = "INTERNAL_ERROR";
                responseBody = "";
                System.out.println(e.getMessage());
                this.response(socket, host, requestHttpVersion, statusCode, statusMessage, responseBody);
            }

            socket.close();
        }
    }

}

package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerError;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    // Only in memory
    private final static String host = "localhost";
    private static int port;
    private static long count = 1;

    void run() throws IOException {
        port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen

        // Make a server
        ServerSocket listener = new ServerSocket(port, 0);
        try {
            while (count < 1_000_000) {
                // 2. Accept
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
                bw.write("Socket created\n\n");
                Socket socket = listener.accept();

                // 3. Request

                InputStream inputStream = socket.getInputStream();
                byte[] bytes = new byte[1_000_000];
                int size = inputStream.read(bytes);
                String message = new String(bytes, 0, size);
                bw.write(message + "\n------ REQUEST ------\n\n");
                bw.flush();

                StringTokenizer st = new StringTokenizer(message, "\n");
                String[] firstLine = st.nextToken().split(" ");
                String method = firstLine[0];
                String[] path = firstLine[1].split("/");

                boolean hasBody = false;

                while (st.hasMoreElements()) {
                    if (st.nextToken().trim().length() == 0) {
                        hasBody = true;
                        break;
                    }
                }

                StringBuilder sb = new StringBuilder();

                while (hasBody && st.hasMoreElements()) {
                    sb.append(st.nextToken()).append("\n");
                }

                String requestBody = sb.toString();

                // 4. Response
                String responseAll;

                if (path.length > 1 && !path[1].equals("tasks")) {
                    responseAll = makeHeader(404, "Not Found", "") + "\n";

                } else if (path.length == 3 && method.charAt(0) == 'P' && method.charAt(1) == 'A') {
                    try {
                        long id = Long.parseLong(path[2]);
                        responseAll = patchTask(id, requestBody, tasks);
                        bw.write("Patch " + id + "\n");
                    } catch (NumberFormatException ne) {
                        responseAll = makeHeader(404, "Not Found", "") + "\n";
                    }

                } else if (path.length == 3 && method.charAt(0) == 'D') {
                    try {
                        long id = Long.parseLong(path[2]);
                        responseAll = deleteTask(id, tasks);
                        bw.write("Delete " + id + "\n");
                    } catch (NumberFormatException ne) {
                        responseAll = makeHeader(404, "Not Found", "") + "\n";
                    }

                } else if (path.length == 2 && method.charAt(0) == 'G') {
                    responseAll = getTasks(tasks);
                    bw.write("Get\n");

                } else if (path.length == 2 && method.charAt(0) == 'P' && method.charAt(1) == 'O') {
                    responseAll = postTask(requestBody, tasks);
                    bw.write("Post\n");

                } else {
                    responseAll = makeHeader(404, "Not Found", "") + "\n";
                }

                bw.write(message + "\n------ RESPONSE ------\n\n");

                // write response
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(responseAll);
                writer.flush();

                // Close socket

                socket.close();

                bw.write("\nSocket closed...\n==================\n\n");
                bw.flush();
            }
        } catch (ServerError e) {
            listener.close();
        }
    }

    // Making Header String
    private String makeHeader(int statusCode, String statusMessage, String body) {
        String header = "HTTP/1.1 " + statusCode + " " + statusMessage + "\n";
        header = header + "Content-Length: " + body.getBytes().length + "\n";
        header = header + "Content-Type: application/json; charset=UTF-8\n";
        header = header + "Host: " + host + ":" + port + "\n";
        return header;
    }

    // GET Response
    private String getTasks(Map<Long, String> tasks) {
        String body = new Gson().toJson(tasks);
        String response = makeHeader(200, "OK", body);
        response = response + "\n" + body + "\n"; // with a blank line
        return response;
    }

    // POST Response
    private String postTask(String requestBody, Map<Long, String> tasks) {
        if (requestBody.trim().length() == 0) {
            return makeHeader(400, "Bad Request", "") + "\n";
        }

        JsonObject taskJson = JsonParser.parseString(requestBody).getAsJsonObject();
        String newTask = taskJson.get("task").getAsString();
        if (newTask.trim().length() == 0) { // 제목을 안적음
            return makeHeader(400, "Bad Request", "") + "\n";
        }

        tasks.put(count, newTask);

        count++;

        String body = new Gson().toJson(tasks);
        String response = makeHeader(201, "Created", body);
        response = response + "\n" + body + "\n"; // with a blank line
        return response;
    }

    // PATCH Response
    private String patchTask(long id, String requestBody, Map<Long, String> tasks) {
        if (!tasks.containsKey(id)) {
            return makeHeader(404, "Not Found", "") + "\n";
        } else if (requestBody.trim().length() == 0) { // 아예 바디 내용 없음
            return makeHeader(400, "Bad Request", "") + "\n";
        }

        JsonObject taskJson = JsonParser.parseString(requestBody).getAsJsonObject();
        String newTask = taskJson.get("task").getAsString();
        if (newTask.trim().length() == 0) { // 제목을 제대로 안적음
            return makeHeader(400, "Bad Request", "") + "\n";
        }

        tasks.replace(id, newTask);

        String body = new Gson().toJson(tasks);
        String response = makeHeader(200, "OK", body);
        response = response + "\n" + body + "\n"; // with a blank line
        return response;
    }

    // DELETE Reponse
    private String deleteTask(long id, Map<Long, String> tasks) {
        if (!tasks.containsKey(id)) {
            return makeHeader(404, "Not Found", "") + "\n";
        }

        tasks.remove(id);

        String body = new Gson().toJson(tasks);
        String response = makeHeader(200, "OK", body);
        response = response + "\n" + body + "\n"; // with a blank line
        return response;
    }

}
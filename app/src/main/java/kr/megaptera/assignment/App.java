package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) throws IOException {
        new App().run();
    }

    private void run() throws IOException {
        int port = 8080;
        Gson GSON = new Gson();
        Map<Long, String> tasks = new HashMap<>();
        long taskIndex = 1;
        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();
            HttpRequest httpRequest = new HttpRequest(socket);

            if (!httpRequest.isMethodAllowed()) {
                writeResponse(socket, HttpResponse.METHOD_NOT_ALLOWED.createResponse(null));
            }

            // 3. Request
            if (httpRequest.isPostMethod() && httpRequest.isPathEquals("/tasks")) {
                taskIndex = postProcess(GSON, tasks, taskIndex, socket, httpRequest);
            }

            if (httpRequest.isGetMethod()) {
                getProcess(GSON, tasks, socket, httpRequest);
            }

            if (httpRequest.isPatchMethod() && httpRequest.isPathStartsWith("/tasks/")) {
                patchProcess(GSON, tasks, socket, httpRequest);
            }

            if (httpRequest.isDeleteMethod() && httpRequest.isPathStartsWith("/tasks/")) {
                deleteProcess(GSON, tasks, socket, httpRequest);
            }

            writeResponse(socket, HttpResponse.NOT_FOUND.createResponse(null));
        }
    }

    // 4. Response
    private void deleteProcess(Gson GSON, Map<Long, String> tasks, Socket socket, HttpRequest httpRequest) throws IOException {
        long taskId = httpRequest.getTaskIdFromPath();
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            writeResponse(socket, HttpResponse.OK.createResponse(GSON.toJson(tasks)));
        }
    }

    private void patchProcess(Gson GSON, Map<Long, String> tasks, Socket socket, HttpRequest httpRequest) throws IOException {
        if (httpRequest.isBodyNotEmpty()) {
            long taskId = httpRequest.getTaskIdFromPath();
            if (tasks.containsKey(taskId)) {
                String task = httpRequest.getTaskFromBody();
                tasks.put(taskId, task);
                writeResponse(socket, HttpResponse.OK.createResponse(GSON.toJson(tasks)));
            } else {
                writeResponse(socket, HttpResponse.NOT_FOUND.createResponse(null));
            }
        } else {
            writeResponse(socket, HttpResponse.BAD_REQUEST.createResponse(null));
        }
    }

    private void getProcess(Gson GSON, Map<Long, String> tasks, Socket socket, HttpRequest httpRequest) throws IOException {
        if (httpRequest.isPathEquals("/tasks")) {
            writeResponse(socket, HttpResponse.OK.createResponse(GSON.toJson(tasks)));
        } else if (httpRequest.isPathStartsWith("/tasks/")) {
            long taskId = httpRequest.getTaskIdFromPath();
            if (tasks.containsKey(taskId)) {
                writeResponse(socket, HttpResponse.OK.createResponse(GSON.toJson(tasks.get(taskId))));
            }
        }
    }

    private long postProcess(Gson GSON, Map<Long, String> tasks, long taskIndex, Socket socket, HttpRequest httpRequest) throws IOException {
        if (httpRequest.isBodyNotEmpty()) {
            String task = httpRequest.getTaskFromBody();
            tasks.put(taskIndex, task);
            writeResponse(socket, HttpResponse.CREATED.createResponse(GSON.toJson(tasks)));
            taskIndex++;
        } else {
            writeResponse(socket, HttpResponse.BAD_REQUEST.createResponse(null));
        }
        return taskIndex;
    }

    private void writeResponse(Socket socket, String response) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(response);
        writer.flush();
    }
}

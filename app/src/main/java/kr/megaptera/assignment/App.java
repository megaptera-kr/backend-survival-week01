package kr.megaptera.assignment;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class App {

    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public static final String LOCALHOST = "localhost:8080";
    public static final String BLANK_LINE = "";

    public static final String HTTP_1_1_201_CREATED = "HTTP/1.1 201 Created";
    public static final String HTTP_1_1_200_OK = "HTTP/1.1 200 Ok";
    public static final String HTTP_1_1_400_BAD_REQUEST = "HTTP/1.1 400 Bad Request";
    public static final String HTTP_1_1_404_NOT_FOUND = "HTTP/1.1 404 Not Found";

    public static final Gson gson = new Gson();
    public static final Logger LOGGER = LoggerFactory.getLogger("kr.megaptera.assignment.App");
    public static final AtomicLong taskId = new AtomicLong(0L);
    public static final Map<Long, String> tasks = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        // 1. Listen
        ServerSocket serverSocket = new ServerSocket(port);

        // 2. Accept
        while (true) {
            Socket socket = serverSocket.accept();
            Thread task = new SocketThread(socket);
            task.start();

        }
    }

    class Task {
        private String task;

        public String getTask() {
            return task;
        }

        @Override
        public String toString() {
            return this.task;
        }
    }

    class SocketThread extends Thread {
        private Socket socket;
        private Map<String, String> reqHeaderMap = new HashMap<>();
        private Map<String, String> respHeaderMap = new HashMap<>();
        private Map<ArrayList<String>, Consumer<String>> context = new HashMap<>();

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Reader reader = null;
            Writer writer = null;

            try {
                reader = new InputStreamReader(socket.getInputStream());
                writer = new OutputStreamWriter(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            while (!socket.isClosed()) {
                CharBuffer charBuffer = CharBuffer.allocate(1_000_000);

                try {
                    reader.read(charBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                charBuffer.flip();

                String request = charBuffer.toString();

                // 텅빈 입력 skip
                if (request.isEmpty() || request.isBlank()) {
                    continue;
                }

                // request startline parsing
                String[] reqSplitByNewline = request.split("\n");
                String startLine = "";
                String[] startLineSplitByWhitespace = {};
                String method = "";
                String path = "";
                String reqBody = null;

                for (int i = 0; i < reqSplitByNewline.length; i++) {
                    if (i == 0) {
                        try {
                            startLine = reqSplitByNewline[i];
                            startLineSplitByWhitespace = startLine.split(" ");
                            method = startLineSplitByWhitespace[0];
                            path = startLineSplitByWhitespace[1];
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // request header parsing
                        String header = reqSplitByNewline[i];

                        if (header.equals("\r") || header.equals("\n")) {
                            if (reqHeaderMap.containsKey("content-length")) {
                                String[] reqBodyArr = Arrays.copyOfRange(reqSplitByNewline, i, reqSplitByNewline.length);
                                reqBody = String.join("\n", reqBodyArr).trim();
                                //                            System.out.println("reqBody = " + reqBody);
                            }
                            break;
                        }

                        String[] strSplitByColon = header.split(":");
                        reqHeaderMap.put(strSplitByColon[0].trim(), strSplitByColon[1].trim());
                    }
                }


                // handler path mapping 셋팅
                setContexts(respHeaderMap, reqBody, tasks, context, writer);

                for (Map.Entry<ArrayList<String>, Consumer<String>> entry : context.entrySet()) {
                    ArrayList<String> methodAndPath = entry.getKey();

                    // 4. Response
                    if (method.equals(methodAndPath.get(0)) && path.startsWith(methodAndPath.get(1))) {
                        context.get(methodAndPath).accept(path);
                        break;
                    }
                }
            }

            // 리소스 정리
            try {
                cleanUp(socket, reader, writer);
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void cleanUp(Socket socket, Reader reader, Writer writer) throws IOException {
        writer.close();
        reader.close();
        socket.close();
    }

    /**
     * 각각의 response header join하기
     */
    private String getHeadersAndFlush(Map<String, String> respHeaderMap) {
        StringJoiner sj = new StringJoiner("\n");
        for (Map.Entry<String, String> entry : respHeaderMap.entrySet()) {
            sj.add(entry.getKey() + ": " + entry.getValue());
        }
        respHeaderMap.clear();

        return sj.toString();
    }

    private void setContexts(Map<String, String> respHeaderMap, String reqBody, Map<Long, String> tasks,
                             Map<ArrayList<String>, Consumer<String>> context, Writer writer) {
        // method:GET path:/tasks
        getTasks(respHeaderMap, tasks, context, writer);

        // method:POST path:/tasks
        postTasks(respHeaderMap, reqBody, tasks, context, writer);

        // method:PATCH path:/tasks/{id}
        patchTasks(respHeaderMap, reqBody, tasks, context, writer);

        // method:DELETE path:/tasks/{id}
        deleteTasks(respHeaderMap, reqBody, tasks, context, writer);
    }

    /**
     * handler
     * method: GET
     * path: /tasks
     */
    private void getTasks(Map<String, String> respHeaderMap, Map<Long, String> tasks,
                          Map<ArrayList<String>, Consumer<String>> context, Writer writer) {
        context.put(new ArrayList<>(Arrays.asList("GET", "/tasks")), (pathVariable) -> {
            String respBody = new Gson().toJson(tasks);
            String contentLength = String.valueOf(respBody.getBytes().length);

            respHeaderMap.put("Content-Length", contentLength);
            respHeaderMap.put("Content-Type", CONTENT_TYPE_JSON);
            respHeaderMap.put("Host", LOCALHOST);
            String responseHeaders = getHeadersAndFlush(respHeaderMap);

            String totalResponse = getTotalResponse(HTTP_1_1_200_OK, responseHeaders, respBody);

            try {
                writer.write(totalResponse);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("writer exception", e);
            }
        });
    }

    /**
     * handler
     * method: POST
     * path: /tasks
     */
    private void postTasks(Map<String, String> respHeaderMap, String reqBody,
                           Map<Long, String> tasks, Map<ArrayList<String>, Consumer<String>> context, Writer writer) {
        context.put(new ArrayList<>(Arrays.asList("POST", "/tasks")), (path) -> {
            String statusLine;
            String respBody;
            String contentLength;

            if (Objects.isNull(reqBody) || reqBody.length() == 0) {
                statusLine = HTTP_1_1_400_BAD_REQUEST;
                contentLength = "0";
                respBody = "";

            } else {
                Task task = gson.fromJson(reqBody, Task.class);

                statusLine = HTTP_1_1_201_CREATED;
                long tasksId = taskId.incrementAndGet();
                tasks.put(tasksId, task.toString());

                respBody = new Gson().toJson(tasks);
                contentLength = String.valueOf(respBody.getBytes().length);
            }

            respHeaderMap.put("Content-Length", contentLength);
            respHeaderMap.put("Content-Type", CONTENT_TYPE_JSON);
            respHeaderMap.put("Host", LOCALHOST);
            String responseHeaders = getHeadersAndFlush(respHeaderMap);

            String response = getTotalResponse(statusLine, responseHeaders, respBody);

            try {
                writer.write(response);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("writer exception", e);
            }
        });
    }

    /**
     * handler
     * method: PATCH
     * path: /tasks/{id}
     */
    private void patchTasks(Map<String, String> respHeaderMap, String reqBody,
                            Map<Long, String> tasks, Map<ArrayList<String>, Consumer<String>> context, Writer writer) {
        context.put(new ArrayList<>(Arrays.asList("PATCH", "/tasks/")), (path) -> {
            String pathVariable = path.substring("/tasks/".length());

            String statusLine = "";
            String respBody = "";
            String contentLength = "";


            if (Objects.isNull(reqBody) || reqBody.length() == 0) {
                statusLine = HTTP_1_1_400_BAD_REQUEST;
                contentLength = "0";
                respBody = "";

            } else if (!tasks.containsKey(Long.parseLong(pathVariable))) {
                statusLine = HTTP_1_1_404_NOT_FOUND;
                contentLength = "0";
                respBody = "";
            } else {
                statusLine = HTTP_1_1_200_OK;
                String inputTask = gson.fromJson(reqBody, Task.class).getTask();
                tasks.put(Long.valueOf(pathVariable), inputTask);

                respBody = new Gson().toJson(tasks);
                contentLength = String.valueOf(respBody.getBytes().length);
            }

            respHeaderMap.put("Content-Length", contentLength);
            respHeaderMap.put("Content-Type", CONTENT_TYPE_JSON);
            respHeaderMap.put("Host", LOCALHOST);
            String responseHeaders = getHeadersAndFlush(respHeaderMap);

            String response = getTotalResponse(statusLine, responseHeaders, respBody);

            try {
                writer.write(response);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("writer exception", e);
            }
        });
    }

    /**
     * handler
     * method: DELETE
     * path: /tasks/{id}
     */
    private void deleteTasks(Map<String, String> respHeaderMap, String reqBody,
                             Map<Long, String> tasks, Map<ArrayList<String>, Consumer<String>> context, Writer writer) {
        context.put(new ArrayList<>(Arrays.asList("DELETE", "/tasks/")), (path) -> {
            String pathVariable = path.substring("/tasks/".length());
            String statusLine = "";
            String respBody = "";
            String contentLength = "";
            long taskId = Long.parseLong(pathVariable);

            if (!tasks.containsKey(taskId)) {
                statusLine = HTTP_1_1_404_NOT_FOUND;
                contentLength = "0";
                respBody = "{}";
            } else {
                tasks.remove(taskId);

                statusLine = HTTP_1_1_200_OK;
                respBody = new Gson().toJson(tasks);
                contentLength = String.valueOf(respBody.getBytes().length);
            }

            respHeaderMap.put("Content-Length", contentLength);
            respHeaderMap.put("Content-Type", CONTENT_TYPE_JSON);
            respHeaderMap.put("Host", LOCALHOST);
            String responseHeaders = getHeadersAndFlush(respHeaderMap);

            String response = getTotalResponse(statusLine, responseHeaders, respBody);

            try {
                writer.write(response);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("writer exception", e);
            }
        });
    }

    private static String getTotalResponse(String statusLine, String responseHeaders, String respBody) {
        StringJoiner sj = new StringJoiner("\n");
        String response = sj.add(statusLine)
                .add(responseHeaders)
                .add(BLANK_LINE)
                .add(respBody)
                .toString();
        return response;
    }
}

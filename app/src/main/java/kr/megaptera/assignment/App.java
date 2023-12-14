package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.*;
import java.util.function.Function;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private final PathMatcher pathMatcher = new PathMatcher();
    private long id = 0;

    private long getNextId() {
        return ++id;
    }

    private void run() throws IOException {
        int port = 8080;
        // TODO: 요구사항에 맞게 과제를 진행해주세요.
        Map<Long, String> tasks = new HashMap<>();

        pathMatcher.addPath("GET", "/tasks", httpRequest -> {
            return new HttpResponse(200, "Ok", new Gson().toJson(tasks));
        });

        pathMatcher.addPath("POST", "/tasks", httpRequest -> {
            String task;
            try {
                JsonElement element = JsonParser.parseString(httpRequest.getRequestBody());
                task = element.getAsJsonObject().get("task").getAsString();
            } catch (Exception e) {
                return new HttpResponse(400, "Bad Request", new Gson().toJson(tasks));
            }

            tasks.put(getNextId(), task);
            return new HttpResponse(201, "Created", new Gson().toJson(tasks));
        });

        pathMatcher.addPath("PATCH", "/tasks/{id}", httpRequest -> {
            String task;
            try {
                JsonElement element = JsonParser.parseString(httpRequest.getRequestBody());
                task = element.getAsJsonObject().get("task").getAsString();
            } catch (Exception e) {
                return new HttpResponse(400, "Bad Request", new Gson().toJson(tasks));
            }
            long targetId = Long.parseLong(httpRequest.getPathVariables().get("id"));
            if (tasks.containsKey(targetId)) {
                tasks.put(targetId, task);
                return new HttpResponse(200, "Ok", new Gson().toJson(tasks));
            }
            return new HttpResponse(404, "Not Found", new Gson().toJson(tasks));
        });

        pathMatcher.addPath("DELETE", "/tasks/{id}", httpRequest -> {
            long targetId = Long.parseLong(httpRequest.getPathVariables().get("id"));
            if (tasks.containsKey(targetId)) {
                tasks.remove(targetId);
                return new HttpResponse(200, "Ok", new Gson().toJson(tasks));
            }
            return new HttpResponse(404, "Not Found", new Gson().toJson(tasks));
        });

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);
        while (true) {
            // 2. Accept
            Socket socket = listener.accept();
            Runnable runnable = () -> {
                try {
                    InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                    // 3. Request
                    String requestPayload = readFromInputStream(reader);
                    String responsePayload = onClientRequest(requestPayload);

                    // 4. Response
                    writer.write(responsePayload);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            new Thread(runnable).start();
        }
    }

    private String readFromInputStream(InputStreamReader inputStreamReader) throws IOException {
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        inputStreamReader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();
    }

    private String onClientRequest(String message) {
        String[] elements = message.replace("\r", "").split("\n\n");
        String header = elements[0];
        String body = elements.length > 1 ? elements[1] : "";

        HttpStartLine httpStartLine = parseStartLine(header);
        if (httpStartLine == null) {
            return createHttpResponse(400, "Bad Request", "text/html", "");
        }

        HttpHeader httpHeader = parseHeader(header);
        String endpoint = findEndpoint(httpStartLine);

        PathMatcherResult result = pathMatcher.find(httpStartLine.getMethod(), endpoint);
        if (result == null) {
            return createHttpResponse(404, "Not Found", "text/html", "");
        } else {
            try {
                HttpRequest httpRequest = new HttpRequest(httpHeader, result.getPathVariableMap(), body);
                HttpResponse httpResponse = result.getHandler().apply(httpRequest);
                return createHttpResponse(httpResponse.getStatusCode(), httpResponse.getStatus(), "text/html", httpResponse.getBody().trim() + "\n");
            } catch (Exception e) {
                return createHttpResponse(500, "Internal Server Error", "text/html", "");
            }
        }
    }

    /**
     * 첫번째 이후 라인부터 헤더를 추출한다.
     * 첫번째 콜론(:)을 기준으로 Key-Value(String-String)로 분리함.
     */
    private HttpHeader parseHeader(String header) {
        int newLineIndex = header.indexOf("\n");
        if (header.isBlank()) {
            return null;
        }
        String headerString = header.substring(newLineIndex + 1);
        String[] elements = headerString.split("\n");
        Map<String, String> headerMap = new HashMap<>();
        for (String element : elements) {
            if (element.isBlank()) {
                continue;
            }
            int colonIndex = element.indexOf(":");
            String key = element.substring(0, colonIndex);
            String value = element.substring(colonIndex).trim();
            headerMap.put(key, value);
        }
        return new HttpHeader(headerMap);
    }

    public String createHttpResponse(int statusCode, String statusString, String contentType, String body) {
        byte[] bytes = body.getBytes();
        return String.format("""
                HTTP/1.1 %d %s
                Content-Type: %s
                Content-Length: %d
                Connection: close
                                
                %s""", statusCode, statusString, contentType, bytes.length, body);
    }

    /**
     * 요청문의 첫번째 라인에서 Start Line의 내용을 추출한다
     * 유효성 검증안함
     */
    private HttpStartLine parseStartLine(String line) {
        String[] elements = line.split(" ");
        String method = elements[0];
        String requestTarget = elements[1];
        String httpVersion = elements[2];
        return new HttpStartLine(method, requestTarget, httpVersion);
    }

    /**
     * URL이나 도메인의 절대 경로에서 Endpoint부분을 추출한다.
     * 유효성 검증안함
     * <br>
     * - 예시
     * http://localhost:8080/hello/world -> /hello/world
     * /hello/world -> /hello/world
     */
    private String findEndpoint(HttpStartLine httpStartLine) {
        String endpoint;
        if (httpStartLine.isAbsolute()) {
            String requestTarget = httpStartLine.getRequestTarget();
            requestTarget = requestTarget.replace("://", "");
            int slashIndex = requestTarget.indexOf("/");
            endpoint = requestTarget.substring(slashIndex);
        } else {
            endpoint = httpStartLine.getRequestTarget();
        }
        return endpoint;
    }

    private static class HttpStartLine {
        private final String method;
        private final String requestTarget;
        private final String httpVersion;
        // 요청 타겟: Origin, Absolute, Authority, Asterisk
        // 일단 absolute 인지만 판단하자
        private final boolean isAbsolute;

        public HttpStartLine(String method, String requestTarget, String httpVersion) {
            this.method = method;
            this.requestTarget = requestTarget;
            this.httpVersion = httpVersion;
            this.isAbsolute = isAbsolute(requestTarget);
        }

        private boolean isAbsolute(String requestTarget) {
            return !requestTarget.startsWith("/");
        }

        public String getMethod() {
            return method;
        }

        public String getRequestTarget() {
            return requestTarget;
        }

        public String getHttpVersion() {
            return httpVersion;
        }

        public boolean isAbsolute() {
            return isAbsolute;
        }
    }

    public class PathMatcherResult {
        private final Map<String, String> pathVariableMap;
        private final Function<HttpRequest, HttpResponse> handler;

        public PathMatcherResult(Map<String, String> pathVariableMap, Function<HttpRequest, HttpResponse> handler) {
            this.pathVariableMap = pathVariableMap;
            this.handler = handler;
        }

        public Map<String, String> getPathVariableMap() {
            return pathVariableMap;
        }

        public Function<HttpRequest, HttpResponse> getHandler() {
            return handler;
        }
    }


    public class PathMatcher {
        private final Map<String, List<PathMatcherPair>> requestPathMap;
        private static final Set<String> ACCEPTABLE_METHOD_SET = new HashSet<>(Arrays.asList(
                "GET", "POST", "PATCH", "DELETE"
        ));

        public PathMatcher() {
            this.requestPathMap = new HashMap<>();
            requestPathMap.put("GET", new ArrayList<>());
            requestPathMap.put("POST", new ArrayList<>());
            requestPathMap.put("PATCH", new ArrayList<>());
            requestPathMap.put("DELETE", new ArrayList<>());
        }

        public PathMatcherResult find(String method, String value) {
            if (!ACCEPTABLE_METHOD_SET.contains(method.toUpperCase())) {
                return null;
            }
            for (PathMatcherPair pathMatcherPair : requestPathMap.get(method.toUpperCase())) {
                if (matched(value, pathMatcherPair.nodeList)) {
                    Map<String, String> pathVariableMap = makePathVariableMap(value, pathMatcherPair.nodeList);
                    return new PathMatcherResult(pathVariableMap, pathMatcherPair.handler);
                }
            }
            return null;
        }

        /**
         * GET, POST, PATCH, DELETE 중 하나의 메서드만 등록이 가능
         *
         * @param method
         * @param path
         * @param consumer
         */
        public void addPath(String method, String path, Function<HttpRequest, HttpResponse> consumer) {
            if (!ACCEPTABLE_METHOD_SET.contains(method.toUpperCase())) {
                return;
            }
            String[] elements = path.split("/");
            List<PathMatcherNode> nodeList = new ArrayList<>();
            for (String element : elements) {
                PathMatcherNode node = PathMatcherNode.parse(element);
                nodeList.add(node);
            }
            PathMatcherPair pathMatcherPair = new PathMatcherPair(nodeList, consumer);
            requestPathMap.get(method.toUpperCase()).add(pathMatcherPair);
        }


        private Map<String, String> makePathVariableMap(String value, List<PathMatcherNode> nodeList) {
            String[] elements = value.split("/");
            if (elements.length != nodeList.size()) {
                return null;
            }

            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < elements.length; i++) {
                String element = elements[i];
                PathMatcherNode node = nodeList.get(i);
                if (node.isPathVariable) {
                    result.put(node.name, element);
                }
            }
            return result;
        }

        private boolean matched(String value, List<PathMatcherNode> nodeList) {
            String[] elements = value.split("/");
            if (elements.length != nodeList.size()) {
                return false;
            }

            for (int i = 0; i < elements.length; i++) {
                String element = elements[i];
                PathMatcherNode node = nodeList.get(i);
                if (!node.isPathVariable && !node.name.equals(element)) {
                    return false;
                }
            }
            return true;
        }

        public static class PathMatcherPair {
            private final List<PathMatcherNode> nodeList;
            private final Function<HttpRequest, HttpResponse> handler;

            public PathMatcherPair(List<PathMatcherNode> nodeList, Function<HttpRequest, HttpResponse> handler) {
                this.nodeList = nodeList;
                this.handler = handler;
            }
        }

        public static class PathMatcherNode {
            private final String name;
            private final boolean isPathVariable;

            public PathMatcherNode(String name, boolean isPathVariable) {
                this.name = name;
                this.isPathVariable = isPathVariable;
            }

            public static PathMatcherNode parse(String value) {
                String name;
                boolean isPathVariable;

                if (value.startsWith("{") && value.endsWith("}")) {
                    isPathVariable = true;
                    name = value.replaceAll("(^\\{|}$)", "");
                } else {
                    isPathVariable = false;
                    name = value;
                }
                return new PathMatcherNode(name, isPathVariable);
            }
        }
    }

    public class HttpHeader {
        // 이 정도로 하자
        private Map<String, String> headers;

        public HttpHeader(@Nonnull Map<String, String> headers) {
            this.headers = headers;
        }

        public HttpHeader(String key, String value) {
            this(new HashMap<>());
            this.headers.put(key, value);
        }

        public String getHeaderValue(String key) {
            return headers.get(key);
        }
    }


    public class HttpRequest {
        private final HttpHeader httpHeader;
        private final Map<String, String> pathVariables;
        private final String requestBody;

        public HttpRequest(HttpHeader httpHeader, Map<String, String> pathVariables, String requestBody) {
            this.httpHeader = httpHeader;
            this.pathVariables = pathVariables;
            this.requestBody = requestBody;
        }

        public HttpHeader getHttpHeader() {
            return httpHeader;
        }

        public Map<String, String> getPathVariables() {
            return pathVariables;
        }

        public String getRequestBody() {
            return requestBody;
        }
    }

    public class HttpResponse {
        private final int statusCode;
        private final String status;
        private final String body;

        public HttpResponse(int statusCode, String status, String body) {
            this.statusCode = statusCode;
            this.status = status;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }
    }
}

package kr.megaptera.assignment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static kr.megaptera.assignment.TodoService.createTodo;
import static kr.megaptera.assignment.TodoService.deleteTodo;
import static kr.megaptera.assignment.TodoService.getTodo;
import static kr.megaptera.assignment.TodoService.hasTodoItem;
import static kr.megaptera.assignment.TodoService.updateTodo;

public class App {
    static final String HTTP_METHOD_GET = "GET";
    static final String HTTP_METHOD_POST = "POST";
    static final String HTTP_METHOD_PATCH = "PATCH";
    static final String HTTP_METHOD_DELETE = "DELETE";
    static final String TASK_REQ_MAPPING = "/tasks";

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        // TODO: 요구사항에 맞게 과제를 진행해주세요.
        try(// 1. Listen
            ServerSocket listener = new ServerSocket(port)
            ){
            while(true){
                // 2. Accept
                Socket socket = listener.accept();

                // 3. Request
                String requestData = getRequestData(socket);
                RequestDto requestDto = parseRequestData(requestData);
                if(requestDto != null){
                    // 4. Response
                    responseToClientByRequest(requestDto, socket);
                }
                // 5. 처리후 소켓 닫기
                socket.close();
            }
        }
    }

    private void responseToClientByRequest(RequestDto requestDto, Socket socket) throws IOException {


        final String httpMethod = requestDto.getHttpMethod();
        final String pathFirstPart = requestDto.getPathFirstPart();
        final String todoId = requestDto.getPathSecondPart();
        HttpStatusType httpStatusType = HttpStatusType.INTERNAL_SERVER_ERROR;
        String responseBody = "";

        if(httpMethod != null && pathFirstPart != null && TASK_REQ_MAPPING.equals(pathFirstPart)){
            if (HTTP_METHOD_GET.equals(httpMethod)){
                // get 목록.
                httpStatusType = HttpStatusType.OK;
                responseBody = getTodo();
            } else if (HTTP_METHOD_POST.equals(httpMethod)) {
                if( !hasBody(requestDto.getRawBody(), requestDto.getBody()) ){
                    // 400 bad request
                    httpStatusType = HttpStatusType.BAD_REQUEST;
                }else{
                    // 생성.
                    responseBody = createTodo(requestDto.getBody());
                    httpStatusType = HttpStatusType.CREATED;
                }
            } else if (HTTP_METHOD_PATCH.equals(httpMethod)) {
                if(todoId == null || hasTodoItem(todoId) == false){
                    // 404 not found
                    httpStatusType = HttpStatusType.NOT_FOUND;
                }else if(!hasBody(requestDto.getRawBody(), requestDto.getBody())){
                    // 400 bad request
                    httpStatusType = HttpStatusType.BAD_REQUEST;
                }else{
                    // 수정.
                    responseBody = updateTodo(Integer.valueOf(todoId),requestDto.getBody());
                    httpStatusType = HttpStatusType.OK;
                }
            } else if(HTTP_METHOD_DELETE.equals(httpMethod)){
                if(todoId == null || hasTodoItem(todoId) == false){
                    // 404 not found
                    httpStatusType = HttpStatusType.NOT_FOUND;
                }else {
                    // 삭제
                    responseBody = deleteTodo(Integer.valueOf(todoId));
                    httpStatusType = HttpStatusType.OK;
                }
            }
        }

        responseToClient(socket, httpStatusType, responseBody, requestDto.getHost());
    }

    private void responseToClient(Socket socket, HttpStatusType httpStatusType, String responseBody, String host) throws IOException {
        byte[] bytes = responseBody.getBytes();
        String message = "" +
            "HTTP/1.1 "+ httpStatusType.value()+ " "+ httpStatusType.getReasonPhrase()+ "\n" +
            "Content-Length: " + bytes.length + "\n" +
            "Content-Type: application/json; charset=UTF-8\n" +
            "Host: "+ host +"\n" +
            "\n" +
            responseBody;
        // 6-2 전송
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

        writer.write(message);
        writer.flush();
    }

    private boolean hasBody(String rawBody, JsonElement body) {
        return !(body == null ||
            body.isJsonNull() ||
            rawBody == null ||
            rawBody.isEmpty());
    }


    private RequestDto parseRequestData(String requestData) {
        String patternString = "^([A-Z]+)\\s(.+?)\\s.*\\r?\\n((.|\\n|\\r)*)Host:\\s(.+)((.|\\n|\\r)*)\\r?\\n\\r?\\n((.|\\n|\\r)*)$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(requestData);
        matcher.find();
        RequestDto requestDto = new RequestDto();
        requestDto.setHttpMethod(matcher.group(1));

        requestDto.setPath(matcher.group(2));
        String path = requestDto.getPath();
        int lastIndexOfSlash = path.lastIndexOf("/");
        if(lastIndexOfSlash == 0){
            requestDto.setPathFirstPart(path);
        }else{
            requestDto.setPathFirstPart(path.substring(0,lastIndexOfSlash));
            requestDto.setPathSecondPart(path.substring(lastIndexOfSlash+1));
        }


        requestDto.setHost(matcher.group(5));

        requestDto.setRawBody(matcher.group(8));
        if(requestDto.getRawBody() != null){
            requestDto.setBody(JsonParser.parseString(requestDto.getRawBody()));
        }
        return requestDto;
    }

    private String getRequestData(Socket socket) throws IOException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();
    }
}

package kr.megaptera.assignment;

public class reponseProvider {

    // Request Parser
    private final requestParser requestParser;

    // Tasks
    private final taskRepository taskRepository;

    public reponseProvider(String requestMessage, taskRepository taskRepository) {
        this.requestParser = new requestParser(requestMessage);
        // repository는 주입
        this.taskRepository = taskRepository;
    }

    public String getResponseMessage() {
        switch (checkMethod()) {
            case POST:
                return makePostResponse();
            case PATCH:
                return makePatchResponse();
            case DELETE:
                return makeDeleteResponse();
            default:
                return makeGetResponse();
        }
    }


    private Method checkMethod() {
        return Method.valueOf(this.requestParser.getHttpMethod());
        // TODO: ENUM 예외처리? Method 외의 값이 들어온다면?
    }

    private String makeGetResponse() {
        String messageBody = this.taskRepository.getTasksByJson();
        return buildMessage(StatusCode.OK, messageBody);
    }

    private String makePostResponse() {
        String requestBody = this.requestParser.getBody();
        // 메세지 생성 하기
        if (requestBody.isEmpty()) {
            // return 400 Bad Request
            return buildMessage(StatusCode.BAD_REQUEST, "");
        }
        // Create
        this.taskRepository.insert(this.requestParser.getBody());
        // Post Response message 만들기
        String messageBody = this.taskRepository.getTasksByJson();
        return buildMessage(StatusCode.CREATED, messageBody);
    }

    private String makePatchResponse() {
        // key 추출
        String key = this.requestParser.getTaskKey();
        // key에 대한 예외처리
        Long LongTypeKey;
        try {
            LongTypeKey = Long.valueOf(key);
        } catch (NumberFormatException exception) {
            // 404 Not Found
            return buildMessage(StatusCode.NON_FOUND, "");
        }
        if (!this.taskRepository.checkKey(LongTypeKey)) {
            return buildMessage(StatusCode.NON_FOUND, "");
        }
        // body check
        String requestBody = this.requestParser.getBody();
        if (requestBody.isEmpty()) {
            // return 400 Bad Request
            return buildMessage(StatusCode.BAD_REQUEST, "");
        }
        // Update
        this.taskRepository.update(LongTypeKey, this.requestParser.getBody());
        // Patch Complete : OK
        return makeGetResponse();
    }

    private String makeDeleteResponse() {
        // key 추출
        String key = this.requestParser.getTaskKey();
        // key에 대한 예외처리
        Long LongTypeKey;
        try {
            LongTypeKey = Long.valueOf(key);
        } catch (NumberFormatException exception) {
            // 404 Not Found
            return buildMessage(StatusCode.NON_FOUND, "");
        }
        if (!this.taskRepository.checkKey(LongTypeKey)) {
            return buildMessage(StatusCode.NON_FOUND, "");
        }
        // Delete
        this.taskRepository.delete(LongTypeKey);
        // Delete Complete : OK
        return makeGetResponse();
    }

    private String buildMessage(StatusCode statusCode, String body) {
        byte[] bytes = body.getBytes();
        return "HTTP/1.1 " + statusCode.getCode() + " " + statusCode.getMessage() + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + requestParser.getHost() + "\n" +
                "\n" +
                body;
    }
}

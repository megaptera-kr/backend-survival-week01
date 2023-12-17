package kr.megaptera.assignment;

public class reponseProvider {

    // Request Parser
    private final requestParser requestParser;

    // Tasks
    private taskRepository taskRepository;

    public reponseProvider(String requestMessage) {
        this.requestParser = new requestParser(requestMessage);
        this.taskRepository = new taskRepository();
    }

    public String getResponseMessage() {
        switch (checkMethod()) {
            case GET:
                return makeGetResponse();
            case POST:
                return makePostResponse();
            case PATCH:
                break;
            case DELETE:
                break;
        }
        return "";
    }


    private Method checkMethod() {
        return Method.valueOf(this.requestParser.getHttpMethod());
        // TODO: ENUM 예외처리? Method 외의 값이 들어온다면?
    }

    private String makeGetResponse() {
        String body = this.taskRepository.getTasksByJson();
        byte[] bytes = body.getBytes();
        return "HTTP/1.1 200 OK\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + requestParser.getHost() + "\n" +
                "\n" +
                body;
    }

    private String makePostResponse() {
        String requestBody = this.requestParser.getBody();
        // 메세지 생성 하기
        if (requestBody == null) {
            // return 400 Bad Request
            return "HTTP/1.1 400 Bad Request\n" +
                    "Content-Length: " + 0 + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Host: " + requestParser.getHost() + "\n" +
                    "\n";
        }
        // Create
        this.taskRepository.insert(this.requestParser.getBody());
        // Post Response message 만들기
        String body = this.taskRepository.getTasksByJson();
        byte[] bytes = body.getBytes();
        return "HTTP/1.1 200 OK\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + requestParser.getHost() + "\n" +
                "\n" +
                body;
    }

    private String makePatchResponse() {
        return null;
    }

    private String makeDeleteResponse() {
        return null;
    }

}

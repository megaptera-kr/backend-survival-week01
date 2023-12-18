package kr.megaptera.assignment;

public class requestParser {
    private String[] parsedResultByLine;

    private String startLine;
    private String headers;
    private String body;

    public requestParser(String requestMessage) {
        String[] parts = requestMessage.split("\r\n\r\n", 2);
        String[] notBody = parts[0].split("\r\n", 2);
        this.startLine = notBody[0];
        this.headers = notBody[1];
        if (parts.length > 1) {
            this.body = parts[1];
        }
        parsedResultByLine = parseByLine(requestMessage);
    }

    public String getHttpMethod() {
        return getStartLine().split(" ")[0];
    }

    public String getTaskKey() {
        String[] parts = getTargetPath().split("/");
        // 마지막요소 : key
        return parts[parts.length - 1];
    }

    public String getHost() {
        return getHeaderFirstLine().split(" ")[1];
    }

    public String getBody() {
        return this.body;
    }

    private String[] parseByLine(String requestMessage) {
        return requestMessage.split("\n");
    }

    private String getStartLine() {
        return parsedResultByLine[0];
    }

    private String getHeaderFirstLine() {
        return parsedResultByLine[1];
    }

    private String getTargetPath() {
        return getStartLine().split(" ")[1];
    }

}

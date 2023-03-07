package kr.megaptera.assignment.models;

public class HttpRequestSource {
    private String startLine;
    private String[] headers;
    private String[] bodies;

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String[] getBodies() {
        return bodies;
    }

    public void setBodies(String[] bodies) {
        this.bodies = bodies;
    }
}

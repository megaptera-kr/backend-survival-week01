package kr.megaptera.assignment.models;

import java.util.HashMap;

public class HttpStartLine {
    private HttpMethodType httpMethodType;
    private String path;
    private String version;

    public HttpMethodType getHttpMethodType() {
        return httpMethodType;
    }

    public void setHttpMethodType(HttpMethodType httpMethodType) {
        this.httpMethodType = httpMethodType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

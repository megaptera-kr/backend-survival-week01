package kr.megaptera.assignment.models;

import java.util.HashMap;
import java.util.HashSet;

public class HttpStartLine {
    private HttpMethodType httpMethodType;
    private String path;
    private String version;
    private HashSet<HttpParameter> parameters;

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

    public HashSet<HttpParameter> getParameters() {
        return parameters;
    }

    public void setParameters(HashSet<HttpParameter> parameters) {
        this.parameters = parameters;
    }
}

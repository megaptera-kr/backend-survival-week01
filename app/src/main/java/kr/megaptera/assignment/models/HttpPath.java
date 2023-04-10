package kr.megaptera.assignment.models;

import kr.megaptera.assignment.functionals.HttpProcessFunction;

public class HttpPath {
    private HttpMethodType methodType;
    private String path;
    private HttpPathType pathType;
    private HttpProcessFunction processFunction;

    public HttpPath(HttpMethodType methodType, String path, HttpPathType pathType, HttpProcessFunction processFunction) {
        this.methodType = methodType;
        this.path = path;
        this.pathType = pathType;
        this.processFunction = processFunction;
    }
    
    public HttpMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(HttpMethodType methodType) {
        this.methodType = methodType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpPathType getPathType() {
        return pathType;
    }

    public void setPathType(HttpPathType pathType) {
        this.pathType = pathType;
    }

    public HttpProcessFunction getProcessFunction() {
        return processFunction;
    }

    public void setProcessFunction(HttpProcessFunction processFunction) {
        this.processFunction = processFunction;
    }
}

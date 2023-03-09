package kr.megaptera.assignment.managers;

import kr.megaptera.assignment.functionals.HttpProcessFunction;
import kr.megaptera.assignment.models.HttpMethodType;

import java.util.HashMap;

public class HttpPathBindingManager {

    private HashMap<String, HttpProcessFunction> bindings;

    public void Put(HttpMethodType methodType, String path, HttpProcessFunction function){
        bindings.put(makeKey(methodType, path), function);
    }

    public HttpProcessFunction Get(HttpMethodType methodType, String path){
        return bindings.getOrDefault(makeKey(methodType, path), null);
    }

    private static String makeKey(HttpMethodType methodType, String path) {
        return methodType.name() + " " + path;
    }
}

package kr.megaptera.assignment.managers;

import kr.megaptera.assignment.functionals.HttpProcessFunction;
import kr.megaptera.assignment.models.HttpMethodType;
import kr.megaptera.assignment.models.HttpPath;
import kr.megaptera.assignment.models.HttpPathType;

import java.util.ArrayList;

public class HttpPathBindingManager {

    private ArrayList<HttpPath> httpPaths = new ArrayList<HttpPath>();

    public void Add(HttpPath newHttpPath) throws Exception {
        for (var httpPath:httpPaths) {
            var hasSamePath = httpPath.getPath() == newHttpPath.getPath();
            var hasSameType = httpPath.getMethodType() == newHttpPath.getMethodType();

            if(hasSamePath && hasSameType){
                throw new Exception();
            }
        }

        httpPaths.add(newHttpPath);
    }

    public HttpProcessFunction Get(HttpMethodType methodType, String path){
        for (var httpPath: httpPaths) {

            var anyResourcePath = "";

            if(httpPath.getPathType() == HttpPathType.HasValue){
                var containResourcePath = path;
                var lastParameter = containResourcePath.lastIndexOf('/');
                anyResourcePath = httpPath.getPath().substring(0, lastParameter);
            }
            else{
                anyResourcePath = path;
            }

            var hasSameType = httpPath.getMethodType() == methodType;
            var hasSamePath = httpPath.getPath().equals(anyResourcePath);

            if(hasSamePath && hasSameType){
                return httpPath.getProcessFunction();
            }
        }

        return null;
    }
}

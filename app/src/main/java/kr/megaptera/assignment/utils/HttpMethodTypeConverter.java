package kr.megaptera.assignment.utils;

import kr.megaptera.assignment.models.HttpMethodType;

public class HttpMethodTypeConverter {

    public static HttpMethodType Convert(String source){
        source = source.toUpperCase();

        switch (source){
            case "GET":
                return HttpMethodType.Get;
            case "POST":
                return HttpMethodType.Post;
            case "PATCH":
                return HttpMethodType.Patch;
            case "DELETE":
                return HttpMethodType.Delete;
            default:
                throw new UnsupportedOperationException("System find not support method : " + source);
        }
    }
}

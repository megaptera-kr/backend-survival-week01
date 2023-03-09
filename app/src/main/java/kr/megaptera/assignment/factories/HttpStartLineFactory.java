package kr.megaptera.assignment.factories;

import kr.megaptera.assignment.models.HttpFirstLine;
import kr.megaptera.assignment.models.HttpMethodType;

public class HttpStartLineFactory {
    public static HttpFirstLine Create(String message){

        var sources = message.split(" ");

        var model =  new HttpFirstLine();

        var methodString = sources[0].toUpperCase();

        switch (methodString){
            case "GET":
                model.setHttpMethodType(HttpMethodType.Get);
                break;
            case "POST":
                model.setHttpMethodType(HttpMethodType.Post);
                break;
            case "PATCH":
                model.setHttpMethodType(HttpMethodType.Patch);
                break;
            case "DELETE":
                model.setHttpMethodType(HttpMethodType.Delete);
                break;
            default:
                throw new UnsupportedOperationException("System find not support method : " + methodString);
        }

        model.setPath(sources[1]);
        model.setVersion(sources[2]);

        return model;
    }
}

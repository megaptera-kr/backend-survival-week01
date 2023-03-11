package kr.megaptera.assignment.factories;

import kr.megaptera.assignment.models.HttpStartLine;
import kr.megaptera.assignment.utils.HttpMethodTypeConverter;

public class HttpStartLineFactory {
    public HttpStartLine Create(String message){

        var sources = message.split(" ");
        var model = new HttpStartLine();

        model.setHttpMethodType(HttpMethodTypeConverter.Convert(sources[0]));
        model.setPath(sources[1]);
        model.setVersion(sources[2]);

        return model;
    }
}

package kr.megaptera.assignment.utils;

import kr.megaptera.assignment.models.HttpParameter;

public class HttpParameterConverter {

    private static final String parser = "=";

    public static HttpParameter Convert(String source){
        var parsedSource = source.split(parser);

        var parameter = new HttpParameter();
        parameter.setKey(parsedSource[0]);
        parameter.setValue(parsedSource[1]);

        return parameter;
    }
}

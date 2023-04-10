package kr.megaptera.assignment.utils;

import kr.megaptera.assignment.models.HttpPathWithParameter;

public class PathWithParameterConverter {

    private static final String originParser = "?";
    private static final String parser = "\\?";

    public static HttpPathWithParameter Convert(String source) {
        var model = new HttpPathWithParameter();

        if(source.contains(originParser)){
            var sources = source.split(parser);
            model.setPath(sources[0]);
            model.setParameter(sources[1]);
        }
        else{
            model.setPath(source);
            model.setParameter("");
        }

        return model;
    }

}

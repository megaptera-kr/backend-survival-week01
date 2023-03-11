package kr.megaptera.assignment.utils;

import kr.megaptera.assignment.models.HttpParameter;

import java.util.HashMap;

public class HttpParametersConverter {

    private static final String parser = "&";

    public static HashMap<String, String> Convert(String source) {
        var hashMap = new HashMap<String, String>();

        if (source == null || source.length() == 0)
        {
            return hashMap;
        }

        var paramSources = source.split(parser);

        for (String paramSource : paramSources) {
            var httpParameter = HttpParameterConverter.Convert(paramSource);
            hashMap.put(httpParameter.getKey(), httpParameter.getValue());
        }

        return hashMap;
    }
}

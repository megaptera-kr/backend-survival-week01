package kr.megaptera.assignment.utils;

import kr.megaptera.assignment.models.HttpParameter;

import java.util.HashSet;

public class HttpParametersConverter {

    private static final String parser = "&";

    public static HashSet<HttpParameter> Convert(String source) {
        var hashSet = new HashSet<HttpParameter>();

        if (source == null || source.length() == 0)
        {
            return hashSet;
        }

        var paramSources = source.split(parser);

        for (String paramSource : paramSources) {
            var httpParameter = HttpParameterConverter.Convert(paramSource);
            hashSet.add(httpParameter);
        }

        return hashSet;
    }
}

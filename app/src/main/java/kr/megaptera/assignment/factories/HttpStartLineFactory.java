package kr.megaptera.assignment.factories;

import kr.megaptera.assignment.models.HttpStartLine;
import kr.megaptera.assignment.utils.HttpMethodTypeConverter;
import kr.megaptera.assignment.utils.HttpParametersConverter;
import kr.megaptera.assignment.utils.PathWithParameterConverter;

public class HttpStartLineFactory {
    public HttpStartLine Create(String message){

        var sources = message.split(" ");
        var model = new HttpStartLine();

        model.setHttpMethodType(HttpMethodTypeConverter.Convert(sources[0]));

        var pathWithParameter = PathWithParameterConverter.Convert(sources[1]);
        var parameters = HttpParametersConverter.Convert(pathWithParameter.getParameter());

        model.setPath(pathWithParameter.getPath());
        model.setParameters(parameters);

        model.setVersion(sources[2]);

        return model;
    }
}

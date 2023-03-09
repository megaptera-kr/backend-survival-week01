package kr.megaptera.assignment.functionals;

import kr.megaptera.assignment.models.HttpRequestSource;
import kr.megaptera.assignment.models.HttpResponseSource;

public interface HttpProcessFunction {
    public abstract HttpResponseSource execute(HttpRequestSource requestSource);
}

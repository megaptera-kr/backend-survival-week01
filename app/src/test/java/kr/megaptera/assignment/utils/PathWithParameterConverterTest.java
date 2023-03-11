package kr.megaptera.assignment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathWithParameterConverterTest {
    @Test
    void stringSource_toHttpPathWithParameter_shouldReturn() {
        var path = "http://example.com/example";
        var parameters = "param1=value1&param2=value2";
        var source = path + "?" + parameters;

        var httpPathWithParameter = PathWithParameterConverter.Convert(source);

        assertEquals(path, httpPathWithParameter.getPath());
        assertEquals(parameters, httpPathWithParameter.getParameter());
    }

    @Test
    void stringSource_to_shouldReturnHttpParameters() {
        var source = "http://example.com/example";
        var httpPathWithParameter = PathWithParameterConverter.Convert(source);

        assertEquals(source, httpPathWithParameter.getPath());
        assertEquals("", httpPathWithParameter.getParameter());
    }
}
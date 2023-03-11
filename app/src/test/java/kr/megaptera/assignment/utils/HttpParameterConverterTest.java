package kr.megaptera.assignment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpParameterConverterTest {

    @Test
    void stringSource_toParameterSet_shouldReturnHttpParameter() {
        String key = "parameter1";
        String value = "value1";
        String source = key + "=" + value;

        var httpParameter = HttpParameterConverter.Convert(source);

        assertEquals(key, httpParameter.getKey());
        assertEquals(value, httpParameter.getValue());
    }
}
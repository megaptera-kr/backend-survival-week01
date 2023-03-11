package kr.megaptera.assignment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpParametersConverterTest {
    @Test
    void stringSource_toParameterSets_shouldReturnHttpParameters() {
        String source = "parameter1=value1&parameter2=value2";
        var httpParameters = HttpParametersConverter.Convert(source);

        assertEquals(2, httpParameters.size());
    }
}
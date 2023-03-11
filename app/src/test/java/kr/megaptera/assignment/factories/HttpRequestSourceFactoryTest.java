package kr.megaptera.assignment.factories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestSourceFactoryTest {

    // MethodName_ExpectedBehavior_StateUnderTest
    @Test
    void getRequestMessage_withSingleHeaderAndSingleBody_makeCorrectRequestSource() {
        var requestMessage = """
            GET /get-method HTTP/1.1
            Host: example.com
            
            Hello world!
            """;

        var factory = new HttpRequestSourceFactory();
        var requestSource = factory.Create(requestMessage);

        var headers = requestSource.getHeaders();
        assertEquals(1, headers.length);
        assertEquals("Host: example.com", headers[0]);

        var body = requestSource.getBody();
        assertEquals("Hello world!", body);
    }

    @Test
    void getRequestMessage_withMultipleHeaderAndMultipleBody_makeCorrectRequestSource() {
        var requestMessage = """
                GET /get-method HTTP/1.1
                Header1: example.com
                Header2: example.com
                
                BodyLine 1
                BodyLine 2
                """;

        var factory = new HttpRequestSourceFactory();
        var requestSource = factory.Create(requestMessage);

        var headers = requestSource.getHeaders();
        assertEquals(2, headers.length);
        assertEquals("Header1: example.com", headers[0]);
        assertEquals("Header2: example.com", headers[1]);

        var body = requestSource.getBody();
        var expectBody = """
                BodyLine 1
                BodyLine 2""";
        assertEquals(expectBody, body);
    }

}

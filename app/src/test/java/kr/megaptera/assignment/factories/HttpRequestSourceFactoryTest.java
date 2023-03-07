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

        var requestSource = HttpRequestSourceFactory.Create(requestMessage);

        assertEquals("GET /get-method HTTP/1.1", requestSource.getStartLine());
        var headers = requestSource.getHeaders();
        assertEquals(1, headers.length);
        assertEquals("Host: example.com", headers[0]);

        var bodies = requestSource.getBodies();
        assertEquals(1, bodies.length);
        assertEquals("Hello world!", bodies[0]);
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

        var requestSource = HttpRequestSourceFactory.Create(requestMessage);

        assertEquals("GET /get-method HTTP/1.1", requestSource.getStartLine());
        var headers = requestSource.getHeaders();
        assertEquals(2, headers.length);
        assertEquals("Header1: example.com", headers[0]);
        assertEquals("Header2: example.com", headers[1]);

        var bodies = requestSource.getBodies();
        assertEquals(2, bodies.length);
        assertEquals("BodyLine 1", bodies[0]);
        assertEquals("BodyLine 2", bodies[1]);
    }

}

package com.microsoft.rest.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class MockHttpClientTests {
    @Test
    public void sendRequestInternalAsyncToBase64UrlBytes() {
        final MockHttpClient client = new MockHttpClient();
        final HttpRequest request = new HttpRequest("fake_caller_method", "GET", "http://httpbin.org/base64urlbytes/10");
        final HttpResponse response = client.sendRequestInternalAsync(request).toBlocking().value();
        assertNotNull(response);
        final String bodyString = response.bodyAsStringAsync().toBlocking().value();
        assertEquals("\"AAECAwQFBgcICQ\"", bodyString);
        assertArrayEquals(new byte[] { 34, 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81, 34}, bodyString.getBytes());
    }

    @Test
    public void sendRequestInternalAsyncToBase64UrlListOfBytes() {
        final MockHttpClient client = new MockHttpClient();
        final HttpRequest request = new HttpRequest("fake_caller_method", "GET", "http://httpbin.org/base64urllistofbytes");
        final HttpResponse response = client.sendRequestInternalAsync(request).toBlocking().value();
        assertNotNull(response);
        final String bodyString = response.bodyAsStringAsync().toBlocking().value();
        assertEquals("[\"AAECAwQFBgcICQ\",\"AAECAwQFBgcICQoLDA0ODxAREhM\",\"AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwd\"]", bodyString);
    }

    @Test
    public void sendRequestInternalAsyncToBase64UrlListOfListOfBytes() {
        final MockHttpClient client = new MockHttpClient();
        final HttpRequest request = new HttpRequest("fake_caller_method", "GET", "http://httpbin.org/base64urllistoflistofbytes");
        final HttpResponse response = client.sendRequestInternalAsync(request).toBlocking().value();
        assertNotNull(response);
        final String bodyString = response.bodyAsStringAsync().toBlocking().value();
        assertEquals("[[\"AAECAwQ\",\"AAECAwQFBgcICQ\"],[\"AAECAwQ\",\"AAECAwQFBgcICQ\",\"AAECAwQFBgcICQoLDA0O\",\"AAECAwQFBgcICQoLDA0ODxAREhM\"]]", bodyString);
    }

    @Test
    public void sendRequestInternalAsyncToDateTimeRfc1123() {
        final MockHttpClient client = new MockHttpClient();
        final HttpRequest request = new HttpRequest("fake_caller_method", "GET", "http://httpbin.org/DateTimeRfc1123");
        final HttpResponse response = client.sendRequestInternalAsync(request).toBlocking().value();
        assertNotNull(response);
        final String bodyString = response.bodyAsStringAsync().toBlocking().value();
        assertEquals("\"Thu, 01 Jan 1970 00:00:00 GMT\"", bodyString);
    }

    @Test
    public void sendRequestInternalAsyncToDateTimeUnix() {
        final MockHttpClient client = new MockHttpClient();
        final HttpRequest request = new HttpRequest("fake_caller_method", "GET", "http://httpbin.org/DateTimeUnix");
        final HttpResponse response = client.sendRequestInternalAsync(request).toBlocking().value();
        assertNotNull(response);
        final String bodyString = response.bodyAsStringAsync().toBlocking().value();
        assertEquals("0", bodyString);
    }
}

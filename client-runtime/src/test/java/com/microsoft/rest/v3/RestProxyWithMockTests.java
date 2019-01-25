package com.microsoft.rest.v3;

import com.microsoft.rest.v3.annotations.ExpectedResponses;
import com.microsoft.rest.v3.annotations.GET;
import com.microsoft.rest.v3.annotations.HeaderCollection;
import com.microsoft.rest.v3.annotations.Host;
import com.microsoft.rest.v3.annotations.ReturnValueWireType;
import com.microsoft.rest.v3.entities.HttpBinJSON;
import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import com.microsoft.rest.v3.http.MockHttpResponse;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RestProxyWithMockTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockHttpClient();
    }

    @Host("http://httpbin.org")
    private interface Service1 {
        @GET("Base64UrlBytes/10")
        @ReturnValueWireType(Base64Url.class)
        byte[] getBase64UrlBytes10();

        @GET("Base64UrlListOfBytes")
        @ReturnValueWireType(Base64Url.class)
        List<byte[]> getBase64UrlListOfBytes();

        @GET("Base64UrlListOfListOfBytes")
        @ReturnValueWireType(Base64Url.class)
        List<List<byte[]>> getBase64UrlListOfListOfBytes();

        @GET("Base64UrlMapOfBytes")
        @ReturnValueWireType(Base64Url.class)
        Map<String,byte[]> getBase64UrlMapOfBytes();

        @GET("DateTimeRfc1123")
        @ReturnValueWireType(DateTimeRfc1123.class)
        OffsetDateTime getDateTimeRfc1123();

        @GET("UnixTime")
        @ReturnValueWireType(UnixTime.class)
        OffsetDateTime getDateTimeUnix();
    }

    @Test
    public void service1GetBase64UrlBytes10() {
        final byte[] bytes = createService(Service1.class)
                .getBase64UrlBytes10();
        assertNotNull(bytes);
        assertEquals(10, bytes.length);
        for (int i = 0; i < 10; ++i) {
            assertEquals((byte)i, bytes[i]);
        }
    }

    @Test
    public void service1GetBase64UrlListOfBytes() {
        final List<byte[]> bytesList = createService(Service1.class)
                .getBase64UrlListOfBytes();
        assertNotNull(bytesList);
        assertEquals(3, bytesList.size());

        for (int i = 0; i < bytesList.size(); ++i) {
            final byte[] bytes = bytesList.get(i);
            assertNotNull(bytes);
            assertEquals((i + 1) * 10, bytes.length);
            for (int j = 0; j < bytes.length; ++j) {
                assertEquals((byte)j, bytes[j]);
            }
        }
    }

    @Test
    public void service1GetBase64UrlListOfListOfBytes() {
        final List<List<byte[]>> bytesList = createService(Service1.class)
                .getBase64UrlListOfListOfBytes();
        assertNotNull(bytesList);
        assertEquals(2, bytesList.size());

        for (int i = 0; i < bytesList.size(); ++i) {
            final List<byte[]> innerList = bytesList.get(i);
            assertEquals((i + 1) * 2, innerList.size());

            for (int j = 0; j < innerList.size(); ++j) {
                final byte[] bytes = innerList.get(j);
                assertNotNull(bytes);
                assertEquals((j + 1) * 5, bytes.length);
                for (int k = 0; k < bytes.length; ++k) {
                    assertEquals(k, bytes[k]);
                }
            }
        }
    }

    @Test
    public void service1GetBase64UrlMapOfBytes() {
        final Map<String,byte[]> bytesMap = createService(Service1.class)
                .getBase64UrlMapOfBytes();
        assertNotNull(bytesMap);
        assertEquals(2, bytesMap.size());

        for (int i = 0; i < bytesMap.size(); ++i) {
            final byte[] bytes = bytesMap.get(Integer.toString(i));

            final int expectedArrayLength = (i + 1) * 10;
            assertEquals(expectedArrayLength, bytes.length);
            for (int j = 0; j < expectedArrayLength; ++j) {
                assertEquals((byte)j, bytes[j]);
            }
        }
    }

    @Test
    public void service1GetDateTimeRfc1123() {
        final OffsetDateTime dateTime = createService(Service1.class)
                .getDateTimeRfc1123();
        assertNotNull(dateTime);
        assertEquals(OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC), dateTime);
    }

    @Test
    public void service1GetDateTimeUnix() {
        final OffsetDateTime dateTime = createService(Service1.class)
                .getDateTimeUnix();
        assertNotNull(dateTime);
        assertEquals(OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC), dateTime);
    }


    @Host("http://httpbin.org")
    interface ServiceErrorWithCharsetService {
        @GET("/get")
        @ExpectedResponses({400})
        HttpBinJSON get();
    }

    @Test
    public void ServiceErrorWithResponseContentType() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                HttpPipeline.build(new HttpClient() {
                    @Override
                    public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json");

                        HttpResponse response = new MockHttpResponse(200, headers,
                                "{ \"error\": \"Something went wrong, but at least this JSON is valid.\"}".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (RuntimeException ex) {
            assertEquals(ex.getMessage(), "Status code 200, \"{ \"error\": \"Something went wrong, but at least this JSON is valid.\"}\"");
        }
    }

    @Test
    public void ServiceErrorWithResponseContentTypeBadJSON() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                HttpPipeline.build(new HttpClient() {
                    @Override
                    public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json");

                        HttpResponse response = new MockHttpResponse(200, headers, "BAD JSON".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (RestException ex) {
            assertContains(ex.getMessage(), "Status code 200");
            assertContains(ex.getMessage(), "\"BAD JSON\"");
        }
    }

    @Test
    public void ServiceErrorWithResponseContentTypeCharset() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                HttpPipeline.build(new HttpClient() {
                    @Override
                    public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json; charset=UTF-8");

                        HttpResponse response = new MockHttpResponse(200, headers,
                                "{ \"error\": \"Something went wrong, but at least this JSON is valid.\"}".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (RuntimeException ex) {
            assertEquals(ex.getMessage(), "Status code 200, \"{ \"error\": \"Something went wrong, but at least this JSON is valid.\"}\"");
        }
    }

    @Test
    public void ServiceErrorWithResponseContentTypeCharsetBadJSON() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                HttpPipeline.build(new HttpClient() {
                    @Override
                    public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json; charset=UTF-8");

                        HttpResponse response = new MockHttpResponse(200, headers, "BAD JSON".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (RestException ex) {
            assertContains(ex.getMessage(), "Status code 200");
            assertContains(ex.getMessage(), "\"BAD JSON\"");
        }
    }

    private static class HeaderCollectionTypePublicFields {
        public String name;

        @HeaderCollection("header-collection-prefix-")
        public Map<String,String> headerCollection;
    }

    private static class HeaderCollectionTypeProtectedFields {
        protected String name;

        @HeaderCollection("header-collection-prefix-")
        protected Map<String,String> headerCollection;
    }

    private static class HeaderCollectionTypePrivateFields {
        private String name;

        @HeaderCollection("header-collection-prefix-")
        private Map<String,String> headerCollection;
    }

    private static class HeaderCollectionTypePackagePrivateFields {
        String name;

        @HeaderCollection("header-collection-prefix-")
        Map<String,String> headerCollection;
    }

    @Host("https://www.example.com")
    interface ServiceHeaderCollections {
        @GET("url/path")
        RestResponse<HeaderCollectionTypePublicFields,Void> publicFields();

        @GET("url/path")
        RestResponse<HeaderCollectionTypeProtectedFields,Void> protectedFields();

        @GET("url/path")
        RestResponse<HeaderCollectionTypePrivateFields,Void> privateFields();

        @GET("url/path")
        RestResponse<HeaderCollectionTypePackagePrivateFields,Void> packagePrivateFields();
    }

    private static final HttpClient headerCollectionHttpClient = new MockHttpClient() {
        @Override
        public Mono<HttpResponse> sendRequestAsync(HttpRequest request) {
            final HttpHeaders headers = new HttpHeaders();
            headers.set("name", "Phillip");
            headers.set("header-collection-prefix-one", "1");
            headers.set("header-collection-prefix-two", "2");
            headers.set("header-collection-prefix-three", "3");
            final MockHttpResponse response = new MockHttpResponse(200, headers);
            return Mono.<HttpResponse>just(response);
        }
    };

    private ServiceHeaderCollections createHeaderCollectionsService() {
        return createService(ServiceHeaderCollections.class, headerCollectionHttpClient);
    }

    private static void assertHeaderCollectionsRawHeaders(RestResponse<?,Void> response) {
        final HttpHeaders responseRawHeaders = new HttpHeaders(response.rawHeaders());
        assertEquals("Phillip", responseRawHeaders.value("name"));
        assertEquals("1", responseRawHeaders.value("header-collection-prefix-one"));
        assertEquals("2", responseRawHeaders.value("header-collection-prefix-two"));
        assertEquals("3", responseRawHeaders.value("header-collection-prefix-three"));
        assertEquals(4, responseRawHeaders.size());
    }

    private static void assertHeaderCollections(Map<String,String> headerCollections) {
        final Map<String,String> expectedHeaderCollections = new HashMap<>();
        expectedHeaderCollections.put("one", "1");
        expectedHeaderCollections.put("two", "2");
        expectedHeaderCollections.put("three", "3");

        for (final String key : headerCollections.keySet()) {
            assertEquals(expectedHeaderCollections.get(key), headerCollections.get(key));
        }
        assertEquals(expectedHeaderCollections.size(), headerCollections.size());
    }

    @Test
    public void serviceHeaderCollectionPublicFields() {
        final RestResponse<HeaderCollectionTypePublicFields,Void> response = createHeaderCollectionsService()
            .publicFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePublicFields responseHeaders = response.headers();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    @Test
    public void serviceHeaderCollectionProtectedFields() {
        final RestResponse<HeaderCollectionTypeProtectedFields,Void> response = createHeaderCollectionsService()
            .protectedFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypeProtectedFields responseHeaders = response.headers();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    @Test
    public void serviceHeaderCollectionPrivateFields() {
        final RestResponse<HeaderCollectionTypePrivateFields,Void> response = createHeaderCollectionsService()
            .privateFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePrivateFields responseHeaders = response.headers();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    @Test
    public void serviceHeaderCollectionPackagePrivateFields() {
        final RestResponse<HeaderCollectionTypePackagePrivateFields,Void> response = createHeaderCollectionsService()
            .packagePrivateFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePackagePrivateFields responseHeaders = response.headers();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }
}

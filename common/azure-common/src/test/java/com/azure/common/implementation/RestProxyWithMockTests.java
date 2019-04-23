// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation;

import com.azure.common.annotations.BodyParam;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.GET;
import com.azure.common.annotations.HeaderCollection;
import com.azure.common.annotations.Host;
import com.azure.common.annotations.POST;
import com.azure.common.annotations.ReturnValueWireType;
import com.azure.common.entities.HttpBinJSON;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.MockHttpClient;
import com.azure.common.http.MockHttpResponse;
import com.azure.common.http.ProxyOptions;
import com.azure.common.http.rest.Page;
import com.azure.common.http.rest.PagedResponse;
import com.azure.common.http.rest.Response;
import com.azure.common.http.rest.ResponseBase;
import com.azure.common.implementation.http.ContentType;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        Map<String, byte[]> getBase64UrlMapOfBytes();

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
            assertEquals((byte) i, bytes[i]);
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
                assertEquals((byte) j, bytes[j]);
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
        final Map<String, byte[]> bytesMap = createService(Service1.class)
                .getBase64UrlMapOfBytes();
        assertNotNull(bytesMap);
        assertEquals(2, bytesMap.size());

        for (int i = 0; i < bytesMap.size(); ++i) {
            final byte[] bytes = bytesMap.get(Integer.toString(i));

            final int expectedArrayLength = (i + 1) * 10;
            assertEquals(expectedArrayLength, bytes.length);
            for (int j = 0; j < expectedArrayLength; ++j) {
                assertEquals((byte) j, bytes[j]);
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
    public void serviceErrorWithResponseContentType() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                new HttpPipeline(new SimpleMockHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json");

                        HttpResponse response = new MockHttpResponse(request, 200, headers,
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
    public void serviceErrorWithResponseContentTypeBadJSON() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                new HttpPipeline(new SimpleMockHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json");

                        HttpResponse response = new MockHttpResponse(request, 200, headers, "BAD JSON".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (ServiceRequestException ex) {
            assertContains(ex.getMessage(), "Status code 200");
            assertContains(ex.getMessage(), "\"BAD JSON\"");
        }
    }

    @Test
    public void serviceErrorWithResponseContentTypeCharset() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                new HttpPipeline(new SimpleMockHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json; charset=UTF-8");

                        HttpResponse response = new MockHttpResponse(request, 200, headers,
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
    public void serviceErrorWithResponseContentTypeCharsetBadJSON() {
        ServiceErrorWithCharsetService service = RestProxy.create(
                ServiceErrorWithCharsetService.class,
                new HttpPipeline(new SimpleMockHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json; charset=UTF-8");

                        HttpResponse response = new MockHttpResponse(request, 200, headers, "BAD JSON".getBytes(StandardCharsets.UTF_8));
                        return Mono.just(response);
                    }
                }));

        try {
            service.get();
            fail();
        } catch (ServiceRequestException ex) {
            assertContains(ex.getMessage(), "Status code 200");
            assertContains(ex.getMessage(), "\"BAD JSON\"");
        }
    }

    private static class HeaderCollectionTypePublicFields {
        @JsonProperty()
        private String name;

        @HeaderCollection("header-collection-prefix-")
        private Map<String, String> headerCollection;

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }

        public Map<String, String> headerCollection() {
            return headerCollection;
        }

        public void headerCollection(Map<String, String> headerCollection) {
            this.headerCollection = headerCollection;
        }
    }

    private static class HeaderCollectionTypeProtectedFields {
        protected String name;

        @HeaderCollection("header-collection-prefix-")
        protected Map<String, String> headerCollection;
    }

    private static class HeaderCollectionTypePrivateFields {
        private String name;

        @HeaderCollection("header-collection-prefix-")
        private Map<String, String> headerCollection;
    }

    private static class HeaderCollectionTypePackagePrivateFields {
        String name;

        @HeaderCollection("header-collection-prefix-")
        Map<String, String> headerCollection;
    }

    @Host("https://www.example.com")
    interface ServiceHeaderCollections {
        @GET("url/path")
        ResponseBase<HeaderCollectionTypePublicFields, Void> publicFields();

        @GET("url/path")
        ResponseBase<HeaderCollectionTypeProtectedFields, Void> protectedFields();

        @GET("url/path")
        ResponseBase<HeaderCollectionTypePrivateFields, Void> privateFields();

        @GET("url/path")
        ResponseBase<HeaderCollectionTypePackagePrivateFields, Void> packagePrivateFields();
    }

    private static final HttpClient HEADER_COLLECTION_HTTP_CLIENT = new MockHttpClient() {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            final HttpHeaders headers = new HttpHeaders();
            headers.set("name", "Phillip");
            headers.set("header-collection-prefix-one", "1");
            headers.set("header-collection-prefix-two", "2");
            headers.set("header-collection-prefix-three", "3");
            final MockHttpResponse response = new MockHttpResponse(request, 200, headers);
            return Mono.<HttpResponse>just(response);
        }
    };

    private ServiceHeaderCollections createHeaderCollectionsService() {
        return createService(ServiceHeaderCollections.class, HEADER_COLLECTION_HTTP_CLIENT);
    }

    private static void assertHeaderCollectionsRawHeaders(Response<Void> response) {
        final HttpHeaders responseRawHeaders = response.headers();
        assertEquals("Phillip", responseRawHeaders.value("name"));
        assertEquals("1", responseRawHeaders.value("header-collection-prefix-one"));
        assertEquals("2", responseRawHeaders.value("header-collection-prefix-two"));
        assertEquals("3", responseRawHeaders.value("header-collection-prefix-three"));
        assertEquals(4, responseRawHeaders.size());
    }

    private static void assertHeaderCollections(Map<String, String> headerCollections) {
        final Map<String, String> expectedHeaderCollections = new HashMap<>();
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
        final ResponseBase<HeaderCollectionTypePublicFields, Void> response = createHeaderCollectionsService()
            .publicFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePublicFields responseHeaders = response.deserializedHeaders();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name());
        assertHeaderCollections(responseHeaders.headerCollection());
    }

    @Test
    public void serviceHeaderCollectionProtectedFields() {
        final ResponseBase<HeaderCollectionTypeProtectedFields, Void> response = createHeaderCollectionsService()
            .protectedFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypeProtectedFields responseHeaders = response.deserializedHeaders();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    @Test
    public void serviceHeaderCollectionPrivateFields() {
        final ResponseBase<HeaderCollectionTypePrivateFields, Void> response = createHeaderCollectionsService()
            .privateFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePrivateFields responseHeaders = response.deserializedHeaders();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    @Test
    public void serviceHeaderCollectionPackagePrivateFields() {
        final ResponseBase<HeaderCollectionTypePackagePrivateFields, Void> response = createHeaderCollectionsService()
            .packagePrivateFields();
        assertNotNull(response);
        assertHeaderCollectionsRawHeaders(response);

        final HeaderCollectionTypePackagePrivateFields responseHeaders = response.deserializedHeaders();
        assertNotNull(responseHeaders);
        assertEquals("Phillip", responseHeaders.name);
        assertHeaderCollections(responseHeaders.headerCollection);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }

    private abstract static class SimpleMockHttpClient implements HttpClient {

        @Override
        public abstract Mono<HttpResponse> send(HttpRequest request);

        @Override
        public HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
            throw new IllegalStateException("MockHttpClient.proxy not implemented.");
        }

        @Override
        public HttpClient wiretap(boolean enableWiretap) {
            throw new IllegalStateException("MockHttpClient.wiretap not implemented.");
        }

        @Override
        public HttpClient port(int port) {
            throw new IllegalStateException("MockHttpClient.port not implemented.");
        }
    }


    static class KeyValue {
        @JsonProperty("key")
        private int key;

        @JsonProperty("value")
        private String value;

        KeyValue() { }
        KeyValue(int key, String value) {
            this.key = key;
            this.value = value;
        }

        int key() {
            return this.key;
        }

        String value() {
            return this.value;
        }
    }

    static class KeyValuePage implements Page<KeyValue> {
        @JsonProperty()
        private List<KeyValue> items;

        @JsonProperty("nextLink")
        private String nextLink;

        KeyValuePage() {
        }

        KeyValuePage(List<KeyValue> items, String nextLink) {
            this.items = items;
            this.nextLink = nextLink;
        }

        @Override
        public List<KeyValue> items() {
            return items;
        }

        @Override
        public String nextLink() {
            return nextLink;
        }
    }

    static class ConformingPage<T> implements Page<T> {
        private List<T> items;
        private String nextLink;

        ConformingPage(List<T> items, String nextLink) {
            this.items = items;
            this.nextLink = nextLink;
        }

        @Override
        public List<T> items() {
            return items;
        }

        @Override
        public String nextLink() {
            return nextLink;
        }
    }

    /*
     * Non-conforming page because it does not implement the Page interface and instead of a Page.items(), has
     * badItems(), which would result in different JSON.
     */
    static class NonComformingPage<T> {
        private List<T> badItems;
        private String nextLink;

        NonComformingPage(List<T> items, String nextLink) {
            this.badItems = items;
            this.nextLink = nextLink;
        }

        @JsonGetter()
        public List<T> badItems() {
            return badItems;
        }

        public String nextLink() {
            return nextLink;
        }
    }

    @Host("http://echo.org")
    interface Service2 {
        @POST("anything/json")
        @ExpectedResponses({200})
        @ReturnValueWireType(KeyValuePage.class)
        PagedResponse<KeyValue> getPage(@BodyParam(ContentType.APPLICATION_JSON) Page<KeyValue> values);

        @POST("anything/json")
        @ExpectedResponses({200})
        @ReturnValueWireType(Page.class)
        Mono<PagedResponse<KeyValue>> getPageAsync(@BodyParam(ContentType.APPLICATION_JSON) Page<KeyValue> values);

        @POST("anything/json")
        @ExpectedResponses({200})
        @ReturnValueWireType(Page.class)
        Mono<PagedResponse<KeyValue>> getPageAsyncSerializes(@BodyParam(ContentType.APPLICATION_JSON) NonComformingPage<KeyValue> values);
    }

    /**
     * Verifies that we can get a PagedResponse<T> when the user has implemented their own class from {@link Page}.
     */
    @Test
    public void service2getPage() {
        List<KeyValue> array = new ArrayList<>();
        KeyValue key1 = new KeyValue(1, "Foo");
        KeyValue key2 = new KeyValue(2, "Bar");
        KeyValue key3 = new KeyValue(10, "Baz");

        array.add(key1);
        array.add(key2);
        array.add(key3);
        KeyValuePage page = new KeyValuePage(array, "SomeNextLink");

        PagedResponse<KeyValue> response = createService(Service2.class).getPage(page);
        assertNotNull(response);
        assertEquals(array.size(), response.value().size());
    }

    /**
     * Verifies that if we pass in a {@link ReturnValueWireType} of {@link Page}, the service can return a
     * representation of that data.
     */
    @Test
    public void service2getPageAsync() {
        List<KeyValue> array = new ArrayList<>();
        KeyValue key1 = new KeyValue(1, "Foo");
        KeyValue key2 = new KeyValue(2, "Bar");
        KeyValue key3 = new KeyValue(10, "Baz");

        array.add(key1);
        array.add(key2);
        array.add(key3);
        ConformingPage<KeyValue> page = new ConformingPage<>(array, "MyNextLink");

        StepVerifier.create(createService(Service2.class).getPageAsync(page))
            .assertNext(r -> {
                assertEquals(page.nextLink, r.nextLink());

                assertEquals(r.items().size(), 3);
                for (KeyValue keyValue : r.value()) {
                    assertTrue(array.removeIf(kv -> kv.key == keyValue.key && kv.value().equals(keyValue.value())));
                }
                assertTrue(array.isEmpty());
            })
            .verifyComplete();
    }

    /*
     * Verifies that even though our HTTP response does not conform to the Page<T> interface, the service does not throw
     * an exception and returns a response.
     * This is a scenario where our developer has set @ReturnValueWireType(Page.class), but their service returns a JSON
     * object that does not conform to Page<T> interface.
     */
    @Test
    public void service2getPageSerializes() {
        List<KeyValue> array = new ArrayList<>();
        KeyValue key1 = new KeyValue(1, "Foo");
        KeyValue key2 = new KeyValue(2, "Bar");
        KeyValue key3 = new KeyValue(10, "Baz");

        array.add(key1);
        array.add(key2);
        array.add(key3);
        NonComformingPage<KeyValue> page = new NonComformingPage<>(array, "A next link!");

        StepVerifier.create(createService(Service2.class).getPageAsyncSerializes(page))
            .assertNext(response -> {
                assertEquals(page.nextLink(), response.nextLink());
                assertNull(response.items());
            })
            .verifyComplete();
    }

}

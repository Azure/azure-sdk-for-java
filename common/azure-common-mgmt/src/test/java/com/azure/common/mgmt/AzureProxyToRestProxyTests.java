// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

import com.azure.common.annotations.BodyParam;
import com.azure.common.annotations.DELETE;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.GET;
import com.azure.common.annotations.HEAD;
import com.azure.common.annotations.HeaderParam;
import com.azure.common.annotations.Headers;
import com.azure.common.annotations.Host;
import com.azure.common.annotations.HostParam;
import com.azure.common.annotations.PATCH;
import com.azure.common.annotations.POST;
import com.azure.common.annotations.PUT;
import com.azure.common.annotations.PathParam;
import com.azure.common.annotations.QueryParam;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.rest.ResponseBase;
import com.azure.common.implementation.exception.InvalidReturnTypeException;
import com.azure.common.implementation.http.ContentType;
import com.azure.common.implementation.serializer.SerializerAdapter;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class AzureProxyToRestProxyTests {
    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    @Host("http://httpbin.org")
    private interface Service1 {
        @GET("bytes/100")
        @ExpectedResponses({200})
        byte[] getByteArray();

        @GET("bytes/100")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync();

        @GET("bytes/100")
        Mono<byte[]> getByteArrayAsyncWithNoExpectedResponses();
    }

    @Test
    public void syncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class)
                .getByteArray();
        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    @Test
    public void asyncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class)
                .getByteArrayAsync()
                .block();
        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    @Test
    public void getByteArrayAsyncWithNoExpectedResponses() {
        final byte[] result = createService(Service1.class)
                .getByteArrayAsyncWithNoExpectedResponses()
                .block();
        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    @Host("http://{hostName}.org")
    private interface Service2 {
        @GET("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("hostName") String host, @PathParam("numberOfBytes") int numberOfBytes);

        @GET("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync(@HostParam("hostName") String host, @PathParam("numberOfBytes") int numberOfBytes);
    }

    @Test
    public void syncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArray("httpbin", 50);
        assertNotNull(result);
        assertEquals(result.length, 50);
    }

    @Test
    public void asyncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArrayAsync("httpbin", 50)
                .block();
        assertNotNull(result);
        assertEquals(result.length, 50);
    }

    @Host("http://httpbin.org")
    private interface Service3 {
        @GET("bytes/2")
        @ExpectedResponses({200})
        void getNothing();

        @GET("bytes/2")
        @ExpectedResponses({200})
        Mono<Void> getNothingAsync();
    }

    @Test
    public void syncGetRequestWithNoReturn() {
        createService(Service3.class).getNothing();
    }

    @Test
    public void asyncGetRequestWithNoReturn() {
        createService(Service3.class)
                .getNothingAsync()
                .block();
    }

    @Host("http://httpbin.org")
    private interface Service5 {
        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything();

        @GET("anything/with+plus")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPlus();

        @GET("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPathParam(@PathParam("path") String pathParam);

        @GET("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncodedPathParam(@PathParam(value = "path", encoded = true) String pathParam);

        @GET("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync();
    }

    @Test
    public void syncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnything();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPlus();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+plus", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("withpathparam");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with path param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with+path+param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+path+param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("withpathparam");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with%20path%20param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with+path+param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+path+param", json.url());
    }

    @Test
    public void asyncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
    }

    @Host("http://httpbin.org")
    private interface Service6 {
        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@QueryParam("a") String a, @QueryParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncoded(@QueryParam(value = "a", encoded = true) String a, @QueryParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@QueryParam("a") String a, @QueryParam("b") int b);
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A%20Z", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A%2520Z&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingWithEncoded("x%20y", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=x y&b=15", json.url());
    }

    @Test
    public void asyncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingAsync("A", 15)
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A&b=15", json.url());
    }

    @Host("http://httpbin.org")
    private interface Service7 {
        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HeaderParam("a") String a, @HeaderParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@HeaderParam("a") String a, @HeaderParam("b") int b);
    }

    @Test
    public void syncGetRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders(json.headers());
        assertEquals("A", headers.value("A"));
        assertArrayEquals(new String[]{"A"}, headers.values("A"));
        assertEquals("15", headers.value("B"));
        assertArrayEquals(new String[]{"15"}, headers.values("B"));
    }

    @Test
    public void asyncGetRequestWithHeaderParametersAndAnything() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnythingAsync("A", 15)
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders(json.headers());
        assertEquals("A", headers.value("A"));
        assertArrayEquals(new String[]{"A"}, headers.values("A"));
        assertEquals("15", headers.value("B"));
        assertArrayEquals(new String[]{"15"}, headers.values("B"));
    }

    @Host("http://httpbin.org")
    private interface Service8 {
        @POST("post")
        @ExpectedResponses({200})
        HttpBinJSON post(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);

        @POST("post")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> postAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);
    }

    @Test
    public void syncPostRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class)
                .post("I'm a post body!");
        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Test
    public void asyncPostRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class)
                .postAsync("I'm a post body!")
                .block();
        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Host("http://httpbin.org")
    private interface Service9 {
        @PUT("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @PUT("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @PUT("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyAzureException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    @Test
    public void syncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class)
                .put(42);
        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    @Test
    public void asyncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class)
                .putAsync(42)
                .block();
        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    @Test
    public void syncPutRequestWithUnexpectedResponse() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponse("I'm the body!");
            fail("Expected RestException would be thrown.");
        } catch (ServiceRequestException e) {
            assertNotNull(e.value());
            assertTrue(e.value() instanceof LinkedHashMap);

            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.value();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAndExceptionType("I'm the body!");
            fail("Expected RestException would be thrown.");
        } catch (MyAzureException e) {
            assertNotNull(e.value());
            assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Throwable of wrong type thrown.");
        }
    }

    @Host("http://httpbin.org")
    private interface Service10 {
        @HEAD("anything")
        @ExpectedResponses({200})
        ResponseBase<Void, Void> restResponseHead();


        @HEAD("anything")
        @ExpectedResponses({200})
        void voidHead();

        @HEAD("anything")
        @ExpectedResponses({200})
        Mono<ResponseBase<Void, Void>> restResponseHeadAsync();

        @HEAD("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync();
    }

    @Test
    public void syncRestResponseHeadRequest() {
        ResponseBase<?, ?> res = createService(Service10.class)
                .restResponseHead();
        assertNull(res.value());
    }

    @Test
    public void syncVoidHeadRequest() {
        createService(Service10.class)
                .voidHead();
    }

    @Test
    public void asyncRestResponseHeadRequest() {
        ResponseBase<?, ?> res = createService(Service10.class)
                .restResponseHeadAsync()
                .block();

        assertNull(res.value());
    }

    @Test
    public void asyncCompletableHeadRequest() {
        createService(Service10.class)
                .completableHeadAsync()
                .block();
    }

    @Host("http://httpbin.org")
    private interface Service11 {
        @DELETE("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@BodyParam(ContentType.APPLICATION_JSON) boolean bodyBoolean);

        @DELETE("delete")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> deleteAsync(@BodyParam(ContentType.APPLICATION_JSON) boolean bodyBoolean);
    }

    @Test
    public void syncDeleteRequest() {
        final HttpBinJSON json = createService(Service11.class)
                .delete(false);
        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Test
    public void asyncDeleteRequest() {
        final HttpBinJSON json = createService(Service11.class)
                .deleteAsync(false)
                .block();
        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Host("http://httpbin.org")
    private interface Service12 {
        @PATCH("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        @PATCH("patch")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> patchAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);
    }

    @Test
    public void syncPatchRequest() {
        final HttpBinJSON json = createService(Service12.class)
                .patch("body-contents");
        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    @Test
    public void asyncPatchRequest() {
        final HttpBinJSON json = createService(Service12.class)
                .patchAsync("body-contents")
                .block();
        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    @Host("http://httpbin.org")
    private interface Service13 {
        @GET("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value" })
        HttpBinJSON get();

        @GET("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value" })
        Mono<HttpBinJSON> getAsync();
    }

    @Test
    public void syncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class)
                .get();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders(json.headers());
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.values("MyHeader"));
        assertEquals("My,Header,Value", headers.value("MyOtherHeader"));
        assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.values("MyOtherHeader"));
    }

    @Test
    public void asyncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class)
                .getAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders(json.headers());
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.values("MyHeader"));
    }

    @Host("https://httpbin.org")
    private interface Service14 {
        @GET("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue" })
        HttpBinJSON get();

        @GET("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue" })
        Mono<HttpBinJSON> getAsync();
    }

    @Test
    public void asyncHttpsHeadersRequest() {
        final HttpBinJSON json = createService(Service14.class)
                .getAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders(json.headers());
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
    }

    @Host("https://httpbin.org")
    private interface Service15 {
        @GET("anything")
        @ExpectedResponses({200})
        Flux<HttpBinJSON> get();
    }

    @Test
    public void service15Get() {
        final Service15 service = createService(Service15.class);
        try {
            service.get();
            fail("Expected exception.");
        } catch (InvalidReturnTypeException e) {
            assertContains(e.getMessage(), "reactor.core.publisher.Flux<com.azure.common.mgmt.HttpBinJSON>");
            assertContains(e.getMessage(), "AzureProxyToRestProxyTests$Service15.get()");
        }
    }

    @Host("http://httpbin.org")
    private interface Service16 {
        @PUT("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] putBody);

        @PUT("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] putBody);
    }

    @Test
    public void service16Put() {
        final Service16 service = createService(Service16.class);
        final HttpBinJSON result = service.put(new byte[] { 0, 1, 2, 3, 4, 5 });
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/put", result.url());
        assertTrue(result.data() instanceof String);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5 }, ((String) result.data()).getBytes());
    }

    @Test
    public void service16PutAsync() {
        final Service16 service = createService(Service16.class);
        final HttpBinJSON result = service.putAsync(new byte[] { 0, 1, 2, 3, 4, 5 })
                .block();
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/put", result.url());
        assertTrue(result.data() instanceof String);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5 }, ((String) result.data()).getBytes());
    }

    @Host("http://{hostPart1}{hostPart2}.org")
    private interface Service17 {
        @GET("get")
        @ExpectedResponses({200})
        HttpBinJSON get(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);

        @GET("get")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAsync(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);
    }

    @Test
    public void syncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.get("http", "bin");
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/get", result.url());
    }

    @Test
    public void asyncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.getAsync("http", "bin").block();
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/get", result.url());
    }

    @Host("https://httpbin.org")
    private interface Service18 {
        @GET("status/200")
        void getStatus200();

        @GET("status/200")
        @ExpectedResponses({200})
        void getStatus200WithExpectedResponse200();

        @GET("status/300")
        void getStatus300();

        @GET("status/300")
        @ExpectedResponses({300})
        void getStatus300WithExpectedResponse300();

        @GET("status/400")
        void getStatus400();

        @GET("status/400")
        @ExpectedResponses({400})
        void getStatus400WithExpectedResponse400();

        @GET("status/500")
        void getStatus500();

        @GET("status/500")
        @ExpectedResponses({500})
        void getStatus500WithExpectedResponse500();
    }

    @Test
    public void service18GetStatus200() {
        createService(Service18.class)
                .getStatus200();
    }

    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        createService(Service18.class)
                .getStatus200WithExpectedResponse200();
    }

    @Test
    public void service18GetStatus300() {
        createService(Service18.class)
                .getStatus300();
    }

    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        createService(Service18.class)
                .getStatus300WithExpectedResponse300();
    }

    @Test(expected = ServiceRequestException.class)
    public void service18GetStatus400() {
        createService(Service18.class)
                .getStatus400();
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        createService(Service18.class)
                .getStatus400WithExpectedResponse400();
    }

    @Test(expected = ServiceRequestException.class)
    public void service18GetStatus500() {
        createService(Service18.class)
                .getStatus500();
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        createService(Service18.class)
                .getStatus500WithExpectedResponse500();
    }

    private <T> T createService(Class<T> serviceClass) {
        HttpPipeline pipeline = new HttpPipeline(createHttpClient());
        //
        return AzureProxy.create(serviceClass, null, pipeline, SERIALIZER);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }

    private static void assertMatchWithHttpOrHttps(String url1, String url2) {
        final String s1 = "http://" + url1;
        if (s1.equalsIgnoreCase(url2)) {
            return;
        }
        final String s2 = "https://" + url1;
        if (s2.equalsIgnoreCase(url2)) {
            return;
        }
        Assert.assertTrue("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'.", false);
    }

    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
}

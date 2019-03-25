package com.azure.common.implementation;

import com.azure.common.MyRestException;
import com.azure.common.http.rest.RestException;
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
import com.azure.common.entities.HttpBinHeaders;
import com.azure.common.entities.HttpBinJSON;
import com.azure.common.implementation.http.ContentType;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpLoggingPolicy;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.http.rest.RestResponseBase;
import com.azure.common.http.rest.RestStreamResponse;
import com.azure.common.http.rest.RestVoidResponse;
import com.azure.common.implementation.serializer.SerializerAdapter;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;
import com.azure.common.implementation.util.FluxUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public abstract class RestProxyTests {

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
    public void SyncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class)
                .getByteArray();
        assertNotNull(result);
        assertEquals(100, result.length);
    }

    @Test
    public void AsyncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class)
                .getByteArrayAsync()
                .block();
        assertNotNull(result);
        assertEquals(100, result.length);
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
    public void SyncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArray("httpbin", 50);
        assertNotNull(result);
        assertEquals(result.length, 50);
    }

    @Test
    public void AsyncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArrayAsync("httpbin", 50)
                .block();
        assertNotNull(result);
        assertEquals(result.length, 50);
    }

    @Test
    public void SyncRequestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArray("httpbin", 0);
        // If no body then for async returns Mono.empty() for sync return null.
        assertNull(result);
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
    public void SyncGetRequestWithNoReturn() {
        createService(Service3.class).getNothing();
    }

    @Test
    public void AsyncGetRequestWithNoReturn() {
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
        HttpBinJSON getAnythingWithEncodedPathParam(@PathParam(value="path", encoded=true) String pathParam);

        @GET("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync();
    }

    @Test
    public void SyncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnything();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPlus();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+plus", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("withpathparam");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/withpathparam", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with path param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with path param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with+path+param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+path+param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("withpathparam");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/withpathparam", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with%20path%20param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with path param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with+path+param");
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything/with+path+param", json.url);
    }

    @Test
    public void AsyncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
    }

    @Host("http://httpbin.org")
    private interface Service6 {
        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@QueryParam("a") String a, @QueryParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncoded(@QueryParam(value="a", encoded=true) String a, @QueryParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@QueryParam("a") String a, @QueryParam("b") int b);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A%20Z", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A%2520Z&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingWithEncoded("x%20y", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=x y&b=15", json.url);
    }

    @Test
    public void AsyncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingAsync("A", 15)
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?a=A&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithNullQueryParameter() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything(null, 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?b=15", json.url);
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
    public void SyncGetRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
        assertEquals("A", headers.value("A"));
        assertArrayEquals(new String[]{"A"}, headers.values("A"));
        assertEquals("15", headers.value("B"));
        assertArrayEquals(new String[]{"15"}, headers.values("B"));
    }

    @Test
    public void AsyncGetRequestWithHeaderParametersAndAnything() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnythingAsync("A", 15)
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
        assertEquals("A", headers.value("A"));
        assertArrayEquals(new String[]{"A"}, headers.values("A"));
        assertEquals("15", headers.value("B"));
        assertArrayEquals(new String[]{"15"}, headers.values("B"));
    }

    @Test
    public void SyncGetRequestWithNullHeader() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnything(null, 15);

        final HttpHeaders headers = new HttpHeaders(json.headers);

        assertEquals(null, headers.value("A"));
        assertArrayEquals(null, headers.values("A"));
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
    public void SyncPostRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class)
                .post("I'm a post body!");
        assertEquals(String.class, json.data.getClass());
        assertEquals("I'm a post body!", (String)json.data);
    }

    @Test
    public void AsyncPostRequestWithStringBody() {
        final HttpBinJSON json = createService(Service8.class)
                .postAsync("I'm a post body!")
                .block();
        assertEquals(String.class, json.data.getClass());
        assertEquals("I'm a post body!", (String)json.data);
    }

    @Test
    public void SyncPostRequestWithNullBody() {
        final HttpBinJSON result = createService(Service8.class).post(null);
        assertEquals("", result.data);
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
        Mono<HttpBinJSON> putWithUnexpectedResponseAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    @Test
    public void SyncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class)
                .put(42);
        assertEquals(String.class, json.data.getClass());
        assertEquals("42", (String)json.data);
    }

    @Test
    public void AsyncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class)
                .putAsync(42)
                .block();
        assertEquals(String.class, json.data.getClass());
        assertEquals("42", (String)json.data);
    }

    @Test
    public void SyncPutRequestWithUnexpectedResponse() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponse("I'm the body!");
            fail("Expected RestException would be thrown.");
        } catch (RestException e) {
            assertNotNull(e.body());
            assertTrue(e.body() instanceof LinkedHashMap);

            final LinkedHashMap<String,String> expectedBody = (LinkedHashMap<String, String>)e.body();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void AsyncPutRequestWithUnexpectedResponse() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAsync("I'm the body!")
                    .block();
            fail("Expected RestException would be thrown.");
        } catch (RestException e) {
            assertNotNull(e.body());
            assertTrue(e.body() instanceof LinkedHashMap);

            final LinkedHashMap<String,String> expectedBody = (LinkedHashMap<String, String>)e.body();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void SyncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAndExceptionType("I'm the body!");
            fail("Expected RestException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.body());
            Assert.assertEquals("I'm the body!", e.body().data);
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void AsyncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAndExceptionTypeAsync("I'm the body!")
                    .block();
            fail("Expected RestException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.body());
            Assert.assertEquals("I'm the body!", e.body().data);
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Host("http://httpbin.org")
    private interface Service10 {
        @HEAD("anything")
        @ExpectedResponses({200})
        RestVoidResponse head();

        @HEAD("anything")
        @ExpectedResponses({200})
        boolean headBoolean();

        @HEAD("anything")
        @ExpectedResponses({200})
        void voidHead();

        @HEAD("anything")
        @ExpectedResponses({200})
        Mono<RestVoidResponse> headAsync();

        @HEAD("anything")
        @ExpectedResponses({200})
        Mono<Boolean> headBooleanAsync();

        @HEAD("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync();
    }

    @Test
    public void SyncHeadRequest() {
        final Void body = createService(Service10.class)
                .head()
                .body();
        assertNull(body);
    }

    @Test
    public void SyncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBoolean();
        assertTrue(result);
    }

    @Test
    public void SyncVoidHeadRequest() {
        createService(Service10.class)
                .voidHead();
    }

    @Test
    public void AsyncHeadRequest() {
        final Void body = createService(Service10.class)
                .headAsync()
                .block()
                .body();

        assertNull(body);
    }

    @Test
    public void AsyncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBooleanAsync().block();
        assertTrue(result);
    }

    @Test
    public void AsyncCompletableHeadRequest() {
        createService(Service10.class)
                .completableHeadAsync()
                .block();
    }

    @Host("http://httpbin.org")
    private interface Service11 {
        @DELETE("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

        @DELETE("delete")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> deleteAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);
    }

    @Test
    public void SyncDeleteRequest() {
        final HttpBinJSON json = createService(Service11.class)
                .delete(false);
        assertEquals(String.class, json.data.getClass());
        assertEquals("false", (String)json.data);
    }

    @Test
    public void AsyncDeleteRequest() {
        final HttpBinJSON json = createService(Service11.class)
                .deleteAsync(false)
                .block();
        assertEquals(String.class, json.data.getClass());
        assertEquals("false", (String)json.data);
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
    public void SyncPatchRequest() {
        final HttpBinJSON json = createService(Service12.class)
                .patch("body-contents");
        assertEquals(String.class, json.data.getClass());
        assertEquals("body-contents", (String)json.data);
    }

    @Test
    public void AsyncPatchRequest() {
        final HttpBinJSON json = createService(Service12.class)
                .patchAsync("body-contents")
                .block();
        assertEquals(String.class, json.data.getClass());
        assertEquals("body-contents", (String)json.data);
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
    public void SyncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class)
                .get();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.values("MyHeader"));
        assertEquals("My,Header,Value", headers.value("MyOtherHeader"));
        assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.values("MyOtherHeader"));
    }

    @Test
    public void AsyncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class)
                .getAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
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
    public void AsyncHttpsHeadersRequest() {
        final HttpBinJSON json = createService(Service14.class)
                .getAsync()
                .block();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
    }

    @Host("https://httpbin.org")
    private interface Service16 {
        @PUT("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @PUT("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putByteArrayAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);
    }

    @Test
    public void service16Put() throws Exception {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJSON httpBinJSON = service16.putByteArray(expectedBytes);

        // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        assertTrue(httpBinJSON.data instanceof String);

        final String base64String = (String) httpBinJSON.data;
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void service16PutAsync() throws Exception {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJSON httpBinJSON = service16.putByteArrayAsync(expectedBytes)
                .block();
        assertTrue(httpBinJSON.data instanceof String);

        final String base64String = (String) httpBinJSON.data;
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
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
    public void SyncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.get("http", "bin");
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/get", result.url);
    }

    @Test
    public void AsyncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.getAsync("http", "bin").block();
        assertNotNull(result);
        assertMatchWithHttpOrHttps("httpbin.org/get", result.url);
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

    @Test(expected = RestException.class)
    public void service18GetStatus400() {
        createService(Service18.class)
                .getStatus400();
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        createService(Service18.class)
                .getStatus400WithExpectedResponse400();
    }

    @Test(expected = RestException.class)
    public void service18GetStatus500() {
        createService(Service18.class)
                .getStatus500();
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        createService(Service18.class)
                .getStatus500WithExpectedResponse500();
    }

    @Host("http://httpbin.org")
    private interface Service19 {
        @PUT("put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @PUT("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @PUT("put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_JSON) String body);

        @PUT("put")
        @Headers({ "Content-Type: application/json" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @PUT("put")
        @Headers({ "Content-Type: application/json; charset=utf-8" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @PUT("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @PUT("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @PUT("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_JSON) String body);

        @PUT("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(@BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @PUT("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @PUT("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @PUT("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody("");
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody("hello");
        assertEquals("hello", result.data);
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody("");
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals("\"AAECAwQ=\"", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("");
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("soups and stuff");
        assertEquals("soups and stuff", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody("");
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody("penguins");
        assertEquals("penguins", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody("");
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody("");
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals("\"AAECAwQ=\"", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody("");
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody("penguins");
        assertEquals("penguins", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(null);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data);
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data);
    }

    @Host("http://httpbin.org")
    private interface Service20 {
        @GET("bytes/100")
        RestResponseBase<HttpBinHeaders,Void> getBytes100OnlyHeaders();

        @GET("bytes/100")
        RestResponseBase<HttpHeaders,Void> getBytes100OnlyRawHeaders();

        @GET("bytes/100")
        RestResponseBase<HttpBinHeaders,byte[]> getBytes100BodyAndHeaders();

        @PUT("put")
        RestResponseBase<HttpBinHeaders,Void> putOnlyHeaders(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @PUT("put")
        RestResponseBase<HttpBinHeaders,HttpBinJSON> putBodyAndHeaders(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @GET("bytes/100")
        RestResponseBase<Void, Void> getBytesOnlyStatus();

        @GET("bytes/100")
        RestVoidResponse getVoidRestResponse();

        @PUT("put")
        RestResponse<HttpBinJSON> putBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        final RestResponseBase<HttpBinHeaders,Void> response = createService(Service20.class)
                .getBytes100OnlyHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection.toLowerCase());
        assertNotNull(headers.date);
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final RestResponseBase<HttpBinHeaders,byte[]> response = createService(Service20.class)
                .getBytes100BodyAndHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final byte[] body = response.body();
        assertNotNull(body);
        assertEquals(100, body.length);

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertNotNull(headers.date);
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        final RestResponse<Void> response = createService(Service20.class)
                .getBytesOnlyStatus();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        final RestResponse<Void> response = createService(Service20.class)
                .getBytes100OnlyRawHeaders();

        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertNotNull(response.headers());
        assertNotEquals(0, response.headers().size());
    }

    @Test
    public void service20PutOnlyHeaders() {
        final RestResponseBase<HttpBinHeaders,Void> response = createService(Service20.class)
                .putOnlyHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection.toLowerCase());
        assertNotNull(headers.date);
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20PutBodyAndHeaders() {
        final RestResponseBase<HttpBinHeaders,HttpBinJSON> response = createService(Service20.class)
                .putBodyAndHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinJSON body = response.body();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("httpbin.org/put", body.url);
        assertEquals("body string", body.data);

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection.toLowerCase());
        assertNotNull(headers.date);
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20GetVoidRestResponse() {
        final RestVoidResponse response = createService(Service20.class).getVoidRestResponse();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void service20GetRestResponseBody() {
        final RestResponse<HttpBinJSON> response = createService(Service20.class).putBody("body string");
        assertNotNull(response);
        assertEquals(200, response.statusCode());

        final HttpBinJSON body = response.body();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("httpbin.org/put", body.url);
        assertEquals("body string", body.data);

        final HttpHeaders headers = response.headers();
        assertNotNull(headers);
    }

    @Host("http://httpbin.org")
    interface UnexpectedOKService {
        @GET("/bytes/1024")
        @ExpectedResponses({400})
        RestStreamResponse getBytes();
    }

    @Test
    public void UnexpectedHTTPOK() {
        try {
            createService(UnexpectedOKService.class).getBytes();
            fail();
        } catch (RestException e) {
            assertEquals("Status code 200, (1024-byte body)", e.getMessage());
        }
    }

    @Host("https://www.example.com")
    private interface Service21 {
        @GET("http://httpbin.org/bytes/100")
        @ExpectedResponses({200})
        byte[] getBytes100();
    }

    @Test
    public void service21GetBytes100() {
        final byte[] bytes = createService(Service21.class)
            .getBytes100();
        assertNotNull(bytes);
        assertEquals(100, bytes.length);
    }

    @Host("http://httpbin.org")
    interface DownloadService {
        @GET("/bytes/30720")
        RestStreamResponse getBytes();

        @GET("/bytes/30720")
        Flux<ByteBuf> getBytesFlowable();
    }

    @Test
    public void SimpleDownloadTest() {
        try (RestStreamResponse response = createService(DownloadService.class).getBytes()) {
            int count = 0;
            for (ByteBuf byteBuf : response.body().doOnNext(b -> b.retain()).toIterable()) {
                // assertEquals(1, byteBuf.refCnt());
                count += byteBuf.readableBytes();
                ReferenceCountUtil.refCnt(byteBuf);
            }
            assertEquals(30720, count);
        }
    }

    @Test
    public void RawFlowableDownloadTest() {
        Flux<ByteBuf> response = createService(DownloadService.class).getBytesFlowable();
        int count = 0;
        for (ByteBuf byteBuf : response.doOnNext(b -> b.retain()).toIterable()) {
            count += byteBuf.readableBytes();
            ReferenceCountUtil.refCnt(byteBuf);
        }
        assertEquals(30720, count);
    }

    @Host("https://httpbin.org")
    interface FlowableUploadService {
        @PUT("/put")
        RestResponse<HttpBinJSON> put(@BodyParam("text/plain") Flux<ByteBuf> content, @HeaderParam("Content-Length") long contentLength);
    }

    @Test
    public void FlowableUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        Flux<ByteBuf> stream = FluxUtil.byteBufStreamFromFile(AsynchronousFileChannel.open(filePath));

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipeline(httpClient,
                new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS, true));
        //
        RestResponse<HttpBinJSON> response = RestProxy.create(FlowableUploadService.class, httpPipeline, serializer).put(stream, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.body().data);
    }

    @Test
    public void SegmentUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
        RestResponse<HttpBinJSON> response = createService(FlowableUploadService.class)
                .put(FluxUtil.byteBufStreamFromFile(fileChannel, 4, 15), 15);

        assertEquals("quick brown fox", response.body().data);
    }

    @Host("{url}")
    interface Service22 {
        @GET("{container}/{blob}")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Test
    public void service22GetBytes() {
        final byte[] bytes = createService(Service22.class).getBytes("http://httpbin.org/bytes/27");
        assertNotNull(bytes);
        assertEquals(27, bytes.length);
    }

    @Host("http://httpbin.org/")
    interface Service23 {
        @GET("bytes/28")
        byte[] getBytes();
    }

    @Test
    public void service23GetBytes() {
        final byte[] bytes = createService(Service23.class)
            .getBytes();
        assertNotNull(bytes);
        assertEquals(28, bytes.length);
    }

    @Host("http://httpbin.org/")
    interface Service24 {
        @PUT("put")
        HttpBinJSON put(@HeaderParam("ABC") Map<String,String> headerCollection);
    }

    @Test
    public void service24Put() {
        final Map<String,String> headerCollection = new HashMap<>();
        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");
        final HttpBinJSON result = createService(Service24.class)
            .put(headerCollection);
        assertNotNull(result.headers);
        final HttpHeaders resultHeaders = new HttpHeaders(result.headers);
        assertEquals("GHIJ", resultHeaders.value("ABCDEF"));
        assertEquals("45", resultHeaders.value("ABC123"));
    }

    @Host("http://httpbin.org")
    interface Service25 {
        @GET("anything")
        HttpBinJSON get();

        @GET("anything")
        Mono<HttpBinJSON> getAsync();

        @GET("anything")
        Mono<RestResponse<HttpBinJSON>> getBodyResponseAsync();
    }

    @Test(expected = RestException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipeline());
        service.get();
    }

    @Test(expected = RestException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testSingleMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipeline());
        service.getAsync().block();
        service.getBodyResponseAsync().block();
    }

    @Test(expected = RestException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testSingleBodyResponseMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipeline());
        service.getBodyResponseAsync().block();
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline = new HttpPipeline(httpClient);

        return RestProxy.create(serviceClass, httpPipeline, serializer);
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
        Assert.assertTrue("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'." , false);
    }

    private static final SerializerAdapter serializer = new JacksonAdapter();
}

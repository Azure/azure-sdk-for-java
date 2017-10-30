package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HEAD;
import com.microsoft.rest.v2.annotations.HeaderParam;
import com.microsoft.rest.v2.annotations.Headers;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.annotations.QueryParam;
import com.microsoft.rest.v2.annotations.UnexpectedResponseExceptionType;
import com.microsoft.rest.v2.entities.HttpBinHeaders;
import com.microsoft.rest.v2.entities.HttpBinJSON;
import com.microsoft.rest.v2.http.ContentType;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import org.junit.Assert;
import org.junit.Test;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        Single<byte[]> getByteArrayAsync();

        @GET("bytes/100")
        Single<byte[]> getByteArrayAsyncWithNoExpectedResponses();
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
                .toBlocking().value();
        assertNotNull(result);
        assertEquals(100, result.length);
    }

    @Test
    public void getByteArrayAsyncWithNoExpectedResponses() {
        final byte[] result = createService(Service1.class)
                .getByteArrayAsyncWithNoExpectedResponses()
                .toBlocking().value();
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
        Single<byte[]> getByteArrayAsync(@HostParam("hostName") String host, @PathParam("numberOfBytes") int numberOfBytes);
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
                .toBlocking().value();
        assertNotNull(result);
        assertEquals(result.length, 50);
    }

    @Test
    public void SyncRequestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArray("httpbin", 0);
        assertNotNull(result);
        assertEquals(result.length, 0);
    }

    @Host("http://httpbin.org")
    private interface Service3 {
        @GET("bytes/2")
        @ExpectedResponses({200})
        void getNothing();

        @GET("bytes/2")
        @ExpectedResponses({200})
        Completable getNothingAsync();
    }

    @Test
    public void SyncGetRequestWithNoReturn() {
        createService(Service3.class).getNothing();
    }

    @Test
    public void AsyncGetRequestWithNoReturn() {
        createService(Service3.class)
                .getNothingAsync()
                .await();
    }

    @Host("http://httpbin.org")
    private interface Service4 {
        @GET("bytes/2")
        @ExpectedResponses({200})
        InputStream getByteStream();

        @GET("bytes/2")
        @ExpectedResponses({200})
        Single<InputStream> getByteStreamAsync();
    }

    @Test
    public void SyncGetRequestWithInputStreamReturn() throws IOException {
        final InputStream byteStream = createService(Service4.class)
                .getByteStream();
        final byte[] buffer = new byte[10];
        assertEquals(2, byteStream.read(buffer));
        assertEquals(-1, byteStream.read(buffer));
    }

    @Test
    public void AsyncGetRequestWithInputStreamReturn() throws IOException {
        final InputStream byteStream = createService(Service4.class)
                .getByteStreamAsync()
                .toBlocking().value();
        final byte[] buffer = new byte[10];
        assertEquals(2, byteStream.read(buffer));
        assertEquals(-1, byteStream.read(buffer));
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
        Single<HttpBinJSON> getAnythingAsync();
    }

    @Test
    public void SyncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnything();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPlus();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/with+plus", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("withpathparam");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/withpathparam", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with path param");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/with path param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithPathParam("with+path+param");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/with+path+param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("withpathparam");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/withpathparam", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with%20path%20param");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/with path param", json.url);
    }

    @Test
    public void SyncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingWithEncodedPathParam("with+path+param");
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything/with+path+param", json.url);
    }

    @Test
    public void AsyncGetRequestWithAnything() {
        final HttpBinJSON json = createService(Service5.class)
                .getAnythingAsync()
                .toBlocking().value();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
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
        Single<HttpBinJSON> getAnythingAsync(@QueryParam("a") String a, @QueryParam("b") int b);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything?a=A&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything("A%20Z", 15);
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything?a=A%2520Z&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingWithEncoded("x%20y", 15);
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything?a=x y&b=15", json.url);
    }

    @Test
    public void AsyncGetRequestWithQueryParametersAndAnything() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnythingAsync("A", 15)
                .toBlocking().value();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything?a=A&b=15", json.url);
    }

    @Test
    public void SyncGetRequestWithNullQueryParameter() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything(null, 15);
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything?b=15", json.url);
    }

    @Host("http://httpbin.org")
    private interface Service7 {
        @GET("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HeaderParam("a") String a, @HeaderParam("b") int b);

        @GET("anything")
        @ExpectedResponses({200})
        Single<HttpBinJSON> getAnythingAsync(@HeaderParam("a") String a, @HeaderParam("b") int b);
    }

    @Test
    public void SyncGetRequestWithHeaderParametersAndAnythingReturn() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnything("A", 15);
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
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
                .toBlocking().value();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
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
        HttpBinJSON post(@BodyParam String postBody);

        @POST("post")
        @ExpectedResponses({200})
        Single<HttpBinJSON> postAsync(@BodyParam String postBody);
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
                .toBlocking().value();
        assertEquals(String.class, json.data.getClass());
        assertEquals("I'm a post body!", (String)json.data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void SyncPostRequestWithNullBody() {
        createService(Service8.class).post(null);
    }

    @Host("http://httpbin.org")
    private interface Service9 {
        @PUT("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam int putBody);

        @PUT("put")
        @ExpectedResponses({200})
        Single<HttpBinJSON> putAsync(@BodyParam int putBody);

        @PUT("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        Single<HttpBinJSON> putWithUnexpectedResponseAsync(@BodyParam String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@BodyParam String putBody);

        @PUT("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Single<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(@BodyParam String putBody);
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
                .toBlocking().value();
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
                    .toBlocking()
                    .value();
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
                    .toBlocking()
                    .value();
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
        HttpBinJSON head();

        @HEAD("anything")
        @ExpectedResponses({200})
        boolean headBoolean();

        @HEAD("anything")
        @ExpectedResponses({200})
        void voidHead();

        @HEAD("anything")
        @ExpectedResponses({200})
        Single<HttpBinJSON> headAsync();

        @HEAD("anything")
        @ExpectedResponses({200})
        Single<Boolean> headBooleanAsync();

        @HEAD("anything")
        @ExpectedResponses({200})
        Completable completableHeadAsync();
    }

    @Test
    public void SyncHeadRequest() {
        final HttpBinJSON json = createService(Service10.class)
                .head();
        assertNull(json);
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
        final HttpBinJSON json = createService(Service10.class)
                .headAsync()
                .toBlocking().value();
        assertNull(json);
    }

    @Test
    public void AsyncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBooleanAsync().toBlocking().value();
        assertTrue(result);
    }

    @Test
    public void AsyncCompletableHeadRequest() {
        createService(Service10.class)
                .completableHeadAsync()
                .await();
    }

    @Host("http://httpbin.org")
    private interface Service11 {
        @DELETE("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@BodyParam boolean bodyBoolean);

        @DELETE("delete")
        @ExpectedResponses({200})
        Single<HttpBinJSON> deleteAsync(@BodyParam boolean bodyBoolean);
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
                .toBlocking().value();
        assertEquals(String.class, json.data.getClass());
        assertEquals("false", (String)json.data);
    }

    @Host("http://httpbin.org")
    private interface Service12 {
        @PATCH("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@BodyParam String bodyString);

        @PATCH("patch")
        @ExpectedResponses({200})
        Single<HttpBinJSON> patchAsync(@BodyParam String bodyString);
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
                .toBlocking().value();
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
        Single<HttpBinJSON> getAsync();
    }

    @Test
    public void SyncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class)
                .get();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
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
                .toBlocking().value();
        assertNotNull(json);
        assertEquals("http://httpbin.org/anything", json.url);
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
        Single<HttpBinJSON> getAsync();
    }

    @Test
    public void AsyncHttpsHeadersRequest() {
        final HttpBinJSON json = createService(Service14.class)
                .getAsync()
                .toBlocking().value();
        assertNotNull(json);
        assertEquals("https://httpbin.org/anything", json.url);
        assertNotNull(json.headers);
        final HttpHeaders headers = new HttpHeaders(json.headers);
        assertEquals("MyHeaderValue", headers.value("MyHeader"));
    }

    @Host("https://httpbin.org")
    private interface Service15 {
        @GET("anything")
        @ExpectedResponses({200})
        Observable<HttpBinJSON> get();
    }

    @Test
    public void service15Get() {
        final Service15 service = createService(Service15.class);
        try {
            service.get();
            fail("Expected exception.");
        }
        catch (InvalidReturnTypeException e) {
            assertContains(e.getMessage(), "rx.Observable<com.microsoft.rest.v2.entities.HttpBinJSON>");
            assertContains(e.getMessage(), "RestProxyTests$Service15.get()");
        }
    }

    @Host("https://httpbin.org")
    private interface Service16 {
        @PUT("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@BodyParam byte[] bytes);

        @PUT("put")
        @ExpectedResponses({200})
        Single<HttpBinJSON> putByteArrayAsync(@BodyParam byte[] bytes);
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
                .toBlocking()
                .value();
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
        Single<HttpBinJSON> getAsync(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);
    }

    @Test
    public void SyncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.get("http", "bin");
        assertNotNull(result);
        assertEquals("http://httpbin.org/get", result.url);
    }

    @Test
    public void AsyncRequestWithMultipleHostParams() {
        final Service17 service17 = createService(Service17.class);
        final HttpBinJSON result = service17.getAsync("http", "bin").toBlocking().value();
        assertNotNull(result);
        assertEquals("http://httpbin.org/get", result.url);
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
        HttpBinJSON putWithNoContentTypeAndStringBody(@BodyParam String body);

        @PUT("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@BodyParam byte[] body);

        @PUT("put")
        @Headers({ "Content-Type: application/json" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(@BodyParam String body);

        @PUT("put")
        @Headers({ "Content-Type: application/json" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@BodyParam byte[] body);

        @PUT("put")
        @Headers({ "Content-Type: application/json; charset=utf-8" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@BodyParam String body);

        @PUT("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@BodyParam String body);

        @PUT("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@BodyParam byte[] body);

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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithNoContentTypeAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(null);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("");
        assertEquals("\"\"", result.data);
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(null);
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

    @Test(expected = IllegalArgumentException.class)
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(null);
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
        RestResponse<HttpBinHeaders,Void> getBytes100OnlyHeaders();

        @GET("bytes/100")
        RestResponse<Map<String, String>,Void> getBytes100OnlyRawHeaders();

        @GET("bytes/100")
        RestResponse<HttpBinHeaders,byte[]> getBytes100BodyAndHeaders();

        @PUT("put")
        RestResponse<HttpBinHeaders,Void> putOnlyHeaders(@BodyParam String body);

        @PUT("put")
        RestResponse<HttpBinHeaders,HttpBinJSON> putBodyAndHeaders(@BodyParam String body);

        @GET("bytes/100")
        RestResponse<Void, Void> getBytesOnlyStatus();
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        final RestResponse<HttpBinHeaders,Void> response = createService(Service20.class)
                .getBytes100OnlyHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.headers();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection);
        assertNotNull(headers.date);
        assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final RestResponse<HttpBinHeaders,byte[]> response = createService(Service20.class)
                .getBytes100BodyAndHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final byte[] body = response.body();
        assertNotNull(body);
        assertEquals(100, body.length);

        final HttpBinHeaders headers = response.headers();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection);
        assertNotNull(headers.date);
        assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        final RestResponse<Void,Void> response = createService(Service20.class)
                .getBytesOnlyStatus();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        final RestResponse<Map<String, String>, Void> response = createService(Service20.class)
                .getBytes100OnlyRawHeaders();

        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertNotNull(response.headers());
        assertNotEquals(0, response.headers().size());
    }

    @Test
    public void service20PutOnlyHeaders() {
        final RestResponse<HttpBinHeaders,Void> response = createService(Service20.class)
                .putOnlyHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.headers();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection);
        assertNotNull(headers.date);
        assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    @Test
    public void service20PutBodyAndHeaders() {
        final RestResponse<HttpBinHeaders,HttpBinJSON> response = createService(Service20.class)
                .putBodyAndHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinJSON body = response.body();
        assertNotNull(body);
        assertEquals("http://httpbin.org/put", body.url);
        assertEquals("body string", body.data);

        final HttpBinHeaders headers = response.headers();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials);
        assertEquals("keep-alive", headers.connection);
        assertNotNull(headers.date);
        assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime);
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return RestProxy.create(serviceClass, null, httpClient, serializer);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }

    private static final SerializerAdapter<?> serializer = new JacksonAdapter();
}

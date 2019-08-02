// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.MyRestException;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.implementation.annotation.BodyParam;
import com.azure.core.implementation.annotation.Delete;
import com.azure.core.implementation.annotation.ExpectedResponses;
import com.azure.core.implementation.annotation.FormParam;
import com.azure.core.implementation.annotation.Get;
import com.azure.core.implementation.annotation.Head;
import com.azure.core.implementation.annotation.HeaderParam;
import com.azure.core.implementation.annotation.Headers;
import com.azure.core.implementation.annotation.Host;
import com.azure.core.implementation.annotation.HostParam;
import com.azure.core.implementation.annotation.Patch;
import com.azure.core.implementation.annotation.Post;
import com.azure.core.implementation.annotation.Put;
import com.azure.core.implementation.annotation.PathParam;
import com.azure.core.implementation.annotation.QueryParam;
import com.azure.core.implementation.annotation.ServiceInterface;
import com.azure.core.implementation.annotation.UnexpectedResponseExceptionType;
import com.azure.core.entities.HttpBinFormDataJSON;
import com.azure.core.entities.HttpBinFormDataJSON.PizzaSize;
import com.azure.core.entities.HttpBinHeaders;
import com.azure.core.entities.HttpBinJSON;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.ContentType;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.FluxUtil;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
    @ServiceInterface(name = "Service1")
    private interface Service1 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        byte[] getByteArray();

        @Get("bytes/100")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync();

        @Get("bytes/100")
        Mono<byte[]> getByteArrayAsyncWithNoExpectedResponses();
    }

    @Test
    public void syncRequestWithByteArrayReturnType() {
        final byte[] result = createService(Service1.class)
                .getByteArray();
        assertNotNull(result);
        assertEquals(100, result.length);
    }

    @Test
    public void asyncRequestWithByteArrayReturnType() {
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
    @ServiceInterface(name = "Service2")
    private interface Service2 {
        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("hostName") String host, @PathParam("numberOfBytes") int numberOfBytes);

        @Get("bytes/{numberOfBytes}")
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

    @Test
    public void syncRequestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class)
                .getByteArray("httpbin", 0);
        // If no body then for async returns Mono.empty() for sync return null.
        assertNull(result);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service3")
    private interface Service3 {
        @Get("bytes/2")
        @ExpectedResponses({200})
        void getNothing();

        @Get("bytes/2")
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
    @ServiceInterface(name = "Service5")
    private interface Service5 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything();

        @Get("anything/with+plus")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPlus();

        @Get("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithPathParam(@PathParam("path") String pathParam);

        @Get("anything/{path}")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncodedPathParam(@PathParam(value = "path", encoded = true) String pathParam);

        @Get("anything")
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
    @ServiceInterface(name = "Service6")
    private interface Service6 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@QueryParam("a") String a, @QueryParam("b") int b);

        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncoded(@QueryParam(value = "a", encoded = true) String a, @QueryParam("b") int b);

        @Get("anything")
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

    @Test
    public void syncGetRequestWithNullQueryParameter() {
        final HttpBinJSON json = createService(Service6.class)
                .getAnything(null, 15);
        assertNotNull(json);
        assertMatchWithHttpOrHttps("httpbin.org/anything?b=15", json.url());
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service7")
    private interface Service7 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HeaderParam("a") String a, @HeaderParam("b") int b);

        @Get("anything")
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

    @Test
    public void syncGetRequestWithNullHeader() {
        final HttpBinJSON json = createService(Service7.class)
                .getAnything(null, 15);

        final HttpHeaders headers = new HttpHeaders(json.headers());

        assertEquals(null, headers.value("A"));
        assertArrayEquals(null, headers.values("A"));
        assertEquals("15", headers.value("B"));
        assertArrayEquals(new String[]{"15"}, headers.values("B"));
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service8")
    private interface Service8 {
        @Post("post")
        @ExpectedResponses({200})
        HttpBinJSON post(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);

        @Post("post")
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

    @Test
    public void syncPostRequestWithNullBody() {
        final HttpBinJSON result = createService(Service8.class).post(null);
        assertEquals("", result.data());
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        Mono<HttpBinJSON> putWithUnexpectedResponseAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
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
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.value());
            assertTrue(e.value() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.value();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponse() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAsync("I'm the body!")
                    .block();
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.value());
            assertTrue(e.value() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.value();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAndExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class)
                    .putWithUnexpectedResponseAndExceptionTypeAsync("I'm the body!")
                    .block();
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndDeterminedExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndDeterminedExceptionTypeAsync("I'm the body!")
                .block();
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndFallthroughExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndFallthroughExceptionTypeAsync("I'm the body!")
                .block();
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.value());
            Assert.assertEquals("I'm the body!", e.value().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndNoFallthroughExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.value());
            assertTrue(e.value() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.value();
            assertEquals("I'm the body!", expectedBody.get("data"));
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        try {
            createService(Service9.class)
                .putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync("I'm the body!")
                .block();
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.value());
            assertTrue(e.value() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.value();
            assertEquals("I'm the body!", expectedBody.get("data"));
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service10")
    private interface Service10 {
        @Head("anything")
        @ExpectedResponses({200})
        VoidResponse head();

        @Head("anything")
        @ExpectedResponses({200})
        boolean headBoolean();

        @Head("anything")
        @ExpectedResponses({200})
        void voidHead();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<VoidResponse> headAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Boolean> headBooleanAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync();
    }

    @Test
    public void syncHeadRequest() {
        final Void body = createService(Service10.class)
                .head()
                .value();
        assertNull(body);
    }

    @Test
    public void syncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBoolean();
        assertTrue(result);
    }

    @Test
    public void syncVoidHeadRequest() {
        createService(Service10.class)
                .voidHead();
    }

    @Test
    public void asyncHeadRequest() {
        final Void body = createService(Service10.class)
                .headAsync()
                .block()
                .value();

        assertNull(body);
    }

    @Test
    public void asyncHeadBooleanRequest() {
        final boolean result = createService(Service10.class).headBooleanAsync().block();
        assertTrue(result);
    }

    @Test
    public void asyncCompletableHeadRequest() {
        createService(Service10.class)
                .completableHeadAsync()
                .block();
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service11")
    private interface Service11 {
        @Delete("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

        @Delete("delete")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> deleteAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);
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
    @ServiceInterface(name = "Service12")
    private interface Service12 {
        @Patch("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        @Patch("patch")
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
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value" })
        HttpBinJSON get();

        @Get("anything")
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
    @ServiceInterface(name = "Service14")
    private interface Service14 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({ "MyHeader:MyHeaderValue" })
        HttpBinJSON get();

        @Get("anything")
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
    @ServiceInterface(name = "Service16")
    private interface Service16 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putByteArrayAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);
    }

    @Test
    public void service16Put() throws Exception {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJSON httpBinJSON = service16.putByteArray(expectedBytes);

        // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        assertTrue(httpBinJSON.data() instanceof String);

        final String base64String = (String) httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void service16PutAsync() throws Exception {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        final HttpBinJSON httpBinJSON = service16.putByteArrayAsync(expectedBytes)
                .block();
        assertTrue(httpBinJSON.data() instanceof String);

        final String base64String = (String) httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Host("http://{hostPart1}{hostPart2}.org")
    @ServiceInterface(name = "Service17")
    private interface Service17 {
        @Get("get")
        @ExpectedResponses({200})
        HttpBinJSON get(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);

        @Get("get")
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
    @ServiceInterface(name = "Service18")
    private interface Service18 {
        @Get("status/200")
        void getStatus200();

        @Get("status/200")
        @ExpectedResponses({200})
        void getStatus200WithExpectedResponse200();

        @Get("status/300")
        void getStatus300();

        @Get("status/300")
        @ExpectedResponses({300})
        void getStatus300WithExpectedResponse300();

        @Get("status/400")
        void getStatus400();

        @Get("status/400")
        @ExpectedResponses({400})
        void getStatus400WithExpectedResponse400();

        @Get("status/500")
        void getStatus500();

        @Get("status/500")
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

    @Test(expected = HttpResponseException.class)
    public void service18GetStatus400() {
        createService(Service18.class)
                .getStatus400();
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        createService(Service18.class)
                .getStatus400WithExpectedResponse400();
    }

    @Test(expected = HttpResponseException.class)
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
    @ServiceInterface(name = "Service19")
    private interface Service19 {
        @Put("put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        @Headers({ "Content-Type: application/json" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @Headers({ "Content-Type: application/json; charset=utf-8" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({ "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(@BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody("");
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndStringBody("hello");
        assertEquals("hello", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithNoContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody("");
        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("");
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("soups and stuff");
        assertEquals("soups and stuff", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody("");
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndStringBody("penguins");
        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody("");
        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody("");
        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody("soups and stuff");
        assertEquals("\"soups and stuff\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("\"\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals("\"AAECAwQ=\"", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody("");
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndStringBody("penguins");
        assertEquals("penguins", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(null);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0]);
        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
                .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(new String(new byte[] { 0, 1, 2, 3, 4 }), result.data());
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service20")
    private interface Service20 {
        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, Void> getBytes100OnlyHeaders();

        @Get("bytes/100")
        ResponseBase<HttpHeaders, Void> getBytes100OnlyRawHeaders();

        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, byte[]> getBytes100BodyAndHeaders();

        @Put("put")
        ResponseBase<HttpBinHeaders, Void> putOnlyHeaders(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        ResponseBase<HttpBinHeaders, HttpBinJSON> putBodyAndHeaders(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Get("bytes/100")
        ResponseBase<Void, Void> getBytesOnlyStatus();

        @Get("bytes/100")
        VoidResponse getVoidResponse();

        @Put("put")
        Response<HttpBinJSON> putBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class)
                .getBytes100OnlyHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials());
        assertEquals("keep-alive", headers.connection().toLowerCase());
        assertNotNull(headers.date());
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime());
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, byte[]> response = createService(Service20.class)
                .getBytes100BodyAndHeaders();
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final byte[] body = response.value();
        assertNotNull(body);
        assertEquals(100, body.length);

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime());
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        final Response<Void> response = createService(Service20.class)
                .getBytesOnlyStatus();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        final Response<Void> response = createService(Service20.class)
                .getBytes100OnlyRawHeaders();

        assertNotNull(response);
        assertEquals(200, response.statusCode());
        assertNotNull(response.headers());
        assertNotEquals(0, response.headers().size());
    }

    @Test
    public void service20PutOnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class)
                .putOnlyHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials());
        assertEquals("keep-alive", headers.connection().toLowerCase());
        assertNotNull(headers.date());
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime());
    }

    @Test
    public void service20PutBodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, HttpBinJSON> response = createService(Service20.class)
                .putBodyAndHeaders("body string");
        assertNotNull(response);

        assertEquals(200, response.statusCode());

        final HttpBinJSON body = response.value();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("httpbin.org/put", body.url());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.deserializedHeaders();
        assertNotNull(headers);
        assertEquals(true, headers.accessControlAllowCredentials());
        assertEquals("keep-alive", headers.connection().toLowerCase());
        assertNotNull(headers.date());
        // assertEquals("1.1 vegur", headers.via);
        assertNotEquals(0, headers.xProcessedTime());
    }

    @Test
    public void service20GetVoidResponse() {
        final VoidResponse response = createService(Service20.class).getVoidResponse();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void service20GetResponseBody() {
        final Response<HttpBinJSON> response = createService(Service20.class).putBody("body string");
        assertNotNull(response);
        assertEquals(200, response.statusCode());

        final HttpBinJSON body = response.value();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("httpbin.org/put", body.url());
        assertEquals("body string", body.data());

        final HttpHeaders headers = response.headers();
        assertNotNull(headers);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "UnexpectedOKService")
    interface UnexpectedOKService {
        @Get("/bytes/1024")
        @ExpectedResponses({400})
        StreamResponse getBytes();
    }

    @Test
    public void unexpectedHTTPOK() {
        try {
            createService(UnexpectedOKService.class).getBytes();
            fail();
        } catch (HttpResponseException e) {
            assertEquals("Status code 200, (1024-byte body)", e.getMessage());
        }
    }

    @Host("https://www.example.com")
    @ServiceInterface(name = "Service21")
    private interface Service21 {
        @Get("http://httpbin.org/bytes/100")
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
    @ServiceInterface(name = "DownloadService")
    interface DownloadService {
        @Get("/bytes/30720")
        StreamResponse getBytes();

        @Get("/bytes/30720")
        Flux<ByteBuf> getBytesFlowable();
    }

    @Test
    public void simpleDownloadTest() {
        try (StreamResponse response = createService(DownloadService.class).getBytes()) {
            int count = 0;
            for (ByteBuf byteBuf : response.value().doOnNext(b -> b.retain()).toIterable()) {
                // assertEquals(1, byteBuf.refCnt());
                count += byteBuf.readableBytes();
                ReferenceCountUtil.refCnt(byteBuf);
            }
            assertEquals(30720, count);
        }
    }

    @Test
    public void rawFlowableDownloadTest() {
        Flux<ByteBuf> response = createService(DownloadService.class).getBytesFlowable();
        int count = 0;
        for (ByteBuf byteBuf : response.doOnNext(b -> b.retain()).toIterable()) {
            count += byteBuf.readableBytes();
            ReferenceCountUtil.refCnt(byteBuf);
        }
        assertEquals(30720, count);
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "FlowableUploadService")
    interface FlowableUploadService {
        @Put("/put")
        Response<HttpBinJSON> put(@BodyParam("text/plain") Flux<ByteBuf> content, @HeaderParam("Content-Length") long contentLength);
    }

    @Test
    public void flowableUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        Flux<ByteBuf> stream = FluxUtil.byteBufStreamFromFile(AsynchronousFileChannel.open(filePath));

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS, true))
            .build();
        //
        Response<HttpBinJSON> response = RestProxy.create(FlowableUploadService.class, httpPipeline, SERIALIZER).put(stream, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.value().data());
    }

    @Test
    public void segmentUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
        Response<HttpBinJSON> response = createService(FlowableUploadService.class)
                .put(FluxUtil.byteBufStreamFromFile(fileChannel, 4, 15), 15);

        assertEquals("quick brown fox", response.value().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("{container}/{blob}")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Test
    public void service22GetBytes() {
        final byte[] bytes = createService(Service22.class).getBytes("http://httpbin.org/bytes/27");
        assertNotNull(bytes);
        assertEquals(27, bytes.length);
    }

    @Host("http://httpbin.org/")
    @ServiceInterface(name = "Service23")
    interface Service23 {
        @Get("bytes/28")
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
    @ServiceInterface(name = "Service24")
    interface Service24 {
        @Put("put")
        HttpBinJSON put(@HeaderParam("ABC") Map<String, String> headerCollection);
    }

    @Test
    public void service24Put() {
        final Map<String, String> headerCollection = new HashMap<>();
        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");
        final HttpBinJSON result = createService(Service24.class)
            .put(headerCollection);
        assertNotNull(result.headers());
        final HttpHeaders resultHeaders = new HttpHeaders(result.headers());
        assertEquals("GHIJ", resultHeaders.value("ABCDEF"));
        assertEquals("45", resultHeaders.value("ABC123"));
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service25")
    interface Service25 {
        @Get("anything")
        HttpBinJSON get();

        @Get("anything")
        Mono<HttpBinJSON> getAsync();

        @Get("anything")
        Mono<Response<HttpBinJSON>> getBodyResponseAsync();
    }

    @Test(expected = HttpResponseException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipelineBuilder().build());
        service.get();
    }

    @Test(expected = HttpResponseException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testSingleMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipelineBuilder().build());
        service.getAsync().block();
        service.getBodyResponseAsync().block();
    }

    @Test(expected = HttpResponseException.class)
    @Ignore("Decoding is not a policy anymore")
    public void testSingleBodyResponseMissingDecodingPolicyCausesException() {
        Service25 service = RestProxy.create(Service25.class, new HttpPipelineBuilder().build());
        service.getBodyResponseAsync().block();
    }

    @Host("http://httpbin.org/")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        HttpBinFormDataJSON postForm(@FormParam("custname") String name, @FormParam("custtel") String telephone, @FormParam("custemail") String email, @FormParam("size") PizzaSize size, @FormParam("toppings") List<String> toppings);
    }

    @Test
    public void postUrlFormEncoded() {
        Service26 service = RestProxy.create(Service26.class, new HttpPipelineBuilder().build());
        HttpBinFormDataJSON response = service.postForm("Foo", "123", "foo@bar.com", PizzaSize.LARGE, Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo@bar.com", response.form().customerEmail());
        assertEquals(PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .build();

        return RestProxy.create(serviceClass, httpPipeline, SERIALIZER);
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

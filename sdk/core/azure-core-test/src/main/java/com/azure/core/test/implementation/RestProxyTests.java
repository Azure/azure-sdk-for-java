// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.PortPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.test.MyRestException;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON.PizzaSize;
import com.azure.core.test.implementation.entities.HttpBinHeaders;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class RestProxyTests {

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    /**
     * Get the dynamic port the WireMock server is using to properly route the request.
     *
     * @return The HTTP port WireMock is using.
     */
    protected abstract int getWireMockPort();

    @Host("http://localhost")
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
        final byte[] result = createService(Service1.class).getByteArray();

        assertNotNull(result);
        assertEquals(100, result.length);
    }

    @Test
    public void asyncRequestWithByteArrayReturnType() {
        StepVerifier.create(createService(Service1.class).getByteArrayAsync())
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    @Test
    public void getByteArrayAsyncWithNoExpectedResponses() {
        StepVerifier.create(createService(Service1.class).getByteArrayAsyncWithNoExpectedResponses())
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    @Host("http://{hostName}")
    @ServiceInterface(name = "Service2")
    private interface Service2 {
        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        byte[] getByteArray(@HostParam("hostName") String host, @PathParam("numberOfBytes") int numberOfBytes);

        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        Mono<byte[]> getByteArrayAsync(@HostParam("hostName") String host,
            @PathParam("numberOfBytes") int numberOfBytes);
    }

    @Test
    public void syncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class).getByteArray("localhost", 100);

        assertNotNull(result);
        assertEquals(result.length, 100);
    }

    @Test
    public void asyncRequestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        StepVerifier.create(createService(Service2.class).getByteArrayAsync("localhost", 100))
            .assertNext(bytes -> assertEquals(100, bytes.length))
            .verifyComplete();
    }

    @Test
    public void syncRequestWithEmptyByteArrayReturnTypeAndParameterizedHostAndPath() {
        final byte[] result = createService(Service2.class).getByteArray("localhost", 0);

        // If no body then for async returns Mono.empty() for sync return null.
        assertNull(result);
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service3")
    private interface Service3 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        void getNothing();

        @Get("bytes/100")
        @ExpectedResponses({200})
        Mono<Void> getNothingAsync();
    }

    @Test
    public void syncGetRequestWithNoReturn() {
        try {
            createService(Service3.class).getNothing();
        } catch (Throwable throwable) {
            fail("Received unexpected exception.");
        }
    }

    @Test
    public void asyncGetRequestWithNoReturn() {
        StepVerifier.create(createService(Service3.class).getNothingAsync())
            .verifyComplete();
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service5.class).getAnything();

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPlus();

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam("withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithSpace() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam("with path param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithPathParam("with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParam() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam("withpathparam");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPercent20() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam("with%20path%20param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
    }

    @Test
    public void syncGetRequestWithAnythingWithEncodedPathParamWithPlus() {
        final HttpBinJSON json = createService(Service5.class).getAnythingWithEncodedPathParam("with+path+param");

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
    }

    @Test
    public void asyncGetRequestWithAnything() {
        StepVerifier.create(createService(Service5.class).getAnythingAsync())
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/anything", json.url()))
            .verifyComplete();
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service6.class).getAnything("A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnything("A%20Z", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.url());
    }

    @Test
    public void syncGetRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        final HttpBinJSON json = createService(Service6.class).getAnythingWithEncoded("x%20y", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.url());
    }

    @Test
    public void asyncGetRequestWithQueryParametersAndAnything() {
        StepVerifier.create(createService(Service6.class).getAnythingAsync("A", 15))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url()))
            .verifyComplete();
    }

    @Test
    public void syncGetRequestWithNullQueryParameter() {
        final HttpBinJSON json = createService(Service6.class).getAnything(null, 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything?b=15", json.url());
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service7.class).getAnything("A", 15);

        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
        assertEquals("A", headers.getValue("A"));
        assertArrayEquals(new String[]{"A"}, headers.getValues("A"));
        assertEquals("15", headers.getValue("B"));
        assertArrayEquals(new String[]{"15"}, headers.getValues("B"));
    }

    @Test
    public void asyncGetRequestWithHeaderParametersAndAnything() {
        StepVerifier.create(createService(Service7.class).getAnythingAsync("A", 15))
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
                assertEquals("A", headers.getValue("A"));
                assertArrayEquals(new String[]{"A"}, headers.getValues("A"));
                assertEquals("15", headers.getValue("B"));
                assertArrayEquals(new String[]{"15"}, headers.getValues("B"));
            })
            .verifyComplete();
    }

    @Test
    public void syncGetRequestWithNullHeader() {
        final HttpBinJSON json = createService(Service7.class).getAnything(null, 15);

        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
        assertNull(headers.getValue("A"));
        assertArrayEquals(null, headers.getValues("A"));
        assertEquals("15", headers.getValue("B"));
        assertArrayEquals(new String[]{"15"}, headers.getValues("B"));
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service8.class).post("I'm a post body!");

        assertEquals(String.class, json.data().getClass());
        assertEquals("I'm a post body!", json.data());
    }

    @Test
    public void asyncPostRequestWithStringBody() {
        StepVerifier.create(createService(Service8.class).postAsync("I'm a post body!"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("I'm a post body!", json.data());
            })
            .verifyComplete();
    }

    @Test
    public void syncPostRequestWithNullBody() {
        final HttpBinJSON result = createService(Service8.class).post(null);

        assertEquals("", result.data());
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putBodyAndContentLength(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putAsyncBodyAndContentLength(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) Flux<ByteBuffer> body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        Mono<HttpBinJSON> putWithUnexpectedResponseAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    @Test
    public void syncPutRequestWithIntBody() {
        final HttpBinJSON json = createService(Service9.class).put(42);

        assertEquals(String.class, json.data().getClass());
        assertEquals("42", json.data());
    }

    @Test
    public void asyncPutRequestWithIntBody() {
        StepVerifier.create(createService(Service9.class).putAsync(42))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("42", json.data());
            }).verifyComplete();
    }

    // Test all scenarios for the body length and content length comparison for sync API
    @Test
    public void syncPutRequestWithBodyAndEqualContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        final HttpBinJSON json = createService(Service9.class).putBodyAndContentLength(body, 4L);

        assertEquals("test", json.data());
        assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
        assertEquals("4", json.getHeaderValue("Content-Length"));
    }

    @Test
    public void syncPutRequestWithBodyLessThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        UnexpectedLengthException unexpectedLengthException = assertThrows(UnexpectedLengthException.class, () -> {
            createService(Service9.class).putBodyAndContentLength(body, 5L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("less than"));
    }

    @Test
    public void syncPutRequestWithBodyMoreThanContentLength() {
        ByteBuffer body = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        UnexpectedLengthException unexpectedLengthException = assertThrows(UnexpectedLengthException.class, () -> {
            createService(Service9.class).putBodyAndContentLength(body, 3L);
            body.clear();
        });
        assertTrue(unexpectedLengthException.getMessage().contains("more than"));
    }

    // Test all scenarios for the body length and content length comparison for Async API
    @Test
    public void asyncPutRequestWithBodyAndEqualContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(body, 4L))
            .assertNext(json -> {
                assertEquals("test", json.data());
                assertEquals(ContentType.APPLICATION_OCTET_STREAM, json.getHeaderValue("Content-Type"));
                assertEquals("4", json.getHeaderValue("Content-Length"));
            }).verifyComplete();
    }

    @Test
    public void asyncPutRequestWithBodyAndLessThanContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(body, 5L))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException);
                assertTrue(exception.getMessage().contains("less than"));
            });
    }

    @Test
    public void asyncPutRequestWithBodyAndMoreThanContentLength() {
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        StepVerifier.create(createService(Service9.class).putAsyncBodyAndContentLength(body, 3L))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof UnexpectedLengthException);
                assertTrue(exception.getMessage().contains("more than"));
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponse() {
        try {
            createService(Service9.class).putWithUnexpectedResponse("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.getValue());
            assertTrue(e.getValue() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponse() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAsync("I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) throwable;
                assertNotNull(exception.getValue());
                assertTrue(exception.getValue() instanceof LinkedHashMap);

                @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) exception.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndExceptionType() {
        try {
            createService(Service9.class).putWithUnexpectedResponseAndExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndExceptionTypeAsync("I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof MyRestException, "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                MyRestException myRestException = (MyRestException) throwable;
                assertNotNull(myRestException.getValue());
                assertEquals("I'm the body!", myRestException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        try {
            createService(Service9.class).putWithUnexpectedResponseAndDeterminedExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndDeterminedExceptionTypeAsync("I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof MyRestException, "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                MyRestException restException = (MyRestException) throwable;
                assertNotNull(restException.getValue());
                assertEquals("I'm the body!", restException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        try {
            createService(Service9.class).putWithUnexpectedResponseAndFallthroughExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (MyRestException e) {
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndFallthroughExceptionTypeAsync("I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof MyRestException, "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                MyRestException restException = (MyRestException) throwable;
                assertNotNull(restException.getValue());
                assertEquals("I'm the body!", restException.getValue().data());
            });
    }

    @Test
    public void syncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        try {
            createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionType("I'm the body!");
            fail("Expected HttpResponseException would be thrown.");
        } catch (HttpResponseException e) {
            assertNotNull(e.getValue());
            assertTrue(e.getValue() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
            assertEquals("I'm the body!", expectedBody.get("data"));
        } catch (Throwable e) {
            fail("Expected MyRestException would be thrown. Instead got " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void asyncPutRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        StepVerifier.create(createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync("I'm the body!"))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof HttpResponseException, "Expected MyRestException would be thrown. Instead got " + throwable.getClass().getSimpleName());
                HttpResponseException responseException = (HttpResponseException) throwable;
                assertNotNull(responseException.getValue());
                assertTrue(responseException.getValue() instanceof LinkedHashMap);

                @SuppressWarnings("unchecked") final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) responseException.getValue();
                assertEquals("I'm the body!", expectedBody.get("data"));
            });
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service10")
    private interface Service10 {
        @Head("anything")
        @ExpectedResponses({200})
        Response<Void> head();

        @Head("anything")
        @ExpectedResponses({200})
        boolean headBoolean();

        @Head("anything")
        @ExpectedResponses({200})
        void voidHead();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Response<Void>> headAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Boolean> headBooleanAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync();
    }

    @Test
    public void syncHeadRequest() {
        final Void body = createService(Service10.class).head().getValue();
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
        StepVerifier.create(createService(Service10.class).headAsync())
            .assertNext(response -> assertNull(response.getValue()))
            .verifyComplete();
    }

    @Test
    public void asyncHeadBooleanRequest() {
        StepVerifier.create(createService(Service10.class).headBooleanAsync())
            .assertNext(Assertions::assertTrue)
            .verifyComplete();
    }

    @Test
    public void asyncCompletableHeadRequest() {
        StepVerifier.create(createService(Service10.class).completableHeadAsync())
            .verifyComplete();
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service11.class).delete(false);

        assertEquals(String.class, json.data().getClass());
        assertEquals("false", json.data());
    }

    @Test
    public void asyncDeleteRequest() {
        StepVerifier.create(createService(Service11.class).deleteAsync(false))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("false", json.data());
            }).verifyComplete();
    }

    @Host("http://localhost")
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
        final HttpBinJSON json = createService(Service12.class).patch("body-contents");

        assertEquals(String.class, json.data().getClass());
        assertEquals("body-contents", json.data());
    }

    @Test
    public void asyncPatchRequest() {
        StepVerifier.create(createService(Service12.class).patchAsync("body-contents"))
            .assertNext(json -> {
                assertEquals(String.class, json.data().getClass());
                assertEquals("body-contents", json.data());
            }).verifyComplete();
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        HttpBinJSON get();

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        Mono<HttpBinJSON> getAsync();
    }

    @Test
    public void syncHeadersRequest() {
        final HttpBinJSON json = createService(Service13.class).get();
        assertNotNull(json);
        assertMatchWithHttpOrHttps("localhost/anything", json.url());
        assertNotNull(json.headers());
        final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
        assertEquals("MyHeaderValue", headers.getValue("MyHeader"));
        assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues("MyHeader"));
        assertEquals("My,Header,Value", headers.getValue("MyOtherHeader"));
        assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.getValues("MyOtherHeader"));
    }

    @Test
    public void asyncHeadersRequest() {
        StepVerifier.create(createService(Service13.class).getAsync())
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
                assertEquals("MyHeaderValue", headers.getValue("MyHeader"));
                assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues("MyHeader"));
            }).verifyComplete();
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service14")
    private interface Service14 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        HttpBinJSON get();

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        Mono<HttpBinJSON> getAsync();
    }

    @Test
    public void asyncHttpsHeadersRequest() {
        StepVerifier.create(createService(Service14.class).getAsync())
            .assertNext(json -> {
                assertMatchWithHttpOrHttps("localhost/anything", json.url());
                assertNotNull(json.headers());
                final HttpHeaders headers = new HttpHeaders().setAll(json.headers());
                assertEquals("MyHeaderValue", headers.getValue("MyHeader"));
            }).verifyComplete();
    }

    @Host("http://localhost")
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
    public void service16Put() {
        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        final HttpBinJSON httpBinJSON = service16.putByteArray(expectedBytes);

        // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        assertTrue(httpBinJSON.data() instanceof String);

        final String base64String = (String) httpBinJSON.data();
        final byte[] actualBytes = base64String.getBytes();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void service16PutAsync() {
        final byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        StepVerifier.create(createService(Service16.class).putByteArrayAsync(expectedBytes))
            .assertNext(json -> {
                assertTrue(json.data() instanceof String);
                assertArrayEquals(expectedBytes, ((String) json.data()).getBytes());
            }).verifyComplete();
    }

    @Host("http://{hostPart1}{hostPart2}")
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
        final HttpBinJSON result = createService(Service17.class).get("local", "host");

        assertNotNull(result);
        assertMatchWithHttpOrHttps("localhost/get", result.url());
    }

    @Test
    public void asyncRequestWithMultipleHostParams() {
        StepVerifier.create(createService(Service17.class).getAsync("local", "host"))
            .assertNext(json -> assertMatchWithHttpOrHttps("localhost/get", json.url()))
            .verifyComplete();
    }

    @Host("http://localhost")
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
        createService(Service18.class).getStatus200();
    }

    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        createService(Service18.class).getStatus200WithExpectedResponse200();
    }

    @Test
    public void service18GetStatus300() {
        createService(Service18.class).getStatus300();
    }

    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        createService(Service18.class).getStatus300WithExpectedResponse300();
    }

    @Test
    public void service18GetStatus400() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus400());
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        createService(Service18.class).getStatus400WithExpectedResponse400();
    }

    @Test
    public void service18GetStatus500() {
        assertThrows(HttpResponseException.class, () -> createService(Service18.class).getStatus500());
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        createService(Service18.class).getStatus500WithExpectedResponse500();
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service19")
    private interface Service19 {
        @Put("put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        @Headers({"Content-Type: application/json"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @Headers({"Content-Type: application/json; charset=utf-8"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody(null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody("");

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndStringBody("hello");

        assertEquals("hello", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(null);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        final HttpBinJSON result = createService(Service19.class).putWithNoContentTypeAndByteArrayBody(new byte[0]);

        assertEquals("", result.data());
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        final HttpBinJSON result = createService(Service19.class)
            .putWithNoContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
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
            .putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4});

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
            .putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
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
            .putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4});

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
            .putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4});

        assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), result.data());
    }

    @Host("http://localhost")
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
        ResponseBase<HttpBinHeaders, HttpBinJSON> putBodyAndHeaders(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Get("bytes/100")
        ResponseBase<Void, Void> getBytesOnlyStatus();

        @Get("bytes/100")
        Response<Void> getVoidResponse();

        @Put("put")
        Response<HttpBinJSON> putBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class).getBytes100OnlyHeaders();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, byte[]> response = createService(Service20.class).getBytes100BodyAndHeaders();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final byte[] body = response.getValue();
        assertNotNull(body);
        assertEquals(100, body.length);

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        final Response<Void> response = createService(Service20.class).getBytesOnlyStatus();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        final Response<Void> response = createService(Service20.class).getBytes100OnlyRawHeaders();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotEquals(0, response.getHeaders().getSize());
    }

    @Test
    public void service20PutOnlyHeaders() {
        final ResponseBase<HttpBinHeaders, Void> response = createService(Service20.class).putOnlyHeaders("body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20PutBodyAndHeaders() {
        final ResponseBase<HttpBinHeaders, HttpBinJSON> response = createService(Service20.class).putBodyAndHeaders("body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpBinHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers);
        assertTrue(headers.accessControlAllowCredentials());
        assertNotNull(headers.date());
        assertNotEquals(0, (Object) headers.xProcessedTime());
    }

    @Test
    public void service20GetVoidResponse() {
        final Response<Void> response = createService(Service20.class).getVoidResponse();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void service20GetResponseBody() {
        final Response<HttpBinJSON> response = createService(Service20.class).putBody("body string");
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        final HttpBinJSON body = response.getValue();
        assertNotNull(body);
        assertMatchWithHttpOrHttps("localhost/put", body.url());
        assertEquals("body string", body.data());

        final HttpHeaders headers = response.getHeaders();
        assertNotNull(headers);
    }

    @Host("http://localhost")
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
        @Get("http://localhost/bytes/100")
        @ExpectedResponses({200})
        byte[] getBytes100();
    }

    @Test
    public void service21GetBytes100() {
        final byte[] bytes = createService(Service21.class).getBytes100();

        assertNotNull(bytes);
        assertEquals(100, bytes.length);
    }

    @Host("http://localhost")
    @ServiceInterface(name = "DownloadService")
    interface DownloadService {
        @Get("/bytes/30720")
        StreamResponse getBytes();

        @Get("/bytes/30720")
        Flux<ByteBuffer> getBytesFlux();
    }

    @Test
    public void simpleDownloadTest() {
        try (StreamResponse response = createService(DownloadService.class).getBytes()) {
            StepVerifier.create(response.getValue().map(ByteBuffer::remaining).reduce(0, Integer::sum))
                .assertNext(count -> assertEquals(30720, count))
                .verifyComplete();
        }
    }

    @Test
    public void rawFluxDownloadTest() {
        StepVerifier.create(createService(DownloadService.class).getBytesFlux()
            .map(ByteBuffer::remaining).reduce(0, Integer::sum))
            .assertNext(count -> assertEquals(30720, count))
            .verifyComplete();
    }

    @Host("http://localhost")
    @ServiceInterface(name = "FluxUploadService")
    interface FluxUploadService {
        @Put("/put")
        Response<HttpBinJSON> put(@BodyParam("text/plain") Flux<ByteBuffer> content,
            @HeaderParam("Content-Length") long contentLength);
    }

    @Test
    public void fluxUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        Flux<ByteBuffer> stream = FluxUtil.readFile(AsynchronousFileChannel.open(filePath));

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new PortPolicy(getWireMockPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();
        //
        Response<HttpBinJSON> response = RestProxy
            .create(FluxUploadService.class, httpPipeline, SERIALIZER).put(stream, Files.size(filePath));

        assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
    }

    @Test
    public void segmentUploadTest() throws Exception {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("upload.txt").toURI());
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
        Response<HttpBinJSON> response = createService(FluxUploadService.class)
            .put(FluxUtil.readFile(fileChannel, 4, 15), 15);

        assertEquals("quick brown fox", response.getValue().data());
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("/")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Test
    public void service22GetBytes() {
        final byte[] bytes = createService(Service22.class).getBytes("http://localhost/bytes/27");
        assertNotNull(bytes);
        assertEquals(27, bytes.length);
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service23")
    interface Service23 {
        @Get("bytes/28")
        byte[] getBytes();
    }

    @Test
    public void service23GetBytes() {
        final byte[] bytes = createService(Service23.class).getBytes();

        assertNotNull(bytes);
        assertEquals(28, bytes.length);
    }

    @Host("http://localhost")
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
        final HttpHeaders resultHeaders = new HttpHeaders().setAll(result.headers());
        assertEquals("GHIJ", resultHeaders.getValue("ABCDEF"));
        assertEquals("45", resultHeaders.getValue("ABC123"));
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        HttpBinFormDataJSON postForm(@FormParam("custname") String name, @FormParam("custtel") String telephone,
            @FormParam("custemail") String email, @FormParam("size") PizzaSize size,
            @FormParam("toppings") List<String> toppings);

        @Post("post")
        HttpBinFormDataJSON postEncodedForm(@FormParam("custname") String name, @FormParam("custtel") String telephone,
            @FormParam(value = "custemail", encoded = true) String email, @FormParam("size") PizzaSize size,
            @FormParam("toppings") List<String> toppings);
    }

    @Test
    public void postUrlForm() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJSON response = service.postForm("Foo", "123", "foo@bar.com", PizzaSize.LARGE,
            Arrays.asList("Bacon", "Onion"));
        assertNotNull(response);
        assertNotNull(response.form());
        assertEquals("Foo", response.form().customerName());
        assertEquals("123", response.form().customerTelephone());
        assertEquals("foo%40bar.com", response.form().customerEmail());
        assertEquals(PizzaSize.LARGE, response.form().pizzaSize());

        assertEquals(2, response.form().toppings().size());
        assertEquals("Bacon", response.form().toppings().get(0));
        assertEquals("Onion", response.form().toppings().get(1));
    }

    @Test
    public void postUrlFormEncoded() {
        Service26 service = createService(Service26.class);
        HttpBinFormDataJSON response = service.postEncodedForm("Foo", "123", "foo@bar.com", PizzaSize.LARGE,
            Arrays.asList("Bacon", "Onion"));
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
            .policies(new PortPolicy(getWireMockPort(), true))
            .httpClient(httpClient)
            .build();

        return RestProxy.create(serviceClass, httpPipeline, SERIALIZER);
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
        fail("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }

    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
}

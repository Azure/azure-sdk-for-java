// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.implementation.serializer.HttpResponseHeaderDecoderTests.MockHeaders;
import com.azure.core.util.mocking.MockSerializerAdapter;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GeneratedCodeUtilsTests {
    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7 })
    public void responseConsumptionControls(int controls) {
        Context context = GeneratedCodeUtils.createRequestContext(Context.NONE, null, "GeneratedClient.get",
            (controls & 1) != 0, (controls & 2) != 0, (controls & 4) != 0);

        assertEquals("GeneratedClient.get", context.getData("caller-method").orElse(null));
        assertEquals((controls & 1) != 0 ? Boolean.TRUE : null,
            context.getData("azure-eagerly-read-response").orElse(null));
        assertEquals((controls & 2) != 0 ? Boolean.TRUE : null,
            context.getData("azure-ignore-response-body").orElse(null));
        assertEquals((controls & 4) != 0 ? Boolean.TRUE : null,
            context.getData("azure-eagerly-convert-headers").orElse(null));
    }

    @Test
    public void requestOptionsContextTakesPrecedence() {
        Context callerContext = new Context("shared", "caller").addData("caller-only", "caller");
        Context optionsContext = new Context("shared", "options").addData("options-only", "options");
        RequestOptions options = new RequestOptions().setContext(optionsContext);

        Context context = GeneratedCodeUtils.createRequestContext(callerContext, options, "GeneratedClient.get", false,
            false, false);

        assertEquals("options", context.getData("shared").orElse(null));
        assertEquals("caller", context.getData("caller-only").orElse(null));
        assertEquals("options", context.getData("options-only").orElse(null));
        assertEquals("caller", callerContext.getData("shared").orElse(null));
        assertFalse(callerContext.getData("caller-method").isPresent());
    }

    @Test
    public void existingResponseControlsAreRetained() {
        RequestOptions options = new RequestOptions().setContext(new Context("azure-eagerly-read-response", true));

        Context context = GeneratedCodeUtils.createRequestContext(Context.NONE, options, "GeneratedClient.get", false,
            false, false);

        assertEquals(Boolean.TRUE, context.getData("azure-eagerly-read-response").orElse(null));
    }

    @Test
    public void nullCallerContextUsesEmptyContext() {
        Context context
            = GeneratedCodeUtils.createRequestContext(null, null, "GeneratedClient.get", false, false, false);

        assertEquals("GeneratedClient.get", context.getData("caller-method").orElse(null));
        assertFalse(Context.NONE.getData("caller-method").isPresent());
    }

    @Test
    public void nullCallerMethodIsRejected() {
        assertThrows(NullPointerException.class,
            () -> GeneratedCodeUtils.createRequestContext(Context.NONE, null, null, false, false, false));
    }

    @Test
    public void pathParametersUseRestProxyEscaping() {
        assertEquals("a%20b%2Fc%3Fd&x=+%25", GeneratedCodeUtils.encodePathParameter("a b/c?d&x=+%"));
        assertEquals("caf%C3%A9", GeneratedCodeUtils.encodePathParameter("caf\u00e9"));
    }

    @Test
    public void queryParametersUseRestProxyEscaping() {
        assertEquals("a%20b/c?d%26x%3D%2B%25", GeneratedCodeUtils.encodeQueryParameter("a b/c?d&x=+%"));
        assertEquals("caf%C3%A9", GeneratedCodeUtils.encodeQueryParameter("caf\u00e9"));
    }

    @Test
    public void nullParameterValuesAreRejected() {
        assertThrows(NullPointerException.class, () -> GeneratedCodeUtils.encodePathParameter(null));
        assertThrows(NullPointerException.class, () -> GeneratedCodeUtils.encodeQueryParameter(null));
    }

    @Test
    public void repeatedQueryParametersAreAppended() {
        UrlBuilder urlBuilder = UrlBuilder.parse("https://example.com?existing=value");

        GeneratedCodeUtils.addQueryParameter(urlBuilder, "$filter", true, "a b", true);
        GeneratedCodeUtils.addQueryParameter(urlBuilder, "$filter", true, "a+b", true);

        assertEquals("https://example.com?existing=value&%24filter=a%20b&%24filter=a%2Bb", urlBuilder.toString());
    }

    @Test
    public void alreadyEncodedQueryParametersArePreserved() {
        UrlBuilder urlBuilder = UrlBuilder.parse("https://example.com");

        GeneratedCodeUtils.addQueryParameter(urlBuilder, "%24filter", false, "a%20b", false);

        assertEquals("https://example.com?%24filter=a%20b", urlBuilder.toString());
    }

    @Test
    public void nullQueryParameterIsOmitted() {
        UrlBuilder urlBuilder = UrlBuilder.parse("https://example.com?existing=value");

        GeneratedCodeUtils.addQueryParameter(urlBuilder, "omitted", true, null, true);

        assertEquals("https://example.com?existing=value", urlBuilder.toString());
    }

    @Test
    public void nullQueryBuilderOrNameIsRejected() {
        assertThrows(NullPointerException.class,
            () -> GeneratedCodeUtils.addQueryParameter(null, "name", true, "value", true));
        assertThrows(NullPointerException.class,
            () -> GeneratedCodeUtils.addQueryParameter(new UrlBuilder(), null, true, "value", true));
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 4, 5 })
    public void synchronousRequestBodyLengthIsValidated(int contentLength) {
        HttpRequest request = requestWithBody(contentLength);
        if (contentLength == 4) {
            assertSame(request, GeneratedCodeUtils.validateRequestBodyLength(request));
        } else {
            assertThrows(UnexpectedLengthException.class, () -> GeneratedCodeUtils.validateRequestBodyLength(request));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 4, 5 })
    public void asynchronousRequestBodyLengthIsValidated(int contentLength) {
        HttpRequest request = requestWithBody(contentLength);
        if (contentLength == 4) {
            StepVerifier.create(GeneratedCodeUtils.validateRequestBodyLengthAsync(request))
                .assertNext(validated -> assertSame(request, validated))
                .verifyComplete();
        } else {
            StepVerifier.create(GeneratedCodeUtils.validateRequestBodyLengthAsync(request))
                .verifyError(UnexpectedLengthException.class);
        }
    }

    @Test
    public void streamingBodyLengthIsValidatedDuringConsumption() {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, "https://example.com").setHeader(HttpHeaderName.CONTENT_LENGTH, "3")
                .setBody(BinaryData.fromStream(new ByteArrayInputStream("body".getBytes(StandardCharsets.UTF_8))));

        GeneratedCodeUtils.validateRequestBodyLength(request);

        assertThrows(UnexpectedLengthException.class, () -> request.getBodyAsBinaryData().toBytes());
    }

    @Test
    public void emptyRequestDoesNotRequireContentLength() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com");

        assertSame(request, GeneratedCodeUtils.validateRequestBodyLength(request));
        StepVerifier.create(GeneratedCodeUtils.validateRequestBodyLengthAsync(request))
            .assertNext(validated -> assertSame(request, validated))
            .verifyComplete();
    }

    @Test
    public void nullRequestForLengthValidationIsRejected() {
        assertThrows(NullPointerException.class, () -> GeneratedCodeUtils.validateRequestBodyLength(null));
        assertThrows(NullPointerException.class, () -> GeneratedCodeUtils.validateRequestBodyLengthAsync(null));
    }

    @Test
    public void jsonResponseBodyIsDecoded() {
        byte[] body = "\"decoded\"".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 200, body)) {
            assertEquals("decoded",
                GeneratedCodeUtils.decodeResponseBody(response, body, String.class, null, SERIALIZER));
        }
    }

    @Test
    public void parameterizedResponseBodyIsDecoded() {
        byte[] body = "[\"first\",\"second\"]".getBytes(StandardCharsets.UTF_8);
        Type bodyType = new TypeReference<List<String>>() {
        }.getJavaType();
        try (HttpResponse response = response(HttpMethod.GET, 200, body)) {
            List<String> decoded = GeneratedCodeUtils.decodeResponseBody(response, body, bodyType, null, SERIALIZER);

            assertEquals(Arrays.asList("first", "second"), decoded);
        }
    }

    @Test
    public void wireDateIsConverted() {
        byte[] body = "\"Tue, 03 Jun 2008 11:05:30 GMT\"".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 200, body)) {
            OffsetDateTime decoded = GeneratedCodeUtils.decodeResponseBody(response, body, OffsetDateTime.class,
                DateTimeRfc1123.class, SERIALIZER);

            assertEquals(OffsetDateTime.of(2008, 6, 3, 11, 5, 30, 0, ZoneOffset.UTC), decoded);
        }
    }

    @Test
    public void wireBase64IsConverted() {
        byte[] body = "\"YWJjZA\"".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 200, body)) {
            byte[] decoded
                = GeneratedCodeUtils.decodeResponseBody(response, body, byte[].class, Base64Url.class, SERIALIZER);

            assertArrayEquals("abcd".getBytes(StandardCharsets.UTF_8), decoded);
        }
    }

    @Test
    public void headResponseDoesNotDeserializeBody() {
        byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.HEAD, 200, body)) {
            assertNull(GeneratedCodeUtils.decodeResponseBody(response, body, String.class, null, SERIALIZER));
        }
    }

    @Test
    public void emptyAndStreamingBodiesAreNotDeserialized() {
        try (HttpResponse response = response(HttpMethod.GET, 200, new byte[0])) {
            assertNull(GeneratedCodeUtils.decodeResponseBody(response, new byte[0], String.class, null, SERIALIZER));
            assertNull(GeneratedCodeUtils.decodeResponseBody(response, null, InputStream.class, null, SERIALIZER));
        }
    }

    @Test
    public void malformedBodyRetainsResponseAndCause() {
        byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 200, body)) {
            HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> GeneratedCodeUtils.decodeResponseBody(response, body, String.class, null, SERIALIZER));

            assertSame(response, exception.getResponse());
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Test
    public void responseContentTypeSelectsXmlEncoding() {
        byte[] body = "<value>decoded</value>".getBytes(StandardCharsets.UTF_8);
        SerializerAdapter serializer = new MockSerializerAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) {
                assertArrayEquals(body, bytes);
                assertSame(String.class, type);
                assertEquals(SerializerEncoding.XML, encoding);
                return (T) "decoded";
            }
        };
        try (HttpResponse response = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"), 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"), body)) {
            assertEquals("decoded",
                GeneratedCodeUtils.decodeResponseBody(response, body, String.class, null, serializer));
        }
    }

    @Test
    public void typedResponseHeadersAreDecoded() {
        try (HttpResponse response = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"), 200,
            new HttpHeaders().set(HttpHeaderName.fromString("mock-a"), "value"))) {
            MockHeaders headers = GeneratedCodeUtils.decodeResponseHeaders(response, MockHeaders.class, SERIALIZER);

            assertEquals(Collections.singletonMap("a", "value"), headers.getHeaderCollection());
            assertNull(GeneratedCodeUtils.decodeResponseHeaders(response, null, SERIALIZER));
        }
    }

    @Test
    public void headerDeserializationFailureRetainsResponseAndCause() {
        IOException failure = new IOException("invalid headers");
        SerializerAdapter serializer = new MockSerializerAdapter() {
            @Override
            public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
                throw failure;
            }
        };
        try (HttpResponse response = response(HttpMethod.GET, 200, new byte[0])) {
            HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> GeneratedCodeUtils.decodeResponseHeaders(response, MockHeaders.class, serializer));

            assertSame(response, exception.getResponse());
            assertSame(failure, exception.getCause());
        }
    }

    @Test
    public void unexpectedResponseUsesSelectedExceptionType() {
        byte[] body = "\"error\"".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 401, body)) {
            HttpResponseException exception = GeneratedCodeUtils.createUnexpectedResponseException(response, body,
                ClientAuthenticationException.class, SERIALIZER);

            assertInstanceOf(ClientAuthenticationException.class, exception);
            assertSame(response, exception.getResponse());
            assertEquals("error", exception.getValue());
            assertEquals("Status code 401, \"\"error\"\"", exception.getMessage());
        }
    }

    @Test
    public void customUnexpectedResponseExceptionRetainsTypedBody() {
        byte[] body = "\"error\"".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 400, body)) {
            HttpResponseException exception = GeneratedCodeUtils.createUnexpectedResponseException(response, body,
                StringResponseException.class, SERIALIZER);

            StringResponseException typedException = assertInstanceOf(StringResponseException.class, exception);
            assertSame(response, typedException.getResponse());
            assertEquals("error", typedException.getValue());
        }
    }

    @Test
    public void unexpectedResponseDefaultsToHttpResponseException() {
        try (HttpResponse response = response(HttpMethod.GET, 400, new byte[0])) {
            HttpResponseException exception
                = GeneratedCodeUtils.createUnexpectedResponseException(response, null, null, SERIALIZER);

            assertEquals(HttpResponseException.class, exception.getClass());
            assertSame(response, exception.getResponse());
            assertNull(exception.getValue());
            assertEquals("Status code 400, (empty body)", exception.getMessage());
        }
    }

    @Test
    public void malformedErrorBodyRetainsResponseAndCause() {
        byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
        try (HttpResponse response = response(HttpMethod.GET, 400, body)) {
            HttpResponseException exception = GeneratedCodeUtils.createUnexpectedResponseException(response, body,
                ClientAuthenticationException.class, SERIALIZER);

            assertEquals(HttpResponseException.class, exception.getClass());
            assertSame(response, exception.getResponse());
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Test
    public void responseOwnershipIsRetainedByCaller() {
        AtomicBoolean closed = new AtomicBoolean();
        byte[] body = "\"decoded\"".getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"), 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"), body) {
            @Override
            public void close() {
                closed.set(true);
                super.close();
            }
        };
        try {
            GeneratedCodeUtils.decodeResponseBody(response, body, String.class, null, SERIALIZER);
            GeneratedCodeUtils.decodeResponseHeaders(response, null, SERIALIZER);
            GeneratedCodeUtils.createUnexpectedResponseException(response, body, null, SERIALIZER);

            assertFalse(closed.get());
        } finally {
            response.close();
        }
    }

    @Test
    public void internalRequestOptionsAccessorsRemainPackagePrivate() throws NoSuchMethodException {
        assertEquals(0, RequestOptions.class.getDeclaredMethod("getRequestCallback").getModifiers()
            & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
        assertEquals(0, RequestOptions.class.getDeclaredMethod("getErrorOptions").getModifiers()
            & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
    }

    @Test
    public void publicSignaturesDoNotExposeImplementationTypes() {
        Arrays.stream(GeneratedCodeUtils.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .forEach(method -> {
                assertFalse(method.getGenericReturnType().getTypeName().contains("com.azure.core.implementation"));
                Arrays.stream(method.getGenericParameterTypes())
                    .forEach(type -> assertFalse(type.getTypeName().contains("com.azure.core.implementation")));
            });
    }

    private static HttpResponse response(HttpMethod method, int statusCode, byte[] body) {
        return new MockHttpResponse(new HttpRequest(method, "https://example.com"), statusCode,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"), body);
    }

    private static HttpRequest requestWithBody(int contentLength) {
        return new HttpRequest(HttpMethod.POST, "https://example.com").setBody(BinaryData.fromString("body"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public static final class StringResponseException extends HttpResponseException {
        private static final long serialVersionUID = 1L;

        public StringResponseException(String message, HttpResponse response, String value) {
            super(message, response, value);
        }

        @Override
        public String getValue() {
            return (String) super.getValue();
        }
    }
}

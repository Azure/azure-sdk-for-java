// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.v2.exception.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.rest.Page;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.ResponseBase;
import com.azure.core.v2.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.util.Base64Url;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import com.azure.core.v2.util.IterableStream;
import com.azure.core.v2.util.mocking.MockHttpResponseDecodeData;
import com.azure.core.v2.util.mocking.MockSerializerAdapter;
import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link HttpResponseBodyDecoder}.
 */
public class HttpResponseBodyDecoderTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    private static final HttpRequest GET_REQUEST = new HttpRequest(HttpMethod.GET, "https://localhost");
    private static final HttpRequest HEAD_REQUEST = new HttpRequest(HttpMethod.HEAD, "https://localhost");

    @ParameterizedTest
    @MethodSource("invalidHttpResponseSupplier")
    public void invalidHttpResponse(Response<?> response) {
        assertThrows(NullPointerException.class,
            () -> HttpResponseBodyDecoder.decodeByteArray(null, response, null, null));

    }

    private static Stream<Arguments> invalidHttpResponseSupplier() {
        return Stream.of(
            // Null response.
            Arguments.of((HttpResponse) null),

            // Response without a request.
            Arguments.of(new MockHttpResponse(null, 200)),

            // Response with a request that is missing the HttpMethod.
            Arguments.of(new MockHttpResponse(new HttpRequest(null, "https://example.com"), 200)));
    }

    @ParameterizedTest
    @MethodSource("errorResponseSupplier")
    public void errorResponse(HttpResponse httpResponse, HttpResponseDecodeData decodeData, boolean isEmpty,
        Object expected) {
        StepVerifier.FirstStep<Object> firstStep = StepVerifier.create(httpResponse.getBodyAsByteArray()
            .mapNotNull(body -> HttpResponseBodyDecoder.decodeByteArray(body, httpResponse, ADAPTER, decodeData)));

        if (isEmpty) {
            firstStep.verifyComplete();
        } else {
            firstStep.assertNext(actual -> assertEquals(expected, actual)).verifyComplete();
        }
    }

    private static Stream<Arguments> errorResponseSupplier() {
        UnexpectedExceptionInformation exceptionInformation
            = new MockUnexpectedExceptionInformation(HttpResponseException.class, String.class);

        HttpResponseDecodeData noExpectedStatusCodes = new MockHttpResponseDecodeData(exceptionInformation);
        HttpResponseDecodeData expectedStatusCodes = new MockHttpResponseDecodeData(202, exceptionInformation);

        HttpResponse emptyResponse = new MockHttpResponse(GET_REQUEST, 300, (Object) null);
        Response<?> response = new MockHttpResponse(GET_REQUEST, 300, "expected");
        HttpResponse wrongGoodResponse = new MockHttpResponse(GET_REQUEST, 200, "good response");

        return Stream.of(Arguments.of(emptyResponse, noExpectedStatusCodes, true, null),
            Arguments.of(emptyResponse, expectedStatusCodes, true, null),
            Arguments.of(response, noExpectedStatusCodes, false, "expected"),
            Arguments.of(response, expectedStatusCodes, false, "expected"),
            Arguments.of(wrongGoodResponse, expectedStatusCodes, false, "good response"),

            // Improperly formatted JSON string causes MalformedValueException.
            Arguments.of(emptyResponse, noExpectedStatusCodes, true, null));
    }

    @Test
    public void ioExceptionInErrorDeserializationReturnsException() {
        SerializerAdapter ioExceptionThrower = new MockSerializerAdapter() {
            @Override
            public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
                throw new IOException();
            }
        };

        HttpResponseDecodeData noExpectedStatusCodes
            = new MockHttpResponseDecodeData(new UnexpectedExceptionInformation(HttpResponseException.class));

        Response<?> response = new MockHttpResponse(GET_REQUEST, 300, new HttpHeaders(), new byte[1024]);

        assertInstanceOf(IOException.class, HttpResponseBodyDecoder.decodeByteArray(new byte[1024], response,
            ioExceptionThrower, noExpectedStatusCodes));
    }

    @Test
    public void headRequestReturnsEmpty() {
        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200);

        Response<?> response = new MockHttpResponse(HEAD_REQUEST, 200);
        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, ADAPTER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("nonDecodableResponseSupplier")
    public void nonDecodableResponse(HttpResponseDecodeData decodeData) {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200);

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, ADAPTER, decodeData));
    }

    private static Stream<Arguments> nonDecodableResponseSupplier() {
        // Types that will cause a response to be non decodable.
        HttpResponseDecodeData nullReturnType = new MockHttpResponseDecodeData(200, null, false);

        ParameterizedType fluxByteBuffer = mockParameterizedType(Flux.class, ByteBuffer.class);
        HttpResponseDecodeData fluxByteBufferReturnType = new MockHttpResponseDecodeData(200, fluxByteBuffer, false);

        ParameterizedType monoByteArray = mockParameterizedType(Mono.class, byte[].class);
        HttpResponseDecodeData monoByteArrayReturnType = new MockHttpResponseDecodeData(200, monoByteArray, false);

        ParameterizedType voidTypeResponse = mockParameterizedType(ResponseBase.class, int.class, Void.TYPE);
        HttpResponseDecodeData voidTypeResponseReturnType
            = new MockHttpResponseDecodeData(200, voidTypeResponse, false);

        ParameterizedType voidClassResponse = mockParameterizedType(ResponseBase.class, int.class, void.class);
        HttpResponseDecodeData voidClassResponseReturnType
            = new MockHttpResponseDecodeData(200, voidClassResponse, false);

        return Stream.of(Arguments.of(nullReturnType), Arguments.of(fluxByteBufferReturnType),
            Arguments.of(monoByteArrayReturnType), Arguments.of(voidTypeResponseReturnType),
            Arguments.of(voidClassResponseReturnType));
    }

    @Test
    public void emptyResponseReturnsMonoEmpty() {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);

        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, true);

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, ADAPTER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("decodableResponseSupplier")
    public void decodableResponse(Response<?> response, HttpResponseDecodeData decodeData, Object expected) {
        StepVerifier
            .create(response.getBodyAsByteArray()
                .mapNotNull(bytes -> HttpResponseBodyDecoder.decodeByteArray(bytes, response, ADAPTER, decodeData)))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    private static Stream<Arguments> decodableResponseSupplier() {
        HttpResponseDecodeData stringDecodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);
        HttpResponse stringResponse = new MockHttpResponse(GET_REQUEST, 200, "hello");

        HttpResponseDecodeData offsetDateTimeDecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, OffsetDateTime.class, true);
        OffsetDateTime offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);
        HttpResponse offsetDateTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        HttpResponseDecodeData dateTimeRfc1123DecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, DateTimeRfc1123.class, true);
        DateTimeRfc1123 dateTimeRfc1123Now = new DateTimeRfc1123(offsetDateTimeNow);
        HttpResponse dateTimeRfc1123Response = new MockHttpResponse(GET_REQUEST, 200, dateTimeRfc1123Now);

        HttpResponseDecodeData unixTimeDecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, OffsetDateTime.class, true);
        HttpResponse unixTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        ParameterizedType stringList = mockParameterizedType(List.class, String.class);
        HttpResponseDecodeData stringListDecodeData
            = new MockHttpResponseDecodeData(200, stringList, String.class, true);
        List<String> list = Arrays.asList("hello", "azure");
        HttpResponse stringListResponse = new MockHttpResponse(GET_REQUEST, 200, list);

        ParameterizedType mapStringString = mockParameterizedType(Map.class, String.class, String.class);
        HttpResponseDecodeData mapStringStringDecodeData
            = new MockHttpResponseDecodeData(200, mapStringString, String.class, true);
        Map<String, String> map = Collections.singletonMap("hello", "azure");
        HttpResponse mapStringStringResponse = new MockHttpResponse(GET_REQUEST, 200, map);

        return Stream.of(Arguments.of(stringResponse, stringDecodeData, "hello"),
            Arguments.of(offsetDateTimeResponse, offsetDateTimeDecodeData, offsetDateTimeNow),
            Arguments.of(dateTimeRfc1123Response, dateTimeRfc1123DecodeData,
                new DateTimeRfc1123(dateTimeRfc1123Now.toString()).getDateTime()),
            Arguments.of(unixTimeResponse, unixTimeDecodeData, offsetDateTimeNow),
            Arguments.of(stringListResponse, stringListDecodeData, list),
            Arguments.of(mapStringStringResponse, mapStringStringDecodeData, map));
    }

    @Test
    public void decodeListBase64UrlResponse() {
        ParameterizedType parameterizedType = mockParameterizedType(List.class, byte[].class);
        HttpResponseDecodeData decodeData
            = new MockHttpResponseDecodeData(200, parameterizedType, Base64Url.class, true);

        List<Base64Url> base64Urls = Arrays.asList(new Base64Url("base"), new Base64Url("64"));
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, base64Urls);

        StepVerifier
            .create(response.getBodyAsByteArray()
                .mapNotNull(body -> HttpResponseBodyDecoder.decodeByteArray(body, response, ADAPTER, decodeData)))
            .assertNext(actual -> {
                assertTrue(actual instanceof List);
                @SuppressWarnings("unchecked")
                List<byte[]> decoded = (List<byte[]>) actual;
                assertEquals(2, decoded.size());
                assertArraysEqual(base64Urls.get(0).decodedBytes(), decoded.get(0));
                assertArraysEqual(base64Urls.get(1).decodedBytes(), decoded.get(1));
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void decodePageResponse() {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, new Page<String>() {
            @Override
            public IterableStream<String> getElements() {
                return IterableStream.of(null);
            }

            @Override
            public String getContinuationToken() {
                return null;
            }
        });

        HttpResponseDecodeData pageDecodeData = new MockHttpResponseDecodeData(200, String.class, Page.class, true);

        HttpResponseDecodeData itemPageDecodeData
            = new MockHttpResponseDecodeData(200, String.class, ItemPage.class, true);

        StepVerifier
            .create(response.getBodyAsByteArray()
                .mapNotNull(body -> HttpResponseBodyDecoder.decodeByteArray(body, response, ADAPTER, pageDecodeData)))
            .assertNext(actual -> {
                assertTrue(actual instanceof Page);
                Page<String> page = (Page<String>) actual;
                assertFalse(page.getElements().iterator().hasNext());
                assertNull(page.getContinuationToken());
            })
            .verifyComplete();

        StepVerifier.create(response.getBodyAsByteArray()
            .mapNotNull(body -> HttpResponseBodyDecoder.decodeByteArray(body, response, ADAPTER, itemPageDecodeData)))
            .assertNext(actual -> {
                assertTrue(actual instanceof Page);
                Page<String> page = (Page<String>) actual;
                assertFalse(page.getElements().iterator().hasNext());
                assertNull(page.getContinuationToken());
            })
            .verifyComplete();
    }

    @Test
    public void malformedBodyReturnsError() {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);

        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);

        assertThrows(HttpResponseException.class, () -> HttpResponseBodyDecoder
            .decodeByteArray("malformed JSON string".getBytes(StandardCharsets.UTF_8), response, ADAPTER, decodeData));
    }

    @Test
    public void ioExceptionReturnsError() {
        byte[] body = "valid JSON string".getBytes(StandardCharsets.UTF_8);
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, new HttpHeaders(), body);

        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);

        SerializerAdapter serializer = new MockSerializerAdapter() {
            @Override
            public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
                throw new IOException();
            }
        };

        assertThrows(HttpResponseException.class,
            () -> HttpResponseBodyDecoder.decodeByteArray(body, response, serializer, decodeData));
    }

    @ParameterizedTest
    @MethodSource("decodeTypeSupplier")
    public void decodeType(Response<?> response, HttpResponseDecodeData data, Type expected) {
        assertEquals(expected, HttpResponseBodyDecoder.decodedType(response, data));
    }

    private static Stream<Arguments> decodeTypeSupplier() {
        HttpResponse badResponse = new MockHttpResponse(GET_REQUEST, 400);
        HttpResponse headResponse = new MockHttpResponse(HEAD_REQUEST, 200);
        HttpResponse getResponse = new MockHttpResponse(GET_REQUEST, 200);

        HttpResponseDecodeData badResponseData
            = new MockHttpResponseDecodeData(-1, new UnexpectedExceptionInformation(HttpResponseException.class));

        HttpResponseDecodeData nonDecodable = new MockHttpResponseDecodeData(200, void.class, false);

        HttpResponseDecodeData stringReturn = new MockHttpResponseDecodeData(200, String.class, true);

        ParameterizedType monoString = mockParameterizedType(Mono.class, String.class);
        HttpResponseDecodeData monoStringReturn = new MockHttpResponseDecodeData(200, monoString, true);

        ParameterizedType responseString = mockParameterizedType(Response.class, String.class);
        HttpResponseDecodeData responseStringReturn = new MockHttpResponseDecodeData(200, responseString, true);

        HttpResponseDecodeData headDecodeData = new MockHttpResponseDecodeData(200, null, false);
        return Stream.of(Arguments.of(badResponse, badResponseData, Object.class),
            Arguments.of(headResponse, headDecodeData, null), Arguments.of(getResponse, nonDecodable, null),
            Arguments.of(getResponse, stringReturn, String.class),
            Arguments.of(getResponse, monoStringReturn, String.class),
            Arguments.of(getResponse, responseStringReturn, String.class));
    }

    private static ParameterizedType mockParameterizedType(Type rawType, Type... actualTypeArguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return actualTypeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    private static final class MockUnexpectedExceptionInformation extends UnexpectedExceptionInformation {
        private final Class<?> exceptionBodyType;

        /**
         * Creates an UnexpectedExceptionInformation object with the given exception type and expected response body.
         *
         * @param exceptionType Exception type to be thrown.
         */
        MockUnexpectedExceptionInformation(Class<? extends HttpResponseException> exceptionType,
            Class<?> exceptionBodyType) {
            super(exceptionType);
            this.exceptionBodyType = exceptionBodyType;
        }

        @Override
        public Class<? extends HttpResponseException> getExceptionType() {
            return super.getExceptionType();
        }

        @Override
        public Class<?> getExceptionBodyType() {
            return exceptionBodyType;
        }
    }
}

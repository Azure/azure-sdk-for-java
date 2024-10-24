// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.MockHttpResponseDecodeData;
import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.implementation.http.serializer.HttpResponseBodyDecoder;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.util.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link HttpResponseBodyDecoder}.
 */
public class HttpResponseBodyDecoderTests {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();

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
            Arguments.of((Response<?>) null),

            // Response without a request.
            Arguments.of(new MockHttpResponse(null, 200))
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponseSupplier")
    public void errorResponse(Response<?> response, HttpResponseDecodeData decodeData,
                              boolean isEmpty, Object expected) {
        BinaryData body = response.getBody();
        HttpResponseBodyDecoder.decodeByteArray(body, response, SERIALIZER, decodeData);

        if (!isEmpty) {
            assertEquals(expected.toString(), body.toString());
        }
    }

    private static Stream<Arguments> errorResponseSupplier() {
        UnexpectedExceptionInformation exceptionInformation =
            new MockUnexpectedExceptionInformation(null, String.class);

        HttpResponseDecodeData noExpectedStatusCodes = new MockHttpResponseDecodeData(exceptionInformation);
        HttpResponseDecodeData expectedStatusCodes = new MockHttpResponseDecodeData(202, exceptionInformation);

        Response<?> emptyResponse = new MockHttpResponse(GET_REQUEST, 300, (Object) null);
        Response<?> response = new MockHttpResponse(GET_REQUEST, 300, "expected");
        Response<?> wrongGoodResponse = new MockHttpResponse(GET_REQUEST, 200, "good response");

        return Stream.of(
            Arguments.of(emptyResponse, noExpectedStatusCodes, true, null),
            Arguments.of(emptyResponse, expectedStatusCodes, true, null),
            Arguments.of(response, noExpectedStatusCodes, false, "\"expected\""),
            Arguments.of(response, expectedStatusCodes, false, "\"expected\""),
            Arguments.of(wrongGoodResponse, expectedStatusCodes, false, "\"good response\"")
        );
    }

    @Test
    public void exceptionInErrorDeserializationReturnsException() {
        ObjectSerializer ioExceptionThrower = new DefaultJsonSerializer() {
            @Override
            public <T> T deserializeFromBytes(byte[] bytes, Type type) {
                throw new UncheckedIOException(new IOException());
            }
        };

        HttpResponseDecodeData noExpectedStatusCodes = new MockHttpResponseDecodeData(
            new UnexpectedExceptionInformation(null, null));

        Response<?> response = new MockHttpResponse(GET_REQUEST, 300);

        assertInstanceOf(UncheckedIOException.class,
            HttpResponseBodyDecoder.decodeByteArray(null, response, ioExceptionThrower, noExpectedStatusCodes));
    }

    @Test
    public void headRequestReturnsEmpty() {
        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200);

        Response<?> response = new MockHttpResponse(HEAD_REQUEST, 200);
        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("nonDecodableResponseSupplier")
    public void nonDecodableResponse(HttpResponseDecodeData decodeData) {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200);

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    private static Stream<Arguments> nonDecodableResponseSupplier() {
        // Types that will cause a response to be non decodable.
        HttpResponseDecodeData nullReturnType = new MockHttpResponseDecodeData(200, null, false);

        ParameterizedType voidTypeResponse = mockParameterizedType(Response.class, int.class, Void.TYPE);
        HttpResponseDecodeData voidTypeResponseReturnType = new MockHttpResponseDecodeData(200, voidTypeResponse,
            false);

        ParameterizedType voidClassResponse = mockParameterizedType(Response.class, int.class, void.class);
        HttpResponseDecodeData voidClassResponseReturnType = new MockHttpResponseDecodeData(200, voidClassResponse,
            false);

        return Stream.of(
            Arguments.of(nullReturnType),
            Arguments.of(voidTypeResponseReturnType),
            Arguments.of(voidClassResponseReturnType)
        );
    }

    @Test
    public void emptyResponseReturnsNull() {
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);

        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, true);

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("decodableResponseSupplier")
    public void decodableResponse(Response<?> response, HttpResponseDecodeData decodeData, Object expected) {
        BinaryData body = response.getBody();
        Object actual = HttpResponseBodyDecoder.decodeByteArray(body, response, SERIALIZER, decodeData);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> decodableResponseSupplier() {
        HttpResponseDecodeData stringDecodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);
        Response<?> stringResponse = new MockHttpResponse(GET_REQUEST, 200, "hello");

        HttpResponseDecodeData offsetDateTimeDecodeData = new MockHttpResponseDecodeData(200, OffsetDateTime.class,
            OffsetDateTime.class, true);
        OffsetDateTime offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);
        Response<?> offsetDateTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        HttpResponseDecodeData dateTimeRfc1123DecodeData = new MockHttpResponseDecodeData(200, OffsetDateTime.class,
            DateTimeRfc1123.class, true);
        DateTimeRfc1123 dateTimeRfc1123Now = new DateTimeRfc1123(offsetDateTimeNow);
        Response<?> dateTimeRfc1123Response = new MockHttpResponse(GET_REQUEST, 200, dateTimeRfc1123Now);

        HttpResponseDecodeData unixTimeDecodeData = new MockHttpResponseDecodeData(200, OffsetDateTime.class,
            OffsetDateTime.class, true);
        Response<?> unixTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        ParameterizedType stringList = mockParameterizedType(List.class, String.class);
        HttpResponseDecodeData stringListDecodeData = new MockHttpResponseDecodeData(200, stringList, String.class,
            true);
        List<String> list = Arrays.asList("hello", "azure");
        Response<?> stringListResponse = new MockHttpResponse(GET_REQUEST, 200, list);

        ParameterizedType mapStringString = mockParameterizedType(Map.class, String.class, String.class);
        HttpResponseDecodeData mapStringStringDecodeData = new MockHttpResponseDecodeData(200, mapStringString,
            String.class, true);
        Map<String, String> map = Collections.singletonMap("hello", "azure");
        Response<?> mapStringStringResponse = new MockHttpResponse(GET_REQUEST, 200, map);

        return Stream.of(
            Arguments.of(stringResponse, stringDecodeData, "hello"),
            Arguments.of(offsetDateTimeResponse, offsetDateTimeDecodeData, offsetDateTimeNow),
            Arguments.of(dateTimeRfc1123Response, dateTimeRfc1123DecodeData,
                new DateTimeRfc1123(dateTimeRfc1123Now.toString()).getDateTime()),
            Arguments.of(unixTimeResponse, unixTimeDecodeData, offsetDateTimeNow),
            Arguments.of(stringListResponse, stringListDecodeData, list),
            Arguments.of(mapStringStringResponse, mapStringStringDecodeData, map)
        );
    }

    @Test
    public void decodeListBase64UriResponse() {
        ParameterizedType parameterizedType = mockParameterizedType(List.class, byte[].class);
        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, parameterizedType, Base64Uri.class,
            true);

        List<Base64Uri> base64Uris = Arrays.asList(new Base64Uri("base"), new Base64Uri("64"));
        Response<?> response = new MockHttpResponse(GET_REQUEST, 200, base64Uris);

        BinaryData body = response.getBody();
        Object actual = HttpResponseBodyDecoder.decodeByteArray(body, response, SERIALIZER, decodeData);

        assertInstanceOf(List.class, actual);

        @SuppressWarnings("unchecked")
        List<byte[]> decoded = (List<byte[]>) actual;

        assertEquals(2, decoded.size());
        assertArraysEqual(base64Uris.get(0).decodedBytes(), decoded.get(0));
        assertArraysEqual(base64Uris.get(1).decodedBytes(), decoded.get(1));
    }

    @Test
    public void malformedBodyReturnsError() throws IOException {
        try (Response<?> response = new MockHttpResponse(GET_REQUEST, 200, (Object) null)) {
            HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);

            assertThrows(HttpResponseException.class, () ->
                HttpResponseBodyDecoder.decodeByteArray(BinaryData.fromString("malformed JSON string"), response,
                    SERIALIZER, decodeData));
        }
    }

    @ParameterizedTest
    @MethodSource("decodeTypeSupplier")
    public void decodeType(Response<?> response, HttpResponseDecodeData data, Type expected) {
        assertEquals(expected, HttpResponseBodyDecoder.decodedType(response, data));
    }

    private static Stream<Arguments> decodeTypeSupplier() {
        Response<?> badResponse = new MockHttpResponse(GET_REQUEST, 400);
        Response<?> headResponse = new MockHttpResponse(HEAD_REQUEST, 200);
        Response<?> getResponse = new MockHttpResponse(GET_REQUEST, 200);

        HttpResponseDecodeData badResponseData = new MockHttpResponseDecodeData(-1,
            new UnexpectedExceptionInformation(null, null));

        HttpResponseDecodeData nonDecodable = new MockHttpResponseDecodeData(200, void.class, false);

        HttpResponseDecodeData stringReturn = new MockHttpResponseDecodeData(200, String.class, true);

        ParameterizedType responseString = mockParameterizedType(Response.class, String.class);
        HttpResponseDecodeData responseStringReturn = new MockHttpResponseDecodeData(200, responseString, true);

        HttpResponseDecodeData headDecodeData = new MockHttpResponseDecodeData(200, null, false);
        return Stream.of(
            Arguments.of(badResponse, badResponseData, Object.class),
            Arguments.of(headResponse, headDecodeData, null),
            Arguments.of(getResponse, nonDecodable, null),
            Arguments.of(getResponse, stringReturn, String.class),
            Arguments.of(getResponse, responseStringReturn, String.class)
        );
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
        MockUnexpectedExceptionInformation(HttpExceptionType exceptionType, Class<?> exceptionBodyType) {
            super(exceptionType, null);

            this.exceptionBodyType = exceptionBodyType;
        }

        @Override
        public HttpExceptionType getExceptionType() {
            return super.getExceptionType();
        }

        @Override
        public Class<?> getExceptionBodyClass() {
            return exceptionBodyType;
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.http.MockHttpResponseDecodeData;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.CompositeSerializer;
import io.clientcore.core.implementation.http.serializer.HttpResponseBodyDecoder;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.utils.Base64Uri;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.DateTimeRfc1123;
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

import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link HttpResponseBodyDecoder}.
 */
public class HttpResponseBodyDecoderTests {
    private static final CompositeSerializer SERIALIZER = new CompositeSerializer(Arrays.asList(new JsonSerializer()));

    private static final HttpRequest GET_REQUEST
        = new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost");
    private static final HttpRequest HEAD_REQUEST
        = new HttpRequest().setMethod(HttpMethod.HEAD).setUri("https://localhost");

    @ParameterizedTest
    @MethodSource("invalidHttpResponseSupplier")
    public void invalidHttpResponse(Response<BinaryData> response) {
        assertThrows(NullPointerException.class,
            () -> HttpResponseBodyDecoder.decodeByteArray(null, response, null, null));

    }

    private static Stream<Arguments> invalidHttpResponseSupplier() {
        return Stream.of(
            // Null response.
            Arguments.of((Response<BinaryData>) null),

            // Response without a request.
            Arguments.of(new Response<>(null, 200, new HttpHeaders(), BinaryData.empty())));
    }

    @ParameterizedTest
    @MethodSource("errorResponseSupplier")
    public void errorResponse(Response<BinaryData> response, HttpResponseDecodeData decodeData, boolean isEmpty,
        Object expected) {
        BinaryData body = response.getValue();
        HttpResponseBodyDecoder.decodeByteArray(body, response, SERIALIZER, decodeData);

        if (!isEmpty) {
            assertEquals(expected.toString(), body.toString());
        }
    }

    private static Stream<Arguments> errorResponseSupplier() {
        UnexpectedExceptionInformation exceptionInformation = new MockUnexpectedExceptionInformation(String.class);

        HttpResponseDecodeData noExpectedStatusCodes = new MockHttpResponseDecodeData(exceptionInformation);
        HttpResponseDecodeData expectedStatusCodes = new MockHttpResponseDecodeData(202, exceptionInformation);

        Response<BinaryData> emptyResponse = new Response<>(GET_REQUEST, 300, new HttpHeaders(), null);
        Response<BinaryData> response
            = new Response<>(GET_REQUEST, 300, new HttpHeaders(), BinaryData.fromObject("expected"));
        Response<BinaryData> wrongGoodResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject("good response"));

        return Stream.of(Arguments.of(emptyResponse, noExpectedStatusCodes, true, null),
            Arguments.of(emptyResponse, expectedStatusCodes, true, null),
            Arguments.of(response, noExpectedStatusCodes, false, "\"expected\""),
            Arguments.of(response, expectedStatusCodes, false, "\"expected\""),
            Arguments.of(wrongGoodResponse, expectedStatusCodes, false, "\"good response\""));
    }

    @Test
    public void exceptionInErrorDeserializationReturnsException() {
        CompositeSerializer ioExceptionThrower = new CompositeSerializer(Arrays.asList(new JsonSerializer() {
            @Override
            public <T> T deserializeFromBytes(byte[] bytes, Type type) {
                throw new UncheckedIOException(new IOException());
            }
        }));

        HttpResponseDecodeData noExpectedStatusCodes
            = new MockHttpResponseDecodeData(new UnexpectedExceptionInformation(null));

        Response<BinaryData> response = new Response<>(GET_REQUEST, 300, new HttpHeaders(), BinaryData.empty());

        assertInstanceOf(UncheckedIOException.class,
            HttpResponseBodyDecoder.decodeByteArray(null, response, ioExceptionThrower, noExpectedStatusCodes));
    }

    @Test
    public void headRequestReturnsEmpty() {
        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200);

        Response<BinaryData> response = new Response<>(HEAD_REQUEST, 200, new HttpHeaders(), BinaryData.empty());
        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("nonDecodableResponseSupplier")
    public void nonDecodableResponse(HttpResponseDecodeData decodeData) {
        Response<BinaryData> response = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.empty());

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    private static Stream<Arguments> nonDecodableResponseSupplier() {
        // Types that will cause a response to be non decodable.
        HttpResponseDecodeData nullReturnType = new MockHttpResponseDecodeData(200, null, false);

        ParameterizedType voidTypeResponse = CoreUtils.createParameterizedType(Response.class, Void.TYPE);
        HttpResponseDecodeData voidTypeResponseReturnType
            = new MockHttpResponseDecodeData(200, voidTypeResponse, false);

        ParameterizedType voidClassResponse = CoreUtils.createParameterizedType(Response.class, void.class);
        HttpResponseDecodeData voidClassResponseReturnType
            = new MockHttpResponseDecodeData(200, voidClassResponse, false);

        return Stream.of(Arguments.of(nullReturnType), Arguments.of(voidTypeResponseReturnType),
            Arguments.of(voidClassResponseReturnType));
    }

    @Test
    public void emptyResponseReturnsNull() {
        Response<BinaryData> response = new Response<>(GET_REQUEST, 200, new HttpHeaders(), null);

        HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, true);

        assertNull(HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData));
    }

    @ParameterizedTest
    @MethodSource("decodableResponseSupplier")
    public void decodableResponse(Response<BinaryData> response, HttpResponseDecodeData decodeData, Object expected) {
        BinaryData body = response.getValue();
        Object actual = HttpResponseBodyDecoder.decodeByteArray(body, response, SERIALIZER, decodeData);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> decodableResponseSupplier() {
        HttpResponseDecodeData stringDecodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);
        Response<BinaryData> stringResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject("hello"));

        HttpResponseDecodeData offsetDateTimeDecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, OffsetDateTime.class, true);
        OffsetDateTime offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);
        Response<BinaryData> offsetDateTimeResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(offsetDateTimeNow));

        HttpResponseDecodeData dateTimeRfc1123DecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, DateTimeRfc1123.class, true);
        DateTimeRfc1123 dateTimeRfc1123Now = new DateTimeRfc1123(offsetDateTimeNow);
        Response<BinaryData> dateTimeRfc1123Response
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(dateTimeRfc1123Now));

        HttpResponseDecodeData unixTimeDecodeData
            = new MockHttpResponseDecodeData(200, OffsetDateTime.class, OffsetDateTime.class, true);
        Response<BinaryData> unixTimeResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(offsetDateTimeNow));

        ParameterizedType stringList = CoreUtils.createParameterizedType(List.class, String.class);
        HttpResponseDecodeData stringListDecodeData
            = new MockHttpResponseDecodeData(200, stringList, String.class, true);
        List<String> list = Arrays.asList("hello", "world");
        Response<BinaryData> stringListResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(list));

        ParameterizedType mapStringString = CoreUtils.createParameterizedType(Map.class, String.class, String.class);
        HttpResponseDecodeData mapStringStringDecodeData
            = new MockHttpResponseDecodeData(200, mapStringString, String.class, true);
        Map<String, String> map = Collections.singletonMap("hello", "world");
        Response<BinaryData> mapStringStringResponse
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(map));

        return Stream.of(Arguments.of(stringResponse, stringDecodeData, "hello"),
            Arguments.of(offsetDateTimeResponse, offsetDateTimeDecodeData, offsetDateTimeNow),
            Arguments.of(dateTimeRfc1123Response, dateTimeRfc1123DecodeData,
                new DateTimeRfc1123(dateTimeRfc1123Now.toString()).getDateTime()),
            Arguments.of(unixTimeResponse, unixTimeDecodeData, offsetDateTimeNow),
            Arguments.of(stringListResponse, stringListDecodeData, list),
            Arguments.of(mapStringStringResponse, mapStringStringDecodeData, map));
    }

    @Test
    public void decodeListBase64UriResponse() {
        ParameterizedType parameterizedType = CoreUtils.createParameterizedType(List.class, byte[].class);
        HttpResponseDecodeData decodeData
            = new MockHttpResponseDecodeData(200, parameterizedType, Base64Uri.class, true);

        List<Base64Uri> base64Uris = Arrays.asList(new Base64Uri("base"), new Base64Uri("64"));
        Response<BinaryData> response
            = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.fromObject(base64Uris));

        Object actual = HttpResponseBodyDecoder.decodeByteArray(null, response, SERIALIZER, decodeData);

        assertInstanceOf(List.class, actual);

        @SuppressWarnings("unchecked")
        List<byte[]> decoded = (List<byte[]>) actual;

        assertEquals(2, decoded.size());
        assertArraysEqual(base64Uris.get(0).decodedBytes(), decoded.get(0));
        assertArraysEqual(base64Uris.get(1).decodedBytes(), decoded.get(1));
    }

    @Test
    public void malformedBodyReturnsError() throws IOException {
        try (Response<BinaryData> response = new Response<>(GET_REQUEST, 200, new HttpHeaders(), null)) {
            HttpResponseDecodeData decodeData = new MockHttpResponseDecodeData(200, String.class, String.class, true);

            assertThrows(HttpResponseException.class, () -> HttpResponseBodyDecoder
                .decodeByteArray(BinaryData.fromString("malformed JSON string"), response, SERIALIZER, decodeData));
        }
    }

    @ParameterizedTest
    @MethodSource("decodeTypeSupplier")
    public void decodeType(Response<?> response, HttpResponseDecodeData data, Type expected) {
        assertEquals(expected, HttpResponseBodyDecoder.decodedType(response, data));
    }

    private static Stream<Arguments> decodeTypeSupplier() {
        Response<?> badResponse = new Response<>(GET_REQUEST, 400, new HttpHeaders(), BinaryData.empty());
        Response<?> headResponse = new Response<>(HEAD_REQUEST, 200, new HttpHeaders(), BinaryData.empty());
        Response<?> getResponse = new Response<>(GET_REQUEST, 200, new HttpHeaders(), BinaryData.empty());

        HttpResponseDecodeData badResponseData
            = new MockHttpResponseDecodeData(-1, new UnexpectedExceptionInformation(null));

        HttpResponseDecodeData nonDecodable = new MockHttpResponseDecodeData(200, void.class, false);

        HttpResponseDecodeData stringReturn = new MockHttpResponseDecodeData(200, String.class, true);

        ParameterizedType responseString = CoreUtils.createParameterizedType(Response.class, String.class);
        HttpResponseDecodeData responseStringReturn = new MockHttpResponseDecodeData(200, responseString, true);

        HttpResponseDecodeData headDecodeData = new MockHttpResponseDecodeData(200, null, false);
        return Stream.of(Arguments.of(badResponse, badResponseData, Object.class),
            Arguments.of(headResponse, headDecodeData, null), Arguments.of(getResponse, nonDecodable, null),
            Arguments.of(getResponse, stringReturn, String.class),
            Arguments.of(getResponse, responseStringReturn, String.class));
    }

    private static final class MockUnexpectedExceptionInformation extends UnexpectedExceptionInformation {
        private final Class<?> exceptionBodyType;

        /**
         * Creates an UnexpectedExceptionInformation object with the given exception type and expected response body.
         */
        MockUnexpectedExceptionInformation(Class<?> exceptionBodyType) {
            super(null);
            this.exceptionBodyType = exceptionBodyType;
        }

        @Override
        public Class<?> getExceptionBodyClass() {
            return exceptionBodyType;
        }
    }
}

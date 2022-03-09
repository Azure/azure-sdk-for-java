// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.UnixTime;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link HttpResponseBodyDecoder}.
 */
public class HttpResponseBodyDecoderTests {
    private static final HttpRequest GET_REQUEST = new HttpRequest(HttpMethod.GET, "https://localhost");
    private static final HttpRequest HEAD_REQUEST = new HttpRequest(HttpMethod.HEAD, "https://localhost");

    private AutoCloseable openMocks;

    @BeforeEach
    public void prepareForMocking() {
        this.openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void clearMocks() throws Exception {
        openMocks.close();
        Mockito.framework().clearInlineMock(this);
    }

    @ParameterizedTest
    @MethodSource("invalidHttpResponseSupplier")
    public void invalidHttpResponse(HttpResponse response) {
        assertThrows(NullPointerException.class, () -> HttpResponseBodyDecoder.decode(null, response, null, null));

    }

    private static Stream<Arguments> invalidHttpResponseSupplier() {
        return Stream.of(
            // Null response.
            Arguments.of((HttpResponse) null),

            // Response without a request.
            Arguments.of(new MockHttpResponse(null, 200)),

            // Response with a request that is missing the HttpMethod.
            Arguments.of(new MockHttpResponse(new HttpRequest(null, "https://example.com"), 200))
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponseSupplier")
    public void errorResponse(String body, HttpResponse httpResponse, HttpResponseDecodeData decodeData,
        boolean isEmpty, Object expected) {
        StepVerifier.FirstStep<Object> firstStep =
            StepVerifier.create(HttpResponseBodyDecoder.decode(body, httpResponse, new JacksonAdapter(), decodeData));

        if (isEmpty) {
            firstStep.verifyComplete();
        } else {
            firstStep.assertNext(actual -> assertEquals(expected, actual)).verifyComplete();
        }
    }

    private static Stream<Arguments> errorResponseSupplier() {
        UnexpectedExceptionInformation exceptionInformation = mock(UnexpectedExceptionInformation.class);
        when(exceptionInformation.getExceptionBodyType()).thenAnswer(invocation -> String.class);

        HttpResponseDecodeData noExpectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(noExpectedStatusCodes.getUnexpectedException(anyInt())).thenReturn(exceptionInformation);

        HttpResponseDecodeData expectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(expectedStatusCodes.isExpectedResponseStatusCode(202)).thenReturn(true);
        when(expectedStatusCodes.getUnexpectedException(anyInt())).thenReturn(exceptionInformation);

        HttpResponse emptyResponse = new MockHttpResponse(GET_REQUEST, 300, (Object) null);
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 300, "expected");
        HttpResponse wrongGoodResponse = new MockHttpResponse(GET_REQUEST, 200, "good response");

        return Stream.of(
            Arguments.of(null, emptyResponse, noExpectedStatusCodes, true, null),
            Arguments.of(null, emptyResponse, expectedStatusCodes, true, null),
            Arguments.of(null, response, noExpectedStatusCodes, false, "expected"),
            Arguments.of(null, response, expectedStatusCodes, false, "expected"),
            Arguments.of("\"expected\"", emptyResponse, noExpectedStatusCodes, false, "expected"),
            Arguments.of("\"expected\"", emptyResponse, expectedStatusCodes, false, "expected"),
            Arguments.of("\"not expected\"", response, noExpectedStatusCodes, false, "not expected"),
            Arguments.of("\"not expected\"", response, expectedStatusCodes, false, "not expected"),
            Arguments.of(null, wrongGoodResponse, expectedStatusCodes, false, "good response"),
            Arguments.of("\"bad response\"", wrongGoodResponse, expectedStatusCodes, false, "bad response"),

            // Improperly formatted JSON string causes MalformedValueException.
            Arguments.of("expected", emptyResponse, noExpectedStatusCodes, true, null)
        );
    }

    @Test
    public void ioExceptionInErrorDeserializationReturnsEmpty() throws IOException {
        JacksonAdapter ioExceptionThrower = mock(JacksonAdapter.class);
        when(ioExceptionThrower.deserialize((InputStream) any(), any(), any())).thenThrow(IOException.class);

        HttpResponseDecodeData noExpectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(noExpectedStatusCodes.getUnexpectedException(anyInt()))
            .thenReturn(new UnexpectedExceptionInformation(HttpResponseException.class));
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 300);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, ioExceptionThrower, noExpectedStatusCodes))
            .verifyComplete();
    }

    @Test
    public void headRequestReturnsEmpty() {
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponse response = new MockHttpResponse(HEAD_REQUEST, 200);
        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("nonDecodableResponseSupplier")
    public void nonDecodableResponse(HttpResponseDecodeData decodeData) {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
            .verifyComplete();
    }

    private static Stream<Arguments> nonDecodableResponseSupplier() {
        // Types that will cause a response to be non decodable.
        HttpResponseDecodeData nullReturnType = mock(HttpResponseDecodeData.class);
        when(nullReturnType.getReturnType()).thenReturn(null);
        when(nullReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType fluxByteBuffer = mockParameterizedType(Flux.class, ByteBuffer.class);
        HttpResponseDecodeData fluxByteBufferReturnType = mock(HttpResponseDecodeData.class);
        when(fluxByteBufferReturnType.getReturnType()).thenReturn(fluxByteBuffer);
        when(fluxByteBufferReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType monoByteArray = mockParameterizedType(Mono.class, byte[].class);
        HttpResponseDecodeData monoByteArrayReturnType = mock(HttpResponseDecodeData.class);
        when(monoByteArrayReturnType.getReturnType()).thenReturn(monoByteArray);
        when(monoByteArrayReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType voidTypeResponse = mockParameterizedType(ResponseBase.class, int.class, Void.TYPE);
        HttpResponseDecodeData voidTypeResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidTypeResponseReturnType.getReturnType()).thenReturn(voidTypeResponse);
        when(voidTypeResponseReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType voidClassResponse = mockParameterizedType(ResponseBase.class, int.class, void.class);
        HttpResponseDecodeData voidClassResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidClassResponseReturnType.getReturnType()).thenReturn(voidClassResponse);
        when(voidClassResponseReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        return Stream.of(
            Arguments.of(nullReturnType),
            Arguments.of(fluxByteBufferReturnType),
            Arguments.of(monoByteArrayReturnType),
            Arguments.of(voidTypeResponseReturnType),
            Arguments.of(voidClassResponseReturnType)
        );
    }

    @Test
    public void emptyResponseReturnsMonoEmpty() {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("decodableResponseSupplier")
    public void decodableResponse(HttpResponse response, HttpResponseDecodeData decodeData, Object expected) {
        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    private static Stream<Arguments> decodableResponseSupplier() {
        HttpResponseDecodeData stringDecodeData = mock(HttpResponseDecodeData.class);
        when(stringDecodeData.getReturnType()).thenReturn(String.class);
        when(stringDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(stringDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        HttpResponse stringResponse = new MockHttpResponse(GET_REQUEST, 200, "hello");

        HttpResponseDecodeData offsetDateTimeDecodeData = mock(HttpResponseDecodeData.class);
        when(offsetDateTimeDecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(offsetDateTimeDecodeData.getReturnValueWireType()).thenReturn(OffsetDateTime.class);
        when(offsetDateTimeDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        OffsetDateTime offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);
        HttpResponse offsetDateTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        HttpResponseDecodeData dateTimeRfc1123DecodeData = mock(HttpResponseDecodeData.class);
        when(dateTimeRfc1123DecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(dateTimeRfc1123DecodeData.getReturnValueWireType()).thenReturn(DateTimeRfc1123.class);
        when(dateTimeRfc1123DecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        DateTimeRfc1123 dateTimeRfc1123Now = new DateTimeRfc1123(offsetDateTimeNow);
        HttpResponse dateTimeRfc1123Response = new MockHttpResponse(GET_REQUEST, 200, dateTimeRfc1123Now);

        HttpResponseDecodeData unixTimeDecodeData = mock(HttpResponseDecodeData.class);
        when(unixTimeDecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(unixTimeDecodeData.getReturnValueWireType()).thenReturn(UnixTime.class);
        when(unixTimeDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        UnixTime unixTimeNow = new UnixTime(offsetDateTimeNow);
        HttpResponse unixTimeResponse = new MockHttpResponse(GET_REQUEST, 200, unixTimeNow);

        ParameterizedType stringList = mockParameterizedType(List.class, String.class);
        HttpResponseDecodeData stringListDecodeData = mock(HttpResponseDecodeData.class);
        when(stringListDecodeData.getReturnType()).thenReturn(stringList);
        when(stringListDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(stringListDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        List<String> list = Arrays.asList("hello", "azure");
        HttpResponse stringListResponse = new MockHttpResponse(GET_REQUEST, 200, list);

        ParameterizedType mapStringString = mockParameterizedType(Map.class, String.class, String.class);
        HttpResponseDecodeData mapStringStringDecodeData = mock(HttpResponseDecodeData.class);
        when(mapStringStringDecodeData.getReturnType()).thenReturn(mapStringString);
        when(mapStringStringDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(mapStringStringDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        Map<String, String> map = Collections.singletonMap("hello", "azure");
        HttpResponse mapStringStringResponse = new MockHttpResponse(GET_REQUEST, 200, map);

        return Stream.of(
            Arguments.of(stringResponse, stringDecodeData, "hello"),
            Arguments.of(offsetDateTimeResponse, offsetDateTimeDecodeData, offsetDateTimeNow),
            Arguments.of(dateTimeRfc1123Response, dateTimeRfc1123DecodeData,
                new DateTimeRfc1123(dateTimeRfc1123Now.toString()).getDateTime()),
            Arguments.of(unixTimeResponse, unixTimeDecodeData,
                new UnixTime(Long.parseLong(unixTimeNow.toString())).getDateTime()),
            Arguments.of(stringListResponse, stringListDecodeData, list),
            Arguments.of(mapStringStringResponse, mapStringStringDecodeData, map)
        );
    }

    @Test
    public void decodeListBase64UrlResponse() {
        ParameterizedType parameterizedType = mockParameterizedType(List.class, byte[].class);
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(parameterizedType);
        when(decodeData.getReturnValueWireType()).thenReturn(Base64Url.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        List<Base64Url> base64Urls = Arrays.asList(new Base64Url("base"), new Base64Url("64"));
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, base64Urls);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
            .assertNext(actual -> {
                assertTrue(actual instanceof List);
                @SuppressWarnings("unchecked") List<byte[]> decoded = (List<byte[]>) actual;
                assertEquals(2, decoded.size());
                assertArrayEquals(base64Urls.get(0).decodedBytes(), decoded.get(0));
                assertArrayEquals(base64Urls.get(1).decodedBytes(), decoded.get(1));
            }).verifyComplete();
    }

    @Test
    public void decodePageResponse() {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, new Page<String>() {
            @Override
            public IterableStream<String> getElements() {
                return IterableStream.of(null);
            }

            @Override
            public String getContinuationToken() {
                return null;
            }
        });

        HttpResponseDecodeData pageDecodeData = mock(HttpResponseDecodeData.class);
        when(pageDecodeData.getReturnType()).thenReturn(String.class);
        when(pageDecodeData.getReturnValueWireType()).thenReturn(Page.class);
        when(pageDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponseDecodeData itemPageDecodeData = mock(HttpResponseDecodeData.class);
        when(itemPageDecodeData.getReturnType()).thenReturn(String.class);
        when(itemPageDecodeData.getReturnValueWireType()).thenReturn(ItemPage.class);
        when(itemPageDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), pageDecodeData))
            .assertNext(actual -> {
                assertTrue(actual instanceof Page);
                @SuppressWarnings("unchecked") Page<String> page = (Page<String>) actual;
                assertFalse(page.getElements().iterator().hasNext());
                assertNull(page.getContinuationToken());
            }).verifyComplete();

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), itemPageDecodeData))
            .assertNext(actual -> {
                assertTrue(actual instanceof Page);
                @SuppressWarnings("unchecked") Page<String> page = (Page<String>) actual;
                assertFalse(page.getElements().iterator().hasNext());
                assertNull(page.getContinuationToken());
            }).verifyComplete();
    }

    @Test
    public void malformedBodyReturnsError() {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);

        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.getReturnValueWireType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        StepVerifier.create(HttpResponseBodyDecoder
            .decode("malformed JSON string", response, new JacksonAdapter(), decodeData))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void ioExceptionReturnsError() throws IOException {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, "valid JSON string");

        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.getReturnValueWireType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        SerializerAdapter serializer = mock(SerializerAdapter.class);
        when(serializer.deserialize(any(byte[].class), any(), any())).thenThrow(IOException.class);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, serializer, decodeData))
            .verifyError(HttpResponseException.class);
    }

    @ParameterizedTest
    @MethodSource("decodeTypeSupplier")
    public void decodeType(HttpResponse response, HttpResponseDecodeData data, Type expected) {
        assertEquals(expected, HttpResponseBodyDecoder.decodedType(response, data));
    }

    private static Stream<Arguments> decodeTypeSupplier() {
        HttpResponse badResponse = new MockHttpResponse(GET_REQUEST, 400);
        HttpResponse headResponse = new MockHttpResponse(HEAD_REQUEST, 200);
        HttpResponse getResponse = new MockHttpResponse(GET_REQUEST, 200);

        HttpResponseDecodeData badResponseData = mock(HttpResponseDecodeData.class);
        when(badResponseData.getUnexpectedException(anyInt()))
            .thenReturn(new UnexpectedExceptionInformation(HttpResponseException.class));
        when(badResponseData.isExpectedResponseStatusCode(400)).thenReturn(false);

        HttpResponseDecodeData nonDecodable = mock(HttpResponseDecodeData.class);
        when(nonDecodable.getReturnType()).thenReturn(void.class);
        when(nonDecodable.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponseDecodeData stringReturn = mock(HttpResponseDecodeData.class);
        when(stringReturn.getReturnType()).thenReturn(String.class);
        when(stringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType monoString = mockParameterizedType(Mono.class, String.class);
        HttpResponseDecodeData monoStringReturn = mock(HttpResponseDecodeData.class);
        when(monoStringReturn.getReturnType()).thenReturn(monoString);
        when(monoStringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType responseString = mockParameterizedType(Response.class, String.class);
        HttpResponseDecodeData responseStringReturn = mock(HttpResponseDecodeData.class);
        when(responseStringReturn.getReturnType()).thenReturn(responseString);
        when(responseStringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponseDecodeData headDecodeData = mock(HttpResponseDecodeData.class);
        when(headDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        return Stream.of(
            Arguments.of(badResponse, badResponseData, Object.class),
            Arguments.of(headResponse, headDecodeData, null),
            Arguments.of(getResponse, nonDecodable, null),
            Arguments.of(getResponse, stringReturn, String.class),
            Arguments.of(getResponse, monoStringReturn, String.class),
            Arguments.of(getResponse, responseStringReturn, String.class)
        );
    }

    private static ParameterizedType mockParameterizedType(Type rawType, Type... actualTypeArguments) {
        ParameterizedType parameterizedType = mock(ParameterizedType.class);
        when(parameterizedType.getRawType()).thenReturn(rawType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(actualTypeArguments);

        return parameterizedType;
    }

    @ParameterizedTest
    @MethodSource("isReturnTypeDecodableSupplier")
    public void isReturnTypeDecodable(Type returnType, boolean expected) {
        assertEquals(expected, HttpResponseBodyDecoder.isReturnTypeDecodable(returnType));
    }

    private static Stream<Arguments> isReturnTypeDecodableSupplier() {
        return Stream.of(
            // Unknown response type can't be determined to be decode-able.
            Arguments.of(null, false),

            // BinaryData, Byte arrays, ByteBuffers, InputStream, and voids aren't decode-able.
            Arguments.of(BinaryData.class, false),

            Arguments.of(byte[].class, false),

            // Both ByteBuffer and sub-types shouldn't be decode-able.
            Arguments.of(ByteBuffer.class, false),
            Arguments.of(MappedByteBuffer.class, false),

            // Both InputSteam and sub-types shouldn't be decode-able.
            Arguments.of(InputStream.class, false),
            Arguments.of(FileInputStream.class, false),

            Arguments.of(void.class, false),
            Arguments.of(Void.class, false),
            Arguments.of(Void.TYPE, false),

            // Other POJO types are decode-able.
            Arguments.of(JsonPatchDocument.class, true),

            // In addition to the direct types, reactive and Response generic types should be handled.

            // Reactive generics.
            // Mono generics.
            Arguments.of(createParameterizedMono(BinaryData.class), false),
            Arguments.of(createParameterizedMono(byte[].class), false),
            Arguments.of(createParameterizedMono(ByteBuffer.class), false),
            Arguments.of(createParameterizedMono(MappedByteBuffer.class), false),
            Arguments.of(createParameterizedMono(InputStream.class), false),
            Arguments.of(createParameterizedMono(FileInputStream.class), false),
            Arguments.of(createParameterizedMono(void.class), false),
            Arguments.of(createParameterizedMono(Void.class), false),
            Arguments.of(createParameterizedMono(Void.TYPE), false),
            Arguments.of(createParameterizedMono(JsonPatchDocument.class), true),

            // Flux generics.
            Arguments.of(createParameterizedFlux(BinaryData.class), false),
            Arguments.of(createParameterizedFlux(byte[].class), false),
            Arguments.of(createParameterizedFlux(ByteBuffer.class), false),
            Arguments.of(createParameterizedFlux(MappedByteBuffer.class), false),
            Arguments.of(createParameterizedFlux(InputStream.class), false),
            Arguments.of(createParameterizedFlux(FileInputStream.class), false),
            Arguments.of(createParameterizedFlux(void.class), false),
            Arguments.of(createParameterizedFlux(Void.class), false),
            Arguments.of(createParameterizedFlux(Void.TYPE), false),
            Arguments.of(createParameterizedFlux(JsonPatchDocument.class), true),


            // Response generics.
            // If the raw type is Response it should check the first, and only, generic type.
            Arguments.of(createParameterizedResponse(BinaryData.class), false),
            Arguments.of(createParameterizedResponse(byte[].class), false),
            Arguments.of(createParameterizedResponse(ByteBuffer.class), false),
            Arguments.of(createParameterizedResponse(MappedByteBuffer.class), false),
            Arguments.of(createParameterizedResponse(InputStream.class), false),
            Arguments.of(createParameterizedResponse(FileInputStream.class), false),
            Arguments.of(createParameterizedResponse(void.class), false),
            Arguments.of(createParameterizedResponse(Void.class), false),
            Arguments.of(createParameterizedResponse(Void.TYPE), false),
            Arguments.of(createParameterizedResponse(JsonPatchDocument.class), true),

            // If the raw type is ResponseBase it should check the second generic type, the first is deserialized
            // headers.
            Arguments.of(createParameterizedResponseBase(BinaryData.class), false),
            Arguments.of(createParameterizedResponseBase(byte[].class), false),
            Arguments.of(createParameterizedResponseBase(ByteBuffer.class), false),
            Arguments.of(createParameterizedResponseBase(MappedByteBuffer.class), false),
            Arguments.of(createParameterizedResponseBase(InputStream.class), false),
            Arguments.of(createParameterizedResponseBase(FileInputStream.class), false),
            Arguments.of(createParameterizedResponseBase(void.class), false),
            Arguments.of(createParameterizedResponseBase(Void.class), false),
            Arguments.of(createParameterizedResponseBase(Void.TYPE), false),
            Arguments.of(createParameterizedResponseBase(JsonPatchDocument.class), true),

            // Reactive generics containing response generics.
            // Mono of Response
            Arguments.of(createParameterizedMono(createParameterizedResponse(BinaryData.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(byte[].class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(ByteBuffer.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(MappedByteBuffer.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(InputStream.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(FileInputStream.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(void.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(Void.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(Void.TYPE)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponse(JsonPatchDocument.class)), true),

            // Mono of ResponseBase
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(BinaryData.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(byte[].class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(ByteBuffer.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(MappedByteBuffer.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(InputStream.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(FileInputStream.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(void.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(Void.class)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(Void.TYPE)), false),
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(JsonPatchDocument.class)), true),

            // Flux of Response
            Arguments.of(createParameterizedFlux(createParameterizedResponse(BinaryData.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(byte[].class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(ByteBuffer.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(MappedByteBuffer.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(InputStream.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(FileInputStream.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(void.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(Void.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(Void.TYPE)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponse(JsonPatchDocument.class)), true),

            // Flux of ResponseBase
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(BinaryData.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(byte[].class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(ByteBuffer.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(MappedByteBuffer.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(InputStream.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(FileInputStream.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(void.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(Void.class)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(Void.TYPE)), false),
            Arguments.of(createParameterizedFlux(createParameterizedResponseBase(JsonPatchDocument.class)), true),

            // Custom implementations of Response and ResponseBase.
            Arguments.of(VoidResponse.class, false),
            Arguments.of(StringResponse.class, true),

            Arguments.of(VoidResponseWithDeserializedHeaders.class, false),
            Arguments.of(StringResponseWithDeserializedHeaders.class, true)
        );
    }

    private static ParameterizedType createParameterizedMono(Type genericType) {
        return TypeUtil.createParameterizedType(Mono.class, genericType);
    }

    private static ParameterizedType createParameterizedFlux(Type genericType) {
        return TypeUtil.createParameterizedType(Flux.class, genericType);
    }

    private static ParameterizedType createParameterizedResponse(Type genericType) {
        return TypeUtil.createParameterizedType(Response.class, genericType);
    }

    private static ParameterizedType createParameterizedResponseBase(Type genericType) {
        return TypeUtil.createParameterizedType(ResponseBase.class, HttpHeaders.class, genericType);
    }

    private static final class VoidResponse extends SimpleResponse<Void> {
        VoidResponse(Response<?> response, Void value) {
            super(response, value);
        }
    }

    private static final class StringResponse extends SimpleResponse<String> {
        StringResponse(Response<?> response, String value) {
            super(response, value);
        }
    }

    private static final class VoidResponseWithDeserializedHeaders extends ResponseBase<HttpHeaders, Void> {
        VoidResponseWithDeserializedHeaders(HttpRequest request, int statusCode, HttpHeaders headers, Void value,
            HttpHeaders deserializedHeaders) {
            super(request, statusCode, headers, value, deserializedHeaders);
        }
    }

    private static final class StringResponseWithDeserializedHeaders extends ResponseBase<HttpHeaders, String> {
        StringResponseWithDeserializedHeaders(HttpRequest request, int statusCode, HttpHeaders headers,
            String value, HttpHeaders deserializedHeaders) {
            super(request, statusCode, headers, value, deserializedHeaders);
        }
    }
}

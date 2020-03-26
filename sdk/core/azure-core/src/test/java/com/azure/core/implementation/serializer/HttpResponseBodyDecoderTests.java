// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
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

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    public void prepareForMocking() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void clearMocks() {
        Mockito.framework().clearInlineMocks();
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
        when(expectedStatusCodes.getExpectedStatusCodes()).thenReturn(new int[] { 202 });
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
        when(ioExceptionThrower.deserialize(any(), any(), any())).thenThrow(IOException.class);

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
        when(decodeData.getExpectedStatusCodes()).thenReturn(new int[] { 200 });

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

        ParameterizedType fluxByteBuffer = mock(ParameterizedType.class);
        when(fluxByteBuffer.getRawType()).thenReturn(Flux.class);
        when(fluxByteBuffer.getActualTypeArguments()).thenReturn(new Type[] { ByteBuffer.class });
        HttpResponseDecodeData fluxByteBufferReturnType = mock(HttpResponseDecodeData.class);
        when(fluxByteBufferReturnType.getReturnType()).thenReturn(fluxByteBuffer);

        ParameterizedType monoByteArray = mock(ParameterizedType.class);
        when(monoByteArray.getRawType()).thenReturn(Mono.class);
        when(monoByteArray.getActualTypeArguments()).thenReturn(new Type[] { byte[].class });
        HttpResponseDecodeData monoByteArrayReturnType = mock(HttpResponseDecodeData.class);
        when(monoByteArrayReturnType.getReturnType()).thenReturn(monoByteArray);

        ParameterizedType voidTypeResponse = mock(ParameterizedType.class);
        when(voidTypeResponse.getRawType()).thenReturn(ResponseBase.class);
        when(voidTypeResponse.getActualTypeArguments()).thenReturn(new Type[] { int.class, Void.TYPE });
        HttpResponseDecodeData voidTypeResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidTypeResponseReturnType.getReturnType()).thenReturn(voidTypeResponse);

        ParameterizedType voidClassResponse = mock(ParameterizedType.class);
        when(voidClassResponse.getRawType()).thenReturn(ResponseBase.class);
        when(voidClassResponse.getActualTypeArguments()).thenReturn(new Type[] { int.class, void.class });
        HttpResponseDecodeData voidClassResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidClassResponseReturnType.getReturnType()).thenReturn(voidClassResponse);

        return Stream.of(
            Arguments.of(nullReturnType),
            Arguments.of(fluxByteBufferReturnType),
            Arguments.of(monoByteArrayReturnType),
            Arguments.of(voidTypeResponseReturnType),
            Arguments.of(voidClassResponseReturnType)
        );
    }

    private static boolean isReturnTypeDecodable(HttpResponseDecodeData decodeData) {
        Type returnType = decodeData.getReturnType();

        // Unwrap from Mono
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = TypeUtil.getTypeArgument(returnType);
        }

        // Find body for complex responses
        if (TypeUtil.isTypeOrSubTypeOf(returnType, ResponseBase.class)) {
            ParameterizedType parameterizedType =
                (ParameterizedType) TypeUtil.getSuperType(returnType, ResponseBase.class);
            if (parameterizedType.getActualTypeArguments().length == 2) {
                // check body type
                returnType = parameterizedType.getActualTypeArguments()[1];
            }
        }

        return !FluxUtil.isFluxByteBuffer(returnType)
            && !TypeUtil.isTypeOrSubTypeOf(returnType, byte[].class)
            && !TypeUtil.isTypeOrSubTypeOf(returnType, Void.TYPE)
            && !TypeUtil.isTypeOrSubTypeOf(returnType, Void.class);
    }
}

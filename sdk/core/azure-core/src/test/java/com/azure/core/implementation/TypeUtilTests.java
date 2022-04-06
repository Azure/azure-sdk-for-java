// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeUtilTests {

    @Test
    public void testGetClasses() {
        Puppy puppy = new Puppy();
        List<Class<?>> classes = TypeUtil.getAllClasses(puppy.getClass());
        Assertions.assertEquals(4, classes.size());
        Assertions.assertTrue(classes.contains(Puppy.class));
        Assertions.assertTrue(classes.contains(Dog.class));
        Assertions.assertTrue(classes.contains(Pet.class));
        Assertions.assertTrue(classes.contains(Object.class));
    }

    @Test
    public void testGetTypeArguments() {
        Type[] puppyArgs = TypeUtil.getTypeArguments(Puppy.class);
        Type[] dogArgs = TypeUtil.getTypeArguments(Puppy.class.getGenericSuperclass());
        Type[] petArgs = TypeUtil.getTypeArguments(Dog.class.getGenericSuperclass());

        Assertions.assertEquals(0, puppyArgs.length);
        Assertions.assertEquals(1, dogArgs.length);
        Assertions.assertEquals(2, petArgs.length);
    }

    @Test
    public void testGetTypeArgument() {
        Type dogArgs = TypeUtil.getTypeArgument(Puppy.class.getGenericSuperclass());
        Assertions.assertEquals(Kid.class, dogArgs);
    }

    @Test
    public void testGetRawClass() {
        Type petType = Puppy.class.getSuperclass().getGenericSuperclass();
        Assertions.assertEquals(Pet.class, TypeUtil.getRawClass(petType));
    }

    @Test
    public void testGetSuperType() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);

        Type[] arguments = TypeUtil.getTypeArguments(petType);
        Assertions.assertEquals(2, arguments.length);
        Assertions.assertEquals(Kid.class, arguments[0]);
        Assertions.assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testGetTopSuperType() {
        Type petType = TypeUtil.getSuperType(Puppy.class, Pet.class);

        Type[] arguments = TypeUtil.getTypeArguments(petType);
        Assertions.assertEquals(2, arguments.length);
        Assertions.assertEquals(Kid.class, arguments[0]);
        Assertions.assertEquals(String.class, arguments[1]);
    }

    @Test
    public void testIsTypeOrSubTypeOf() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);

        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, dogType));
        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, Puppy.class));
        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(Puppy.class, petType));
        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(dogType, petType));
        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(dogType, dogType));
        Assertions.assertTrue(TypeUtil.isTypeOrSubTypeOf(petType, petType));
    }

    @Test
    public void testCreateParameterizedType() {
        Type dogType = TypeUtil.getSuperType(Puppy.class);
        Type petType = TypeUtil.getSuperType(dogType);

        Type createdType = TypeUtil.createParameterizedType(Pet.class, Kid.class, String.class);
        Assertions.assertEquals(TypeUtil.getRawClass(petType), TypeUtil.getRawClass(createdType));
        Assertions.assertArrayEquals(TypeUtil.getTypeArguments(petType), TypeUtil.getTypeArguments(createdType));
    }

    @ParameterizedTest
    @MethodSource("isReturnTypeDecodableSupplier")
    public void isReturnTypeDecodable(Type returnType, boolean expected) {
        assertEquals(expected, TypeUtil.isReturnTypeDecodable(returnType));
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

    @ParameterizedTest
    @MethodSource("getHeadersTypeSupplier")
    public void getHeadersType(Type returnType, Type expectedHeadersType) {
        assertEquals(expectedHeadersType, TypeUtil.getHeadersType(returnType));
    }

    private static Stream<Arguments> getHeadersTypeSupplier() {
        return Stream.of(
            // Synchronous return types without headers.
            Arguments.of(String.class, null),
            Arguments.of(createParameterizedResponse(String.class), null),

            // Synchronous return types with headers.
            Arguments.of(createParameterizedResponseBase(String.class), HttpHeaders.class),
            Arguments.of(StringResponseWithDeserializedHeaders.class, HttpHeaders.class),

            // Asynchronous return types without headers.
            Arguments.of(createParameterizedMono(String.class), null),
            Arguments.of(createParameterizedMono(createParameterizedResponse(String.class)), null),

            // Asynchronous return types with headers.
            Arguments.of(createParameterizedMono(createParameterizedResponseBase(String.class)), HttpHeaders.class),
            Arguments.of(createParameterizedMono(StringResponseWithDeserializedHeaders.class), HttpHeaders.class)
        );
    }

    private abstract static class Pet<T extends Human, V> {
        abstract T owner();
    }

    private static class Human {
    }

    private static class Kid extends Human {
    }

    private static class Dog<T extends Human> extends Pet<T, String> {
        private T owner;

        @Override
        public T owner() {
            return owner;
        }
    }

    private static class Puppy extends Dog<Kid> {
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

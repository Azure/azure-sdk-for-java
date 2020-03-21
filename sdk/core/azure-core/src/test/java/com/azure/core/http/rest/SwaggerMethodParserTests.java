// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.implementation.UnixTime;
import com.azure.core.util.Base64Url;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.security.auth.login.Configuration;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SwaggerMethodParserTests {
    interface OperationMethods {
        void noMethod();

        @Get("test")
        void getMethod();

        @Put("test")
        void putMethod();

        @Head("test")
        void headMethod();

        @Delete("test")
        void deleteMethod();

        @Post("test")
        void postMethod();

        @Patch("test")
        void patchMethod();
    }

    @Test
    public void noHttpMethodAnnotation() throws NoSuchMethodException {
        Method noHttpMethodAnnotation = OperationMethods.class.getDeclaredMethod("noMethod");
        assertThrows(MissingRequiredAnnotationException.class, () ->
            new SwaggerMethodParser(noHttpMethodAnnotation, "s://raw.host.com"));
    }

    @ParameterizedTest
    @MethodSource("httpMethodSupplier")
    public void httpMethod(Method method, HttpMethod expectedMethod, String expectedRelativePath) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");
        assertEquals(expectedMethod, swaggerMethodParser.getHttpMethod());
        assertEquals(expectedRelativePath, swaggerMethodParser.setPath(null));
    }

    private static Stream<Arguments> httpMethodSupplier() throws NoSuchMethodException {
        Class<OperationMethods> clazz = OperationMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("getMethod"), HttpMethod.GET, "test"),
            Arguments.of(clazz.getDeclaredMethod("putMethod"), HttpMethod.PUT, "test"),
            Arguments.of(clazz.getDeclaredMethod("headMethod"), HttpMethod.HEAD, "test"),
            Arguments.of(clazz.getDeclaredMethod("deleteMethod"), HttpMethod.DELETE, "test"),
            Arguments.of(clazz.getDeclaredMethod("postMethod"), HttpMethod.POST, "test"),
            Arguments.of(clazz.getDeclaredMethod("patchMethod"), HttpMethod.PATCH, "test")
        );
    }

    interface WireTypesMethods {
        @Get("test")
        void noWireType();

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64Url();

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTime();

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123();

        @Get("test")
        @ReturnValueWireType(Page.class)
        void page();

        @Get("test")
        @ReturnValueWireType(Boolean.class)
        void unknownType();
    }

    @ParameterizedTest
    @MethodSource("wireTypesSupplier")
    public void wireTypes(Method method, Class<?> expectedWireType) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");
        assertEquals(expectedWireType, swaggerMethodParser.getReturnValueWireType());
    }

    private static Stream<Arguments> wireTypesSupplier() throws NoSuchMethodException {
        Class<WireTypesMethods> clazz = WireTypesMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noWireType"), null),
            Arguments.of(clazz.getDeclaredMethod("base64Url"), Base64Url.class),
            Arguments.of(clazz.getDeclaredMethod("unixTime"), UnixTime.class),
            Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123"), DateTimeRfc1123.class),
            Arguments.of(clazz.getDeclaredMethod("page"), Page.class),
            Arguments.of(clazz.getDeclaredMethod("unknownType"), null)
        );
    }

    interface HeaderMethods {
        @Get("test")
        void noHeaders();

        @Get("test")
        @Headers({"", ":", "nameOnly:", ":valueOnly"})
        void malformedHeaders();

        @Get("test")
        @Headers({"name1:value1", " name2: value2", "name3 :value3 "})
        void headers();

        @Get("test")
        @Headers({"name:value1", "name:value2"})
        void sameKeyTwiceLastWins();
    }

    @ParameterizedTest
    @MethodSource("headersSupplier")
    public void headers(Method method, HttpHeaders expectedHeaders) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        for (HttpHeader header : swaggerMethodParser.setHeaders(null)) {
            assertEquals(expectedHeaders.getValue(header.getName()), header.getValue());
        }
    }

    private static Stream<Arguments> headersSupplier() throws NoSuchMethodException {
        Class<HeaderMethods> clazz = HeaderMethods.class;
        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("malformedHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("headers"), new HttpHeaders()
                .put("name1", "value1").put("name2", "value2").put("name3", "value3")),
            Arguments.of(clazz.getDeclaredMethod("sameKeyTwiceLastWins"), new HttpHeaders().put("name", "value2"))
        );
    }

    interface HostSubstitutionMethods {
    }

    interface PathSubstitutionMethods {

    }

    interface QuerySubstitutionMethods {

    }

    interface HeaderSubstitutionMethods {

    }

    interface BodySubstitutionMethods {

    }

    interface FormSubstitutionMethods {

    }

    @ParameterizedTest
    @MethodSource("setContextSupplier")
    public void setContext(SwaggerMethodParser swaggerMethodParser, Object[] arguments, Context expectedContext) {
        assertEquals(expectedContext, swaggerMethodParser.setContext(arguments));
    }

    private static Stream<Arguments> setContextSupplier() throws NoSuchMethodException {
        Method method = OperationMethods.class.getDeclaredMethod("getMethod");
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        return Stream.of(
            Arguments.of(swaggerMethodParser, null, Context.NONE),
            Arguments.of(swaggerMethodParser, new Object[]{}, Context.NONE),
            Arguments.of(swaggerMethodParser, new Object[]{"string"}, Context.NONE),
            Arguments.of(swaggerMethodParser, new Object[]{Configuration.getConfiguration()},
                Configuration.getConfiguration())
        );
    }

    interface ExpectedStatusCodeMethods {
        @Get("test")
        void noExpectedStatusCodes();

        @Get("test")
        @ExpectedResponses({ 200 })
        void only200IsExpected();

        @Get("test")
        @ExpectedResponses({ 429, 503 })
        void retryAfterExpected();
    }

    @ParameterizedTest
    @MethodSource("expectedStatusCodeSupplier")
    public void expectedStatusCodeSupplier(Method method, int statusCode, int[] expectedStatusCodes,
        boolean matchesExpected) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        assertArrayEquals(expectedStatusCodes, swaggerMethodParser.getExpectedStatusCodes());
        assertEquals(matchesExpected, swaggerMethodParser.isExpectedResponseStatusCode(statusCode));
    }

    private static Stream<Arguments> expectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<ExpectedStatusCodeMethods> clazz = ExpectedStatusCodeMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 200, null, true),
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 201, null, true),
            Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 400, null, false),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 200, new int[] {200}, true),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 201, new int[] {200}, false),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 400, new int[] {200}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 200, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 201, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 400, new int[] {429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 429, new int[] {429, 503}, true),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 503, new int[] {429, 503}, true)
        );
    }
}

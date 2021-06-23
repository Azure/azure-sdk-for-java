// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.implementation.UnixTime;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.http.ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void httpMethod(Method method, HttpMethod expectedMethod, String expectedRelativePath,
        String expectedFullyQualifiedName) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");
        assertEquals(expectedMethod, swaggerMethodParser.getHttpMethod());
        assertEquals(expectedRelativePath, swaggerMethodParser.setPath(null));
        assertEquals(expectedFullyQualifiedName, swaggerMethodParser.getFullyQualifiedMethodName());
    }

    private static Stream<Arguments> httpMethodSupplier() throws NoSuchMethodException {
        Class<OperationMethods> clazz = OperationMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("getMethod"), HttpMethod.GET, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.getMethod"),
            Arguments.of(clazz.getDeclaredMethod("putMethod"), HttpMethod.PUT, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.putMethod"),
            Arguments.of(clazz.getDeclaredMethod("headMethod"), HttpMethod.HEAD, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.headMethod"),
            Arguments.of(clazz.getDeclaredMethod("deleteMethod"), HttpMethod.DELETE, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.deleteMethod"),
            Arguments.of(clazz.getDeclaredMethod("postMethod"), HttpMethod.POST, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.postMethod"),
            Arguments.of(clazz.getDeclaredMethod("patchMethod"), HttpMethod.PATCH, "test",
                "com.azure.core.http.rest.SwaggerMethodParserTests$OperationMethods.patchMethod")
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

        HttpHeaders actual = new HttpHeaders();
        swaggerMethodParser.setHeaders(null, actual);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.getValue(header.getName()), header.getValue());
        }
    }

    private static Stream<Arguments> headersSupplier() throws NoSuchMethodException {
        Class<HeaderMethods> clazz = HeaderMethods.class;
        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("malformedHeaders"), new HttpHeaders()),
            Arguments.of(clazz.getDeclaredMethod("headers"), new HttpHeaders()
                .set("name1", "value1").set("name2", "value2").set("name3", "value3")),
            Arguments.of(clazz.getDeclaredMethod("sameKeyTwiceLastWins"), new HttpHeaders().set("name", "value2"))
        );
    }

    interface HostSubstitutionMethods {
        @Get("test")
        void noSubstitutions(String sub1);

        @Get("test")
        void substitution(@HostParam("sub1") String sub1);

        @Get("test")
        void encodingSubstitution(@HostParam(value = "sub1", encoded = false) String sub1);
    }

    @ParameterizedTest
    @MethodSource("hostSubstitutionSupplier")
    public void hostSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, rawHost);
        UrlBuilder urlBuilder = new UrlBuilder();
        swaggerMethodParser.setSchemeAndHost(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    private static Stream<Arguments> hostSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "https://{sub1}.host.com";
        String sub2RawHost = "https://{sub2}.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "https://{sub1}.host.com"),
            Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("{sub1}"), "https://{sub1}.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
            Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("{sub1}"), "https://%7Bsub1%7D.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
            Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com")
        );
    }

    @ParameterizedTest
    @MethodSource("schemeSubstitutionSupplier")
    public void schemeSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, rawHost);
        UrlBuilder urlBuilder = new UrlBuilder();
        swaggerMethodParser.setSchemeAndHost(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    private static Stream<Arguments> schemeSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "{sub1}://raw.host.com";
        String sub2RawHost = "{sub2}://raw.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray("ĥttps"), "ĥttps://raw.host.com"),
            Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
            Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("ĥttps"), "raw.host.com"),
            Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
            Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "raw.host.com")
        );
    }

    interface PathSubstitutionMethods {
        @Get("{sub1}")
        void noSubstitutions(String sub1);

        @Get("{sub1}")
        void substitution(@PathParam("sub1") String sub1);

        @Get("{sub1}")
        void encodedSubstitution(@PathParam(value = "sub1", encoded = true) String sub1);
    }

    @ParameterizedTest
    @MethodSource("pathSubstitutionSupplier")
    public void pathSubstitution(Method method, Object[] arguments, String expectedPath) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");
        assertEquals(expectedPath, swaggerMethodParser.setPath(arguments));
    }

    private static Stream<Arguments> pathSubstitutionSupplier() throws NoSuchMethodException {
        Class<PathSubstitutionMethods> clazz = PathSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitution", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, toObjectArray("path"), "{sub1}"),
            Arguments.of(encodedSubstitution, toObjectArray("path"), "path"),
            Arguments.of(encodedSubstitution, toObjectArray("{sub1}"), "{sub1}"),
            Arguments.of(encodedSubstitution, toObjectArray((String) null), ""),
            Arguments.of(substitution, toObjectArray("path"), "path"),
            Arguments.of(substitution, toObjectArray("{sub1}"), "%7Bsub1%7D"),
            Arguments.of(substitution, toObjectArray((String) null), "")
        );
    }

    interface QuerySubstitutionMethods {
        @Get("test")
        void substitutions(@QueryParam("sub1") String sub1, @QueryParam("sub2") boolean sub2);

        @Get("test")
        void encodedSubstitutions(@QueryParam(value = "sub1", encoded = true) String sub1,
            @QueryParam(value = "sub2", encoded = true) boolean sub2);

    }

    @ParameterizedTest
    @MethodSource("querySubstitutionSupplier")
    public void querySubstitution(Method method, Object[] arguments, String expectedUrl) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        UrlBuilder urlBuilder = UrlBuilder.parse("https://raw.host.com");
        swaggerMethodParser.setEncodedQueryParameters(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    private static Stream<Arguments> querySubstitutionSupplier() throws NoSuchMethodException {
        Class<QuerySubstitutionMethods> clazz = QuerySubstitutionMethods.class;
        Method substitution = clazz.getDeclaredMethod("substitutions", String.class, boolean.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitutions", String.class, boolean.class);

        return Stream.of(
            Arguments.of(substitution, null, "https://raw.host.com"),
            Arguments.of(substitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
            Arguments.of(substitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
            Arguments.of(substitution, toObjectArray("{sub1}", false),
                "https://raw.host.com?sub1=%7Bsub1%7D&sub2=false"),
            Arguments.of(encodedSubstitution, null, "https://raw.host.com"),
            Arguments.of(encodedSubstitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
            Arguments.of(encodedSubstitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
            Arguments.of(encodedSubstitution, toObjectArray("{sub1}", false),
                "https://raw.host.com?sub1={sub1}&sub2=false")
        );
    }

    interface HeaderSubstitutionMethods {
        @Get("test")
        void addHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        @Headers({ "sub1:sub1", "sub2:false" })
        void overrideHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        void headerMap(@HeaderParam("x-ms-meta-") Map<String, String> headers);
    }

    @ParameterizedTest
    @MethodSource("headerSubstitutionSupplier")
    public void headerSubstitution(Method method, Object[] arguments, Map<String, String> expectedHeaders) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        HttpHeaders actual = new HttpHeaders();
        swaggerMethodParser.setHeaders(arguments, actual);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.get(header.getName()), header.getValue());
        }
    }

    private static Stream<Arguments> headerSubstitutionSupplier() throws NoSuchMethodException {
        Class<HeaderSubstitutionMethods> clazz = HeaderSubstitutionMethods.class;
        Method addHeaders = clazz.getDeclaredMethod("addHeaders", String.class, boolean.class);
        Method overrideHeaders = clazz.getDeclaredMethod("overrideHeaders", String.class, boolean.class);
        Method headerMap = clazz.getDeclaredMethod("headerMap", Map.class);

        Map<String, String> simpleHeaderMap = Collections.singletonMap("key", "value");
        Map<String, String> expectedSimpleHeadersMap = Collections.singletonMap("x-ms-meta-key", "value");

        Map<String, String> complexHeaderMap = new HttpHeaders().set("key1", (String) null).set("key2", "value2").toMap();
        Map<String, String> expectedComplexHeaderMap = Collections.singletonMap("x-ms-meta-key2", "value2");

        return Stream.of(
            Arguments.of(addHeaders, null, null),
            Arguments.of(addHeaders, toObjectArray("header", true), createExpectedParameters("header", true)),
            Arguments.of(addHeaders, toObjectArray(null, true), createExpectedParameters(null, true)),
            Arguments.of(addHeaders, toObjectArray("{sub1}", false), createExpectedParameters("{sub1}", false)),
            Arguments.of(overrideHeaders, null, createExpectedParameters("sub1", false)),
            Arguments.of(overrideHeaders, toObjectArray(null, true), createExpectedParameters("sub1", true)),
            Arguments.of(overrideHeaders, toObjectArray("header", false), createExpectedParameters("header", false)),
            Arguments.of(overrideHeaders, toObjectArray("{sub1}", true), createExpectedParameters("{sub1}", true)),
            Arguments.of(headerMap, null, null),
            Arguments.of(headerMap, toObjectArray(simpleHeaderMap), expectedSimpleHeadersMap),
            Arguments.of(headerMap, toObjectArray(complexHeaderMap), expectedComplexHeaderMap)
        );
    }

    interface BodySubstitutionMethods {
        @Get("test")
        void applicationJsonBody(@BodyParam(ContentType.APPLICATION_JSON) String jsonBody);

        @Get("test")
        void formBody(@FormParam("name") String name, @FormParam("age") Integer age,
            @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormBody(@FormParam(value = "name", encoded = true) String name, @FormParam("age") Integer age,
            @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormKey(@FormParam(value = "x:ms:value") String value);

        @Get("test")
        void encodedFormKey2(@FormParam(value = "x:ms:value", encoded = true) String value);
    }

    @ParameterizedTest
    @MethodSource("bodySubstitutionSupplier")
    public void bodySubstitution(Method method, Object[] arguments, String expectedBodyContentType,
        Object expectedBody) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        assertEquals(void.class, swaggerMethodParser.getReturnType());
        assertEquals(String.class, swaggerMethodParser.getBodyJavaType());
        assertEquals(expectedBodyContentType, swaggerMethodParser.getBodyContentType());
        assertEquals(expectedBody, swaggerMethodParser.setBody(arguments));
    }

    private static Stream<Arguments> bodySubstitutionSupplier() throws NoSuchMethodException {
        Class<BodySubstitutionMethods> clazz = BodySubstitutionMethods.class;
        Method jsonBody = clazz.getDeclaredMethod("applicationJsonBody", String.class);
        Method formBody = clazz.getDeclaredMethod("formBody", String.class, Integer.class, OffsetDateTime.class,
            List.class);
        Method encodedFormBody = clazz.getDeclaredMethod("encodedFormBody", String.class, Integer.class,
            OffsetDateTime.class, List.class);
        Method encodedFormKey = clazz.getDeclaredMethod("encodedFormKey", String.class);
        Method encodedFormKey2 = clazz.getDeclaredMethod("encodedFormKey2", String.class);

        OffsetDateTime dob = OffsetDateTime.of(1980, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        List<String> favoriteColors = Arrays.asList("blue", "green");
        List<String> badFavoriteColors = Arrays.asList(null, "green");

        return Stream.of(
            Arguments.of(jsonBody, null, ContentType.APPLICATION_JSON, null),
            Arguments.of(jsonBody, toObjectArray("{name:John Doe,age:40,dob:01-01-1980}"), ContentType.APPLICATION_JSON,
                "{name:John Doe,age:40,dob:01-01-1980}"),
            Arguments.of(formBody, null, APPLICATION_X_WWW_FORM_URLENCODED, null),
            Arguments.of(formBody, toObjectArray("John Doe", null, dob, null), APPLICATION_X_WWW_FORM_URLENCODED,
                "name=John+Doe&dob=1980-01-01T00%3A00%3A00Z"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, favoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=blue&favoriteColors=green"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=green"),
            Arguments.of(encodedFormBody, null, APPLICATION_X_WWW_FORM_URLENCODED, null),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", null, dob, null), APPLICATION_X_WWW_FORM_URLENCODED,
                "name=John Doe&dob=1980-01-01T00%3A00%3A00Z"),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, favoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John Doe&age=40&favoriteColors=blue&favoriteColors=green"),
            Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John Doe&age=40&favoriteColors=green"),
            Arguments.of(encodedFormKey, toObjectArray("value"), APPLICATION_X_WWW_FORM_URLENCODED,
                "x%3Ams%3Avalue=value"),
            Arguments.of(encodedFormKey2, toObjectArray("value"), APPLICATION_X_WWW_FORM_URLENCODED,
                "x%3Ams%3Avalue=value")
        );
    }

    @ParameterizedTest
    @MethodSource("setContextSupplier")
    public void setContext(SwaggerMethodParser swaggerMethodParser, Object[] arguments, Context expectedContext) {
        assertEquals(expectedContext, swaggerMethodParser.setContext(arguments));
    }

    @ParameterizedTest
    @MethodSource("setRequestOptionsSupplier")
    public void setRequestOptions(SwaggerMethodParser swaggerMethodParser, Object[] arguments, RequestOptions expectedRequestOptions) {
        assertEquals(expectedRequestOptions, swaggerMethodParser.setRequestOptions(arguments));
    }

    private static Stream<Arguments> setContextSupplier() throws NoSuchMethodException {
        Method method = OperationMethods.class.getDeclaredMethod("getMethod");
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        Context context = new Context("key", "value");

        return Stream.of(
            Arguments.of(swaggerMethodParser, null, Context.NONE),
            Arguments.of(swaggerMethodParser, toObjectArray(), Context.NONE),
            Arguments.of(swaggerMethodParser, toObjectArray("string"), Context.NONE),
            Arguments.of(swaggerMethodParser, toObjectArray(context), context)
        );
    }

    private static Stream<Arguments> setRequestOptionsSupplier() throws NoSuchMethodException {
        Method method = OperationMethods.class.getDeclaredMethod("getMethod");
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        RequestOptions bodyOptions = new RequestOptions()
            .setBody(BinaryData.fromString("{\"id\":\"123\"}"));

        RequestOptions headerQueryOptions = new RequestOptions()
            .addHeader("x-ms-foo", "bar")
            .addQueryParam("foo", "bar");

        RequestOptions urlOptions = new RequestOptions()
            .addRequestCallback(httpRequest -> httpRequest.setUrl("https://foo.host.com"));

        RequestOptions statusOptionOptions = new RequestOptions()
            .setThrowOnError(false);

        return Stream.of(
            Arguments.of(swaggerMethodParser, null, null),
            Arguments.of(swaggerMethodParser, toObjectArray(), null),
            Arguments.of(swaggerMethodParser, toObjectArray("string"), null),
            Arguments.of(swaggerMethodParser, toObjectArray(bodyOptions), bodyOptions),
            Arguments.of(swaggerMethodParser, toObjectArray("string", headerQueryOptions), headerQueryOptions),
            Arguments.of(swaggerMethodParser, toObjectArray("string1", "string2", urlOptions), urlOptions),
            Arguments.of(swaggerMethodParser, toObjectArray(statusOptionOptions), statusOptionOptions)
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

        if (expectedStatusCodes != null) {
            for (int expectedCode : expectedStatusCodes) {
                assertTrue(swaggerMethodParser.isExpectedResponseStatusCode(expectedCode));
            }
        }
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

    interface UnexpectedStatusCodeMethods {
        @Get("test")
        void noUnexpectedStatusCodes();

        @Get("test")
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404})
        void notFoundStatusCode();

        @Get("test")
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404})
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class)
        void customDefault();
    }

    @ParameterizedTest
    @MethodSource("unexpectedStatusCodeSupplier")
    public void unexpectedStatusCode(Method method, int statusCode, Class<?> expectedExceptionType) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method, "https://raw.host.com");

        assertEquals(expectedExceptionType, swaggerMethodParser.getUnexpectedException(statusCode).getExceptionType());
    }

    private static Stream<Arguments> unexpectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<UnexpectedStatusCodeMethods> clazz = UnexpectedStatusCodeMethods.class;
        Method noUnexpectedStatusCodes = clazz.getDeclaredMethod("noUnexpectedStatusCodes");
        Method notFoundStatusCode = clazz.getDeclaredMethod("notFoundStatusCode");
        Method customDefault = clazz.getDeclaredMethod("customDefault");

        return Stream.of(
            Arguments.of(noUnexpectedStatusCodes, 500, HttpResponseException.class),
            Arguments.of(noUnexpectedStatusCodes, 400, HttpResponseException.class),
            Arguments.of(noUnexpectedStatusCodes, 404, HttpResponseException.class),
            Arguments.of(notFoundStatusCode, 500, HttpResponseException.class),
            Arguments.of(notFoundStatusCode, 400, ResourceNotFoundException.class),
            Arguments.of(notFoundStatusCode, 404, ResourceNotFoundException.class),
            Arguments.of(customDefault, 500, ResourceModifiedException.class),
            Arguments.of(customDefault, 400, ResourceNotFoundException.class),
            Arguments.of(customDefault, 404, ResourceNotFoundException.class)
        );
    }

    private static Object[] toObjectArray(Object... objects) {
        return objects;
    }

    private static Map<String, String> createExpectedParameters(String sub1Value, boolean sub2Value) {
        Map<String, String> expectedParameters = new HashMap<>();
        if (sub1Value != null) {
            expectedParameters.put("sub1", sub1Value);
        }

        expectedParameters.put("sub2", String.valueOf(sub2Value));

        return expectedParameters;
    }
}

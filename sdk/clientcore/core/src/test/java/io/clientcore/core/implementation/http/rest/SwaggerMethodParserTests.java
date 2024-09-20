// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.FormParam;
import io.clientcore.core.http.annotation.HeaderParam;
import io.clientcore.core.http.annotation.HostParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.annotation.PathParam;
import io.clientcore.core.http.annotation.QueryParam;
import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.implementation.util.UriBuilder;
import io.clientcore.core.models.SimpleClass;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.http.models.ContentType.APPLICATION_JSON;
import static io.clientcore.core.http.models.ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SwaggerMethodParserTests {
    private static final ObjectSerializer DEFAULT_SERIALIZER = new DefaultJsonSerializer();

    @ServiceInterface(name = "OperationMethods", host = "https://raw.host.com")
    interface OperationMethods {
        void noMethod();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void getMethod();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void getMethodWithContext(Context context);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void getMethodWithRequestOptions(RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "test")
        void putMethod();

        @HttpRequestInformation(method = HttpMethod.HEAD, path = "test")
        void headMethod();

        @HttpRequestInformation(method = HttpMethod.DELETE, path = "test")
        void deleteMethod();

        @HttpRequestInformation(method = HttpMethod.POST, path = "test")
        void postMethod();

        @HttpRequestInformation(method = HttpMethod.PATCH, path = "test")
        void patchMethod();

        @HttpRequestInformation(method = HttpMethod.OPTIONS, path = "test")
        void optionsMethod();
    }

    @Test
    public void noHttpMethodAnnotation() throws NoSuchMethodException {
        Method noHttpMethodAnnotation = OperationMethods.class.getDeclaredMethod("noMethod");

        assertThrows(MissingRequiredAnnotationException.class, () -> new SwaggerMethodParser(noHttpMethodAnnotation));
    }

    @ParameterizedTest
    @MethodSource("httpMethodSupplier")
    public void httpMethod(Method method, HttpMethod expectedMethod, String expectedRelativePath,
                           String expectedFullyQualifiedName) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        assertEquals(expectedMethod, swaggerMethodParser.getHttpMethod());
        assertEquals(expectedRelativePath, swaggerMethodParser.setPath(null, DEFAULT_SERIALIZER));
        assertEquals(expectedFullyQualifiedName, swaggerMethodParser.getFullyQualifiedMethodName());
    }

    private static Stream<Arguments> httpMethodSupplier() throws NoSuchMethodException {
        Class<OperationMethods> clazz = OperationMethods.class;
        String clazzName = clazz.getName();

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("getMethod"), HttpMethod.GET, "test",
                clazzName + ".getMethod"),
            Arguments.of(clazz.getDeclaredMethod("putMethod"), HttpMethod.PUT, "test",
                clazzName + ".putMethod"),
            Arguments.of(clazz.getDeclaredMethod("headMethod"), HttpMethod.HEAD, "test",
                clazzName + ".headMethod"),
            Arguments.of(clazz.getDeclaredMethod("deleteMethod"), HttpMethod.DELETE, "test",
                clazzName + ".deleteMethod"),
            Arguments.of(clazz.getDeclaredMethod("postMethod"), HttpMethod.POST, "test",
                clazzName + ".postMethod"),
            Arguments.of(clazz.getDeclaredMethod("patchMethod"), HttpMethod.PATCH, "test",
                clazzName + ".patchMethod"),
            Arguments.of(clazz.getDeclaredMethod("optionsMethod"), HttpMethod.OPTIONS, "test",
                clazzName + ".optionsMethod")
        );
    }

    @ServiceInterface(name = "WireTypesMethods", host = "https://raw.host.com")
    interface WireTypesMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noWireType();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", returnValueWireType = Base64Uri.class)
        void base64Uri();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", returnValueWireType = DateTimeRfc1123.class)
        void dateTimeRfc1123();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", returnValueWireType = Boolean.class)
        void unknownType();
    }

    @ParameterizedTest
    @MethodSource("wireTypesSupplier")
    public void wireTypes(Method method, Class<?> expectedWireType) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        assertEquals(expectedWireType, swaggerMethodParser.getReturnValueWireType());
    }

    private static Stream<Arguments> wireTypesSupplier() throws NoSuchMethodException {
        Class<WireTypesMethods> clazz = WireTypesMethods.class;

        return Stream.of(
            Arguments.of(clazz.getDeclaredMethod("noWireType"), null),
            Arguments.of(clazz.getDeclaredMethod("base64Uri"), Base64Uri.class),
            Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123"), DateTimeRfc1123.class),
            Arguments.of(clazz.getDeclaredMethod("unknownType"), null)
        );
    }

    @ServiceInterface(name = "HeaderMethods", host = "https://raw.host.com")
    interface HeaderMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noHeaders();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", headers = {"", ":", "nameOnly:", ":valueOnly"})
        void malformedHeaders();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test",
            headers = {"name1:value1", "name2:value2", "name3:value3"})
        void headers();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", headers = {"name:value1", "name:value2"})
        void sameKeyTwiceLastWins();
    }

    @ParameterizedTest
    @MethodSource("headersSupplier")
    public void headers(Method method, HttpHeaders expectedHeaders) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        HttpHeaders actual = new HttpHeaders();
        swaggerMethodParser.setHeaders(null, actual, DEFAULT_SERIALIZER);

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
                .set(HttpHeaderName.fromString("name1"), "value1")
                .set(HttpHeaderName.fromString("name2"), "value2")
                .set(HttpHeaderName.fromString("name3"), "value3")),
            Arguments.of(clazz.getDeclaredMethod("sameKeyTwiceLastWins"), new HttpHeaders()
                .set(HttpHeaderName.fromString("name"), "value2"))
        );
    }

    @ServiceInterface(name = "HostSubstitutionMethods", host = "https://{sub1}.host.com")
    interface HostSubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noSubstitutions(String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void substitution(@HostParam("sub1") String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodingSubstitution(@HostParam(value = "sub1", encoded = false) String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void substitutionWrongParam(@HostParam("sub2") String sub2);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodingSubstitutionWrongParam(@HostParam(value = "sub2", encoded = false) String sub2);
    }

    @ParameterizedTest
    @MethodSource("hostSubstitutionSupplier")
    public void hostSubstitution(Method method, Object[] arguments, String expectedUri) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);
        UriBuilder uriBuilder = new UriBuilder();
        SwaggerMethodParser.setSchemeAndHost("https://{sub1}.host.com", swaggerMethodParser.hostSubstitutions,
            arguments, uriBuilder, DEFAULT_SERIALIZER);

        assertEquals(expectedUri, uriBuilder.toString());
    }

    private static Stream<Arguments> hostSubstitutionSupplier() throws NoSuchMethodException {
        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);
        Method substitutionWrongParam = clazz.getDeclaredMethod("substitutionWrongParam", String.class);
        Method encodingSubstitutionWrongParam = clazz.getDeclaredMethod("encodingSubstitutionWrongParam", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, toObjectArray("raw"), "https://{sub1}.host.com"),
            Arguments.of(substitution, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(substitution, toObjectArray("{sub1}"), "https://{sub1}.host.com"),
            Arguments.of(substitution, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, null, "https://{sub1}.host.com"),
            Arguments.of(substitutionWrongParam, toObjectArray("raw"), "https://{sub1}.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray("raw"), "https://raw.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray("{sub1}"), "https://%7Bsub1%7D.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray((String) null), "https://.host.com"),
            Arguments.of(substitution, null, "https://{sub1}.host.com"),
            Arguments.of(encodingSubstitutionWrongParam, toObjectArray("raw"), "https://{sub1}.host.com")
        );
    }

    @ServiceInterface(name = "SchemeSubstitutionMethods", host = "{sub1}://raw.host.com")
    interface SchemeSubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noSubstitutions(String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void substitution(@HostParam("sub1") String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodingSubstitution(@HostParam(value = "sub1", encoded = false) String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void substitutionWrongParam(@HostParam("sub2") String sub2);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodingSubstitutionWrongParam(@HostParam(value = "sub2", encoded = false) String sub2);
    }

    @ParameterizedTest
    @MethodSource("schemeSubstitutionSupplier")
    public void schemeSubstitution(Method method, Object[] arguments, String expectedUri) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);
        UriBuilder uriBuilder = new UriBuilder();
        SwaggerMethodParser.setSchemeAndHost("{sub1}://raw.host.com", swaggerMethodParser.hostSubstitutions, arguments,
            uriBuilder, DEFAULT_SERIALIZER);

        assertEquals(expectedUri, uriBuilder.toString());
    }

    private static Stream<Arguments> schemeSubstitutionSupplier() throws NoSuchMethodException {
        Class<SchemeSubstitutionMethods> clazz = SchemeSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);
        Method substitutionWrongParam = clazz.getDeclaredMethod("substitutionWrongParam", String.class);
        Method encodingSubstitutionWrongParam = clazz.getDeclaredMethod("encodingSubstitutionWrongParam", String.class);

        return Stream.of(
            Arguments.of(noSubstitutions, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(substitution, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(substitution, toObjectArray("ĥttps"), "ĥttps://raw.host.com"),
            Arguments.of(substitution, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, null, "raw.host.com"),
            Arguments.of(substitutionWrongParam, toObjectArray("raw"), "raw.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray("http"), "http://raw.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray("ĥttps"), "raw.host.com"),
            Arguments.of(encodingSubstitution, toObjectArray((String) null), "raw.host.com"),
            Arguments.of(substitution, null, "raw.host.com"),
            Arguments.of(encodingSubstitutionWrongParam, toObjectArray("raw"), "raw.host.com")
        );
    }

    @ServiceInterface(name = "PathSubstitutionMethods", host = "https://raw.host.com")
    interface PathSubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "{sub1}")
        void noSubstitutions(String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{sub1}")
        void substitution(@PathParam("sub1") String sub1);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{sub1}")
        void encodedSubstitution(@PathParam(value = "sub1", encoded = true) String sub1);
    }

    @ParameterizedTest
    @MethodSource("pathSubstitutionSupplier")
    public void pathSubstitution(Method method, Object[] arguments, String expectedPath) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        assertEquals(expectedPath, swaggerMethodParser.setPath(arguments, DEFAULT_SERIALIZER));
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

    @ServiceInterface(name = "QuerySubstitutionMethods", host = "https://raw.host.com")
    interface QuerySubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void substitutions(@QueryParam("sub1") String sub1, @QueryParam("sub2") boolean sub2);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodedSubstitutions(@QueryParam(value = "sub1", encoded = true) String sub1,
                                  @QueryParam(value = "sub2", encoded = true) boolean sub2);

    }

    @ParameterizedTest
    @MethodSource("querySubstitutionSupplier")
    public void querySubstitution(Method method, Object[] arguments, String expectedUri) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        UriBuilder uriBuilder = UriBuilder.parse("https://raw.host.com");
        swaggerMethodParser.setEncodedQueryParameters(arguments, uriBuilder, DEFAULT_SERIALIZER);

        assertEquals(expectedUri, uriBuilder.toString());
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

    @ServiceInterface(name = "HeaderSubstitutionMethods", host = "https://raw.host.com")
    interface HeaderSubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void addHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", headers = {"sub1:sub1", "sub2:false"})
        void overrideHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void headerMap(@HeaderParam("x-ms-meta-") Map<String, String> headers);
    }

    @ParameterizedTest
    @MethodSource("headerSubstitutionSupplier")
    public void headerSubstitution(Method method, Object[] arguments, Map<HttpHeaderName, String> expectedHeaders) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        HttpHeaders actual = new HttpHeaders();
        swaggerMethodParser.setHeaders(arguments, actual, DEFAULT_SERIALIZER);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.get(header.getName()), header.getValue());
        }
    }

    private static Stream<Arguments> headerSubstitutionSupplier() throws NoSuchMethodException {
        Class<HeaderSubstitutionMethods> clazz = HeaderSubstitutionMethods.class;
        Method addHeaders = clazz.getDeclaredMethod("addHeaders", String.class, boolean.class);
        Method overrideHeaders = clazz.getDeclaredMethod("overrideHeaders", String.class, boolean.class);
        Method headerMap = clazz.getDeclaredMethod("headerMap", Map.class);

        Map<HttpHeaderName, String> simpleHeaderMap = Collections.singletonMap(HttpHeaderName.fromString("key"), "value");
        Map<HttpHeaderName, String> expectedSimpleHeadersMap = Collections.singletonMap(HttpHeaderName.fromString("x-ms-meta-key"), "value");

        Map<String, String> complexHeaderMap = new HttpHeaders()
            .set(HttpHeaderName.fromString("key1"), (String) null)
            .set(HttpHeaderName.fromString("key2"), "value2")
            .toMap();
        Map<HttpHeaderName, String> expectedComplexHeaderMap = Collections.singletonMap(HttpHeaderName.fromString("x-ms-meta-key2"), "value2");

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

    @ServiceInterface(name = "BodySubstitutionMethods", host = "https://raw.host.com")
    interface BodySubstitutionMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void applicationJsonBody(@BodyParam(APPLICATION_JSON) String jsonBody);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void formBody(@FormParam("name") String name, @FormParam("age") Integer age,
                      @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodedFormBody(@FormParam(value = "name", encoded = true) String name, @FormParam("age") Integer age,
                             @FormParam("dob") OffsetDateTime dob,
                             @FormParam("favoriteColors") List<String> favoriteColors);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodedFormKey(@FormParam(value = "x:ms:value") String value);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void encodedFormKey2(@FormParam(value = "x:ms:value", encoded = true) String value);

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void formBodyEnum(@FormParam("enum1") HttpMethod enum1, @FormParam("enum2") HttpMethod enum2,
                          @FormParam("expandableEnum1") HttpHeaderName expandableEnum1,
                          @FormParam("expandableEnum2") HttpHeaderName expandableEnum2);
    }

    @ParameterizedTest
    @MethodSource("bodySubstitutionSupplier")
    public void bodySubstitution(Method method, Object[] arguments, String expectedBodyContentType,
                                 Object expectedBody) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        assertEquals(void.class, swaggerMethodParser.getReturnType());
        assertEquals(String.class, swaggerMethodParser.getBodyJavaType());
        assertEquals(expectedBodyContentType, swaggerMethodParser.getBodyContentType());
        assertEquals(expectedBody, swaggerMethodParser.setBody(arguments, DEFAULT_SERIALIZER));
    }

    private static Stream<Arguments> bodySubstitutionSupplier() throws NoSuchMethodException {
        Class<BodySubstitutionMethods> clazz = BodySubstitutionMethods.class;
        Method jsonBody = clazz.getDeclaredMethod("applicationJsonBody", String.class);
        Method formBody =
            clazz.getDeclaredMethod("formBody", String.class, Integer.class, OffsetDateTime.class, List.class);
        Method formBodyEnum =
            clazz.getDeclaredMethod("formBodyEnum", HttpMethod.class, HttpMethod.class, HttpHeaderName.class, HttpHeaderName.class);
        Method encodedFormBody =
            clazz.getDeclaredMethod("encodedFormBody", String.class, Integer.class, OffsetDateTime.class, List.class);
        Method encodedFormKey = clazz.getDeclaredMethod("encodedFormKey", String.class);
        Method encodedFormKey2 = clazz.getDeclaredMethod("encodedFormKey2", String.class);
        OffsetDateTime dob = OffsetDateTime.of(1980, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        List<String> favoriteColors = Arrays.asList("blue", "green");
        List<String> badFavoriteColors = Arrays.asList(null, "green");

        return Stream.of(
            Arguments.of(jsonBody, null, APPLICATION_JSON, null),
            Arguments.of(jsonBody, toObjectArray("{name:John Doe,age:40,dob:01-01-1980}"), APPLICATION_JSON,
                "{name:John Doe,age:40,dob:01-01-1980}"),
            Arguments.of(formBody, null, APPLICATION_X_WWW_FORM_URLENCODED, null),
            Arguments.of(formBody, toObjectArray("John Doe", null, dob, null), APPLICATION_X_WWW_FORM_URLENCODED,
                "name=John+Doe&dob=1980-01-01T00%3A00%3A00Z"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, favoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=blue&favoriteColors=green"),
            Arguments.of(formBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=green"),
            Arguments.of(formBodyEnum, toObjectArray(HttpMethod.GET, null, HttpHeaderName.ACCEPT, HttpHeaderName.fromString("MyHeader")),
                APPLICATION_X_WWW_FORM_URLENCODED, "enum1=GET&expandableEnum1=Accept&expandableEnum2=MyHeader"),
            Arguments.of(formBodyEnum, toObjectArray(HttpMethod.GET, null, HttpHeaderName.ACCEPT, HttpHeaderName.fromString(null)),
                APPLICATION_X_WWW_FORM_URLENCODED, "enum1=GET&expandableEnum1=Accept"),
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
    @MethodSource("setRequestOptionsSupplier")
    public void setRequestOptions(SwaggerMethodParser swaggerMethodParser, Object[] arguments,
                                  RequestOptions expectedRequestOptions) {
        assertEquals(expectedRequestOptions, swaggerMethodParser.setRequestOptions(arguments));
    }

    private static Stream<Arguments> setRequestOptionsSupplier() throws NoSuchMethodException {
        Method method = OperationMethods.class.getDeclaredMethod("getMethodWithRequestOptions", RequestOptions.class);
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        RequestOptions bodyOptions = new RequestOptions()
            .setBody(BinaryData.fromString("{\"id\":\"123\"}"));

        RequestOptions headerQueryOptions = new RequestOptions()
            .addHeader(new HttpHeader(HttpHeaderName.fromString("x-ms-foo"), "bar"))
            .addQueryParam("foo", "bar");

        RequestOptions uriOptions = new RequestOptions()
            .addRequestCallback(httpRequest -> httpRequest.setUri("https://foo.host.com"));

        // Add this test back if error options is ever made public.
        // RequestOptions statusOptionOptions = new RequestOptions().setErrorOptions(EnumSet.of(ErrorOptions.NO_THROW));

        return Stream.of(
            Arguments.of(swaggerMethodParser, toObjectArray((Object) null), null),
            Arguments.of(swaggerMethodParser, toObjectArray(bodyOptions), bodyOptions),
            Arguments.of(swaggerMethodParser, toObjectArray(headerQueryOptions), headerQueryOptions),
            Arguments.of(swaggerMethodParser, toObjectArray(uriOptions), uriOptions)
            // Arguments.of(swaggerMethodParser, toObjectArray(statusOptionOptions), statusOptionOptions)
        );
    }

    @ServiceInterface(name = "ExpectedStatusCodeMethods", host = "https://raw.host.com")
    interface ExpectedStatusCodeMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noExpectedStatusCodes();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", expectedStatusCodes = {200})
        void only200IsExpected();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test", expectedStatusCodes = {429, 503})
        void retryAfterExpected();
    }

    @ParameterizedTest
    @MethodSource("expectedStatusCodeSupplier")
    public void expectedStatusCodeSupplier(Method method, int statusCode, int[] expectedStatusCodes,
                                           boolean matchesExpected) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

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
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 200, new int[]{200}, true),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 201, new int[]{200}, false),
            Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 400, new int[]{200}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 200, new int[]{429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 201, new int[]{429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 400, new int[]{429, 503}, false),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 429, new int[]{429, 503}, true),
            Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 503, new int[]{429, 503}, true)
        );
    }

    @ServiceInterface(name = "UnexpectedStatusCodeMethods", host = "https://raw.host.com")
    interface UnexpectedStatusCodeMethods {
        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        void noUnexpectedStatusCodes();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        @UnexpectedResponseExceptionDetail(exceptionTypeName = "RESOURCE_NOT_FOUND", statusCode = {400, 404})
        void notFoundStatusCode();

        @HttpRequestInformation(method = HttpMethod.GET, path = "test")
        @UnexpectedResponseExceptionDetail(exceptionTypeName = "RESOURCE_NOT_FOUND", statusCode = {400, 404})
        @UnexpectedResponseExceptionDetail(exceptionTypeName = "RESOURCE_MODIFIED")
        void customDefault();
    }

    @ParameterizedTest
    @MethodSource("unexpectedStatusCodeSupplier")
    public void unexpectedStatusCode(Method method, int statusCode, HttpExceptionType expectedExceptionType) {
        SwaggerMethodParser swaggerMethodParser = new SwaggerMethodParser(method);

        assertEquals(expectedExceptionType, swaggerMethodParser.getUnexpectedException(statusCode).getExceptionType());
    }

    private static Stream<Arguments> unexpectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<UnexpectedStatusCodeMethods> clazz = UnexpectedStatusCodeMethods.class;
        Method noUnexpectedStatusCodes = clazz.getDeclaredMethod("noUnexpectedStatusCodes");
        Method notFoundStatusCode = clazz.getDeclaredMethod("notFoundStatusCode");
        Method customDefault = clazz.getDeclaredMethod("customDefault");

        return Stream.of(
            Arguments.of(noUnexpectedStatusCodes, 500, null),
            Arguments.of(noUnexpectedStatusCodes, 400, null),
            Arguments.of(noUnexpectedStatusCodes, 404, null),
            Arguments.of(notFoundStatusCode, 500, null),
            Arguments.of(notFoundStatusCode, 400, HttpExceptionType.RESOURCE_NOT_FOUND),
            Arguments.of(notFoundStatusCode, 404, HttpExceptionType.RESOURCE_NOT_FOUND),
            Arguments.of(customDefault, 500, HttpExceptionType.RESOURCE_MODIFIED),
            Arguments.of(customDefault, 400, HttpExceptionType.RESOURCE_NOT_FOUND),
            Arguments.of(customDefault, 404, HttpExceptionType.RESOURCE_NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("isReturnTypeDecodableSupplier")
    public void isReturnTypeDecodable(Type returnType, boolean expected) {
        Type unwrappedReturnType = SwaggerMethodParser.unwrapReturnType(returnType);

        assertEquals(expected, SwaggerMethodParser.isReturnTypeDecodable(unwrappedReturnType));
    }

    private static Stream<Arguments> isReturnTypeDecodableSupplier() {
        return returnTypeSupplierForDecodable(true, false, false);
    }

    private static Stream<Arguments> returnTypeSupplierForDecodable(boolean nonBinaryTypeStatus,
                                                                    boolean binaryTypeStatus, boolean voidTypeStatus) {
        return Stream.of(
            // Unknown response type can't be determined to be decodable.
            Arguments.of(null, false),

            // BinaryData, Byte arrays, ByteBuffers, InputStream, and voids aren't decodable.
            Arguments.of(BinaryData.class, binaryTypeStatus),

            Arguments.of(byte[].class, binaryTypeStatus),

            // Both ByteBuffer and subtypes shouldn't be decodable.
            Arguments.of(ByteBuffer.class, binaryTypeStatus),
            Arguments.of(MappedByteBuffer.class, binaryTypeStatus),

            // Both InputSteam and subtypes shouldn't be decodable.
            Arguments.of(InputStream.class, binaryTypeStatus),
            Arguments.of(FileInputStream.class, binaryTypeStatus),

            Arguments.of(void.class, voidTypeStatus),
            Arguments.of(Void.class, voidTypeStatus),
            Arguments.of(Void.TYPE, voidTypeStatus),

            // Other POJO types are decodable.
            Arguments.of(SimpleClass.class, nonBinaryTypeStatus),

            // In addition to the direct types, reactive and Response generic types should be handled.

            // Response generics.
            // If the raw type is Response it should check the first, and only, generic type.
            Arguments.of(createParameterizedResponse(BinaryData.class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(byte[].class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(ByteBuffer.class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(MappedByteBuffer.class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(InputStream.class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(FileInputStream.class), binaryTypeStatus),
            Arguments.of(createParameterizedResponse(void.class), voidTypeStatus),
            Arguments.of(createParameterizedResponse(Void.class), voidTypeStatus),
            Arguments.of(createParameterizedResponse(Void.TYPE), voidTypeStatus),
            Arguments.of(createParameterizedResponse(SimpleClass.class), nonBinaryTypeStatus),

            // Custom implementations of Response.
            Arguments.of(VoidResponse.class, voidTypeStatus),
            Arguments.of(StringResponse.class, nonBinaryTypeStatus)
        );
    }

    private static ParameterizedType createParameterizedResponse(Type genericType) {
        return TypeUtil.createParameterizedType(Response.class, genericType);
    }

    private static final class VoidResponse extends HttpResponse<Void> {
        VoidResponse(Response<?> response, Void value) {
            super(response.getRequest(), response.getStatusCode(), response.getHeaders(), value);
        }
    }

    private static final class StringResponse extends HttpResponse<String> {
        StringResponse(Response<?> response, String value) {
            super(response.getRequest(), response.getStatusCode(), response.getHeaders(), value);
        }
    }

    private static Object[] toObjectArray(Object... objects) {
        return objects;
    }

    private static Map<HttpHeaderName, String> createExpectedParameters(String sub1Value, boolean sub2Value) {
        Map<HttpHeaderName, String> expectedParameters = new HashMap<>();
        if (sub1Value != null) {
            expectedParameters.put(HttpHeaderName.fromString("sub1"), sub1Value);
        }

        expectedParameters.put(HttpHeaderName.fromString("sub2"), String.valueOf(sub2Value));

        return expectedParameters;
    }
}

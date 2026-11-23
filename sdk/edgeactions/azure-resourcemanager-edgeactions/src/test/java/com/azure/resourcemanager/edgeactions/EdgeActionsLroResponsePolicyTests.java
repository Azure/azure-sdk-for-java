// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class EdgeActionsLroResponsePolicyTests {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String RESOURCE_URL = "https://management.azure.com/subscriptions/"
        + "00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Cdn/edgeActions/test-action";
    private static final String STATUS_URL = "https://management.azure.com/operation-status/test-operation";
    private static final String LOCATION_URL = "https://management.azure.com/operation-result/test-operation";
    private static final String DEPLOY_PATH = "/versions/v1/deployVersionCode";
    private static final String SWAP_PATH = "/versions/v1/swapDefault";
    private static final String VALID_STATUS = "{\"status\":\"Running\"}";
    private static final String VALID_VERSION = "{\"location\":\"global\",\"properties\":"
        + "{\"deploymentType\":\"file\",\"isDefaultVersion\":\"True\",\"provisioningState\":\"Succeeded\"}}";
    private static final String RESOURCE_METADATA = "\"location\":\"global\",\"id\":\"resource-id\","
        + "\"name\":\"test\",\"type\":\"resource-type\",\"tags\":{\"purpose\":\"test\"},"
        + "\"systemData\":{\"createdBy\":\"creator\",\"createdByType\":\"User\","
        + "\"createdAt\":\"2026-10-16T12:00:00Z\",\"lastModifiedBy\":\"modifier\","
        + "\"lastModifiedByType\":\"Application\",\"lastModifiedAt\":\"2026-10-16T12:00:00Z\"}";
    private static final String FULL_VERSION
        = "{" + RESOURCE_METADATA + ",\"properties\":{\"deploymentType\":\"file\",\"isDefaultVersion\":\"True\","
            + "\"provisioningState\":\"Succeeded\",\"validationStatus\":\"Succeeded\","
            + "\"lastPackageUpdateTime\":\"2026-10-16T12:00:00Z\"}}";
    private static final String FULL_ACTION
        = "{" + RESOURCE_METADATA + ",\"sku\":{\"name\":\"Standard\",\"tier\":\"Standard\"},"
            + "\"properties\":{\"provisioningState\":\"Succeeded\","
            + "\"attachments\":[{\"id\":\"attachment-id\",\"attachedResourceId\":\"resource-id\"}]}}";
    private static final String FULL_FILTER = "{" + RESOURCE_METADATA
        + ",\"properties\":{\"versionId\":\"version-id\",\"executionFilterIdentifierHeaderName\":\"x-test\","
        + "\"executionFilterIdentifierHeaderValue\":\"test\",\"provisioningState\":\"Succeeded\","
        + "\"lastUpdateTime\":\"2026-10-16T12:00:00Z\"}}";
    private static final String FULL_ERROR = "{\"error\":{\"code\":\"InvalidRequest\",\"message\":\"Invalid request\","
        + "\"target\":\"test\",\"details\":[{\"code\":\"InvalidField\",\"message\":\"Invalid field\",\"target\":\"field\"}],"
        + "\"additionalInfo\":[{\"type\":\"diagnostics\",\"info\":{\"arbitrary\":null}}]}}";

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void validatesStatusAndPreservesResponseBody(boolean sync) {
        exchange(HttpMethod.POST, DEPLOY_PATH, STATUS_URL, 200, VALID_STATUS, sync);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"status\":\"Succeeded\"}",
            "{\"status\":\"ProviderPending\"}",
            "{\"status\":\"Failed\",\"error\":null}",
            "{\"status\":\"Canceled\"}",
            "{\"status\":\"Running\",\"id\":null,\"name\":42,\"startTime\":\"not-a-date\","
                + "\"endTime\":null,\"percentComplete\":\"25\",\"error\":null}" })
    public void acceptsStatusOnlyAndIgnoresOtherFields(String body) {
        exchange(HttpMethod.POST, DEPLOY_PATH, STATUS_URL, 200, body, true);
        exchange(HttpMethod.POST, DEPLOY_PATH, STATUS_URL, 200, body, false);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "{",
            "[]",
            "{}",
            "{\"id\":\"operation-id\"}",
            "{\"status\":null}",
            "{\"status\":200}",
            "{\"status\":true}",
            "{\"status\":[]}",
            "{\"status\":{}}" })
    public void rejectsInvalidStatusBodies(String body) {
        Assertions.assertThrows(AssertionError.class,
            () -> exchange(HttpMethod.POST, DEPLOY_PATH, STATUS_URL, 200, body, true));
    }

    @Test
    public void rejectsUnexpectedStatusResponseCode() {
        RuntimeException error = Assertions.assertThrows(RuntimeException.class,
            () -> exchange(HttpMethod.POST, DEPLOY_PATH, STATUS_URL, 204, "", false));
        Assertions.assertInstanceOf(AssertionError.class, Exceptions.unwrap(error));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void swapExemptionDoesNotHideInvalidAsyncStatus(boolean sync) {
        Class<? extends Throwable> errorType = sync ? AssertionError.class : RuntimeException.class;
        Throwable error = Assertions.assertThrows(errorType,
            () -> exchange(HttpMethod.POST, SWAP_PATH, STATUS_URL, 200, "{\"id\":\"operation-id\"}", sync));
        Throwable cause = Exceptions.unwrap(error);
        Assertions.assertInstanceOf(AssertionError.class, cause);
        Assertions.assertTrue(cause.getMessage().contains("Azure-AsyncOperation.status"));
    }

    @ParameterizedTest
    @MethodSource("validLocationResponses")
    public void validatesLocationResponses(HttpMethod method, String path, int status, String body) {
        exchange(method, path, LOCATION_URL, status, body, true);
        exchange(method, path, LOCATION_URL, status, body, false);
    }

    private static Stream<Arguments> validLocationResponses() {
        return Stream.of(Arguments.of(HttpMethod.POST, DEPLOY_PATH, 200, VALID_VERSION),
            Arguments.of(HttpMethod.POST, "/versions/v1/getVersionCode", 200,
                "{\"content\":\"dGVzdA==\",\"name\":\"testdeployment\"}"),
            Arguments.of(HttpMethod.PUT, "/versions/v1", 200, VALID_VERSION),
            Arguments.of(HttpMethod.PATCH, "", 200,
                "{\"location\":\"global\",\"sku\":{\"name\":\"Standard\",\"tier\":\"Standard\"},"
                    + "\"properties\":{\"attachments\":[]},\"tags\":{\"purpose\":\"test\"}}"),
            Arguments.of(HttpMethod.PATCH, "/executionFilters/f1", 200,
                "{\"location\":\"global\",\"properties\":{\"versionId\":\"/versions/v1\","
                    + "\"executionFilterIdentifierHeaderName\":\"x-test\",\"executionFilterIdentifierHeaderValue\":\"v1\"}}"),
            Arguments.of(HttpMethod.DELETE, "/versions/v1", 200, ""),
            Arguments.of(HttpMethod.DELETE, "/executionFilters/f1", 204, ""),
            Arguments.of(HttpMethod.DELETE, "", 202, ""));
    }

    @ParameterizedTest
    @MethodSource("invalidLocationResponses")
    public void rejectsInvalidLocationResponses(HttpMethod method, String path, int status, String body) {
        Assertions.assertThrows(AssertionError.class, () -> exchange(method, path, LOCATION_URL, status, body, true));
    }

    private static Stream<Arguments> invalidLocationResponses() {
        return Stream.of(Arguments.of(HttpMethod.POST, DEPLOY_PATH, 204, ""),
            Arguments.of(HttpMethod.POST, DEPLOY_PATH, 200,
                "{\"deploymentType\":\"file\",\"isDefaultVersion\":\"True\",\"provisioningState\":\"Succeeded\"}"),
            Arguments.of(HttpMethod.POST, DEPLOY_PATH, 200, "{\"location\":\"global\",\"properties\":null}"),
            Arguments.of(HttpMethod.POST, DEPLOY_PATH, 200,
                "{\"location\":\"global\",\"properties\":{\"isDefaultVersion\":\"True\"}}"),
            Arguments.of(HttpMethod.POST, DEPLOY_PATH, 200,
                "{\"location\":\"global\",\"properties\":{\"deploymentType\":\"file\",\"isDefaultVersion\":true}}"),
            Arguments.of(HttpMethod.POST, "/versions/v1/getVersionCode", 200, "{\"name\":\"testdeployment\"}"),
            Arguments.of(HttpMethod.PATCH, "", 200, "{\"location\":\"global\"}"),
            Arguments.of(HttpMethod.PATCH, "/executionFilters/f1", 200,
                "{\"location\":\"global\",\"properties\":{\"versionId\":\"/versions/v1\"}}"),
            Arguments.of(HttpMethod.DELETE, "", 200, "{}"), Arguments.of(HttpMethod.DELETE, "", 204, "{}"),
            Arguments.of(HttpMethod.DELETE, "", 202, "{}"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 204 })
    public void skipsOnlySwapLocationResponse(int status) {
        exchange(HttpMethod.POST, SWAP_PATH, LOCATION_URL, status, status == 204 ? "" : "unchecked body", true);
    }

    @Test
    public void nonEdgeActionGetIsNotMistakenForALocationResponse() {
        exchange(HttpMethod.POST, DEPLOY_PATH, "https://management.azure.com/providers/Microsoft.Other/test", 200,
            "unrelated body", true);
    }

    @ParameterizedTest
    @MethodSource("validDirectResponses")
    public void validatesDirectResponses(HttpMethod method, String url, int status, String body) {
        direct(method, url, status, body, true);
        direct(method, url, status, body, false);
    }

    private static Stream<Arguments> validDirectResponses() {
        return Stream.of(Arguments.of(HttpMethod.GET, RESOURCE_URL, 200, FULL_ACTION),
            Arguments.of(HttpMethod.PUT, RESOURCE_URL, 201, FULL_ACTION),
            Arguments.of(HttpMethod.PUT, RESOURCE_URL, 200, FULL_ACTION),
            Arguments.of(HttpMethod.PATCH, RESOURCE_URL, 200, FULL_ACTION),
            Arguments.of(HttpMethod.GET, RESOURCE_URL + "/versions/v1", 200, FULL_VERSION),
            Arguments.of(HttpMethod.PUT, RESOURCE_URL + "/versions/v1", 201, FULL_VERSION),
            Arguments.of(HttpMethod.PATCH, RESOURCE_URL + "/versions/v1", 200, FULL_VERSION),
            Arguments.of(HttpMethod.GET, RESOURCE_URL + "/executionFilters/f1", 200, FULL_FILTER),
            Arguments.of(HttpMethod.PUT, RESOURCE_URL + "/executionFilters/f1", 201, FULL_FILTER),
            Arguments.of(HttpMethod.PATCH, RESOURCE_URL + "/executionFilters/f1", 200, FULL_FILTER),
            Arguments.of(HttpMethod.GET, RESOURCE_URL.substring(0, RESOURCE_URL.lastIndexOf('/')), 200,
                "{\"value\":[" + FULL_ACTION + "]}"),
            Arguments.of(HttpMethod.GET,
                "https://management.azure.com/subscriptions/"
                    + "00000000-0000-0000-0000-000000000000/providers/Microsoft.Cdn/edgeActions",
                200, "{\"value\":[]}"),
            Arguments.of(HttpMethod.GET, RESOURCE_URL + "/versions", 200, "{\"value\":[" + FULL_VERSION + "]}"),
            Arguments.of(HttpMethod.GET, RESOURCE_URL + "/executionFilters", 200, "{\"value\":[" + FULL_FILTER + "]}"),
            Arguments.of(HttpMethod.POST, RESOURCE_URL + DEPLOY_PATH, 200, VALID_VERSION),
            Arguments.of(HttpMethod.POST, RESOURCE_URL + "/versions/v1/getVersionCode", 200,
                "{\"content\":\"dGVzdA==\",\"name\":\"test\"}"),
            Arguments.of(HttpMethod.POST, RESOURCE_URL + SWAP_PATH, 204, ""),
            Arguments.of(HttpMethod.PATCH, RESOURCE_URL, 202, ""),
            Arguments.of(HttpMethod.DELETE, RESOURCE_URL, 200, ""),
            Arguments.of(HttpMethod.DELETE, RESOURCE_URL, 202, ""),
            Arguments.of(HttpMethod.DELETE, RESOURCE_URL, 204, ""),
            Arguments.of(HttpMethod.GET, RESOURCE_URL, 400, FULL_ERROR));
    }

    @ParameterizedTest
    @MethodSource("responseFields")
    public void checksEveryModeledFieldTypeAndDistinguishesOmissionFromNull(String url, int status, String body,
        String pointer, boolean required) throws JsonProcessingException {
        JsonNode field = JSON.readTree(body).at(pointer);
        Assertions.assertFalse(field.isMissingNode(), "The fixture must contain the field under test");
        String omitted = replaceField(body, pointer, null);
        String explicitNull = replaceField(body, pointer, NullNode.getInstance());
        String wrongType
            = replaceField(body, pointer, field.isTextual() ? IntNode.valueOf(42) : TextNode.valueOf("bad"));
        for (boolean sync : new boolean[] { true, false }) {
            if (required) {
                assertInvalidDirect(HttpMethod.GET, url, status, omitted, sync);
            } else {
                direct(HttpMethod.GET, url, status, omitted, sync);
            }
            assertInvalidDirect(HttpMethod.GET, url, status, explicitNull, sync);
            assertInvalidDirect(HttpMethod.GET, url, status, wrongType, sync);
        }
    }

    private static Stream<Arguments> responseFields() {
        Stream<Arguments> resources = Stream
            .of(new String[] { RESOURCE_URL, FULL_ACTION },
                new String[] { RESOURCE_URL + "/versions/v1", FULL_VERSION },
                new String[] { RESOURCE_URL + "/executionFilters/f1", FULL_FILTER })
            .flatMap(resource -> Stream.concat(fields(resource[0], 200, resource[1], true, "/location"),
                fields(resource[0], 200, resource[1], false, "/id", "/name", "/type", "/tags", "/tags/purpose",
                    "/systemData", "/systemData/createdBy", "/systemData/createdByType", "/systemData/createdAt",
                    "/systemData/lastModifiedBy", "/systemData/lastModifiedByType", "/systemData/lastModifiedAt",
                    "/properties", "/properties/provisioningState")));
        return Stream.of(resources,
            fields(RESOURCE_URL, 200, FULL_ACTION, true, "/sku", "/sku/name", "/sku/tier", "/properties/attachments",
                "/properties/attachments/0/attachedResourceId"),
            fields(RESOURCE_URL, 200, FULL_ACTION, false, "/properties/attachments/0/id"),
            fields(RESOURCE_URL + "/versions/v1", 200, FULL_VERSION, true, "/properties/deploymentType",
                "/properties/isDefaultVersion"),
            fields(RESOURCE_URL + "/versions/v1", 200, FULL_VERSION, false, "/properties/lastPackageUpdateTime"),
            fields(RESOURCE_URL + "/executionFilters/f1", 200, FULL_FILTER, true, "/properties/versionId",
                "/properties/executionFilterIdentifierHeaderName", "/properties/executionFilterIdentifierHeaderValue"),
            fields(RESOURCE_URL + "/executionFilters/f1", 200, FULL_FILTER, false, "/properties/lastUpdateTime"),
            fields(RESOURCE_URL + "/versions", 200, "{\"value\":[],\"nextLink\":\"https://example.test/next\"}", true,
                "/value"),
            fields(RESOURCE_URL + "/versions", 200, "{\"value\":[],\"nextLink\":\"https://example.test/next\"}", false,
                "/nextLink"),
            fields(RESOURCE_URL, 400, FULL_ERROR, false, "/error", "/error/code", "/error/message", "/error/target",
                "/error/details", "/error/details/0/code", "/error/details/0/message", "/error/details/0/target",
                "/error/additionalInfo", "/error/additionalInfo/0/type", "/error/additionalInfo/0/info"))
            .flatMap(stream -> stream);
    }

    private static Stream<Arguments> fields(String url, int status, String body, boolean required, String... pointers) {
        return Arrays.stream(pointers).map(pointer -> Arguments.of(url, status, body, pointer, required));
    }

    private static String replaceField(String body, String pointer, JsonNode replacement)
        throws JsonProcessingException {
        JsonNode value = JSON.readTree(body);
        int separator = pointer.lastIndexOf('/');
        ObjectNode parent = Assertions.assertInstanceOf(ObjectNode.class, value.at(pointer.substring(0, separator)));
        String field = pointer.substring(separator + 1);
        if (replacement == null) {
            parent.remove(field);
        } else {
            parent.set(field, replacement);
        }
        return value.toString();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/versions/v1", "/executionFilters/f1", "" })
    public void validatesEveryListElement(String resourcePath) {
        String listUrl = resourcePath.isEmpty()
            ? RESOURCE_URL.substring(0, RESOURCE_URL.lastIndexOf('/'))
            : RESOURCE_URL + resourcePath.substring(0, resourcePath.lastIndexOf('/'));
        for (String body : new String[] { "{\"value\":[null]}", "{\"value\":[42]}", "{\"value\":[{}]}" }) {
            assertInvalidDirect(HttpMethod.GET, listUrl, 200, body, true);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void omitsOnlyValidationStatusAcrossVersionResponses(boolean sync) throws JsonProcessingException {
        for (JsonNode omittedValue : new JsonNode[] {
            null,
            NullNode.instance,
            IntNode.valueOf(42),
            TextNode.valueOf("Failed"),
            JSON.createObjectNode(),
            JSON.createArrayNode() }) {
            String body = replaceField(FULL_VERSION, "/properties/validationStatus", omittedValue);
            direct(HttpMethod.PUT, RESOURCE_URL + "/versions/v1", 201, body, sync);
            direct(HttpMethod.GET, RESOURCE_URL + "/versions/v1", 200, body, sync);
            direct(HttpMethod.PATCH, RESOURCE_URL + "/versions/v1", 200, body, sync);
            direct(HttpMethod.GET, RESOURCE_URL + "/versions", 200, "{\"value\":[" + body + "]}", sync);
            direct(HttpMethod.POST, RESOURCE_URL + DEPLOY_PATH, 200, body, sync);
            exchange(HttpMethod.POST, DEPLOY_PATH, LOCATION_URL, 200, body, sync);
            exchange(HttpMethod.PUT, "/versions/v1", LOCATION_URL, 200, body, sync);
            exchange(HttpMethod.PATCH, "/versions/v1", LOCATION_URL, 200, body, sync);
            String invalid = replaceField(body, "/properties/provisioningState", NullNode.instance);
            assertInvalidDirect(HttpMethod.GET, RESOURCE_URL + "/versions/v1", 200, invalid, sync);
            assertInvalidDirect(HttpMethod.GET, RESOURCE_URL + "/versions", 200, "{\"value\":[" + invalid + "]}", sync);
            Class<? extends Throwable> errorType = sync ? AssertionError.class : RuntimeException.class;
            Throwable error = Assertions.assertThrows(errorType,
                () -> exchange(HttpMethod.POST, DEPLOY_PATH, LOCATION_URL, 200, invalid, sync));
            Assertions.assertInstanceOf(AssertionError.class, Exceptions.unwrap(error));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}",
            "{\"content\":null,\"name\":\"test\"}",
            "{\"content\":42,\"name\":\"test\"}",
            "{\"content\":\"test\",\"name\":null}",
            "{\"content\":\"test\",\"name\":false}" })
    public void validatesCodeResponseFields(String body) {
        assertInvalidDirect(HttpMethod.POST, RESOURCE_URL + "/versions/v1/getVersionCode", 200, body, true);
        Assertions.assertThrows(AssertionError.class,
            () -> exchange(HttpMethod.POST, "/versions/v1/getVersionCode", LOCATION_URL, 200, body, true));
    }

    @ParameterizedTest
    @MethodSource("invalidDirectResponses")
    public void rejectsUndeclaredStatusesAndUnexpectedBodies(HttpMethod method, String url, int status, String body) {
        assertInvalidDirect(method, url, status, body, true);
        assertInvalidDirect(method, url, status, body, false);
    }

    private static Stream<Arguments> invalidDirectResponses() {
        return Stream.of(Arguments.of(HttpMethod.GET, RESOURCE_URL, 201, FULL_ACTION),
            Arguments.of(HttpMethod.PUT, RESOURCE_URL + "/versions/v1", 202, ""),
            Arguments.of(HttpMethod.PATCH, RESOURCE_URL, 202, FULL_ACTION),
            Arguments.of(HttpMethod.DELETE, RESOURCE_URL, 204, "{}"),
            Arguments.of(HttpMethod.POST, RESOURCE_URL + SWAP_PATH, 200, FULL_VERSION),
            Arguments.of(HttpMethod.POST, RESOURCE_URL + SWAP_PATH, 204, "{}"),
            Arguments.of(HttpMethod.GET, RESOURCE_URL, 400, "{\"error\":{\"details\":[null]}}"),
            Arguments.of(HttpMethod.GET, RESOURCE_URL, 400, "{\"error\":{\"additionalInfo\":[42]}}"),
            Arguments.of(HttpMethod.GET, RESOURCE_URL, 400, "{\"error\":{\"details\":[{\"details\":[null]}]}}"));
    }

    @Test
    public void validatesResourceTimestampFormatsAndPreservesExtensibleEnums() throws JsonProcessingException {
        for (String field : new String[] {
            "/systemData/createdAt",
            "/systemData/lastModifiedAt",
            "/properties/lastPackageUpdateTime" }) {
            for (String invalid : new String[] { "not-a-date", "2026-10-16T12:00:00" }) {
                assertInvalidDirect(HttpMethod.GET, RESOURCE_URL + "/versions/v1", 200,
                    replaceField(FULL_VERSION, field, TextNode.valueOf(invalid)), true);
            }
        }
        assertInvalidDirect(HttpMethod.GET, RESOURCE_URL + "/executionFilters/f1", 200,
            replaceField(FULL_FILTER, "/properties/lastUpdateTime", TextNode.valueOf("not-a-date")), true);
        for (String field : new String[] {
            "/systemData/createdByType",
            "/systemData/lastModifiedByType",
            "/properties/deploymentType",
            "/properties/isDefaultVersion",
            "/properties/provisioningState" }) {
            direct(HttpMethod.GET, RESOURCE_URL + "/versions/v1", 200,
                replaceField(FULL_VERSION, field, TextNode.valueOf("FutureValue")), true);
        }
    }

    @Test
    public void validatesOpaquePaginationUrls() {
        String nextUrl = "https://management.azure.com/next-page";
        AtomicInteger calls = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200,
                (calls.getAndIncrement() == 0
                    ? "{\"value\":[],\"nextLink\":\"" + nextUrl + "\"}"
                    : "{\"value\":[null]}").getBytes(StandardCharsets.UTF_8))))
            .build();
        try (
            HttpResponse response = send(pipeline, new HttpRequest(HttpMethod.GET, RESOURCE_URL + "/versions"), true)) {
            Assertions.assertEquals(200, response.getStatusCode());
        }
        Assertions.assertThrows(AssertionError.class,
            () -> send(pipeline, new HttpRequest(HttpMethod.GET, nextUrl), true));
    }

    @Test
    public void swapLocationExemptionDoesNotExemptSubsequentResourceGet() {
        AtomicInteger calls = new AtomicInteger();
        String versionUrl = RESOURCE_URL + "/versions/v1";
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy())
            .httpClient(request -> Mono.just(calls.getAndIncrement() == 0
                ? new MockHttpResponse(request, 202, new HttpHeaders().set(HttpHeaderName.LOCATION, versionUrl))
                : new MockHttpResponse(request, 200, "{}".getBytes(StandardCharsets.UTF_8))))
            .build();
        try (HttpResponse response = send(pipeline, new HttpRequest(HttpMethod.POST, RESOURCE_URL + SWAP_PATH), true)) {
            Assertions.assertEquals(202, response.getStatusCode());
        }
        try (HttpResponse response = send(pipeline, new HttpRequest(HttpMethod.GET, versionUrl), true)) {
            Assertions.assertEquals(200, response.getStatusCode());
        }
        Assertions.assertThrows(AssertionError.class,
            () -> send(pipeline, new HttpRequest(HttpMethod.GET, versionUrl), true));
    }

    private static void assertInvalidDirect(HttpMethod method, String url, int status, String body, boolean sync) {
        Class<? extends Throwable> errorType = sync ? AssertionError.class : RuntimeException.class;
        Throwable error = Assertions.assertThrows(errorType, () -> direct(method, url, status, body, sync));
        Assertions.assertInstanceOf(AssertionError.class, Exceptions.unwrap(error));
    }

    private static void direct(HttpMethod method, String url, int status, String body, boolean sync) {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy())
            .httpClient(
                request -> Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8))))
            .build();
        try (HttpResponse response = send(pipeline, new HttpRequest(method, url), sync)) {
            Assertions.assertEquals(status, response.getStatusCode());
            Assertions.assertEquals(body, response.getBodyAsBinaryData().toString());
        }
    }

    @Test
    public void invalidHeaderClosesResponseWithoutExposingItsValue() {
        AtomicBoolean closed = new AtomicBoolean();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 202,
                new HttpHeaders().set(HttpHeaderName.AZURE_ASYNCOPERATION, STATUS_URL + "?t=opaque value")) {
                @Override
                public void close() {
                    closed.set(true);
                }
            }))
            .build();
        AssertionError error = Assertions.assertThrows(AssertionError.class,
            () -> send(pipeline, new HttpRequest(HttpMethod.POST, RESOURCE_URL + DEPLOY_PATH), true));
        Assertions.assertTrue(closed.get());
        Assertions.assertTrue(error.getMessage().contains("Azure-AsyncOperation"));
        Assertions.assertFalse(error.getMessage().contains("opaque"));
    }

    @Test
    public void followsUpdatedHeaderUrls() {
        AtomicInteger requests = new AtomicInteger();
        String updatedStatus = STATUS_URL + "-updated";
        String updatedLocation = LOCATION_URL + "-updated";
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy()).httpClient(request -> {
                int step = requests.getAndIncrement();
                HttpHeaders headers = new HttpHeaders();
                if (step == 0) {
                    headers.set(HttpHeaderName.AZURE_ASYNCOPERATION, STATUS_URL);
                    headers.set(HttpHeaderName.LOCATION, LOCATION_URL);
                    return Mono.just(new MockHttpResponse(request, 202, headers));
                }
                if (step == 1) {
                    headers.set(HttpHeaderName.AZURE_ASYNCOPERATION, updatedStatus);
                    headers.set(HttpHeaderName.LOCATION, updatedLocation);
                    return Mono.just(
                        new MockHttpResponse(request, 200, headers, VALID_STATUS.getBytes(StandardCharsets.UTF_8)));
                }
                return Mono.just(new MockHttpResponse(request, 200, "{}".getBytes(StandardCharsets.UTF_8)));
            }).build();
        try (
            HttpResponse ignored = send(pipeline, new HttpRequest(HttpMethod.POST, RESOURCE_URL + DEPLOY_PATH), true)) {
            Assertions.assertEquals(202, ignored.getStatusCode());
        }
        try (HttpResponse ignored = send(pipeline, new HttpRequest(HttpMethod.GET, STATUS_URL), true)) {
            Assertions.assertEquals(200, ignored.getStatusCode());
        }
        Assertions.assertThrows(AssertionError.class,
            () -> send(pipeline, new HttpRequest(HttpMethod.GET, updatedStatus), true));
        Assertions.assertThrows(AssertionError.class,
            () -> send(pipeline, new HttpRequest(HttpMethod.GET, updatedLocation), true));
    }

    private static void exchange(HttpMethod method, String path, String followUrl, int status, String body,
        boolean sync) {
        AtomicInteger requests = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new EdgeActionsLroResponsePolicy()).httpClient(request -> {
                if (requests.getAndIncrement() == 0) {
                    return Mono.just(new MockHttpResponse(request, method == HttpMethod.PUT ? 201 : 202,
                        new HttpHeaders().set(HttpHeaderName.AZURE_ASYNCOPERATION, STATUS_URL)
                            .set(HttpHeaderName.LOCATION, LOCATION_URL),
                        method == HttpMethod.PUT ? VALID_VERSION.getBytes(StandardCharsets.UTF_8) : new byte[0]));
                }
                return Mono.just(new MockHttpResponse(request, status, body.getBytes(StandardCharsets.UTF_8)));
            }).build();
        try (HttpResponse initial = send(pipeline, new HttpRequest(method, RESOURCE_URL + path), sync)) {
            Assertions.assertTrue(initial.getStatusCode() == 201 || initial.getStatusCode() == 202);
        }
        try (HttpResponse response = send(pipeline, new HttpRequest(HttpMethod.GET, followUrl), sync)) {
            Assertions.assertEquals(status, response.getStatusCode());
            Assertions.assertEquals(body, response.getBodyAsBinaryData().toString());
        }
        Assertions.assertEquals(2, requests.get());
    }

    private static HttpResponse send(HttpPipeline pipeline, HttpRequest request, boolean sync) {
        return sync ? pipeline.sendSync(request, Context.NONE) : pipeline.send(request).block(Duration.ofSeconds(5));
    }
}

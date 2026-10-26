// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EdgeActionsLroResponsePolicy implements HttpPipelinePolicy {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern RESOURCE_PATH = Pattern.compile(
        "^/subscriptions/[^/]+/(?:resourceGroups/[^/]+/)?providers/Microsoft\\.Cdn/edgeActions(?:/(.*))?$",
        Pattern.CASE_INSENSITIVE);
    private final Map<String, Contract> statusUrls = new ConcurrentHashMap<>();
    private final Map<String, Contract> locationUrls = new ConcurrentHashMap<>();
    private final Map<String, Contract> pageUrls = new ConcurrentHashMap<>();

    private enum Contract {
        STATUS,
        EDGE_ACTION,
        VERSION,
        FILTER,
        EDGE_ACTION_LIST,
        VERSION_LIST,
        FILTER_LIST,
        CODE,
        DELETE,
        SWAP_LOCATION,
        ERROR
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        // Observe service URLs before the test proxy rewrites requests.
        return HttpPipelinePosition.PER_CALL;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        String url = request.getUrl().toString();
        return next.process().flatMap(response -> {
            Contract contract = track(request.getHttpMethod(), url, response);
            if (contract == null) {
                return Mono.just(response);
            }
            HttpResponse buffered = response.buffer();
            return buffered.getBodyAsString().defaultIfEmpty("").map(body -> {
                validate(contract, response.getStatusCode(), body);
                return buffered;
            }).doOnError(error -> buffered.close());
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpRequest request = context.getHttpRequest();
        String url = request.getUrl().toString();
        HttpResponse response = next.processSync();
        Contract contract = track(request.getHttpMethod(), url, response);
        if (contract == null) {
            return response;
        }
        HttpResponse buffered = response.buffer();
        boolean validated = false;
        try {
            validate(contract, response.getStatusCode(), buffered.getBodyAsBinaryData().toString());
            validated = true;
            return buffered;
        } finally {
            if (!validated) {
                buffered.close();
            }
        }
    }

    private Contract track(HttpMethod method, String url, HttpResponse response) {
        boolean validated = false;
        try {
            Contract contract = trackHeaders(method, url, response);
            validated = true;
            return contract;
        } finally {
            if (!validated) {
                response.close();
            }
        }
    }

    private Contract trackHeaders(HttpMethod method, String url, HttpResponse response) {
        int status = response.getStatusCode();
        if (method == HttpMethod.GET) {
            Contract finalContract = statusUrls.get(url);
            if (finalContract != null) {
                Assertions.assertEquals(200, status, "Azure-AsyncOperation must return 200");
                registerHeaders(response, finalContract);
                return Contract.STATUS;
            }
            finalContract = status == 202 ? locationUrls.get(url) : locationUrls.remove(url);
            if (finalContract != null) {
                if (finalContract == Contract.SWAP_LOCATION) {
                    return null;
                }
                Assertions.assertTrue(
                    status == 200 || status == 202 || finalContract == Contract.DELETE && status == 204,
                    "Location must return a declared response status");
                return finalContract;
            }
        }
        Contract contract = method == HttpMethod.GET ? pageUrls.get(url) : null;
        if (contract == null) {
            contract = resourceContract(method, URI.create(url).getPath());
        }
        if (contract == null) {
            return null;
        }
        if (status < 200 || status >= 300) {
            return Contract.ERROR;
        }
        boolean validStatus;
        if (contract == Contract.SWAP_LOCATION) {
            validStatus = status == 202 || status == 204;
        } else if (method == HttpMethod.DELETE) {
            validStatus = status == 200 || status == 202 || status == 204;
        } else if (method == HttpMethod.PUT) {
            validStatus = status == 200 || status == 201;
        } else if (method == HttpMethod.GET) {
            validStatus = status == 200;
        } else {
            validStatus = status == 200 || status == 202;
        }
        Assertions.assertTrue(validStatus, "Edge Actions " + method + " must return a declared response status");
        if (method != HttpMethod.GET) {
            registerHeaders(response, contract);
        }
        return contract;
    }

    private static Contract resourceContract(HttpMethod method, String path) {
        Matcher matcher = RESOURCE_PATH.matcher(path);
        if (!matcher.matches()) {
            return null;
        }
        String tail = matcher.group(1);
        String[] segments = tail == null ? new String[0] : tail.split("/");
        if (segments.length == 0) {
            return method == HttpMethod.GET ? Contract.EDGE_ACTION_LIST : null;
        }
        boolean version = segments.length >= 2 && "versions".equalsIgnoreCase(segments[1]);
        boolean filter = segments.length >= 2 && "executionFilters".equalsIgnoreCase(segments[1]);
        if (segments.length == 2 && (version || filter)) {
            return method == HttpMethod.GET ? version ? Contract.VERSION_LIST : Contract.FILTER_LIST : null;
        }
        if (segments.length == 4 && version && method == HttpMethod.POST) {
            if ("deployVersionCode".equalsIgnoreCase(segments[3])) {
                return Contract.VERSION;
            }
            if ("getVersionCode".equalsIgnoreCase(segments[3])) {
                return Contract.CODE;
            }
            return "swapDefault".equalsIgnoreCase(segments[3]) ? Contract.SWAP_LOCATION : null;
        }
        if (segments.length == 1 || segments.length == 3 && (version || filter)) {
            if (method == HttpMethod.DELETE) {
                return Contract.DELETE;
            }
            if (method == HttpMethod.GET || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                return version ? Contract.VERSION : filter ? Contract.FILTER : Contract.EDGE_ACTION;
            }
        }
        return null;
    }

    private void registerHeaders(HttpResponse response, Contract contract) {
        String status = response.getHeaderValue(HttpHeaderName.AZURE_ASYNCOPERATION);
        if (status != null) {
            validateUrl(status, "Azure-AsyncOperation");
            statusUrls.put(status, contract);
        }
        String location = response.getHeaderValue(HttpHeaderName.LOCATION);
        if (location != null) {
            validateUrl(location, "Location");
            // Track the swap exemption so a resource-shaped Location URL is not checked as an ordinary GET.
            locationUrls.put(location, contract);
        }
    }

    private static void validateUrl(String value, String header) {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException exception) {
            throw new AssertionError(header + " must contain a valid URL");
        }
        Assertions.assertTrue(uri.isAbsolute() && uri.getHost() != null && uri.getFragment() == null,
            header + " must contain an absolute URL without a fragment");
    }

    private void validate(Contract contract, int statusCode, String body) {
        String label = contract == Contract.STATUS ? "Azure-AsyncOperation" : "Edge Actions " + contract;
        if (contract != Contract.STATUS && statusCode == 202) {
            Assertions.assertTrue(body.isEmpty(), label + " pending response must have no body");
            return;
        }
        if (contract == Contract.DELETE || contract == Contract.SWAP_LOCATION) {
            Assertions.assertTrue(body.isEmpty(), label + " completed response must have no body");
            return;
        }
        JsonNode value;
        try {
            value = JSON.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new AssertionError(label + " must contain valid JSON");
        }
        object(value, label);
        if (contract == Contract.STATUS) {
            strings(value, label, true, "status");
        } else if (contract == Contract.CODE) {
            strings(value, label, true, "content", "name");
        } else if (contract == Contract.ERROR) {
            if (value.has("error")) {
                error(value.get("error"), label + ".error");
            }
        } else if (contract == Contract.EDGE_ACTION_LIST
            || contract == Contract.VERSION_LIST
            || contract == Contract.FILTER_LIST) {
            array(value.get("value"), label + ".value");
            Contract itemContract = contract == Contract.EDGE_ACTION_LIST
                ? Contract.EDGE_ACTION
                : contract == Contract.VERSION_LIST ? Contract.VERSION : Contract.FILTER;
            for (JsonNode item : value.get("value")) {
                resource(itemContract, item, label + ".value[]");
            }
            strings(value, label, false, "nextLink");
            if (value.has("nextLink")) {
                pageUrls.put(value.get("nextLink").textValue(), contract);
            }
        } else {
            resource(contract, value, label);
        }
    }

    private static void resource(Contract contract, JsonNode value, String label) {
        object(value, label);
        strings(value, label, true, "location");
        strings(value, label, false, "id", "name", "type");
        if (value.has("systemData")) {
            JsonNode systemData = value.get("systemData");
            String systemLabel = label + ".systemData";
            object(systemData, systemLabel);
            strings(systemData, systemLabel, false, "createdBy", "createdByType", "lastModifiedBy",
                "lastModifiedByType");
            dateTime(systemData, "createdAt", systemLabel);
            dateTime(systemData, "lastModifiedAt", systemLabel);
        }
        if (value.has("tags")) {
            object(value.get("tags"), label + ".tags");
            value.get("tags")
                .elements()
                .forEachRemaining(
                    tag -> Assertions.assertTrue(tag.isTextual(), label + ".tags values must be strings"));
        }
        if (contract == Contract.EDGE_ACTION) {
            object(value.get("sku"), label + ".sku");
            strings(value.get("sku"), label + ".sku", true, "name", "tier");
        }
        if (value.has("properties")) {
            JsonNode properties = value.get("properties");
            String propertyLabel = label + ".properties";
            object(properties, propertyLabel);
            strings(properties, propertyLabel, false, "provisioningState");
            if (contract == Contract.VERSION) {
                strings(properties, propertyLabel, true, "deploymentType", "isDefaultVersion");
                // Approved omission until the RP fixes validationStatus; keep other version fields checked.
                dateTime(properties, "lastPackageUpdateTime", propertyLabel);
            } else if (contract == Contract.FILTER) {
                strings(properties, propertyLabel, true, "versionId", "executionFilterIdentifierHeaderName",
                    "executionFilterIdentifierHeaderValue");
                dateTime(properties, "lastUpdateTime", propertyLabel);
            } else {
                array(properties.get("attachments"), propertyLabel + ".attachments");
                for (JsonNode attachment : properties.get("attachments")) {
                    object(attachment, propertyLabel + ".attachments[]");
                    strings(attachment, propertyLabel + ".attachments[]", true, "attachedResourceId");
                    strings(attachment, propertyLabel + ".attachments[]", false, "id");
                }
            }
        }
    }

    private static void error(JsonNode value, String label) {
        object(value, label);
        strings(value, label, false, "code", "message", "target");
        if (value.has("details")) {
            array(value.get("details"), label + ".details");
            for (JsonNode detail : value.get("details")) {
                error(detail, label + ".details[]");
            }
        }
        if (value.has("additionalInfo")) {
            array(value.get("additionalInfo"), label + ".additionalInfo");
            for (JsonNode info : value.get("additionalInfo")) {
                String infoLabel = label + ".additionalInfo[]";
                object(info, infoLabel);
                strings(info, infoLabel, false, "type");
                if (info.has("info")) {
                    object(info.get("info"), infoLabel + ".info");
                }
            }
        }
    }

    private static void array(JsonNode value, String label) {
        Assertions.assertTrue(value != null && value.isArray(), label + " must be an array");
    }

    private static void object(JsonNode value, String label) {
        Assertions.assertTrue(value != null && value.isObject(), label + " must be a JSON object");
    }

    private static void strings(JsonNode value, String label, boolean required, String... fields) {
        for (String field : fields) {
            if (required || value.has(field)) {
                Assertions.assertTrue(value.has(field) && value.get(field).isTextual(),
                    label + "." + field + " must be a string");
            }
        }
    }

    private static void dateTime(JsonNode value, String field, String label) {
        if (value.has(field)) {
            strings(value, label, true, field);
            try {
                OffsetDateTime.parse(value.get(field).textValue());
            } catch (DateTimeParseException exception) {
                throw new AssertionError(label + "." + field + " must be a date-time with an offset");
            }
        }
    }
}

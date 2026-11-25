// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.implementation.evaluation.MissingPolicyTokenDetails;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pipeline policy that drives the Azure Policy external evaluation ("Invoke") flow.
 * <p>
 * When a resource operation is disallowed by policy because an external evaluation policy token is missing, Azure
 * Resource Manager responds with {@code 403 RequestDisallowedByPolicy}. This policy acquires a token through the same
 * downstream pipeline, applies it to the {@code x-ms-policy-external-evaluations} header, and retries the original
 * operation once.
 * <p>
 * The flow applies to all resource operations except {@code GET}. Requests that already carry the token header are
 * left untouched to prevent the replay from re-entering the flow.
 */
public class ExternalEvaluationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ExternalEvaluationPolicy.class);

    private static final HttpHeaderName POLICY_EXTERNAL_EVALUATIONS
        = HttpHeaderName.fromString("x-ms-policy-external-evaluations");
    private static final HttpHeaderName CONTENT_TYPE = HttpHeaderName.fromString("Content-Type");
    private static final HttpHeaderName ACCEPT = HttpHeaderName.fromString("Accept");

    private static final String APPLICATION_JSON = "application/json";
    private static final String POLICY_API_VERSION = "2026-07-01";
    private static final String ACQUIRE_POLICY_TOKEN
        = "/providers/Microsoft.Authorization/acquirePolicyToken?api-version=" + POLICY_API_VERSION;

    private static final int FORBIDDEN = 403;
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final byte[] ACQUIRE_BODY_SUFFIX = "}}".getBytes(StandardCharsets.UTF_8);

    private static final Pattern MANAGEMENT_GROUP_SCOPE = Pattern
        .compile("^/providers/Microsoft\\.Management/managementGroups/([^/]+)(?:/|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESOURCE_GROUP_SCOPE
        = Pattern.compile("^/subscriptions/([^/]+)/resourceGroups/([^/]+)(?:/|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBSCRIPTION_SCOPE
        = Pattern.compile("^/subscriptions/([^/]+)(?:/|$)", Pattern.CASE_INSENSITIVE);

    /**
     * Creates an {@link ExternalEvaluationPolicy}.
     */
    public ExternalEvaluationPolicy() {
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        if (shouldSkip(request)) {
            return next.process();
        }

        return makeBodyReplayable(request).then(Mono.defer(() -> {
            HttpRequest originalRequest = context.getHttpRequest().copy();
            PolicyScope scope = PolicyScope.from(originalRequest.getUrl());
            HttpPipelineNextPolicy original = next.clone();
            HttpPipelineNextPolicy acquire = next.clone();
            HttpPipelineNextPolicy replay = next.clone();
            boolean bodyLoggingWasDisabled = isBodyLoggingDisabled(context);

            context.setHttpRequest(originalRequest.copy());
            return original.process().flatMap(response -> {
                if (response.getStatusCode() != FORBIDDEN) {
                    return Mono.just(response);
                }

                HttpResponse bufferedResponse = response.buffer();
                return bufferedResponse.getBodyAsByteArray().defaultIfEmpty(EMPTY_BODY).flatMap(body -> {
                    MissingPolicyTokenDetails details = MissingPolicyTokenDetails.parse(body);
                    if (details == null || scope == null) {
                        return Mono.just(bufferedResponse);
                    }

                    RuntimeException guardError = validate(details);
                    if (guardError != null) {
                        bufferedResponse.close();
                        return Mono.error(guardError);
                    }

                    return acquirePolicyToken(context, acquire, originalRequest, scope).onErrorMap(error -> {
                        context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, bodyLoggingWasDisabled);
                        return policyDeniedException(bufferedResponse, body, error);
                    }).flatMap(token -> {
                        HttpRequest replayRequest = originalRequest.copy();
                        replayRequest.setHeader(POLICY_EXTERNAL_EVALUATIONS, token);
                        context.setHttpRequest(replayRequest);
                        context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, bodyLoggingWasDisabled);
                        bufferedResponse.close();
                        return replay.process();
                    });
                });
            });
        }));
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpRequest request = context.getHttpRequest();
        if (shouldSkip(request)) {
            return next.processSync();
        }

        makeBodyReplayableSync(request);
        HttpRequest originalRequest = context.getHttpRequest().copy();
        PolicyScope scope = PolicyScope.from(originalRequest.getUrl());
        HttpPipelineNextSyncPolicy original = next.clone();
        HttpPipelineNextSyncPolicy acquire = next.clone();
        HttpPipelineNextSyncPolicy replay = next.clone();
        boolean bodyLoggingWasDisabled = isBodyLoggingDisabled(context);

        context.setHttpRequest(originalRequest.copy());
        HttpResponse response = original.processSync();
        if (response.getStatusCode() != FORBIDDEN) {
            return response;
        }

        HttpResponse bufferedResponse = response.buffer();
        byte[] body = bufferedResponse.getBodyAsByteArray().block();
        MissingPolicyTokenDetails details = MissingPolicyTokenDetails.parse(body);
        if (details == null || scope == null) {
            return bufferedResponse;
        }

        RuntimeException guardError = validate(details);
        if (guardError != null) {
            bufferedResponse.close();
            throw LOGGER.logExceptionAsError(guardError);
        }

        String token;
        try {
            token = acquirePolicyTokenSync(context, acquire, originalRequest, scope);
        } catch (RuntimeException acquisitionError) {
            context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, bodyLoggingWasDisabled);
            throw LOGGER.logExceptionAsError(policyDeniedException(bufferedResponse, body, acquisitionError));
        }

        HttpRequest replayRequest = originalRequest.copy();
        replayRequest.setHeader(POLICY_EXTERNAL_EVALUATIONS, token);
        context.setHttpRequest(replayRequest);
        context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, bodyLoggingWasDisabled);
        bufferedResponse.close();
        return replay.processSync();
    }

    private static Mono<String> acquirePolicyToken(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
        HttpRequest originalRequest, PolicyScope scope) {
        return createAcquireRequest(originalRequest, scope).flatMap(request -> {
            context.setHttpRequest(request);
            context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, true);
            return next.process();
        }).flatMap(ExternalEvaluationPolicy::readPolicyToken);
    }

    private static String acquirePolicyTokenSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
        HttpRequest originalRequest, PolicyScope scope) {
        context.setHttpRequest(createAcquireRequestSync(originalRequest, scope));
        context.setData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, true);
        return readPolicyTokenSync(next.processSync());
    }

    private static Mono<HttpRequest> createAcquireRequest(HttpRequest originalRequest, PolicyScope scope) {
        byte[] prefix = createAcquireBodyPrefix(originalRequest);
        BinaryData originalBody = originalRequest.getBodyAsBinaryData();
        Flux<ByteBuffer> serializedBody = originalBody == null
            ? Flux.just(ByteBuffer.wrap(prefix), ByteBuffer.wrap(ACQUIRE_BODY_SUFFIX))
            : Flux.concat(Flux.just(ByteBuffer.wrap(prefix)), originalBody.toFluxByteBuffer(),
                Flux.just(ByteBuffer.wrap(ACQUIRE_BODY_SUFFIX)));

        return BinaryData.fromFlux(serializedBody)
            .map(body -> createAcquireRequest(originalRequest.getUrl(), scope, body));
    }

    private static HttpRequest createAcquireRequestSync(HttpRequest originalRequest, PolicyScope scope) {
        byte[] prefix = createAcquireBodyPrefix(originalRequest);
        BinaryData originalBody = originalRequest.getBodyAsBinaryData();
        byte[] body = originalBody == null ? EMPTY_BODY : originalBody.toBytes();
        int bodyLength = body.length;
        byte[] serializedBody = new byte[prefix.length + bodyLength + ACQUIRE_BODY_SUFFIX.length];
        System.arraycopy(prefix, 0, serializedBody, 0, prefix.length);
        if (originalBody != null) {
            System.arraycopy(body, 0, serializedBody, prefix.length, body.length);
        }
        System.arraycopy(ACQUIRE_BODY_SUFFIX, 0, serializedBody, prefix.length + bodyLength,
            ACQUIRE_BODY_SUFFIX.length);
        return createAcquireRequest(originalRequest.getUrl(), scope, BinaryData.fromBytes(serializedBody));
    }

    private static HttpRequest createAcquireRequest(URL originalUrl, PolicyScope scope, BinaryData body) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, scope.acquireUrl(originalUrl));
        request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.setHeader(ACCEPT, APPLICATION_JSON);
        request.setBody(body);
        return request;
    }

    private static byte[] createAcquireBodyPrefix(HttpRequest request) {
        String prefix = "{\"operation\":{\"uri\":" + toJsonString(request.getUrl().toString()) + ",\"httpMethod\":"
            + toJsonString(request.getHttpMethod().toString());
        if (request.getBodyAsBinaryData() != null) {
            prefix += ",\"content\":";
        }
        return prefix.getBytes(StandardCharsets.UTF_8);
    }

    private static String toJsonString(String value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            writer.writeString(value);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Failed to serialize the acquire request.", e));
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static Mono<String> readPolicyToken(HttpResponse response) {
        HttpResponse bufferedResponse = response.buffer();
        return bufferedResponse.getBodyAsByteArray().defaultIfEmpty(EMPTY_BODY).flatMap(body -> {
            try {
                String token = extractPolicyToken(bufferedResponse, body);
                bufferedResponse.close();
                return Mono.just(token);
            } catch (ManagementException e) {
                return Mono.error(e);
            } catch (RuntimeException e) {
                bufferedResponse.close();
                return Mono.error(e);
            }
        });
    }

    private static String readPolicyTokenSync(HttpResponse response) {
        HttpResponse bufferedResponse = response.buffer();
        byte[] body = bufferedResponse.getBodyAsByteArray().block();
        try {
            String token = extractPolicyToken(bufferedResponse, body);
            bufferedResponse.close();
            return token;
        } catch (ManagementException e) {
            throw e;
        } catch (RuntimeException e) {
            bufferedResponse.close();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractPolicyToken(HttpResponse response, byte[] body) {
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            ManagementError error = parseManagementError(body);
            String message = error != null && error.getMessage() != null
                ? error.getMessage()
                : "The policy token acquisition request failed with status code " + response.getStatusCode() + ".";
            throw new ManagementException(message, response, error);
        }

        try (JsonReader reader = JsonProviders.createReader(body)) {
            Object root = reader.readUntyped();
            Object tokenValue = root instanceof Map ? ((Map<String, Object>) root).get("token") : null;
            String token = tokenValue instanceof String ? (String) tokenValue : null;
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
            throw new IllegalStateException("The policy token acquisition response did not contain a valid token.");
        } catch (IOException e) {
            throw new IllegalStateException("The policy token acquisition response could not be parsed.", e);
        }
    }

    private static Mono<Void> makeBodyReplayable(HttpRequest request) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null || body.isReplayable()) {
            return Mono.empty();
        }
        return body.toReplayableBinaryDataAsync().doOnNext(request::setBody).then();
    }

    private static void makeBodyReplayableSync(HttpRequest request) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body != null && !body.isReplayable()) {
            request.setBody(body.toReplayableBinaryData());
        }
    }

    private static RuntimeException validate(MissingPolicyTokenDetails details) {
        if (details.isChangeReferenceRequired()) {
            return new IllegalStateException("The policy external evaluation flow requires a change reference, which "
                + "is not supported by this version of the SDK. Please upgrade to a newer version of the Azure SDK.");
        }
        return null;
    }

    private static ManagementException policyDeniedException(HttpResponse deniedResponse, byte[] deniedBody,
        Throwable cause) {
        ManagementError error = parseManagementError(deniedBody);
        String message = error != null && error.getMessage() != null
            ? error.getMessage()
            : "The resource operation was disallowed by policy and a policy token could not be acquired.";
        ManagementException exception = new ManagementException(message, deniedResponse, error);
        exception.initCause(cause);
        return exception;
    }

    private static ManagementError parseManagementError(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try (JsonReader reader = JsonProviders.createReader(body)) {
            return ManagementError.fromJson(reader);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static boolean shouldSkip(HttpRequest request) {
        return request.getHttpMethod() == HttpMethod.GET
            || request.getHeaders().getValue(POLICY_EXTERNAL_EVALUATIONS) != null;
    }

    private static boolean isBodyLoggingDisabled(HttpPipelineCallContext context) {
        return Boolean.TRUE.equals(context.getData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT).orElse(false));
    }

    private static final class PolicyScope {
        private final String acquirePath;

        private PolicyScope(String acquirePath) {
            this.acquirePath = acquirePath;
        }

        private static PolicyScope from(URL url) {
            String path = url.getPath();
            Matcher managementGroup = MANAGEMENT_GROUP_SCOPE.matcher(path);
            if (managementGroup.find()) {
                return new PolicyScope("/providers/Microsoft.Management/managementGroups/" + managementGroup.group(1)
                    + ACQUIRE_POLICY_TOKEN);
            }

            Matcher resourceGroup = RESOURCE_GROUP_SCOPE.matcher(path);
            if (resourceGroup.find()) {
                return new PolicyScope("/subscriptions/" + resourceGroup.group(1) + "/resourceGroups/"
                    + resourceGroup.group(2) + ACQUIRE_POLICY_TOKEN);
            }

            Matcher subscription = SUBSCRIPTION_SCOPE.matcher(path);
            if (subscription.find()) {
                return new PolicyScope("/subscriptions/" + subscription.group(1) + ACQUIRE_POLICY_TOKEN);
            }

            LOGGER.verbose("Unable to determine the ARM scope from the request URL; "
                + "skipping the policy external evaluation flow.");
            return null;
        }

        private URL acquireUrl(URL originalUrl) {
            try {
                return originalUrl.toURI().resolve(acquirePath).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Failed to construct the policy token acquisition URL.", e));
            }
        }
    }
}

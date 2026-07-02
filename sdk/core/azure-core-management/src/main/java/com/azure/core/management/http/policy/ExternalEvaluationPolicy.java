// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.evaluation.PolicyToken;
import com.azure.core.management.evaluation.PolicyTokenCredential;
import com.azure.core.management.evaluation.PolicyTokenRequestContext;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.implementation.evaluation.MissingPolicyTokenDetails;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pipeline policy that drives the Azure Policy external evaluation ("Invoke") flow.
 * <p>
 * When a resource operation is disallowed by policy because an external evaluation policy token is missing, Azure
 * Resource Manager responds with {@code 403 RequestDisallowedByPolicy} and a {@code missingPolicyTokenDetails} object
 * in the response body. This policy detects that response, acquires a policy token via the supplied
 * {@link PolicyTokenCredential}, applies the token to the {@code x-ms-policy-external-evaluations} header and retries
 * the original operation once.
 * <p>
 * The policy is positioned {@link HttpPipelinePosition#PER_CALL} so it observes the response after the retry policy
 * has exhausted its own retries. The original operation is retried by replaying the downstream pipeline (reusing the
 * retry and authentication policies), while the token acquisition is delegated out-of-band to the
 * {@link PolicyTokenCredential} so that this policy is never re-entered.
 */
public class ExternalEvaluationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ExternalEvaluationPolicy.class);

    private static final HttpHeaderName POLICY_EXTERNAL_EVALUATIONS
        = HttpHeaderName.fromString("x-ms-policy-external-evaluations");

    private static final int FORBIDDEN = 403;

    private static final Pattern SUBSCRIPTION_ID_PATTERN
        = Pattern.compile("/subscriptions/([^/?]+)", Pattern.CASE_INSENSITIVE);

    private static final byte[] EMPTY_BODY = new byte[0];

    private final PolicyTokenCredential credential;

    /**
     * Creates an {@link ExternalEvaluationPolicy}.
     *
     * @param credential the {@link PolicyTokenCredential} used to acquire a policy token when required.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public ExternalEvaluationPolicy(PolicyTokenCredential credential) {
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpPipelineNextPolicy replay = next.clone();
        HttpRequest request = context.getHttpRequest();
        return makeBodyReplayable(request).then(Mono.defer(next::process)).flatMap(response -> {
            if (response.getStatusCode() != FORBIDDEN) {
                return Mono.just(response);
            }
            HttpResponse bufferedResponse = response.buffer();
            return bufferedResponse.getBodyAsByteArray().defaultIfEmpty(EMPTY_BODY).flatMap(body -> {
                MissingPolicyTokenDetails details = MissingPolicyTokenDetails.parse(body);
                if (details == null) {
                    // Not an external evaluation scenario (for example, a plain policy deny); pass the response back.
                    return Mono.just(bufferedResponse);
                }
                RuntimeException guardError = validate(details);
                if (guardError != null) {
                    bufferedResponse.close();
                    return Mono.error(guardError);
                }
                PolicyTokenRequestContext acquireContext = buildAcquireContext(request);
                if (acquireContext == null) {
                    // Unable to determine the subscription; surface the original response.
                    return Mono.just(bufferedResponse);
                }
                return credential.getPolicyToken(acquireContext)
                    // If the token cannot be acquired, the operation remains denied by policy. Re-surface the original
                    // 403 as the ManagementException the caller would otherwise see, chaining the acquisition failure
                    // as its cause for diagnosability.
                    .onErrorResume(
                        acquisitionError -> Mono.error(policyDeniedException(bufferedResponse, body, acquisitionError)))
                    .flatMap(policyToken -> {
                        // Apply the token to the request currently held by the context. The retry policy below this
                        // policy replaces the context's request with a fresh copy on each attempt, so mutating the
                        // early-captured request instance would be lost on the replay.
                        context.getHttpRequest().setHeader(POLICY_EXTERNAL_EVALUATIONS, policyToken.getToken());
                        bufferedResponse.close();
                        return replay.process();
                    });
            });
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpPipelineNextSyncPolicy replay = next.clone();
        HttpRequest request = context.getHttpRequest();
        makeBodyReplayableSync(request);
        HttpResponse response = next.processSync();
        if (response.getStatusCode() != FORBIDDEN) {
            return response;
        }
        HttpResponse bufferedResponse = response.buffer();
        byte[] body = bufferedResponse.getBodyAsByteArray().block();
        MissingPolicyTokenDetails details = MissingPolicyTokenDetails.parse(body);
        if (details == null) {
            return bufferedResponse;
        }
        RuntimeException guardError = validate(details);
        if (guardError != null) {
            bufferedResponse.close();
            throw LOGGER.logExceptionAsError(guardError);
        }
        PolicyTokenRequestContext acquireContext = buildAcquireContext(request);
        if (acquireContext == null) {
            return bufferedResponse;
        }
        PolicyToken policyToken;
        try {
            policyToken = credential.getPolicyTokenSync(acquireContext);
        } catch (RuntimeException acquisitionError) {
            // If the token cannot be acquired, the operation remains denied by policy. Re-surface the original 403 as
            // the ManagementException the caller would otherwise see, chaining the acquisition failure as its cause.
            throw LOGGER.logExceptionAsError(policyDeniedException(bufferedResponse, body, acquisitionError));
        }
        // Apply the token to the request currently held by the context. The retry policy below this policy replaces
        // the context's request with a fresh copy on each attempt, so mutating the early-captured request instance
        // would be lost on the replay.
        context.getHttpRequest().setHeader(POLICY_EXTERNAL_EVALUATIONS, policyToken.getToken());
        bufferedResponse.close();
        return replay.processSync();
    }

    private static Mono<Void> makeBodyReplayable(HttpRequest request) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null || body.isReplayable()) {
            return Mono.empty();
        }
        return body.toReplayableBinaryDataAsync().map(replayable -> {
            request.setBody(replayable);
            return replayable;
        }).then();
    }

    private static void makeBodyReplayableSync(HttpRequest request) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body != null && !body.isReplayable()) {
            request.setBody(body.toReplayableBinaryData());
        }
    }

    private RuntimeException validate(MissingPolicyTokenDetails details) {
        if (details.isChangeReferenceRequired()) {
            return new IllegalStateException("The policy external evaluation flow requires a change reference, which "
                + "is not supported by this version of the SDK. Please upgrade to a newer version of the Azure SDK.");
        }
        return null;
    }

    private static ManagementException policyDeniedException(HttpResponse deniedResponse, byte[] deniedBody,
        Throwable cause) {
        ManagementError error = parseManagementError(deniedBody);
        String message = (error != null && error.getMessage() != null)
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

    private PolicyTokenRequestContext buildAcquireContext(HttpRequest request) {
        String uri = request.getUrl().toString();
        String subscriptionId = extractSubscriptionId(uri);
        if (subscriptionId == null) {
            LOGGER.verbose("Unable to extract the subscription ID from the request URL; "
                + "skipping the policy external evaluation flow.");
            return null;
        }
        return new PolicyTokenRequestContext().setUri(uri)
            .setHttpMethod(request.getHttpMethod())
            .setContent(request.getBodyAsBinaryData())
            .setSubscriptionId(subscriptionId);
    }

    private static String extractSubscriptionId(String uri) {
        Matcher matcher = SUBSCRIPTION_ID_PATTERN.matcher(uri);
        return matcher.find() ? matcher.group(1) : null;
    }
}

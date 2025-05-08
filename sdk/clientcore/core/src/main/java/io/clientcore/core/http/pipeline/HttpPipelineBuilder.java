// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link HttpPipeline},
 * calling {@link HttpPipelineBuilder#build() build} constructs an instance of the pipeline.
 *
 * <p>A pipeline is configured with a HttpClient that sends the request, if no client is set a default is used.
 * A pipeline may be configured with a list of policies that are applied to each request.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p>Create a pipeline without configuration</p>
 *
 * <!-- src_embed io.clientcore.core.http.HttpPipelineBuilder.noConfiguration -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.HttpPipelineBuilder.noConfiguration -->
 *
 * <p>Create a pipeline using the default HTTP client and a retry policy</p>
 *
 * <!-- src_embed io.clientcore.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .httpClient&#40;HttpClient.getNewInstance&#40;&#41;&#41;
 *     .addPolicy&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 *
 * @see HttpPipeline
 */
@Metadata(properties = MetadataProperties.FLUENT)
public class HttpPipelineBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineBuilder.class);

    private HttpClient httpClient;

    private final LinkedList<HttpPipelinePolicy> beforeRedirect = new LinkedList<>();
    private HttpRedirectPolicy redirectPolicy;
    private final LinkedList<HttpPipelinePolicy> betweenRedirectAndRetry = new LinkedList<>();
    private HttpRetryPolicy retryPolicy;
    private final LinkedList<HttpPipelinePolicy> betweenRetryAndAuthentication = new LinkedList<>();
    private HttpCredentialPolicy credentialPolicy;
    private final LinkedList<HttpPipelinePolicy> betweenAuthenticationAndInstrumentation = new LinkedList<>();
    private HttpInstrumentationPolicy instrumentationPolicy;
    private final LinkedList<HttpPipelinePolicy> afterInstrumentation = new LinkedList<>();

    /**
     * Creates a new instance of HttpPipelineBuilder that can configure options for the {@link HttpPipeline} before
     * creating an instance of it.
     */
    public HttpPipelineBuilder() {
    }

    /**
     * Creates an {@link HttpPipeline} based on options set in the builder. Every time {@code build()} is called, a new
     * instance of {@link HttpPipeline} is created.
     *
     * <p>If HttpClient is not set then a default HttpClient is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = new ArrayList<>(beforeRedirect);

        if (redirectPolicy != null) {
            policies.add(redirectPolicy);
        }

        policies.addAll(betweenRedirectAndRetry);

        if (retryPolicy != null) {
            policies.add(retryPolicy);
        }

        policies.addAll(betweenRetryAndAuthentication);

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        policies.addAll(betweenAuthenticationAndInstrumentation);

        if (instrumentationPolicy != null) {
            policies.add(instrumentationPolicy);
        }

        policies.addAll(afterInstrumentation);

        HttpClient client;

        if (httpClient != null) {
            client = httpClient;
        } else {
            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get("AZURE_HTTP_CLIENT_SHARING"))) {
                client = HttpClient.getSharedInstance();
            } else {
                client = HttpClient.getNewInstance();
            }
        }

        return new HttpPipeline(client, policies);
    }

    /**
     * Sets the HttpClient that the pipeline will use to send requests.
     *
     * @param httpClient The HttpClient the pipeline will use when sending requests.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;

        return this;
    }

    /**
     * Adds an {@link HttpPipelinePolicy} to the builder.
     * <p>
     * The {@code policy} passed will be positioned based on {@link HttpPipelinePolicy#getPipelinePosition()}. If the
     * {@link HttpPipelinePosition} is null an {@link IllegalArgumentException} will be thrown.
     * <p>
     * If the {@code policy} is one of the pillar policies ({@link HttpRedirectPolicy}, {@link HttpRetryPolicy},
     * {@link HttpCredentialPolicy}, or {@link HttpInstrumentationPolicy}) the {@link HttpPipelinePosition} will be ignored
     * as those policies are positioned in a specific location within the pipeline. If a duplicate pillar policy is
     * added (for example two {@link HttpRetryPolicy}) the last one added will be used and a message will be logged.
     *
     * @param policy The policy to add to the pipeline.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (tryAddPillar(policy)) {
            return this;
        }

        HttpPipelinePosition order = policy.getPipelinePosition();
        if (order == null) {
            throw LOGGER.throwableAtError()
                .addKeyValue("policyType", policy.getClass().getCanonicalName())
                .log("Policy has invalid pipeline position - position cannot be null.", IllegalArgumentException::new);
        }

        if (order == HttpPipelinePosition.BEFORE_REDIRECT) {
            beforeRedirect.add(policy);
        } else if (order == HttpPipelinePosition.AFTER_REDIRECT) {
            betweenRedirectAndRetry.add(policy);
        } else if (order == HttpPipelinePosition.AFTER_RETRY) {
            betweenRetryAndAuthentication.add(policy);
        } else if (order == HttpPipelinePosition.AFTER_AUTHENTICATION) {
            betweenAuthenticationAndInstrumentation.add(policy);
        } else if (order == HttpPipelinePosition.AFTER_INSTRUMENTATION) {
            afterInstrumentation.add(policy);
        } else {
            throw LOGGER.throwableAtError()
                .addKeyValue("policyType", policy.getClass().getCanonicalName())
                .addKeyValue("position", order.getValue())
                .log("Policy has unexpected position.", IllegalArgumentException::new);
        }

        return this;
    }

    private boolean tryAddPillar(HttpPipelinePolicy policy) {
        HttpPipelinePolicy previous = null;
        boolean added = false;

        HttpPipelinePosition order = policy.getPipelinePosition();
        if (order == HttpPipelinePosition.REDIRECT) {
            previous = redirectPolicy;
            redirectPolicy = (HttpRedirectPolicy) policy;
            added = true;
        } else if (order == HttpPipelinePosition.RETRY) {
            previous = retryPolicy;
            retryPolicy = (HttpRetryPolicy) policy;
            added = true;
        } else if (order == HttpPipelinePosition.AUTHENTICATION) {
            previous = credentialPolicy;
            credentialPolicy = (HttpCredentialPolicy) policy;
            added = true;
        } else if (order == HttpPipelinePosition.INSTRUMENTATION) {
            previous = instrumentationPolicy;
            instrumentationPolicy = (HttpInstrumentationPolicy) policy;
            added = true;
        }

        if (previous != null) {
            LOGGER.atWarning()
                .addKeyValue("policyType", previous.getClass().getSimpleName())
                .log("A pillar policy was replaced in the pipeline.");
        }

        return added;
    }
}

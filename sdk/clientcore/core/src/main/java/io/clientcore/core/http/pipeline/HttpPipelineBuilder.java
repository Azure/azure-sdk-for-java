// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.util.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

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
 *     .policies&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.HttpPipelineBuilder.defaultHttpClientWithRetryPolicy -->
 *
 * @see HttpPipeline
 */
public class HttpPipelineBuilder {
    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(HttpRedirectPolicy.NAME,
        HttpRetryPolicy.NAME, HttpCredentialPolicy.NAME, HttpLoggingPolicy.NAME, HttpTelemetryPolicy.NAME));

    private final Set<String> policyNames = new HashSet<>();

    private HttpClient httpClient;

    private final LinkedList<HttpPipelinePolicy> beforeRedirect = new LinkedList<>();
    private HttpRedirectPolicy redirectPolicy;
    private final LinkedList<HttpPipelinePolicy> afterRedirect = new LinkedList<>();

    private final LinkedList<HttpPipelinePolicy> beforeRetry = new LinkedList<>();
    private HttpRetryPolicy retryPolicy;
    private final LinkedList<HttpPipelinePolicy> afterRetry = new LinkedList<>();

    private final LinkedList<HttpPipelinePolicy> beforeCredential = new LinkedList<>();
    private HttpCredentialPolicy credentialPolicy;
    private final LinkedList<HttpPipelinePolicy> afterCredential = new LinkedList<>();

    private final LinkedList<HttpPipelinePolicy> beforeLogging = new LinkedList<>();
    private HttpLoggingPolicy loggingPolicy;
    private final LinkedList<HttpPipelinePolicy> afterLogging = new LinkedList<>();

    private final LinkedList<HttpPipelinePolicy> beforeTelemetry = new LinkedList<>();
    private HttpTelemetryPolicy telemetryPolicy;
    private final LinkedList<HttpPipelinePolicy> afterTelemetry = new LinkedList<>();

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
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        addPolicies(policies, beforeRedirect, redirectPolicy, afterRedirect);
        addPolicies(policies, beforeRetry, retryPolicy, afterRetry);
        addPolicies(policies, beforeCredential, credentialPolicy, afterCredential);
        addPolicies(policies, beforeLogging, loggingPolicy, afterLogging);
        addPolicies(policies, beforeTelemetry, telemetryPolicy, afterTelemetry);

        HttpClient client;

        if (httpClient != null) {
            client = httpClient;
        } else {
            if (Configuration.getGlobalConfiguration().get("ENABLE_HTTP_CLIENT_SHARING", Boolean.TRUE)) {
                client = HttpClient.getSharedInstance();
            } else {
                client = HttpClient.getNewInstance();
            }
        }

        return new HttpPipeline(client, policies);
    }

    private static void addPolicies(List<HttpPipelinePolicy> policies, List<HttpPipelinePolicy> before,
        HttpPipelinePolicy policy, List<HttpPipelinePolicy> after) {
        policies.addAll(before);
        if (policy != null) {
            policies.add(policy);
        }
        policies.addAll(after);
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
     * Sets the {@link HttpRedirectPolicy} that the pipeline will use to handle HTTP redirects.
     * <p>
     * If {@code redirectPolicy} is null the pipeline will not handle redirects.
     *
     * @param redirectPolicy The redirect policy to set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setRedirectPolicy(HttpRedirectPolicy redirectPolicy) {
        this.redirectPolicy = redirectPolicy;
        policyNames.add(redirectPolicy.getName());

        return this;
    }

    /**
     * Sets the {@link HttpRetryPolicy} that the pipeline will use to handle retries.
     * <p>
     * If {@code retryPolicy} is null the pipeline will use the default retry policy
     * ({@link HttpRetryPolicy#HttpRetryPolicy()}.
     *
     * @param retryPolicy The retry policy to set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setRetryPolicy(HttpRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        policyNames.add(retryPolicy.getName());

        return this;
    }

    /**
     * Sets the {@link HttpCredentialPolicy} that the pipeline will use to authenticate requests.
     * <p>
     * If {@code credentialPolicy} is null the pipeline will not authenticate requests.
     *
     * @param credentialPolicy The credential policy to set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setCredentialPolicy(HttpCredentialPolicy credentialPolicy) {
        this.credentialPolicy = credentialPolicy;
        policyNames.add(credentialPolicy.getName());

        return this;
    }

    /**
     * Sets the {@link HttpLoggingPolicy} that the pipeline will use to log HTTP requests and responses.
     * <p>
     * If {@code loggingPolicy} is null the pipeline will not log HTTP requests and responses.
     *
     * @param loggingPolicy The logging policy to set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setLoggingPolicy(HttpLoggingPolicy loggingPolicy) {
        this.loggingPolicy = loggingPolicy;
        policyNames.add(loggingPolicy.getName());

        return this;
    }

    /**
     * Sets the {@link HttpTelemetryPolicy} that the pipeline will use to collect telemetry about HTTP requests and
     * responses.
     * <p>
     * If {@code telemetryPolicy} is null the pipeline will not collect telemetry about HTTP requests and responses.
     *
     * @param telemetryPolicy The telemetry policy to set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder setTelemetryPolicy(HttpTelemetryPolicy telemetryPolicy) {
        this.telemetryPolicy = telemetryPolicy;
        policyNames.add(telemetryPolicy.getName());

        return this;
    }

    /**
     * Adds the given {@code policy} before the {@code position} in the pipeline.
     * <p>
     * When this method is called multiple times with the same {@code position}, the policies will be added in the order
     * they were added. For example, if {@code policy1} and then {@code policy2} are added before
     * {@link HttpPipelinePosition#RETRY} the resulting ordering will be {@code policy1}, {@code policy2},
     * {@link HttpRetryPolicy}.
     * <p>
     * If the name of the {@code policy} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"},
     * {@code "logging"}, or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are
     * reserved for the set methods that configure the respective policies.
     *
     * @param policy The policy to add.
     * @param position The position to add the policy before.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policy} or {@code position} is null.
     * @throws IllegalStateException If the {@code policy} shares the same name as a policy already in the pipeline.
     */
    public HttpPipelineBuilder addPolicyBefore(HttpPipelinePolicy policy, HttpPipelinePosition position) {
        return addPolicy(policy, position, true);
    }

    /**
     * Adds the given {@code policy} after the {@code position} in the pipeline.
     * <p>
     * When this method is called multiple times with the same {@code position}, the policies will be added in the order
     * they were added. For example, if {@code policy1} and then {@code policy2} are added after
     * {@link HttpPipelinePosition#RETRY} the resulting ordering will be {@link HttpRetryPolicy}, {@code policy2},
     * {@code policy1}.
     * <p>
     * If the name of the {@code policy} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"},
     * {@code "logging"}, or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are
     * reserved for the set methods that configure the respective policies.
     *
     * @param policy The policy to add.
     * @param position The position to add the policy after.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policy} or {@code position} is null.
     * @throws IllegalStateException If the {@code policy} shares the same name as a policy already in the pipeline.
     */
    public HttpPipelineBuilder addPolicyAfter(HttpPipelinePolicy policy, HttpPipelinePosition position) {
        return addPolicy(policy, position, false);
    }

    private HttpPipelineBuilder addPolicy(HttpPipelinePolicy policy, HttpPipelinePosition position, boolean before) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");
        Objects.requireNonNull(position, "'position' cannot be null.");

        String lowerCaseName = policy.getName().toLowerCase();
        if (RESERVED.contains(lowerCaseName)) {
            throw new IllegalStateException("The policy name is reserved: " + policy.getName());
        }

        if (!policyNames.add(lowerCaseName)) {
            throw new IllegalStateException(
                "A policy with the same name already exists in the pipeline: " + policy.getName());
        }

        switch (position) {
            case REDIRECT:
                if (before) {
                    beforeRedirect.add(policy);
                } else {
                    afterRedirect.push(policy);
                }
                break;

            case RETRY:
                if (before) {
                    beforeRetry.add(policy);
                } else {
                    afterRetry.push(policy);
                }
                break;

            case AUTHENTICATION:
                if (before) {
                    beforeCredential.add(policy);
                } else {
                    afterCredential.push(policy);
                }
                break;

            case LOGGING:
                if (before) {
                    beforeLogging.add(policy);
                } else {
                    afterLogging.push(policy);
                }
                break;

            case TELEMETRY:
                if (before) {
                    beforeTelemetry.add(policy);
                } else {
                    afterTelemetry.push(policy);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown position: " + position);
        }

        policyNames.add(lowerCaseName);
        return this;
    }

    /**
     * Adds the given {@code policy} before the {@link HttpPipelinePolicy} with the given name in the pipeline.
     * <p>
     * When this method is called multiple times with the same {@code policyName}, the policies will be added in the
     * order they were added. For example, if {@code policy1} and then {@code policy2} are added before
     * {@code "retry"} the resulting ordering will be {@code policy1}, {@code policy2}, {@code retry}.
     * <p>
     * If the name of the {@code policy} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"},
     * {@code "logging"}, or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are
     * reserved for the set methods that configure the respective policies.
     *
     * @param policy The policy to add.
     * @param policyName The name of the policy to add the policy before.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policy} or {@code policyName} is null.
     * @throws NoSuchElementException If a policy with the given name is not found.
     * @throws IllegalStateException If the {@code policy} shares the same name as a policy already in the pipeline.
     */
    public HttpPipelineBuilder addPolicyBefore(HttpPipelinePolicy policy, String policyName) {
        return addPolicy(policy, policyName, true);
    }

    /**
     * Adds the given {@code policy} after the {@link HttpPipelinePolicy} with the given name in the pipeline.
     * <p>
     * When this method is called multiple times with the same {@code policyName}, the policies will be added in the
     * order they were added. For example, if {@code policy1} and then {@code policy2} are added after
     * {@code "retry"} the resulting ordering will be {@code retry}, {@code policy2}, {@code policy1}.
     * <p>
     * If the name of the {@code policy} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"},
     * {@code "logging"}, or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are
     * reserved for the set methods that configure the respective policies.
     *
     * @param policy The policy to add.
     * @param policyName The name of the policy to add the policy after.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policy} or {@code policyName} is null.
     * @throws NoSuchElementException If a policy with the given name is not found.
     * @throws IllegalStateException If the {@code policy} shares the same name as a policy already in the pipeline.
     */
    public HttpPipelineBuilder addPolicyAfter(HttpPipelinePolicy policy, String policyName) {
        return addPolicy(policy, policyName, false);
    }

    private HttpPipelineBuilder addPolicy(HttpPipelinePolicy policy, String policyName, boolean before) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");
        Objects.requireNonNull(policyName, "'policyName' cannot be null.");

        String lowerCaseName = policy.getName().toLowerCase();
        if (RESERVED.contains(lowerCaseName)) {
            throw new IllegalStateException("The policy name is reserved: " + policy.getName());
        }

        if (!policyNames.add(lowerCaseName)) {
            throw new IllegalStateException(
                "A policy with the same name already exists in the pipeline: " + policy.getName());
        }

        if (!policyNames.contains(policyName.toLowerCase())) {
            throw new NoSuchElementException("A policy with the name '" + policyName + "' was not found.");
        }

        if (attemptToAdd(policy, policyName, before, beforeRedirect, redirectPolicy, afterCredential)) {
            return this;
        }

        if (attemptToAdd(policy, policyName, before, beforeRetry, retryPolicy, afterRetry)) {
            return this;
        }

        if (attemptToAdd(policy, policyName, before, beforeCredential, credentialPolicy, afterCredential)) {
            return this;
        }

        if (attemptToAdd(policy, policyName, before, beforeLogging, loggingPolicy, afterLogging)) {
            return this;
        }

        if (attemptToAdd(policy, policyName, before, beforeTelemetry, telemetryPolicy, afterTelemetry)) {
            return this;
        }

        throw new IllegalStateException("Should not reach here.");
    }

    private boolean attemptToAdd(HttpPipelinePolicy policy, String policyName, boolean before,
        LinkedList<HttpPipelinePolicy> beforePolicies, HttpPipelinePolicy keyPolicy,
        LinkedList<HttpPipelinePolicy> afterPolicies) {
        int position = 0;
        for (HttpPipelinePolicy toCheck : beforePolicies) {
            if (toCheck.getName().equalsIgnoreCase(policyName)) {
                if (before) {
                    beforePolicies.add(position, policy);
                } else {
                    if (position == beforePolicies.size() - 1) {
                        beforePolicies.add(policy);
                    } else {
                        beforePolicies.add(position + 1, policy);
                    }
                }
                return true;
            }

            position++;
        }

        if (keyPolicy != null && keyPolicy.getName().equalsIgnoreCase(policyName)) {
            if (before) {
                beforePolicies.add(policy);
            } else {
                afterPolicies.push(policy);
            }
            return true;
        }

        position = 0;
        for (HttpPipelinePolicy toCheck : afterPolicies) {
            if (toCheck.getName().equalsIgnoreCase(policyName)) {
                if (before) {
                    afterPolicies.add(position, policy);
                } else {
                    if (position == afterPolicies.size() - 1) {
                        afterPolicies.add(policy);
                    } else {
                        afterPolicies.add(position + 1, policy);
                    }
                }
                return true;
            }

            position++;
        }

        return false;
    }

    /**
     * Replaces an existing policy with the given {@link HttpPipelinePolicy#getName()} with the provided {@code policy}.
     * <p>
     * If the policy with the given name is not found an {@link NoSuchElementException} will be thrown.
     * <p>
     * If the name of the {@code policy} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"},
     * {@code "logging"}, or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are
     * reserved for the set methods that configure the respective policies.
     *
     * @param policy The policy to set.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     * @throws NoSuchElementException If a policy with the given name is not found.
     * @throws IllegalStateException If the name of the {@code policy} is reserved.
     */
    public HttpPipelineBuilder setPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");
        String lowerCaseName = policy.getName().toLowerCase();
        if (!policyNames.contains(lowerCaseName)) {
            throw new NoSuchElementException("A policy with the name '" + policy.getName() + "' was not found.");
        }

        if (RESERVED.contains(lowerCaseName)) {
            throw new IllegalStateException("The policy name is reserved: " + policy.getName());
        }

        return setOrRemove(policy, policy.getName());
    }

    /**
     * Removes the policy with the given {@code policyName} from the pipeline.
     * <p>
     * If a policy with the given name is not found this is a no-op.
     * <p>
     * If the {@code policyName} is any of {@code "redirect"}, {@code "retry"}, {@code "credential"}, {@code "logging"},
     * or {@code "telemetry"} an {@link IllegalStateException} will be thrown. These names are reserved for the set
     * methods that configure the respective policies.
     *
     * @param policyName The name of the policy to remove.
     * @return The updated HttpPipelineBuilder object.
     * @throws NullPointerException If {@code policyName} is null.
     * @throws IllegalStateException If the {@code policyName} is reserved.
     */
    public HttpPipelineBuilder removePolicy(String policyName) {
        Objects.requireNonNull(policyName, "'policyName' cannot be null.");
        String lowerCaseName = policyName.toLowerCase();
        if (!policyNames.contains(lowerCaseName)) {
            return this;
        }

        if (RESERVED.contains(lowerCaseName)) {
            throw new IllegalStateException("The policy name is reserved: " + policyName);
        }

        return setOrRemove(null, policyName);
    }

    private HttpPipelineBuilder setOrRemove(HttpPipelinePolicy policy, String policyName) {
        if (attemptToSetOrRemove(policy, policyName, beforeRedirect, afterCredential)) {
            return this;
        }

        if (attemptToSetOrRemove(policy, policyName, beforeRetry, afterRetry)) {
            return this;
        }

        if (attemptToSetOrRemove(policy, policyName, beforeCredential, afterCredential)) {
            return this;
        }

        if (attemptToSetOrRemove(policy, policyName, beforeLogging, afterLogging)) {
            return this;
        }

        if (attemptToSetOrRemove(policy, policyName, beforeTelemetry, afterTelemetry)) {
            return this;
        }

        throw new IllegalStateException("Should not reach here.");
    }

    private boolean attemptToSetOrRemove(HttpPipelinePolicy policy, String policyName,
        LinkedList<HttpPipelinePolicy> beforePolicies, LinkedList<HttpPipelinePolicy> afterPolicies) {
        int position = 0;
        for (HttpPipelinePolicy toCheck : beforePolicies) {
            if (toCheck.getName().equalsIgnoreCase(policyName)) {
                if (policy != null) {
                    beforePolicies.set(position, policy);
                } else {
                    beforePolicies.remove(position);
                }
                return true;
            }

            position++;
        }

        position = 0;
        for (HttpPipelinePolicy toCheck : afterPolicies) {
            if (toCheck.getName().equalsIgnoreCase(policyName)) {
                if (policy != null) {
                    afterPolicies.set(position, policy);
                } else {
                    afterPolicies.remove(position);
                }
                return true;
            }

            position++;
        }

        return false;
    }
}

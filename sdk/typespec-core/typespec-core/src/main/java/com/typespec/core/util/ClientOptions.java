// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.http.policy.UserAgentPolicy;
import com.typespec.core.util.logging.ClientLogger;

import java.util.Collections;

/**
 * General configuration options for clients.
 */
@Fluent
public class ClientOptions {
    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH = "'applicationId' length cannot be greater than "
        + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";

    // ClientOptions is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(ClientOptions.class);
    private Iterable<Header> headers;

    private String applicationId;

    private MetricsOptions metricsOptions;
    private TracingOptions tracingOptions;

    /**
     * Creates a new instance of {@link ClientOptions}.
     */
    public ClientOptions() {
    }

    /**
     * Gets the application ID.
     *
     * @return The application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID.
     * <p>
     * The {@code applicationId} is used to configure {@link UserAgentPolicy} for telemetry/monitoring purposes.
     * <p>
     * See <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry
     * policy</a> for additional information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create ClientOptions with application ID 'myApplicationId'</p>
     *
     * <!-- src_embed com.azure.core.util.ClientOptions.setApplicationId#String -->
     * <pre>
     * ClientOptions clientOptions = new ClientOptions&#40;&#41;
     *     .setApplicationId&#40;&quot;myApplicationId&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ClientOptions.setApplicationId#String -->
     *
     * @param applicationId The application ID.
     *
     * @return The updated ClientOptions object.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or is larger than 24 characters in
     * length.
     */
    public ClientOptions setApplicationId(String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_LENGTH));
            } else if (applicationId.contains(" ")) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE));
            }
        }

        this.applicationId = applicationId;

        return this;
    }

    /**
     * Sets the {@link Header Headers}.
     * <p>
     * The passed headers are applied to each request sent with the client.
     * <p>
     * This overwrites all previously set headers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create ClientOptions with Header 'myCustomHeader':'myStaticValue'</p>
     *
     * <!-- src_embed com.azure.core.util.ClientOptions.setHeaders#Iterable -->
     * <pre>
     * ClientOptions clientOptions = new ClientOptions&#40;&#41;
     *     .setHeaders&#40;Collections.singletonList&#40;new Header&#40;&quot;myCustomHeader&quot;, &quot;myStaticValue&quot;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ClientOptions.setHeaders#Iterable -->
     *
     * @param headers The headers.
     * @return The updated {@link ClientOptions} object.
     */
    public ClientOptions setHeaders(Iterable<Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the {@link Header Headers}.
     *
     * @return The {@link Header Headers}, if headers weren't set previously an empty list is returned.
     */
    public Iterable<Header> getHeaders() {
        if (headers == null) {
            return Collections.emptyList();
        }
        return headers;
    }

    /**
     * Sets {@link MetricsOptions} that are applied to each metric reported by the client.
     * Use metrics options to enable and disable metrics or pass implementation-specific configuration.
     *
     * @param metricsOptions instance of {@link MetricsOptions} to set.
     * @return The updated {@link ClientOptions} object.
     */
    public ClientOptions setMetricsOptions(MetricsOptions metricsOptions) {
        this.metricsOptions = metricsOptions;
        return this;
    }

    /**
     * Gets {@link MetricsOptions}
     * @return The {@link MetricsOptions} instance, if metric options weren't set previously, {@code null} is returned.
     */
    public MetricsOptions getMetricsOptions() {
        return metricsOptions;
    }

    /**
     * Sets {@link TracingOptions} that are applied to each tracing reported by the client.
     * Use tracing options to enable and disable tracing or pass implementation-specific configuration.
     *
     * @param tracingOptions instance of {@link TracingOptions} to set.
     * @return The updated {@link ClientOptions} object.
     */
    public ClientOptions setTracingOptions(TracingOptions tracingOptions) {
        this.tracingOptions = tracingOptions;
        return this;
    }

    /**
     * Gets {@link MetricsOptions}
     * @return The {@link MetricsOptions} instance, if metric options weren't set previously, {@code null} is returned.
     */
    public TracingOptions getTracingOptions() {
        return tracingOptions;
    }
}

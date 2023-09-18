// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class HttpPipelineBuilder {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE =
        "https://monitor.azure.com//.default";

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private TokenCredential credential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private List<HttpPipelinePolicy> httpPipelinePolicies;
    private ClientOptions clientOptions;
    // cached, based on above properties
    private HttpPipeline cachedHttpPipeline;

    private HttpPipeline explicitHttpPipeline;

    void credential(TokenCredential credential) {
        this.credential = credential;
        cachedHttpPipeline = null;
    }

    void httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        cachedHttpPipeline = null;
    }

    void httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        cachedHttpPipeline = null;
    }

    void addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        httpPipelinePolicies.add(
            Objects.requireNonNull(httpPipelinePolicy, "'policy' cannot be null."));
        cachedHttpPipeline = null;
    }

    void clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        cachedHttpPipeline = null;
    }

    void httpPipeline(HttpPipeline httpPipeline) {
        this.explicitHttpPipeline = httpPipeline;
    }

    HttpPipeline build() {
        if (explicitHttpPipeline != null) {
            return explicitHttpPipeline;
        }
        if (cachedHttpPipeline == null) {
            cachedHttpPipeline = create(httpLogOptions, clientOptions, credential, httpPipelinePolicies, httpClient);
        }
        return cachedHttpPipeline;
    }

    private static HttpPipeline create(HttpLogOptions httpLogOptions,
                                       ClientOptions clientOptions,
                                       TokenCredential credential,
                                       List<HttpPipelinePolicy> httpPipelinePolicies,
                                       HttpClient httpClient) {

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
        }
        policies.addAll(httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(new NoopTracer())
            .build();
    }

    private void validate() {
        if (explicitHttpPipeline != null) {
            if (credential != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'credential' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpClient != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpClient' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpLogOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpLogOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (!httpPipelinePolicies.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpPipelinePolicies' is not supported when custom 'httpPipeline' is specified"));
            }
            if (clientOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'clientOptions' is not supported when custom 'httpPipeline' is specified"));
            }
        }
    }
}

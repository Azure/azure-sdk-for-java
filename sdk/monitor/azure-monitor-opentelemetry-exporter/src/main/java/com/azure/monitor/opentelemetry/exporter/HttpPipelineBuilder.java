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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HttpPipelineBuilder {

    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE =
        "https://monitor.azure.com//.default";

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private final TokenCredential credential;

    private final HttpClient httpClient;
    private final HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> httpPipelinePolicies;

    private final Configuration configuration;
    private final ClientOptions clientOptions;
    private final HttpPipeline httpPipeline;

    HttpPipelineBuilder(TokenCredential credential, HttpClient httpClient, HttpLogOptions httpLogOptions,
                        List<HttpPipelinePolicy> httpPipelinePolicies,
                        Configuration configuration, ClientOptions clientOptions,
                        HttpPipeline httpPipeline) {
        this.credential = credential;
        this.httpClient = httpClient;
        this.httpLogOptions = httpLogOptions;
        this.httpPipelinePolicies = httpPipelinePolicies;
        this.configuration = configuration;
        this.clientOptions = clientOptions;
        this.httpPipeline = httpPipeline;
    }


    // TODO (trask) pass in OpenTelemetry ConfigProperties
    HttpPipeline build() {
        if (httpPipeline != null) {
            // TODO (trask) log warning if any of the other parameters were non-null since they will go unused
            return httpPipeline;
        } else {
            return build(configuration, httpLogOptions, clientOptions, credential, httpPipelinePolicies, httpClient);
        }
    }

    private static HttpPipeline build(Configuration configuration,
                                      HttpLogOptions httpLogOptions,
                                      ClientOptions clientOptions,
                                      TokenCredential credential,
                                      List<HttpPipelinePolicy> httpPipelinePolicies,
                                      HttpClient httpClient) {

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, configuration));
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
}

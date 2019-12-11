// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the base class to be extended by client builders: {@link SearchIndexClientBuilder SearchIndexClientBuilder}
 * and {@link SearchServiceClientBuilder SearchServiceClientBuilder}, and it includes common variables and methods
 * used by both builders
 */
@ServiceClientBuilder(serviceClients = {})
class SearchClientBuilder {
    private static final String SEARCH_PROPERTIES = "azure-search.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    SearchApiKeyCredential searchApiKeyCredential;
    SearchServiceVersion apiVersion;
    String endpoint;
    HttpClient httpClient;
    HttpLogOptions httpLogOptions;
    Configuration configuration;
    List<HttpPipelinePolicy> policies;
    private String clientName;
    private String clientVersion;

    void init() {
        apiVersion = SearchServiceVersion.getLatest();
        policies = new ArrayList<>();
        httpClient = HttpClient.createDefault();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(SEARCH_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    HttpPipeline prepareForBuildClient() {
        // Global Env configuration store
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        if (searchApiKeyCredential != null) {
            this.policies.add(new SearchApiKeyPipelinePolicy(searchApiKeyCredential));
        }

        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}

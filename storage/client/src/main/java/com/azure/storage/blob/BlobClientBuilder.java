// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.configuration.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.*;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

final class BlobClientBuilder {
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private ICredentials credentials = new AnonymousCredentials();
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    BlobClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    AzureBlobStorageImpl build() {
        Objects.requireNonNull(endpoint);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
//
//        policies.add(new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION));
//        policies.add(new RequestIdPolicy());
//        policies.add(new AddDatePolicy());
//        policies.add(credentials); // This needs to be a different credential type.
//        HttpPolicyProviders.addBeforeRetryPolicies(policies);
//
//        policies.add(retryPolicy);
//
//        policies.addAll(this.policies);
//        HttpPolicyProviders.addAfterRetryPolicies(policies);
//        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        AzureBlobStorageImpl azureBlobStorage = new AzureBlobStorageImpl(pipeline);

        return azureBlobStorage;
    }

    public BlobAsyncClient buildAsync() {
        return new BlobAsyncClient(this.build());
    }

    public BlobClient buildSync() {
        return new BlobClient(this.build());
    }

    public BlobClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    public BlobClientBuilder credentials(SharedKeyCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public BlobClientBuilder credentials(TokenCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public BlobClientBuilder anonymousCredentials() {
        this.credentials = new AnonymousCredentials();
        return this;
    }

    public BlobClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);

        Map<String, String> connectionKVPs = new HashMap<>();
        for (String s : connectionString.split(";")) {
            String[] kvp = s.split("=", 2);
            connectionKVPs.put(kvp[0].toLowerCase(), kvp[1]);
        }

        String accountName = connectionKVPs.get(ACCOUNT_NAME);
        String accountKey = connectionKVPs.get(ACCOUNT_KEY);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        // Use accountName and accountKey to get the SAS token using the credential class.

        return this;
    }

    public BlobClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public BlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    public BlobClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public BlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}

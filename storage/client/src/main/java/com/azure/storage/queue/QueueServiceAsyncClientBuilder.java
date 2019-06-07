// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.queue.credentials.SASTokenCredential;
import com.azure.storage.queue.credentials.SharedKeyCredential;
import com.azure.storage.queue.policy.SASTokenCredentialPolicy;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class QueueServiceAsyncClientBuilder {
    // Pieces of the connection string that are needed.
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();

    // Pieces of the URL query parameters that are part of the SAS token
    private static final String SIGNED_VERSION = "sv";
    private static final String SIGNED_SERVICES = "ss";
    private static final String SIGNED_RESOURCE_TYPES = "srt";
    private static final String SIGNED_PERMISSIONS = "sp";
    private static final String SIGNED_EXPIRY = "se";
    private static final String SIGNED_START = "st"; // Optional
    private static final String SIGNED_PROTOCOL = "spr"; // Optional
    private static final String SIGNATURE = "sig";
    private static final String SIGNED_IP = "sip"; // Optional

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private SASTokenCredential sasTokenCredential;
    private SharedKeyCredential sharedKeyCredential;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Configuration configuration;

    QueueServiceAsyncClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
        configuration = ConfigurationManager.getConfiguration();
    }

    QueueServiceAsyncClient build() {
        Objects.requireNonNull(endpoint);

        if (sasTokenCredential == null && sharedKeyCredential == null) {
            throw new IllegalArgumentException("Credentials are required for authorization");
        }
        Objects.requireNonNull(sasTokenCredential);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(QueueConfiguration.NAME, QueueConfiguration.VERSION, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());
        policies.add(new SASTokenCredentialPolicy(sasTokenCredential));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(logLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new QueueServiceAsyncClient(endpoint, pipeline);
    }

    public QueueServiceAsyncClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            URL fullURL = new URL(endpoint);
            this.endpoint = new URL(fullURL.getProtocol() + "://" + fullURL.getHost());
            SASTokenCredential credential = getCredentialFromQueryParam(fullURL.getQuery());
            if (credential != null) { // Should the SASTokenCredential only update if it isn't set as well?
                this.sasTokenCredential = credential;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    public QueueServiceAsyncClientBuilder credentials(SASTokenCredential credentials) {
        this.sasTokenCredential = credentials;
        return this;
    }

    public QueueServiceAsyncClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.sharedKeyCredential = getSharedKeyFromConnectionString(connectionString);
        return this;
    }

    public QueueServiceAsyncClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public QueueServiceAsyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(pipelinePolicy);
        return this;
    }

    public QueueServiceAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public QueueServiceAsyncClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    static SharedKeyCredential getSharedKeyFromConnectionString(String connectionString) {
        Map<String, String> connectionStringPieces = new HashMap<>();
        for (String connectionStringPiece : connectionString.split(";")) {
            String[] kvp = connectionStringPiece.split("=", 2);
            connectionStringPieces.put(kvp[0].toLowerCase(), kvp[1]);
        }

        String accountName = connectionStringPieces.get(ACCOUNT_NAME);
        String accountKey = connectionStringPieces.get(ACCOUNT_KEY);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        return new SharedKeyCredential(accountName, accountKey);
    }

    static SASTokenCredential getCredentialFromQueryParam(String queryParam) {
        if (ImplUtils.isNullOrEmpty(queryParam)) {
            return null;
        }

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder("?" + queryParam);
        Map<String, List<String>> queryParams = queryStringDecoder.parameters();
        if (queryParams.size() < 6) {
            return null;
        }

        if (!queryParams.containsKey(SIGNED_VERSION)
            || !queryParams.containsKey(SIGNED_SERVICES)
            || !queryParams.containsKey(SIGNED_RESOURCE_TYPES)
            || !queryParams.containsKey(SIGNED_PERMISSIONS)
            || !queryParams.containsKey(SIGNED_EXPIRY)
            || !queryParams.containsKey(SIGNATURE)) {
            return null;
        }

        QueryStringEncoder queryStringEncoder = new QueryStringEncoder("");
        queryStringEncoder.addParam(SIGNED_VERSION, String.join(",", queryParams.get(SIGNED_VERSION)));
        queryStringEncoder.addParam(SIGNED_SERVICES, String.join(",", queryParams.get(SIGNED_SERVICES)));
        queryStringEncoder.addParam(SIGNED_RESOURCE_TYPES, String.join(",", queryParams.get(SIGNED_RESOURCE_TYPES)));
        queryStringEncoder.addParam(SIGNED_PERMISSIONS, String.join(",", queryParams.get(SIGNED_PERMISSIONS)));
        queryStringEncoder.addParam(SIGNED_EXPIRY, String.join(",", queryParams.get(SIGNED_EXPIRY)));
        queryStringEncoder.addParam(SIGNATURE, String.join(",", queryParams.get(SIGNATURE)));

        // SIGNED_IP is optional
        if (queryParams.containsKey(SIGNED_IP)) {
            queryStringEncoder.addParam(SIGNED_IP, String.join(",", queryParams.get(SIGNED_IP)));
        }

        // SIGNED_START is optional
        if (queryParams.containsKey(SIGNED_START)) {
            queryStringEncoder.addParam(SIGNED_START, String.join(",", queryParams.get(SIGNED_START)));
        }

        // SIGNED_PROTOCOL is optional
        if (queryParams.containsKey(SIGNED_PROTOCOL)) {
            queryStringEncoder.addParam(SIGNED_PROTOCOL, String.join(",", queryParams.get(SIGNED_PROTOCOL)));
        }

        return new SASTokenCredential(queryStringEncoder.toString().substring(1));
    }
}

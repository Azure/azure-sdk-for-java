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
import com.azure.storage.queue.policy.SASTokenCredentialPolicy;

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
    private static final String SV = "sv";
    private static final String SS = "ss";
    private static final String SRT = "srt";
    private static final String SP = "sp";
    private static final String SE = "se";
    private static final String ST = "st";
    private static final String SPR = "spr";
    private static final String SIG = "sig";
    private static final String SIP = "sip";

    private final List<HttpPipelinePolicy> policies;

    private URL endpoint;
    private SASTokenCredential credentials;
    private HttpClient httpClient;
    private HttpLogDetailLevel logLevel;
    private RetryPolicy retryPolicy;
    private Map<String, String> connectionStringPieces;
    private Configuration configuration;

    QueueServiceAsyncClientBuilder() {
        retryPolicy = new RetryPolicy();
        logLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    QueueServiceAsyncClient build() {
        // TODO alzimmer: Attempt to find the credential information in the configuration
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(credentials);

        Configuration buildConfiguration = (configuration == null) ? ConfigurationManager.getConfiguration().clone() : configuration;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(QueueConfiguration.NAME, QueueConfiguration.VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());
        policies.add(new SASTokenCredentialPolicy(credentials)); // This needs to be a different credential type.
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
            String[] urlPieces = endpoint.split("\\?");
            this.endpoint = new URL(urlPieces[0]);
            SASTokenCredential credential = getCredentialFromQueryParam(urlPieces[1]);
            if (credential != null) {
                this.credentials = credential;
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.");
        }

        return this;
    }

    public QueueServiceAsyncClientBuilder credentials(SASTokenCredential credentials) {
        this.credentials = credentials;
        return this;
    }

    public QueueServiceAsyncClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        this.connectionStringPieces = parseConnectionString(connectionString);
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

    static Map<String, String> parseConnectionString(String connectionString) {
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

        return connectionStringPieces;
    }

    static SASTokenCredential getCredentialFromQueryParam(String queryParam) {
        if (ImplUtils.isNullOrEmpty(queryParam)) {
            return null;
        }

        Map<String, String> queryParamPieces = new HashMap<>();
        for (String queryParamPiece : queryParam.split("&")) {
            String[] kvp = queryParamPiece.split("=", 2);
            queryParamPieces.put(kvp[0], kvp[1]);
        }

        if (queryParamPieces.size() < 8) {
            return null;
        }

        String sv = queryParamPieces.get(SV);
        String ss = queryParamPieces.get(SS);
        String srt = queryParamPieces.get(SRT);
        String sp = queryParamPieces.get(SP);
        String se = queryParamPieces.get(SE);
        String st = queryParamPieces.get(ST);
        String spr = queryParamPieces.get(SPR);
        String sig = queryParamPieces.get(SIG);
        String sip = queryParamPieces.get(SIP); // SIP is an optional allowed IP range

        // If any SAS token pieces are missing we cannot create the credential
        if (ImplUtils.isNullOrEmpty(sv)
            || ImplUtils.isNullOrEmpty(ss)
            || ImplUtils.isNullOrEmpty(srt)
            || ImplUtils.isNullOrEmpty(sp)
            || ImplUtils.isNullOrEmpty(se)
            || ImplUtils.isNullOrEmpty(st)
            || ImplUtils.isNullOrEmpty(spr)
            || ImplUtils.isNullOrEmpty(sig)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        addQueryParam(sb, SV, sv);
        sb.append('&');
        addQueryParam(sb, SS, ss);
        sb.append('&');
        addQueryParam(sb, SRT, srt);
        sb.append('&');
        addQueryParam(sb, SP, sp);
        sb.append('&');
        addQueryParam(sb, SE, se);
        sb.append('&');
        addQueryParam(sb, ST, st);
        sb.append('&');
        addQueryParam(sb, SPR, spr);
        sb.append('&');
        addQueryParam(sb, SIG, sig);

        if (!ImplUtils.isNullOrEmpty(sip)) {
            sb.append('&');
            addQueryParam(sb, SIP, sip);
        }

        return new SASTokenCredential(sb.toString());
    }

    private static void addQueryParam(StringBuilder sb, String paramName, String paramValue) {
        sb.append(paramName);
        sb.append('=');
        sb.append(paramValue);
    }
}

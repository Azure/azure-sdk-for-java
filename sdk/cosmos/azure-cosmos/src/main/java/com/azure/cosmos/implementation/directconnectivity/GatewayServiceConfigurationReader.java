// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.RequestVerb;
import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.DatabaseAccount;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ReplicationPolicy;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will read the service configuration from the gateway.
 *
 * As .Net does code sharing between the SDK and GW there are two implementation to IServiceConfigurationReader
 * GatewayServiceConfigurationReader which is for SDK
 * DatabaseAccountConfigurationReader which is for GW
 * Some of the APIs are not relevant in SDK and due to that in .Net the SDK implementation one throws not-implemented.
 *
 * In java, as we don't do code sharing
 * and we got rid of the interface which is not needed and only implemented the methods in GatewayServiceConfigurationReader
 */
public class GatewayServiceConfigurationReader {

    public static final String GATEWAY_READER_NOT_INITIALIZED = "GatewayServiceConfigurationReader has not been initialized";

    public ReplicationPolicy userReplicationPolicy;
    private ReplicationPolicy systemReplicationPolicy;
    private ConsistencyLevel consistencyLevel;
    private volatile boolean initialized;
    private URI serviceEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private Map<String, Object> queryEngineConfiguration;
    private final BaseAuthorizationTokenProvider baseAuthorizationTokenProvider;
    private final boolean hasAuthKeyResourceToken;
    private final String authKeyResourceToken;
    private HttpClient httpClient;

    public GatewayServiceConfigurationReader(URI serviceEndpoint, boolean hasResourceToken, String resourceToken,
            ConnectionPolicy connectionPolicy, BaseAuthorizationTokenProvider baseAuthorizationTokenProvider,
            HttpClient httpClient) {
        this.serviceEndpoint = serviceEndpoint;
        this.baseAuthorizationTokenProvider = baseAuthorizationTokenProvider;
        this.hasAuthKeyResourceToken = hasResourceToken;
        this.authKeyResourceToken = resourceToken;
        this.connectionPolicy = connectionPolicy;
        this.httpClient = httpClient;
    }

    public ReplicationPolicy getUserReplicationPolicy() {
        this.throwIfNotInitialized();
        return this.userReplicationPolicy;
    }

    public ReplicationPolicy getSystemReplicationPolicy() {
        this.throwIfNotInitialized();
        return this.systemReplicationPolicy;
    }

    public boolean enableAuthorization() {
        return true;
    }

    public ConsistencyLevel getDefaultConsistencyLevel() {
        this.throwIfNotInitialized();
        return this.consistencyLevel;
    }

    public void setDefaultConsistencyLevel(ConsistencyLevel value) {
        this.throwIfNotInitialized();
        this.consistencyLevel = value;
    }

    public Map<String, Object> getQueryEngineConfiguration() {
        this.throwIfNotInitialized();
        return this.queryEngineConfiguration;
    }

    private Mono<DatabaseAccount> getDatabaseAccountAsync(URI serviceEndpoint) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);

        UserAgentContainer userAgentContainer = new UserAgentContainer();
        String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();
        if (userAgentSuffix != null && userAgentSuffix.length() > 0) {
            userAgentContainer.setSuffix(userAgentSuffix);
        }

        httpHeaders.set(HttpConstants.HttpHeaders.USER_AGENT, userAgentContainer.getUserAgent());
        httpHeaders.set(HttpConstants.HttpHeaders.API_TYPE, Constants.Properties.SQL_API_TYPE);

        String xDate = Utils.nowAsRFC1123();
        httpHeaders.set(HttpConstants.HttpHeaders.X_DATE, xDate);

        String authorizationToken;
        if (this.hasAuthKeyResourceToken || baseAuthorizationTokenProvider == null) {
            authorizationToken = HttpUtils.urlEncode(this.authKeyResourceToken);
        } else {
            // Retrieve the document service properties.
            Map<String, String> header = new HashMap<>();
            header.put(HttpConstants.HttpHeaders.X_DATE, xDate);
            authorizationToken = baseAuthorizationTokenProvider
                    .generateKeyAuthorizationSignature(RequestVerb.GET, serviceEndpoint, header);
        }
        httpHeaders.set(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, serviceEndpoint, serviceEndpoint.getPort(), httpHeaders);
        Mono<HttpResponse> httpResponse = httpClient.send(httpRequest);
        return toDatabaseAccountObservable(httpResponse, httpRequest);
    }

    public Mono<DatabaseAccount> initializeReaderAsync() {
            return GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.serviceEndpoint,

                    new ArrayList<>(this.connectionPolicy.getPreferredLocations()), url -> {
                            return getDatabaseAccountAsync(url);

                    }).doOnSuccess(databaseAccount -> {
                        userReplicationPolicy = BridgeInternal.getReplicationPolicy(databaseAccount);
                        systemReplicationPolicy = BridgeInternal.getSystemReplicationPolicy(databaseAccount);
                        queryEngineConfiguration = BridgeInternal.getQueryEngineConfiuration(databaseAccount);
                        consistencyLevel = BridgeInternal.getConsistencyPolicy(databaseAccount).getDefaultConsistencyLevel();
                        initialized = true;
                    });
    }

    private Mono<DatabaseAccount> toDatabaseAccountObservable(Mono<HttpResponse> httpResponse, HttpRequest httpRequest) {

        return HttpClientUtils.parseResponseAsync(httpResponse, httpRequest)
                .map(rxDocumentServiceResponse -> rxDocumentServiceResponse.getResource(DatabaseAccount.class));
    }

    private void throwIfNotInitialized() {
        if (!this.initialized) {
            throw new IllegalArgumentException(GATEWAY_READER_NOT_INITIALIZED);
        }
    }
}

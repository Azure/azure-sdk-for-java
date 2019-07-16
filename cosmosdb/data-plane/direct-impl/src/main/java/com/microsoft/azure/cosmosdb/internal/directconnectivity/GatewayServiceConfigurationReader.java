/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DatabaseAccount;
import com.microsoft.azure.cosmosdb.ReplicationPolicy;
import com.microsoft.azure.cosmosdb.internal.BaseAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.internal.GlobalEndpointManager;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;

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
    private CompositeHttpClient<ByteBuf, ByteBuf> httpClient;

    public GatewayServiceConfigurationReader(URI serviceEndpoint, boolean hasResourceToken, String resourceToken,
            ConnectionPolicy connectionPolicy, BaseAuthorizationTokenProvider baseAuthorizationTokenProvider,
            CompositeHttpClient<ByteBuf, ByteBuf> httpClient) {
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

    private Single<DatabaseAccount> getDatabaseAccountAsync(URI serviceEndpoint) {
        HttpClientRequest<ByteBuf> httpRequest = HttpClientRequest.create(HttpMethod.GET,
                this.serviceEndpoint.toString());

        httpRequest.withHeader(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);

        UserAgentContainer userAgentContainer = new UserAgentContainer();
        String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();
        if (userAgentSuffix != null && userAgentSuffix.length() > 0) {
            userAgentContainer.setSuffix(userAgentSuffix);
        }

        httpRequest.withHeader(HttpConstants.HttpHeaders.USER_AGENT, userAgentContainer.getUserAgent());
        httpRequest.withHeader(HttpConstants.HttpHeaders.API_TYPE, Constants.Properties.SQL_API_TYPE);
        String authorizationToken = StringUtils.EMPTY;
        if (this.hasAuthKeyResourceToken || baseAuthorizationTokenProvider == null) {
            authorizationToken = HttpUtils.urlEncode(this.authKeyResourceToken);
        } else {
            // Retrieve the document service properties.
            String xDate = Utils.nowAsRFC1123();
            httpRequest.withHeader(HttpConstants.HttpHeaders.X_DATE, xDate);
            Map<String, String> header = new HashMap<>();
            header.put(HttpConstants.HttpHeaders.X_DATE, xDate);
            authorizationToken = baseAuthorizationTokenProvider
                    .generateKeyAuthorizationSignature(HttpConstants.HttpMethods.GET, serviceEndpoint, header);
        }

        httpRequest.withHeader(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);
        RxClient.ServerInfo serverInfo = new RxClient.ServerInfo(serviceEndpoint.getHost(), serviceEndpoint.getPort());

        Observable<HttpClientResponse<ByteBuf>> clientResponseObservable = this.httpClient.submit(serverInfo,
                httpRequest);
        return toDatabaseAccountObservable(clientResponseObservable.toSingle());
    }

    public Single<DatabaseAccount> initializeReaderAsync() {
        try {
            return GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.serviceEndpoint.toURL(),

                    new ArrayList<>(this.connectionPolicy.getPreferredLocations()), url -> {
                        try {
                            return getDatabaseAccountAsync(url.toURI());
                        } catch (URISyntaxException e) {
                            throw new IllegalArgumentException("URI " + url);
                        }
                    }).doOnSuccess(new Action1<DatabaseAccount>() {

                        @Override
                        public void call(DatabaseAccount databaseAccount) {
                            userReplicationPolicy = BridgeInternal.getReplicationPolicy(databaseAccount);
                            systemReplicationPolicy = BridgeInternal.getSystemReplicationPolicy(databaseAccount);
                            queryEngineConfiguration = BridgeInternal.getQueryEngineConfiuration(databaseAccount);
                            consistencyLevel = BridgeInternal.getConsistencyPolicy(databaseAccount).getDefaultConsistencyLevel();
                            initialized = true;
                        }
                    });
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(this.serviceEndpoint.toString(), e);
        }
    }

    private Single<DatabaseAccount> toDatabaseAccountObservable(
            Single<HttpClientResponse<ByteBuf>> clientResponseObservable) {
        return clientResponseObservable.flatMap(clientResponse -> {
            return HttpClientUtils.parseResponseAsync(clientResponse)
                    .map(rxDocumentServiceResponse -> rxDocumentServiceResponse.getResource(DatabaseAccount.class));
        });
    }

    private void throwIfNotInitialized() {
        if (!this.initialized) {
            throw new IllegalArgumentException(GATEWAY_READER_NOT_INITIALIZED);
        }
    }
}

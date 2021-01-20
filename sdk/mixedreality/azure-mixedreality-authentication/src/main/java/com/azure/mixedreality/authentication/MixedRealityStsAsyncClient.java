// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.mixedreality.authentication.implementation.MixedRealityStsRestClientImpl;

import reactor.core.publisher.Mono;

/**
 * Represents the Mixed Reality STS client for retrieving STS tokens used to access Mixed Reality services.
 *
 * @see MixedRealityStsClientBuilder
 */
@ServiceClient(builder = MixedRealityStsClientBuilder.class, isAsync = true)
public final class MixedRealityStsAsyncClient {
    private final ClientLogger logger = new ClientLogger(MixedRealityStsAsyncClient.class);
    private final MixedRealityStsRestClientImpl serviceClient;
    private final MixedRealityStsServiceVersion version;

    /**
     * Creates a {@link MixedRealityStsAsyncClient} that sends requests to the Mixed Reality STS service. Each
     * service call goes through the {@code pipeline}.
     *
     * @param serviceClient The service client used to make service calls.
     * @param version The version of the service to use.
     */
    MixedRealityStsAsyncClient(MixedRealityStsRestClientImpl serviceClient, MixedRealityStsServiceVersion version) {
        this.serviceClient = serviceClient;
        this.version = version;
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @return An {@link AccessToken} used to access Mixed Reality services matching the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> getToken() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @return A REST response contains the {@link AccessToken} used to access Mixed Reality services matching
     * the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenWithResponse() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<AccessToken>> getTokenWithResponse(Context context) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}

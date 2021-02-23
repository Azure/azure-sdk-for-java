// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Represents the Mixed Reality STS client for retrieving STS tokens used to access Mixed Reality services.
 *
 * @see MixedRealityStsClientBuilder
 */
@ServiceClient(builder = MixedRealityStsClientBuilder.class)
public final class MixedRealityStsClient {
    private final MixedRealityStsAsyncClient asyncClient;

    /**
     * Creates a {@link MixedRealityStsClient} that sends requests to the Mixed Reality STS service. Each
     * service call goes through the {@code pipeline}.
     *
     * @param asyncClient The {@link MixedRealityStsAsyncClient} that the client routes its requests through.
     */
    MixedRealityStsClient(MixedRealityStsAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @return An {@link AccessToken} used to access Mixed Reality services matching the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken getToken() {
        return this.asyncClient.getToken().block();
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response contains the {@link AccessToken} used to access Mixed Reality services matching
     * the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessToken> getTokenWithResponse(Context context) {
        return this.asyncClient.getTokenWithResponse().block();
    }
}

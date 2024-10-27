// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.mixedreality.authentication.implementation.MixedRealityStsRestClientImpl;

import com.azure.mixedreality.authentication.implementation.models.StsTokenResponseMessage;
import com.azure.mixedreality.authentication.implementation.models.TokenRequestOptions;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Represents the Mixed Reality STS client for retrieving STS tokens used to access Mixed Reality services.
 *
 * @see MixedRealityStsClientBuilder
 */
@ServiceClient(builder = MixedRealityStsClientBuilder.class, isAsync = true)
public final class MixedRealityStsAsyncClient {
    private static final String MIXED_REALITY_TRACING_NAMESPACE_VALUE = "Microsoft.MixedReality";

    private final UUID accountId;
    private final ClientLogger logger = new ClientLogger(MixedRealityStsAsyncClient.class);
    private final MixedRealityStsRestClientImpl serviceClient;

    /**
     * Creates a {@link MixedRealityStsAsyncClient} that sends requests to the Mixed Reality STS service. Each
     * service call goes through the {@code pipeline}.
     *
     * @param accountId The Mixed Reality service account identifier.
     * @param serviceClient The service client used to make service calls.
     */
    MixedRealityStsAsyncClient(UUID accountId, MixedRealityStsRestClientImpl serviceClient) {
        this.accountId = accountId;
        this.serviceClient = serviceClient;
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @return An {@link AccessToken} used to access Mixed Reality services matching the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> getToken() {
        try {
            return this.getTokenWithResponse()
                .map(response -> response.getValue());
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    /**
     * Retrieve a token from the STS service for the specified account information.
     *
     * @return A REST response contains the {@link AccessToken} used to access Mixed Reality services matching
     * the account's permissions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenWithResponse() {
        try {
            return withContext(context -> this.getTokenWithResponse(context));
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    Mono<Response<AccessToken>> getTokenWithResponse(Context context) {
        try {
            TokenRequestOptions requestOptions = new TokenRequestOptions();
            requestOptions.setClientRequestId(CorrelationVector.generateCvBase());

            return serviceClient.getTokenWithResponseAsync(this.accountId, requestOptions, context
                .addData(Tracer.AZ_TRACING_NAMESPACE_KEY, MIXED_REALITY_TRACING_NAMESPACE_VALUE))
                .map(originalResponse -> {
                    AccessToken accessToken = toAccessToken(originalResponse.getValue());
                    return new ResponseBase<>(originalResponse.getRequest(), originalResponse.getStatusCode(),
                        originalResponse.getHeaders(), accessToken, originalResponse.getDeserializedHeaders());
                });
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    private static AccessToken toAccessToken(StsTokenResponseMessage stsTokenResponseMessage) {
        String accessToken = stsTokenResponseMessage.getAccessToken();
        OffsetDateTime tokenExpiration = JsonWebToken.retrieveExpiration(accessToken);

        return new AccessToken(accessToken, tokenExpiration);
    }
}

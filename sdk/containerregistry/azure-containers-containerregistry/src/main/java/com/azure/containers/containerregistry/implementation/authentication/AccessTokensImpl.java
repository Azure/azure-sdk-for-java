// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in AccessTokensService. */
public final class AccessTokensImpl {

    /** The proxy service used to perform REST calls. */
    private final AccessTokensServiceImpl service;

    /** Registry login URL. */
    private final String url;

    /**
     * Gets Registry login URL.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Initializes an instance of AccessTokensImpl.
     *
     * @param url the service endpoint.
     * @param httpPipeline the pipeline to use to make the call.
     * @param serializerAdapter the serializer adapter for the rest client.
     *
     */
    public AccessTokensImpl(String url, HttpPipeline httpPipeline, SerializerAdapter serializerAdapter) {
        this.service =
            RestProxy.create(AccessTokensServiceImpl.class, httpPipeline, serializerAdapter);
        this.url = url;
    }

    /**
     * Exchange ACR Refresh token for an ACR Access Token.
     *
     * @param refreshToken The refreshToken parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcrAccessToken>> getAccessTokenWithResponseAsync(
        String grantType,
        String serviceName,
        String scope,
        String refreshToken) {
        final String accept = "application/json";
        return FluxUtil.withContext(
            context -> service.getAccessToken(getUrl(), grantType, serviceName, scope, refreshToken, accept, context));
    }
    /**
     * Exchange ACR Refresh token for an ACR Access Token.
     *
     * @param refreshToken The refreshToken parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcrAccessToken> getAccessTokenAsync(
        String grantType,
        String serviceName,
        String scope,
        String refreshToken) {
        return getAccessTokenWithResponseAsync(grantType, serviceName, scope, refreshToken)
            .flatMap(
                (Response<AcrAccessToken> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * The interface defining all the services for AccessTokens to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{url}")
    @ServiceInterface(name = "ContainerRegistryAcc")
    public interface AccessTokensServiceImpl {
        @Post("/oauth2/token")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(AcrErrorsException.class)
        Mono<Response<AcrAccessToken>> getAccessToken(
            @HostParam("url") String url,
            @FormParam(value = "grant_type") String grantType,
            @FormParam(value = "service") String service,
            @FormParam(value = "scope") String scope,
            @FormParam(value = "refresh_token") String refreshToken,
            @HeaderParam("Accept") String accept,
            Context context);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
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

/** An instance of this class provides access to all the operations defined in RefreshTokensService. */
public final class RefreshTokensImpl {
    /** The proxy service used to perform REST calls. */
    private final RefreshTokenService service;

    /** Registry login URL. */
    private final String url;

    /**
     * Gets Registry login URL.
     *
     * @return the url value.
     */
    private String getUrl() {
        return this.url;
    }

    /**
     * Initializes an instance of RefreshTokensImpl.
     *
     * @param url the service endpoint.
     * @param httpPipeline the pipeline to use to make the call.
     * @param serializerAdapter the serializer adapter for the rest client.
     *
     */
    public RefreshTokensImpl(String url, HttpPipeline httpPipeline, SerializerAdapter serializerAdapter) {
        this.service =
            RestProxy.create(
                RefreshTokenService.class,
                httpPipeline,
                serializerAdapter);
        this.url = url;
    }

    /**
     * The interface defining all the services for RegistryRefreshTokens to be used by the
     * proxy service to perform REST calls.
     */
    @Host("{url}")
    @ServiceInterface(name = "ContainerRegistryCon")
    public interface RefreshTokenService {
        // @Multipart not supported by RestProxy
        @Post("/oauth2/exchange")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(AcrErrorsException.class)
        Mono<Response<AcrRefreshToken>> getRefreshToken(
            @HostParam("url") String url,
            @FormParam(value = "grant_type", encoded = true) String grantType,
            @FormParam(value = "service", encoded = true) String service,
            @FormParam(value = "access_token", encoded = true) String accessToken,
            @FormParam(value = "tenant", encoded = true) String tenant,
            @HeaderParam("Accept") String accept,
            Context context);
    }

    /**
     * Exchange AAD tokens for an ACR refresh Token.
     *
     * @param accessToken The accessToken parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcrRefreshToken>> getRefreshTokenWithResponseAsync(
        String grantType,
        String accessToken,
        String tenant,
        String serviceValue) {
        final String accept = "application/json";
        return FluxUtil.withContext(
            context -> service.getRefreshToken(this.getUrl(), grantType, serviceValue, accessToken, tenant, accept, context));
    }

    /**
     * Exchange AAD tokens for an ACR refresh Token.
     *
     * @param accessToken The accessToken parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcrRefreshToken> getRefreshTokenAsync(
        String grantType, String accessToken, String tenant, String service) {
        return getRefreshTokenWithResponseAsync(grantType, accessToken, tenant, service)
            .flatMap(
                (Response<AcrRefreshToken> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }
}

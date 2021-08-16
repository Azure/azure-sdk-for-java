// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Authentications. */
public final class AuthenticationsImpl {
    /** The proxy service used to perform REST calls. */
    private final AuthenticationsService service;

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
    public AuthenticationsImpl(String url, HttpPipeline httpPipeline, SerializerAdapter serializerAdapter) {
        this.service =
            RestProxy.create(AuthenticationsService.class, httpPipeline, serializerAdapter);
        this.url = url;
    }

    /**
     * The interface defining all the services for ContainerRegistryAuthentications to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{url}")
    @ServiceInterface(name = "ContainerRegistryAut")
    public interface AuthenticationsService {
        @Post("/oauth2/exchange")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(AcrErrorsException.class)
        Mono<Response<AcrRefreshToken>> exchangeAadAccessTokenForAcrRefreshToken(
                @HostParam("url") String url,
                @FormParam(value = "grant_type", encoded = true) String grantType,
                @FormParam(value = "service", encoded = true) String service,
                @FormParam(value = "access_token", encoded = true) String accessToken,
                @HeaderParam("Accept") String accept,
                Context context);

        @Post("/oauth2/token")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(AcrErrorsException.class)
        Mono<Response<AcrAccessToken>> exchangeAcrRefreshTokenForAcrAccessToken(
                @HostParam("url") String url,
                @FormParam(value = "grant_type") String grantType,
                @FormParam(value = "service") String service,
                @FormParam(value = "scope") String scope,
                @FormParam(value = "refresh_token") String refreshToken,
                @HeaderParam("Accept") String accept,
                Context context);
    }

    /**
     * Exchange AAD tokens for an ACR refresh Token.
     *
     * @param serviceParam Indicates the name of your Azure container registry.
     * @param accessToken AAD access token, mandatory when grant_type is access_token_refresh_token or access_token.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcrRefreshToken>> exchangeAadAccessTokenForAcrRefreshTokenWithResponseAsync(
            String serviceParam, String accessToken) {
        final String grantType = "access_token";
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.exchangeAadAccessTokenForAcrRefreshToken(
                                this.getUrl(), grantType, serviceParam, accessToken, accept, context));
    }

    /**
     * Exchange AAD tokens for an ACR refresh Token.
     *
     * @param serviceParam Indicates the name of your Azure container registry.
     * @param accessToken AAD access token, mandatory when grant_type is access_token_refresh_token or access_token.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    //Used/
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcrRefreshToken> exchangeAadAccessTokenForAcrRefreshTokenAsync(
            String serviceParam, String accessToken) {
        return exchangeAadAccessTokenForAcrRefreshTokenWithResponseAsync(serviceParam, accessToken)
                .flatMap(
                        (Response<AcrRefreshToken> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Exchange ACR Refresh token for an ACR Access Token.
     *
     * @param serviceParam Indicates the name of your Azure container registry.
     * @param scope Which is expected to be a valid scope, and can be specified more than once for multiple scope
     *     requests. You obtained this from the Www-Authenticate response header from the challenge.
     * @param refreshToken Must be a valid ACR refresh token.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcrAccessToken>> exchangeAcrRefreshTokenForAcrAccessTokenWithResponseAsync(
            String serviceParam, String scope, String grantType, String refreshToken) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.exchangeAcrRefreshTokenForAcrAccessToken(
                                this.getUrl(), grantType, serviceParam, scope, refreshToken, accept, context));
    }

    /**
     * Exchange ACR Refresh token for an ACR Access Token.
     *
     * @param serviceParam Indicates the name of your Azure container registry.
     * @param scope Which is expected to be a valid scope, and can be specified more than once for multiple scope
     *     requests. You obtained this from the Www-Authenticate response header from the challenge.
     * @param refreshToken Must be a valid ACR refresh token.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws AcrErrorsException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    // Used/
    public Mono<AcrAccessToken> exchangeAcrRefreshTokenForAcrAccessTokenAsync(
            String serviceParam, String scope, String grantType, String refreshToken) {
        return exchangeAcrRefreshTokenForAcrAccessTokenWithResponseAsync(serviceParam, scope, grantType, refreshToken)
                .flatMap(
                        (Response<AcrAccessToken> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }
}

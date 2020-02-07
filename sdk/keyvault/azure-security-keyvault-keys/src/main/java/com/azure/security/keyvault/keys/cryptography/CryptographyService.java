// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.Put;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link CryptographyAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
@ServiceInterface(name = "KeyVault")
interface CryptographyService {

    @Post("keys/{key-name}/{key-version}/encrypt")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyOperationResult>> encrypt(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @PathParam("key-version") String keyVersion,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyOperationParameters parameters,
                                  @HeaderParam("Content-Type") String type,
                                  Context context);


    @Post("keys/{key-name}/{key-version}/decrypt")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyOperationResult>> decrypt(@HostParam("url") String url,
                                @PathParam("key-name") String keyName,
                                @PathParam("key-version") String keyVersion,
                                @QueryParam("api-version") String apiVersion,
                                @HeaderParam("accept-language") String acceptLanguage,
                                @BodyParam("body") KeyOperationParameters parameters,
                                @HeaderParam("Content-Type") String type,
                                Context context);



    @Post("keys/{key-name}/{key-version}/sign")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyOperationResult>> sign(@HostParam("url") String url,
                                @PathParam("key-name") String keyName,
                                @PathParam("key-version") String keyVersion,
                                @QueryParam("api-version") String apiVersion,
                                @HeaderParam("accept-language") String acceptLanguage,
                                @BodyParam("body") KeySignRequest parameters,
                                @HeaderParam("Content-Type") String type,
                                Context context);


    @Post("keys/{key-name}/{key-version}/verify")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVerifyResponse>> verify(@HostParam("url") String url,
                                @PathParam("key-name") String keyName,
                                @PathParam("key-version") String keyVersion,
                                @QueryParam("api-version") String apiVersion,
                                @HeaderParam("accept-language") String acceptLanguage,
                                @BodyParam("body") KeyVerifyRequest parameters,
                                @HeaderParam("Content-Type") String type,
                                Context context);



    @Post("keys/{key-name}/{key-version}/wrapkey")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyOperationResult>> wrapKey(@HostParam("url") String url,
                                @PathParam("key-name") String keyName,
                                @PathParam("key-version") String keyVersion,
                                @QueryParam("api-version") String apiVersion,
                                @HeaderParam("accept-language") String acceptLanguage,
                                @BodyParam("body") KeyWrapUnwrapRequest parameters,
                                @HeaderParam("Content-Type") String type,
                                Context context);


    @Post("keys/{key-name}/{key-version}/unwrapkey")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyOperationResult>> unwrapKey(@HostParam("url") String url,
                                @PathParam("key-name") String keyName,
                                @PathParam("key-version") String keyVersion,
                                @QueryParam("api-version") String apiVersion,
                                @HeaderParam("accept-language") String acceptLanguage,
                                @BodyParam("body") KeyWrapUnwrapRequest parameters,
                                @HeaderParam("Content-Type") String type,
                                Context context);

    @Get("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultKey>> getKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @PathParam("key-version") String keyVersion,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type,
                                       Context context);

    @Get("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<SecretKey>> getSecret(@HostParam("url") String url,
                                       @PathParam("secret-name") String keyName,
                                       @PathParam("secret-version") String keyVersion,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type,
                                       Context context);

    @Put("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<SecretKey>> setSecret(@HostParam("url") String url,
                                             @PathParam("secret-name") String secretName,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @BodyParam("body") SecretRequestParameters parameters,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.Host;
import com.azure.common.annotations.PUT;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.annotations.HostParam;
import com.azure.common.annotations.QueryParam;
import com.azure.common.annotations.HeaderParam;
import com.azure.common.annotations.BodyParam;
import com.azure.common.annotations.PathParam;
import com.azure.common.annotations.POST;
import com.azure.common.annotations.PATCH;
import com.azure.common.annotations.DELETE;
import com.azure.common.annotations.GET;
import com.azure.common.http.rest.RestException;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.http.rest.RestVoidResponse;
import com.azure.keyvault.implementation.Page;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretAttributes;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link SecretClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface SecretService {

    @PUT("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Secret>> setSecret(@HostParam("url") String url,
                                         @PathParam("secret-name") String secretName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @BodyParam("body") SecretRequestParameters parameters);

    @GET("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Secret>> getSecret(@HostParam("url") String url,
                                         @PathParam("secret-name") String secretName,
                                         @PathParam("secret-version") String secretVersion,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage);


    @PATCH("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<SecretAttributes>> updateSecret(@HostParam("url") String url,
                                                      @PathParam("secret-name") String secretName,
                                                      @PathParam("secret-version") String secretVersion,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @BodyParam("body") SecretRequestParameters parameters);


    @DELETE("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<DeletedSecret>> deleteSecret(@HostParam("url") String url,
                                                   @PathParam("secret-name") String secretName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage);


    @GET("deletedsecrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<DeletedSecret>> getDeletedSecret(@HostParam("url") String url,
                                                       @PathParam("secret-name") String secretName,
                                                       @QueryParam("api-version") String apiVersion,
                                                       @HeaderParam("accept-language") String acceptLanguage);

    @DELETE("deletedsecrets/{secret-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestVoidResponse> purgeDeletedSecret(@HostParam("url") String url,
                                              @PathParam("secret-name") String secretName,
                                              @QueryParam("api-version") String apiVersion,
                                              @HeaderParam("accept-language") String acceptLanguage);


    @POST("deletedsecrets/{secret-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Secret>> recoverDeletedSecret(@HostParam("url") String url,
                                          @PathParam("secret-name") String secretName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage);


    @POST("secrets/{secret-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<SecretBackup>> backupSecret(@HostParam("url") String url,
                                               @PathParam("secret-name") String secretName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage);



    @POST("secrets/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Secret>> restoreSecret(@HostParam("url") String url,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @BodyParam("application/json") SecretRestoreRequestParameters parameters);


    @GET("secrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<SecretAttributes>>> getSecrets(@HostParam("url") String url,
                                                          @QueryParam("maxresults") Integer maxresults,
                                                          @QueryParam("api-version") String apiVersion,
                                                          @HeaderParam("accept-language") String acceptLanguage);


    @GET("secrets/{secret-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<SecretAttributes>>> getSecretVersions(@HostParam("url") String url,
                                                                 @PathParam("secret-name") String secretName,
                                                                 @QueryParam("maxresults") Integer maxresults,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<SecretAttributes>>> getSecrets(@HostParam("url") String url,
                                                          @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                          @HeaderParam("accept-language") String acceptLanguage);


    @GET("deletedsecrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<DeletedSecret>>> getDeletedSecrets(@HostParam("url") String url,
                                                @QueryParam("maxresults") Integer maxresults,
                                                @QueryParam("api-version") String apiVersion,
                                                @HeaderParam("accept-language") String acceptLanguage);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<DeletedSecret>>> getDeletedSecrets(@HostParam("url") String url,
                                                              @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                              @HeaderParam("accept-language") String acceptLanguage);


}

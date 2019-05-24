// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.azure.core.annotations.BodyParam;
import com.azure.core.annotations.DELETE;
import com.azure.core.annotations.ExpectedResponses;
import com.azure.core.annotations.GET;
import com.azure.core.annotations.HeaderParam;
import com.azure.core.annotations.Host;
import com.azure.core.annotations.HostParam;
import com.azure.core.annotations.PATCH;
import com.azure.core.annotations.POST;
import com.azure.core.annotations.PUT;
import com.azure.core.annotations.PathParam;
import com.azure.core.annotations.QueryParam;
import com.azure.core.annotations.ReturnValueWireType;
import com.azure.core.annotations.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.keyvault.implementation.DeletedSecretPage;
import com.azure.keyvault.implementation.SecretBasePage;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretBase;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link SecretAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface SecretService {

    @PUT("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Secret>> setSecret(@HostParam("url") String url,
                                     @PathParam("secret-name") String secretName,
                                     @QueryParam("api-version") String apiVersion,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @BodyParam("body") SecretRequestParameters parameters,
                                     @HeaderParam("Content-Type") String type);

    @GET("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Secret>> getSecret(@HostParam("url") String url,
                                         @PathParam("secret-name") String secretName,
                                         @PathParam("secret-version") String secretVersion,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @PATCH("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<SecretBase>> updateSecret(@HostParam("url") String url,
                                            @PathParam("secret-name") String secretName,
                                            @PathParam("secret-version") String secretVersion,
                                            @QueryParam("api-version") String apiVersion,
                                            @HeaderParam("accept-language") String acceptLanguage,
                                            @BodyParam("body") SecretRequestParameters parameters,
                                            @HeaderParam("Content-Type") String type);


    @DELETE("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedSecret>> deleteSecret(@HostParam("url") String url,
                                                   @PathParam("secret-name") String secretName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedSecret>> getDeletedSecret(@HostParam("url") String url,
                                                       @PathParam("secret-name") String secretName,
                                                       @QueryParam("api-version") String apiVersion,
                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                       @HeaderParam("Content-Type") String type);

    @DELETE("deletedsecrets/{secret-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<VoidResponse> purgeDeletedSecret(@HostParam("url") String url,
                                          @PathParam("secret-name") String secretName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type);


    @POST("deletedsecrets/{secret-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Secret>> recoverDeletedSecret(@HostParam("url") String url,
                                                    @PathParam("secret-name") String secretName,
                                                    @QueryParam("api-version") String apiVersion,
                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                    @HeaderParam("Content-Type") String type);


    @POST("secrets/{secret-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<SecretBackup>> backupSecret(@HostParam("url") String url,
                                                  @PathParam("secret-name") String secretName,
                                                  @QueryParam("api-version") String apiVersion,
                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                  @HeaderParam("Content-Type") String type);



    @POST("secrets/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Secret>> restoreSecret(@HostParam("url") String url,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @BodyParam("application/json") SecretRestoreRequestParameters parameters,
                                             @HeaderParam("Content-Type") String type);


    @GET("secrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecrets(@HostParam("url") String url,
                                               @QueryParam("maxresults") Integer maxresults,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("secrets/{secret-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecretVersions(@HostParam("url") String url,
                                                      @PathParam("secret-name") String secretName,
                                                      @QueryParam("maxresults") Integer maxresults,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @HeaderParam("Content-Type") String type);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecrets(@HostParam("url") String url,
                                               @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedSecretPage.class)
    Mono<PagedResponse<DeletedSecret>> getDeletedSecrets(@HostParam("url") String url,
                                                              @QueryParam("maxresults") Integer maxresults,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @HeaderParam("Content-Type") String type);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedSecretPage.class)
    Mono<PagedResponse<DeletedSecret>> getDeletedSecrets(@HostParam("url") String url,
                                                     @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                     @HeaderParam("Content-Type") String type);
}

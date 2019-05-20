// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.annotations.*;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ServerException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.keyvault.keys.implementation.DeletedKeyPage;
import com.azure.keyvault.keys.implementation.KeyBasePage;
import com.azure.keyvault.keys.models.DeletedKey;
import com.azure.keyvault.keys.models.Key;
import com.azure.keyvault.keys.models.KeyBase;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link KeyAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface KeyService {

    @POST("keys/{key-name}/create")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> createKey(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyRequestParameters parameters,
                                  @HeaderParam("Content-Type") String type);

    @GET("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> getKey(@HostParam("url") String url,
                                     @PathParam("key-name") String keyName,
                                     @PathParam("key-version") String keyVersion,
                                     @QueryParam("api-version") String apiVersion,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type);


    @GET("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<KeyVariant>> getKeyVariant(@HostParam("url") String url,
                               @PathParam("key-name") String keyName,
                               @PathParam("key-version") String keyVersion,
                               @QueryParam("api-version") String apiVersion,
                               @HeaderParam("accept-language") String acceptLanguage,
                               @HeaderParam("Content-Type") String type);

    @POST("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> importKey(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyImportRequestParameters parameters,
                                  @HeaderParam("Content-Type") String type);


    @DELETE("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedKey>> deleteKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);

    @PATCH("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = ServerException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> updateKey(@HostParam("url") String url,
                                      @PathParam("key-name") String keyName,
                                      @PathParam("key-version") String keyVersion,
                                      @QueryParam("api-version") String apiVersion,
                                      @HeaderParam("accept-language") String acceptLanguage,
                                      @BodyParam("body") KeyRequestParameters parameters,
                                      @HeaderParam("Content-Type") String type);

    @GET("keys/{key-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeyVersions(@HostParam("url") String url,
                                                    @PathParam("key-name") String keyName,
                                                    @QueryParam("maxresults") Integer maxresults,
                                                    @QueryParam("api-version") String apiVersion,
                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                    @HeaderParam("Content-Type") String type);

    @POST("keys/{key-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<KeyBackup>> backupKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @POST("keys/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> restoreKey(@HostParam("url") String url,
                                     @QueryParam("api-version") String apiVersion,
                                     @BodyParam("body") KeyRestoreRequestParameters parameters,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type);


    @GET("keys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @QueryParam("maxresults") Integer maxresults,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @GET("deletedkeys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @QueryParam("maxresults") Integer maxresults,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @GET("deletedkeys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedKey>> getDeletedKey(@HostParam("url") String url,
                                             @PathParam("key-name") String keyName,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type);

    @DELETE("deletedkeys/{key-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<VoidResponse> purgeDeletedKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type);


    @POST("deletedkeys/{key-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> recoverDeletedKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type);

    /*@PATCH("secrets/{secret-name}/{secret-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<KeyBase>> updateKey(@HostParam("url") String url,
                                            @PathParam("secret-name") String secretName,
                                            @PathParam("secret-version") String secretVersion,
                                            @QueryParam("api-version") String apiVersion,
                                            @HeaderParam("accept-language") String acceptLanguage,
                                            @BodyParam("body") KeyRequestParameters parameters,
                                            @HeaderParam("Content-Type") String type);


    @DELETE("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedKey>> deleteKey(@HostParam("url") String url,
                                               @PathParam("secret-name") String secretName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedKey>> getDeletedKey(@HostParam("url") String url,
                                                   @PathParam("secret-name") String secretName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @DELETE("deletedsecrets/{secret-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<VoidResponse> purgeDeletedKey(@HostParam("url") String url,
                                          @PathParam("secret-name") String secretName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type);


    @POST("deletedsecrets/{secret-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> recoverDeletedKey(@HostParam("url") String url,
                                                @PathParam("secret-name") String secretName,
                                                @QueryParam("api-version") String apiVersion,
                                                @HeaderParam("accept-language") String acceptLanguage,
                                                @HeaderParam("Content-Type") String type);


    @POST("secrets/{secret-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<KeyBackup>> backupKey(@HostParam("url") String url,
                                              @PathParam("secret-name") String secretName,
                                              @QueryParam("api-version") String apiVersion,
                                              @HeaderParam("accept-language") String acceptLanguage,
                                              @HeaderParam("Content-Type") String type);



    @POST("secrets/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Key>> restoreKey(@HostParam("url") String url,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @BodyParam("application/json") KeyRestoreRequestParameters parameters,
                                         @HeaderParam("Content-Type") String type);


    @GET("secrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                               @QueryParam("maxresults") Integer maxresults,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("secrets/{secret-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeyVersions(@HostParam("url") String url,
                                                      @PathParam("secret-name") String secretName,
                                                      @QueryParam("maxresults") Integer maxresults,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @HeaderParam("Content-Type") String type);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                               @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                         @QueryParam("maxresults") Integer maxresults,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type);*/
}

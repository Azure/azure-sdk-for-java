// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.annotations.POST;
import com.azure.core.annotations.Host;
import com.azure.core.annotations.Service;
import com.azure.core.annotations.ExpectedResponses;
import com.azure.core.annotations.UnexpectedResponseExceptionType;
import com.azure.core.annotations.HostParam;
import com.azure.core.annotations.PathParam;
import com.azure.core.annotations.QueryParam;
import com.azure.core.annotations.HeaderParam;
import com.azure.core.annotations.BodyParam;
import com.azure.core.annotations.GET;
import com.azure.core.annotations.DELETE;
import com.azure.core.annotations.PATCH;
import com.azure.core.annotations.ReturnValueWireType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
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
@Service("KeyVault")
interface KeyService {

    @POST("keys/{key-name}/create")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
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
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> getKey(@HostParam("url") String url,
                                     @PathParam("key-name") String keyName,
                                     @PathParam("key-version") String keyVersion,
                                     @QueryParam("api-version") String apiVersion,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type);

    @POST("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> importKey(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyImportRequestParameters parameters,
                                  @HeaderParam("Content-Type") String type);


    @DELETE("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedKey>> deleteKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);

    @PATCH("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> updateKey(@HostParam("url") String url,
                                      @PathParam("key-name") String keyName,
                                      @PathParam("key-version") String keyVersion,
                                      @QueryParam("api-version") String apiVersion,
                                      @HeaderParam("accept-language") String acceptLanguage,
                                      @BodyParam("body") KeyRequestParameters parameters,
                                      @HeaderParam("Content-Type") String type);

    @GET("keys/{key-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
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
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyBackup>> backupKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @POST("keys/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> restoreKey(@HostParam("url") String url,
                                     @QueryParam("api-version") String apiVersion,
                                     @BodyParam("body") KeyRestoreRequestParameters parameters,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type);


    @GET("keys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @QueryParam("maxresults") Integer maxresults,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type);


    @GET("deletedkeys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @QueryParam("maxresults") Integer maxresults,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @GET("deletedkeys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedKey>> getDeletedKey(@HostParam("url") String url,
                                             @PathParam("key-name") String keyName,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type);

    @DELETE("deletedkeys/{key-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<VoidResponse> purgeDeletedKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type);


    @POST("deletedkeys/{key-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> recoverDeletedKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type);
}

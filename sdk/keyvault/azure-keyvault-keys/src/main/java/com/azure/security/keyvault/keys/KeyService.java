// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.implementation.annotation.BodyParam;
import com.azure.core.implementation.annotation.Delete;
import com.azure.core.implementation.annotation.ExpectedResponses;
import com.azure.core.implementation.annotation.Get;
import com.azure.core.implementation.annotation.HeaderParam;
import com.azure.core.implementation.annotation.Host;
import com.azure.core.implementation.annotation.HostParam;
import com.azure.core.implementation.annotation.Patch;
import com.azure.core.implementation.annotation.Post;
import com.azure.core.implementation.annotation.Put;
import com.azure.core.implementation.annotation.PathParam;
import com.azure.core.implementation.annotation.QueryParam;
import com.azure.core.implementation.annotation.ReturnValueWireType;
import com.azure.core.implementation.annotation.ServiceInterface;
import com.azure.core.implementation.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.implementation.DeletedKeyPage;
import com.azure.security.keyvault.keys.implementation.KeyBasePage;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link KeyAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
@ServiceInterface(name = "KeyVault")
interface KeyService {

    @Post("keys/{key-name}/create")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> createKey(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyRequestParameters parameters,
                                  @HeaderParam("Content-Type") String type,
                                  Context context);

    @Get("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> getKey(@HostParam("url") String url,
                                     @PathParam("key-name") String keyName,
                                     @PathParam("key-version") String keyVersion,
                                     @QueryParam("api-version") String apiVersion,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type,
                                     Context context);

    @Put("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> importKey(@HostParam("url") String url,
                                  @PathParam("key-name") String keyName,
                                  @QueryParam("api-version") String apiVersion,
                                  @HeaderParam("accept-language") String acceptLanguage,
                                  @BodyParam("body") KeyImportRequestParameters parameters,
                                  @HeaderParam("Content-Type") String type,
                                  Context context);


    @Delete("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedKey>> deleteKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);

    @Patch("keys/{key-name}/{key-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> updateKey(@HostParam("url") String url,
                                      @PathParam("key-name") String keyName,
                                      @PathParam("key-version") String keyVersion,
                                      @QueryParam("api-version") String apiVersion,
                                      @HeaderParam("accept-language") String acceptLanguage,
                                      @BodyParam("body") KeyRequestParameters parameters,
                                      @HeaderParam("Content-Type") String type,
                                      Context context);

    @Get("keys/{key-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeyVersions(@HostParam("url") String url,
                                                    @PathParam("key-name") String keyName,
                                                    @QueryParam("maxresults") Integer maxresults,
                                                    @QueryParam("api-version") String apiVersion,
                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                    @HeaderParam("Content-Type") String type,
                                                    Context context);

    @Post("keys/{key-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyBackup>> backupKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);


    @Post("keys/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> restoreKey(@HostParam("url") String url,
                                     @QueryParam("api-version") String apiVersion,
                                     @BodyParam("body") KeyRestoreRequestParameters parameters,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type,
                                     Context context);


    @Get("keys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @QueryParam("maxresults") Integer maxresults,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);


    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyBasePage.class)
    Mono<PagedResponse<KeyBase>> getKeys(@HostParam("url") String url,
                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);


    @Get("deletedkeys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @QueryParam("maxresults") Integer maxresults,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedKeyPage.class)
    Mono<PagedResponse<DeletedKey>> getDeletedKeys(@HostParam("url") String url,
                                                   @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

    @Get("deletedkeys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedKey>> getDeletedKey(@HostParam("url") String url,
                                             @PathParam("key-name") String keyName,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);

    @Delete("deletedkeys/{key-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<VoidResponse> purgeDeletedKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type,
                                       Context context);


    @Post("deletedkeys/{key-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Key>> recoverDeletedKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type,
                                          Context context);
}

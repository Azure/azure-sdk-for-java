// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.implementation.models.DeletedKeyPage;
import com.azure.security.keyvault.keys.implementation.models.KeyPropertiesPage;
import com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.security.keyvault.keys.implementation.models.RandomBytes;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
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
    Mono<Response<KeyVaultKey>> createKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @BodyParam("application/json") KeyRequestParameters parameters,
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

    @Get("keys/{key-name}/{key-version}")
    @ExpectedResponses({200, 404})
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultKey>> getKeyPoller(@HostParam("url") String url,
                                             @PathParam("key-name") String keyName,
                                             @PathParam("key-version") String keyVersion,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);

    @Put("keys/{key-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultKey>> importKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @BodyParam("application/json") KeyImportRequestParameters parameters,
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
    Mono<Response<KeyVaultKey>> updateKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @PathParam("key-version") String keyVersion,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @BodyParam("application/json") KeyRequestParameters parameters,
                                          @HeaderParam("Content-Type") String type,
                                          Context context);

    @Get("keys/{key-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyPropertiesPage.class)
    Mono<PagedResponse<KeyProperties>> getKeyVersions(@HostParam("url") String url,
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
    Mono<Response<KeyVaultKey>> restoreKey(@HostParam("url") String url,
                                           @QueryParam("api-version") String apiVersion,
                                           @BodyParam("application/json") KeyRestoreRequestParameters parameters,
                                           @HeaderParam("accept-language") String acceptLanguage,
                                           @HeaderParam("Content-Type") String type,
                                           Context context);


    @Get("keys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyPropertiesPage.class)
    Mono<PagedResponse<KeyProperties>> getKeys(@HostParam("url") String url,
                                               @QueryParam("maxresults") Integer maxresults,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type,
                                               Context context);


    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(KeyPropertiesPage.class)
    Mono<PagedResponse<KeyProperties>> getKeys(@HostParam("url") String url,
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


    @Get("deletedkeys/{key-name}")
    @ExpectedResponses({200, 404})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedKey>> getDeletedKeyPoller(@HostParam("url") String url,
                                                   @PathParam("key-name") String keyName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);


    @Delete("deletedkeys/{key-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Void>> purgeDeletedKey(@HostParam("url") String url,
                                         @PathParam("key-name") String keyName,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);


    @Post("deletedkeys/{key-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultKey>> recoverDeletedKey(@HostParam("url") String url,
                                                  @PathParam("key-name") String keyName,
                                                  @QueryParam("api-version") String apiVersion,
                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                  @HeaderParam("Content-Type") String type,
                                                  Context context);

    @Post("rng")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<RandomBytes>> getRandomBytes(@HostParam("url") String url,
                                               @QueryParam("api-version") String apiVersion,
                                               @BodyParam("application/json") GetRandomBytesRequest parameters,
                                               @HeaderParam("Accept") String accept,
                                               Context context);

    @Post("keys/{key-name}/{key-version}/release")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ReleaseKeyResult>> release(@HostParam("url") String url,
                                             @PathParam("key-name") String keyName,
                                             @PathParam("key-version") String keyVersion,
                                             @QueryParam("api-version") String apiVersion,
                                             @BodyParam("application/json") KeyReleaseParameters parameters,
                                             @HeaderParam("Accept") String accept,
                                             Context context);

    @Post("/keys/{key-name}/rotate")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultKey>> rotateKey(@HostParam("url") String url,
                                          @PathParam("key-name") String keyName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("Accept") String accept,
                                          Context context);

    @Get("/keys/{key-name}/rotationpolicy")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyRotationPolicy>> getKeyRotationPolicy(@HostParam("url") String url,
                                                           @PathParam("key-name") String keyName,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("Accept") String accept,
                                                           Context context);

    @Put("/keys/{key-name}/rotationpolicy")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicy(@HostParam("url") String url,
                                                              @PathParam("key-name") String keyName,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @BodyParam("application/json") KeyRotationPolicy keyRotationPolicy,
                                                              @HeaderParam("Accept") String accept,
                                                              Context context);
}

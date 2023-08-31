// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.implementation.models.DeletedKeyPage;
import com.azure.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.security.keyvault.keys.implementation.models.KeyPropertiesPage;
import com.azure.security.keyvault.keys.implementation.models.RandomBytes;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

public class KeyClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(KeyClientImpl.class);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final KeyService service;
    private final HttpPipeline pipeline;
    private final KeyServiceVersion keyServiceVersion;

    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    /**
     * Creates a {@link KeyClientImpl} that uses an {@link HttpPipeline} to service requests.
     *
     * @param vaultUrl URL for the Azure Key Vault service.
     * @param pipeline {@link HttpPipeline} that the HTTP requests and responses will flow through.
     * @param keyServiceVersion {@link KeyServiceVersion} of the service to be used when making requests.
     */
    public KeyClientImpl(String vaultUrl, HttpPipeline pipeline, KeyServiceVersion keyServiceVersion) {
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl;
        this.service = RestProxy.create(KeyService.class, pipeline);
        this.pipeline = pipeline;
        this.keyServiceVersion = keyServiceVersion;
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     *
     * @return The vault endpoint url
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The {@link HttpPipeline pipeline}.
     */
    public HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the default polling interval for long running operations.
     *
     * @return The default polling interval for long running operations
     */
    public Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * Gets the Key Service version.
     *
     * @return the service version.
     */
    public KeyServiceVersion getKeyServiceVersion() {
        return keyServiceVersion;
    }


    /**
     * The interface defining all the services for {@link KeyClientImpl} to be used
     * by the proxy service to perform REST calls.
     *
     * This is package-private so that these REST calls are transparent to the user.
     */
    @Host("{url}")
    @ServiceInterface(name = "KeyVault")
    public interface KeyService {
        @Post("keys/{key-name}/create")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultKey>> createKeyAsync(@HostParam("url") String url,
                                                   @PathParam("key-name") String keyName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @BodyParam("application/json") KeyRequestParameters parameters,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

        @Post("keys/{key-name}/create")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> createKey(@HostParam("url") String url,
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
        Mono<Response<KeyVaultKey>> getKeyAsync(@HostParam("url") String url,
                                                @PathParam("key-name") String keyName,
                                                @PathParam("key-version") String keyVersion,
                                                @QueryParam("api-version") String apiVersion,
                                                @HeaderParam("accept-language") String acceptLanguage,
                                                @HeaderParam("Content-Type") String type,
                                                Context context);

        @Get("keys/{key-name}/{key-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> getKey(@HostParam("url") String url,
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
        Mono<Response<KeyVaultKey>> getKeyPollerAsync(@HostParam("url") String url,
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
        Response<KeyVaultKey> getKeyPoller(@HostParam("url") String url,
                                           @PathParam("key-name") String keyName,
                                           @PathParam("key-version") String keyVersion,
                                           @QueryParam("api-version") String apiVersion,
                                           @HeaderParam("accept-language") String acceptLanguage,
                                           @HeaderParam("Content-Type") String type,
                                           Context context);

        @Put("keys/{key-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultKey>> importKeyAsync(@HostParam("url") String url,
                                                   @PathParam("key-name") String keyName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @BodyParam("application/json") KeyImportRequestParameters parameters,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

        @Put("keys/{key-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> importKey(@HostParam("url") String url,
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
        Mono<Response<DeletedKey>> deleteKeyAsync(@HostParam("url") String url,
                                                  @PathParam("key-name") String keyName,
                                                  @QueryParam("api-version") String apiVersion,
                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                  @HeaderParam("Content-Type") String type,
                                                  Context context);

        @Delete("keys/{key-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedKey> deleteKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type,
                                       Context context);

        @Patch("keys/{key-name}/{key-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultKey>> updateKeyAsync(@HostParam("url") String url,
                                                   @PathParam("key-name") String keyName,
                                                   @PathParam("key-version") String keyVersion,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @BodyParam("application/json") KeyRequestParameters parameters,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

        @Patch("keys/{key-name}/{key-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> updateKey(@HostParam("url") String url,
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
        Mono<PagedResponse<KeyProperties>> getKeyVersionsAsync(@HostParam("url") String url,
                                                               @PathParam("key-name") String keyName,
                                                               @QueryParam("maxresults") Integer maxresults,
                                                               @QueryParam("api-version") String apiVersion,
                                                               @HeaderParam("accept-language") String acceptLanguage,
                                                               @HeaderParam("Content-Type") String type,
                                                               Context context);

        @Get("keys/{key-name}/versions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(KeyPropertiesPage.class)
        PagedResponse<KeyProperties> getKeyVersions(@HostParam("url") String url,
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
        Mono<Response<KeyBackup>> backupKeyAsync(@HostParam("url") String url,
                                                 @PathParam("key-name") String keyName,
                                                 @QueryParam("api-version") String apiVersion,
                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                 @HeaderParam("Content-Type") String type,
                                                 Context context);

        @Post("keys/{key-name}/backup")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyBackup> backupKey(@HostParam("url") String url,
                                      @PathParam("key-name") String keyName,
                                      @QueryParam("api-version") String apiVersion,
                                      @HeaderParam("accept-language") String acceptLanguage,
                                      @HeaderParam("Content-Type") String type,
                                      Context context);

        @Post("keys/restore")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultKey>> restoreKeyAsync(@HostParam("url") String url,
                                                    @QueryParam("api-version") String apiVersion,
                                                    @BodyParam("application/json") KeyRestoreRequestParameters parameters,
                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                    @HeaderParam("Content-Type") String type,
                                                    Context context);

        @Post("keys/restore")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> restoreKey(@HostParam("url") String url,
                                         @QueryParam("api-version") String apiVersion,
                                         @BodyParam("application/json") KeyRestoreRequestParameters parameters,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);

        @Get("keys")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(KeyPropertiesPage.class)
        Mono<PagedResponse<KeyProperties>> getKeysAsync(@HostParam("url") String url,
                                                        @QueryParam("maxresults") Integer maxresults,
                                                        @QueryParam("api-version") String apiVersion,
                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                        @HeaderParam("Content-Type") String type,
                                                        Context context);

        @Get("keys")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(KeyPropertiesPage.class)
        PagedResponse<KeyProperties> getKeys(@HostParam("url") String url,
                                             @QueryParam("maxresults") Integer maxresults,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(KeyPropertiesPage.class)
        Mono<PagedResponse<KeyProperties>> getKeysAsync(@HostParam("url") String url,
                                                        @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                        @HeaderParam("Content-Type") String type,
                                                        Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(KeyPropertiesPage.class)
        PagedResponse<KeyProperties> getKeys(@HostParam("url") String url,
                                             @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);

        @Get("deletedkeys")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedKeyPage.class)
        Mono<PagedResponse<DeletedKey>> getDeletedKeysAsync(@HostParam("url") String url,
                                                            @QueryParam("maxresults") Integer maxresults,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);

        @Get("deletedkeys")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedKeyPage.class)
        PagedResponse<DeletedKey> getDeletedKeys(@HostParam("url") String url,
                                                 @QueryParam("maxresults") Integer maxresults,
                                                 @QueryParam("api-version") String apiVersion,
                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                 @HeaderParam("Content-Type") String type,
                                                 Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedKeyPage.class)
        Mono<PagedResponse<DeletedKey>> getDeletedKeysAsync(@HostParam("url") String url,
                                                            @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedKeyPage.class)
        PagedResponse<DeletedKey> getDeletedKeys(@HostParam("url") String url,
                                                 @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                 @HeaderParam("Content-Type") String type,
                                                 Context context);

        @Get("deletedkeys/{key-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeletedKey>> getDeletedKeyAsync(@HostParam("url") String url,
                                                      @PathParam("key-name") String keyName,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @HeaderParam("Content-Type") String type,
                                                      Context context);

        @Get("deletedkeys/{key-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedKey> getDeletedKey(@HostParam("url") String url,
                                           @PathParam("key-name") String keyName,
                                           @QueryParam("api-version") String apiVersion,
                                           @HeaderParam("accept-language") String acceptLanguage,
                                           @HeaderParam("Content-Type") String type,
                                           Context context);

        @Get("deletedkeys/{key-name}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeletedKey>> getDeletedKeyPollerAsync(@HostParam("url") String url,
                                                            @PathParam("key-name") String keyName,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);

        @Get("deletedkeys/{key-name}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedKey> getDeletedKeyPoller(@HostParam("url") String url,
                                                 @PathParam("key-name") String keyName,
                                                 @QueryParam("api-version") String apiVersion,
                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                 @HeaderParam("Content-Type") String type,
                                                 Context context);

        @Delete("deletedkeys/{key-name}")
        @ExpectedResponses({204})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> purgeDeletedKeyAsync(@HostParam("url") String url,
                                                  @PathParam("key-name") String keyName,
                                                  @QueryParam("api-version") String apiVersion,
                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                  @HeaderParam("Content-Type") String type,
                                                  Context context);

        @Delete("deletedkeys/{key-name}")
        @ExpectedResponses({204})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<Void> purgeDeletedKey(@HostParam("url") String url,
                                       @PathParam("key-name") String keyName,
                                       @QueryParam("api-version") String apiVersion,
                                       @HeaderParam("accept-language") String acceptLanguage,
                                       @HeaderParam("Content-Type") String type,
                                       Context context);

        @Post("deletedkeys/{key-name}/recover")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultKey>> recoverDeletedKeyAsync(@HostParam("url") String url,
                                                           @PathParam("key-name") String keyName,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

        @Post("deletedkeys/{key-name}/recover")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> recoverDeletedKey(@HostParam("url") String url,
                                                @PathParam("key-name") String keyName,
                                                @QueryParam("api-version") String apiVersion,
                                                @HeaderParam("accept-language") String acceptLanguage,
                                                @HeaderParam("Content-Type") String type,
                                                Context context);

        @Post("rng")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<RandomBytes>> getRandomBytesAsync(@HostParam("url") String url,
                                                        @QueryParam("api-version") String apiVersion,
                                                        @BodyParam("application/json") GetRandomBytesRequest parameters,
                                                        @HeaderParam("Accept") String accept,
                                                        Context context);

        @Post("rng")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<RandomBytes> getRandomBytes(@HostParam("url") String url,
                                             @QueryParam("api-version") String apiVersion,
                                             @BodyParam("application/json") GetRandomBytesRequest parameters,
                                             @HeaderParam("Accept") String accept,
                                             Context context);

        @Post("keys/{key-name}/{key-version}/release")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<ReleaseKeyResult>> releaseAsync(@HostParam("url") String url,
                                                      @PathParam("key-name") String keyName,
                                                      @PathParam("key-version") String keyVersion,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @BodyParam("application/json") KeyReleaseParameters parameters,
                                                      @HeaderParam("Accept") String accept,
                                                      Context context);

        @Post("keys/{key-name}/{key-version}/release")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<ReleaseKeyResult> release(@HostParam("url") String url,
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
        Mono<Response<KeyVaultKey>> rotateKeyAsync(@HostParam("url") String url,
                                                   @PathParam("key-name") String keyName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("Accept") String accept,
                                                   Context context);

        @Post("/keys/{key-name}/rotate")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultKey> rotateKey(@HostParam("url") String url,
                                        @PathParam("key-name") String keyName,
                                        @QueryParam("api-version") String apiVersion,
                                        @HeaderParam("Accept") String accept,
                                        Context context);

        @Get("/keys/{key-name}/rotationpolicy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyRotationPolicy>> getKeyRotationPolicyAsync(@HostParam("url") String url,
                                                                    @PathParam("key-name") String keyName,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("Accept") String accept,
                                                                    Context context);

        @Get("/keys/{key-name}/rotationpolicy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyRotationPolicy> getKeyRotationPolicy(@HostParam("url") String url,
                                                         @PathParam("key-name") String keyName,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("Accept") String accept,
                                                         Context context);

        @Put("/keys/{key-name}/rotationpolicy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicyAsync(@HostParam("url") String url,
                                                                       @PathParam("key-name") String keyName,
                                                                       @QueryParam("api-version") String apiVersion,
                                                                       @BodyParam("application/json") KeyRotationPolicy keyRotationPolicy,
                                                                       @HeaderParam("Accept") String accept,
                                                                       Context context);

        @Put("/keys/{key-name}/rotationpolicy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyRotationPolicy> updateKeyRotationPolicy(@HostParam("url") String url,
                                                            @PathParam("key-name") String keyName,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @BodyParam("application/json") KeyRotationPolicy keyRotationPolicy,
                                                            @HeaderParam("Accept") String accept,
                                                            Context context);
    }

    public Mono<Response<KeyVaultKey>> createKeyWithResponseAsync(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);

        return service.createKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to create key - {}", name, error));
    }

    public Response<KeyVaultKey> createKeyWithResponse(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<KeyVaultKey>> createKeyWithResponseAsync(CreateKeyOptions createKeyOptions, Context context) {
        KeyRequestParameters parameters = validateAndCreateKeyRequestParameters(createKeyOptions);

        return service.createKeyAsync(vaultUrl, createKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating key - {}", createKeyOptions.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to create key - {}", createKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        KeyRequestParameters parameters = validateAndCreateKeyRequestParameters(createKeyOptions);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private KeyRequestParameters validateAndCreateKeyRequestParameters(CreateKeyOptions createKeyOptions) {
        Objects.requireNonNull(createKeyOptions, "The key create options parameter cannot be null.");

        return new KeyRequestParameters()
            .setKty(createKeyOptions.getKeyType())
            .setKeyOps(createKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createKeyOptions))
            .setTags(createKeyOptions.getTags())
            .setReleasePolicy(createKeyOptions.getReleasePolicy());
    }


    public Mono<Response<KeyVaultKey>> createRsaKeyWithResponseAsync(CreateRsaKeyOptions createRsaKeyOptions,
                                                                     Context context) {
        KeyRequestParameters parameters = validateAndCreateRsaKeyRequestParameters(createRsaKeyOptions);

        return service.createKeyAsync(vaultUrl, createRsaKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating RSA key - {}", createRsaKeyOptions.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Created RSA key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to create RSA key - {}", createRsaKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions, Context context) {
        KeyRequestParameters parameters = validateAndCreateRsaKeyRequestParameters(createRsaKeyOptions);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createRsaKeyOptions.getName(), keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private KeyRequestParameters validateAndCreateRsaKeyRequestParameters(CreateRsaKeyOptions createRsaKeyOptions) {
        Objects.requireNonNull(createRsaKeyOptions, "The RSA key options parameter cannot be null.");

        return new KeyRequestParameters()
            .setKty(createRsaKeyOptions.getKeyType())
            .setKeySize(createRsaKeyOptions.getKeySize())
            .setKeyOps(createRsaKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createRsaKeyOptions))
            .setPublicExponent(createRsaKeyOptions.getPublicExponent())
            .setTags(createRsaKeyOptions.getTags())
            .setReleasePolicy(createRsaKeyOptions.getReleasePolicy());
    }

    public Mono<Response<KeyVaultKey>> createEcKeyWithResponseAsync(CreateEcKeyOptions createEcKeyOptions,
                                                                    Context context) {
        KeyRequestParameters parameters = validateAndCreateEcKeyRequestParameters(createEcKeyOptions);

        return service.createKeyAsync(vaultUrl, createEcKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating EC key - {}", createEcKeyOptions.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Created EC key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to create EC key - {}", createEcKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        KeyRequestParameters parameters = validateAndCreateEcKeyRequestParameters(createEcKeyOptions);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createEcKeyOptions.getName(), keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private KeyRequestParameters validateAndCreateEcKeyRequestParameters(CreateEcKeyOptions createEcKeyOptions) {
        Objects.requireNonNull(createEcKeyOptions, "The EC key options cannot be null.");

        return new KeyRequestParameters()
            .setKty(createEcKeyOptions.getKeyType())
            .setCurve(createEcKeyOptions.getCurveName())
            .setKeyOps(createEcKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createEcKeyOptions))
            .setTags(createEcKeyOptions.getTags())
            .setReleasePolicy(createEcKeyOptions.getReleasePolicy());
    }

    public Mono<Response<KeyVaultKey>> createOctKeyWithResponseAsync(CreateOctKeyOptions createOctKeyOptions,
                                                                     Context context) {
        KeyRequestParameters parameters = validateAndCreateOctKeyRequestParameters(createOctKeyOptions);

        return service.createKeyAsync(vaultUrl, createOctKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating symmetric key - {}", createOctKeyOptions.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Created symmetric key - {}", response.getValue().getName()))
            .doOnError(error ->
                LOGGER.warning("Failed to create symmetric key - {}", createOctKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions, Context context) {
        KeyRequestParameters parameters = validateAndCreateOctKeyRequestParameters(createOctKeyOptions);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createOctKeyOptions.getName(), keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private KeyRequestParameters validateAndCreateOctKeyRequestParameters(CreateOctKeyOptions createOctKeyOptions) {
        Objects.requireNonNull(createOctKeyOptions, "The create key options cannot be null.");

        return new KeyRequestParameters()
            .setKty(createOctKeyOptions.getKeyType())
            .setKeySize(createOctKeyOptions.getKeySize())
            .setKeyOps(createOctKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createOctKeyOptions))
            .setTags(createOctKeyOptions.getTags())
            .setReleasePolicy(createOctKeyOptions.getReleasePolicy());
    }

    public Mono<Response<KeyVaultKey>> importKeyWithResponseAsync(String name, JsonWebKey keyMaterial,
                                                                  Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);

        return service.importKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Importing key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to import key - {}", name, error));
    }

    public Response<KeyVaultKey> importKeyWithResponse(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.importKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<KeyVaultKey>> importKeyWithResponseAsync(ImportKeyOptions importKeyOptions, Context context) {
        KeyImportRequestParameters parameters = validateAndCreateKeyImportRequestParameters(importKeyOptions);

        return service.importKeyAsync(vaultUrl, importKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Importing key - {}", importKeyOptions.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to import key - {}", importKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        KeyImportRequestParameters parameters = validateAndCreateKeyImportRequestParameters(importKeyOptions);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.importKey(vaultUrl, importKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private KeyImportRequestParameters validateAndCreateKeyImportRequestParameters(ImportKeyOptions importKeyOptions) {
        Objects.requireNonNull(importKeyOptions, "The key import configuration parameter cannot be null.");

        return new KeyImportRequestParameters()
            .setKey(importKeyOptions.getKey())
            .setHsm(importKeyOptions.isHardwareProtected())
            .setKeyAttributes(new KeyRequestAttributes(importKeyOptions))
            .setTags(importKeyOptions.getTags())
            .setReleasePolicy(importKeyOptions.getReleasePolicy());
    }


    public Mono<Response<KeyVaultKey>> getKeyWithResponseAsync(String name, String version, Context context) {
        return service.getKeyAsync(vaultUrl, name, version, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to retrieve key - {}", name, error));
    }

    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKey(vaultUrl, name, version, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponseAsync(KeyProperties keyProperties,
                                                                            Context context,
                                                                            KeyOperation... keyOperations) {
        KeyRequestParameters parameters = validateAndCreateUpdateKeyRequestParameters(keyProperties, keyOperations);

        return service.updateKeyAsync(vaultUrl, keyProperties.getName(), keyProperties.getVersion(),
                keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context)
            .doOnRequest(ignored -> LOGGER.verbose("Updating key - {}", keyProperties.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to update key - {}", keyProperties.getName(), error));
    }

    public Response<KeyVaultKey> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context,
                                                                 KeyOperation... keyOperations) {
        KeyRequestParameters parameters = validateAndCreateUpdateKeyRequestParameters(keyProperties, keyOperations);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateKey(vaultUrl, keyProperties.getName(), keyProperties.getVersion(),
            keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    private KeyRequestParameters validateAndCreateUpdateKeyRequestParameters(KeyProperties keyProperties,
                                                                             KeyOperation... keyOperations) {
        Objects.requireNonNull(keyProperties, "The key properties input parameter cannot be null.");

        KeyRequestParameters parameters = new KeyRequestParameters()
            .setTags(keyProperties.getTags())
            .setKeyAttributes(new KeyRequestAttributes(keyProperties))
            .setReleasePolicy(keyProperties.getReleasePolicy());

        if (keyOperations.length > 0) {
            parameters.setKeyOps(Arrays.asList(keyOperations));
        }

        return parameters;
    }

    public PollerFlux<DeletedKey, Void> beginDeleteKeyAsync(String name) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperationAsync(name),
            createPollOperationAsync(name),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    public SyncPoller<DeletedKey, Void> beginDeleteKey(String name, Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation(name, context).apply(cxt)),
            createPollOperation(name, context),
            (pollingContext, firstResponse) -> null,
            (pollingContext) -> null);
    }

    private Function<PollingContext<DeletedKey>, Mono<DeletedKey>> activationOperationAsync(String name) {
        return (pollingContext) -> withContext(context -> deleteKeyWithResponseAsync(name, context))
            .flatMap(deletedKeyResponse -> Mono.just(deletedKeyResponse.getValue()));
    }

    private Function<PollingContext<DeletedKey>, DeletedKey> activationOperation(String name, Context context) {
        return (pollingContext) -> deleteKeyWithResponse(name, context).getValue();
    }

    /*
     * Async polling operation to poll on create delete key operation status.
     */
    private Function<PollingContext<DeletedKey>, Mono<PollResponse<DeletedKey>>> createPollOperationAsync(String keyName) {
        return (pollingContext) ->
            withContext(context -> service.getDeletedKeyPollerAsync(vaultUrl, keyName, keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context))
                .flatMap(deletedKeyResponse -> {
                    if (deletedKeyResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }

                    return Mono.defer(() ->
                        Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            deletedKeyResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key
                // operation. In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    /**
     * Sync polling operation to poll on the delete key operation status.
     */
    private Function<PollingContext<DeletedKey>, PollResponse<DeletedKey>> createPollOperation(String keyName,
                                                                                               Context context) {
        return (pollingContext) -> {
            try {
                Context contextToUse = context;
                contextToUse = enableSyncRestProxy(contextToUse);
                Response<DeletedKey> deletedKeyResponse = service.getDeletedKeyPoller(vaultUrl, keyName,
                    keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, contextToUse);

                if (deletedKeyResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                }

                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    deletedKeyResponse.getValue());
            } catch (HttpResponseException e) {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key
                // operation. In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }

    private Mono<Response<DeletedKey>> deleteKeyWithResponseAsync(String name, Context context) {
        return service.deleteKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Deleting key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Deleted key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to delete key - {}", name, error));
    }

    private Response<DeletedKey> deleteKeyWithResponse(String name, Context context) {
        context = enableSyncRestProxy(context);

        return service.deleteKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<DeletedKey>> getDeletedKeyWithResponseAsync(String name, Context context) {
        return service.getDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving deleted key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved deleted key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to retrieve deleted key - {}", name, error));
    }

    public Response<DeletedKey> getDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getDeletedKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<Void>> purgeDeletedKeyWithResponseAsync(String name, Context context) {
        return service.purgeDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Purging deleted key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Purged deleted key - {}", name))
            .doOnError(error -> LOGGER.warning("Failed to purge deleted key - {}", name, error));
    }

    public Response<Void> purgeDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.purgeDeletedKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PollerFlux<KeyVaultKey, Void> beginRecoverDeletedKeyAsync(String name) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            recoverActivationOperationAsync(name),
            createRecoverPollOperationAsync(name),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    public SyncPoller<KeyVaultKey, Void> beginRecoverDeletedKey(String name, Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                recoverActivationOperation(name, context).apply(cxt)),
            createRecoverPollOperation(name, context),
            (pollingContext, firstResponse) -> null,
            (pollingContext) -> null);
    }

    private Function<PollingContext<KeyVaultKey>, Mono<KeyVaultKey>> recoverActivationOperationAsync(String name) {
        return (pollingContext) -> withContext(context -> recoverDeletedKeyWithResponseAsync(name, context))
            .flatMap(keyResponse -> Mono.just(keyResponse.getValue()));
    }

    private Function<PollingContext<KeyVaultKey>, KeyVaultKey> recoverActivationOperation(String name, Context context) {
        return (pollingContext) -> recoverDeletedKeyWithResponse(name, context).getValue();
    }

    /*
     * Async polling operation to poll on create delete key operation status.
     */
    private Function<PollingContext<KeyVaultKey>, Mono<PollResponse<KeyVaultKey>>> createRecoverPollOperationAsync(String keyName) {
        return (pollingContext) ->
            withContext(context ->
                service.getKeyPollerAsync(vaultUrl, keyName, "", keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context))
                .flatMap(keyResponse -> {
                    if (keyResponse.getStatusCode() == 404) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }

                    return Mono.defer(() -> Mono.just(
                        new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, keyResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation. In both cases deletion
                // operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    /**
     * Sync polling operation to poll on the delete key operation status.
     */
    private Function<PollingContext<KeyVaultKey>, PollResponse<KeyVaultKey>> createRecoverPollOperation(String keyName, Context context) {
        return (pollingContext) -> {
            try {
                Context contextToUse = context;
                contextToUse = enableSyncRestProxy(contextToUse);
                Response<KeyVaultKey> keyResponse =
                    service.getKeyPoller(vaultUrl, keyName, "", keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, contextToUse);

                if (keyResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                }

                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    keyResponse.getValue());
            } catch (HttpResponseException e) {
                // This means permission is not granted for the get deleted key operation. In both cases the deletion
                // operation was successful when activation operation succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }

    private Mono<Response<KeyVaultKey>> recoverDeletedKeyWithResponseAsync(String name, Context context) {
        return service.recoverDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Recovering deleted key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Recovered deleted key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to recover deleted key - {}", name, error));
    }

    private Response<KeyVaultKey> recoverDeletedKeyWithResponse(String name, Context context) {
        context = enableSyncRestProxy(context);

        return service.recoverDeletedKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<byte[]>> backupKeyWithResponseAsync(String name, Context context) {
        return service.backupKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Backing up key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Backed up key - {}", name))
            .doOnError(error -> LOGGER.warning("Failed to back up key - {}", name, error))
            .flatMap(base64URLResponse -> Mono.just(new SimpleResponse<>(base64URLResponse.getRequest(),
                base64URLResponse.getStatusCode(), base64URLResponse.getHeaders(),
                base64URLResponse.getValue().getValue())));
    }

    public Response<byte[]> backupKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        Response<KeyBackup> backupResponse = service.backupKey(vaultUrl, name, keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);

        return new SimpleResponse<>(backupResponse.getRequest(), backupResponse.getStatusCode(),
            backupResponse.getHeaders(), backupResponse.getValue().getValue());
    }

    public Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponseAsync(byte[] backup, Context context) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);

        return service.restoreKeyAsync(vaultUrl, keyServiceVersion.getVersion(), parameters, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Restoring key"))
            .doOnSuccess(response -> LOGGER.verbose("Restored key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to restore key", error));
    }

    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.restoreKey(vaultUrl, keyServiceVersion.getVersion(), parameters, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeysAsync() {
        try {
            return new PagedFlux<>(() -> withContext(this::listKeysFirstPageAsync),
                continuationToken -> withContext(context -> listKeysNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Gets attributes of the first 25 keys that can be found on a given key vault.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysFirstPageAsync(Context context) {
        try {
            return service.getKeysAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing keys"))
                .doOnSuccess(response -> LOGGER.verbose("Listed keys"))
                .doOnError(error -> LOGGER.warning("Failed to list keys", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets attributes of all the keys given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listPropertiesOfKeysAsync()}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysNextPageAsync(String continuationToken, Context context) {
        try {
            return service.getKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing next keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> LOGGER.verbose("Listed next keys page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<KeyProperties> listPropertiesOfKeys() {
        return new PagedIterable<>(
            () -> listKeysFirstPage(Context.NONE),
            continuationToken -> listKeysNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<KeyProperties> listPropertiesOfKeys(Context context) {
        return new PagedIterable<>(
            () -> listKeysFirstPage(context),
            continuationToken -> listKeysNextPage(continuationToken, context));
    }

    /**
     * Gets attributes of the first 25 keys that can be found on a given key vault.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link PagedResponse} containing {@link KeyProperties} instances from the next page of results.
     */
    private PagedResponse<KeyProperties> listKeysFirstPage(Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of all the keys given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listPropertiesOfKeys(Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link PagedResponse} containing {@link KeyProperties} instances from the next page of results.
     */
    private PagedResponse<KeyProperties> listKeysNextPage(String continuationToken, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<DeletedKey> listDeletedKeysAsync() {
        try {
            return new PagedFlux<>(() -> withContext(this::listDeletedKeysFirstPageAsync),
                continuationToken -> withContext(context -> listDeletedKeysNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Gets attributes of the first 25 deleted keys that can be found on a given key vault.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysFirstPageAsync(Context context) {
        try {
            return service.getDeletedKeysAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing deleted keys"))
                .doOnSuccess(response -> LOGGER.verbose("Listed deleted keys"))
                .doOnError(error -> LOGGER.warning("Failed to list deleted keys", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets attributes of all the keys given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listDeletedKeysAsync()}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to
     * one of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link DeletedKey} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysNextPageAsync(String continuationToken, Context context) {
        try {
            return service.getDeletedKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing next deleted keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> LOGGER.verbose("Listed next deleted keys page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next deleted keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<DeletedKey> listDeletedKeys() {
        return new PagedIterable<>(
            () -> listDeletedKeysFirstPage(Context.NONE),
            continuationToken -> listDeletedKeysNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<DeletedKey> listDeletedKeys(Context context) {
        return new PagedIterable<>(
            () -> listDeletedKeysFirstPage(context),
            continuationToken -> listDeletedKeysNextPage(continuationToken, context));
    }

    /**
     * Gets attributes of the first 25 deleted keys that can be found on a given key vault.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link PagedResponse} containing {@link KeyProperties} instances from the next page of results.
     */
    private PagedResponse<DeletedKey> listDeletedKeysFirstPage(Context context) {
        return service.getDeletedKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of all the keys given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listDeletedKeys()}.
     *
     * @param continuationToken The {@link Page#getContinuationToken()} from a previous, successful call to one of the
     * list operations.
     *
     * @return A {@link PagedResponse} that contains {@link DeletedKey} from the next page of results.
     */
    private PagedResponse<DeletedKey> listDeletedKeysNextPage(String continuationToken, Context context) {
        return service.getDeletedKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeyVersionsAsync(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listKeyVersionsFirstPageAsync(name, context)),
                continuationToken -> withContext(context -> listKeyVersionsNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Gets attributes of the first 25 versions of a key.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeyVersionsFirstPageAsync(String name, Context context) {
        try {
            return service.getKeyVersionsAsync(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing key versions - {}", name))
                .doOnSuccess(response -> LOGGER.verbose("Listed key versions - {}", name))
                .doOnError(error -> LOGGER.warning("Failed to list key versions - {}", name, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets attributes of versions of a key given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listPropertiesOfKeyVersionsAsync(String)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeyVersionsNextPageAsync(String continuationToken, Context context) {
        try {
            return service.getKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing next key versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> LOGGER.verbose("Listed next key versions page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next key versions page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name) {
        return new PagedIterable<>(
            () -> listKeyVersionsFirstPage(name, Context.NONE),
            continuationToken -> listKeyVersionsNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedIterable<>(
            () -> listKeyVersionsFirstPage(name, context),
            continuationToken -> listKeyVersionsNextPage(continuationToken, context));
    }

    /**
     * Gets attributes of the first 25 versions of a key.
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link PagedResponse} containing {@link KeyProperties} instances from the next page of results.
     */
    private PagedResponse<KeyProperties> listKeyVersionsFirstPage(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKeyVersions(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of versions of a key given by the {@code continuationToken} that was retrieved from a call to
     * {@link KeyClientImpl#listPropertiesOfKeyVersions(String)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link PagedResponse} containing {@link KeyProperties} instances from the next page of results.
     */
    private PagedResponse<KeyProperties> listKeyVersionsNextPage(String continuationToken, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<byte[]>> getRandomBytesWithResponseAsync(int count, Context context) {
        try {
            return service.getRandomBytesAsync(vaultUrl, keyServiceVersion.getVersion(),
                    new GetRandomBytesRequest().setCount(count), CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Getting {} random bytes.", count))
                .doOnSuccess(response -> LOGGER.verbose("Got {} random bytes.", count))
                .doOnError(error -> LOGGER.warning("Failed to get random bytes - {}", error))
                .map(response -> new SimpleResponse<>(response, response.getValue().getBytes()));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    public Response<byte[]> getRandomBytesWithResponse(int count, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        Response<RandomBytes> randomBytesResponse = service.getRandomBytes(vaultUrl, keyServiceVersion.getVersion(),
            new GetRandomBytesRequest().setCount(count), CONTENT_TYPE_HEADER_VALUE,
            context);

        return new SimpleResponse<>(randomBytesResponse, randomBytesResponse.getValue().getBytes());
    }

    public Mono<Response<ReleaseKeyResult>> releaseKeyWithResponseAsync(String name, String version,
                                                                        String targetAttestationToken,
                                                                        ReleaseKeyOptions releaseKeyOptions,
                                                                        Context context) {
        try {
            KeyReleaseParameters keyReleaseParameters = validateAndCreateKeyReleaseParameters(name,
                targetAttestationToken, releaseKeyOptions);

            return service.releaseAsync(vaultUrl, name, version, keyServiceVersion.getVersion(), keyReleaseParameters,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Releasing key with name %s and version %s.", name, version))
                .doOnSuccess(response -> LOGGER.verbose("Released key with name %s and version %s.", name, version))
                .doOnError(error -> LOGGER.warning("Failed to release key - {}", error));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version,
                                                             String targetAttestationToken,
                                                             ReleaseKeyOptions releaseKeyOptions, Context context) {
        try {
            KeyReleaseParameters keyReleaseParameters = validateAndCreateKeyReleaseParameters(name,
                targetAttestationToken, releaseKeyOptions);
            context = context == null ? Context.NONE : context;
            context = enableSyncRestProxy(context);

            return service.release(vaultUrl, name, version, keyServiceVersion.getVersion(), keyReleaseParameters,
                CONTENT_TYPE_HEADER_VALUE, context);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private KeyReleaseParameters validateAndCreateKeyReleaseParameters(String name, String targetAttestationToken,
                                                                       ReleaseKeyOptions releaseKeyOptions) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'name' cannot be null or empty"));
        }

        if (CoreUtils.isNullOrEmpty(targetAttestationToken)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'targetAttestationToken' cannot be null or empty"));
        }

        releaseKeyOptions = releaseKeyOptions == null ? new ReleaseKeyOptions() : releaseKeyOptions;

        return new KeyReleaseParameters()
            .setTargetAttestationToken(targetAttestationToken)
            .setAlgorithm(releaseKeyOptions.getAlgorithm())
            .setNonce(releaseKeyOptions.getNonce());
    }

    public Mono<Response<KeyVaultKey>> rotateKeyWithResponseAsync(String name, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                return monoError(LOGGER, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            return service.rotateKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Rotating key with name %s.", name))
                .doOnSuccess(response -> LOGGER.verbose("Rotated key with name %s.", name))
                .doOnError(error -> LOGGER.warning("Failed to rotate key - {}", error));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    public Response<KeyVaultKey> rotateKeyWithResponse(String name, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'name' cannot be null or empty"));
            }

            context = context == null ? Context.NONE : context;
            context = enableSyncRestProxy(context);

            return service.rotateKey(vaultUrl, name, keyServiceVersion.getVersion(), CONTENT_TYPE_HEADER_VALUE,
                context);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    public Mono<Response<KeyRotationPolicy>> getKeyRotationPolicyWithResponseAsync(String keyName, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                return monoError(LOGGER, new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            return service.getKeyRotationPolicyAsync(vaultUrl, keyName, keyServiceVersion.getVersion(),
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Retrieving key rotation policy for key with name.", keyName))
                .doOnSuccess(response -> LOGGER.verbose("Retrieved key rotation policy for key with name.", keyName))
                .doOnError(error -> LOGGER.warning("Failed to retrieve key rotation policy - {}", error));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    public Response<KeyRotationPolicy> getKeyRotationPolicyWithResponse(String keyName, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            context = context == null ? Context.NONE : context;
            context = enableSyncRestProxy(context);

            return service.getKeyRotationPolicy(vaultUrl, keyName, keyServiceVersion.getVersion(),
                CONTENT_TYPE_HEADER_VALUE, context);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }


    public Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicyWithResponseAsync(String keyName,
                                                                                      KeyRotationPolicy keyRotationPolicy,
                                                                                      Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                return monoError(LOGGER, new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            return service.updateKeyRotationPolicyAsync(vaultUrl, keyName, keyServiceVersion.getVersion(),
                    keyRotationPolicy, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Updating key rotation policy for key with name.", keyName))
                .doOnSuccess(response -> LOGGER.verbose("Updated key rotation policy for key with name.", keyName))
                .doOnError(error -> LOGGER.warning("Failed to retrieve key rotation policy - {}", error));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    public Response<KeyRotationPolicy> updateKeyRotationPolicyWithResponse(String keyName,
                                                                           KeyRotationPolicy keyRotationPolicy,
                                                                           Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            context = context == null ? Context.NONE : context;
            context = enableSyncRestProxy(context);

            return service.updateKeyRotationPolicy(vaultUrl, keyName, keyServiceVersion.getVersion(), keyRotationPolicy,
                CONTENT_TYPE_HEADER_VALUE, context);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    public CryptographyClientBuilder getCryptographyClientBuilder(String keyName, String keyVersion) {
        if (CoreUtils.isNullOrEmpty(keyName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty."));
        }

        return new CryptographyClientBuilder()
            .keyIdentifier(generateKeyId(keyName, keyVersion))
            .pipeline(getHttpPipeline())
            .serviceVersion(CryptographyServiceVersion.valueOf(keyServiceVersion.name()));
    }

    private String generateKeyId(String keyName, String keyVersion) {
        StringBuilder stringBuilder = new StringBuilder(getVaultUrl());

        if (!getVaultUrl().endsWith("/")) {
            stringBuilder.append("/");
        }

        stringBuilder.append("keys/").append(keyName);

        if (!CoreUtils.isNullOrEmpty(keyVersion)) {
            stringBuilder.append("/").append(keyVersion);
        }

        return stringBuilder.toString();
    }

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }
}

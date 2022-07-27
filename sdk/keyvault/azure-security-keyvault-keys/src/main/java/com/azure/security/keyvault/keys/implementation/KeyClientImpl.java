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
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
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
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.implementation.models.DeletedKeyPage;
import com.azure.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.security.keyvault.keys.implementation.models.KeyPropertiesPage;
import com.azure.security.keyvault.keys.implementation.models.RandomBytes;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

public class KeyClientImpl {
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final KeyService service;
    private final ClientLogger logger = new ClientLogger(KeyAsyncClient.class);
    private final HttpPipeline pipeline;
    private final KeyServiceVersion keyServiceVersion;

    /**
     * Creates a {@link KeyAsyncClient} that uses an {@link HttpPipeline} to service requests.
     *
     * @param vaultUrl URL for the Azure Key Vault service.
     * @param pipeline {@link HttpPipeline} that the HTTP requests and responses will flow through.
     * @param version {@link KeyServiceVersion} of the service to be used when making requests.
     */
    public KeyClientImpl(String vaultUrl, HttpPipeline pipeline, KeyServiceVersion version) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl;
        this.service = RestProxy.create(KeyService.class, pipeline);
        this.pipeline = pipeline;
        this.keyServiceVersion = version;
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
     * The interface defining all the services for {@link KeyAsyncClient} to be used
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
                CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating key - {}", name))
            .doOnSuccess(response -> logger.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", name, error));
    }

    public Response<KeyVaultKey> createKeyWithResponse(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);
        context = enableSyncRestProxy(context);
        return service.createKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<KeyVaultKey>> createKeyWithResponseAsync(CreateKeyOptions createKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateKeyRequestParameters(createKeyOptions);

        return service.createKeyAsync(vaultUrl, createKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating key - {}", createKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", createKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateKeyRequestParameters(createKeyOptions);
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE));
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
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateRsaKeyRequestParameters(createRsaKeyOptions);

        return service.createKeyAsync(vaultUrl, createRsaKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions,
                                                                     Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateRsaKeyRequestParameters(createRsaKeyOptions);
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createRsaKeyOptions.getName(), keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private KeyRequestParameters validateAndCreateRsaKeyRequestParameters(CreateRsaKeyOptions createRsaKeyOptions) {
        Objects.requireNonNull(createRsaKeyOptions, "The Rsa key options parameter cannot be null.");
        return new KeyRequestParameters()
            .setKty(createRsaKeyOptions.getKeyType())
            .setKeySize(createRsaKeyOptions.getKeySize())
            .setKeyOps(createRsaKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createRsaKeyOptions))
            .setPublicExponent(createRsaKeyOptions.getPublicExponent())
            .setTags(createRsaKeyOptions.getTags())
            .setReleasePolicy(createRsaKeyOptions.getReleasePolicy());
    }

    public Mono<Response<KeyVaultKey>> createEcKeyWithResponseAsync(CreateEcKeyOptions createEcKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateEcKeyRequestParameters(createEcKeyOptions);

        return service.createKeyAsync(vaultUrl, createEcKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating Ec key - {}", createEcKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created Ec key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Ec key - {}", createEcKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateEcKeyRequestParameters(createEcKeyOptions);
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createEcKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    private KeyRequestParameters validateAndCreateEcKeyRequestParameters(CreateEcKeyOptions createEcKeyOptions) {
        Objects.requireNonNull(createEcKeyOptions, "The Ec key options cannot be null.");
        return new KeyRequestParameters()
            .setKty(createEcKeyOptions.getKeyType())
            .setCurve(createEcKeyOptions.getCurveName())
            .setKeyOps(createEcKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createEcKeyOptions))
            .setTags(createEcKeyOptions.getTags())
            .setReleasePolicy(createEcKeyOptions.getReleasePolicy());
    }

    public Mono<Response<KeyVaultKey>> createOctKeyWithResponseAsync(CreateOctKeyOptions createOctKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateOctKeyRequestParameters(createOctKeyOptions);

        return service.createKeyAsync(vaultUrl, createOctKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating symmetric key - {}", createOctKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created symmetric key - {}", response.getValue().getName()))
            .doOnError(error ->
                logger.warning("Failed to create symmetric key - {}", createOctKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateOctKeyRequestParameters(createOctKeyOptions);
        context = enableSyncRestProxy(context);

        return service.createKey(vaultUrl, createOctKeyOptions.getName(), keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE));
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

    public Mono<Response<KeyVaultKey>> importKeyWithResponseAsync(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);

        return service.importKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Response<KeyVaultKey> importKeyWithResponse(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);
        context = enableSyncRestProxy(context);

        return service.importKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<KeyVaultKey>>importKeyWithResponseAsync(ImportKeyOptions importKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyImportRequestParameters parameters = validateAndCreateKeyImportRequestParameters(importKeyOptions);

        return service.importKeyAsync(vaultUrl, importKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Importing key - {}", importKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", importKeyOptions.getName(), error));
    }

    public Response<KeyVaultKey> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        context = context == null ? Context.NONE : context;
        KeyImportRequestParameters parameters = validateAndCreateKeyImportRequestParameters(importKeyOptions);
        context = enableSyncRestProxy(context);

        return service.importKey(vaultUrl, importKeyOptions.getName(), keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE));
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
        context = context == null ? Context.NONE : context;

        return service.getKeyAsync(vaultUrl, name, version, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving key - {}", name))
            .doOnSuccess(response -> logger.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKey(vaultUrl, name, version, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponseAsync(KeyProperties keyProperties, Context context,
                                                                KeyOperation... keyOperations) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateUpdateKeyRequestParameters(keyProperties, keyOperations);

        return service.updateKeyAsync(vaultUrl, keyProperties.getName(), keyProperties.getVersion(),
                keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating key - {}", keyProperties.getName()))
            .doOnSuccess(response -> logger.verbose("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update key - {}", keyProperties.getName(), error));
    }

    public Response<KeyVaultKey> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context,
                                                                       KeyOperation... keyOperations) {
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = validateAndCreateUpdateKeyRequestParameters(keyProperties, keyOperations);
        context = enableSyncRestProxy(context);

        return service.updateKey(vaultUrl, keyProperties.getName(), keyProperties.getVersion(),
                keyServiceVersion.getVersion(), ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
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
            activationOperation(name),
            createPollOperation(name),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    private Function<PollingContext<DeletedKey>, Mono<DeletedKey>> activationOperation(String name) {
        return (pollingContext) -> withContext(context -> deleteKeyWithResponse(name, context))
            .flatMap(deletedKeyResponse -> Mono.just(deletedKeyResponse.getValue()));
    }

    /*
     * Polling operation to poll on create delete key operation status.
     */
    private Function<PollingContext<DeletedKey>, Mono<PollResponse<DeletedKey>>> createPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getDeletedKeyPollerAsync(vaultUrl, keyName, keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
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

    private Mono<Response<DeletedKey>> deleteKeyWithResponse(String name, Context context) {
        context = enableSyncRestProxy(context);
        return service.deleteKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting key - {}", name))
            .doOnSuccess(response -> logger.verbose("Deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete key - {}", name, error));
    }

    public Mono<Response<DeletedKey>> getDeletedKeyWithResponseAsync(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Retrieved deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    public Response<DeletedKey> getDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        return service.getDeletedKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<Void>> purgeDeletedKeyWithResponseAsync(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.purgeDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Purging deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Purged deleted key - {}", name))
            .doOnError(error -> logger.warning("Failed to purge deleted key - {}", name, error));
    }

    public Response<Void> purgeDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        return service.purgeDeletedKey(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public PollerFlux<KeyVaultKey, Void> beginRecoverDeletedKeyAsync(String name) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            recoverActivationOperation(name),
            createRecoverPollOperation(name),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultKey>, Mono<KeyVaultKey>> recoverActivationOperation(String name) {
        return (pollingContext) -> withContext(context -> recoverDeletedKeyWithResponse(name, context))
            .flatMap(keyResponse -> Mono.just(keyResponse.getValue()));
    }

    /*
     * Polling operation to poll on create delete key operation status.
     */
    private Function<PollingContext<KeyVaultKey>, Mono<PollResponse<KeyVaultKey>>> createRecoverPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getKeyPollerAsync(vaultUrl, keyName, "", keyServiceVersion.getVersion(),
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE)))
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

    private Mono<Response<KeyVaultKey>> recoverDeletedKeyWithResponse(String name, Context context) {
        return service.recoverDeletedKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Recovering deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Recovered deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to recover deleted key - {}", name, error));
    }

    public Mono<Response<byte[]>> backupKeyWithResponseAsync(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.backupKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Backing up key - {}", name))
            .doOnSuccess(response -> logger.verbose("Backed up key - {}", name))
            .doOnError(error -> logger.warning("Failed to backup key - {}", name, error))
            .flatMap(base64URLResponse -> Mono.just(new SimpleResponse<>(base64URLResponse.getRequest(),
                base64URLResponse.getStatusCode(), base64URLResponse.getHeaders(),
                base64URLResponse.getValue().getValue())));
    }

    public Response<byte[]> backupKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        Response<KeyBackup> backupResponse =  service.backupKey(vaultUrl, name, keyServiceVersion.getVersion(),
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));

        return new SimpleResponse<>(backupResponse.getRequest(),
            backupResponse.getStatusCode(), backupResponse.getHeaders(),
            backupResponse.getValue().getValue());
    }

    public Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponseAsync(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        return service.restoreKeyAsync(vaultUrl, keyServiceVersion.getVersion(), parameters, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Attempting to restore key"))
            .doOnSuccess(response -> logger.verbose("Restored Key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to restore key - {}", error));
    }

    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        return service.restoreKey(vaultUrl, keyServiceVersion.getVersion(), parameters, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeys() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listKeysFirstPage),
                continuationToken -> withContext(context -> listKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeys(Context context) {
        return new PagedFlux<>(
            () -> listKeysFirstPage(context),
            continuationToken -> listKeysNextPage(continuationToken, context));
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listPropertiesOfKeys()}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysNextPage(String continuationToken, Context context) {
        try {
            return service.getKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next keys page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Calls the service and retrieve first page result. It makes one call and retrieve
     */
    private Mono<PagedResponse<KeyProperties>> listKeysFirstPage(Context context) {
        try {
            return service.getKeysAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(), ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing keys"))
                .doOnSuccess(response -> logger.verbose("Listed keys"))
                .doOnError(error -> logger.warning("Failed to list keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public PagedFlux<DeletedKey> listDeletedKeys() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listDeletedKeysFirstPage),
                continuationToken -> withContext(context -> listDeletedKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<DeletedKey> listDeletedKeys(Context context) {
        return new PagedFlux<>(
            () -> listDeletedKeysFirstPage(context),
            continuationToken -> listDeletedKeysNextPage(continuationToken, context));
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listDeletedKeys()}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to
     * one of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link DeletedKey} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysNextPage(String continuationToken, Context context) {
        try {
            return service.getDeletedKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next deleted keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next deleted keys page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next deleted keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Calls the service and retrieve first page result. It makes one call and retrieve
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysFirstPage(Context context) {
        try {
            return service.getDeletedKeysAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing deleted keys"))
                .doOnSuccess(response -> logger.verbose("Listed deleted keys"))
                .doOnError(error -> logger.warning("Failed to list deleted keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listKeyVersionsFirstPage(name, context)),
                continuationToken -> withContext(context -> listKeyVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listKeyVersionsFirstPage(name, context),
            continuationToken -> listKeyVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<KeyProperties>> listKeyVersionsFirstPage(String name, Context context) {
        try {
            return service.getKeyVersionsAsync(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, keyServiceVersion.getVersion(),
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing key versions - {}", name))
                .doOnSuccess(response -> logger.verbose("Listed key versions - {}", name))
                .doOnError(error -> logger.warning("Failed to list key versions - {}", name, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listPropertiesOfKeyVersions(String)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link KeyProperties} instances from the next page of
     * results.
     */
    private Mono<PagedResponse<KeyProperties>> listKeyVersionsNextPage(String continuationToken, Context context) {
        try {
            return service.getKeysAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next key versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next key versions page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next key versions page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public Mono<Response<byte[]>> getRandomBytesWithResponseAsync(int count, Context context) {
        try {
            return service.getRandomBytesAsync(vaultUrl, keyServiceVersion.getVersion(),
                    new GetRandomBytesRequest().setCount(count), "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Getting {} random bytes.", count))
                .doOnSuccess(response -> logger.verbose("Got {} random bytes.", count))
                .doOnError(error -> logger.warning("Failed to get random bytes - {}", error))
                .map(response -> new SimpleResponse<>(response, response.getValue().getBytes()));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    public Response<byte[]> getRandomBytesWithResponse(int count, Context context) {
        context = enableSyncRestProxy(context);
        Response<RandomBytes> randomBytesResponse = service.getRandomBytes(vaultUrl, keyServiceVersion.getVersion(),
                new GetRandomBytesRequest().setCount(count), "application/json",
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
        return new SimpleResponse<>(randomBytesResponse, randomBytesResponse.getValue().getBytes());
    }

    public Mono<Response<ReleaseKeyResult>> releaseKeyWithResponseAsync(String name, String version,
                                                    String targetAttestationToken, ReleaseKeyOptions releaseKeyOptions,
                                                                        Context context) {
        try {
            KeyReleaseParameters keyReleaseParameters = validateAndCreateKeyReleaseParameters(name, version,
                targetAttestationToken, releaseKeyOptions);

            return service.releaseAsync(vaultUrl, name, version, keyServiceVersion.getVersion(), keyReleaseParameters,
                    "application/json", context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Releasing key with name %s and version %s.", name, version))
                .doOnSuccess(response -> logger.verbose("Released key with name %s and version %s.", name, version))
                .doOnError(error -> logger.warning("Failed to release key - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version,
                             String targetAttestationToken, ReleaseKeyOptions releaseKeyOptions, Context context) {
        try {
            KeyReleaseParameters keyReleaseParameters = validateAndCreateKeyReleaseParameters(name, version,
                targetAttestationToken, releaseKeyOptions);
            context = enableSyncRestProxy(context);

            return service.release(vaultUrl, name, version, keyServiceVersion.getVersion(), keyReleaseParameters,
                    "application/json", context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
        } catch (RuntimeException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    private KeyReleaseParameters validateAndCreateKeyReleaseParameters(String name, String version,
                                               String targetAttestationToken, ReleaseKeyOptions releaseKeyOptions) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be null or empty"));
        }

        if (CoreUtils.isNullOrEmpty(targetAttestationToken)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'targetAttestationToken' cannot be null or empty"));
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
                return monoError(logger, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            return service.rotateKeyAsync(vaultUrl, name, keyServiceVersion.getVersion(), "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Rotating key with name %s.", name))
                .doOnSuccess(response -> logger.verbose("Rotated key with name %s.", name))
                .doOnError(error -> logger.warning("Failed to rotate key - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    public Response<KeyVaultKey> rotateKeyWithResponse(String name, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be null or empty"));
            }
            context = enableSyncRestProxy(context);
            return service.rotateKey(vaultUrl, name, keyServiceVersion.getVersion(), "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
        } catch (RuntimeException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    public Mono<Response<KeyRotationPolicy>> getKeyRotationPolicyWithResponseAsync(String keyName, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                return monoError(logger, new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            return service.getKeyRotationPolicyAsync(vaultUrl, keyName, keyServiceVersion.getVersion(), "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Retrieving key rotation policy for key with name.", keyName))
                .doOnSuccess(response -> logger.verbose("Retrieved key rotation policy for key with name.", keyName))
                .doOnError(error -> logger.warning("Failed to retrieve key rotation policy - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    public Response<KeyRotationPolicy> getKeyRotationPolicyWithResponse(String keyName, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty"));
            }
            context = enableSyncRestProxy(context);
            return service.getKeyRotationPolicy(vaultUrl, keyName, keyServiceVersion.getVersion(), "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
        } catch (RuntimeException e) {
            throw logger.logExceptionAsError(e);
        }
    }


    public Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicyWithResponseAsync(String keyName,
                                                                          KeyRotationPolicy keyRotationPolicy,
                                                                          Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                return monoError(logger, new IllegalArgumentException("'keyName' cannot be null or empty"));
            }

            return service.updateKeyRotationPolicyAsync(vaultUrl, keyName, keyServiceVersion.getVersion(), keyRotationPolicy,
                    "application/json", context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Updating key rotation policy for key with name.", keyName))
                .doOnSuccess(response -> logger.verbose("Updated key rotation policy for key with name.", keyName))
                .doOnError(error -> logger.warning("Failed to retrieve key rotation policy - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    public Response<KeyRotationPolicy> updateKeyRotationPolicyWithResponse(String keyName,
                                                                                      KeyRotationPolicy keyRotationPolicy,
                                                                                      Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(keyName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty"));
            }
            context = enableSyncRestProxy(context);
            return service.updateKeyRotationPolicy(vaultUrl, keyName, keyServiceVersion.getVersion(), keyRotationPolicy,
                    "application/json", context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
        } catch (RuntimeException e) {
            throw logger.logExceptionAsError(e);
        }
    }

    public CryptographyClientBuilder getCryptographyClientBuilder(String keyName, String keyVersion) {
        if (CoreUtils.isNullOrEmpty(keyName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty."));
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

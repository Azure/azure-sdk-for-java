// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
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
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicyAttributes;
import com.azure.security.keyvault.keys.implementation.models.LifetimeAction;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionTrigger;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionsType;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.RandomBytes;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyAsyncClient} provides asynchronous methods to manage {@link KeyVaultKey keys} in the Azure Key Vault.
 * The client supports creating, retrieving, updating, deleting, purging, backing up, restoring, listing, releasing
 * and rotating the {@link KeyVaultKey keys}. The client also supports listing {@link DeletedKey deleted keys} for a
 * soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the async client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.instantiation}
 *
 * @see KeyClientBuilder
 * @see PagedFlux
 */
@ServiceClient(builder = KeyClientBuilder.class, isAsync = true, serviceInterfaces = KeyService.class)
public final class KeyAsyncClient {
    private final String apiVersion;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final KeyService service;
    private final ClientLogger logger = new ClientLogger(KeyAsyncClient.class);
    private final HttpPipeline pipeline;

    /**
     * Creates a {@link KeyAsyncClient} that uses an {@link HttpPipeline} to service requests.
     *
     * @param vaultUrl URL for the Azure Key Vault service.
     * @param pipeline {@link HttpPipeline} that the HTTP requests and responses will flow through.
     * @param version {@link KeyServiceVersion} of the service to be used when making requests.
     */
    KeyAsyncClient(URL vaultUrl, HttpPipeline pipeline, KeyServiceVersion version) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();
        this.service = RestProxy.create(KeyService.class, pipeline);
        this.pipeline = pipeline;
        apiVersion = version.getVersion();
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
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the default polling interval for long running operations.
     *
     * @return The default polling interval for long running operations
     */
    Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of {@link KeyVaultKey key} to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM},
     * {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key}. Subscribes to the call asynchronously and prints out the newly
     * {@link KeyVaultKey created key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createKey#String-KeyType}
     *
     * @param name The name of the {@link KeyVaultKey key} being created.
     * @param keyType The type of {@link KeyVaultKey key} to create. For valid values, see {@link KeyType KeyType}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@code name} is an empty string.
     * @throws NullPointerException If {@code name} or {@code keyType} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createKey(String name, KeyType keyType) {
        try {
            return withContext(context -> createKeyWithResponse(name, keyType, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of {@link KeyVaultKey key} to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM},
     * {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key}. Subscribes to the call asynchronously and prints out the newly
     * {@link KeyVaultKey created key} details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createKeyWithResponse#CreateKeyOptions}
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the
     * {@link KeyVaultKey key} being created.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code name} or {@code keyType} are {@code null}.
     * @throws ResourceModifiedException If {@code createKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createKeyWithResponse(CreateKeyOptions createKeyOptions) {
        try {
            return withContext(context -> createKeyWithResponse(createKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createKeyWithResponse(String name, KeyType keyType, Context context) {
        KeyRequestParameters parameters = new KeyRequestParameters().setKty(keyType);
        return service.createKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating key - {}", name))
            .doOnSuccess(response -> logger.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", name, error));
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} parameter is required. The {@link CreateKeyOptions#getExpiresOn() expires} and
     * {@link CreateKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not specified.
     * </p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA},
     * {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} which activates in one day and expires in one year. Subscribes to
     * the call asynchronously and prints out the newly {@link KeyVaultKey created key} details when a response has been
     * received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createKey#CreateKeyOptions}
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the
     * {@link KeyVaultKey key} being created.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createKey(CreateKeyOptions createKeyOptions) {
        try {
            return createKeyWithResponse(createKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        Objects.requireNonNull(createKeyOptions, "The key create options parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createKeyOptions.getKeyType())
            .setKeyOps(createKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createKeyOptions))
            .setTags(createKeyOptions.getTags())
            .setReleasePolicy(createKeyOptions.getReleasePolicy());

        return service.createKey(vaultUrl, createKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating key - {}", createKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create key - {}", createKeyOptions.getName(), error));
    }

    /**
     * /**
     * Creates a new {@link KeyVaultKey RSA key} and stores it in the key vault. The create RSA key operation can be
     * used to create any RSA key type in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name already
     * exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} parameter is required. The {@link CreateRsaKeyOptions#getKeySize() keySize}
     * can be optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateRsaKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} with size 2048 which activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly {@link KeyVaultKey created key} details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKey#CreateRsaKeyOptions}
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the
     * {@link KeyVaultKey RSA key} being created.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateRsaKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createRsaKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createRsaKey(CreateRsaKeyOptions createRsaKeyOptions) {
        try {
            return createRsaKeyWithResponse(createRsaKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new {@link KeyVaultKey RSA key} and stores it in the key vault. The create RSA key operation can be
     * used to create any RSA key type in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name already
     * exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} parameter is required. The {@link CreateRsaKeyOptions#getKeySize() keySize}
     * can be optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateRsaKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} with size 2048 which activates in one day and expires in one year.
     * Subscribes to the call asynchronously and prints out the newly {@link KeyVaultKey created key} details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createRsaKeyWithResponse#CreateRsaKeyOptions}
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the
     * {@link KeyVaultKey RSA key} being created.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     * KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateRsaKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createRsaKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions) {
        try {
            return withContext(context -> createRsaKeyWithResponse(createRsaKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions, Context context) {
        Objects.requireNonNull(createRsaKeyOptions, "The Rsa key options parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createRsaKeyOptions.getKeyType())
            .setKeySize(createRsaKeyOptions.getKeySize())
            .setKeyOps(createRsaKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createRsaKeyOptions))
            .setPublicExponent(createRsaKeyOptions.getPublicExponent())
            .setTags(createRsaKeyOptions.getTags())
            .setReleasePolicy(createRsaKeyOptions.getReleasePolicy());

        return service.createKey(vaultUrl, createRsaKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating Rsa key - {}", createRsaKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created Rsa key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Rsa key - {}", createRsaKeyOptions.getName(), error));
    }

    /**
     * Creates a new {@link KeyVaultKey EC key} and stores it in the key vault. The create EC key operation can be
     * used to create any EC {@link KeyType key type} in Azure Key Vault. If a {@link KeyVaultKey key} with the
     * provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve}
     * can be optionally specified. If not specified, the default value {@link KeyCurveName#P_256 P-256} is used by
     * Azure Key Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not specified.
     * </p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key} with a {@link KeyCurveName#P_384 P-384} web key curve. The key
     * activates in one day and expires in one year. Subscribes to the call asynchronously and prints out the newly
     * {@link KeyVaultKey created key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createEcKey#CreateEcKeyOptions}
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the
     * {@link KeyVaultKey EC key} being created.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateEcKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createEcKey(CreateEcKeyOptions createEcKeyOptions) {
        try {
            return createEcKeyWithResponse(createEcKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new {@link KeyVaultKey EC key} and stores it in the key vault. The create EC key operation can be
     * used to create any EC {@link KeyType key type} in Azure Key Vault. If a {@link KeyVaultKey key} with the
     * provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve}
     * can be optionally specified. If not specified, the default value {@link KeyCurveName#P_256 P-256} is used by
     * Azure Key Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.
     * </p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key} with a {@link KeyCurveName#P_384 P-384} web key curve. The key
     * activates in one day and expires in one year. Subscribes to the call asynchronously and prints out the newly
     * {@link KeyVaultKey created key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createEcKeyWithResponse#CreateEcKeyOptions}
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the
     * {@link KeyVaultKey EC key} being created.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateEcKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions) {
        try {
            return withContext(context -> createEcKeyWithResponse(createEcKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        Objects.requireNonNull(createEcKeyOptions, "The Ec key options cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createEcKeyOptions.getKeyType())
            .setCurve(createEcKeyOptions.getCurveName())
            .setKeyOps(createEcKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createEcKeyOptions))
            .setTags(createEcKeyOptions.getTags())
            .setReleasePolicy(createEcKeyOptions.getReleasePolicy());

        return service.createKey(vaultUrl, createEcKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating Ec key - {}", createEcKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created Ec key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create Ec key - {}", createEcKeyOptions.getName(), error));
    }

    /**
     * Creates and stores a new {@link KeyVaultKey symmetric key} in the key vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the key. This operation requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey symmetric key}. The {@link KeyVaultKey key} activates in one day and expires
     * in one year. Subscribes to the call asynchronously and prints out the details of the newly
     * {@link KeyVaultKey created key} when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createOctKey#CreateOctKeyOptions}
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * {@link KeyVaultKey symmetric key} being created.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateOctKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code ecKeyCreateOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code ecKeyCreateOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> createOctKey(CreateOctKeyOptions createOctKeyOptions) {
        try {
            return createOctKeyWithResponse(createOctKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates and stores a new {@link KeyVaultKey symmetric key} in the key vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the key. This operation requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey symmetric key}. The {@link KeyVaultKey key} activates in one day and expires
     * in one year. Subscribes to the call asynchronously and prints out the details of the newly
     * {@link KeyVaultKey created key} when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.createOctKeyWithResponse#CreateOctKeyOptions}
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * {@link KeyVaultKey symmetric key} being created.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     * KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateOctKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createOctKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions) {
        try {
            return withContext(context -> createOctKeyWithResponse(createOctKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions, Context context) {
        Objects.requireNonNull(createOctKeyOptions, "The create key options cannot be null.");

        context = context == null ? Context.NONE : context;

        KeyRequestParameters parameters = new KeyRequestParameters()
            .setKty(createOctKeyOptions.getKeyType())
            .setKeyOps(createOctKeyOptions.getKeyOperations())
            .setKeyAttributes(new KeyRequestAttributes(createOctKeyOptions))
            .setTags(createOctKeyOptions.getTags())
            .setReleasePolicy(createOctKeyOptions.getReleasePolicy());

        return service.createKey(vaultUrl, createOctKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating symmetric key - {}", createOctKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Created symmetric key - {}", response.getValue().getName()))
            .doOnError(error ->
                logger.warning("Failed to create symmetric key - {}", createOctKeyOptions.getName(), error));
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into key vault. Subscribes to the call asynchronously and prints out the
     * newly {@link KeyVaultKey imported key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.importKey#String-JsonWebKey}
     *
     * @param name The name for the imported key.
     * @param keyMaterial The Json web key being imported.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     * KeyVaultKey
     * imported key}.
     *
     * @throws HttpResponseException If {@code name} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> importKey(String name, JsonWebKey keyMaterial) {
        try {
            return withContext(context -> importKeyWithResponse(name, keyMaterial, context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> importKeyWithResponse(String name, JsonWebKey keyMaterial, Context context) {
        KeyImportRequestParameters parameters = new KeyImportRequestParameters().setKey(keyMaterial);
        return service.importKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Importing key - {}", name))
            .doOnSuccess(response -> logger.verbose("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", name, error));
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p>{@link ImportKeyOptions} is required and its fields {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} cannot be {@code null}. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. If not specified, no values are set for the fields. The
     * {@link ImportKeyOptions#isEnabled() enabled} field is set to {@code true} and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by Azure Key Vault, if they are
     * not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into key vault. Subscribes to the call asynchronously and prints out the
     * newly {@link KeyVaultKey imported key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.importKey#ImportKeyOptions}
     *
     * @param importKeyOptions The {@link ImportKeyOptions options object} containing information about the
     * {@link JsonWebKey} being imported.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey imported key}.
     *
     * @throws HttpResponseException If {@link ImportKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code importKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> importKey(ImportKeyOptions importKeyOptions) {
        try {
            return importKeyWithResponse(importKeyOptions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p>{@link ImportKeyOptions} is required and its fields {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} cannot be {@code null}. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. If not specified, no values are set for the fields. The
     * {@link ImportKeyOptions#isEnabled() enabled} field is set to {@code true} and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by Azure Key Vault, if they are
     * not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into key vault. Subscribes to the call asynchronously and prints out the
     * newly {@link KeyVaultKey imported key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.importKeyWithResponse#ImportKeyOptions}
     *
     * @param importKeyOptions The {@link ImportKeyOptions options object} containing information about the
     * {@link JsonWebKey} being imported.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultKey imported key}.
     *
     * @throws HttpResponseException If {@link ImportKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code importKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> importKeyWithResponse(ImportKeyOptions importKeyOptions) {
        try {
            return withContext(context -> importKeyWithResponse(importKeyOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        Objects.requireNonNull(importKeyOptions, "The key import configuration parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyImportRequestParameters parameters = new KeyImportRequestParameters()
            .setKey(importKeyOptions.getKey())
            .setHsm(importKeyOptions.isHardwareProtected())
            .setKeyAttributes(new KeyRequestAttributes(importKeyOptions))
            .setTags(importKeyOptions.getTags())
            .setReleasePolicy(importKeyOptions.getReleasePolicy());

        return service.importKey(vaultUrl, importKeyOptions.getName(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Importing key - {}", importKeyOptions.getName()))
            .doOnSuccess(response -> logger.verbose("Imported key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to import key - {}", importKeyOptions.getName(), error));
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Subscribes to the call asynchronously
     * and prints out the {@link KeyVaultKey retrieved key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String-String}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     * equivalent to calling {@link KeyAsyncClient#getKey(String)}, with the latest version being retrieved.
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     * The content of the key is {@code null} if both {@code name} and {@code version} are {@code null} or empty.
     *
     * @throws HttpResponseException If a valid {@code name} and a non null/empty {@code version} is specified.
     * @throws ResourceNotFoundException When a {@link KeyVaultKey key} with the provided {@code name} doesn't exist in
     * the key vault or an empty/{@code null} {@code name} and a non-null/empty {@code version} is provided.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey(String name, String version) {
        try {
            return getKeyWithResponse(name, version).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Subscribes to the call asynchronously
     * and prints out the {@link KeyVaultKey retrieved key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getKeyWithResponse#String-String}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     * @param version The version of the key to retrieve. If this is an empty String or null, this call is
     * equivalent to calling {@link KeyAsyncClient#getKey(String)}, with the latest version being retrieved.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultKey key}. The content of the key is {@code null} if both {@code name} and
     * {@code version} are {@code null} or empty.
     *
     * @throws HttpResponseException If a valid {@code name} and a non-null/empty {@code version} is specified.
     * @throws ResourceNotFoundException When a {@link KeyVaultKey key} with the provided {@code name} doesn't exist in
     * the key vault or an empty/{@code null} {@code name} and a non-null/empty {@code version} is provided.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> getKeyWithResponse(String name, String version) {
        try {
            return withContext(context -> getKeyWithResponse(name, version == null ? "" : version, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> getKeyWithResponse(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getKey(vaultUrl, name, version, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving key - {}", name))
            .doOnSuccess(response -> logger.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Subscribes to the call asynchronously
     * and prints out the {@link KeyVaultKey retrieved key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}. The content of the key is {@code null}
     * if {@code name} is {@code null} or empty.
     *
     * @throws HttpResponseException If a valid {@code name} and a non-null/empty {@code version} is specified.
     * @throws ResourceNotFoundException When a {@link KeyVaultKey key} with the provided {@code name} doesn't exist in
     * the key vault or an empty/{@code null} {@code name} and a non-null/empty {@code version} is provided.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey(String name) {
        try {
            return getKeyWithResponse(name, "").flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the {@link KeyProperties attributes} and {@link KeyOperation key operations} associated with the
     * specified {@link KeyVaultKey key}, but not the cryptographic key material of the specified
     * {@link KeyVaultKey key} in the key vault. The update operation changes specified
     * {@link KeyProperties attributes} of an existing stored {@link KeyVaultKey key} and
     * {@link KeyProperties attributes} that are not specified in the request are left unchanged. The cryptographic
     * key material of a {@link KeyVaultKey key} itself cannot be changed. This operation requires the
     * {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the {@link KeyVaultKey key}, changes its notBefore time and then updates it in the
     * Azure Key Vault. Subscribes to the call asynchronously and prints out the {@link KeyVaultKey returned key}
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyPropertiesWithResponse#KeyProperties-KeyOperation}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated {@link KeyOperation key operations} to associate with the key.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultKey updated key}.
     *
     * @throws HttpResponseException If {@link KeyProperties#getName() name} or
     * {@link KeyProperties#getVersion() version} is an empty string.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException When a key with {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponse(KeyProperties keyProperties, KeyOperation... keyOperations) {
        try {
            return withContext(context -> updateKeyPropertiesWithResponse(keyProperties, context, keyOperations));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates the {@link KeyProperties attributes} and {@link KeyOperation key operations} associated with the
     * specified {@link KeyVaultKey key}, but not the cryptographic key material of the specified
     * {@link KeyVaultKey key} in the key vault. The update operation changes specified
     * {@link KeyProperties attributes} of an existing stored {@link KeyVaultKey key} and
     * {@link KeyProperties attributes} that are not specified in the request are left unchanged. The cryptographic
     * key material of a {@link KeyVaultKey key} itself cannot be changed. This operation requires the
     * {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the {@link KeyVaultKey key}, changes its notBefore time and then updates it in the
     * Azure Key Vault. Subscribes to the call asynchronously and prints out the {@link KeyVaultKey returned key}
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyProperties#KeyProperties-KeyOperation}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated {@link KeyOperation key operations} to associate with the key.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey updated key}.
     *
     * @throws HttpResponseException If {@link KeyProperties#getName() name} or
     * {@link KeyProperties#getVersion() version} is an empty string.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException When a key with {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> updateKeyProperties(KeyProperties keyProperties, KeyOperation... keyOperations) {
        try {
            return updateKeyPropertiesWithResponse(keyProperties, keyOperations).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context, KeyOperation... keyOperations) {
        Objects.requireNonNull(keyProperties, "The key properties input parameter cannot be null.");
        context = context == null ? Context.NONE : context;
        KeyRequestParameters parameters = new KeyRequestParameters()
            .setTags(keyProperties.getTags())
            .setKeyAttributes(new KeyRequestAttributes(keyProperties))
            .setReleasePolicy(keyProperties.getReleasePolicy());

        if (keyOperations.length > 0) {
            parameters.setKeyOps(Arrays.asList(keyOperations));
        }

        return service.updateKey(vaultUrl, keyProperties.getName(), keyProperties.getVersion(), apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating key - {}", keyProperties.getName()))
            .doOnSuccess(response -> logger.verbose("Updated key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to update key - {}", keyProperties.getName(), error));
    }

    /**
     * Deletes a {@link KeyVaultKey key} of any type from the key vault. If soft-delete is enabled on the key vault then
     * the {@link KeyVaultKey key} is placed in the deleted state and requires to be purged for permanent deletion
     * else the {@link KeyVaultKey key} is permanently deleted. The delete operation applies to any
     * {@link KeyVaultKey key} stored in Azure Key Vault but it cannot be applied to an individual version
     * of a {@link KeyVaultKey key}. This operation removes the cryptographic material associated with the
     * {@link KeyVaultKey key}, which means the {@link KeyVaultKey key} is not usable for {@code Sign/Verify},
     * {@code Wrap/Unwrap} or {@code Encrypt/Decrypt} operations. This operation requires the {@code keys/delete}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the {@link KeyVaultKey key} in the Azure Key Vault. Subscribes to the call asynchronously and prints
     * out the {@link KeyVaultKey deleted key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.deleteKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key} to be deleted.
     *
     * @return A {@link PollerFlux} to poll on the {@link DeletedKey deleted key} status.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DeletedKey, Void> beginDeleteKey(String name) {
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
            withContext(context -> service.getDeletedKeyPoller(vaultUrl, keyName, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(deletedKeyResponse -> {
                    if (deletedKeyResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, deletedKeyResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<DeletedKey>> deleteKeyWithResponse(String name, Context context) {
        return service.deleteKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting key - {}", name))
            .doOnSuccess(response -> logger.verbose("Deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete key - {}", name, error));
    }

    /**
     * Gets the public part of a {@link KeyVaultKey deleted key}. The get deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the {@link KeyVaultKey deleted key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKey#String}
     *
     * @param name The name of the deleted {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link DeletedKey deleted key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeletedKey> getDeletedKey(String name) {
        try {
            return getDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the public part of a {@link KeyVaultKey deleted key}. The get deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the {@link KeyVaultKey deleted key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getDeletedKeyWithResponse#String}
     *
     * @param name The name of the deleted {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the {@link
     * DeletedKey deleted key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name) {
        try {
            return withContext(context -> getDeletedKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DeletedKey>> getDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Retrieved deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    /**
     * Permanently deletes the specified {@link KeyVaultKey key} without the possibility of recovery. The purge
     * deleted key operation is applicable for soft-delete enabled vaults. This operation requires the
     * {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the status code from the server response when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKey#String}
     *
     * @param name The name of the {@link KeyVaultKey deleted key}.
     *
     * @return An empty {@link Mono}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purgeDeletedKey(String name) {
        try {
            return purgeDeletedKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Permanently deletes the specified {@link KeyVaultKey key} without the possibility of recovery. The purge
     * deleted key operation is applicable for soft-delete enabled vaults. This operation requires the
     * {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Subscribes to the call
     * asynchronously and prints out the status code from the server response when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.purgeDeletedKeyWithResponse#String}
     *
     * @param name The name of the {@link KeyVaultKey deleted key}.
     *
     * @return A {@link Mono} containing a Response containing status code and HTTP headers.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purgeDeletedKeyWithResponse(String name) {
        try {
            return withContext(context -> purgeDeletedKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purgeDeletedKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.purgeDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Purging deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Purged deleted key - {}", name))
            .doOnError(error -> logger.warning("Failed to purge deleted key - {}", name, error));
    }

    /**
     * Recovers the {@link KeyVaultKey deleted key} in the key vault to its latest version and can only be performed
     * on a soft-delete enabled vault. An attempt to recover an {@link KeyVaultKey non-deleted key} will return an
     * error. Consider this the inverse of the delete operation on soft-delete enabled vaults. This operation
     * requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Subscribes to the
     * call asynchronously and prints out the recovered key details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.recoverDeletedKey#String}
     *
     * @param name The name of the {@link KeyVaultKey deleted key} to be recovered.
     *
     * @return A {@link PollerFlux} to poll on the {@link KeyVaultKey recovered key} status.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
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
            withContext(context -> service.getKeyPoller(vaultUrl, keyName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE)))
                .flatMap(keyResponse -> {
                    if (keyResponse.getStatusCode() == 404) {
                        return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                            pollingContext.getLatestResponse().getValue())));
                    }
                    return Mono.defer(() -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        keyResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching
                // here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    Mono<Response<KeyVaultKey>> recoverDeletedKeyWithResponse(String name, Context context) {
        return service.recoverDeletedKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Recovering deleted key - {}", name))
            .doOnSuccess(response -> logger.verbose("Recovered deleted key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to recover deleted key - {}", name, error));
    }

    /**
     * Requests a backup of the specified {@link KeyVaultKey key} be downloaded to the client. The key backup
     * operation exports a {@link KeyVaultKey key} from Azure Key Vault in a protected form. Note that this operation
     * does not return key material in a form that can be used outside the Azure Key Vault system, the returned key
     * material is either protected to a Azure Key Vault HSM or to Azure Key Vault itself. The intent of this
     * operation is to allow a client to generate a {@link KeyVaultKey key} in one Azure Key Vault instance, backup the
     * {@link KeyVaultKey key}, and then restore it into another Azure Key Vault instance. The backup operation may
     * be used to export, in protected form, any {@link KeyType key type} from Azure Key Vault. Individual versions
     * of a {@link KeyVaultKey key} cannot be backed up. {@code Backup/Restore} can be performed within geographical
     * boundaries only; meaning that a backup from one geographical area cannot be restored to another geographical
     * area. For example, a backup from the US geographical area cannot be restored in an EU geographical area. This
     * operation requires the {@code key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the {@link KeyVaultKey key} from the key vault. Subscribes to the call asynchronously and prints out
     * the length of the key's backup byte array returned in the response.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.backupKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the backed up key blob.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> backupKey(String name) {
        try {
            return backupKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Requests a backup of the specified {@link KeyVaultKey key} be downloaded to the client. The key backup
     * operation exports a {@link KeyVaultKey key} from Azure Key Vault in a protected form. Note that this operation
     * does not return key material in a form that can be used outside the Azure Key Vault system, the returned key
     * material is either protected to a Azure Key Vault HSM or to Azure Key Vault itself. The intent of this
     * operation is to allow a client to generate a {@link KeyVaultKey key} in one Azure Key Vault instance, backup the
     * {@link KeyVaultKey key}, and then restore it into another Azure Key Vault instance. The backup operation may
     * be used to export, in protected form, any {@link KeyType key type} from Azure Key Vault. Individual versions
     * of a {@link KeyVaultKey key} cannot be backed up. {@code Backup/Restore} can be performed within geographical
     * boundaries only; meaning that a backup from one geographical area cannot be restored to another geographical
     * area. For example, a backup from the US geographical area cannot be restored in an EU geographical area. This
     * operation requires the {@code key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the {@link KeyVaultKey key} from the key vault. Subscribes to the call asynchronously and prints out
     * the length of the key's backup byte array returned in the response.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.backupKeyWithResponse#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the backed up
     * key blob.
     *
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<byte[]>> backupKeyWithResponse(String name) {
        try {
            return withContext(context -> backupKeyWithResponse(name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<byte[]>> backupKeyWithResponse(String name, Context context) {
        context = context == null ? Context.NONE : context;
        return service.backupKey(vaultUrl, name, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Backing up key - {}", name))
            .doOnSuccess(response -> logger.verbose("Backed up key - {}", name))
            .doOnError(error -> logger.warning("Failed to backup key - {}", name, error))
            .flatMap(base64URLResponse -> Mono.just(new SimpleResponse<byte[]>(base64URLResponse.getRequest(),
                base64URLResponse.getStatusCode(), base64URLResponse.getHeaders(), base64URLResponse.getValue().getValue())));
    }

    /**
     * Restores a backed up {@link KeyVaultKey key} to a vault. Imports a previously backed up {@link KeyVaultKey key}
     * into Azure Key Vault, restoring the {@link KeyVaultKey key}, its key identifier, attributes and access control
     * policies. The restore operation may be used to import a previously backed up {@link KeyVaultKey key}. Individual
     * versions of a {@link KeyVaultKey key} cannot be restored. The {@link KeyVaultKey key} is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target key vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the {@link KeyVaultKey key} is restored to a different vault. Restore will restore all versions
     * and preserve version identifiers. The restore operation is subject to security constraints: The target key
     * vault must be owned by the same Microsoft Azure Subscription as the source key vault. The user must have
     * the {@code restore} permission in the target key vault. This operation requires the {@code keys/restore}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the {@link KeyVaultKey key} in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the restored key details when a response has been received.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackup#byte}
     *
     * @param backup The backup blob associated with the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultKey restored key}.
     *
     * @throws ResourceModifiedException When {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> restoreKeyBackup(byte[] backup) {
        try {
            return restoreKeyBackupWithResponse(backup).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Restores a backed up {@link KeyVaultKey key} to a vault. Imports a previously backed up {@link KeyVaultKey key}
     * into Azure Key Vault, restoring the {@link KeyVaultKey key}, its key identifier, attributes and access control
     * policies. The restore operation may be used to import a previously backed up {@link KeyVaultKey key}. Individual
     * versions of a {@link KeyVaultKey key} cannot be restored. The {@link KeyVaultKey key} is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target key vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the {@link KeyVaultKey key} is restored to a different vault. Restore will restore all versions
     * and preserve version identifiers. The restore operation is subject to security constraints: The target key
     * vault must be owned by the same Microsoft Azure Subscription as the source key vault. The user must have
     * the {@code restore} permission in the target key vault. This operation requires the {@code keys/restore}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the {@link KeyVaultKey key} in the key vault from its backup. Subscribes to the call asynchronously
     * and prints out the restored key details when a response has been received.</p>
     * //Pass the Key Backup Byte array to the restore operation.
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.restoreKeyBackupWithResponse#byte}
     *
     * @param backup The backup blob associated with the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultKey restored key}.
     *
     * @throws ResourceModifiedException When {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponse(byte[] backup) {
        try {
            return withContext(context -> restoreKeyBackupWithResponse(backup, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        context = context == null ? Context.NONE : context;
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().setKeyBackup(backup);
        return service.restoreKey(vaultUrl, apiVersion, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Attempting to restore key"))
            .doOnSuccess(response -> logger.verbose("Restored Key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to restore key - {}", error));
    }

    /**
     * List {@link KeyVaultKey keys} in the key vault. Retrieves a list of the {@link KeyVaultKey keys} in the key
     * vault as {@link JsonWebKey} structures that contain the public part of a stored {@link KeyVaultKey key}. The list
     * operation is applicable to all {@link KeyType key types} and the individual {@link KeyVaultKey key} response
     * in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material from this information. Convert the
     * {@link Flux} containing {@link KeyProperties key properties} to {@link Flux} containing
     * {@link KeyVaultKey key} using {@link KeyAsyncClient#getKey(String, String)} within
     * {@link Flux#flatMap(Function)}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.listPropertiesOfKeys}
     *
     * @return A {@link PagedFlux} containing {@link KeyProperties key} of all the keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listPropertiesOfKeys() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listKeysFirstPage),
                continuationToken -> withContext(context -> listKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<KeyProperties> listPropertiesOfKeys(Context context) {
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
            return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next keys page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next keys page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Calls the service and retrieve first page result. It makes one call and retrieve
     * {@link KeyAsyncClient#DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<KeyProperties>> listKeysFirstPage(Context context) {
        try {
            return service.getKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing keys"))
                .doOnSuccess(response -> logger.verbose("Listed keys"))
                .doOnError(error -> logger.warning("Failed to list keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The {@link DeletedKey deleted keys} are retrieved as
     * {@link JsonWebKey} structures that contain the public part of a {@link DeletedKey deleted key}. The get deleted
     * keys operation is applicable for vaults enabled for soft-delete. This operation requires the {@code keys/list}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the {@link DeletedKey deleted keys} in the key vault. Subscribes to the call asynchronously and prints
     * out the recovery id of each {@link DeletedKey deleted key} when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.listDeletedKeys}
     *
     * @return A {@link PagedFlux} containing all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeletedKey> listDeletedKeys() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listDeletedKeysFirstPage),
                continuationToken -> withContext(context -> listDeletedKeysNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<DeletedKey> listDeletedKeys(Context context) {
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
            return service.getDeletedKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next deleted keys page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next deleted keys page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next deleted keys page - Page {} ", continuationToken,
                    error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Calls the service and retrieve first page result. It makes one call and retrieve
     * {@link KeyAsyncClient#DEFAULT_MAX_PAGE_RESULTS} values.
     */
    private Mono<PagedResponse<DeletedKey>> listDeletedKeysFirstPage(Context context) {
        try {
            return service.getDeletedKeys(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing deleted keys"))
                .doOnSuccess(response -> logger.verbose("Listed deleted keys"))
                .doOnError(error -> logger.warning("Failed to list deleted keys", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all versions of the specified {@link KeyVaultKey keys}. The individual key response in the flux is
     * represented by {@link KeyProperties} as only the key identifier, attributes and tags are provided in the
     * response. The key material values are not provided in the response. This operation requires the
     * {@code keys/list} permission.
     *
     * <p>It is possible to get the keys with key material of all the versions from this information. Convert the
     * {@link Flux} containing {@link KeyProperties key properties} to {@link Flux} containing
     * {@link KeyVaultKey key } using {@link KeyAsyncClient#getKey(String, String)} within
     * {@link Flux#flatMap(Function)}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.listKeyVersions}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return A {@link PagedFlux} containing {@link KeyProperties} of all the versions of the specified
     * {@link KeyVaultKey keys} in the vault. {@link Flux} is empty if key with {@code name} does not exist in the key
     * vault.
     *
     * @throws ResourceNotFoundException When a given key {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listKeyVersionsFirstPage(name, context)),
                continuationToken -> withContext(context -> listKeyVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedFlux<>(
            () -> listKeyVersionsFirstPage(name, context),
            continuationToken -> listKeyVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<KeyProperties>> listKeyVersionsFirstPage(String name, Context context) {
        try {
            return service.getKeyVersions(vaultUrl, name, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
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
            return service.getKeys(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next key versions page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next key versions page - Page {} ", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next key versions page - Page {} ", continuationToken,
                    error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the retrieved bytes in
     * base64Url format.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytes#int}
     *
     * @param count The requested number of random bytes.
     *
     * @return A {@link Mono} containing the requested number of bytes containing random values from a managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RandomBytes> getRandomBytes(int count) {
        try {
            return withContext(context -> getRandomBytesWithResponse(count, context)
                .flatMap(FluxUtil::toMono));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the
     * {@link Response HTTP Response} details and the retrieved bytes in base64Url format.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getRandomBytesWithResponse#int}
     *
     * @param count The requested number of random bytes.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} for this operation and the requested number
     * of bytes containing random values from a managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RandomBytes>> getRandomBytesWithResponse(int count) {
        try {
            return withContext(context -> getRandomBytesWithResponse(count, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<Response<RandomBytes>> getRandomBytesWithResponse(int count, Context context) {
        try {
            return service.getRandomBytes(vaultUrl, apiVersion, new GetRandomBytesRequest().setCount(count),
                    "application/json", context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Getting {} random bytes.", count))
                .doOnSuccess(response -> logger.verbose("Got {} random bytes.", count))
                .doOnError(error -> logger.warning("Failed to get random bytes - {}", error))
                .map(response -> new SimpleResponse<>(response, new RandomBytes(response.getValue().getBytes())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Releases the latest version of a {@link KeyVaultKey key}.
     *
     * <p>The {@link KeyVaultKey key} must be exportable. This operation requires the {@code keys/release} permission.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Subscribes to the call asynchronously and prints out the signed object
     * that contains the {@link KeyVaultKey released key} when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String}
     *
     * @param name The name of the {@link KeyVaultKey key} to release.
     * @param target The attestation assertion for the target of the {@link KeyVaultKey key} release.
     *
     * @return A {@link Mono} containing the {@link ReleaseKeyResult} containing the released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReleaseKeyResult> releaseKey(String name, String target) {
        try {
            return releaseKeyWithResponse(name, "", target, new ReleaseKeyOptions())
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Releases a key.
     *
     * <p>The key must be exportable. This operation requires the 'keys/release' permission.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Subscribes to the call asynchronously and prints out the signed object
     * that contains the {@link KeyVaultKey released key} when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.releaseKey#String-String-String}
     *
     * @param name The name of the {@link KeyVaultKey key} to release.
     * @param version The version of the key to retrieve. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyAsyncClient#releaseKey(String, String)}, with the latest key version being released.
     * @param target The attestation assertion for the target of the key release.
     *
     * @return A {@link Mono} containing the {@link ReleaseKeyResult} containing the released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReleaseKeyResult> releaseKey(String name, String version, String target) {
        try {
            return releaseKeyWithResponse(name, version, target, new ReleaseKeyOptions())
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Releases a key.
     *
     * <p>The key must be exportable. This operation requires the 'keys/release' permission.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Subscribes to the call asynchronously and prints out the
     * {@link Response HTTP Response} details and the signed object that contains the {@link KeyVaultKey released key}
     * when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions}
     *
     * @param name The name of the key to release.
     * @param version The version of the key to retrieve. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyAsyncClient#releaseKey(String, String)}, with the latest key version being released.
     * @param target The attestation assertion for the target of the key release.
     * @param options Additional {@link ReleaseKeyOptions options} for releasing a {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} for this operation and the
     * {@link ReleaseKeyResult} containing the released key.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReleaseKeyResult>> releaseKeyWithResponse(String name, String version, String target,
                                                                   ReleaseKeyOptions options) {
        try {
            return withContext(context -> releaseKeyWithResponse(name, version, target, options, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<Response<ReleaseKeyResult>> releaseKeyWithResponse(String name, String version, String target,
                                                            ReleaseKeyOptions options, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                return monoError(logger, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            if (CoreUtils.isNullOrEmpty(target)) {
                return monoError(logger, new IllegalArgumentException("'target' cannot be null or empty"));
            }

            options = options == null ? new ReleaseKeyOptions() : options;

            KeyReleaseParameters keyReleaseParameters = new KeyReleaseParameters()
                .setTarget(target)
                .setAlgorithm(options.getAlgorithm())
                .setNonce(options.getNonce());

            return service.release(vaultUrl, name, version, apiVersion, keyReleaseParameters, "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Releasing key with name %s and version %s.", name, version))
                .doOnSuccess(response -> logger.verbose("Released key with name %s and version %s.", name, version))
                .doOnError(error -> logger.warning("Failed to release key - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Rotates a {@link KeyVaultKey key}. The rotate key operation will do so based on
     * {@link KeyRotationPolicy key's rotation policy}. This operation requires the {@code keys/rotate} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Rotates a {@link KeyVaultKey key}. Prints out {@link KeyVaultKey rotated key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.rotateKey#String}
     *
     * @param name The name of {@link KeyVaultKey key} to be rotated. The system will generate a new version in the
     * specified {@link KeyVaultKey key}.
     *
     * @return The new version of the rotated {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> rotateKey(String name) {
        try {
            return rotateKeyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Rotates a {@link KeyVaultKey key}. The rotate key operation will do so based on
     * {@link KeyRotationPolicy key's rotation policy}. This operation requires the {@code keys/rotate} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Rotates a {@link KeyVaultKey key}. Subscribes to the call asynchronously and prints out the
     * {@link Response HTTP Response} and {@link KeyVaultKey rotated key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.rotateKeyWithResponse#String}
     *
     * @param name The name of {@link KeyVaultKey key} to be rotated. The system will generate a new version in the
     * specified {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} for this operation and the new version of
     * the rotated {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> rotateKeyWithResponse(String name) {
        try {
            return withContext(context -> rotateKeyWithResponse(name, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<Response<KeyVaultKey>> rotateKeyWithResponse(String name, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                return monoError(logger, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            return service.rotateKey(vaultUrl, name, apiVersion, "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Rotating key with name %s.", name))
                .doOnSuccess(response -> logger.verbose("Rotated key with name %s.", name))
                .doOnError(error -> logger.warning("Failed to rotate key - {}", error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the {@link KeyRotationPolicy} for the {@link KeyVaultKey key} with the provided name. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Subscribes to the
     * call asynchronously and prints out the {@link KeyRotationPolicy rotation policy key} details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicy#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link KeyRotationPolicy} for the key.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyRotationPolicy> getKeyRotationPolicy(String name) {
        try {
            return getKeyRotationPolicyWithResponse(name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets the {@link KeyRotationPolicy} for the {@link KeyVaultKey key} with the provided name. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Subscribes to the
     * call asynchronously and prints out the {@link Response HTTP Response} and
     * {@link KeyRotationPolicy rotation policy key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.getKeyRotationPolicyWithResponse#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} for this operation and the
     * {@link KeyRotationPolicy} for the key.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyRotationPolicy>> getKeyRotationPolicyWithResponse(String name) {
        try {
            return withContext(context -> getKeyRotationPolicyWithResponse(name, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<Response<KeyRotationPolicy>> getKeyRotationPolicyWithResponse(String name, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                return monoError(logger, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            return service.getKeyRotationPolicy(vaultUrl, name, apiVersion, "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Retrieving key rotation policy for key with name.", name))
                .doOnSuccess(response -> logger.verbose("Retrieved key rotation policy for key with name.", name))
                .doOnError(error -> logger.warning("Failed to retrieve key rotation policy - {}", error))
                .map(response -> new SimpleResponse<>(response, toKeyRotationPolicy(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Updates the {@link KeyRotationPolicy} of the key with the provided name. This operation requires the
     * {@code keys/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Subscribes to the
     * call asynchronously and prints out the {@link KeyRotationPolicy rotation policy key} details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param keyRotationPolicyProperties The {@link KeyRotationPolicyProperties} for the key.
     *
     * @return A {@link Mono} containing the {@link KeyRotationPolicy} for the key.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyRotationPolicy> updateKeyRotationPolicy(String name, KeyRotationPolicyProperties keyRotationPolicyProperties) {
        try {
            return updateKeyRotationPolicyWithResponse(name, keyRotationPolicyProperties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Updates the {@link KeyRotationPolicy} of the key with the provided name. This operation requires the
     * {@code keys/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Subscribes to the
     * call asynchronously and prints out the {@link Response HTTP Response} and
     * {@link KeyRotationPolicy rotation policy key} details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyAsyncClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param keyRotationPolicyProperties The {@link KeyRotationPolicyProperties} for the key.
     *
     * @return A {@link Mono} containing the {@link Response HTTP response} for this operation and the
     * {@link KeyRotationPolicy} for the key.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicyWithResponse(String name, KeyRotationPolicyProperties keyRotationPolicyProperties) {
        try {
            return withContext(context ->
                updateKeyRotationPolicyWithResponse(name, keyRotationPolicyProperties, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<Response<KeyRotationPolicy>> updateKeyRotationPolicyWithResponse(String name, KeyRotationPolicyProperties keyRotationPolicyProperties, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(name)) {
                return monoError(logger, new IllegalArgumentException("'name' cannot be null or empty"));
            }

            List<LifetimeAction> lifetimeActions = new ArrayList<>();

            for (KeyRotationLifetimeAction lifetimeAction : keyRotationPolicyProperties.getLifetimeActions()) {
                lifetimeActions.add(new LifetimeAction()
                    .setAction(new LifetimeActionsType()
                        .setType(lifetimeAction.getType()))
                    .setTrigger(new LifetimeActionTrigger()
                        .setTimeAfterCreate(lifetimeAction.getTimeAfterCreate())
                        .setTimeBeforeExpiry(lifetimeAction.getTimeBeforeExpiry())));
            }

            com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy keyRotationPolicy =
                new com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy()
                    .setAttributes(new KeyRotationPolicyAttributes()
                        .setExpiryTime(keyRotationPolicyProperties.getExpiryTime()))
                    .setLifetimeActions(lifetimeActions);

            return service.updateKeyRotationPolicy(vaultUrl, name, apiVersion, keyRotationPolicy, "application/json",
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Updating key rotation policy for key with name.", name))
                .doOnSuccess(response -> logger.verbose("Updated key rotation policy for key with name.", name))
                .doOnError(error -> logger.warning("Failed to retrieve key rotation policy - {}", error))
                .map(response -> new SimpleResponse<>(response, toKeyRotationPolicy(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    private KeyRotationPolicy toKeyRotationPolicy(com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicy keyRotationPolicy) {
        if (keyRotationPolicy == null) {
            return null;
        }

        List<KeyRotationLifetimeAction> keyRotationLifetimeActions = null;

        if (keyRotationPolicy.getLifetimeActions() != null) {
            keyRotationLifetimeActions = new ArrayList<>();

            for (LifetimeAction lifetimeAction : keyRotationPolicy.getLifetimeActions()) {
                keyRotationLifetimeActions.add(new KeyRotationLifetimeAction(lifetimeAction.getAction().getType())
                    .setTimeBeforeExpiry(lifetimeAction.getTrigger().getTimeBeforeExpiry())
                    .setTimeAfterCreate(lifetimeAction.getTrigger().getTimeAfterCreate()));
            }
        }

        OffsetDateTime createdOn = null;
        OffsetDateTime updatedOn = null;
        String expiryTime = null;

        if (keyRotationPolicy.getAttributes() != null) {
            createdOn = OffsetDateTime.of(LocalDateTime.ofEpochSecond(keyRotationPolicy.getAttributes().getCreatedOn(),
                0, ZoneOffset.UTC), ZoneOffset.UTC);
            updatedOn = OffsetDateTime.of(LocalDateTime.ofEpochSecond(keyRotationPolicy.getAttributes().getUpdatedOn(),
                0, ZoneOffset.UTC), ZoneOffset.UTC);
            expiryTime = keyRotationPolicy.getAttributes().getExpiryTime();
        }

        return new KeyRotationPolicy(keyRotationPolicy.getId(), createdOn, updatedOn)
            .setExpiryTime(expiryTime)
            .setLifetimeActions(keyRotationLifetimeActions);
    }
}


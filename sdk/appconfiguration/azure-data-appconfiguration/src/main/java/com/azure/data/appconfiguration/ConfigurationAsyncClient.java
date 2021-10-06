// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonDeserializer;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, deleting, set read-only
 * status ConfigurationSettings, and listing settings or revision of a setting based on a
 * {@link SettingSelector filter}.
 *
 * <p><strong>Instantiating an asynchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.async.configurationclient.instantiation}
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ConfigurationClientBuilder
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true,
    serviceInterfaces = ConfigurationService.class)
public final class ConfigurationAsyncClient {
    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClient.class);
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";
    private static final String ETAG_ANY = "*";

    private static final SerializerAdapter SERIALIZER_ADAPTER;

    private final String serviceEndpoint;
    private final ConfigurationService service;
    private final String apiVersion;
    private final SyncTokenPolicy syncTokenPolicy;

    static {
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(ConfigurationSettingJsonSerializer.getModule());
        jacksonAdapter.serializer().registerModule(ConfigurationSettingJsonDeserializer.getModule());

        SERIALIZER_ADAPTER = jacksonAdapter;
    }

    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     * @param serviceEndpoint The URL string for the App Configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ConfigurationServiceVersion} of the service to be used when making requests.
     * @param syncTokenPolicy {@link SyncTokenPolicy} to be used to update the external synchronization token to ensure
     *  service requests receive up-to-date values.
     */
    ConfigurationAsyncClient(String serviceEndpoint, HttpPipeline pipeline, ConfigurationServiceVersion version,
        SyncTokenPolicy syncTokenPolicy) {

        this.service = RestProxy.create(ConfigurationService.class, pipeline, SERIALIZER_ADAPTER);
        this.serviceEndpoint = serviceEndpoint;
        this.apiVersion = version.getVersion();
        this.syncTokenPolicy = syncTokenPolicy;
    }

    /**
     * Adds a configuration value in the service if that key does not exist. The {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS" and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string}
     *
     * @param key The key of the configuration setting to add.
     * @param label The label of the configuration setting to add. If {@code null} no label will be used.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addConfigurationSetting(String key, String label, String value) {
        try {
            return withContext(context -> addConfigurationSetting(
                new ConfigurationSetting().setKey(key).setLabel(label).setValue(value), context))
                .flatMap(response -> Mono.justOrEmpty(response.getValue()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting}
     *
     * @param setting The setting to add based on its key and optional label combination.
     *
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addConfigurationSetting(ConfigurationSetting setting) {
        return addConfigurationSettingWithResponse(setting).map(response -> response.getValue());
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The setting to add based on its key and optional label combination.
     * @return A REST response containing the {@link ConfigurationSetting} that was created, if a key collision occurs
     * or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> addConfigurationSettingWithResponse(ConfigurationSetting setting) {
        try {
            return withContext(context -> addConfigurationSetting(setting, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ConfigurationSetting>> addConfigurationSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
        // If the service finds any existing configuration settings, then its e-tag will match and the service will
        // return an error.
        return service.setKey(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, setting, null,
            getETagValue(ETAG_ANY), context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .onErrorResume(HttpResponseException.class,
                (Function<Throwable, Mono<Response<ConfigurationSetting>>>) throwable -> {
                    final HttpResponseException e = (HttpResponseException) throwable;
                    final HttpResponse httpResponse = e.getResponse();
                    if (httpResponse.getStatusCode() == 412) {
                        return Mono.error(new ResourceExistsException("Setting was already present.", httpResponse,
                            throwable));
                    }

                    return Mono.error(throwable);
                })
            .doOnSubscribe(ignoredValue -> logger.info("Adding ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Added ConfigurationSetting - {}", response.getValue()))
            .onErrorMap(ConfigurationAsyncClient::addConfigurationSettingExceptionMapper)
            .doOnError(error -> logger.warning("Failed to add ConfigurationSetting - {}", setting, error));
    }

    /**
     * Creates or updates a configuration value in the service with the given key. the {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection"</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string}
     *
     * @param key The key of the configuration setting to create or update.
     * @param label The label of the configuration setting to create or update, If {@code null} no label will be used.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or an empty Mono if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setConfigurationSetting(String key, String label, String value) {
        try {
            return withContext(context -> setConfigurationSetting(
                new ConfigurationSetting().setKey(key).setLabel(label).setValue(value), false, context))
                .flatMap(response -> Mono.justOrEmpty(response.getValue()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection"</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting}
     *
     * @param setting The setting to add based on its key and optional label combination.
     *
     * @return The {@link ConfigurationSetting} that was created or updated, or an empty Mono if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setConfigurationSetting(ConfigurationSetting setting) {
        return setConfigurationSettingWithResponse(setting, false).map(response -> response.getValue());
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * If {@link ConfigurationSetting#getETag() ETag} is specified, the configuration value is updated if the current
     * setting's ETag matches. If the ETag's value is equal to the wildcard character ({@code "*"}), the setting will
     * always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean}
     *
     * @param setting The setting to create or update based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @return A REST response containing the {@link ConfigurationSetting} that was created or updated, if the key is an
     * invalid value, the setting is read-only, or an ETag was provided but does not match the service's current ETag
     * value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#getETag() ETag} was specified, is not the
     * wildcard character, and the current configuration value's ETag does not match, or the setting exists and is
     * read-only.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged) {
        try {
            return withContext(context -> setConfigurationSetting(setting, ifUnchanged, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ConfigurationSetting>> setConfigurationSetting(ConfigurationSetting setting, boolean ifUnchanged,
        Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        // This service method call is similar to addConfigurationSetting except it will create or update a
        // configuration setting.
        // If the user provides an ETag value, it is passed in as If-Match = "{ETag value}". If the current value in the
        // service has a matching ETag then it matches, then its value is updated with what the user passed in.
        // Otherwise, the service throws an exception because the current configuration value was updated and we have an
        // old value locally.
        // If no ETag value was passed in, then the value is always added or updated.
        return service.setKey(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, setting, ifMatchETag,
            null, context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                   .doOnSubscribe(ignoredValue -> logger.info("Setting ConfigurationSetting - {}", setting))
                   .doOnSuccess(response -> logger.info("Set ConfigurationSetting - {}", response.getValue()))
                   .doOnError(error -> logger.warning("Failed to set ConfigurationSetting - {}", setting, error));
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, and the optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string}

     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve. If {@code null} no label will be used.
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label) {
        try {
            return getConfigurationSetting(key, label, null);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, the optional {@code label}, and the optional
     * {@code acceptDateTime} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection" and a time that one minute before now at UTC-Zone</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime}
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve. If {@code null} no label will be used.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label, OffsetDateTime acceptDateTime) {
        try {
            return withContext(context -> getConfigurationSetting(
                new ConfigurationSetting().setKey(key).setLabel(label), acceptDateTime, false, context))
                .flatMap(response -> Mono.justOrEmpty(response.getValue()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection" and a time that one minute before now at UTC-Zone</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting}
     *
     * @param setting The setting to retrieve.
     *
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(ConfigurationSetting setting) {
        return getConfigurationSettingWithResponse(setting, null, false).map(response -> response.getValue());
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean}
     *
     * @param setting The setting to retrieve.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * If-None-Match header.
     * @return A REST response containing the {@link ConfigurationSetting} stored in the service, or {@code null} if
     * didn't exist. {@code null} is also returned if the configuration value does not exist or the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponse(ConfigurationSetting setting,
        OffsetDateTime acceptDateTime, boolean ifChanged) {
        try {
            return withContext(context -> getConfigurationSetting(setting, acceptDateTime, ifChanged, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ConfigurationSetting>> getConfigurationSetting(ConfigurationSetting setting,
        OffsetDateTime acceptDateTime, boolean onlyIfChanged, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifNoneMatchETag = onlyIfChanged ? getETagValue(setting.getETag()) : null;
        return service.getKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, null,
            acceptDateTime == null ? null : acceptDateTime.toString(), null, ifNoneMatchETag,
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .onErrorResume(HttpResponseException.class,
                (Function<Throwable, Mono<Response<ConfigurationSetting>>>) throwable -> {
                    final HttpResponseException e = (HttpResponseException) throwable;
                    final HttpResponse httpResponse = e.getResponse();
                    if (httpResponse.getStatusCode() == 304) {
                        return Mono.just(new ResponseBase<Void, ConfigurationSetting>(httpResponse.getRequest(),
                            httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null));
                    } else if (httpResponse.getStatusCode() == 404) {
                        return Mono.error(new ResourceNotFoundException("Setting not found.", httpResponse, throwable));
                    }

                    return Mono.error(throwable);
                })
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Retrieved ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to get ConfigurationSetting - {}", setting, error));
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key} and optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string}
     *
     * @param key The key of configuration setting to delete.
     * @param label The label of configuration setting to delete. If {@code null} no label will be used.
     * @return The deleted ConfigurationSetting or an empty Mono is also returned if the {@code key} is an invalid value
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteConfigurationSetting(String key, String label) {
        try {
            return withContext(context -> deleteConfigurationSetting(
                new ConfigurationSetting().setKey(key).setLabel(label), false, context))
                .flatMap(response -> Mono.justOrEmpty(response.getValue()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination from the service.
     *
     * If {@link ConfigurationSetting#getETag() ETag} is specified and is not the wildcard character ({@code "*"}), then
     * the setting is <b>only</b> deleted if the ETag matches the current ETag; this means that no one has updated the
     * ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting}
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     *
     * @return The deleted ConfigurationSetting or an empty Mono is also returned if the {@code key} is an invalid value
     * (which will also throw HttpResponseException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteConfigurationSetting(ConfigurationSetting setting) {
        return deleteConfigurationSettingWithResponse(setting, false).map(response -> response.getValue());
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination from the service.
     *
     * If {@link ConfigurationSetting#getETag() ETag} is specified and is not the wildcard character ({@code "*"}), then
     * the setting is <b>only</b> deleted if the ETag matches the current ETag; this means that no one has updated the
     * ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key-label "prodDBConnection"-"westUS"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean}
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value or
     * {@link ConfigurationSetting#getETag() ETag} is set but does not match the current ETag
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> deleteConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged) {
        try {
            return withContext(context -> deleteConfigurationSetting(setting, ifUnchanged, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ConfigurationSetting>> deleteConfigurationSetting(ConfigurationSetting setting, boolean ifUnchanged,
        Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;

        final String ifMatchETag = ifUnchanged ? getETagValue(setting.getETag()) : null;
        return service.delete(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, ifMatchETag,
            null, context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Deleting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Deleted ConfigurationSetting - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to delete ConfigurationSetting - {}", setting, error));
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting} that matches the {@code key}, the optional
     * {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean}
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly}
     *
     * @param key The key of configuration setting to set to be read-only.
     * @param label The label of configuration setting to read-only. If {@code null} no label will be used.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the
     * key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(String key, String label, boolean isReadOnly) {
        try {
            return withContext(context -> setReadOnly(
                new ConfigurationSetting().setKey(key).setLabel(label), isReadOnly, context))
                .flatMap(response -> Mono.justOrEmpty(response.getValue()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean}
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly}
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     *
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the
     * key is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(ConfigurationSetting setting, boolean isReadOnly) {
        return setReadOnlyWithResponse(setting, isReadOnly).map(response -> response.getValue());
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean}
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly}
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @return A REST response containing the read-only or not read-only ConfigurationSetting if {@code isReadOnly}
     * is true or null, or false respectively. Or return {@code null} if the setting didn't exist.
     * {@code null} is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value.
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setReadOnlyWithResponse(ConfigurationSetting setting,
        boolean isReadOnly) {
        try {
            return withContext(context -> setReadOnly(setting, isReadOnly, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ConfigurationSetting>> setReadOnly(ConfigurationSetting setting, boolean isReadOnly,
        Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        context = context == null ? Context.NONE : context;
        if (isReadOnly) {
            return service.lockKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, null,
                null, context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.verbose("Setting read only ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.info("Set read only ConfigurationSetting - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to set read only ConfigurationSetting - {}", setting,
                    error));
        } else {
            return service.unlockKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion,
                null, null, context)
                .doOnSubscribe(ignoredValue -> logger.verbose("Clearing read only ConfigurationSetting - {}", setting))
                .doOnSuccess(
                    response -> logger.info("Cleared read only ConfigurationSetting - {}", response.getValue()))
                .doOnError(
                    error -> logger.warning("Failed to clear read only ConfigurationSetting - {}", setting, error));
        }
    }

    /**
     * Fetches the configuration settings that match the {@code selector}. If {@code selector} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.listsettings}
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code selector}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listConfigurationSettings(SettingSelector selector) {
        try {
            return new PagedFlux<>(() -> withContext(context -> listFirstPageSettings(selector, context)),
                continuationToken -> withContext(context -> listNextPageSettings(context, continuationToken)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<ConfigurationSetting> listConfigurationSettings(SettingSelector selector, Context context) {
        return new PagedFlux<>(() -> listFirstPageSettings(selector, context),
            continuationToken -> listNextPageSettings(context, continuationToken));
    }

    private Mono<PagedResponse<ConfigurationSetting>> listNextPageSettings(Context context, String continuationToken) {
        try {
            if (continuationToken == null || continuationToken.isEmpty()) {
                return Mono.empty();
            }

            return service.listKeyValues(serviceEndpoint, continuationToken,
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(
                    ignoredValue -> logger.info("Retrieving the next listing page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", continuationToken))
                .doOnError(
                    error -> logger.warning("Failed to retrieve the next listing page - Page {}", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<PagedResponse<ConfigurationSetting>> listFirstPageSettings(SettingSelector selector, Context context) {
        try {
            if (selector == null) {
                return service.listKeyValues(serviceEndpoint, null, null, apiVersion, null, null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.info("Listing all ConfigurationSettings"))
                    .doOnSuccess(response -> logger.info("Listed all ConfigurationSettings"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting", error));
            }

            final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
            final String keyFilter = selector.getKeyFilter();
            final String labelFilter = selector.getLabelFilter();

            return service.listKeyValues(serviceEndpoint, keyFilter, labelFilter, apiVersion, fields,
                selector.getAcceptDateTime(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.info("Listing ConfigurationSettings - {}", selector))
                .doOnSuccess(response -> logger.info("Listed ConfigurationSettings - {}", selector))
                .doOnError(error -> logger.warning("Failed to list ConfigurationSetting - {}", selector, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date.
     * Revisions expire after a period of time, see <a href="https://azure.microsoft.com/pricing/details/app-configuration/">Pricing</a>
     * for more information.
     *
     * If {@code selector} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code selector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions}
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listRevisions(SettingSelector selector) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listRevisionsFirstPage(selector, context)),
                continuationToken -> withContext(context -> listRevisionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    Mono<PagedResponse<ConfigurationSetting>> listRevisionsFirstPage(SettingSelector selector, Context context) {
        try {
            Mono<PagedResponse<ConfigurationSetting>> result;

            if (selector != null) {
                final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
                final String keyFilter = selector.getKeyFilter();
                final String labelFilter = selector.getLabelFilter();

                result = service.listKeyValueRevisions(serviceEndpoint, keyFilter, labelFilter, apiVersion, fields,
                    selector.getAcceptDateTime(), null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.info("Listing ConfigurationSetting revisions - {}", selector))
                    .doOnSuccess(response -> logger.info("Listed ConfigurationSetting revisions - {}", selector))
                    .doOnError(error ->
                        logger.warning("Failed to list ConfigurationSetting revisions - {}", selector, error));
            } else {
                result = service.listKeyValueRevisions(
                    serviceEndpoint, null, null, apiVersion, null, null, null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                    .doOnRequest(ignoredValue -> logger.info("Listing ConfigurationSetting revisions"))
                    .doOnSuccess(response -> logger.info("Listed ConfigurationSetting revisions"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting revisions", error));
            }

            return result;
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<PagedResponse<ConfigurationSetting>> listRevisionsNextPage(String nextPageLink, Context context) {
        try {
            Mono<PagedResponse<ConfigurationSetting>> result = service
                .listKeyValues(serviceEndpoint, nextPageLink,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
                .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
                .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                    error));
            return result;
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    PagedFlux<ConfigurationSetting> listRevisions(SettingSelector selector, Context context) {
        return new PagedFlux<>(() ->
            listRevisionsFirstPage(selector, context),
            continuationToken -> listRevisionsNextPage(continuationToken, context));
    }

    private Flux<ConfigurationSetting> listConfigurationSettings(String nextPageLink, Context context) {
        Mono<PagedResponse<ConfigurationSetting>> result = service.listKeyValues(serviceEndpoint, nextPageLink,
            context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error));

        return result.flatMapMany(r -> extractAndFetchConfigurationSettings(r, context));
    }

    /**
     * Adds an external synchronization token to ensure service requests receive up-to-date values.
     *
     * @param token an external synchronization token to ensure service requests receive up-to-date values.
     * @throws NullPointerException if the given token is null.
     */
    public void updateSyncToken(String token) {
        Objects.requireNonNull(token, "'token' cannot be null.");
        syncTokenPolicy.updateSyncToken(token);
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(
        PagedResponse<ConfigurationSetting> page, Context context) {
        return CoreUtils.extractAndFetch(page, context, this::listConfigurationSettings);
    }

    /*
     * Azure Configuration service requires that the ETag value is surrounded in quotation marks.
     *
     * @param ETag The ETag to get the value for. If null is pass in, an empty string is returned.
     * @return The ETag surrounded by quotations. (ex. "ETag")
     */
    private static String getETagValue(String etag) {
        return (etag == null || etag.equals("*")) ? etag : "\"" + etag + "\"";
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    private static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.getKey() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    /*
     * Remaps the exception returned from the service if it is a PRECONDITION_FAILED response. This is performed since
     * add setting returns PRECONDITION_FAILED when the configuration already exists, all other uses of setKey return
     * this status when the configuration doesn't exist.
     *
     * @param throwable Error response from the service.
     *
     * @return Exception remapped to a ResourceModifiedException if the throwable was a ResourceNotFoundException,
     * otherwise the throwable is returned unmodified.
     */
    private static Throwable addConfigurationSettingExceptionMapper(Throwable throwable) {
        if (!(throwable instanceof ResourceNotFoundException)) {
            return throwable;
        }

        ResourceNotFoundException notFoundException = (ResourceNotFoundException) throwable;
        return new ResourceModifiedException(notFoundException.getMessage(), notFoundException.getResponse());
    }
}

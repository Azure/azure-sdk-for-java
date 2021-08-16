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
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonDeserializer;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.CoreUtils.addTelemetryValue;
import static com.azure.core.util.CoreUtils.createTelemetryValue;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, deleting, set read-only
 * status ConfigurationSettings, and listing settings or revision of a setting based on a {@link SettingSelector
 * filter}.
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
    // See https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers
    // for more information on Azure resource provider namespaces.
    private static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";
    private static final String CLASS_NAME = ConfigurationAsyncClient.class.getSimpleName();

    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClient.class);

    private static final String ETAG_ANY = "*";
    private final String serviceEndpoint;
    private final ConfigurationService service;
    private final String apiVersion;
    private final SyncTokenPolicy syncTokenPolicy;

    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceEndpoint The URL string for the App Configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link ConfigurationServiceVersion} of the service to be used when making requests.
     * @param syncTokenPolicy {@link SyncTokenPolicy} to be used to update the external synchronization token to ensure
     * service requests receive up-to-date values.
     */
    ConfigurationAsyncClient(String serviceEndpoint, HttpPipeline pipeline, ConfigurationServiceVersion version,
        SyncTokenPolicy syncTokenPolicy) {

        final JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(ConfigurationSettingJsonSerializer.getModule());
        jacksonAdapter.serializer().registerModule(ConfigurationSettingJsonDeserializer.getModule());

        this.service = RestProxy.create(ConfigurationService.class, pipeline, jacksonAdapter);
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
        return addConfigurationSettingWithResponse(createConfiguration(key, label, value)).flatMap(FluxUtil::toMono);
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
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addConfigurationSetting(ConfigurationSetting setting) {
        return addConfigurationSettingWithResponse(setting).map(Response::getValue);
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
        return withContext(context -> addConfigurationSetting(setting, context, CLASS_NAME, true));
    }

    Mono<Response<ConfigurationSetting>> addConfigurationSetting(ConfigurationSetting setting, Context context,
        String className, boolean isAsync) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        return validateSetting(setting, logger)
            // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
            // If the service finds any existing configuration settings, then its e-tag will match and the service will
            // return an error.
            .then(apiCallWithTelemetry(ctx -> service.setKey(serviceEndpoint, setting.getKey(), setting.getLabel(),
                    apiVersion, setting, null, getETagValue(ETAG_ANY), ctx), context, className, "addConfigurationSetting",
                isAsync, logger)
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
                .doOnSubscribe(ignoredValue -> logger.verbose("Adding ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.verbose("Added ConfigurationSetting - {}", response.getValue()))
                .onErrorMap(ConfigurationAsyncClient::addConfigurationSettingExceptionMapper)
                .doOnError(error -> logger.warning("Failed to add ConfigurationSetting - {}", setting, error)));
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
        return setConfigurationSettingWithResponse(createConfiguration(key, label, value), false)
            .flatMap(FluxUtil::toMono);
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
     * @return The {@link ConfigurationSetting} that was created or updated, or an empty Mono if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setConfigurationSetting(ConfigurationSetting setting) {
        return setConfigurationSettingWithResponse(setting, false).map(Response::getValue);
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
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
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
        return withContext(context -> setConfigurationSetting(setting, ifUnchanged, context, CLASS_NAME, true));
    }

    Mono<Response<ConfigurationSetting>> setConfigurationSetting(ConfigurationSetting setting, boolean ifUnchanged,
        Context context, String className, boolean isAsync) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        return validateSetting(setting, logger)
            // This service method call is similar to addConfigurationSetting except it will create or update a
            // configuration setting.
            // If the user provides an ETag value, it is passed in as If-Match = "{ETag value}". If the current value in
            // the service has a matching ETag then it matches, then its value is updated with what the user passed in.
            // Otherwise, the service throws an exception because the current configuration value was updated, and we
            // have an old value locally.
            // If no ETag value was passed in, then the value is always added or updated.
            .then(apiCallWithTelemetry(ctx -> service.setKey(serviceEndpoint, setting.getKey(), setting.getLabel(),
                    apiVersion, setting, ifUnchanged ? getETagValue(setting.getETag()) : null, null, ctx),
                context, className, "setConfigurationSetting", isAsync, logger)
                .doOnSubscribe(ignoredValue -> logger.verbose("Setting ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.verbose("Set ConfigurationSetting - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to set ConfigurationSetting - {}", setting, error)));
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, and the optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string}
     *
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
        return getConfigurationSetting(key, label, null);
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
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null} then the
     * current state of the configuration setting will be returned.
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label, OffsetDateTime acceptDateTime) {
        return getConfigurationSettingWithResponse(createConfiguration(key, label, null), acceptDateTime, false)
            .flatMap(FluxUtil::toMono);
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
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(ConfigurationSetting setting) {
        return getConfigurationSettingWithResponse(setting, null, false).map(Response::getValue);
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
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null} then the
     * current state of the configuration setting will be returned.
     * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
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
        return withContext(context -> getConfigurationSetting(setting, acceptDateTime, ifChanged, context,
            CLASS_NAME, true));
    }

    Mono<Response<ConfigurationSetting>> getConfigurationSetting(ConfigurationSetting setting,
        OffsetDateTime acceptDateTime, boolean onlyIfChanged, Context context, String className, boolean isAsync) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        return validateSetting(setting, logger)
            .then(apiCallWithTelemetry(ctx -> service.getKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(),
                    apiVersion, null, acceptDateTime == null ? null : acceptDateTime.toString(), null,
                    onlyIfChanged ? getETagValue(setting.getETag()) : null, ctx), context, className,
                "getConfigurationSetting", isAsync, logger)
                .onErrorResume(HttpResponseException.class,
                    (Function<Throwable, Mono<Response<ConfigurationSetting>>>) throwable -> {
                        final HttpResponseException e = (HttpResponseException) throwable;
                        final HttpResponse httpResponse = e.getResponse();
                        if (httpResponse.getStatusCode() == 304) {
                            return Mono.just(new ResponseBase<Void, ConfigurationSetting>(httpResponse.getRequest(),
                                httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null));
                        } else if (httpResponse.getStatusCode() == 404) {
                            return Mono.error(new ResourceNotFoundException("Setting not found.", httpResponse,
                                throwable));
                        }

                        return Mono.error(throwable);
                    })
                .doOnSubscribe(ignoredValue -> logger.verbose("Retrieving ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.verbose("Retrieved ConfigurationSetting - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to get ConfigurationSetting - {}", setting, error)));
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
        return deleteConfigurationSettingWithResponse(createConfiguration(key, label, null), false)
            .flatMap(FluxUtil::toMono);
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
     * @return The deleted ConfigurationSetting or an empty Mono is also returned if the {@code key} is an invalid value
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteConfigurationSetting(ConfigurationSetting setting) {
        return deleteConfigurationSettingWithResponse(setting, false).map(Response::getValue);
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
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
     * IF-MATCH header.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value or {@link
     * ConfigurationSetting#getETag() ETag} is set but does not match the current ETag (which will also throw
     * HttpResponseException described below).
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
        return withContext(context -> deleteConfigurationSetting(setting, ifUnchanged, context, CLASS_NAME, true));
    }

    Mono<Response<ConfigurationSetting>> deleteConfigurationSetting(ConfigurationSetting setting, boolean ifUnchanged,
        Context context, String className, boolean isAsync) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        return validateSetting(setting, logger)
            .then(apiCallWithTelemetry(ctx -> service.delete(serviceEndpoint, setting.getKey(), setting.getLabel(),
                    apiVersion, ifUnchanged ? getETagValue(setting.getETag()) : null, null, ctx), context, className,
                "deleteConfigurationSetting", isAsync, logger)
                .doOnSubscribe(ignoredValue -> logger.verbose("Deleting ConfigurationSetting - {}", setting))
                .doOnSuccess(response -> logger.verbose("Deleted ConfigurationSetting - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to delete ConfigurationSetting - {}", setting, error)));
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting} that matches the {@code key}, the optional {@code
     * label}.
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
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(String key, String label, boolean isReadOnly) {
        return setReadOnlyWithResponse(createConfiguration(key, label, null), isReadOnly).flatMap(FluxUtil::toMono);
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
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(ConfigurationSetting setting, boolean isReadOnly) {
        return setReadOnlyWithResponse(setting, isReadOnly).map(Response::getValue);
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
     * @return A REST response containing the read-only or not read-only ConfigurationSetting if {@code isReadOnly} is
     * true or null, or false respectively. Or return {@code null} if the setting didn't exist. {@code null} is also
     * returned if the {@link ConfigurationSetting#getKey() key} is an invalid value. (which will also throw
     * HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setReadOnlyWithResponse(ConfigurationSetting setting,
        boolean isReadOnly) {
        return withContext(context -> setReadOnly(setting, isReadOnly, context, CLASS_NAME, true));
    }

    Mono<Response<ConfigurationSetting>> setReadOnly(ConfigurationSetting setting, boolean isReadOnly,
        Context context, String className, boolean isAsync) {
        // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
        return validateSetting(setting, logger)
            .then(apiCallWithTelemetry(ctx -> {
                if (isReadOnly) {
                    return service.lockKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion, null,
                            null, ctx)
                        .doOnSubscribe(ignoredValue -> logger.verbose("Setting read only ConfigurationSetting - {}",
                            setting))
                        .doOnSuccess(response -> logger.verbose("Set read only ConfigurationSetting - {}",
                            response.getValue()))
                        .doOnError(error -> logger.warning("Failed to set read only ConfigurationSetting - {}", setting,
                            error));
                } else {
                    return service.unlockKeyValue(serviceEndpoint, setting.getKey(), setting.getLabel(), apiVersion,
                            null, null, ctx)
                        .doOnSubscribe(ignoredValue -> logger.verbose("Clearing read only ConfigurationSetting - {}",
                            setting))
                        .doOnSuccess(response -> logger.verbose("Cleared read only ConfigurationSetting - {}",
                            response.getValue()))
                        .doOnError(error -> logger.warning("Failed to clear read only ConfigurationSetting - {}",
                            setting, error));
                }
            }, context, className, "setReadOnly", isAsync, logger));
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
     * contains all current settings in the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listConfigurationSettings(SettingSelector selector) {
        return new PagedFlux<>(() -> withContext(context -> listFirstPageSettings(selector, context, CLASS_NAME, true)),
            continuationToken -> withContext(context -> listNextPageSettings(context, continuationToken, CLASS_NAME,
                true)));
    }

    PagedFlux<ConfigurationSetting> listConfigurationSettings(SettingSelector selector, Context context,
        String className, boolean isAsync) {
        return new PagedFlux<>(() -> listFirstPageSettings(selector, context, className, isAsync),
            continuationToken -> listNextPageSettings(context, continuationToken, className, isAsync));
    }

    private Mono<PagedResponse<ConfigurationSetting>> listNextPageSettings(Context context, String continuationToken,
        String className, boolean isAsync) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        return apiCallWithTelemetry(ctx -> service.listKeyValues(serviceEndpoint, continuationToken, ctx)
            .doOnSubscribe(ignoredValue -> logger.verbose("Retrieving the next listing page - Page {}", continuationToken))
            .doOnSuccess(response -> logger.verbose("Retrieved the next listing page - Page {}", continuationToken))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", continuationToken,
                error)), context, className, "listConfigurationSettings", isAsync, logger);
    }

    private Mono<PagedResponse<ConfigurationSetting>> listFirstPageSettings(SettingSelector selector, Context context,
        String className, boolean isAsync) {
        return apiCallWithTelemetry(ctx -> {
            if (selector == null) {
                return service.listKeyValues(serviceEndpoint, null, null, apiVersion, null, null,
                        addTracingNamespace(context))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing all ConfigurationSettings"))
                    .doOnSuccess(response -> logger.verbose("Listed all ConfigurationSettings"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting", error));
            }

            final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
            final String keyFilter = selector.getKeyFilter();
            final String labelFilter = selector.getLabelFilter();

            return service.listKeyValues(serviceEndpoint, keyFilter, labelFilter, apiVersion, fields,
                    selector.getAcceptDateTime(), addTracingNamespace(context))
                .doOnSubscribe(ignoredValue -> logger.verbose("Listing ConfigurationSettings - {}", selector))
                .doOnSuccess(response -> logger.verbose("Listed ConfigurationSettings - {}", selector))
                .doOnError(error -> logger.warning("Failed to list ConfigurationSetting - {}", selector, error));
        }, context, className, "listConfigurationSettings", isAsync, logger);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date. Revisions expire
     * after a period of time, see <a href="https://azure.microsoft.com/pricing/details/app-configuration/">Pricing</a>
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
        return new PagedFlux<>(() -> withContext(context -> listRevisionsFirstPage(selector, context, CLASS_NAME,
            true)),
            continuationToken -> withContext(context -> listRevisionsNextPage(continuationToken, context, CLASS_NAME,
                true)));
    }

    Mono<PagedResponse<ConfigurationSetting>> listRevisionsFirstPage(SettingSelector selector, Context context,
        String className, boolean isAsync) {
        return apiCallWithTelemetry(ctx -> {
            if (selector != null) {
                final String fields = CoreUtils.arrayToString(selector.getFields(), SettingFields::toStringMapper);
                final String keyFilter = selector.getKeyFilter();
                final String labelFilter = selector.getLabelFilter();

                return service.listKeyValueRevisions(serviceEndpoint, keyFilter, labelFilter, apiVersion, fields,
                        selector.getAcceptDateTime(), null, addTracingNamespace(context))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing ConfigurationSetting revisions - {}", selector))
                    .doOnSuccess(response -> logger.verbose("Listed ConfigurationSetting revisions - {}", selector))
                    .doOnError(error ->
                        logger.warning("Failed to list ConfigurationSetting revisions - {}", selector, error));
            } else {
                return service.listKeyValueRevisions(serviceEndpoint, null, null, apiVersion, null, null, null,
                        addTracingNamespace(context))
                    .doOnRequest(ignoredValue -> logger.verbose("Listing ConfigurationSetting revisions"))
                    .doOnSuccess(response -> logger.verbose("Listed ConfigurationSetting revisions"))
                    .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting revisions", error));
            }
        }, context, className, "listRevisions", isAsync, logger);
    }

    Mono<PagedResponse<ConfigurationSetting>> listRevisionsNextPage(String nextPageLink, Context context,
        String className, boolean isAsync) {
        return apiCallWithTelemetry(ctx -> service.listKeyValues(serviceEndpoint, nextPageLink, ctx)
            .doOnSubscribe(ignoredValue -> logger.verbose("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.verbose("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error)), context, className, "listRevisions", isAsync, logger);
    }

    PagedFlux<ConfigurationSetting> listRevisions(SettingSelector selector, Context context, String className,
        boolean isAsync) {
        return new PagedFlux<>(() -> listRevisionsFirstPage(selector, context, className, isAsync),
            continuationToken -> listRevisionsNextPage(continuationToken, context, className, isAsync));
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

    private static <T> Mono<T> apiCallWithTelemetry(Function<Context, Mono<T>> apiCall, Context context,
        String className, String methodName, boolean isAsync, ClientLogger logger) {
        try {
            context = context == null ? Context.NONE : context;
            String telemetry = createTelemetryValue(className, methodName, isAsync);
            return apiCall.apply(addTracingNamespace(addTelemetryValue(context, telemetry)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    private static Mono<Void> validateSetting(ConfigurationSetting setting, ClientLogger logger) {
        return Mono.fromRunnable(() -> {
            Objects.requireNonNull(setting);

            if (setting.getKey() == null) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Parameter 'key' is required and cannot be null."));
            }
        });
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

    /*
     * Helper method used to create ConfigurationSettings with required properties.
     */
    static ConfigurationSetting createConfiguration(String key, String label, String value) {
        return new ConfigurationSetting().setKey(key).setLabel(label).setValue(value);
    }

    /*
     * Helper method that adds tracing namespace to Context.
     */
    private static Context addTracingNamespace(Context context) {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false)
            ? context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE)
            : context;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.RestProxy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, updating, and deleting
 * ConfigurationSettings, and listing settings or revision of a setting based on a {@link SettingSelector filter}.
 *
 * <p><strong>Instantiating an Asynchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.async.configurationclient.instantiation}
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationClientCredentials
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true, serviceInterfaces = ConfigurationService.class)
public final class ConfigurationAsyncClient {
    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClient.class);

    private static final String ETAG_ANY = "*";
    private static final String RANGE_QUERY = "items=%s";

    private final String serviceEndpoint;
    private final ConfigurationService service;

    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceEndpoint URL for the App Configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    ConfigurationAsyncClient(URL serviceEndpoint, HttpPipeline pipeline) {
        this.service = RestProxy.create(ConfigurationService.class, pipeline);
        this.serviceEndpoint = serviceEndpoint.toString();
    }

    /**
     * Adds a configuration value in the service if that key does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addSetting#string-string}
     *
     * @param key The key of the configuration setting to add.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addSetting(String key, String value) {
        return withContext(
            context -> addSetting(new ConfigurationSetting().key(key).value(value), context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addSetting#ConfigurationSetting}
     *
     * @param setting The setting to add to the configuration service.
     * @return The {@link ConfigurationSetting} that was created, if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addSetting(ConfigurationSetting setting) {
        return withContext(context -> addSetting(setting, context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.addSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The setting to add to the configuration service.
     * @return A REST response containing the {@link ConfigurationSetting} that was created, if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> addSettingWithResponse(ConfigurationSetting setting) {
        return withContext(context -> addSetting(setting, context));
    }

    Mono<Response<ConfigurationSetting>> addSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        // This service method call is similar to setSetting except we're passing If-Not-Match = "*". If the service
        // finds any existing configuration settings, then its e-tag will match and the service will return an error.
        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, null, getETagValue(ETAG_ANY), context)
            .doOnRequest(ignoredValue -> logger.info("Adding ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Added ConfigurationSetting - {}", response.value()))
            .onErrorMap(ConfigurationAsyncClient::addSettingExceptionMapper)
            .doOnError(error -> logger.warning("Failed to add ConfigurationSetting - {}", setting, error));
    }

    /**
     * Creates or updates a configuration value in the service with the given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting}
     *
     * @param key The key of the configuration setting to create or update.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setSetting(String key, String value) {
        return withContext(
            context -> setSetting(new ConfigurationSetting().key(key).value(value), context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is updated if the current
     * setting's etag matches. If the etag's value is equal to the wildcard character ({@code "*"}), the setting
     * will always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setSetting#ConfigurationSetting}
     *
     * @param setting The configuration setting to create or update.
     * @return The {@link ConfigurationSetting} that was created or updated, if the key is an invalid
     * value, the setting is locked, or an etag was provided but does not match the service's current etag value (which
     * will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#etag() etag} was specified, is not the
     * wildcard character, and the current configuration value's etag does not match, or the
     * setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setSetting(ConfigurationSetting setting) {
        return withContext(context -> setSetting(setting, context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is updated if the current
     * setting's etag matches. If the etag's value is equal to the wildcard character ({@code "*"}), the setting
     * will always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The configuration setting to create or update.
     * @return A REST response containing the {@link ConfigurationSetting} that was created or updated, if the key is an invalid
     * value, the setting is locked, or an etag was provided but does not match the service's current etag value (which
     * will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#etag() etag} was specified, is not the
     * wildcard character, and the current configuration value's etag does not match, or the
     * setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setSettingWithResponse(ConfigurationSetting setting) {
        return withContext(context -> setSetting(setting, context));
    }

    Mono<Response<ConfigurationSetting>> setSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        // This service method call is similar to addSetting except it will create or update a configuration setting.
        // If the user provides an etag value, it is passed in as If-Match = "{etag value}". If the current value in the
        // service has a matching etag then it matches, then its value is updated with what the user passed in.
        // Otherwise, the service throws an exception because the current configuration value was updated and we have an
        // old value locally.
        // If no etag value was passed in, then the value is always added or updated.
        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, getETagValue(setting.etag()), null, context)
            .doOnRequest(ignoredValue -> logger.info("Setting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Set ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.warning("Failed to set ConfigurationSetting - {}", setting, error));
    }

    /**
     * Updates an existing configuration value in the service with the given key. The setting must already exist.
     *
     *
     * ><strong>Code Samples</strong></p>
     *
     * <p>Update a setting with the key "prodDBConnection" to have the value "updated_db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.updateSetting#string-string}
     *
     * @param key The key of the configuration setting to update.
     * @param value The updated value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was updated, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If a ConfigurationSetting with the key does not exist or the configuration value
     * is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> updateSetting(String key, String value) {
        return withContext(
            context -> updateSetting(new ConfigurationSetting().key(key).value(value), context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist. Partial updates are not
     * supported, the entire configuration value is replaced.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is only updated if it matches.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the setting with the key-label pair "prodDBConnection"-"westUS" to have the value "updated_db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.updateSetting#ConfigurationSetting}
     *
     * @param setting The setting to add or update in the service.
     * @return The {@link ConfigurationSetting} that was updated, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label does not
     * exist, the setting is locked, or {@link ConfigurationSetting#etag() etag} is specified but does not match
     * the current value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> updateSetting(ConfigurationSetting setting) {
        return withContext(context -> updateSetting(setting, context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist. Partial updates are not
     * supported, the entire configuration value is replaced.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is only updated if it matches.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the setting with the key-label pair "prodDBConnection"-"westUS" to have the value "updated_db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.updateSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The setting to add or update in the service.
     * @return A REST response containing the {@link ConfigurationSetting} that was updated, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label does not
     * exist, the setting is locked, or {@link ConfigurationSetting#etag() etag} is specified but does not match
     * the current value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> updateSettingWithResponse(ConfigurationSetting setting) {
        return withContext(context -> updateSetting(setting, context));
    }

    Mono<Response<ConfigurationSetting>> updateSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        String etag = setting.etag() == null ? ETAG_ANY : setting.etag();

        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, getETagValue(etag), null, context)
            .doOnRequest(ignoredValue -> logger.info("Updating ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Updated ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.warning("Failed to update ConfigurationSetting - {}", setting, error));
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getSetting#string}
     *
     * @param key The key of the setting to retrieve.
     * @return The {@link ConfigurationSetting} stored in the service, if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getSetting(String key) {
        return withContext(
            context -> getSetting(new ConfigurationSetting().key(key), context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Attempts to get the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getSetting#ConfigurationSetting}
     *
     * @param setting The setting to retrieve based on its key and optional label combination.
     * @return The {@link ConfigurationSetting} stored in the service, if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@code} key is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getSetting(ConfigurationSetting setting) {
        return withContext(context -> getSetting(setting, context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Attempts to get the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.getSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The setting to retrieve based on its key and optional label combination.
     * @return A REST response containing the {@link ConfigurationSetting} stored in the service, if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@code} key is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> getSettingWithResponse(ConfigurationSetting setting) {
        return withContext(context -> getSetting(setting, context));
    }

    Mono<Response<ConfigurationSetting>> getSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        return service.getKeyValue(serviceEndpoint, setting.key(), setting.label(), null, null, null, null, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Retrieved ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.warning("Failed to get ConfigurationSetting - {}", setting, error));
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#string}
     *
     * @param key The key of the setting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteSetting(String key) {
        return withContext(
            context -> deleteSetting(new ConfigurationSetting().key(key), context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching key, along with the given label and etag.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified and is not the wildcard character ({@code "*"}),
     * then the setting is <b>only</b> deleted if the etag matches the current etag; this means that no one has updated
     * the ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#ConfigurationSetting}
     *
     * @param setting The ConfigurationSetting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value or {@link ConfigurationSetting#etag() etag} is set but does not match the
     * current etag (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#etag() etag} is specified, not the wildcard
     * character, and does not match the current etag value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteSetting(ConfigurationSetting setting) {
        return withContext(context -> deleteSetting(setting, context))
            .flatMap(response -> Mono.justOrEmpty(response.value()));
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching key, along with the given label and etag.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified and is not the wildcard character ({@code "*"}),
     * then the setting is <b>only</b> deleted if the etag matches the current etag; this means that no one has updated
     * the ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.deleteSettingWithResponse#ConfigurationSetting}
     *
     * @param setting The ConfigurationSetting to delete.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value or {@link ConfigurationSetting#etag() etag} is set but does not match the
     * current etag (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#etag() etag} is specified, not the wildcard
     * character, and does not match the current etag value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> deleteSettingWithResponse(ConfigurationSetting setting) {
        return withContext(context -> deleteSetting(setting, context));
    }

    Mono<Response<ConfigurationSetting>> deleteSetting(ConfigurationSetting setting, Context context) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        return service.delete(serviceEndpoint, setting.key(), setting.label(), getETagValue(setting.etag()), null, context)
            .doOnRequest(ignoredValue -> logger.info("Deleting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.info("Deleted ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.warning("Failed to delete ConfigurationSetting - {}", setting, error));
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.configurationasyncclient.listsettings}
     *
     * @param options Optional. Options to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code options}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listSettings(SettingSelector options) {
        return new PagedFlux<>(() -> withContext(context -> listFirstPageSettings(options, context)),
            continuationToken -> withContext(context -> listNextPageSettings(context, continuationToken)));
    }

    PagedFlux<ConfigurationSetting> listSettings(SettingSelector options, Context context) {
        return new PagedFlux<>(() -> listFirstPageSettings(options, context),
            continuationToken -> listNextPageSettings(context, continuationToken));
    }

    private Mono<PagedResponse<ConfigurationSetting>> listNextPageSettings(Context context, String continuationToken) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        return service.listKeyValues(serviceEndpoint, continuationToken, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", continuationToken))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", continuationToken))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", continuationToken,
                error));
    }

    private Mono<PagedResponse<ConfigurationSetting>> listFirstPageSettings(SettingSelector options, Context context) {
        if (options == null) {
            return service.listKeyValues(serviceEndpoint, null, null, null, null, context)
                .doOnRequest(ignoredValue -> logger.info("Listing all ConfigurationSettings"))
                .doOnSuccess(response -> logger.info("Listed all ConfigurationSettings"))
                .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting", error));
        }

        String fields = ImplUtils.arrayToString(options.fields(), SettingFields::toStringMapper);
        String keys = ImplUtils.arrayToString(options.keys(), key -> key);
        String labels = ImplUtils.arrayToString(options.labels(), label -> label);

        return service.listKeyValues(serviceEndpoint, keys, labels, fields, options.acceptDateTime(), context)
            .doOnRequest(ignoredValue -> logger.info("Listing ConfigurationSettings - {}", options))
            .doOnSuccess(response -> logger.info("Listed ConfigurationSettings - {}", options))
            .doOnError(error -> logger.warning("Failed to list ConfigurationSetting - {}", options, error));

    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#lastModified() lastModified} date. Revisions expire
     * after a period of time. The service maintains change history for up to 7 days.
     *
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code options}.
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
    public PagedFlux<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return new PagedFlux<>(() ->
            withContext(context -> listSettingRevisionsFirstPage(selector, context)),
            continuationToken -> withContext(context -> listSettingRevisionsNextPage(continuationToken, context)));
    }

    Mono<PagedResponse<ConfigurationSetting>> listSettingRevisionsFirstPage(SettingSelector selector, Context context) {
        Mono<PagedResponse<ConfigurationSetting>> result;

        if (selector != null) {
            String fields = ImplUtils.arrayToString(selector.fields(), SettingFields::toStringMapper);
            String keys = ImplUtils.arrayToString(selector.keys(), key -> key);
            String labels = ImplUtils.arrayToString(selector.labels(), label -> label);
            String range = selector.range() != null ? String.format(RANGE_QUERY, selector.range()) : null;

            result = service.listKeyValueRevisions(serviceEndpoint, keys, labels, fields, selector.acceptDateTime(), range, context)
                .doOnRequest(ignoredValue -> logger.info("Listing ConfigurationSetting revisions - {}", selector))
                .doOnSuccess(response -> logger.info("Listed ConfigurationSetting revisions - {}", selector))
                .doOnError(error -> logger.warning("Failed to list ConfigurationSetting revisions - {}", selector, error));
        } else {
            result = service.listKeyValueRevisions(serviceEndpoint, null, null, null, null, null, context)
                .doOnRequest(ignoredValue -> logger.info("Listing ConfigurationSetting revisions"))
                .doOnSuccess(response -> logger.info("Listed ConfigurationSetting revisions"))
                .doOnError(error -> logger.warning("Failed to list all ConfigurationSetting revisions", error));
        }

        return result;
    }

    Mono<PagedResponse<ConfigurationSetting>> listSettingRevisionsNextPage(String nextPageLink, Context context) {
        Mono<PagedResponse<ConfigurationSetting>> result = service.listKeyValues(serviceEndpoint, nextPageLink, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink, error));
        return result;
    }

    PagedFlux<ConfigurationSetting> listSettingRevisions(SettingSelector selector, Context context) {
        return new PagedFlux<>(() ->
            listSettingRevisionsFirstPage(selector, context),
            continuationToken -> listSettingRevisionsNextPage(continuationToken, context));
    }

    private Flux<ConfigurationSetting> listSettings(String nextPageLink, Context context) {
        Mono<PagedResponse<ConfigurationSetting>> result = service.listKeyValues(serviceEndpoint, nextPageLink, context)
            .doOnRequest(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink, error));

        return result.flatMapMany(r -> extractAndFetchConfigurationSettings(r, context));
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(PagedResponse<ConfigurationSetting> page, Context context) {
        return ImplUtils.extractAndFetch(page, context, this::listSettings);
    }

    /*
     * Azure Configuration service requires that the etag value is surrounded in quotation marks.
     *
     * @param etag The etag to get the value for. If null is pass in, an empty string is returned.
     * @return The etag surrounded by quotations. (ex. "etag")
     */
    private static String getETagValue(String etag) {
        return etag == null ? "" : "\"" + etag + "\"";
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    private static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.key() == null) {
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
    private static Throwable addSettingExceptionMapper(Throwable throwable) {
        if (!(throwable instanceof ResourceNotFoundException)) {
            return throwable;
        }

        ResourceNotFoundException notFoundException = (ResourceNotFoundException) throwable;
        return new ResourceModifiedException(notFoundException.getMessage(), notFoundException.response());
    }
}

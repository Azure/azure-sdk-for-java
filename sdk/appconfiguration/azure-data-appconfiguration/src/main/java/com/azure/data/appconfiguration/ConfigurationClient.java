// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.time.OffsetDateTime;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, deleting, lock and unlock
 * ConfigurationSettings, and listing settings or revision of a setting based on a {@link SettingSelector filter}.
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * {@codesnippet com.azure.data.applicationconfig.configurationclient.instantiation}
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationClientCredentials
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, serviceInterfaces = ConfigurationService.class)
public final class ConfigurationClient {
    private final ConfigurationAsyncClient client;

    /**
     * Creates a ConfigurationClient that sends requests to the configuration service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link ConfigurationAsyncClient} that the client routes its request through.
     */
    ConfigurationClient(ConfigurationAsyncClient client) {
        this.client = client;
    }

    /**
     * Adds a configuration value in the service if that key does not exist. The {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS" and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.ConfigurationClient.addSetting#String-String-String}
     *
     * @param key The key of the configuration setting to add.
     * @param label The label of the configuration setting to create, or optionally, null if a setting with
     * label is desired.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting addSetting(String key, String label, String value) {
        return addSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label).setValue(value),
            Context.NONE).getValue();
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.ConfigurationClient.addSettingWithResponse#ConfigurationSetting-Context}
     *
     * @param setting The setting to add based on its key and optional label combination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the the {@link ConfigurationSetting} that was created, or {@code null}, if a
     * key collision occurs or the key is an invalid value (which will also throw ServiceRequestException described
     * below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> addSettingWithResponse(ConfigurationSetting setting, Context context) {
        return client.addSetting(setting, context).block();
    }

    /**
     * Creates or updates a configuration value in the service with the given key and. the {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.ConfigurationClient.setSetting#String-String-String}
     *
     * @param key The key of the configuration setting to create or update.
     * @param label The label of the configuration setting to create or update, or optionally, null if a setting with
     * label is desired.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null} if the key is an invalid
     * value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setSetting(String key, String label, String value) {
        return setSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label).setValue(value),
            false, Context.NONE).getValue();
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * If {@link ConfigurationSetting#getETag() etag} is specified, the configuration value is updated if the current
     * setting's etag matches. If the etag's value is equal to the wildcard character ({@code "*"}), the setting will
     * always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * {@codesnippet com.azure.data.appconfiguration.ConfigurationClient.setSettingWithResponse#ConfigurationSetting-boolean-Context}
     *
     * @param setting The setting to create or update based on its key, optional label and optional ETag combination.
     * @param ifUnchanged A boolean indicates if {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response contains the {@link ConfigurationSetting} that was created or updated, or {@code null},
     * if the configuration value does not exist or the key is an invalid value (which will also throw
     * ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#getETag() etag} was specified, is not the
     * wildcard character, and the current configuration value's etag does not match, or the setting exists and is
     * locked.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> setSettingWithResponse(ConfigurationSetting setting, boolean ifUnchanged,
                                                                 Context context) {
        return client.setSetting(setting, ifUnchanged, context).block();
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, and the optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.getSetting#string-string}
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve, or optionally, null if a setting with
     * label is desired.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getSetting(String key, String label) {
        return getSetting(key, label, null);
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, the optional {@code label}, and the optional
     * {@code asOfDateTime} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.getSetting#string-string-OffsetDateTime}
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to create or update, or optionally, null if a setting with
     * label is desired.
     * @param asOfDateTime To access a past state of the configuration setting, or optionally, null if a setting with
     * {@code asOfDateTime} is desired.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getSetting(String key, String label, OffsetDateTime asOfDateTime) {
        return client.getSetting(
            new ConfigurationSetting().setKey(key).setLabel(label), asOfDateTime, false, Context.NONE)
            .flatMap(FluxUtil::toMono).block();
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code asOfDateTime} and optional ETag combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.getSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean-Context}
     *
     * @param setting The setting to retrieve.
     * @param asOfDateTime To access a past state of the configuration setting, or optionally, null if a setting with
     * {@code asOfDateTime} is desired.
     * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * If-None-Match header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response contains the {@link ConfigurationSetting} stored in the service, or {@code null}, if the
     * configuration value does not exist or the key is an invalid value (which will also throw ServiceRequestException
     * described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> getSettingWithResponse(ConfigurationSetting setting,
                                                                 OffsetDateTime asOfDateTime,
                                                                 boolean ifChanged,
                                                                 Context context) {
        return client.getSetting(setting, asOfDateTime, ifChanged, context).block();
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@code key} and optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.deleteSetting#string-string}
     *
     * @param key The key of configuration setting to delete.
     * @param label The label of configuration setting to delete, or optionally, null if a setting with
     * label is desired.
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if the
     * {@code key} is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting deleteSetting(String key, String label) {
        return deleteSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label),
            false, Context.NONE).getValue();
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination.
     *
     * If {@link ConfigurationSetting#getETag() etag} is specified and is not the wildcard character ({@code "*"}), then
     * the setting is <b>only</b> deleted if the etag matches the current etag; this means that no one has updated the
     * ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.deleteSettingWithResponse#ConfigurationSetting-boolean-Context}
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value or
     * {@link ConfigurationSetting#getETag() etag} is set but does not match the current etag
     * (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is locked.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() etag} is specified, not the wildcard
     * character, and does not match the current etag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> deleteSettingWithResponse(ConfigurationSetting setting, boolean ifUnchanged,
                                                                    Context context) {
        return client.deleteSetting(setting, ifUnchanged, context).block();
    }

    /**
     * Lock the {@link ConfigurationSetting} with a matching {@code key}, optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Lock the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string}
     *
     * @param key The key of configuration setting to lock.
     * @param label The label of configuration setting to lock, or optionally, null if a setting with label is desired.
     * @return The {@link ConfigurationSetting} that was locked, or {@code null} is also returned if a key collision
     * occurs or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setReadOnly(String key, String label) {
        return setReadOnlyWithResponse(new ConfigurationSetting().setKey(key).setLabel(label), Context.NONE).getValue();
    }

    /**
     * Lock the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Lock the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-Context}
     *
     * @param setting The setting to lock based on its key and optional label combination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the locked ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value. (which will also throw
     * HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> setReadOnlyWithResponse(ConfigurationSetting setting, Context context) {
        return client.setReadOnly(setting, context).block();
    }

    /**
     * Unlock the {@link ConfigurationSetting} with a matching {@code key}, optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Unlock the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.clearReadOnly#string-string}
     *
     * @param key The key of configuration setting to unlock.
     * @param label The label of configuration setting to unlock, or optionally, null if a setting with
     * label is desired.
     * @return The {@link ConfigurationSetting} that was unlocked, or {@code null} is also returned if a key collision
     * occurs or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting clearReadOnly(String key, String label) {
        return clearReadOnlyWithResponse(new ConfigurationSetting().setKey(key).setLabel(label), Context.NONE)
            .getValue();
    }

    /**
     * Unlock the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Unlock the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.clearReadOnlyWithResponse#ConfigurationSetting-Context}
     *
     * @param setting The setting to unlock based on its key and optional label combination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the unlocked ConfigurationSetting, or {@code null} if didn't exist.
     * {@code null} is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value. (which will
     * also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> clearReadOnlyWithResponse(ConfigurationSetting setting, Context context) {
        return client.clearReadOnly(setting, context).block();
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector}
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code options}. If no options were
     * provided, the List contains all of the current settings in the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listSettings(SettingSelector selector) {
        return listSettings(selector, Context.NONE);
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector-context}
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code options}. If no options were
     * provided, the {@link PagedIterable} contains all of the current settings in the service.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listSettings(SettingSelector selector, Context context) {
        return new PagedIterable<>(client.listSettings(selector, context));
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date. Revisions expire
     * after a period of time. The service maintains change history for up to 7 days.
     *
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code options}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector}
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return {@link PagedIterable} of {@link ConfigurationSetting} revisions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return listSettingRevisions(selector, Context.NONE);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date. Revisions expire
     * after a period of time. The service maintains change history for up to 7 days.
     *
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code options}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * {@codesnippet com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector-context}
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link ConfigurationSetting} revisions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listSettingRevisions(SettingSelector selector, Context context) {
        return new PagedIterable<>(client.listSettingRevisions(selector, context));
    }
}

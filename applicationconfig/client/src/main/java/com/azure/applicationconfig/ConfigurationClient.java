// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;

import java.util.List;


/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, updating, and deleting
 * ConfigurationSettings, and listing settings or revision of a setting based on a {@link SettingSelector filter}.
 *
 * <p><strong>Instantiating an Asynchronous Configuration Client</strong></p>
 *
 * <pre>
 * ConfigurationClient client = ConfigurationClient.builder()
 *     .credentials(new ConfigurationClientCredentials(connectionString))
 *     .build();
 * </pre>
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClient {
    private final ConfigurationAsyncClient client;

    /**
     * Creates a ConfigurationClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param client The {@link ConfigurationAsyncClient} that the client routes its request through.
     */
    ConfigurationClient(ConfigurationAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a builder that can configure options for the ConfigurationClient before creating an instance of it.
     *
     * @return A new {@link ConfigurationClientBuilder} to create a ConfigurationClient.
     */
    public static ConfigurationClientBuilder builder() {
        return new ConfigurationClientBuilder();
    }

    /**
     * Adds a configuration value in the service if that key does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.addSetting("prodDBConnection", "db_connection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param key The key of the configuration setting to add.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null}, if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> addSetting(String key, String value) {
        return addSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.addSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"));
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param setting The setting to add to the configuration service.
     * @return The {@link ConfigurationSetting} that was created, or {@code null}, if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> addSetting(ConfigurationSetting setting) {
        return client.addSetting(setting).block();
    }

    /**
     * Creates or updates a configuration value in the service with the given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.setSetting("prodDBConnection", "db_connection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * <p>Update the value of the setting to "updated_db_connection".</p>
     *
     * <pre>
     * result = client.setSetting("prodDBConnection", "updated_db_connection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param key The key of the configuration setting to create or update.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null}, if the key is an invalid
     * value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
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
     * <pre>
     * ConfigurationSetting result = client.setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"));
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * <p>Update the value of the setting to "updated_db_connection".</p>
     *
     * <pre>
     * result = client
     *     .setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("updated_db_connection"))
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param setting The configuration setting to create or update.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null}, if the key is an invalid
     * value, the setting is locked, or an etag was provided but does not match the service's current etag value (which
     * will also throw ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#etag() etag} was specified, is not the
     * wildcard character, and the current configuration value's etag does not match, or the
     * setting exists and is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> setSetting(ConfigurationSetting setting) {
        return client.setSetting(setting).block();
    }

    /**
     * Updates an existing configuration value in the service with the given key. The setting must already exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update a setting with the key "prodDBConnection" to have the value "updated_db_connection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.updateSetting("prodDCConnection", "updated_db_connection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param key The key of the configuration setting to update.
     * @param value The updated value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was updated, or {@code null}, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If a ConfigurationSetting with the key does not exist or the configuration value
     * is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
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
     * <pre>
     * ConfigurationSetting result = client.updateSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("updated_db_connection"));
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param setting The setting to add or update in the service.
     * @return The {@link ConfigurationSetting} that was updated, or {@code null}, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label does not
     * exist, the setting is locked, or {@link ConfigurationSetting#etag() etag} is specified but does not match
     * the current value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> updateSetting(ConfigurationSetting setting) {
        return client.updateSetting(setting).block();
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.get("prodDBConnection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param key The key of the setting to retrieve.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Attempts to get the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.getSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"));
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param setting The setting to retrieve based on its key and optional label combination.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@code} key is an empty string.
     */
    public Response<ConfigurationSetting> getSetting(ConfigurationSetting setting) {
        return client.getSetting(setting).block();
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <pre>
     * ConfigurationSetting result = client.deleteSetting("prodDBConnection");
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param key The key of the setting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
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
     * <pre>
     * ConfigurationSetting result = client.deleteSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"));
     * System.out.printf("Key: %s, Value: %s", result.key(), result.value());</pre>
     *
     * @param setting The ConfigurationSetting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value or {@link ConfigurationSetting#etag() etag} is set but does not match the
     * current etag (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#etag() etag} is specified, not the wildcard
     * character, and does not match the current etag value.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> deleteSetting(ConfigurationSetting setting) {
        return client.deleteSetting(setting).block();
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * <pre>
     * for (ConfigurationSetting setting : client.listSettings(new SettingSelector().key("prodDBConnection"))) {
     *     System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
     * }</pre>
     *
     * @param options Optional. Options to filter configuration setting results from the service.
     * @return A List of ConfigurationSettings that matches the {@code options}. If no options were provided, the List
     * contains all of the current settings in the service.
     */
    public List<ConfigurationSetting> listSettings(SettingSelector options) {
        return client.listSettings(options).collectList().block();
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
     * <pre>
     * for (ConfigurationSetting revision : client.listSettingRevisions(new SettingSelector().key("prodDBConnection"))) {
     *     System.out.printf("Key: %s, Value: %s", revision.key(), revision.value());
     * }</pre>
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     */
    public List<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return client.listSettingRevisions(selector).collectList().block();
    }
}

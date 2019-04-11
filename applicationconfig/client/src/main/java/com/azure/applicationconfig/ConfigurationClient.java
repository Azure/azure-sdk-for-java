package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.rest.Response;

import java.net.URL;
import java.util.List;

/**
 * Synchronous client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings} in Azure Configuration
 * Store.
 *
 * @see ConfigurationClientBuilder
 * @see com.azure.applicationconfig.credentials.ConfigurationClientCredentials
 */
public final class ConfigurationClient extends ConfigurationClientBase<Response<ConfigurationSetting>, List<ConfigurationSetting>> {
    /**
     * Creates a ConfigurationClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceEndpoint URL for the App Config service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    ConfigurationClient(URL serviceEndpoint, HttpPipeline pipeline) {
        super(serviceEndpoint, pipeline);
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
     * @param key The key of the configuration setting to add.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null}, if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key exists. Or, {@code key} is an empty
     * string.
     */
    public Response<ConfigurationSetting> addSetting(String key, String value) {
        return addSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> addSetting(ConfigurationSetting setting) {
        return super.addSettingBase(setting).block();
    }

    /**
     * Creates or updates a configuration value in the service with the given key.
     *
     * @param key The key of the configuration setting to create or update.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null}, if the key is an invalid
     * value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ServiceRequestException If the setting exists and is locked. Or, if {@code key} is an empty string.
     */
    public Response<ConfigurationSetting> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> setSetting(ConfigurationSetting setting) {
        return super.setSettingBase(setting).block();
    }

    public Response<ConfigurationSetting> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
    }

    public Response<ConfigurationSetting> updateSetting(ConfigurationSetting setting) {
        return super.updateSettingBase(setting).block();
    }

    public Response<ConfigurationSetting> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    public Response<ConfigurationSetting> getSetting(ConfigurationSetting setting) {
        return super.getSettingBase(setting).block();
    }

    public Response<ConfigurationSetting> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
    }

    public Response<ConfigurationSetting> deleteSetting(ConfigurationSetting setting) {
        return super.deleteSettingBase(setting).block();
    }

    public List<ConfigurationSetting> listSettings(SettingSelector options) {
        return super.listSettingsBase(options).collectList().block();
    }

    public List<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return super.listSettingRevisionsBase(selector).collectList().block();
    }
}

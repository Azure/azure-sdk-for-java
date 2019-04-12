package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
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
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> addSetting(String key, String value) {
        return addSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> addSetting(ConfigurationSetting setting) {
        return addSettingBase(setting).block();
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> setSetting(ConfigurationSetting setting) {
        return setSettingBase(setting).block();
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> updateSetting(ConfigurationSetting setting) {
        return updateSettingBase(setting).block();
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> getSetting(ConfigurationSetting setting) {
        return getSettingBase(setting).block();
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
    }

    /**
     * {@inheritDoc}
     */
    public Response<ConfigurationSetting> deleteSetting(ConfigurationSetting setting) {
        return deleteSettingBase(setting).block();
    }

    /**
     * {@inheritDoc}
     * @return A List of ConfigurationSettings that matches the {@code options}. If no options were provided, the List
     * contains all of the current settings in the service.
     */
    public List<ConfigurationSetting> listSettings(SettingSelector options) {
        return listSettingsBase(options).collectList().block();
    }

    /**
     * {@inheritDoc}
     */
    public List<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return listSettingRevisionsBase(selector).collectList().block();
    }
}

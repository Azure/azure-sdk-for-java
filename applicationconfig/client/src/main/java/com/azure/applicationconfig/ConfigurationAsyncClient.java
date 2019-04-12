// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.rest.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;

/**
 * Asynchronous client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings} in Azure Configuration
 * Store.
 *
 * @see ConfigurationAsyncClientBuilder
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationAsyncClient extends ConfigurationClientBase<Mono<Response<ConfigurationSetting>>, Flux<ConfigurationSetting>> {
    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceEndpoint URL for the Application configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    ConfigurationAsyncClient(URL serviceEndpoint, HttpPipeline pipeline) {
        super(serviceEndpoint, pipeline);
    }

    /**
     * Creates a builder that can configure options for the ConfigurationAsyncClient before creating an instance of it.
     *
     * @return A new ConfigurationAsyncClientBuilder to create a ConfigurationAsyncClient from.
     */
    public static ConfigurationAsyncClientBuilder builder() {
        return new ConfigurationAsyncClientBuilder();
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> addSetting(String key, String value) {
        return addSettingBase(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> addSetting(ConfigurationSetting setting) {
        return addSettingBase(setting);
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> setSetting(ConfigurationSetting setting) {
        return super.setSettingBase(setting);
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(ConfigurationSetting setting) {
        return super.updateSettingBase(setting);
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> getSetting(ConfigurationSetting setting) {
        return super.getSettingBase(setting);
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(ConfigurationSetting setting) {
        return super.deleteSettingBase(setting);
    }

    /**
     * {@inheritDoc}
     */
    public Flux<ConfigurationSetting> listSettings(SettingSelector options) {
        return super.listSettingsBase(options);
    }

    /**
     * {@inheritDoc}
     */
    public Flux<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        return super.listSettingRevisionsBase(selector);
    }
}

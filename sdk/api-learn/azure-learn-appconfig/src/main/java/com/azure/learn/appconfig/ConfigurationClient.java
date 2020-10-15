package com.azure.learn.appconfig;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.learn.appconfig.models.ConfigurationSetting;

import static com.azure.core.util.FluxUtil.withContext;

@ServiceClient(builder = ConfigurationClientBuilder.class)
public final class ConfigurationClient {

    private final ConfigurationAsyncClient asyncClient;

    ConfigurationClient(ConfigurationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Get a ConfigurationSetting.
     * @param key The unique name of the ConfigurationSetting.
     * @return The ConfigurationSetting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key) {
        return asyncClient.getConfigurationSetting(key).block();
    }

    /**
     * Get a ConfigurationSetting.
     * @param key The unique name of the ConfigurationSetting.
     * @param label The label of the ConfigurationSetting.
     * @return The ConfigurationSetting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key, String label) {
        return asyncClient.getConfigurationSetting(key, label).block();
    }

    /**
     * Get a ConfigurationSetting.
     * @param setting The ConfigurationSetting.
     * @param ifChanged If true, will only return the ConfigurationSetting if it has changed.
     * @param context The Context.
     * @return The Response with the ConfigurationSetting.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                              boolean ifChanged, Context context) {
        return asyncClient.getConfigurationSettingWithResponse(setting, ifChanged, context).block();
    }
}

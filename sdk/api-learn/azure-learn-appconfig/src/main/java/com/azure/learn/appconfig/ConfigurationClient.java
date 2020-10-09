package com.azure.learn.appconfig;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.learn.appconfig.models.ConfigurationSetting;

@ServiceClient(builder = ConfigurationClientBuilder.class)
public final class ConfigurationClient {

    private final ConfigurationAsyncClient asyncClient;

    ConfigurationClient(ConfigurationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key) {
        return asyncClient.getConfigurationSetting(key).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key, String label) {
        return asyncClient.getConfigurationSetting(key, label).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                              boolean ifChanged, Context context) {
        return asyncClient.getConfigurationSettingWithResponse(setting, ifChanged, context).block();
    }
}

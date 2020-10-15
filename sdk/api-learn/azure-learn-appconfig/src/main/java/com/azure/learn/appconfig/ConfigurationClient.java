package com.azure.learn.appconfig;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.learn.appconfig.models.ConfigurationSetting;

/**
 * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
 * {@link ConfigurationSetting#getLabel() label} and optional ETag combination.
 *
 * @param setting The configuration setting to retrieve.
 * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
 * If-None-Match header.
 * @param context Additional context that is passed through the Http pipeline during the service call.
 * @return A REST response contains the {@link ConfigurationSetting} stored in the service, or {@code null}, if the
 * configuration value does not exist or the key is an invalid value (which will also throw ServiceRequestException
 * described below).
 * @throws NullPointerException If {@code setting} is {@code null}.
 * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
 * @throws HttpResponseException If the call to the service failed for any other reason.
 */
@ServiceClient(builder = ConfigurationClientBuilder.class)
public final class ConfigurationClient {

    private final ConfigurationAsyncClient asyncClient;

    ConfigurationClient(ConfigurationAsyncClient asyncClient) {
        // package-private constructor
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

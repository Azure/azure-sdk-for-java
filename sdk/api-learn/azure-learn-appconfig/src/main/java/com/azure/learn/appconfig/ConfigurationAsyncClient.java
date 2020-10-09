package com.azure.learn.appconfig;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import reactor.core.publisher.Mono;

@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true)
public final class ConfigurationAsyncClient {

    ConfigurationAsyncClient() {
        // package-private constructor
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key) {
        return Mono.empty();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label) {
        return Mono.empty();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                                    boolean ifChanged) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}

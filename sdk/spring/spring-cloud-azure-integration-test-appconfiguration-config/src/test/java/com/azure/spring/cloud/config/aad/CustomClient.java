package com.azure.spring.cloud.config.aad;

import com.azure.spring.cloud.config.BaseCustomClient;
import org.springframework.core.env.Environment;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;

public class CustomClient extends BaseCustomClient implements ConfigurationClientCustomizer, SecretClientCustomizer {

    public CustomClient(Environment environment) {
        super(environment);
    }

    @Override
    public void customize(ConfigurationClientBuilder builder, String endpoint) {
        builder.credential(buildCredential());
    }

    @Override
    public void customize(SecretClientBuilder builder, String endpoint) {
        builder.credential(buildCredential());
    }

}

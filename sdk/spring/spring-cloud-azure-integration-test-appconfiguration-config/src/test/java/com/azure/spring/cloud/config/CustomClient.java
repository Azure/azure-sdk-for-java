package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class CustomClient implements ConfigurationClientCustomizer, SecretClientCustomizer {

    TokenCredential buildCredential() {
        return new EnvironmentCredentialBuilder().build();
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
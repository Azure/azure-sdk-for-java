package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

public class CustomClient implements ConfigurationClientCustomizer {



    TokenCredential buildCredential() {
        return new EnvironmentCredentialBuilder().build();
    }

    @Override
    public void setup(ConfigurationClientBuilder builder, String endpoint) {
        builder.credential(buildCredential());
    }

}
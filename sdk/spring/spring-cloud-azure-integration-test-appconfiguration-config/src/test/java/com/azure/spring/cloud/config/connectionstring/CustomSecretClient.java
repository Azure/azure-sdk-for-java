package com.azure.spring.cloud.config.connectionstring;

import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.config.BaseCustomClient;
import org.springframework.core.env.Environment;

public class CustomSecretClient extends BaseCustomClient implements SecretClientCustomizer {
    public CustomSecretClient(Environment environment) {
        super(environment);
    }

    @Override
    public void customize(SecretClientBuilder builder, String endpoint) {
        builder.credential(buildCredential());
    }

}

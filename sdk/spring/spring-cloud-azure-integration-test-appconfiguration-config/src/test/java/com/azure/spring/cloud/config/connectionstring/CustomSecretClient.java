package com.azure.spring.cloud.config.connectionstring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import com.azure.spring.cloud.config.BaseCustomClient;
import org.springframework.core.env.Environment;

public class CustomSecretClient extends BaseCustomClient implements SecretClientCustomizer {
    private final Environment environment;

    public CustomSecretClient(Environment environment) {
        this.environment = environment;
    }

    TokenCredential buildCredential() {
        String azureClientId = environment.getProperty("AZURE_CLIENT_ID_MANAGED_IDENTITY");
        return new DefaultAzureCredentialBuilder()
            .managedIdentityClientId(azureClientId)
            .build();
    }

    @Override
    public void customize(SecretClientBuilder builder, String endpoint) {
        builder.credential(buildCredential());
    }

}

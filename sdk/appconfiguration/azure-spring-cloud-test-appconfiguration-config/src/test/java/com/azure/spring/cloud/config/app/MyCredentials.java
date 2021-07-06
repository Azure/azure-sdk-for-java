package com.azure.spring.cloud.config.app;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;

public class MyCredentials implements AppConfigurationCredentialProvider, KeyVaultCredentialProvider{

    @Override
    public TokenCredential getKeyVaultCredential(String uri) {
        return buildCredential();
    }

    @Override
    public TokenCredential getAppConfigCredential(String uri) {
        return buildCredential();
    }

    TokenCredential buildCredential() {
            return  new EnvironmentCredentialBuilder().build();
    }

}
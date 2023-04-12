package com.azure.spring.cloud.config;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;

public class CustomClient implements ConfigurationClientCustomizer, SecretClientCustomizer {
    
    private final Environment environment;
    
    public CustomClient(Environment environment) {
        this.environment = environment;
    }

    TokenCredential buildCredential() {
        EnvironmentCredentialBuilder cred = new EnvironmentCredentialBuilder();
        String authorityHost = environment.getProperty("AZURE_AUTHORITY_HOST");
        if (StringUtils.hasText(authorityHost)) {
            if (authorityHost.startsWith("https://login.microsoftonline.us")) {
                cred.authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT);
            } else if (authorityHost.startsWith("https://login.chinacloudapi.cn")) {
                cred.authorityHost(AzureAuthorityHosts.AZURE_CHINA);
            }
        }
        
        return cred.build();
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
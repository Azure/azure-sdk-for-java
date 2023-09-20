package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.EnvironmentCredentialBuilder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class BaseCustomClient {

    private final Environment environment;

    public BaseCustomClient(Environment environment) {
        this.environment = environment;
    }

    protected TokenCredential buildCredential() {
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
}

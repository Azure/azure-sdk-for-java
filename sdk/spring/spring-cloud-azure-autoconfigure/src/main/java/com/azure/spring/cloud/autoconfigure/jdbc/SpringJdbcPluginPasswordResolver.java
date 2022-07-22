package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.JdbcAccessTokenProvider;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.TokenCredentialProvider;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver.PasswordResolver;
import com.azure.spring.cloud.autoconfigure.jdbc.provider.AzureTokenCredentialProvider;

import java.util.Map;

public class SpringJdbcPluginPasswordResolver implements PasswordResolver<String> {

    private TokenCredentialProvider tokenCredentialProvider;

    public SpringJdbcPluginPasswordResolver(Map<String, String> map) {
        this.tokenCredentialProvider = new AzureTokenCredentialProvider(map);
    }

    @Override
    public String getPassword(){
        AccessToken accessToken = getAccessToken();
        if (accessToken != null) {
            return  accessToken.getToken();
        }
        return null;
    }

    private AccessToken getAccessToken() {
        TokenCredential credential = tokenCredentialProvider.getTokenCredential();
        return new JdbcAccessTokenProvider(credential).getAccessToken();

    }
}

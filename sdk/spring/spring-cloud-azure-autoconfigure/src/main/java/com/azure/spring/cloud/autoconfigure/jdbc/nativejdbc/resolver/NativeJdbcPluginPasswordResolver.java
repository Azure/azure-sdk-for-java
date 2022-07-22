package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.JdbcTokenCredentialProvider;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.JdbcAccessTokenProvider;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider.TokenCredentialProvider;

import java.util.Map;

public class NativeJdbcPluginPasswordResolver implements PasswordResolver<String> {

    private TokenCredentialProvider tokenCredentialProvider;

    public NativeJdbcPluginPasswordResolver(Map<String, String> map) {
        this.tokenCredentialProvider = new JdbcTokenCredentialProvider(map);
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

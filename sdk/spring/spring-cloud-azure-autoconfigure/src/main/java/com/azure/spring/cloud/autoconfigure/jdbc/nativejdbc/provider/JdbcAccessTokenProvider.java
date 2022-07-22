package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import java.time.Duration;
import java.util.ArrayList;

public class JdbcAccessTokenProvider implements AccessTokenProvider<AccessToken> {

   //@TODO this is  public cloud specific, do we plan to support national clouds
    private String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    private TokenCredential tokenCredential;

    public JdbcAccessTokenProvider(TokenCredential tokenCredential, String OSSRDBMS_SCOPE) {
        this.tokenCredential = tokenCredential;
        if (OSSRDBMS_SCOPE != null) {
            this.OSSRDBMS_SCOPE = OSSRDBMS_SCOPE;
        }
    }

    public JdbcAccessTokenProvider(TokenCredential tokenCredential) {
        this(tokenCredential, null);
    }

    @Override
    public AccessToken getAccessToken() {
        TokenRequestContext request = new TokenRequestContext();
        ArrayList<String> scopes = new ArrayList<>();
        scopes.add(OSSRDBMS_SCOPE);
        request.setScopes(scopes);
        return tokenCredential.getToken(request).block(Duration.ofSeconds(30));
    }
}

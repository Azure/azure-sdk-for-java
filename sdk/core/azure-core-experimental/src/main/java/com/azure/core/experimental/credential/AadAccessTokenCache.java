package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import java.util.Objects;

public final class AadAccessTokenCache extends AccessTokenCache<TokenCredential, TokenRequestContext> {

    public AadAccessTokenCache(TokenCredential tokenCredential)
    {
        super(new TokenSupplier<TokenCredential, TokenRequestContext>(tokenCredential));
        Objects.requireNonNull(tokenCredential, "The token credential cannot be null");

    }

    public boolean checkIfWeShouldForceRefresh(TokenRequestContext tokenRequestContext) {
        return !(this.tokenRequestContext != null
            && (this.tokenRequestContext.getClaims() == null ? tokenRequestContext.getClaims() == null
            : (tokenRequestContext.getClaims() == null ? false
            : tokenRequestContext.getClaims().equals(this.tokenRequestContext.getClaims())))
            && this.tokenRequestContext.getScopes().equals(tokenRequestContext.getScopes()));
    }
}

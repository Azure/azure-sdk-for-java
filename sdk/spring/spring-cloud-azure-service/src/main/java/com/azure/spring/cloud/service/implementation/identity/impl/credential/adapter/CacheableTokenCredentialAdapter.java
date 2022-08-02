package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CacheableTokenCredentialAdapter<T extends TokenCredential>
        implements CacheKeyDescriptor<String, TokenRequestContext>, TokenCredential {

    private static final String KEY_DELIMITER = "/";
    protected static final String SUB_KEY_DELIMITER = "_";

    protected abstract Descriptor[] getTokenCredentialKeyDescriptors();
    private final TokenCredentialProviderOptions options;
    private final T delegate;
    public CacheableTokenCredentialAdapter(TokenCredentialProviderOptions options, T delegate) {
        this.options = options;
        this.delegate = delegate;
    }

    @Override
    public String getCacheKey(TokenRequestContext requestContext) {
        List<String> credentialKeyValues = Arrays.stream(getTokenCredentialKeyDescriptors())
                .map(descriptor -> descriptor.getter.apply(this.options)).collect(Collectors.toList());

        String tokenCredentialKey = String.join(SUB_KEY_DELIMITER, credentialKeyValues);
        String tokenRequestContextKey = String.join(SUB_KEY_DELIMITER, requestContext.getTenantId(), requestContext.getClaims(),
                requestContext.getScopes().toString());
        return String.join(KEY_DELIMITER, tokenCredentialKey, tokenRequestContextKey);
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return this.delegate.getToken(request);
    }

    protected TokenCredentialProviderOptions getOptions() {
        return options;
    }


    enum Descriptor {
        AUTHORITY_HOST(TokenCredentialProviderOptions::getAuthorityHost),
        CLIENT_ID(TokenCredentialProviderOptions::getClientId),
        CLIENT_SECRET(TokenCredentialProviderOptions::getClientSecret),
        CLIENT_CERTIFICATE_PATH(TokenCredentialProviderOptions::getClientCertificatePath),
        CLIENT_CERTIFICATE_PASSWORD(TokenCredentialProviderOptions::getClientCertificatePassword),
        TENANT_ID(TokenCredentialProviderOptions::getTenantId),
        USERNAME(TokenCredentialProviderOptions::getUsername),
        PASSWORD(TokenCredentialProviderOptions::getPassword)
        ;
        Function<TokenCredentialProviderOptions, String> getter;

        Descriptor(Function<TokenCredentialProviderOptions, String> getter) {
            this.getter = getter;
        }

    }
}

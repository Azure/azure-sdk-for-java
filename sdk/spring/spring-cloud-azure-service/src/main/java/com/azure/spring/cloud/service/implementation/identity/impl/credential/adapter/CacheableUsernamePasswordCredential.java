package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

import com.azure.identity.UsernamePasswordCredential;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;

public class CacheableUsernamePasswordCredential extends CacheableTokenCredentialAdapter<UsernamePasswordCredential> {

    public CacheableUsernamePasswordCredential(TokenCredentialProviderOptions options,
                                               UsernamePasswordCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[] {
                Descriptor.AUTHORITY_HOST,
                Descriptor.TENANT_ID,
                Descriptor.USERNAME,
                Descriptor.PASSWORD
        };
    }
}

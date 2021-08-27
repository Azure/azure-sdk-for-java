// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of Spring token credential. It will populate credentials from the default
 * property prefix <i><b>azure.credential</b></i> and an alternative prefix if specified, for example
 * <i><b>spring.cloud.azure</b></i>.
 */
public class DefaultSpringCredentialBuilder extends SpringCredentialBuilderBase<DefaultSpringCredentialBuilder> {

    /**
     * Defines the AZURE_CREDENTIAL_PREFIX.
     */
    static final String AZURE_CREDENTIAL_PREFIX = "azure.credential.";

    private String alternativePrefix;

    public DefaultSpringCredentialBuilder alternativePrefix(String alternative) {
        if (alternative != null) {
            this.alternativePrefix = alternative + (alternative.endsWith(".") ? "" : ".");
        }

        return this;
    }

    /**
     * Build a default Spring token credential, which will be a chained credential.
     * If an alternative prefix is specified in the builder, the chain of credential
     * will have three credentials, one with the specified prefix, one with the default
     * spring credential prefix, and the default managed identity credential without client id
     * set. Otherwise, the chain will consist the credential with the default prefix and the default
     * managed identity credential.
     *
     * @return the default Spring token credential.
     * @throws IllegalArgumentException if no environment is set.
     */
    public TokenCredential build() {
        if (environment == null) {
            throw new IllegalArgumentException("To build a spring credential the environment must be set.");
        }

        List<TokenCredential> tokenCredentials = new ArrayList<>();

        if (alternativePrefix != null) {
            addToChain(tokenCredentials, populateTokenCredentialBasedOnClientId(alternativePrefix));
        }

        addToChain(tokenCredentials, populateTokenCredentialBasedOnClientId(AZURE_CREDENTIAL_PREFIX));

        addToChain(tokenCredentials, defaultManagedIdentityCredential());

        return new ChainedTokenCredentialBuilder().addAll(tokenCredentials).build();
    }

    private void addToChain(List<TokenCredential> chain, TokenCredential tokenCredential) {
        if (tokenCredential != null) {
            chain.add(tokenCredential);
        }
    }

}

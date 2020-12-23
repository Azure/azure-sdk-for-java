// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DefaultSpringCredentialBuilder extends SpringCredentialBuilderBase<DefaultSpringCredentialBuilder> {

    /**
     * Defines the AZURE_CREDENTIAL_PREFIX.
     */
    static final String AZURE_CREDENTIAL_PREFIX = "azure.credential.";

    private String alternativePrefix;

    public DefaultSpringCredentialBuilder alternativePrfix(String alternative) {
        if (alternative != null) {
            this.alternativePrefix = alternative + (alternative.endsWith(".") ? "" : ".");
        }

        return this;
    }

    public TokenCredential build() {
        if (environment == null) {
            throw new IllegalArgumentException("To build a spring credential the environment must be set");
        }

        List<TokenCredential> tokenCredentials = new ArrayList<>();

        if (alternativePrefix != null) {
            tokenCredentials.add(populateTokenCredential(alternativePrefix));
        }

        tokenCredentials.add(populateDefaultTokenCredential());

        return new ChainedTokenCredentialBuilder().addAll(tokenCredentials).build();
    }

    private TokenCredential populateDefaultTokenCredential() {
        return populateTokenCredential(AZURE_CREDENTIAL_PREFIX);
    }

}

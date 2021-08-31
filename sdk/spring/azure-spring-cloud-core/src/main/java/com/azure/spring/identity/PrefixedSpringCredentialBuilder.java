// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import org.springframework.util.StringUtils;

/**
 * Spring token credential built from the prefixed properties.
 */
public class PrefixedSpringCredentialBuilder extends SpringCredentialBuilderBase<PrefixedSpringCredentialBuilder> {

    private String prefix;

    public PrefixedSpringCredentialBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Build a Spring token credential with a specific prefix.
     *
     * @return the token credential populated from the prefixed properties.
     * @throws IllegalArgumentException if no environment or prefix is set.
     */
    public TokenCredential build() {
        if (environment == null) {
            throw new IllegalArgumentException("To build a spring credential the environment must be set.");
        }

        if (StringUtils.isEmpty(prefix)) {
            throw new IllegalArgumentException("The prefix must be set.");
        }

        return populateTokenCredential(prefix);
    }

}

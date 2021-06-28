// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Spring token credential built from the prefixed properties.
 */
public class PrefixedSpringEnvironmentCredentialBuilder
    extends SpringCredentialBuilderBase<PrefixedSpringEnvironmentCredential> {

    private String prefix;

    public PrefixedSpringEnvironmentCredentialBuilder(Environment environment) {
        super(environment);
    }

    public PrefixedSpringEnvironmentCredentialBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Build a Spring token credential with a specific prefix.
     *
     * @return the token credential populated from the prefixed properties.
     * @throws IllegalArgumentException if no environment or prefix is set.
     */
    public PrefixedSpringEnvironmentCredential internalBuild() {
        Assert.notNull(prefix, "To build a PrefixedSpringEnvironmentCredential the prefix must be set.");

        return new PrefixedSpringEnvironmentCredential(environment, prefix, identityClientOptions);
    }


}

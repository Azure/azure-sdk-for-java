// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 *
 */
public abstract class SpringCredentialBuilderBase<T extends SpringCredentialBuilderBase<T>> {

    protected Environment environment;

    SpringCredentialBuilderBase(Environment environment) {
        Assert.notNull(environment, "To build a spring credential the environment must be set.");
        this.environment = environment;
    }

}

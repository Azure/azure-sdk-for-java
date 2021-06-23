// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import org.springframework.core.env.Environment;

public class SpringEnvrionmentCredentialBuilder
    extends SpringCredentialBuilderBase<SpringEnvrionmentCredentialBuilder> {

    public SpringEnvrionmentCredentialBuilder(Environment environment) {
        super(environment);
    }


}

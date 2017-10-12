/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthPool;

/**
 * Implementation of application gateway backend health information.
 */
@LangDefinition
public class ApplicationGatewayBackendHealthImpl implements ApplicationGatewayBackendHealth {

    private final ApplicationGatewayBackendHealthPool inner;

    ApplicationGatewayBackendHealthImpl(ApplicationGatewayBackendHealthPool inner) {
        this.inner = inner;
    }

    @Override
    public ApplicationGatewayBackendHealthPool inner() {
        return this.inner;
    }
}

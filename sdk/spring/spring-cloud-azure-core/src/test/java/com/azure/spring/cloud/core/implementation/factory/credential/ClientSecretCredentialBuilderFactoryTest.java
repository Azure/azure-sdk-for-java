// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.spring.cloud.core.properties.AzureProperties;

class ClientSecretCredentialBuilderFactoryTest extends AzureAadCredentialBuilderFactoryTest<
    ClientSecretCredentialBuilder,
    ClientSecretCredentialBuilderFactory> {

    @Override
    Class<ClientSecretCredentialBuilderFactory> getType() {
        return ClientSecretCredentialBuilderFactory.class;
    }

    @Override
    ClientSecretCredentialBuilderFactory createBuilderFactoryInstance(AzureProperties properties) {
        return new ClientSecretCredentialBuilderFactory(properties);
    }
}

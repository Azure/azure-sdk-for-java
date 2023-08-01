// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.core.properties.AzureProperties;

class UsernamePasswordCredentialBuilderFactoryTest extends AzureAadCredentialBuilderFactoryTest<
    UsernamePasswordCredentialBuilder,
    UsernamePasswordCredentialBuilderFactory> {

    @Override
    Class<UsernamePasswordCredentialBuilderFactory> getType() {
        return UsernamePasswordCredentialBuilderFactory.class;
    }

    @Override
    UsernamePasswordCredentialBuilderFactory createBuilderFactoryInstance(AzureProperties properties) {
        return new UsernamePasswordCredentialBuilderFactory(properties);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.spring.cloud.core.properties.AzureProperties;

class ClientCertificateCredentialBuilderFactoryTest extends AzureAadCredentialBuilderFactoryTest<
    ClientCertificateCredentialBuilder,
    ClientCertificateCredentialBuilderFactory> {

    @Override
    Class<ClientCertificateCredentialBuilderFactory> getType() {
        return ClientCertificateCredentialBuilderFactory.class;
    }

    @Override
    ClientCertificateCredentialBuilderFactory createBuilderFactoryInstance(AzureProperties properties) {
        return new ClientCertificateCredentialBuilderFactory(properties);
    }
}

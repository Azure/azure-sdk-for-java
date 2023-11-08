// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory.credential;

import com.azure.identity.AadCredentialBuilderBase;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AzureAadCredentialBuilderFactoryTest<
    B extends AadCredentialBuilderBase<B>,
    F extends AzureAadCredentialBuilderFactory<B>> {

    abstract Class<F> getType();

    abstract F createBuilderFactoryInstance(AzureProperties properties);

    @Test
    void usGovCloudAuthorityHostShouldApply() {
        AzureStorageQueueProperties properties = new AzureStorageQueueProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        F factory = createBuilderFactoryInstance(properties);
        B builder = factory.build();

        IdentityClientOptions identityClientOptions = getIdentityClientOptions(builder);
        assertEquals(AzureAuthorityHosts.AZURE_GOVERNMENT, identityClientOptions.getAuthorityHost());
    }

    private IdentityClientOptions getIdentityClientOptions(AadCredentialBuilderBase<B> builder) {
        return (IdentityClientOptions) ReflectionUtils.getField(AadCredentialBuilderBase.class,
            "identityClientOptions", builder);
    }

    static class AzureStorageQueueProperties extends AzureHttpSdkProperties {

    }

}

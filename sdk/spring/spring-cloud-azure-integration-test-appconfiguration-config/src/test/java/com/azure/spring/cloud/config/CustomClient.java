// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;

class CustomClient implements ConfigurationClientCustomizer, SecretClientCustomizer {

    private final TokenCredential credential = new DefaultAzureCredentialBuilder().build();

    private final PaginationPagePolicy pagePolicy;

    CustomClient(PaginationPagePolicy pagePolicy) {
        this.pagePolicy = pagePolicy;
    }

    @Override
    public void customize(ConfigurationClientBuilder builder, String endpoint) {
        builder.credential(credential);
        builder.addPolicy(pagePolicy);
    }

    @Override
    public void customize(SecretClientBuilder builder, String endpoint) {
        builder.credential(credential);
    }

}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration
public class AzureDefaultTokenCredentialAutoConfiguration {

    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springDefaultAzureCredential";

    @SuppressWarnings("rawtypes")
    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order()
    public TokenCredential azureTokenCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

}

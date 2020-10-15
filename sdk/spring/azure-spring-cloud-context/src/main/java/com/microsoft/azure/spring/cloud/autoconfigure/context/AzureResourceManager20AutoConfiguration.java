// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.autoconfigure.context;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.Azure;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.identity.spring.SpringEnvironmentTokenBuilder;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(Azure.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureResourceManager20AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public com.azure.resourcemanager.Azure.Authenticated azure20(TokenCredential tokenCredential,
            AzureProperties azureProperties) {
        AzureEnvironment legacyEnvironment = azureProperties.getEnvironment();
        com.azure.core.management.AzureEnvironment azureEnvironment = Arrays
                .stream(com.azure.core.management.AzureEnvironment.knownEnvironments())
                .filter(env -> env.getManagementEndpoint().equals(legacyEnvironment.managementEndpoint())).findFirst()
                .get();
        return com.azure.resourcemanager.Azure.authenticate(tokenCredential, new AzureProfile(azureEnvironment));
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential tokenCredential(AzureTokenCredentials credentials, Environment environment,
            AzureProperties azureProperties) {
        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder().fromEnvironment(environment);
        if (!StringUtils.isBlank(azureProperties.getCredentialFilePath())) {
            builder.overrideNamedCredential("", new LegacyTokenCredentialAdapter(credentials));
        }
        return builder.build();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.autoconfigure.context;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.identity.spring.SpringEnvironmentTokenBuilder;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureResourceManager20AutoConfiguration {

    private static Logger logger = LoggerFactory.getLogger(AzureResourceManager20AutoConfiguration.class);

    @Autowired(required = false)
    private AzureTokenCredentials credentials;

    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager.Authenticated azure20(TokenCredential tokenCredential,
            AzureProperties azureProperties) {
        AzureEnvironment legacyEnvironment = azureProperties.getEnvironment();
        com.azure.core.management.AzureEnvironment azureEnvironment = 
                com.azure.core.management.AzureEnvironment.knownEnvironments().stream()
                .filter(env -> env.getManagementEndpoint().equals(legacyEnvironment.managementEndpoint())).findFirst()
                .get();
        return AzureResourceManager.authenticate(tokenCredential, new AzureProfile(azureEnvironment));
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential tokenCredential(Environment environment, AzureProperties azureProperties) {
        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder().fromEnvironment(environment);
        if (!StringUtils.isBlank(azureProperties.getCredentialFilePath())) {
            if (credentials == null) {
                logger.error("Legacy azure credentials not initialized though credential-file-path was provided");
            }
            builder.overrideNamedCredential("", new LegacyTokenCredentialAdapter(credentials));
        }
        return builder.build();
    }
}

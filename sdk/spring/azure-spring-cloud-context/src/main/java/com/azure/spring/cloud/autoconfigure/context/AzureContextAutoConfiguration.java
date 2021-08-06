// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ResourceGroupManager;
import com.azure.spring.identity.DefaultSpringCredentialBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-config to provide default {@link CredentialsProvider} for all Azure services
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureContextAutoConfiguration {

    /**
     * Create an {@link AzureResourceManager} bean.
     *
     * @param credential The credential to connect to Azure.
     * @param profile The azure profile.
     * @return An AzureResourceManager object.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager azureResourceManager(TokenCredential credential, AzureProfile profile) {
        // TODO (xiada) Do we need to pass our User-Agent to with the management sdk?
        return AzureResourceManager.configure()
                                   .authenticate(credential, profile)
                                   .withDefaultSubscription();
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureProfile azureProfile(AzureProperties azureProperties) {
        return new AzureProfile(azureProperties.getTenantId(), azureProperties.getSubscriptionId(),
            azureProperties.getEnvironment().getAzureEnvironment());
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential credential(Environment environment) {
        return new DefaultSpringCredentialBuilder().environment(environment)
                                                   .alternativePrefix(AzureProperties.PREFIX)
                                                   .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public ResourceGroupManager resourceGroupManager(AzureResourceManager azureResourceManager,
                                                         AzureProperties azureProperties) {
        ResourceGroupManager resourceGroupManager = new ResourceGroupManager(azureResourceManager, azureProperties);
        if (azureProperties.isAutoCreateResources()
            && !resourceGroupManager.exists(azureProperties.getResourceGroup())) {
            resourceGroupManager.create(azureProperties.getResourceGroup());
        }
        return resourceGroupManager;
    }
}

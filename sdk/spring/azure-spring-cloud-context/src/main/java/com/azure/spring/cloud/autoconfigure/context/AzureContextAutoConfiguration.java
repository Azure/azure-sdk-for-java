// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.impl.ResourceGroupManager;
import com.azure.spring.identity.DefaultSpringCredentialBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
/**
 * Auto-config to provide default {@link CredentialsProvider} for all Azure services
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureContextProperties.class)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
@Import(AzureEnvironmentAutoConfiguration.class)
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
    public AzureProfile azureProfile(AzureContextProperties azureContextProperties,
                                     EnvironmentProvider environmentProvider) {
        return new AzureProfile(azureContextProperties.getTenantId(), azureContextProperties.getSubscriptionId(),
            environmentProvider.getEnvironment());
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential credential(Environment environment) {
        return new DefaultSpringCredentialBuilder().environment(environment)
                                                   .alternativePrefix(AzureContextProperties.PREFIX)
                                                   .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public ResourceGroupManager resourceGroupManager(AzureResourceManager azureResourceManager,
                                                         AzureContextProperties azureContextProperties) {
        ResourceGroupManager resourceGroupManager = new ResourceGroupManager(azureResourceManager, azureContextProperties);
        if (azureContextProperties.isAutoCreateResources()
            && !resourceGroupManager.exists(azureContextProperties.getResourceGroup())) {
            resourceGroupManager.create(azureContextProperties.getResourceGroup());
        }
        return resourceGroupManager;
    }
}

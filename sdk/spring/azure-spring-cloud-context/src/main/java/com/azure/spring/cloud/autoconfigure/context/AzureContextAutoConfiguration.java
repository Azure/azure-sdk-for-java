// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.identity.DefaultSpringCredential;
import com.azure.spring.identity.DefaultSpringCredentialBuilder;
import com.azure.spring.identity.PrefixedSpringEnvironmentCredentialBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

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
        return AzureResourceManager.configure().authenticate(credential, profile).withDefaultSubscription();
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
        final List<TokenCredential> tokenCredentials = ((DefaultSpringCredential) new DefaultSpringCredentialBuilder(
            environment).build()).getTokenCredentials();

        final TokenCredential prefixedTokenCredential = new PrefixedSpringEnvironmentCredentialBuilder(
            environment).prefix(AzureProperties.PREFIX).build();

        return new ChainedTokenCredentialBuilder().addAll(tokenCredentials).addFirst(prefixedTokenCredential).build();
    }
}

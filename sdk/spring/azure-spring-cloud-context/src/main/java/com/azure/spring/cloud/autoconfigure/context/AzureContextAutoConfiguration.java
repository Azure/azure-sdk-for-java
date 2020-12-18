// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.telemetry.SubscriptionSupplier;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.DefaultCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    private static final String PROJECT_VERSION = AzureContextAutoConfiguration.class.getPackage()
                                                                                     .getImplementationVersion();
    private static final String SPRING_CLOUD_USER_AGENT = "spring-cloud-azure/" + PROJECT_VERSION;


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
        // TODO (xiada) USER AGENT
        return AzureResourceManager.configure()
//                                   .withLogLevel(HttpLogDetailLevel.BASIC)
                                   .authenticate(credential, profile)
                                   .withDefaultSubscription();
    }
//
//    @VisibleForTesting
//    protected Azure authenticateToAzure(RestClient restClient, String subscriptionId,
//                                        AzureTokenCredentials credentials) {
//        return Azure.authenticate(restClient, credentials.domain()).withSubscription(subscriptionId);
//    }

    @Bean
    @ConditionalOnMissingBean
    public AzureProfile azureProfile(AzureProperties azureProperties) {
        return new AzureProfile(azureProperties.getTenantId(), azureProperties.getSubscriptionId(),
            azureProperties.getEnvironment().getAzureEnvironment());
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential credential(AzureProperties azureProperties) {
        CredentialsProvider credentialsProvider = new DefaultCredentialsProvider(azureProperties);
        return credentialsProvider.getCredential();

        // TODO (xiada) combine two credential methods
/*
        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder().fromEnvironment(environment);
        if (!StringUtils.isBlank(azureProperties.getCredentialFilePath())) {
            if (credentials == null) {
                LOGGER.error("Legacy azure credentials not initialized though credential-file-path was provided");
            }
            builder.overrideNamedCredential("", new LegacyTokenCredentialAdapter(credentials));
        }
        return builder.build();*/
    }

    @Bean
    @ConditionalOnBean(AzureResourceManager.class)
    public SubscriptionSupplier subscriptionSupplier(AzureResourceManager azureResourceManager) {
        return azureResourceManager::subscriptionId;
    }

}

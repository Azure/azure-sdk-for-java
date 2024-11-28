// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.scheduler.Schedulers;

import static org.springframework.util.StringUtils.hasText;


/**
 * {@link EnableAutoConfiguration} will enable the autoconfiguration classes
 * {@link SpringBootConfiguration} will enable find configuration classes with
 * {@link org.springframework.context.annotation.Configuration} and
 * {@link org.springframework.boot.test.context.TestConfiguration}
 */
@EnableAutoConfiguration
@SpringBootConfiguration
public class ApplicationConfiguration {

    @Primary
    @Bean
    TokenCredential integrationTestTokenCredential() {
        ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
            .addLast(new EnvironmentCredentialBuilder().build())
            .addLast(new AzureCliCredentialBuilder().build())
            .addLast(new AzureDeveloperCliCredentialBuilder().build());
        TokenCredential createAzurePipelinesCredential = createAzurePipelinesCredential();
        if (createAzurePipelinesCredential != null) {
            builder.addLast(createAzurePipelinesCredential);
        }
        builder.addLast(new AzurePowerShellCredentialBuilder().build());
        return builder.build();
    }

    private TokenCredential createAzurePipelinesCredential() {
        Configuration config = Configuration.getGlobalConfiguration();
        String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
        String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
        String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");
        if (hasText(serviceConnectionId)
            && hasText(clientId)
            && hasText(tenantId)
            && hasText(systemAccessToken)) {
            AzurePipelinesCredential pipelinesCredential = new AzurePipelinesCredentialBuilder()
                .systemAccessToken(systemAccessToken)
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .build();
            return request -> pipelinesCredential.getToken(request).subscribeOn(Schedulers.boundedElastic());
        }
        return null;
    }

}

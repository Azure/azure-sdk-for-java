// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.useragent.http;

import ch.qos.logback.classic.Level;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
class IdentityUserAgentTests {

    @Test
    void userAgentTest(CapturedOutput output) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class
            ))
            .withUserConfiguration(CredentialCustomizerConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=sample",
                "spring.cloud.azure.credential.client-id=sample",
                "spring.cloud.azure.credential.client-secret=sample",
                "spring.cloud.azure.retry.fixed.delay=1",
                "spring.cloud.azure.retry.fixed.max-retries=0",
                "spring.cloud.azure.retry.mode=fixed"
            )
            .run(context -> {
                // see https://github.com/Azure/azure-sdk-for-java/blob/c8aecbf72f6826de8e465bdf6ae1ddcf2d09fe4e/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/HttpLoggingPolicy.java#L456
                ch.qos.logback.classic.Logger httpLoggingPolicyLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("");
                httpLoggingPolicyLogger.setLevel(Level.DEBUG);

                assertThat(context).hasSingleBean(TokenCredential.class);

                TokenCredential tokenCredential = context.getBean(TokenCredential.class);
                try {
                    final TokenRequestContext request = new TokenRequestContext();
                    request.setScopes(Arrays.asList("https://servicebus.azure.net/.default"));
                    tokenCredential.getTokenSync(request);

                } catch (Exception exception) {
                    // Eat it because we just want the log.
                }
                String allOutput = output.getAll();
                String format1 = String.format("User-Agent:%s", AzureSpringIdentifier.AZURE_SPRING_IDENTITY);
                String format2 = String.format("\"User-Agent\":\"%s", AzureSpringIdentifier.AZURE_SPRING_IDENTITY);
                assertTrue(allOutput.contains(format1) || allOutput.contains(format2));
            });
    }

    static class CredentialCustomizerConfiguration {

        @Bean
        AzureServiceClientBuilderCustomizer<ClientSecretCredentialBuilder> httpLogOptionsCustomizer() {
            return builder -> {
                final HttpLogOptions httpLogOptions = new HttpLogOptions();
                httpLogOptions.setLogLevel(HttpLogDetailLevel.HEADERS);
                Set<String> allowedHeaderNames = new HashSet<>();
                allowedHeaderNames.add("User-Agent");
                httpLogOptions.setAllowedHeaderNames(allowedHeaderNames);
                builder.httpLogOptions(httpLogOptions);
            };
        }
    }


}

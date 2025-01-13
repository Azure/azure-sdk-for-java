// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AzureKeyVaultSecretAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    SecretClientBuilderFactory, AzureKeyVaultSecretProperties> {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureKeyVaultSecretAutoConfiguration.class));

    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        return this.contextRunner
            .withPropertyValues("spring.cloud.azure.keyvault.secret.endpoint=" + String.format(ENDPOINT, "mykv"));
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureKeyVaultSecretProperties.PREFIX;
    }

    @Override
    protected Class<SecretClientBuilderFactory> getBuilderFactoryType() {
        return SecretClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureKeyVaultSecretProperties> getConfigurationPropertiesType() {
        return AzureKeyVaultSecretProperties.class;
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void withoutSecretClientBuilderShouldNotConfigure(String endpointProperty) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(SecretClientBuilder.class))
            .withPropertyValues(endpointProperty + "=" + String.format(ENDPOINT, "mykv"))
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void disableKeyVaultSecretShouldNotConfigure(String endpointProperty) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.secret.enabled=false",
                endpointProperty + "=" + String.format(ENDPOINT, "mykv")
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void disableKeyVaultShouldNotConfigure(String endpointProperty) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.enabled=false",
                endpointProperty + "=" + String.format(ENDPOINT, "mykv")
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @Test
    void withoutVaultEndpointShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void withVaultEndpointShouldConfigure(String endpointProperty) {
        this.contextRunner
            .withPropertyValues(endpointProperty + "=" + String.format(ENDPOINT, "mykv"))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultSecretAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSecretProperties.class);
                assertThat(context).hasSingleBean(SecretClient.class);
                assertThat(context).hasSingleBean(SecretAsyncClient.class);
                assertThat(context).hasSingleBean(SecretClientBuilder.class);
                assertThat(context).hasSingleBean(SecretClientBuilderFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void customizerShouldBeCalled(String endpointProperty) {
        SecretBuilderCustomizer customizer = new SecretBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(endpointProperty + "=" + String.format(ENDPOINT, "mykv"))
            .withBean("customizer1", SecretBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", SecretBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.keyvault.secret.endpoint", "spring.cloud.azure.keyvault.endpoint" })
    void otherCustomizerShouldNotBeCalled(String endpointProperty) {
        SecretBuilderCustomizer customizer = new SecretBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(endpointProperty + "="  + String.format(ENDPOINT, "mykv"))
            .withBean("customizer1", SecretBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", SecretBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String endpoint = String.format(ENDPOINT, "mykv");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.secret.endpoint=" + endpoint,
                "spring.cloud.azure.keyvault.secret.service-version=V7_2",
                "spring.cloud.azure.keyvault.secret.challenge-resource-verification-enabled=false",

                "spring.cloud.azure.keyvault.secret.property-source-enabled=false",
                "spring.cloud.azure.keyvault.secret.property-sources[0].endpoint=" + endpoint + "-1",
                "spring.cloud.azure.keyvault.secret.property-sources[0].service-version=V7_0",
                "spring.cloud.azure.keyvault.secret.property-sources[0].name=property-source-1",
                "spring.cloud.azure.keyvault.secret.property-sources[0].case-sensitive=false",
                "spring.cloud.azure.keyvault.secret.property-sources[0].secret-keys=a,b,c",
                "spring.cloud.azure.keyvault.secret.property-sources[0].refresh-interval=5m"

            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultSecretProperties.class);
                AzureKeyVaultSecretProperties properties = context.getBean(AzureKeyVaultSecretProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertFalse(properties.isPropertySourceEnabled());
                assertEquals(SecretServiceVersion.V7_2, properties.getServiceVersion());
                assertFalse(properties.isChallengeResourceVerificationEnabled());

                AzureKeyVaultPropertySourceProperties propertySourceProperties = properties.getPropertySources().get(0);
                assertEquals(endpoint + "-1", propertySourceProperties.getEndpoint());
                assertEquals(SecretServiceVersion.V7_0, propertySourceProperties.getServiceVersion());
                assertEquals("property-source-1", propertySourceProperties.getName());
                assertFalse(propertySourceProperties.isCaseSensitive());
                assertEquals(Arrays.asList("a", "b", "c"), propertySourceProperties.getSecretKeys());
                assertEquals(Duration.ofMinutes(5), propertySourceProperties.getRefreshInterval());

            });
    }

    private static class SecretBuilderCustomizer extends TestBuilderCustomizer<SecretClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}

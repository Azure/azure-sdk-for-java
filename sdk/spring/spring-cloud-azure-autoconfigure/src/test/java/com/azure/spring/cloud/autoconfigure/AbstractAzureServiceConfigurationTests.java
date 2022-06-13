// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.implementation.IdentityClient;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractAzureServiceConfigurationTests<T extends AbstractAzureServiceClientBuilderFactory<?>,
    P extends AzureProperties> {

    protected abstract ApplicationContextRunner getMinimalContextRunner();

    protected abstract String getPropertyPrefix();

    protected abstract Class<T> getBuilderFactoryType();

    protected abstract Class<P> getConfigurationPropertiesType();

    @Test
    protected void usGovCloudShouldWorkWithClientSecretCredential() {
        getMinimalContextRunner()
            .withPropertyValues(
                getPropertyPrefix() + ".profile.cloud-type=AZURE_US_GOVERNMENT",
                getPropertyPrefix() + ".profile.tenant-id=test-tenant-id",
                getPropertyPrefix() + ".credential.client-id=test-client-id",
                getPropertyPrefix() + ".credential.client-secret=test-client-secret"
            )
            .withConfiguration(AutoConfigurations.of(
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class
            ))
            .run(context -> {
                assertSovereignCloudsSetInCredential(context, ClientSecretCredential.class);
            });
    }

    @Test
    protected void usGovCloudShouldWorkWithClientCertificateCredential() {
        getMinimalContextRunner()
            .withPropertyValues(
                getPropertyPrefix() + ".profile.cloud-type=AZURE_US_GOVERNMENT",
                getPropertyPrefix() + ".profile.tenant-id=test-tenant-id",
                getPropertyPrefix() + ".credential.client-id=test-client-id",
                getPropertyPrefix() + ".credential.client-certificate-path=test-client-cert-path"
            )
            .withConfiguration(AutoConfigurations.of(
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class
            ))
            .run(context -> {
                assertSovereignCloudsSetInCredential(context, ClientCertificateCredential.class);
            });
    }

    @Test
    protected void usGovCloudShouldWorkWithUsernamePasswordCredential() {
        getMinimalContextRunner()
            .withPropertyValues(
                getPropertyPrefix() + ".profile.cloud-type=AZURE_US_GOVERNMENT",
                getPropertyPrefix() + ".credential.client-id=test-client-id",
                getPropertyPrefix() + ".credential.username=test-username",
                getPropertyPrefix() + ".credential.password=test-password"
            )
            .withConfiguration(AutoConfigurations.of(
                AzureTokenCredentialAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class
            ))
            .run(context -> {
                assertSovereignCloudsSetInCredential(context, UsernamePasswordCredential.class);
            });
    }

    private <C> void assertSovereignCloudsSetInCredential(AssertableApplicationContext context, Class<C> credentialType) {
        assertThat(context).hasSingleBean(getBuilderFactoryType());
        T builderFactory = context.getBean(getBuilderFactoryType());

        assertThat(context).hasSingleBean(getConfigurationPropertiesType());
        P properties = context.getBean(getConfigurationPropertiesType());

        AzureTokenCredentialResolver tokenCredentialResolver = getAzureTokenCredentialResolver(builderFactory);

        TokenCredential tokenCredential = tokenCredentialResolver.resolve(properties);
        Assertions.assertInstanceOf(credentialType, tokenCredential);

        IdentityClient identityClient = getIdentityClient(tokenCredential);
        Assertions.assertEquals(AzureAuthorityHosts.AZURE_GOVERNMENT, identityClient.getIdentityClientOptions().getAuthorityHost());
    }

    private AzureTokenCredentialResolver getAzureTokenCredentialResolver(T builderFactory) {
        return (AzureTokenCredentialResolver) ReflectionUtils.getField(getBuilderFactoryType(),
            "tokenCredentialResolver", builderFactory);
    }

    private IdentityClient getIdentityClient(TokenCredential credential) {
        return (IdentityClient) ReflectionUtils.getField(credential.getClass(), "identityClient", credential);
    }

}

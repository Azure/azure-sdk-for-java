// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.implementation.IdentityClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;
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
                getPropertyPrefix() + ".profile.tenant-id=fake-tenant-id",
                getPropertyPrefix() + ".credential.client-id=fakeClientIdPlaceholder",
                getPropertyPrefix() + ".credential.client-secret=fake-client-secret"
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
                getPropertyPrefix() + ".profile.tenant-id=fake-tenant-id",
                getPropertyPrefix() + ".credential.client-id=fakeClientIdPlaceholder",
                getPropertyPrefix() + ".credential.client-certificate-path=fake-client-cert-path"
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
                getPropertyPrefix() + ".credential.client-id=fakeClientIdPlaceholder",
                getPropertyPrefix() + ".credential.username=fakeNamePlaceholder",
                getPropertyPrefix() + ".credential.password=fakePasswordPlaceholder"
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
        Assertions.assertTrue(credentialType.isAssignableFrom(tokenCredential.getClass()));

        IdentityClient identityClient = getIdentityClient(tokenCredential);
        Assertions.assertEquals(AzureAuthorityHosts.AZURE_GOVERNMENT, identityClient.getIdentityClientOptions().getAuthorityHost());
    }

    private AzureTokenCredentialResolver getAzureTokenCredentialResolver(T builderFactory) {
        return (AzureTokenCredentialResolver) getField(getBuilderFactoryType(),
                "tokenCredentialResolver", builderFactory);
    }

    private IdentityClient getIdentityClient(TokenCredential credential) {
        return (IdentityClient) getField(credential.getClass(), "identityClient", credential);
    }

}

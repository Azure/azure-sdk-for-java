package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureNamedKeyCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureNamedKeyCredentialResolver;

import java.util.function.Consumer;

public class NamedKeyAuthenticationDescriptor implements AuthenticationDescriptor<AzureNamedKeyCredentialProvider> {

    private final Consumer<AzureNamedKeyCredentialProvider> consumer;

    public NamedKeyAuthenticationDescriptor(Consumer<AzureNamedKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return AzureCredentialType.KEY_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureNamedKeyCredentialProvider> azureCredentialResolver() {
        return new AzureNamedKeyCredentialResolver();
    }

    @Override
    public Consumer<AzureNamedKeyCredentialProvider> consumer() {
        return consumer;
    }
}

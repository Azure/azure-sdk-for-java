package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureSasCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureSasCredentialResolver;

import java.util.function.Consumer;

public class SasAuthenticationDescriptor implements AuthenticationDescriptor<AzureSasCredentialProvider> {

    private final Consumer<AzureSasCredentialProvider> consumer;

    public SasAuthenticationDescriptor(Consumer<AzureSasCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return AzureCredentialType.SAS_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureSasCredentialProvider> azureCredentialResolver() {
        return new AzureSasCredentialResolver();
    }

    @Override
    public Consumer<AzureSasCredentialProvider> consumer() {
        return consumer;
    }
}

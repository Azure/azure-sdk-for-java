package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureConnectionStringProvider;
import com.azure.spring.core.credential.resolver.AzureConnectionStringResolver;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;

import java.util.function.Consumer;

import static com.azure.spring.core.credential.AzureCredentialType.CONNECTION_STRING_CREDENTIAL;

public class ConnectionStringAuthenticationDescriptor implements AuthenticationDescriptor<AzureConnectionStringProvider> {

    private final Consumer<AzureConnectionStringProvider> consumer;

    public ConnectionStringAuthenticationDescriptor(Consumer<AzureConnectionStringProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return CONNECTION_STRING_CREDENTIAL;
    }

    @Override
    public AzureCredentialResolver<AzureConnectionStringProvider> azureCredentialResolver() {
        return new AzureConnectionStringResolver();
    }

    @Override
    public Consumer<AzureConnectionStringProvider> consumer() {
        return consumer;
    }
}

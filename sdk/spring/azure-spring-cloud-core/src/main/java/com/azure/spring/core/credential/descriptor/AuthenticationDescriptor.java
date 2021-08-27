package com.azure.spring.core.credential.descriptor;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;

import java.util.function.Consumer;

/**
 * Describe the azure credential authentication.
 */
public interface AuthenticationDescriptor<T extends AzureCredentialProvider<?>> {

    AzureCredentialType azureCredentialType();

    AzureCredentialResolver<T> azureCredentialResolver();

    Consumer<T> consumer();
}

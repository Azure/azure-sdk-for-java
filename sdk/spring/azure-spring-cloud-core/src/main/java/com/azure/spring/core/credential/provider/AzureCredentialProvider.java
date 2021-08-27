package com.azure.spring.core.credential.provider;

import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Azure credential provider interface to unify all the credential cases.
 * @param <T> The actual credential instance which azure SDKs used.
 */
public interface AzureCredentialProvider<T> {

    AzureCredentialType getType();

    T getCredential();
}

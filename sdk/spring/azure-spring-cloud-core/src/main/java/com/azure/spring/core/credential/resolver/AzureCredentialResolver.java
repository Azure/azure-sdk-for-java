package com.azure.spring.core.credential.resolver;

import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Resolver interface to resolve an azure credential provider implementation,
 * the method {@link AzureCredentialResolver#resolve(AzureProperties)}
 * returns null if the related properties does not match.
 * @param <T> Azure credential implementation
 */
public interface AzureCredentialResolver<T extends AzureCredentialProvider<?>> {

    T resolve(AzureProperties properties);

    boolean isResolvable(AzureProperties properties);

}

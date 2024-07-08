// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.client.traits;

import com.azure.core.v2.credential.AzureKeyCredential;

/**
 * An {@link com.azure.core.client.traits Azure SDK for Java trait} providing a consistent interface for setting
 * {@link AzureKeyCredential}. Refer to the Azure SDK for Java
 * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
 * documentation for more details on proper usage of the {@link AzureKeyCredential} type.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 * @see com.azure.core.client.traits
 * @see AzureKeyCredential
 */
public interface AzureKeyCredentialTrait<T extends AzureKeyCredentialTrait<T>> {
    /**
     * Sets the {@link AzureKeyCredential} used for authentication. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link AzureKeyCredential} type.
     *
     * @param credential the {@link AzureKeyCredential} to be used for authentication.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(AzureKeyCredential credential);
}

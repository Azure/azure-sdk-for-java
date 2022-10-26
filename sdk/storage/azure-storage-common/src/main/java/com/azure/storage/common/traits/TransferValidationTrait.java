// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.traits;

import com.azure.storage.common.TransferValidationOptions;

/**
 * An Azure SDK for Java trait providing a consistent interface for setting AzureNamedKeyCredential. Refer to the Azure
 * SDK for Java identity and authentication  documentation for more details on proper usage of the
 * AzureNamedKeyCredential type.
 *
 * @see com.azure.core.client.traits
 * @see TransferValidationOptions
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 *            return the concrete type, rather than the trait type.
 */
public interface TransferValidationTrait<T extends TransferValidationTrait<T>> {
    /**
     * Sets the {@link TransferValidationOptions} used for objet content transfers by the client and its derived
     * clients.
     *
     * @param validationOptions The validation options for a client to use by default.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T transferValidationOptions(TransferValidationOptions validationOptions);
}

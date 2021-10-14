// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.customizer;

/**
 * A service client builder customizer that performs no operation.
 *
 * @param <T> The type of the Azure service client builder.
 */
public class NoOpAzureServiceClientBuilderCustomizer<T> implements AzureServiceClientBuilderCustomizer<T> {

    @Override
    public void customize(Object builder) {
        // no-op
    }
}

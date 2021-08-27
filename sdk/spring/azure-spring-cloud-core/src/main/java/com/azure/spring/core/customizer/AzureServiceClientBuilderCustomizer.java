// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.customizer;

/**
 * Customizer of an Azure service client builder.
 *
 * @param <T> The type of the service client builder.
 */
public interface AzureServiceClientBuilderCustomizer<T> {

    void customize(T builder);

}

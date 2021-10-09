// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

/**
 * Azure SDK service client builder factory of all modules.
 *
 * @param <T> Type of the service client builder
 */
public interface AzureServiceClientBuilderFactory<T> {

    T build();
}




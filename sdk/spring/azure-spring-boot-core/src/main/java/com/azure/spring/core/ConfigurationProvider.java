// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

/**
 * Configuration provider for Azure clients.
 */
public interface ConfigurationProvider<T, C> {

    C getConfiguration (T properties);

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.util.Configuration;

/**
 * The interface for client builders that support a {@link Configuration}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface ConfigurationTrait<TBuilder extends ConfigurationTrait<TBuilder>> {
    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated TBuilder object
     */
    TBuilder configuration(Configuration configuration);
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.util.ClientOptions;

/**
 * The interface for client builders that support a {@link com.azure.core.util.ClientOptions}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface ClientOptionsTrait<TBuilder extends ClientOptionsTrait<TBuilder>> {
    /**
     * Sets the client options for all the requests made through the client.
     *
     * @param clientOptions {@link ClientOptions}.
     * @return the updated {@code TBuilder} object.
     */
    TBuilder clientOptions(ClientOptions clientOptions);
}

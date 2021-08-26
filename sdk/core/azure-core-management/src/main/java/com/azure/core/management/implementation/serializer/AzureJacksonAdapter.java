// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.serializer;

import com.azure.core.util.serializer.JacksonAdapter;

/**
 * A serialization helper class overriding {@link JacksonAdapter} with extra
 * functionality useful for Azure operations.
 */
public final class AzureJacksonAdapter extends JacksonAdapter {
    /**
     * Creates an instance of the Azure flavored Jackson adapter.
     */
    public AzureJacksonAdapter() {
        super((mapper, innerMapper) -> mapper.registerModule(ManagementErrorDeserializer.getModule(innerMapper)));
    }
}

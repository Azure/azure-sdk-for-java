// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.serializer;

import com.azure.core.management.implementation.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/**
 * Factory to create SerializerAdapter for Azure resource manager (ARM).
 */
public final class SerializerFactory {

    private static SerializerAdapter serializerAdapter;

    private SerializerFactory() {
    }

    /**
     * Gets the singleton instance of the default management serializer adapter.
     *
     * @return the default management serializer adapter.
     */
    public static synchronized SerializerAdapter createDefaultManagementSerializerAdapter() {
        if (serializerAdapter == null) {
            serializerAdapter = new AzureJacksonAdapter();
        }
        return serializerAdapter;
    }
}

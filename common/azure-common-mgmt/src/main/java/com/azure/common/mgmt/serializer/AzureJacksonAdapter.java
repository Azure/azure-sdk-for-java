/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt.serializer;

import com.azure.common.implementation.serializer.SerializerAdapter;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;

/**
 * A serialization helper class overriding {@link JacksonAdapter} with extra
 * functionality useful for Azure operations.
 */
public final class AzureJacksonAdapter extends JacksonAdapter implements SerializerAdapter {
    /**
     * Creates an instance of the Azure flavored Jackson adapter.
     */
    public AzureJacksonAdapter() {
        super();
        serializer().registerModule(CloudErrorDeserializer.getModule(simpleMapper()));
    }
}

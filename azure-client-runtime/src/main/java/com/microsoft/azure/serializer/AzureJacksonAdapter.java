/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.FlatteningDeserializer;
import com.microsoft.rest.serializer.FlatteningSerializer;
import com.microsoft.rest.serializer.JacksonAdapter;

/**
 * A serialization helper class overriding {@link JacksonAdapter} with extra
 * functionality useful for Azure operations.
 */
public final class AzureJacksonAdapter extends JacksonAdapter implements SerializerAdapter<ObjectMapper> {
    @Override
    public ObjectMapper serializer() {
        return mapper().registerModule(CloudErrorDeserializer.getModule(simpleMapper()));
    }
}

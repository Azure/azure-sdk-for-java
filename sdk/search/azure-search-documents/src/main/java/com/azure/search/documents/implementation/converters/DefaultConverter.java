// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import java.io.IOException;

class DefaultConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultConverter.class);

    public static <I, O> I convert(O obj, Class<I> clazz) {
        JacksonAdapter adapter = new JacksonAdapter();
        try {
            String jsonString = adapter.serialize(obj, SerializerEncoding.JSON);
            return new JacksonAdapter().deserialize(jsonString, clazz, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Something wrong with conversion."));
        }

        //return I.setName(obj.getName());
    }
}

/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.protocol.SerializerAdapter;

/**
 * A serialization helper class overriding {@link JacksonAdapter} with configurations
 * compatible with AutoRest generated clients.
 */
public class SimpleJacksonAdapter extends JacksonAdapter implements SerializerAdapter<ObjectMapper> {
    /**
     * Gets a static instance of {@link ObjectMapper}.
     *
     * @return an instance of {@link ObjectMapper}.
     */
    public ObjectMapper serializer() {
        return mapper();
    }
}

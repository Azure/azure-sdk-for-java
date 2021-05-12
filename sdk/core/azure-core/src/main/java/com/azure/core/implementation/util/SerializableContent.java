// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.RequestOutbound;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * A {@link RequestContent} implementation which is backed by a serializable object.
 */
public final class SerializableContent implements RequestContent {
    private final ClientLogger logger = new ClientLogger(SerializableContent.class);

    private final Object serializable;
    private final ObjectSerializer objectSerializer;

    /**
     * Creates a new instance of {@link SerializableContent}.
     *
     * @param serializable The serializable {@link Object} content.
     * @param objectSerializer The {@link ObjectSerializer} that will serialize the {@link Object} content.
     */
    public SerializableContent(Object serializable, ObjectSerializer objectSerializer) {
        this.serializable = serializable;
        this.objectSerializer = objectSerializer;
    }

    @Override
    public void writeTo(RequestOutbound requestOutbound) {
        try {
            requestOutbound.getRequestChannel().write(ByteBuffer.wrap(objectSerializer.serializeToBytes(serializable)));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Long getLength() {
        return null;
    }
}

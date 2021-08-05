// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.converter;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * A converter to turn the payload of a {@link Message} from serialized form to a typed
 * Object and vice versa.
 *
 * @param <I> The Azure message type when sending to the broker using Azure SDK.
 * @param <O> The Azure message type when receiving from the broker using Azure SDK.
 */
public interface AzureMessageConverter<I, O> {

    /**
     * Convert the payload of a {@link Message} from a serialized form to a typed Object
     * of the specified target class.
     *
     * @param message the input message
     * @param targetClass the target class for the conversion
     * @return the result of the conversion, or {@code null} if the converter cannot
     * perform the conversion
     */
    @Nullable
    O fromMessage(Message<?> message, Class<O> targetClass);

    /**
     * Create a {@link Message} whose payload is the result of converting the given
     * payload Object to serialized form. The optional {@link MessageHeaders} parameter
     * may contain additional headers to be added to the message.
     * @param azureMessage the Object to convert
     * @param headers optional headers for the message
     * @param targetPayloadClass the target payload class for the conversion
     * @param <U> payload class type in message
     * @return the new message, or {@code null} if the converter does not support the
     *      * Object type or the target media type
     */
    @Nullable
    <U> Message<U> toMessage(I azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass);

    @Nullable
    default <U> Message<U> toMessage(I azureMessage, Class<U> targetPayloadClass) {
        return this.toMessage(azureMessage, new HashMap<>(), targetPayloadClass);
    }
}

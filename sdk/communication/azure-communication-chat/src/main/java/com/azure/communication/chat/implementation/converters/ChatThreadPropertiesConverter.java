// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatThreadProperties;
import java.util.Map;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatThreadProperties} and
 * {@link ChatThreadProperties}.
 */
public final class ChatThreadPropertiesConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatThreadProperties} to {@link ChatThreadProperties}.
     */
    public static ChatThreadProperties
        convert(com.azure.communication.chat.implementation.models.ChatThreadProperties obj) {
        if (obj == null) {
            return null;
        }

        ChatThreadProperties chatThreadProperties
            = new ChatThreadProperties().setId(obj.getId()).setTopic(obj.getTopic()).setCreatedOn(obj.getCreatedOn());

        if (obj.getCreatedByCommunicationIdentifier() != null) {
            chatThreadProperties
                .setCreatedBy(CommunicationIdentifierConverter.convert(obj.getCreatedByCommunicationIdentifier()));
        }

        // Map metadata if present.
        Map<String, String> metadata = obj.getMetadata();
        if (metadata != null) {
            chatThreadProperties.setMetadata(metadata);
        }

        // Map retention policy.
        com.azure.communication.chat.implementation.models.ChatRetentionPolicy implPolicy = obj.getRetentionPolicy();
        if (implPolicy != null) {
            chatThreadProperties.setRetentionPolicy(ChatRetentionPolicyConverter.convertFromImpl(implPolicy));
        }

        return chatThreadProperties;
    }

    private ChatThreadPropertiesConverter() {
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatRetentionPolicy;
import com.azure.communication.chat.models.NoneRetentionPolicy;
import com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatRetentionPolicy} and
 * {@link ChatRetentionPolicy}.
 */
public final class ChatRetentionPolicyConverter {
    /**
     * Maps from the implementation {@link com.azure.communication.chat.implementation.models.ChatRetentionPolicy}
     * to the public {@link ChatRetentionPolicy} model.
     *
     * @param obj the implementation model to convert
     * @return a {@link ChatRetentionPolicy} instance, or null if the input is null or not a compatible type
     */
    public static ChatRetentionPolicy convertFromImpl(Object obj) {
        if (!(obj instanceof com.azure.communication.chat.implementation.models.ChatRetentionPolicy)) {
            return null;
        }

        ChatRetentionPolicy chatRetentionPolicy;
        if (obj instanceof com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy) {
            com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy
                = (com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy) obj;
            chatRetentionPolicy = new ThreadCreationDateRetentionPolicy(
                basedOnThreadCreationDateRetentionPolicy.getDeleteThreadAfterDays());
        } else {
            chatRetentionPolicy = new NoneRetentionPolicy();
        }
        return chatRetentionPolicy;
    }

    /**
     * Maps from the public {@link ChatRetentionPolicy} model
     * to the implementation {@link com.azure.communication.chat.implementation.models.ChatRetentionPolicy} type.
     *
     * @param obj the public model to convert
     * @return an implementation model instance, or null if the input is null or not a compatible type
     */
    public static com.azure.communication.chat.implementation.models.ChatRetentionPolicy convertToImpl(Object obj) {
        if (!(obj instanceof ChatRetentionPolicy)) {
            return null;
        }

        com.azure.communication.chat.implementation.models.ChatRetentionPolicy retentionPolicy;
        if (obj instanceof ThreadCreationDateRetentionPolicy) {
            ThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy
                = (ThreadCreationDateRetentionPolicy) obj;
            retentionPolicy = new com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy()
                .setDeleteThreadAfterDays(basedOnThreadCreationDateRetentionPolicy.getDeleteThreadAfterDays());
        } else {
            retentionPolicy = new com.azure.communication.chat.implementation.models.NoneRetentionPolicy();
        }
        return retentionPolicy;
    }

    // Prevent instantiation
    private ChatRetentionPolicyConverter() {
    }
}

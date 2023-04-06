// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatRetentionPolicy;
import com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatRetentionPolicy} and
 * {@link ChatRetentionPolicy}.
 */
public final class RetentionPolicyConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatRetentionPolicy} to {@link ChatRetentionPolicy}.
     */
    public static ChatRetentionPolicy convertFromImpl(Object obj) {
        if (obj == null || obj instanceof ChatRetentionPolicy == false) {
            return null;
        }

        ChatRetentionPolicy chatRetentionPolicy;
        if (obj instanceof com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy) {
            com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy =
                (com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy) obj;
            chatRetentionPolicy = new ThreadCreationDateRetentionPolicy().setDeleteThreadAfterDays(basedOnThreadCreationDateRetentionPolicy.getDeleteThreadAfterDays());
        } else {
            chatRetentionPolicy = new ChatRetentionPolicy();
        }
        return chatRetentionPolicy;
    }

    /**
     * Maps from {ChatRetentionPolicy} to {@link com.azure.communication.chat.implementation.models.ChatRetentionPolicy}.
     */
    public static com.azure.communication.chat.implementation.models.ChatRetentionPolicy convertToImpl(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof ChatRetentionPolicy == false) {
            return null;
        }

        com.azure.communication.chat.implementation.models.ChatRetentionPolicy retentionPolicy;
        if (obj instanceof ThreadCreationDateRetentionPolicy) {
            ThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy = (ThreadCreationDateRetentionPolicy) obj;
            retentionPolicy = new com.azure.communication.chat.implementation.models.ThreadCreationDateRetentionPolicy()
                .setDeleteThreadAfterDays(basedOnThreadCreationDateRetentionPolicy.getDeleteThreadAfterDays());
        } else {
            retentionPolicy = new com.azure.communication.chat.implementation.models.ChatRetentionPolicy();
        }
        return retentionPolicy;
    }

    private RetentionPolicyConverter() {
    }
}

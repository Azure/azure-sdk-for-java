// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.BasedOnThreadCreationDateRetentionPolicy;
import com.azure.communication.chat.models.RetentionPolicy;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.RetentionPolicy} and
 * {@link com.azure.communication.chat.models.RetentionPolicy}.
 */
public final class RetentionPolicyConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.RetentionPolicy} to {@link RetentionPolicy}.
     */
    public static RetentionPolicy convertFromImpl(Object obj) {
        if (obj == null || obj instanceof RetentionPolicy == false) {
            return null;
        }

        RetentionPolicy retentionPolicy;
        if (obj instanceof com.azure.communication.chat.implementation.models.BasedOnThreadCreationDateRetentionPolicy) {
            com.azure.communication.chat.implementation.models.BasedOnThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy =
                (com.azure.communication.chat.implementation.models.BasedOnThreadCreationDateRetentionPolicy) obj;
            retentionPolicy = new BasedOnThreadCreationDateRetentionPolicy().setDaysAfterCreation(basedOnThreadCreationDateRetentionPolicy.getDaysAfterCreation());
        } else {
            retentionPolicy = new RetentionPolicy();
        }
        return retentionPolicy;
    }

    /**
     * Maps from {RetentionPolicy} to {@link com.azure.communication.chat.implementation.models.RetentionPolicy}.
     */
    public static com.azure.communication.chat.implementation.models.RetentionPolicy convertToImpl(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof RetentionPolicy == false) {
            return null;
        }

        com.azure.communication.chat.implementation.models.RetentionPolicy retentionPolicy;
        if (obj instanceof BasedOnThreadCreationDateRetentionPolicy) {
            BasedOnThreadCreationDateRetentionPolicy basedOnThreadCreationDateRetentionPolicy = (BasedOnThreadCreationDateRetentionPolicy) obj;
            retentionPolicy = new com.azure.communication.chat.implementation.models.BasedOnThreadCreationDateRetentionPolicy()
                .setDaysAfterCreation(basedOnThreadCreationDateRetentionPolicy.getDaysAfterCreation());
        } else {
            retentionPolicy = new com.azure.communication.chat.implementation.models.RetentionPolicy();
        }
        return retentionPolicy;
    }

    private RetentionPolicyConverter() {
    }
}

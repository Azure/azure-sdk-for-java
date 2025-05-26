// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Retention Policy Type.
 */
public final class RetentionPolicyKind
    extends ExpandableStringEnum<com.azure.communication.chat.models.RetentionPolicyKind> {
    /**
     * Thread retention policy based on thread creation date.
     */
    public static final com.azure.communication.chat.models.RetentionPolicyKind THREAD_CREATION_DATE
        = fromString("threadCreationDate");

    /**
     * No thread retention policy.
     */
    public static final com.azure.communication.chat.models.RetentionPolicyKind NONE = fromString("none");

    /**
     * Creates a new instance of RetentionPolicyKind value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RetentionPolicyKind() {
    }

    /**
     * Creates or finds a RetentionPolicyKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RetentionPolicyKind.
     */
    public static com.azure.communication.chat.models.RetentionPolicyKind fromString(String name) {
        return fromString(name, com.azure.communication.chat.models.RetentionPolicyKind.class);
    }

    /**
     * Gets known RetentionPolicyKind values.
     *
     * @return known RetentionPolicyKind values.
     */
    public static Collection<com.azure.communication.chat.models.RetentionPolicyKind> values() {
        return values(com.azure.communication.chat.models.RetentionPolicyKind.class);
    }
}

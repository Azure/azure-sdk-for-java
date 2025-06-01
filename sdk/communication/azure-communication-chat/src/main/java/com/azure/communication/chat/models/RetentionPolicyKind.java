// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Retention Policy Type.
 */
public final class RetentionPolicyKind extends ExpandableStringEnum<RetentionPolicyKind> {
    /**
     * Thread retention policy based on thread creation date.
     */
    public static final RetentionPolicyKind THREAD_CREATION_DATE = fromString("threadCreationDate");

    /**
     * No thread retention policy.
     */
    public static final RetentionPolicyKind NONE = fromString("none");

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
    public static RetentionPolicyKind fromString(String name) {
        return fromString(name, RetentionPolicyKind.class);
    }

    /**
     * Gets known RetentionPolicyKind values.
     *
     * @return known RetentionPolicyKind values.
     */
    public static Collection<RetentionPolicyKind> values() {
        return values(RetentionPolicyKind.class);
    }
}

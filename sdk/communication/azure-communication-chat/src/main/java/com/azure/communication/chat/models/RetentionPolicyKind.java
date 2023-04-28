package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for RetentionPolicyKind. */
public final class RetentionPolicyKind extends ExpandableStringEnum<RetentionPolicyKind> {
    /** Static value threadCreationDate for RetentionPolicyKind. */
    public static final RetentionPolicyKind THREAD_CREATION_DATE = fromString("threadCreationDate");

    /**
     * Creates or finds a RetentionPolicyKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RetentionPolicyKind.
     */
    @JsonCreator
    public static RetentionPolicyKind fromString(String name) {
        return fromString(name, RetentionPolicyKind.class);
    }

    /** @return known RetentionPolicyKind values. */
    public static Collection<RetentionPolicyKind> values() {
        return values(RetentionPolicyKind.class);
    }
}

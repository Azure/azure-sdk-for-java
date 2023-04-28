package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Kind. */
public final class RetentionPolicyKind extends ExpandableStringEnum<RetentionPolicyKind> {
    /** Static value threadCreationDate for Kind. */
    public static final RetentionPolicyKind THREAD_CREATION_DATE = fromString("threadCreationDate");

    /**
     * Creates or finds a Kind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Kind.
     */
    @JsonCreator
    public static RetentionPolicyKind fromString(String name) {
        return fromString(name, RetentionPolicyKind.class);
    }

    /** @return known Kind values. */
    public static Collection<RetentionPolicyKind> values() {
        return values(RetentionPolicyKind.class);
    }
}

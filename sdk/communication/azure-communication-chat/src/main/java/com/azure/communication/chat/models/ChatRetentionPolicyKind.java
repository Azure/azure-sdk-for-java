package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Kind. */
public final class ChatRetentionPolicyKind extends ExpandableStringEnum<ChatRetentionPolicyKind> {
    /** Static value threadCreationDate for Kind. */
    public static final ChatRetentionPolicyKind THREAD_CREATION_DATE = fromString("threadCreationDate");

    /**
     * Creates or finds a Kind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Kind.
     */
    @JsonCreator
    public static ChatRetentionPolicyKind fromString(String name) {
        return fromString(name, ChatRetentionPolicyKind.class);
    }

    /** @return known Kind values. */
    public static Collection<ChatRetentionPolicyKind> values() {
        return values(ChatRetentionPolicyKind.class);
    }
}

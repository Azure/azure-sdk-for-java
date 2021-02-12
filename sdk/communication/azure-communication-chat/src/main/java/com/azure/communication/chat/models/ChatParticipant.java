// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/** The ChatParticipant model. */
@Fluent
public final class ChatParticipant {
    /*
     * The user identity of the chat participant
     * in the format `8:acs...`.
     */
    @JsonProperty(value = "user", required = true)
    private CommunicationUserIdentifier user;

    /*
     * Display name for the chat participant.
     */
    @JsonProperty(value = "displayName")
    private String displayName;

    /*
     * Time from which the chat history is shared with the member. The
     * timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "shareHistoryTime")
    private OffsetDateTime shareHistoryTime;

    /**
     * Get the user property: The user identity of the chat participant in the format `8:acs...`.
     *
     * @return the user value.
     */
    public CommunicationUserIdentifier getUser() {
        return this.user;
    }

    /**
     * Set the user property: The user identity of the chat participant in the format `8:acs...`.
     *
     * @param user the user value to set.
     * @return the ChatParticipant object itself.
     */
    public ChatParticipant setUser(CommunicationUserIdentifier user) {
        this.user = user;
        return this;
    }

    /**
     * Get the displayName property: Display name for the chat participant.
     *
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: Display name for the chat participant.
     *
     * @param displayName the displayName value to set.
     * @return the ChatParticipant object itself.
     */
    public ChatParticipant setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the shareHistoryTime property: Time from which the chat history is shared with the chat participant.
     * The timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the shareHistoryTime value.
     */
    public OffsetDateTime getShareHistoryTime() {
        return this.shareHistoryTime;
    }

    /**
     * Set the shareHistoryTime property: Time from which the chat history is shared with the chat participant.
     * The timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param shareHistoryTime the shareHistoryTime value to set.
     * @return the ChatThreadMember object itself.
     */
    public ChatParticipant setShareHistoryTime(OffsetDateTime shareHistoryTime) {
        this.shareHistoryTime = shareHistoryTime;
        return this;
    }
}

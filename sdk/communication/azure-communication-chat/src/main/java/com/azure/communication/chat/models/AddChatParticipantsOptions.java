// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The AddChatParticipantsOptions model. */
@Fluent
public final class AddChatParticipantsOptions {
    /*
     * Participants to add to a chat thread.
     */
    @JsonProperty(value = "participants", required = true)
    private List<ChatParticipant> participants;

    /**
     * Get the participants property: Participants to add to a chat thread.
     *
     * @return the participants value.
     */
    public List<ChatParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: Participants to add to a chat thread.
     *
     * @param participants the participants value to set.
     * @return the AddChatParticipantsOptions object itself.
     */
    public AddChatParticipantsOptions setParticipants(List<ChatParticipant> participants) {
        this.participants = participants;
        return this;
    }
}

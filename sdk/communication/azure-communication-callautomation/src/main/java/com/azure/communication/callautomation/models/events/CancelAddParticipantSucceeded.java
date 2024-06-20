// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The CancelAddParticipantSucceeded model. */
@Immutable
public final class CancelAddParticipantSucceeded extends CallAutomationEventBase {
    /*
     * The invitation ID used to cancel the add participant request.
     */
    @JsonProperty(value = "invitationId")
    private String invitationId;

    @JsonCreator
    private CancelAddParticipantSucceeded() {
        invitationId = null;
    }

    /**
     * Get the invitationId property: The invitation ID used to cancel the add
     * participant request.
     *
     * @return the invitationId value.
     */
    public String getInvitationId() {
        return invitationId;
    }

    static CancelAddParticipantSucceeded fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CancelAddParticipantSucceeded event = new CancelAddParticipantSucceeded();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("invitationId".equals(fieldName)) {
                    event.invitationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}

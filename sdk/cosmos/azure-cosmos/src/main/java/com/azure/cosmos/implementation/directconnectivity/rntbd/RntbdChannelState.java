// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = RntbdChannelState.RntbdChannelStateJsonSerializer.class)
public class RntbdChannelState {
    public static RntbdChannelState NULL_REQUEST_MANAGER = new RntbdChannelState(State.NULL_REQUEST_MANAGER, 0);
    public static RntbdChannelState CLOSED = new RntbdChannelState(State.CLOSED, 0);

    private final int pendingRequests;
    private final State state;

    public RntbdChannelState(State state, int pendingRequests) {
        this.state = state;
        this.pendingRequests = pendingRequests;
    }

    public static RntbdChannelState ok(int pendingRequests) {
        return new RntbdChannelState(State.OK, pendingRequests);
    }

    public static RntbdChannelState pendingLimit(int pendingRequests) {
        return new RntbdChannelState(State.PENDING_LIMIT, pendingRequests);
    }

    public static RntbdChannelState contextNegotiationPending(int pendingRequests) {
        return new RntbdChannelState(State.CONTEXT_NEGOTIATION_PENDING, pendingRequests);
    }

    public boolean isOk() {
        return this.state == State.OK;
    }

    public static class RntbdChannelStateJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdChannelState> {
        @Override
        public void serialize(RntbdChannelState channelState,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeNumberField(channelState.state.toString(), channelState.pendingRequests);
            writer.writeEndObject();
        }
    }

    enum State{
        OK("ok"),
        CLOSED("closed"),
        NULL_REQUEST_MANAGER("nullRequestManager"),
        PENDING_LIMIT("pendingLimit"),
        CONTEXT_NEGOTIATION_PENDING("contextNegotiationPending");

        private String value;
        State(String value) {
            this.value = value;
        }

        @Override
        public String toString(){
            return value;
        }
    }
}

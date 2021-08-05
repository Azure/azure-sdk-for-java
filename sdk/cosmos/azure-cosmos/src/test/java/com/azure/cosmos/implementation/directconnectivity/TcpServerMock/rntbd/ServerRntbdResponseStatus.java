// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.UUID;

/**
 * Methods included in this class are copied from com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponseStatus.
 */
@JsonPropertyOrder({ "length", "status", "activityId" })
public class ServerRntbdResponseStatus {
    // region Fields

    static final int LENGTH = Integer.BYTES  // length
        + Integer.BYTES  // status
        + 2 * Long.BYTES;  // activityId

    @JsonProperty("activityId")
    private final UUID activityId;

    @JsonProperty("length")
    private final int length;

    private final HttpResponseStatus status;

    // endregion

    ServerRntbdResponseStatus(final int length, final HttpResponseStatus status, final UUID activityId) {
        this.length = length;
        this.status = status;
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    public int getLength() {
        return this.length;
    }

    public HttpResponseStatus getStatus() {
        return this.status;
    }

    @JsonProperty("status")
    public int getStatusCode() {
        return this.status.code();
    }

    void encode(final ByteBuf out) {
        out.writeIntLE(this.getLength());
        out.writeIntLE(this.getStatusCode());
        RntbdUUID.encode(this.getActivityId(), out);
    }

    @Override
    public String toString() {
        final ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (final JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
    }
}

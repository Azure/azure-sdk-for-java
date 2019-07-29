// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.UUID;

@JsonPropertyOrder({ "length", "status", "activityId" })
final class RntbdResponseStatus {

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

    RntbdResponseStatus(final int length, final HttpResponseStatus status, final UUID activityId) {
        this.length = length;
        this.status = status;
        this.activityId = activityId;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    int getHeadersLength() {
        return this.length - LENGTH;
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

    static RntbdResponseStatus decode(final ByteBuf in) {

        final long length = in.readUnsignedIntLE();

        if (!(LENGTH <= length && length <= Integer.MAX_VALUE)) {
            final String reason = String.format("frame length: %d", length);
            throw new CorruptedFrameException(reason);
        }

        final int code = in.readIntLE();
        final HttpResponseStatus status = HttpResponseStatus.valueOf(code);

        if (status == null) {
            final String reason = String.format("status code: %d", code);
            throw new CorruptedFrameException(reason);
        }

        final UUID activityId = RntbdUUID.decode(in);
        return new RntbdResponseStatus((int)length, status, activityId);
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

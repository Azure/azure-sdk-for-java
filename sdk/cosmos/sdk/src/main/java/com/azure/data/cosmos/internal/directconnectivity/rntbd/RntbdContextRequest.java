/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.UserAgentContainer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.azure.data.cosmos.internal.HttpConstants.Versions;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.CurrentProtocolVersion;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdContextRequestHeader;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdOperationType;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdResourceType;

public final class RntbdContextRequest {

    @JsonProperty
    private final UUID activityId;

    @JsonProperty
    private final Headers headers;

    RntbdContextRequest(final UUID activityId, final UserAgentContainer userAgent) {
        this(activityId, new Headers(userAgent));
    }

    private RntbdContextRequest(final UUID activityId, final Headers headers) {
        this.activityId = activityId;
        this.headers = headers;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    public String getClientVersion() {
        return this.headers.clientVersion.getValue(String.class);
    }

    public static RntbdContextRequest decode(final ByteBuf in) {

        final int resourceOperationTypeCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationTypeCode != 0) {
            final String reason = String.format("resourceOperationCode=0x%08X", resourceOperationTypeCode);
            throw new IllegalStateException(reason);
        }

        final int start = in.readerIndex();
        final int expectedLength = in.readIntLE();

        final RntbdRequestFrame header = RntbdRequestFrame.decode(in);
        final Headers headers = Headers.decode(in.readSlice(expectedLength - (in.readerIndex() - start)));

        final int observedLength = in.readerIndex() - start;

        if (observedLength != expectedLength) {
            final String reason = String.format("expectedLength=%d, observeredLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        in.discardReadBytes();
        return new RntbdContextRequest(header.getActivityId(), headers);
    }

    public void encode(final ByteBuf out) {

        final int expectedLength = RntbdRequestFrame.LENGTH + this.headers.computeLength();
        final int start = out.writerIndex();

        out.writeIntLE(expectedLength);

        final RntbdRequestFrame header = new RntbdRequestFrame(this.getActivityId(), RntbdOperationType.Connection, RntbdResourceType.Connection);
        header.encode(out);
        this.headers.encode(out);

        final int observedLength = out.writerIndex() - start;

        if (observedLength != expectedLength) {
            final String reason = String.format("expectedLength=%d, observeredLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }
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

    private static final class Headers extends RntbdTokenStream<RntbdContextRequestHeader> {

        private static final byte[] ClientVersion = Versions.CURRENT_VERSION.getBytes(StandardCharsets.UTF_8);

        @JsonProperty
        RntbdToken clientVersion;

        @JsonProperty
        RntbdToken protocolVersion;

        @JsonProperty
        RntbdToken userAgent;

        Headers(final UserAgentContainer container) {

            this();

            this.clientVersion.setValue(ClientVersion);
            this.protocolVersion.setValue(CurrentProtocolVersion);
            this.userAgent.setValue(container.getUserAgent());
        }

        private Headers() {

            super(RntbdContextRequestHeader.set, RntbdContextRequestHeader.map);

            this.clientVersion = this.get(RntbdContextRequestHeader.ClientVersion);
            this.protocolVersion = this.get(RntbdContextRequestHeader.ProtocolVersion);
            this.userAgent = this.get(RntbdContextRequestHeader.UserAgent);
        }

        static Headers decode(final ByteBuf in) {
            return Headers.decode(in, new Headers());
        }
    }
}

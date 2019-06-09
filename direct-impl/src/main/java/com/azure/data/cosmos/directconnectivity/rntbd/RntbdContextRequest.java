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

package com.azure.data.cosmos.directconnectivity.rntbd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.azure.data.cosmos.internal.UserAgentContainer;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.azure.data.cosmos.internal.HttpConstants.Versions;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.CurrentProtocolVersion;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdContextRequestHeader;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdOperationType;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdResourceType;

final public class RntbdContextRequest {

    @JsonProperty
    final private UUID activityId;

    @JsonProperty
    final private Headers headers;

    RntbdContextRequest(UUID activityId, UserAgentContainer userAgent) {
        this(activityId, new Headers(userAgent));
    }

    private RntbdContextRequest(UUID activityId, Headers headers) {
        this.activityId = activityId;
        this.headers = headers;
    }

    public static RntbdContextRequest decode(ByteBuf in) {

        int resourceOperationTypeCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationTypeCode != 0) {
            String reason = String.format("resourceOperationCode=0x%08X", resourceOperationTypeCode);
            throw new IllegalStateException(reason);
        }

        int start = in.readerIndex();
        int expectedLength = in.readIntLE();

        RntbdRequestFrame header = RntbdRequestFrame.decode(in);
        Headers headers = Headers.decode(in.readSlice(expectedLength - (in.readerIndex() - start)));

        int observedLength = in.readerIndex() - start;

        if (observedLength != expectedLength) {
            String reason = String.format("expectedLength=%d, observeredLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        in.discardReadBytes();
        return new RntbdContextRequest(header.getActivityId(), headers);
    }

    public UUID getActivityId() {
        return activityId;
    }

    public String getClientVersion() {
        return this.headers.clientVersion.getValue(String.class);
    }

    public void encode(ByteBuf out) {

        int expectedLength = RntbdRequestFrame.LENGTH + headers.computeLength();
        int start = out.writerIndex();

        out.writeIntLE(expectedLength);

        RntbdRequestFrame header = new RntbdRequestFrame(this.getActivityId(), RntbdOperationType.Connection, RntbdResourceType.Connection);
        header.encode(out);
        this.headers.encode(out);

        int observedLength = out.writerIndex() - start;

        if (observedLength != expectedLength) {
            String reason = String.format("expectedLength=%d, observeredLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }
    }

    @Override
    public String toString() {
        ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
    }

    final private static class Headers extends RntbdTokenStream<RntbdContextRequestHeader> {

        private final static byte[] ClientVersion = Versions.CURRENT_VERSION.getBytes(StandardCharsets.UTF_8);

        @JsonProperty
        RntbdToken clientVersion;

        @JsonProperty
        RntbdToken protocolVersion;

        @JsonProperty
        RntbdToken userAgent;

        Headers(UserAgentContainer container) {

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

        static Headers decode(ByteBuf in) {
            return Headers.decode(in, new Headers());
        }
    }
}

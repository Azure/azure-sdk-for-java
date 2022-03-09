// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The methods included in this class are copied from {@link RntbdContextRequest}.
 */
public class ServerRntbdContextRequest {
    @JsonProperty
    private final UUID activityId;

    @JsonProperty
    private final ServerRntbdContextRequest.Headers headers;

    private ServerRntbdContextRequest(final UUID activityId, final ServerRntbdContextRequest.Headers headers) {
        this.activityId = activityId;
        this.headers = headers;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    public static ServerRntbdContextRequest decode(final ByteBuf in) {

        final int resourceOperationTypeCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationTypeCode != 0) {
            final String reason = String.format("resourceOperationCode=0x%08X", resourceOperationTypeCode);
            throw new IllegalStateException(reason);
        }

        final int start = in.readerIndex();
        final int expectedLength = in.readIntLE();

        final ServerRntbdRequestFrame header = ServerRntbdRequestFrame.decode(in);
        final ServerRntbdContextRequest.Headers headers = ServerRntbdContextRequest.Headers.decode(in.readSlice(expectedLength - (in.readerIndex() - start)));

        final int observedLength = in.readerIndex() - start;

        if (observedLength != expectedLength) {
            final String reason = Strings.lenientFormat("expectedLength=%s, observedLength=%s", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        in.discardReadBytes();
        return new ServerRntbdContextRequest(header.getActivityId(), headers);
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

    private static final class Headers extends ServerRntbdTokenStream<ServerRntbdConstants.RntbdContextRequestHeader> {

        private static final byte[] ClientVersion = HttpConstants.Versions.CURRENT_VERSION.getBytes(StandardCharsets.UTF_8);

        @JsonProperty
        ServerRntbdToken clientVersion;

        @JsonProperty
        ServerRntbdToken protocolVersion;

        @JsonProperty
        ServerRntbdToken userAgent;

        Headers(final UserAgentContainer container) {
            this(Unpooled.EMPTY_BUFFER);
            this.clientVersion.setValue(ClientVersion);
            this.userAgent.setValue(container.getUserAgent());
            this.protocolVersion.setValue(ServerRntbdConstants.CURRENT_PROTOCOL_VERSION);
        }

        private Headers(ByteBuf in) {

            super(ServerRntbdConstants.RntbdContextRequestHeader.set, ServerRntbdConstants.RntbdContextRequestHeader.map, in);

            this.clientVersion = this.get(ServerRntbdConstants.RntbdContextRequestHeader.ClientVersion);
            this.protocolVersion = this.get(ServerRntbdConstants.RntbdContextRequestHeader.ProtocolVersion);
            this.userAgent = this.get(ServerRntbdConstants.RntbdContextRequestHeader.UserAgent);
        }

        static ServerRntbdContextRequest.Headers decode(final ByteBuf in) {
            return ServerRntbdContextRequest.Headers.decode(new ServerRntbdContextRequest.Headers(in));
        }
    }
}

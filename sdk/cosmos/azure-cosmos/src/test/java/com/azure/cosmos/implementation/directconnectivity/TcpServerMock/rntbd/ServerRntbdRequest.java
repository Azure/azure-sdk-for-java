// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Methods included in this class are copied from {@link RntbdRequest}.
 */
public final class ServerRntbdRequest {
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private final ServerRntbdRequestFrame frame;
    private final ServerRntbdRequestHeaders headers;
    private final byte[] payload;

    private ServerRntbdRequest(final ServerRntbdRequestFrame frame, final ServerRntbdRequestHeaders headers, final byte[] payload) {

        checkNotNull(frame, "frame");
        checkNotNull(headers, "headers");

        this.frame = frame;
        this.headers = headers;
        this.payload = payload == null ? EMPTY_BYTE_ARRAY : payload;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getHeader(final ServerRntbdConstants.RntbdRequestHeader header) {
        return (T) this.headers.get(header).getValue();
    }

    public static ServerRntbdRequest decode(final ByteBuf in) {

        final int resourceOperationCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationCode == 0) {
            final String reason = String.format("resourceOperationCode=0x%08X", resourceOperationCode);
            throw new IllegalStateException(reason);
        }

        final int start = in.readerIndex();
        final int expectedLength = in.readIntLE();

        final ByteBuf headerBuf = in.readSlice(expectedLength - (in.readerIndex() - start));
        final ServerRntbdRequestFrame header = ServerRntbdRequestFrame.decode(headerBuf);
        final ServerRntbdRequestHeaders metadata = ServerRntbdRequestHeaders.decode(headerBuf);

        ServerRntbdToken payloadPresent = metadata.get(ServerRntbdConstants.RntbdRequestHeader.PayloadPresent);

        if (payloadPresent != null && payloadPresent.getValue(Byte.class) != 0 && in.readableBytes() == 0) {
            // Body is not there yet, let's wait for the body as well.
            return null;
        }

        final int payloadStart = in.readerIndex();
        final int payloadExpectedLength = in.readIntLE();
        final ByteBuf payloadBuf = in.readSlice(payloadExpectedLength - (in.readerIndex() - payloadStart));

        final int observedLength = in.readerIndex() - payloadStart;

        if (observedLength != payloadExpectedLength) {
            final String reason = Strings.lenientFormat("expectedLength=%s, observedLength=%s", payloadExpectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        final byte[] payload = new byte[payloadBuf.readableBytes()];
        payloadBuf.readBytes(payload);
        in.discardReadBytes();

        return new ServerRntbdRequest(header, metadata, payload);
    }
}

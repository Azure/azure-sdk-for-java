// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.ReferenceCounted;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME;
import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

@SuppressWarnings("UnstableApiUsage")
abstract class RntbdTokenStream<T extends Enum<T> & RntbdHeader> implements ReferenceCounted {

    final ByteBuf in;
    final Map<Short, T> headers;
    final EnumMap<T, RntbdToken> tokens;

    RntbdTokenStream(final EnumSet<T> headers, final Map<Short, T> ids, final ByteBuf in, final Class<T> classType) {

        checkNotNull(headers, "expected non-null headers");
        checkNotNull(ids, "expected non-null ids");
        checkNotNull(in, "expected non-null in");

        this.tokens = new EnumMap<>(classType);
        headers.stream().forEach(h -> tokens.put(h, RntbdToken.create(h)));
        this.headers = ids;
        this.in = in;
    }

    // region Methods

    final int computeCount() {

        int count = 0;

        for (final RntbdToken token : this.tokens.values()) {
            if (token.isPresent()) {
                ++count;
            }
        }

        return count;
    }

    final int computeLength() {

        int total = 0;

        for (final Map.Entry<T, RntbdToken> entry : this.tokens.entrySet()) {
            if (entry.getKey() == RntbdConstants.RntbdRequestHeader.TransportRequestID
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.ReplicaPath
            ) {
                continue;
            }

            total += entry.getValue().computeLength();
        }

        return total;
    }

    static <T extends RntbdTokenStream<?>> T decode(final T stream) {

        final ByteBuf in = stream.in;

        while (in.readableBytes() > 0) {

            final short id = in.readShortLE();
            final RntbdTokenType type = RntbdTokenType.fromId(in.readByte());

            RntbdToken token = stream.tokens.get(stream.headers.get(id));

            if (token == null) {
                token = RntbdToken.create(new UndefinedHeader(id, type));
            }

            token.decode(in);
        }

        for (final RntbdToken token : stream.tokens.values()) {
            if (!token.isPresent() && token.isRequired()) {
                final String message = lenientFormat("Required header not found on token stream: %s", token);
                throw new CorruptedFrameException(message);
            }
        }

        return stream;
    }

    final void encode(final ByteBuf out) {
        // TODO: special casing for thin client. need to revisit perf implications.
        RntbdToken epkHeader = this.tokens.get(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey);
        if (epkHeader != null) {
            epkHeader.encode(out);
        }

        RntbdToken globalDbAccount = this.tokens.get(RntbdConstants.RntbdRequestHeader.GlobalDatabaseAccountName);
        if (globalDbAccount != null) {
            globalDbAccount.encode(out);
        }

        RntbdToken dbName = this.tokens.get(RntbdConstants.RntbdRequestHeader.DatabaseName);
        if (dbName != null) {
            dbName.encode(out);
        }

        RntbdToken containerName = this.tokens.get(RntbdConstants.RntbdRequestHeader.CollectionName);
        if (containerName != null) {
            containerName.encode(out);
        }

        RntbdToken containerRid = this.tokens.get(RntbdConstants.RntbdRequestHeader.CollectionRid);
        if (containerRid != null) {
            containerRid.encode(out);
        } else {

        }

        RntbdToken resourceId = this.tokens.get(RntbdConstants.RntbdRequestHeader.ResourceId);
        if (resourceId != null) {
            resourceId.encode(out);
        }

        RntbdToken payloadPresent = this.tokens.get(RntbdConstants.RntbdRequestHeader.PayloadPresent);
        if (payloadPresent != null) {
            payloadPresent.encode(out);
        }

        RntbdToken docName = this.tokens.get(RntbdConstants.RntbdRequestHeader.DocumentName);
        if (docName != null) {
            docName.encode(out);
        }


        RntbdToken authzToken = this.tokens.get(RntbdConstants.RntbdRequestHeader.AuthorizationToken);
        if (authzToken != null) {
            authzToken.encode(out);
        }

        RntbdToken date = this.tokens.get(RntbdConstants.RntbdRequestHeader.Date);
        if (date != null) {
            date.encode(out);
        }

        for (final Map.Entry<T, RntbdToken> entry : this.tokens.entrySet()) {
            if (entry.getKey() == RntbdConstants.RntbdRequestHeader.EffectivePartitionKey
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.GlobalDatabaseAccountName
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.DatabaseName
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.CollectionName
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.CollectionRid
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.ResourceId
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.PayloadPresent
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.DocumentName
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.AuthorizationToken
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.Date
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.TransportRequestID
                || entry.getKey() == RntbdConstants.RntbdRequestHeader.ReplicaPath
            ) {
                continue;
            }

            entry.getValue().encode(out);
        }
    }

    final RntbdToken get(final T header) {
        return this.tokens.get(header);
    }

    @Override
    public final int refCnt() {
        return this.in.refCnt();
    }

    @Override
    public final boolean release() {
        return this.release(1);
    }

    @Override
    public final boolean release(final int count) {
        return this.in.release(count);
    }

    @Override
    public final RntbdTokenStream<T> retain() {
        return this.retain(1);
    }

    @Override
    public final RntbdTokenStream<T> retain(final int count) {
        this.in.retain(count);
        return this;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        return this;
    }

    // endregion

    // region Types

    private static final class UndefinedHeader implements RntbdHeader {

        private final short id;
        private final RntbdTokenType type;

        UndefinedHeader(final short id, final RntbdTokenType type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public short id() {
            return this.id;
        }

        @Override
        public String name() {
            return "Undefined";
        }

        @Override
        public RntbdTokenType type() {
            return this.type;
        }
    }

    // endregion
}

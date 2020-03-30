// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.guava25.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.ReferenceCounted;

import java.util.stream.Collector;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

@SuppressWarnings("UnstableApiUsage")
abstract class RntbdTokenStream<T extends Enum<T> & RntbdHeader> implements ReferenceCounted {

    final ByteBuf in;
    final ImmutableMap<Short, T> headers;
    final ImmutableMap<T, RntbdToken> tokens;

    RntbdTokenStream(final ImmutableSet<T> headers, final ImmutableMap<Short, T> ids, final ByteBuf in) {

        checkNotNull(headers, "expected non-null headers");
        checkNotNull(ids, "expected non-null ids");
        checkNotNull(in, "expected non-null in");

        final Collector<T, ?, ImmutableMap<T, RntbdToken>> collector = Maps.toImmutableEnumMap(h -> h, RntbdToken::create);
        this.tokens = headers.stream().collect(collector);
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

        for (final RntbdToken token : this.tokens.values()) {
            total += token.computeLength();
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
        for (final RntbdToken token : this.tokens.values()) {
            token.encode(out);
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

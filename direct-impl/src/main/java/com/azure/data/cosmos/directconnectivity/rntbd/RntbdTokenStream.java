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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.stream.Collector;

import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdHeader;

abstract class RntbdTokenStream<T extends Enum<T> & RntbdHeader> {

    final ImmutableMap<Short, T> headers;
    final ImmutableMap<T, RntbdToken> tokens;

    RntbdTokenStream(ImmutableSet<T> headers, ImmutableMap<Short, T> ids) {

        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(ids, "ids");

        Collector<T, ?, ImmutableMap<T, RntbdToken>> collector = Maps.toImmutableEnumMap(h -> h, RntbdToken::create);
        this.tokens = headers.stream().collect(collector);
        this.headers = ids;
    }

    final int computeCount() {

        int count = 0;

        for (RntbdToken token : this.tokens.values()) {
            if (token.isPresent()) {
                ++count;
            }
        }

        return count;
    }

    final int computeLength() {

        int total = 0;

        for (RntbdToken token : this.tokens.values()) {
            total += token.computeLength();
        }

        return total;
    }

    static <T extends RntbdTokenStream<?>> T decode(ByteBuf in, T stream) {

        while (in.readableBytes() > 0) {

            final short id = in.readShortLE();
            final RntbdTokenType type = RntbdTokenType.fromId(in.readByte());

            RntbdToken token = stream.tokens.get(stream.headers.get(id));

            if (token == null) {
                token = RntbdToken.create(new UndefinedHeader(id, type));
            }

            token.decode(in);
        }

        for (RntbdToken token : stream.tokens.values()) {
            if (!token.isPresent() && token.isRequired()) {
                String reason = String.format("Required token not found on RNTBD stream: type: %s, identifier: %s",
                    token.getType(), token.getId());
                throw new IllegalStateException(reason);
            }
        }

        return stream;
    }

    final void encode(ByteBuf out) {
        for (RntbdToken token : this.tokens.values()) {
            token.encode(out);
        }
    }

    final RntbdToken get(T header) {
        return this.tokens.get(header);
    }

    final void releaseBuffers() {
        for (RntbdToken token : this.tokens.values()) {
            token.releaseBuffer();
        }
    }

    final static private class UndefinedHeader implements RntbdHeader {

        final private short id;
        final private RntbdTokenType type;

        UndefinedHeader(short id, RntbdTokenType type) {
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
}

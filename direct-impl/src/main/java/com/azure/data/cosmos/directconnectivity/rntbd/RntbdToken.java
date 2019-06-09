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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.Objects;

import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdHeader;

@JsonPropertyOrder({ "id", "name", "type", "present", "required", "value" })
class RntbdToken {

    // region Fields

    private static final int HEADER_LENGTH = Short.BYTES + Byte.BYTES;

    static {
        RntbdObjectMapper.registerPropertyFilter(RntbdToken.class, PropertyFilter.class);
    }

    private final RntbdHeader header;
    private int length;
    private Object value;

    // endregion

    private RntbdToken(RntbdHeader header) {
        Objects.requireNonNull(header);
        this.header = header;
        this.value = null;
        this.length = Integer.MIN_VALUE;
    }

    @JsonProperty
    RntbdTokenType getType() {
        return this.header.type();
    }

    // region Accessors

    @JsonProperty
    final short getId() {
        return this.header.id();
    }

    @JsonProperty
    final String getName() {
        return this.header.name();
    }

    @JsonProperty
    final Object getValue() {

        if (this.value == null) {
            return this.header.type().codec().defaultValue();
        }

        if (this.value instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf)this.value;
            this.value = this.header.type().codec().read(buffer);
            buffer.release();
        } else {
            this.value = this.header.type().codec().convert(this.value);
        }

        return this.value;
    }

    final int computeLength() {

        if (!this.isPresent()) {
            return 0;
        }

        if (this.value instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf)this.value;
            assert buffer.readerIndex() == 0;
            return HEADER_LENGTH + buffer.readableBytes();
        }

        if (this.length == Integer.MIN_VALUE) {
            this.length = HEADER_LENGTH + this.header.type().codec().computeLength(this.value);
        }

        return this.length;
    }

    @JsonProperty
    final void setValue(Object value) {
        this.ensureValid(value);
        this.length = Integer.MIN_VALUE;
        this.value = value;
    }

    @JsonProperty
    final boolean isPresent() {
        return this.value != null;
    }

    @JsonProperty
    final boolean isRequired() {
        return this.header.isRequired();
    }

    // endregion

    // region Methods

    static RntbdToken create(RntbdHeader header) {
        return new RntbdToken(header);
    }

    void decode(ByteBuf in) {

        Objects.requireNonNull(in);

        if (this.value instanceof ByteBuf) {
            ((ByteBuf)this.value).release();
        }

        this.value = this.header.type().codec().readSlice(in).retain(); // No data transfer until the first call to RntbdToken.value
    }

    final void encode(ByteBuf out) {

        Objects.requireNonNull(out);

        if (!this.isPresent()) {
            if (this.isRequired()) {
                String message = String.format("Missing value for required header: %s", this);
                throw new IllegalStateException(message);
            }
            return;
        }

        out.writeShortLE(this.getId());
        out.writeByte(this.getType().id());

        if (this.value instanceof ByteBuf) {
            out.writeBytes((ByteBuf)this.value);
        } else {
            this.ensureValid(value);
            this.header.type().codec().write(value, out);
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

    final <T> T getValue(Class<T> cls) {
        return cls.cast(this.getValue());
    }

    final void releaseBuffer() {
        if (this.value instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf)this.value;
            buffer.release();
        }
    }

    private void ensureValid(Object value) {

        Objects.requireNonNull(value);

        if (!this.header.type().codec().isValid(value)) {
            String reason = String.format("value: %s", value.getClass());
            throw new IllegalArgumentException(reason);
        }
    }

    // endregion

    // region Types

    static class PropertyFilter extends SimpleBeanPropertyFilter {

        @Override
        public void serializeAsField(Object object, JsonGenerator generator, SerializerProvider provider, PropertyWriter writer) throws Exception {

            if (generator.canOmitFields()) {

                Object value = writer.getMember().getValue(object);

                if (value instanceof RntbdToken && !((RntbdToken)value).isPresent()) {
                    return;
                }
            }

            writer.serializeAsField(object, generator, provider);
        }
    }

    // endregion
}

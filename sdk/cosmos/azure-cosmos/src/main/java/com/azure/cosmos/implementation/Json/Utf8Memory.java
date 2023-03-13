package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Utf8Memory
{
    public static final Utf8Memory Empty = new Utf8Memory(ByteBuffer.allocate(0));

    private final ByteBuffer Memory;

    private Utf8Memory(ByteBuffer utf8Bytes) {
        this.Memory = utf8Bytes.asReadOnlyBuffer();
    }

    public ByteBuffer getMemory() {
        return Memory;
    }

    public Utf8Memory slice(int start) {
        ByteBuffer slice = this.Memory.duplicate();
        slice.position(start);
        return new Utf8Memory(slice);
    }

    public Utf8Memory slice(int start, int length) {
        ByteBuffer slice = this.Memory.duplicate();
        slice.position(start);
        slice.limit(start + length);
        return new Utf8Memory(slice);
    }

    public boolean isEmpty() {
        return this.Memory.limit() == 0;
    }

    public int length() {
        return this.Memory.limit();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if (!(obj instanceof Utf8Memory)) {
            return false;
        }

        Utf8Memory other = (Utf8Memory) obj;
        return true;
    }

    public boolean equals(Utf8Memory utf8Memory){
        return this.Memory.equals(utf8Memory);
    }

    @Override
    public String toString() {
        byte[] bytes = new byte[this.Memory.remaining()];
        this.Memory.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static Utf8Memory create(String value) {
        return Utf8Memory.unsafeCreateNoValidation(StandardCharsets.UTF_8.encode(value));
    }

    public static Utf8Memory unsafeCreateNoValidation(ByteBuffer utf8Bytes) {
        return new Utf8Memory(utf8Bytes);
    }

}

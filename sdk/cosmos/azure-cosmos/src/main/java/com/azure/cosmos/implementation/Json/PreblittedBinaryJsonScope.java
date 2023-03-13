package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;

public final class PreblittedBinaryJsonScope
{
    public final ByteBuffer bytes;

    public PreblittedBinaryJsonScope(ByteBuffer bytes) {
        this.bytes = bytes;
    }
}


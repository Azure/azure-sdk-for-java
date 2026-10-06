/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Adapted by Microsoft from FlatBuffers Java 25.2.10 for the internal Blob Storage Arrow reader.

package com.azure.storage.blob.implementation.util.flatbuffers;

import java.nio.ByteBuffer;

/// @cond FLATBUFFERS_INTERNAL

/**
 * All structs in generated FlatBuffers code derive from this class.
 */
public class Struct {
    protected int bb_pos;
    protected ByteBuffer bb;

    protected void __reset(int index, ByteBuffer byteBuffer) {
        bb = byteBuffer;
        bb_pos = bb == null ? 0 : index;
    }

    /**
     * Resets internal state with a null buffer and zero position.
     */
    public void __reset() {
        __reset(0, null);
    }
}

/// @endcond

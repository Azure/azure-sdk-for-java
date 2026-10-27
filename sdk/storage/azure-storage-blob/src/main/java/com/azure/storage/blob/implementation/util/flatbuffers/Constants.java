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

/// @cond FLATBUFFERS_INTERNAL

/**
 * Class that holds shared constants.
 */
public class Constants {
    static final int SIZEOF_BYTE = 1;
    static final int SIZEOF_SHORT = 2;
    static final int SIZEOF_INT = 4;
    static final int SIZEOF_FLOAT = 4;
    static final int SIZEOF_LONG = 8;
    static final int SIZEOF_DOUBLE = 8;
    static final int FILE_IDENTIFIER_LENGTH = 4;
    public static final int SIZE_PREFIX_LENGTH = 4;

    /**
     * Version identifier used by generated FlatBuffers code.
     */
    public static void FLATBUFFERS_25_2_10() {
    }
}

/// @endcond

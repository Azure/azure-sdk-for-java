// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.util;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import reactor.util.annotation.Nullable;

public class CustomCharacterEscapes extends CharacterEscapes {

    private static final long serialVersionUID = 1L;

    private final int[] asciiEscapes;

    public CustomCharacterEscapes() {
        asciiEscapes = standardAsciiEscapesForJSON();
        // By default jackson doesn't escape forward slashes (`/`), but the quick pulse backend requires
        // them to be escaped.
        asciiEscapes['/'] = CharacterEscapes.ESCAPE_CUSTOM;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes.clone();
    }

    @Override
    @Nullable
    public SerializableString getEscapeSequence(int i) {
        if (i == '/') {
            return new SerializedString("\\/");
        } else {
            return null;
        }
    }
}

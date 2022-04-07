// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.fasterxml.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = JsonFactory.class,
    initialization = {
        @InitializationHint(
            initTime = InitializationTime.BUILD,
            types ={
                CharTypes.class,
                JsonStringEncoder.class
            }
        )
    }
)
public class JacksonCoreHints implements NativeConfiguration {
}

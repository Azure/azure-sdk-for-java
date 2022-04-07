// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = TokenCredential.class,
    initialization = {
        @InitializationHint(
            initTime = InitializationTime.BUILD,
            types = { LoggingEventBuilder.class }
        )
    }
)
public class AzureCoreHints implements NativeConfiguration {
}

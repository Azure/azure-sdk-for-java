// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.springframework;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = @TypeHint(types = AnnotationAttributes.class)
)
public class CoreHints implements NativeConfiguration {
}

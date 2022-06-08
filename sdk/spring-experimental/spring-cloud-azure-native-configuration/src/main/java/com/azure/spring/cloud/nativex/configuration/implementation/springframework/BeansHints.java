// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.springframework;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = @TypeHint(
        types = {
            AutowiredAnnotationBeanPostProcessor.class
        }
    )
)
public class BeansHints implements NativeConfiguration {
}

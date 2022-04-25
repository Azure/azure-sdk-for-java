// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.service;

import com.azure.spring.cloud.service.implementation.core.PropertiesMerger;
import com.azure.spring.cloud.service.implementation.storage.common.StorageRetryProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = PropertiesMerger.class,
    types = {
        @TypeHint(
            types = StorageRetryProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS })
    }
)
public class ServiceHints implements NativeConfiguration {
}

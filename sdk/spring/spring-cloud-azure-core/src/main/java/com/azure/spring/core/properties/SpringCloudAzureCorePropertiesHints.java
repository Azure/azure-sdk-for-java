// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = @TypeHint(
        types = AzureSdkProperties.class,
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    )
)
public class SpringCloudAzureCorePropertiesHints implements NativeConfiguration {
}

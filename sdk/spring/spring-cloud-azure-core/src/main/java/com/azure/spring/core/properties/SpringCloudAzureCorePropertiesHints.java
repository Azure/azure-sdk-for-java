// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.core.properties.profile.AzureProfileAdapter;
import com.azure.spring.core.properties.profile.AzureProfileProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = {
        @TypeHint(
            types = AzureEnvironmentProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureProfileProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureProfileAdapter.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureHttpSdkProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureSdkProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        )
    }
)
public class SpringCloudAzureCorePropertiesHints implements NativeConfiguration {
}

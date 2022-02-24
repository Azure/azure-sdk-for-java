// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureServiceConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = {
        @TypeHint(
            types = AbstractAzureServiceConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureProfileConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureProfileConfigurationProperties.AzureEnvironmentConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        )
    }
)
public class SpringCloudAzureHints implements NativeConfiguration {
}

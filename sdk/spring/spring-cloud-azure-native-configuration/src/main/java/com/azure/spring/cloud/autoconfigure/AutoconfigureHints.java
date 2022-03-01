// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureServiceConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    types = {
        @TypeHint(
            types = AzureProfileConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = TokenCredentialConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureResourceMetadataConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AbstractAzureServiceConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureProfileConfigurationProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = AzureStorageProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        )
    }
)
public class AutoconfigureHints implements NativeConfiguration {
}

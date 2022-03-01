// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import com.azure.spring.core.properties.AzureHttpSdkProperties;
import com.azure.spring.core.properties.AzureSdkProperties;
import com.azure.spring.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.profile.AzureEnvironmentProperties;
import com.azure.spring.core.properties.profile.AzureProfileAdapter;
import com.azure.spring.core.properties.profile.AzureProfileProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.HttpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
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
            types = TokenCredentialProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = HttpClientProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = ClientProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = HttpRetryProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = RetryProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = HttpProxyProperties.class,
            access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
        ),
        @TypeHint(
            types = ProxyProperties.class,
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
public class CoreHints implements NativeConfiguration {
}

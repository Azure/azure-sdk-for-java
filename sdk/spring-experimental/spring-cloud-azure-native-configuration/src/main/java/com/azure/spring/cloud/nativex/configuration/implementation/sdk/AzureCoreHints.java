// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.sdk;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.util.Base64Url;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = TokenCredential.class,
    types = @TypeHint(
        types = {
            Base64Url.class,
            LongRunningOperationStatus.class,
            ResourceModifiedException.class
        },
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    )
)
public class AzureCoreHints implements NativeConfiguration {
}

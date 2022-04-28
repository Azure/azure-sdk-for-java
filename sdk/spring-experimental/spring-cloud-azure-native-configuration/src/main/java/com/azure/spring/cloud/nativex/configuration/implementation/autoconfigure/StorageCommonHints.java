// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.autoconfigure;

import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.StorageRetryConfigurationProperties;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = StorageSharedKeyCredential.class,
    types = {
        @TypeHint(
            types = { AzureStorageProperties.class, StorageRetryConfigurationProperties.class },
            access = {
                TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS
            })
    }
)
public class StorageCommonHints implements NativeConfiguration {
}

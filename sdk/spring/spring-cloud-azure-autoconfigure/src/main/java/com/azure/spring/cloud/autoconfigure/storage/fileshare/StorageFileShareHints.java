// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = ShareServiceClientBuilder.class,
    types = @TypeHint(
        types = AzureStorageFileShareProperties.class,
        access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }
    )
)
public class StorageFileShareHints implements NativeConfiguration {
}

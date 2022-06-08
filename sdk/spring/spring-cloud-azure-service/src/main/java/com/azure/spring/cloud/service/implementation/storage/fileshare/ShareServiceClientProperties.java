// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.fileshare;

import com.azure.spring.cloud.service.implementation.storage.common.StorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public interface ShareServiceClientProperties extends StorageProperties {

    /**
     * Get the share service version.
     * @return the share service version.
     */
    ShareServiceVersion getServiceVersion();

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.fileshare;

import com.azure.spring.service.storage.common.StorageProperties;
import com.azure.storage.file.share.ShareServiceVersion;

/**
 * Properties for Azure Storage File Share service.
 */
public interface ShareServiceClientProperties extends StorageProperties {

    ShareServiceVersion getServiceVersion();

}

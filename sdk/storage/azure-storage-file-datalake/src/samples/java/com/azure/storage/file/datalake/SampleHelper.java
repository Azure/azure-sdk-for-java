// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Configuration;

/**
 * This is a helper class of frequently used methods for sample codes.
 */
class SampleHelper {
    static String getAccountName() {
        return Configuration.getGlobalConfiguration().get("STORAGE_DATA_LAKE_ACCOUNT_NAME");
    }

    static String getAccountKey() {
        return Configuration.getGlobalConfiguration().get("STORAGE_DATA_LAKE_ACCOUNT_KEY");
    }
}

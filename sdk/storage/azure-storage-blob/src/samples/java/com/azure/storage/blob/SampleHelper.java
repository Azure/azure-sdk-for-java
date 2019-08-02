// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.configuration.ConfigurationManager;

/**
 * This is a helper class of frequently used methods for sample codes.
 */
class SampleHelper {
    static String getAccountName() {
        return ConfigurationManager.getConfiguration().get("PRIMARY_STORAGE_ACCOUNT_NAME");
    }

    static String getAccountKey() {
        return ConfigurationManager.getConfiguration().get("PRIMARY_STORAGE_ACCOUNT_KEY");
    }
}

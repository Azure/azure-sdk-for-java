// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.core.util.Configuration;

/**
 * This is a helper class of frequently used methods for sample codes.
 */
class SampleHelper {
    static String getAccountName() {
        return Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_ACCOUNT_NAME");
    }

    static String getAccountKey() {
        return Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_ACCOUNT_KEY");
    }
}

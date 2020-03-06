// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.List;

/**
 * This is a helper class for testing.
 */
public class BridgeUtils {

    public static DatabaseAccount createDatabaseAccount(List<DatabaseAccountLocation> readLocations,
                                                        List<DatabaseAccountLocation> writeLocations,
                                                        boolean useMultipleWriteLocations) {
        DatabaseAccount dbAccount = new DatabaseAccount();
        dbAccount.setEnableMultipleWriteLocations(useMultipleWriteLocations);

        dbAccount.setReadableLocations(readLocations);
        dbAccount.setWritableLocations(writeLocations);

        return dbAccount;
    }

    public static DatabaseAccountLocation createDatabaseAccountLocation(String name, String endpoint) {
        DatabaseAccountLocation dal = new DatabaseAccountLocation();
        dal.setName(name);
        dal.setEndpoint(endpoint);

        return dal;
    }
}

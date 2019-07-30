// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.DatabaseAccountLocation;

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

    public static ConflictResolutionPolicy createConflictResolutionPolicy() {
        return new ConflictResolutionPolicy();
    }

    public static ConflictResolutionPolicy setMode(ConflictResolutionPolicy policy, ConflictResolutionMode mode) {
        policy.mode(mode);
        return policy;
    }

    public static ConflictResolutionPolicy setPath(ConflictResolutionPolicy policy, String path) {
        policy.conflictResolutionPath(path);
        return policy;
    }

    public static ConflictResolutionPolicy setStoredProc(ConflictResolutionPolicy policy, String storedProcLink) {
        policy.conflictResolutionProcedure(storedProcLink);
        return policy;
    }
}

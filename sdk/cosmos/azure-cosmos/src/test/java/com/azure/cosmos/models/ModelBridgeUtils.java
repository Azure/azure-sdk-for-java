// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;

import java.util.List;

/**
 * This is a helper class for testing.
 */
public class ModelBridgeUtils {

    public static ConflictResolutionPolicy createConflictResolutionPolicy() {
        return new ConflictResolutionPolicy();
    }

    public static DatabaseAccount createDatabaseAccount(List<DatabaseAccountLocation> readLocations,
                                                        List<DatabaseAccountLocation> writeLocations,
                                                        boolean useMultipleWriteLocations) {
        DatabaseAccount dbAccount = new DatabaseAccount();
        dbAccount.setEnableMultipleWriteLocations(useMultipleWriteLocations);

        dbAccount.setReadableLocations(readLocations);
        dbAccount.setWritableLocations(writeLocations);

        return dbAccount;
    }

    public static ConflictResolutionPolicy setMode(ConflictResolutionPolicy policy, ConflictResolutionMode mode) {
        policy.setMode(mode);
        return policy;
    }

    public static ConflictResolutionPolicy setPath(ConflictResolutionPolicy policy, String path) {
        policy.setConflictResolutionPath(path);
        return policy;
    }

    public static ConflictResolutionPolicy setStoredProc(ConflictResolutionPolicy policy, String storedProcLink) {
        policy.setConflictResolutionProcedure(storedProcLink);
        return policy;
    }
}

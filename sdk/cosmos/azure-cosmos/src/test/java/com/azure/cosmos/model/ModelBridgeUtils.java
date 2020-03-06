// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.model;

/**
 * This is a helper class for testing.
 */
public class ModelBridgeUtils {

    public static ConflictResolutionPolicy createConflictResolutionPolicy() {
        return new ConflictResolutionPolicy();
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

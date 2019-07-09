/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.patch.PatchOperation;
import com.azure.cosmos.patch.PatchOperationCore;
import com.azure.cosmos.patch.implementation.PatchOperationType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PatchUnitTest {

    public static String path = "/random";

    @Test(groups = { "unit" })
    public void throwsOnNullArguement() {

        try {
            PatchOperation.createAddOperation(null, "1");
            Assert.fail();
        } catch(IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("path empty"));
        }

        try {
            PatchOperation.createRemoveOperation(null);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("path empty"));
        }
    }

    @Test(groups = { "unit" })
    public void constructPatchOperationTest() {
        PatchOperation<?> operation = PatchOperation.createAddOperation(path, "string");
        PatchUnitTest.validateOperations(operation, PatchOperationType.ADD, "string");

        Instant current = Instant.now();
        operation = PatchOperation.createAddOperation(path, current);
        PatchUnitTest.validateOperations(operation, PatchOperationType.ADD, current);

        Object object = new  Object();
        operation = PatchOperation.createAddOperation(path, object);
        PatchUnitTest.validateOperations(operation, PatchOperationType.ADD, object);

        operation = PatchOperation.createRemoveOperation(path);
        PatchUnitTest.validateOperations(operation, PatchOperationType.REMOVE, "value not required");

        int[] arrayObject = { 1, 2, 3 };
        operation = PatchOperation.createReplaceOperation(path, arrayObject);
        PatchUnitTest.validateOperations(operation, PatchOperationType.REPLACE, arrayObject);

        UUID uuid = UUID.randomUUID();
        operation = PatchOperation.createSetOperation(path, uuid);
        PatchUnitTest.validateOperations(operation, PatchOperationType.SET, uuid);
    }

    private static <T> void validateOperations(PatchOperation<?> patchOperation, PatchOperationType operationType, T value) {
        assertEquals(operationType, patchOperation.getOperationType());

        if(patchOperation instanceof PatchOperationCore) {
            assertEquals(path, ((PatchOperationCore) patchOperation).getPath());
        }

        if(!operationType.equals(PatchOperationType.REMOVE)) {
            assertEquals(patchOperation.getResource(), value);
        }
    }
}

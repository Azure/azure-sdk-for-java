// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosPatchOperations;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PatchUnitTest {

    public static String path = "/random";

    @Test(groups = { "unit" })
    public void throwsOnNullArgument() {

        try {
            CosmosPatchOperations.create().add(null, "1");
            fail("Should throw IllegalArgumentException");

        } catch(IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }

        try {
            CosmosPatchOperations.create().remove(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }
    }

    @Test(groups = { "unit" })
    public void constructPatchOperation() {
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create().add(path, "string");
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, "string");

        Instant current = Instant.now();
        cosmosPatchOperations = CosmosPatchOperations.create().add(path, current);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, current);

        Object object = new  Object();
        cosmosPatchOperations = CosmosPatchOperations.create().add(path, object);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.ADD, object);

        cosmosPatchOperations = CosmosPatchOperations.create().remove(path);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.REMOVE, "value not required");

        int[] arrayObject = { 1, 2, 3 };
        cosmosPatchOperations = CosmosPatchOperations.create().replace(path, arrayObject);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.REPLACE, arrayObject);

        UUID uuid = UUID.randomUUID();
        cosmosPatchOperations = CosmosPatchOperations.create().set(path, uuid);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.SET, uuid);

        long incr = new Random().nextLong();
        cosmosPatchOperations = CosmosPatchOperations.create().increment(path, incr);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.INCREMENT, incr);

        double value = new Random().nextDouble();
        cosmosPatchOperations = CosmosPatchOperations.create().increment(path, value);
        PatchUnitTest.validateOperations(cosmosPatchOperations, PatchOperationType.INCREMENT, value);
    }

    private static <T> void validateOperations(CosmosPatchOperations cosmosPatchOperations, PatchOperationType operationType, T value) {
        List<PatchOperation> patchOperations = BridgeInternal.getPatchOperationsFromCosmosPatch(cosmosPatchOperations);
        assertThat(patchOperations.size()).isEqualTo(1);

        PatchOperation patchOperation = patchOperations.get(0);
        assertThat(patchOperation.getOperationType()).isEqualTo(operationType);

        if(patchOperation instanceof PatchOperationCore) {
            assertThat(((PatchOperationCore) patchOperation).getPath()).isEqualTo(path);

            if(!operationType.equals(PatchOperationType.REMOVE)) {
                assertThat(((PatchOperationCore) patchOperation).getResource()).isEqualTo(value);
            }
        }
    }
}

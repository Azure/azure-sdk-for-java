// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosPatch;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchUnitTest {

    public static String path = "/random";

    @Test(groups = { "unit" })
    public void throwsOnNullArguement() {

        try {
            CosmosPatch.create().add(null, "1");
            Assertions.fail("Should throw IllegalArgumentException");

        } catch(IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }

        try {
            CosmosPatch.create().remove(null);
            Assertions.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }
    }

    @Test(groups = { "unit" })
    public void constructPatchOperationTest() {
        CosmosPatch cosmosPatch = CosmosPatch.create().add(path, "string");
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, "string");

        Instant current = Instant.now();
        cosmosPatch = CosmosPatch.create().add(path, current);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, current);

        Object object = new  Object();
        cosmosPatch = CosmosPatch.create().add(path, object);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, object);

        cosmosPatch = CosmosPatch.create().remove(path);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.REMOVE, "value not required");

        int[] arrayObject = { 1, 2, 3 };
        cosmosPatch = CosmosPatch.create().replace(path, arrayObject);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.REPLACE, arrayObject);

        UUID uuid = UUID.randomUUID();
        cosmosPatch = CosmosPatch.create().set(path, uuid);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.SET, uuid);

        long incr = new Random().nextLong();
        cosmosPatch = CosmosPatch.create().increment(path, incr);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.INCREMENT, incr);

        double value = new Random().nextDouble();
        cosmosPatch = CosmosPatch.create().increment(path, value);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.INCREMENT, value);
    }

    private static <T> void validateOperations(CosmosPatch cosmosPatch, PatchOperationType operationType, T value) {
        List<PatchOperation> patchOperations = BridgeInternal.getPatchOperationsFromCosmosPatch(cosmosPatch);
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

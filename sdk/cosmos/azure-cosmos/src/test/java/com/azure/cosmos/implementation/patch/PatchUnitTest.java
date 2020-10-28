// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosPatch;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchUnitTest {

    public static String path = "/random";

    @Test(groups = { "unit" })
    public void throwsOnNullArguement() {

        try {
            CosmosPatch.createCosmosPatch().add(null, "1");
            Assertions.fail("Should throw IllegalArgumentException");

        } catch(IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }

        try {
            CosmosPatch.createCosmosPatch().remove(null);
            Assertions.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("path empty");
        }
    }

    @Test(groups = { "unit" })
    public void constructPatchOperationTest() {
        CosmosPatch cosmosPatch = CosmosPatch.createCosmosPatch().add(path, "string");
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, "string");

        Instant current = Instant.now();
        cosmosPatch = CosmosPatch.createCosmosPatch().add(path, current);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, current);

        Object object = new  Object();
        cosmosPatch = CosmosPatch.createCosmosPatch().add(path, object);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.ADD, object);

        cosmosPatch = CosmosPatch.createCosmosPatch().remove(path);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.REMOVE, "value not required");

        int[] arrayObject = { 1, 2, 3 };
        cosmosPatch = CosmosPatch.createCosmosPatch().replace(path, arrayObject);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.REPLACE, arrayObject);

        UUID uuid = UUID.randomUUID();
        cosmosPatch = CosmosPatch.createCosmosPatch().set(path, uuid);
        PatchUnitTest.validateOperations(cosmosPatch, PatchOperationType.SET, uuid);
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

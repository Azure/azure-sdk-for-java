// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.patch.{PatchOperation, PatchOperationCore, PatchOperationType}
import com.fasterxml.jackson.databind.JsonNode

import scala.collection.mutable.ListBuffer

// Only used for ItemPatchBulkUpdate write strategy
class CosmosPatchBulkUpdateOperations {
    private val patchOperations = new ListBuffer[PatchOperationCore[JsonNode]]

    def set(path: String, value: JsonNode): Unit = {
        this.patchOperations += new PatchOperationCore[JsonNode](PatchOperationType.SET, path, value)
    }

    def getPatchOperations(): List[PatchOperation] = {
        this.patchOperations.toList
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark;

import com.azure.cosmos.CosmosItemOperation;
import reactor.core.publisher.EmitterProcessor;

public class JavaUtils {
    public static EmitterProcessor<CosmosItemOperation> createItemOperationEmitter() {
        return EmitterProcessor.create();
    }
}

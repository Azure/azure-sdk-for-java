/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

public abstract class CosmosItemSerializerNoExceptionWrapping extends CosmosItemSerializer {
    public CosmosItemSerializerNoExceptionWrapping() {
        ImplementationBridgeHelpers
            .CosmosItemSerializerHelper
            .getCosmosItemSerializerAccessor()
            .setShouldWrapSerializationExceptions(this, false);
    }
}

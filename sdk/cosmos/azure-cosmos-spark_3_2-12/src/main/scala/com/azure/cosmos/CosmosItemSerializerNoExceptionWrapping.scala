// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos

import com.azure.cosmos.implementation.ImplementationBridgeHelpers

private[cosmos] abstract class CosmosItemSerializerNoExceptionWrapping extends CosmosItemSerializer() {
  ImplementationBridgeHelpers
    .CosmosItemSerializerHelper
    .getCosmosItemSerializerAccessor
    .setShouldWrapSerializationExceptions(this, false)
}

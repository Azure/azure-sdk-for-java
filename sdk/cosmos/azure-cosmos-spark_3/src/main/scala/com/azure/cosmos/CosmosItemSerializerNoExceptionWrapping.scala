// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos

import com.azure.cosmos.implementation.ImplementationBridgeHelpers

private[cosmos] abstract class CosmosItemSerializerNoExceptionWrapping extends CosmosItemSerializer() {
  ImplementationBridgeHelpers
    .CosmosItemSerializerHelper
    .getCosmosItemSerializerAccessor
    .setShouldWrapSerializationExceptions(this, false)

  ImplementationBridgeHelpers
    .CosmosItemSerializerHelper
    .getCosmosItemSerializerAccessor
    .setCanSerialize(this, false)

  // canSerialize is set to false above, so the SDK will never call serialize().
  // This default implementation satisfies the abstract method contract and throws if
  // called unexpectedly.
  override def serialize[T](item: T): java.util.Map[String, AnyRef] = {
    throw new UnsupportedOperationException(
      "serialize() is not supported on CosmosItemSerializerNoExceptionWrapping (canSerialize = false)")
  }
}

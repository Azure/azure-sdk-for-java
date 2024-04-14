// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.types.StructType

/**
 * Spark serializers are not thread-safe - and expensive to create (dynamic code generation)
 * So we will use this object pool to allow reusing serializers based on the targeted schema.
 * The main purpose for pooling serializers (vs. creating new ones in each PartitionReader) is for Structured
 * Streaming scenarios where PartitionReaders for the same schema could be created every couple of 100
 * milliseconds
 * A clean-up task is used to purge serializers for schemas which weren't used anymore
 * For each schema we have an object pool that will use a soft-limit to limit the memory footprint
 */
private object RowSerializerPool {
  private val serializerFactorySingletonInstance =
    new RowSerializerPoolInstance((schema: StructType) => RowEncoder(schema).createSerializer())

  def getOrCreateSerializer(schema: StructType): ExpressionEncoder.Serializer[Row] = {
    serializerFactorySingletonInstance.getOrCreateSerializer(schema)
  }

  def returnSerializerToPool(schema: StructType, serializer: ExpressionEncoder.Serializer[Row]): Boolean = {
    serializerFactorySingletonInstance.returnSerializerToPool(schema, serializer)
  }
}

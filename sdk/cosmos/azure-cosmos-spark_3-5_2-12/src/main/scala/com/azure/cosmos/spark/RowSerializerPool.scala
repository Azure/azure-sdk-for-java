// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.types.StructType

import java.time.Instant

/**
 * Spark serializers are not thread-safe - and expensive to create (dynamic code generation)
 * So we will use this object pool to allow reusing serializers based on the targeted schema.
 * The main purpose for pooling serializers (vs. creating new ones in each PartitionReader) is for Structured
 * Streaming scenarios where PartitionReaders for the same schema could be created every couple of 100
 * milliseconds
 * A clean-up task is used to purge serializers for schemas which weren't used anymore
 * For each schema we have an object pool that will use a soft-limit to limit the memory footprint
 */
private object RowSerializerPool extends RowSerializerPoolBase[RowSerializerQueue] {

    def getOrCreateSerializer(schema: StructType): ExpressionEncoder.Serializer[Row] = {
        schemaScopedSerializerMap.get(schema) match {
            case Some(objectPool) => objectPool.borrowSerializer(schema)
            case None => ExpressionEncoder.apply(schema).createSerializer()
        }
    }

    def returnSerializerToPool(schema: StructType, serializer: ExpressionEncoder.Serializer[Row]): Boolean = {
        schemaScopedSerializerMap.get(schema) match {
            case Some(objectPool) => objectPool.returnSerializer(serializer)
            case None =>
                val newQueue = new RowSerializerQueue()
                newQueue.returnSerializer(serializer)
                schemaScopedSerializerMap.putIfAbsent(schema, newQueue).isEmpty
        }

    }
}

private class RowSerializerQueue extends RowSerializerQueueBase() {

    override def borrowSerializer(schema: StructType): ExpressionEncoder.Serializer[Row] = {
        lastBorrowedAny.set(Instant.now.toEpochMilli)
        Option.apply(objectPool.poll()) match {
            case Some(serializer) =>
                estimatedSize.decrementAndGet()
                serializer
            case None => ExpressionEncoder.apply(schema).createSerializer()
        }
    }
}


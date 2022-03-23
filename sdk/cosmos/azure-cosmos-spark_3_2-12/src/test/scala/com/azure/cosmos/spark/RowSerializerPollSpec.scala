// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class RowSerializerPollSpec extends UnitSpec with BasicLoggingTrait {
  //scalastyle:off multiple.string.literals

  "RowSerializer returned to the pool" should "be reused when capacity not exceeded" in {
    val schema = StructType(Seq(StructField("column_A", IntegerType), StructField("column_B", StringType)))
    val sameSchema = StructType(Seq(StructField("column_A", IntegerType), StructField("column_B", StringType)))

    val serializer = RowSerializerPool.getOrCreateSerializer(schema)
    RowSerializerPool.returnSerializerToPool(schema, serializer) shouldBe true
    val pooledSerializer = RowSerializerPool.getOrCreateSerializer(sameSchema)

    serializer.eq(pooledSerializer) shouldBe true

    val newSerializer = RowSerializerPool.getOrCreateSerializer(sameSchema)
    serializer.eq(newSerializer) shouldBe false
  }

  "RowSerializer " should "be returned to the pool only a limited number of times" in {
    val schema = StructType(Seq(StructField("column01", IntegerType), StructField("column02", StringType)))

    for (_ <- 1 to 256) {
      RowSerializerPool.returnSerializerToPool(schema, RowEncoder(schema).createSerializer()) shouldBe true
    }

    logInfo("First 256 attempt to pool succeeded")

    RowSerializerPool.returnSerializerToPool(schema, RowEncoder(schema).createSerializer()) shouldBe false
  }
  //scalastyle:on multiple.string.literals
}


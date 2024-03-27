// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class RowSerializerPollTest extends RowSerializerPollSpec {
  //scalastyle:off multiple.string.literals

  "RowSerializer " should "be returned to the pool only a limited number of times" in {
    val canRun = Platform.canRunTestAccessingDirectByteBuffer
    assume(canRun._1, canRun._2)

    val schema = StructType(Seq(StructField("column01", IntegerType), StructField("column02", StringType)))

    for (_ <- 1 to 256) {
      RowSerializerPool.returnSerializerToPool(schema, RowEncoder(schema).createSerializer()) shouldBe true
    }

    logInfo("First 256 attempt to pool succeeded")

    RowSerializerPool.returnSerializerToPool(schema, RowEncoder(schema).createSerializer()) shouldBe false
  }
  //scalastyle:on multiple.string.literals
}


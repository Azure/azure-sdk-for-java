// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.google.gson.JsonArray
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{ArrayType, IntegerType, NullType, StringType, StructField, StructType}
import org.assertj.core.api.Assertions.assertThat

class CosmosRowConverterSpec extends UnitSpec {

  "basic spark row" should "translate to ObjectNode" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    assertThat(objectNode.get(colName1).asInt()).isEqualTo(colVal1)
    assertThat(objectNode.get(colName2).asText()).isEqualTo(colVal2)
  }

  "null type in spark row" should "translate to null in ObjectNode" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = null
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    assertThat(objectNode.get(colName1).isNull).isTrue
    assertThat(objectNode.get(colName2).asText()).isEqualTo(colVal2)
  }

  "array in spark row" should "translate to null in ArrayNode" in {

    // TODO add test for array

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = null
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(Seq("arrayElement1", "arrayElement2"), colVal2),
      StructType(Seq(StructField(colName1, ArrayType(StringType, true)), StructField(colName2, StringType))))

    val objectNode = CosmosRowConverter.rowToObjectNode(row)
    assertThat(objectNode.get(colName1).isArray)
    assertThat(objectNode.get(colName1).isInstanceOf[JsonArray]).isTrue
    assertThat(objectNode.get(colName1).asInstanceOf[JsonArray]).hasSize(2)
    assertThat(objectNode.get(colName1).asInstanceOf[JsonArray].get(0)).isEqualTo("arrayElement1")
    assertThat(objectNode.get(colName1).asInstanceOf[JsonArray].get(1)).isEqualTo("arrayElement2")

    assertThat(objectNode.get(colName2).asText()).isEqualTo(colVal2)
  }
}
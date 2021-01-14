// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.ObjectMapper

import java.sql.{Date, Timestamp}
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, BooleanNode, ObjectNode}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{ArrayType, BinaryType, BooleanType, DateType, Decimal, DecimalType, DoubleType, FloatType, IntegerType, LongType, MapType, NullType, StringType, StructField, StructType, TimestampType}
import org.assertj.core.api.Assertions.assertThat

class CosmosRowConverterSpec extends UnitSpec {
    //scalastyle:off null
    //scalastyle:off multiple.string.literals

    val objectMapper = new ObjectMapper();

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
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = "strVal"

        val row = new GenericRowWithSchema(
            Array(Seq("arrayElement1", "arrayElement2"), colVal1),
            StructType(Seq(StructField(colName1, ArrayType(StringType, true)), StructField(colName2, StringType))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        assertThat(objectNode.get(colName1).isArray)
        assertThat(objectNode.get(colName1).asInstanceOf[ArrayNode]).hasSize(2)
        assertThat(objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText()).isEqualTo("arrayElement1")
        assertThat(objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText()).isEqualTo("arrayElement2")

        assertThat(objectNode.get(colName2).asText()).isEqualTo(colVal1)
    }

    "binary in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colVal1 = "strVal".getBytes()

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, BinaryType))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        assertThat(objectNode.get(colName1).isArray)
        val nodeAsBinary = objectNode.get(colName1).asInstanceOf[BinaryNode].binaryValue()
        assertThat(nodeAsBinary).hasSize(colVal1.length)
        for (i <- 0 until colVal1.length)
            assertThat(nodeAsBinary(i)).isEqualTo(colVal1(i))
    }

    "boolean in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colVal1 = true

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, BooleanType))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        val nodeAsBoolean = objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean()
        assertThat(nodeAsBoolean).isEqualTo(colVal1)
    }

    "date and time in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val currentMillis = System.currentTimeMillis()
        val colVal1 = new Date(currentMillis)
        val colVal2 = new Timestamp(colVal1.getTime())

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(StructField(colName1, DateType), StructField(colName2, TimestampType))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        assertThat(objectNode.get(colName1).asLong()).isEqualTo(currentMillis)
        assertThat(objectNode.get(colName2).asLong()).isEqualTo(currentMillis)
    }

    "numeric types in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"

        val colVal1 : Double = 3.5
        val colVal2 : Float = 1e14f
        val colVal3 : Long = 1000000000
        val colVal4 : Decimal = Decimal(4.6)

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2, colVal3, colVal4),
            StructType(Seq(StructField(colName1, DoubleType), StructField(colName2, FloatType),
                StructField(colName3, LongType), StructField(colName4, DecimalType(precision = 2, scale = 2)))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        assertThat(objectNode.get(colName1).asDouble()).isEqualTo(colVal1)
        assertThat(objectNode.get(colName2).asDouble()).isEqualTo(colVal2)
        assertThat(objectNode.get(colName3).asLong()).isEqualTo(colVal3)
        val col4AsDecimal = Decimal(objectNode.get(colName4).asDouble())
        assertThat(col4AsDecimal.compareTo(colVal4)).isEqualTo(0)
    }

    // TODO: Currently RowConverter.scala only works with MapType if the row is RowInternal
    "map in spark row" should "translate to ObjectNode" ignore {
        val colName1 = "testCol1"

        val colVal1 : Map[String, String] = Map("x" -> "a", "y" -> "b")

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, MapType(keyType = StringType, valueType = StringType, valueContainsNull = false)))))

        CosmosRowConverter.rowToObjectNode(row)
    }

    "struct in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"

        val structCol1Name = "structCol"
        val structCol1Val = "testVal"
        val colVal1Definition : StructType = StructType(Seq(StructField(structCol1Name, StringType)))
        val colVal1 = new GenericRowWithSchema(
            Array(structCol1Val),
            colVal1Definition)

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, colVal1Definition))))

        val objectNode = CosmosRowConverter.rowToObjectNode(row)
        assertThat(objectNode.get(colName1).isInstanceOf[ObjectNode])
        val nestedNode = objectNode.get(colName1).asInstanceOf[ObjectNode]
        assertThat(nestedNode.get(structCol1Name).asText()).isEqualTo(structCol1Val)
    }

    "basic ObjectNode" should "translate to InternalRow" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = 8
        val colVal2 = "strVal"

        val schema = StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        assertThat(internalRow.getInt(0)).isEqualTo(colVal1)
        assertThat(internalRow.getString(1)).isEqualTo(colVal2)
    }

    "null type in ObjectNode" should "translate to InternalRow" in {

        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 : String = null
        val colVal2 = "strVal"

        val schema = StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)

        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        assertThat(internalRow.isNullAt(0)).isTrue
        assertThat(internalRow.getString(1)).isEqualTo(colVal2)
    }

    "array in ObjectNode" should "translate to InternalRow" ignore {
        val colName1 = "testCol1"
        val colVal1 : Array[String] = Array("element1", "element2")

        val schema = StructType(Seq(StructField(colName1, ArrayType(StringType, false))))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        val arrayObjectNode = objectMapper.createArrayNode()
        colVal1.foreach(elem => arrayObjectNode.add(elem))
        objectNode.set(colName1, arrayObjectNode)

        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        val arrayNode = internalRow.getArray(0)
        assertThat(arrayNode.numElements()).isEqualTo(colVal1.length)
        for (i <- 0 until colVal1.length)
            assertThat(arrayNode.array(i)).isEqualTo(colVal1(i))
    }

    "binary in ObjectNode" should "translate to InternalRow" in {
        val colName1 = "testCol1"
        val colVal1 = "strVal".getBytes()

        val schema = StructType(Seq(StructField(colName1, BinaryType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        val arrayObjectNode = objectMapper.createArrayNode()
        colVal1.foreach(elem => arrayObjectNode.add(elem))
        objectNode.set(colName1, arrayObjectNode)

        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        val nodeAsBinary = internalRow.getBinary(0)
        for (i <- 0 until colVal1.length)
            assertThat(nodeAsBinary(i)).isEqualTo(colVal1(i))
    }

    "date and time in ObjectNode" should "translate to InternalRow" in {
        val colName1 = "testCol1"
        val colVal1 = System.currentTimeMillis()
        val colVal1AsTime = new Timestamp(colVal1)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        val schema = StructType(Seq(StructField(colName1, TimestampType)))
        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        val asTime = new Timestamp(internalRow.getLong(0))
        assertThat(asTime.compareTo(colVal1AsTime)).isEqualTo(0)
    }

    "nested in ObjectNode" should "translate to InternalRow" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = "testVal1"
        val colVal2 = "testVal2"

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName2, colVal1)
        val nestedObjectNode: ObjectNode = objectNode.putObject(colName1)
        nestedObjectNode.put(colName2, colVal2)
        val schema = StructType(Seq(StructField(colName1, StructType(Seq(StructField(colName2, StringType)))), StructField(colName2, StringType)))
        val internalRow = CosmosRowConverter.toInternalRow(schema, objectNode)
        val asStruct = internalRow.getStruct(0, 1)
        assertThat(asStruct.getString(0)).isEqualTo(colVal2)
        assertThat(internalRow.getString(1)).isEqualTo(colVal1)
    }

  //scalastyle:on null
  //scalastyle:on multiple.string.literals
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.ObjectMapper

import java.sql.{Date, Timestamp}
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, BooleanNode, ObjectNode}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{ArrayType, BinaryType, BooleanType, DateType, Decimal, DecimalType, DoubleType,
    FloatType, IntegerType, LongType, MapType, NullType, StringType, StructField, StructType, TimestampType}

import java.time.{OffsetDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

class CosmosRowConverterSpec extends UnitSpec {
    //scalastyle:off null
    //scalastyle:off multiple.string.literals

    val objectMapper = new ObjectMapper()

    "basic spark row" should "translate to ObjectNode" in {

        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = 8
        val colVal2 = "strVal"

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).asInt() shouldEqual colVal1
        objectNode.get(colName2).asText() shouldEqual colVal2
    }

    "null type in spark row" should "translate to null in ObjectNode" in {

        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = null
        val colVal2 = "strVal"

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).isNull shouldBe true
        objectNode.get(colName2).asText() shouldEqual colVal2
    }

    "array in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = "strVal"

        val row = new GenericRowWithSchema(
            Array(Seq("arrayElement1", "arrayElement2"), colVal1),
            StructType(Seq(StructField(colName1, ArrayType(StringType, true)), StructField(colName2, StringType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).isArray shouldBe true
        objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
        objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText() shouldEqual "arrayElement1"
        objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText() shouldEqual "arrayElement2"
        objectNode.get(colName2).asText() shouldEqual colVal1
    }

    "binary in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colVal1 = "strVal".getBytes()

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, BinaryType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        val nodeAsBinary = objectNode.get(colName1).asInstanceOf[BinaryNode].binaryValue()
        nodeAsBinary should have size colVal1.length
        for (i <- 0 until colVal1.length)
            nodeAsBinary(i) shouldEqual colVal1(i)
    }

    "null in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = null
        val colVal2 = "testVal2"

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(
                StructField(colName1, StringType),
                StructField(colName2, NullType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).isNull shouldBe true
        objectNode.get(colName2).isNull shouldBe true
    }

    "boolean in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colVal1 = true

        val row = new GenericRowWithSchema(
            Array(colVal1),
            StructType(Seq(StructField(colName1, BooleanType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        val nodeAsBoolean = objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean()
        nodeAsBoolean shouldEqual colVal1
    }

    "date and time in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val currentMillis = System.currentTimeMillis()
        val colVal1 = new Date(currentMillis)
        val colVal2 = new Timestamp(colVal1.getTime)

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(StructField(colName1, DateType), StructField(colName2, TimestampType))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).asLong() shouldEqual currentMillis
        objectNode.get(colName2).asLong() shouldEqual currentMillis
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

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).asDouble() shouldEqual colVal1
        objectNode.get(colName2).asDouble() shouldEqual colVal2
        objectNode.get(colName3).asLong() shouldEqual colVal3
        val col4AsDecimal = Decimal(objectNode.get(colName4).asDouble())
        col4AsDecimal.compareTo(colVal4) shouldEqual 0
    }

    "map in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 : Map[String, String] = Map("x" -> "a", "y" -> null)
        val colVal2 : Map[String, Int] = Map("x" -> 20, "y" -> 10)

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2),
            StructType(Seq(
                StructField(colName1, MapType(keyType = StringType, valueType = StringType, valueContainsNull = true)),
                StructField(colName2, MapType(keyType = StringType, valueType = IntegerType, valueContainsNull = false)))))

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        val node1 = objectNode.get(colName1)
        node1.get("x").asText() shouldEqual colVal1("x")
        node1.get("y").isNull shouldBe true
        val node2 = objectNode.get(colName2)
        node2.get("x").asInt() shouldEqual colVal2("x")
        node2.get("y").asInt() shouldEqual colVal2("y")
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

        val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).isInstanceOf[ObjectNode] shouldBe true
        val nestedNode = objectNode.get(colName1).asInstanceOf[ObjectNode]
        nestedNode.get(structCol1Name).asText() shouldEqual structCol1Val
    }

    "basic ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = 8
        val colVal2 = "strVal"

        val schema = StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        row.getInt(0) shouldEqual colVal1
        row.getString(1) shouldEqual colVal2
    }

    "numeric types in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"

        val colVal1 : Double = 3.5
        val colVal2 : Float = 1e14f
        val colVal3 : Long = 1000000000
        val colVal4 : java.math.BigDecimal = new java.math.BigDecimal(4.6)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        objectNode.put(colName3, colVal3)
        objectNode.put(colName4, colVal4)

        val schema = StructType(Seq(StructField(colName1, DoubleType), StructField(colName2, FloatType),
            StructField(colName3, LongType), StructField(colName4, DecimalType(precision = 2, scale = 2))))

        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        row.getDouble(0) shouldEqual colVal1
        row.getDouble(1) shouldEqual colVal2
        row.getLong(2) shouldEqual colVal3
        row.getDecimal(3) shouldEqual colVal4
    }

    "numeric types as strings in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"

        val colVal1 : Double = 3.5
        val colVal2 : Float = 1e14f
        val colVal3 : Long = 1000000000
        val colVal4 : java.math.BigDecimal = new java.math.BigDecimal(4.6)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1.toString)
        objectNode.put(colName2, colVal2.toString)
        objectNode.put(colName3, colVal3.toString)
        objectNode.put(colName4, colVal4.toString)

        val schema = StructType(Seq(StructField(colName1, DoubleType), StructField(colName2, FloatType),
            StructField(colName3, LongType), StructField(colName4, DecimalType(precision = 2, scale = 2))))

        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        row.getDouble(0) shouldEqual colVal1
        row.getFloat(1) shouldEqual colVal2
        row.getLong(2) shouldEqual colVal3
        row.getDecimal(3) shouldEqual colVal4
    }

    "null type in ObjectNode" should "translate to Row" in {

        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 : String = null
        val colVal2 = "strVal"

        val schema = StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)

        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        row.isNullAt(0) shouldBe true
        row.getString(1) shouldEqual colVal2
    }

    "array in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colVal1 : Array[String] = Array("element1", "element2")

        val schema = StructType(Seq(StructField(colName1, ArrayType(StringType, false))))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        val arrayObjectNode = objectMapper.createArrayNode()
        colVal1.foreach(elem => arrayObjectNode.add(elem))
        objectNode.set(colName1, arrayObjectNode)

        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        val arrayNode = row.get(0).asInstanceOf[Array[Any]]
        arrayNode.length shouldEqual colVal1.length
        for (i <- 0 until colVal1.length)
            arrayNode(i) shouldEqual colVal1(i)
    }

    "binary in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colVal1 = "strVal".getBytes()
        val colName2 = "testCol2"
        val colVal2 = "strVal2".getBytes()

        val schema = StructType(Seq(StructField(colName1, BinaryType), StructField(colName2, BinaryType)))
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        val arrayObjectNode = objectMapper.createArrayNode()
        colVal1.foreach(elem => arrayObjectNode.add(elem))
        objectNode.set(colName1, arrayObjectNode)
        objectNode.set(colName2, objectNode.binaryNode(colVal2))

        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        val nodeAsBinary = row.get(0).asInstanceOf[Array[Byte]]
        for (i <- 0 until colVal1.length)
            nodeAsBinary(i) shouldEqual colVal1(i)

        val nodeAsBinary2 = row.get(1).asInstanceOf[Array[Byte]]
        for (i <- 0 until colVal2.length)
            nodeAsBinary2(i) shouldEqual colVal2(i)
    }

    "time in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"
        val colVal1 = System.currentTimeMillis()
        val colVal1AsTime = new Timestamp(colVal1)
        val colVal2 = System.currentTimeMillis()
        val colVal2AsTime = new Timestamp(colVal2)
        val colVal3 = "2021-01-20T20:10:15+01:00"
        val colVal3AsTime = Timestamp.valueOf(OffsetDateTime.parse(colVal3, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime)
        val colVal4 = "2021-01-20T20:10:15Z"
        val ff = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
        val colVal4AsTime = Timestamp.valueOf(OffsetDateTime.parse(colVal4, ff).toLocalDateTime)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        objectNode.put(colName3, colVal3)
        objectNode.put(colName4, colVal4)
        val schema = StructType(Seq(
            StructField(colName1, TimestampType),
            StructField(colName2, TimestampType),
            StructField(colName3, TimestampType),
            StructField(colName4, TimestampType)))
        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        val asTime = row.get(0).asInstanceOf[Timestamp]
        asTime.compareTo(colVal1AsTime) shouldEqual 0
        val asTime2 = row.get(1).asInstanceOf[Timestamp]
        asTime2.compareTo(colVal2AsTime) shouldEqual 0
        val asTime3 = row.get(2).asInstanceOf[Timestamp]
        asTime3.compareTo(colVal3AsTime) shouldEqual 0
        val asTime4 = row.get(3).asInstanceOf[Timestamp]
        asTime4.compareTo(colVal4AsTime) shouldEqual 0
    }

    "date in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"
        val colVal1 = System.currentTimeMillis()
        val colVal1AsTime = new Date(colVal1)
        val colVal2 = System.currentTimeMillis()
        val colVal2AsTime = new Date(colVal2)
        val colVal3 = "2021-01-20T20:10:15+01:00"
        val colVal3AsTime = Date.valueOf(OffsetDateTime.parse(colVal3, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate)
        val colVal4 = "2021-01-20T20:10:15Z"
        val ff = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
        val colVal4AsTime = Date.valueOf(OffsetDateTime.parse(colVal4, ff).toLocalDate)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        objectNode.put(colName3, colVal3)
        objectNode.put(colName4, colVal4)
        val schema = StructType(Seq(
            StructField(colName1, DateType),
            StructField(colName2, DateType),
            StructField(colName3, DateType),
            StructField(colName4, DateType)))
        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        val asTime = row.get(0).asInstanceOf[Date]
        asTime.compareTo(colVal1AsTime) shouldEqual 0
        val asTime2 = row.get(1).asInstanceOf[Date]
        asTime2.compareTo(colVal2AsTime) shouldEqual 0
        val asTime3 = row.get(2).asInstanceOf[Date]
        asTime3.compareTo(colVal3AsTime) shouldEqual 0
        val asTime4 = row.get(3).asInstanceOf[Date]
        asTime4.compareTo(colVal4AsTime) shouldEqual 0
    }

    "nested in ObjectNode" should "translate to Row" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colVal1 = "testVal1"
        val colVal2 = "testVal2"

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        val nestedObjectNode: ObjectNode = objectNode.putObject(colName1)
        nestedObjectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        val schema = StructType(Seq(StructField(colName1, StructType(Seq(StructField(colName1, StringType)))),
            StructField(colName2, StringType)))
        val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode)
        val asStruct = row.getStruct(0)
        asStruct.getString(0) shouldEqual colVal1
        row.getString(1) shouldEqual colVal2
    }

  //scalastyle:on null
  //scalastyle:on multiple.string.literals
}

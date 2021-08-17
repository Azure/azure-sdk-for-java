// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, BooleanNode, ObjectNode}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

import java.sql.{Date, Timestamp}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
// scalastyle:on underscore.import

class CosmosRowConverterSpec extends UnitSpec with BasicLoggingTrait {
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
    val colName3 = "testCol3"
    val colVal1 = "strVal"

    val row = new GenericRowWithSchema(
      Array(
        Seq("arrayElement1", "arrayElement2"),
        ArrayData.toArrayData(Array(1, 2)),
        colVal1),
      StructType(Seq(
        StructField(colName1, ArrayType(StringType, containsNull = false)),
        StructField(colName2, ArrayType(IntegerType, containsNull = false)),
        StructField(colName3, StringType))))

    val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).isArray shouldBe true
    objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText shouldEqual "arrayElement1"
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText shouldEqual "arrayElement2"
    objectNode.get(colName2).isArray shouldBe true
    objectNode.get(colName2).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(0).asInt shouldEqual 1
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(1).asInt shouldEqual 2
    objectNode.get(colName3).asText shouldEqual colVal1
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

    val colVal1: Double = 3.5
    val colVal2: Float = 1e14f
    val colVal3: Long = 1000000000
    val colVal4: Decimal = Decimal(4.6)

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
    val colVal1: Map[String, String] = Map("x" -> "a", "y" -> null)
    val colVal2: Map[String, Int] = Map("x" -> 20, "y" -> 10)

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
    val colName2 = "testCol2"

    val structCol1Name = "structCol"
    val structCol2Name = "structCol2"
    val structCol1Val = "testVal"
    val structCol2Val = "testVal2"
    val colVal1Definition: StructType = StructType(Seq(StructField(structCol1Name, StringType)))
    val colVal2Definition: StructType = StructType(Seq(StructField(structCol2Name, StringType)))
    val colVal1 = new GenericRowWithSchema(
      Array(structCol1Val),
      colVal1Definition)
    val coLVal2 = InternalRow(structCol2Val)

    val row = new GenericRowWithSchema(
      Array(colVal1, coLVal2),
      StructType(Seq(StructField(colName1, colVal1Definition), StructField(colName2, colVal2Definition))))

    val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).isInstanceOf[ObjectNode] shouldBe true
    val nestedNode = objectNode.get(colName1).asInstanceOf[ObjectNode]
    nestedNode.get(structCol1Name).asText() shouldEqual structCol1Val
    val nestedNode2 = objectNode.get(colName2).asInstanceOf[ObjectNode]
    nestedNode2.get(structCol2Name).asText() shouldEqual structCol2Val
  }

  "rawJson in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"
    val sourceObjectNode: ObjectNode = objectMapper.createObjectNode()
    sourceObjectNode.put(colName1, colVal1)
    sourceObjectNode.put(colName2, colVal2)

    val row = new GenericRowWithSchema(
      Array(sourceObjectNode.toString),
      StructType(Seq(StructField(CosmosTableSchemaInferrer.RawJsonBodyAttributeName, StringType))))

    val objectNode = CosmosRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asInt shouldEqual colVal1
    objectNode.get(colName2).asText shouldEqual colVal2
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
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.getInt(0) shouldEqual colVal1
    row.getString(1) shouldEqual colVal2
  }

  "numeric types in ObjectNode" should "translate to Row" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"

    val colVal1: Double = 3.5
    val colVal2: Float = 1e14f
    val colVal3: Long = 1000000000
    val colVal4: java.math.BigDecimal = new java.math.BigDecimal(4.6)

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    objectNode.put(colName2, colVal2)
    objectNode.put(colName3, colVal3)
    objectNode.put(colName4, colVal4)

    val schema = StructType(Seq(StructField(colName1, DoubleType), StructField(colName2, FloatType),
      StructField(colName3, LongType), StructField(colName4, DecimalType(precision = 2, scale = 2))))

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val colVal1: Double = 3.5
    val colVal2: Float = 1e14f
    val colVal3: Long = 1000000000
    val colVal4: java.math.BigDecimal = new java.math.BigDecimal(4.6)

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1.toString)
    objectNode.put(colName2, colVal2.toString)
    objectNode.put(colName3, colVal3.toString)
    objectNode.put(colName4, colVal4.toString)

    val schema = StructType(Seq(StructField(colName1, DoubleType), StructField(colName2, FloatType),
      StructField(colName3, LongType), StructField(colName4, DecimalType(precision = 2, scale = 2))))

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.getDouble(0) shouldEqual colVal1
    row.getFloat(1) shouldEqual colVal2
    row.getLong(2) shouldEqual colVal3
    row.getDecimal(3) shouldEqual colVal4
  }

  "invalid double in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DoubleType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid double in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DoubleType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  "invalid long in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, LongType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid long in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, LongType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  "invalid float in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, FloatType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid float in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, FloatType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  "invalid decimal in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2))))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid decimal in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2))))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  "null for decimal in ObjectNode" should "should not throw when nullable" in {
    val colName1 = "testCol1"
    val colVal1 = ""

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2), nullable = true)))
    try {
      val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(schema)
      val row = CosmosRowConverter.fromObjectNodeToInternalRow(
        schema, rowSerializer, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception =>
        fail("Should not throw exception when property is nullable")
    }
  }

  "null for decimal in ObjectNode" should "should throw when not nullable" in {
    val colName1 = "testCol1"
    val colVal1 = ""

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2), nullable = false)))
    try {
      val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(schema)
      CosmosRowConverter.fromObjectNodeToInternalRow(
        schema, rowSerializer, objectNode, SchemaConversionModes.Relaxed)
      fail("Expected Exception not thrown")
    }
    catch {
      case expectedError: Exception =>
        logInfo("Expected exception", expectedError)
        succeed
    }
  }

  "null type in ObjectNode" should "translate to Row" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1: String = null
    val colVal2 = "strVal"

    val schema = StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType)))
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    objectNode.put(colName2, colVal2)

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.isNullAt(0) shouldBe true
    row.getString(1) shouldEqual colVal2
  }

  "missing attribute in ObjectNode" should "translate to Row" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = "strVal"

    val schema = StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType)))
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.isNullAt(1) shouldBe true
  }

  "array in ObjectNode" should "translate to Row" in {
    val colName1 = "testCol1"
    val colVal1: Array[String] = Array("element1", "element2")

    val schema = StructType(Seq(StructField(colName1, ArrayType(StringType, containsNull = false))))
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val arrayObjectNode = objectMapper.createArrayNode()
    colVal1.foreach(elem => arrayObjectNode.add(elem))
    objectNode.set(colName1, arrayObjectNode)

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    val arrayNode = row.get(0).asInstanceOf[Array[Any]]
    arrayNode.length shouldEqual colVal1.length
    for (i <- colVal1.indices)
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

    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val colVal4AsTime = Timestamp.valueOf(LocalDateTime.parse(colVal4, ff))

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
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    val asTime = row.get(0).asInstanceOf[Timestamp]
    asTime.compareTo(colVal1AsTime) shouldEqual 0
    val asTime2 = row.get(1).asInstanceOf[Timestamp]
    asTime2.compareTo(colVal2AsTime) shouldEqual 0
    val asTime3 = row.get(2).asInstanceOf[Timestamp]
    asTime3.compareTo(colVal3AsTime) shouldEqual 0
    val asTime4 = row.get(3).asInstanceOf[Timestamp]
    asTime4.compareTo(colVal4AsTime) shouldEqual 0
  }

  "invalid time in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, TimestampType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid time in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, TimestampType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
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
    val colVal4AsTime = Date.valueOf(LocalDateTime.parse(colVal4, ff).toLocalDate)

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
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    val asTime = row.get(0).asInstanceOf[Date]
    asTime.compareTo(colVal1AsTime) shouldEqual 0
    val asTime2 = row.get(1).asInstanceOf[Date]
    asTime2.compareTo(colVal2AsTime) shouldEqual 0
    val asTime3 = row.get(2).asInstanceOf[Date]
    asTime3.compareTo(colVal3AsTime) shouldEqual 0
    val asTime4 = row.get(3).asInstanceOf[Date]
    asTime4.compareTo(colVal4AsTime) shouldEqual 0
  }

  "invalid date in ObjectNode" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DateType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "invalid date in ObjectNode" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DateType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
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

    // with struct
    val schema = StructType(Seq(StructField(colName1, StructType(Seq(StructField(colName1, StringType)))),
      StructField(colName2, StringType)))
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    val asStruct = row.getStruct(0)
    asStruct.getString(0) shouldEqual colVal1
    row.getString(1) shouldEqual colVal2

    // with map
    val schemaWithMap = StructType(Seq(
      StructField(colName1, MapType(keyType = StringType, valueType = StringType, valueContainsNull = false)),
      StructField(colName2, StringType)))

    val rowWithMap = CosmosRowConverter.fromObjectNodeToRow(schemaWithMap, objectNode, SchemaConversionModes.Relaxed)

    val convertedMap = rowWithMap.getMap[String, String](0)
    convertedMap(colName1) shouldEqual colVal1
    rowWithMap.getString(1) shouldEqual colVal2
  }

  "raw in ObjectNode" should "translate to Row" in {
    val colName1 = "testCol1"
    val colVal1 = "testVal1"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(StructField(CosmosTableSchemaInferrer.RawJsonBodyAttributeName, StringType)))
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.getString(0) shouldEqual objectNode.toString
  }

  "lsn in ObjectNode" should "translate to Row" in {
    val colName1 = "testCol1"
    val colVal1 = "testVal1"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    objectNode.put(CosmosTableSchemaInferrer.LsnAttributeName, "12345")
    val schemaIncorrectType = StructType(Seq(StructField(CosmosTableSchemaInferrer.LsnAttributeName, StringType)))
    schemaIncorrectType.size shouldEqual 1
    schemaIncorrectType.head.dataType shouldEqual StringType
    val rowIncorrectType = CosmosRowConverter.fromObjectNodeToRow(schemaIncorrectType,
                                                                  objectNode,
                                                                  SchemaConversionModes.Relaxed)
    rowIncorrectType.getString(0) shouldEqual "12345"

    val schema = StructType(Seq(StructField(CosmosTableSchemaInferrer.LsnAttributeName, LongType)))
    schema.size shouldEqual 1
    schema.head.dataType shouldEqual LongType
    val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    row.getLong(0) shouldEqual 12345
  }

  "unknown mapping" should "throw in Strict mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, BinaryType)))
    try {
      CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
      fail("Should have thrown on invalid data")
    }
    catch {
      case _: Exception => succeed
    }
  }

  "unknown mapping" should "return null in Relaxed mode" in {
    val colName1 = "testCol1"
    val colVal1 = "some invalid value"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, BinaryType)))
    try {
      val row = CosmosRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  //scalastyle:on null
  //scalastyle:on multiple.string.literals
}

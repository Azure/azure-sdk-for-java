// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, BooleanNode, ObjectNode}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.CatalystTypeConverters.convertToCatalyst
import org.apache.spark.sql.catalyst.{CatalystTypeConverters, InternalRow}
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

import java.sql.{Date, Timestamp}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.{TimeZone, UUID}
import scala.util.Random

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
// scalastyle:on underscore.import

class CosmosRowConverterSpec extends UnitSpec with BasicLoggingTrait {
  //scalastyle:off null
  //scalastyle:off multiple.string.literals
  //scalastyle:off file.size.limit

  val objectMapper = new ObjectMapper()
  private[this] val defaultRowConverter =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.Always,
        SerializationDateTimeConversionModes.Default
      )
    )
  private[this] val alwaysEpochMsRowConverter =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.Always,
        SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone
      )
    )

  private[this] val alwaysEpochMsRowConverterWithSystemDefaultTimezone =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.Always,
        SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone
      )
    )

  private[this] val alwaysEpochMsRowConverterNonNull =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.NonNull,
        SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone
      )
    )

  private[this] val alwaysEpochMsRowConverterNonNullWithSystemDefaultTimezone =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.NonNull,
        SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone
      )
    )

  private[this] val rowConverterInclusionNonNull =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.NonNull,
        SerializationDateTimeConversionModes.Default
      )
    )

  private[this] val rowConverterInclusionNonDefault =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.NonDefault,
        SerializationDateTimeConversionModes.Default
      )
    )

  private[this] val rowConverterInclusionNonEmpty =
    CosmosRowConverter.get(
      new CosmosSerializationConfig(
        SerializationInclusionModes.NonEmpty,
        SerializationDateTimeConversionModes.Default
      )
    )

  "basic spark row" should "translate to ObjectNode" in {

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, IntegerType), StructField(colName2, StringType))))

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
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

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).isNull shouldBe true
    objectNode.get(colName2).asText() shouldEqual colVal2

    objectNode.toString shouldEqual s"""{"testCol1":null,"testCol2":"strVal"}"""
  }

  "null type in spark row" should "translate to property being skipped if InclusionMode is not Always" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = null
    val colVal2 = "strVal"

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, NullType), StructField(colName2, StringType))))

    val testCases = Array(
      ("NonDefault", rowConverterInclusionNonDefault.fromRowToObjectNode(row)),
      ("NonEmpty", rowConverterInclusionNonEmpty.fromRowToObjectNode(row)),
      ("NonNull", rowConverterInclusionNonNull.fromRowToObjectNode(row))
    )

    for (testCaseAndObjectNodeTuple <- testCases) {
      val testCaseName = testCaseAndObjectNodeTuple._1
      val objectNode = testCaseAndObjectNodeTuple._2
      print(s"TEST CASE: $testCaseName")
      objectNode.get(colName1) shouldBe null
      objectNode.get(colName2).asText() shouldEqual colVal2

      objectNode.toString shouldEqual s"""{"testCol2":"strVal"}"""
    }
  }

  "empty values in spark row" should "translate to property being serialized if InclusionMode is NonNull/Always" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colVal1 = "strVal1"
    val colVal2 = ""
    val colVal3 = Array[Byte]()

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2, colVal3),
      StructType(Seq(
        StructField(colName1, StringType),
        StructField(colName2, StringType),
        StructField(colName3, BinaryType))))

    val testCases = Array(
      ("Always", defaultRowConverter.fromRowToObjectNode(row)),
      ("NonNull", rowConverterInclusionNonNull.fromRowToObjectNode(row))
    )

    for (testCaseAndObjectNodeTuple <- testCases) {
      val testCaseName = testCaseAndObjectNodeTuple._1
      val objectNode = testCaseAndObjectNodeTuple._2
      print(s"TEST CASE: $testCaseName")
      objectNode.get(colName1).asText() shouldEqual colVal1
      objectNode.get(colName2).asText() shouldEqual colVal2
      objectNode.get(colName3).asText() shouldEqual ""

      objectNode.toString shouldEqual s"""{"testCol1":"strVal1","testCol2":"","testCol3":""}"""
    }
  }

  "empty values in spark row" should "translate to property being skipped if InclusionMode is NonDefault/NonEmpty" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colVal1 = "strVal1"
    val colVal2 = ""
    val colVal3 = Array[Byte]()

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2, colVal3),
      StructType(Seq(
        StructField(colName1, StringType),
        StructField(colName2, StringType),
        StructField(colName3, BinaryType))))

    val testCases = Array(
      ("NonEmpty", rowConverterInclusionNonEmpty.fromRowToObjectNode(row)),
      ("NonDefault", rowConverterInclusionNonDefault.fromRowToObjectNode(row))
    )

    for (testCaseAndObjectNodeTuple <- testCases) {
      val testCaseName = testCaseAndObjectNodeTuple._1
      val objectNode = testCaseAndObjectNodeTuple._2
      print(s"TEST CASE: $testCaseName")
      objectNode.get(colName1).asText() shouldEqual colVal1
      objectNode.get(colName2) shouldEqual null
      objectNode.get(colName3) shouldEqual null

      objectNode.toString shouldEqual s"""{"testCol1":"strVal1"}"""
    }
  }

  "default value in spark row" should "translate to property being skipped for InclusionModes.NonDefault" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colName5 = "testCol5"
    val colName6 = "testCol6"
    val colName7 = "testCol7"
    val colName8 = "testCol8"
    val colName9 = "testCol9"
    val colName10 = "testCol10"
    val colName11 = "testCol11"
    val colName12 = "testCol12"
    val colVal1 = 1
    val colVal2 = 0
    val colVal3 = ""
    val colVal4 = Array[Byte]()
    val colVal5: Float = 0
    val colVal6: Float = 0.123f
    val colVal7: Double = 0
    val colVal8: Double = 0.1234
    val colVal9: Short = 0
    val colVal10: Short = 3
    val colVal11: Byte = 0
    val colVal12: Byte = 4

    val row = new GenericRowWithSchema(
      Array(
        colVal1, colVal2, colVal3, colVal4, colVal5, colVal6, colVal7, colVal8, colVal9, colVal10, colVal11, colVal12),
      StructType(Seq(
        StructField(colName1, IntegerType),
        StructField(colName2, IntegerType),
        StructField(colName3, StringType),
        StructField(colName4, BinaryType),
        StructField(colName5, FloatType),
        StructField(colName6, FloatType),
        StructField(colName7, DoubleType),
        StructField(colName8, DoubleType),
        StructField(colName9, ShortType),
        StructField(colName10, ShortType),
        StructField(colName11, ByteType),
        StructField(colName12, ByteType)
      )))

    val objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    objectNode.get(colName1).asInt() shouldEqual colVal1
    objectNode.get(colName2) shouldBe null
    objectNode.get(colName3) shouldBe null
    objectNode.get(colName4) shouldBe null
    objectNode.get(colName5) shouldBe null
    objectNode.get(colName6).asDouble() shouldEqual colVal6.toDouble
    objectNode.get(colName7) shouldBe null
    objectNode.get(colName8).asDouble() shouldEqual colVal8
    objectNode.get(colName9) shouldBe null
    objectNode.get(colName10).asInt() shouldEqual colVal10.toInt
    objectNode.get(colName11) shouldBe null
    objectNode.get(colName12).asInt() shouldEqual colVal12.toInt

    objectNode.toString shouldEqual s"""{"testCol1":1,"testCol6":0.123,"testCol8":0.1234,"testCol10":3,"testCol12":4}"""
  }

  "default value in spark row" should "translate to only empty valuesbeing skipped for InclusionModes.NonEmpty" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colVal1 = 1
    val colVal2 = 0
    val colVal3 = ""
    val colVal4 = Array[Byte]()

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2, colVal3, colVal4),
      StructType(Seq(
        StructField(colName1, IntegerType),
        StructField(colName2, IntegerType),
        StructField(colName3, StringType),
        StructField(colName4, BinaryType)
      )))

    val objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    objectNode.get(colName1).asInt() shouldEqual colVal1
    objectNode.get(colName2).asInt() shouldEqual colVal2
    objectNode.get(colName3) shouldBe null
    objectNode.get(colName4) shouldBe null

    objectNode.toString shouldEqual s"""{"testCol1":1,"testCol2":0}"""
  }

  "array in spark row" should "translate to ObjectNode" in {
    val canRun = Platform.canRunTestAccessingDirectByteBuffer
    assume(canRun._1, canRun._2)

    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colName5 = "testCol5"
    val colVal1 = "strVal"

    val row = new GenericRowWithSchema(
      Array(
        Seq("arrayElement1", "arrayElement2"),
        ArrayData.toArrayData(Array(1, 2)),
        colVal1,
        Seq(),
        ArrayData.toArrayData(Array[Int]())),
      StructType(Seq(
        StructField(colName1, ArrayType(StringType, containsNull = false)),
        StructField(colName2, ArrayType(IntegerType, containsNull = false)),
        StructField(colName3, StringType),
        StructField(colName4, ArrayType(StringType, containsNull = false)),
        StructField(colName5, ArrayType(StringType, containsNull = false))
      ))
    )

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).isArray shouldBe true
    objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText shouldEqual "arrayElement1"
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText shouldEqual "arrayElement2"

    objectNode.get(colName2).isArray shouldBe true
    objectNode.get(colName2).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(0).asInt shouldEqual 1
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(1).asInt shouldEqual 2

    objectNode.get(colName3).asText shouldEqual colVal1

    objectNode.get(colName4).isArray shouldBe true
    objectNode.get(colName4).asInstanceOf[ArrayNode] should have size 0

    objectNode.get(colName5).isArray shouldBe true
    objectNode.get(colName5).asInstanceOf[ArrayNode] should have size 0

    objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    objectNode.get(colName1).isArray shouldBe true
    objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText shouldEqual "arrayElement1"
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText shouldEqual "arrayElement2"

    objectNode.get(colName2).isArray shouldBe true
    objectNode.get(colName2).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(0).asInt shouldEqual 1
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(1).asInt shouldEqual 2

    objectNode.get(colName3).asText shouldEqual colVal1

    objectNode.get(colName4) shouldBe null

    objectNode.get(colName5) shouldBe null

    objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    objectNode.get(colName1).isArray shouldBe true
    objectNode.get(colName1).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(0).asText shouldEqual "arrayElement1"
    objectNode.get(colName1).asInstanceOf[ArrayNode].get(1).asText shouldEqual "arrayElement2"

    objectNode.get(colName2).isArray shouldBe true
    objectNode.get(colName2).asInstanceOf[ArrayNode] should have size 2
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(0).asInt shouldEqual 1
    objectNode.get(colName2).asInstanceOf[ArrayNode].get(1).asInt shouldEqual 2

    objectNode.get(colName3).asText shouldEqual colVal1

    objectNode.get(colName4) shouldBe null

    objectNode.get(colName5) shouldBe null
  }

  "binary in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colVal1 = "strVal".getBytes()

    val row = new GenericRowWithSchema(
      Array(colVal1),
      StructType(Seq(StructField(colName1, BinaryType))))

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
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

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).isNull shouldBe true
    objectNode.get(colName2).isNull shouldBe true

    objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    objectNode.get(colName1) shouldBe null
    objectNode.get(colName2) shouldBe null

    objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    objectNode.get(colName1) shouldBe null
    objectNode.get(colName2) shouldBe null

    objectNode = rowConverterInclusionNonNull.fromRowToObjectNode(row)
    objectNode.get(colName1) shouldBe null
    objectNode.get(colName2) shouldBe null
  }

  "boolean in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = true
    val colVal2 = false

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, BooleanType), StructField(colName2, BooleanType))))

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal1
    objectNode.get(colName2).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal2

    objectNode = rowConverterInclusionNonNull.fromRowToObjectNode(row)
    objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal1
    objectNode.get(colName2).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal2

    objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal1
    objectNode.get(colName2).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal2

    objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    objectNode.get(colName1).asInstanceOf[BooleanNode].asBoolean() shouldEqual colVal1
    objectNode.get(colName2) shouldEqual null
  }

  "date and time in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val currentMillis = System.currentTimeMillis()
    val colVal1 = new Date(currentMillis)
    val colVal2 = new Timestamp(colVal1.getTime)
    val colVal3 = currentMillis.toInt

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2, colVal3, colVal3),
      StructType(Seq(StructField(colName1, DateType),
        StructField(colName2, TimestampType),
        StructField(colName3, DateType),
        StructField(colName4, TimestampType))))

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asLong() shouldEqual currentMillis
    objectNode.get(colName2).asLong() shouldEqual currentMillis
    objectNode.get(colName3).asInt() shouldEqual colVal3
    objectNode.get(colName4).asInt() shouldEqual colVal3
  }

  "date and time in spark row" should "should honor dateTimeConversionMode config" in {
    val canRun = Platform.canRunTestAccessingDirectByteBuffer
    assume(canRun._1, canRun._2)

    val colName1 = "testCol1"
    val colName2 = "testCol2"

    val testDate = LocalDate.of(1945, 12, 12)
    val testTimestamp = new java.sql.Timestamp(
      46, 11, 12, 12, 12, 12, 0)
      .toLocalDateTime.toInstant(ZoneOffset.UTC)

    // Catalyst optimizer will convert java.sql.Date into LocalDate.toEpochDay
    val colVal1Raw = new Date(45, 11, 12)
    convertToCatalyst(colVal1Raw).isInstanceOf[Int] shouldEqual true
    val colVal1= convertToCatalyst(colVal1Raw).asInstanceOf[Int]
    colVal1 shouldEqual -8786
    colVal1 shouldEqual testDate.toEpochDay

    // Catalyst optimizer will convert java.sql.Timestamp into epoch Microseconds
    val colVal2Raw = Timestamp.from(testTimestamp)
    convertToCatalyst(colVal2Raw).isInstanceOf[Long] shouldEqual true
    val colVal2= convertToCatalyst(colVal2Raw).asInstanceOf[Long]
    colVal2 shouldEqual -727530468000000L
    colVal2 shouldEqual ChronoUnit.MICROS.between(Instant.EPOCH, testTimestamp)

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2),
      StructType(Seq(StructField(colName1, DateType),
        StructField(colName2, TimestampType))))

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asLong() shouldEqual colVal1
    objectNode.get(colName2).asLong() shouldEqual colVal2

    objectNode = alwaysEpochMsRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asLong() shouldEqual testDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli
    objectNode.get(colName2).asLong() shouldEqual testTimestamp.toEpochMilli

    val originalDefaultTimezone = java.time.ZoneId.systemDefault
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
      java.time.ZoneId.systemDefault().getId shouldEqual "America/Los_Angeles"
      objectNode = alwaysEpochMsRowConverterWithSystemDefaultTimezone.fromRowToObjectNode(row)
      objectNode.get(colName1).asLong() shouldEqual testDate
        .atStartOfDay()
        .toInstant(TimeZone.getTimeZone("America/Los_Angeles").toZoneId.getRules.getOffset(Instant.now))
        .toEpochMilli
      objectNode.get(colName2).asLong() shouldEqual testTimestamp.toEpochMilli
    } finally {
      TimeZone.setDefault(TimeZone.getTimeZone(originalDefaultTimezone.getId))
    }

    objectNode = alwaysEpochMsRowConverterNonNull.fromRowToObjectNode(row)
    objectNode.get(colName1).asLong() shouldEqual testDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli
    objectNode.get(colName2).asLong() shouldEqual testTimestamp.toEpochMilli

    try {
      TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
      java.time.ZoneId.systemDefault().getId shouldEqual "America/Los_Angeles"
      objectNode = alwaysEpochMsRowConverterNonNullWithSystemDefaultTimezone.fromRowToObjectNode(row)
      objectNode.get(colName1).asLong() shouldEqual testDate
        .atStartOfDay()
        .toInstant(TimeZone.getTimeZone("America/Los_Angeles").toZoneId.getRules.getOffset(Instant.now))
        .toEpochMilli
      objectNode.get(colName2).asLong() shouldEqual testTimestamp.toEpochMilli
    } finally {
      TimeZone.setDefault(TimeZone.getTimeZone(originalDefaultTimezone.getId))
    }
  }

  "numeric types in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colName5 = "testCol5"
    val colName6 = "testCol6"
    val colName7 = "testCol7"
    val colName8 = "testCol8"
    val colName9 = "testCol9"
    val colName10 = "testCol10"
    val colName11 = "testCol11"
    val colName12 = "testCol12"
    val colName13 = "testCol13"
    val colName14 = "testCol14"
    val colName15 = "testCol15"
    val colName16 = "testCol16"

    val colVal1: Double = 3.5
    val colVal2: Float = 1e14f
    val colVal3: Long = 1000000000
    val colVal4: Decimal = Decimal(4.6)
    val colVal5: java.math.BigDecimal = new java.math.BigDecimal(4.6)
    val colVal6: Int = 1234
    val colVal7: Short = 123
    val colVal8: Byte = 12
    val colVal9: Double = 0
    val colVal10: Float = 0
    val colVal11: Long = 0
    val colVal12: Decimal = Decimal(0)
    val colVal13: java.math.BigDecimal = new java.math.BigDecimal(0)
    val colVal14: Int = 0
    val colVal15: Short = 0
    val colVal16: Byte = 0

    val row = new GenericRowWithSchema(
      Array(
        colVal1, colVal2, colVal3, colVal4, colVal5, colVal6, colVal7, colVal8,
        colVal9, colVal10, colVal11, colVal12, colVal13, colVal14, colVal15, colVal16
      ),
      StructType(Seq(
        StructField(colName1, DoubleType),
        StructField(colName2, FloatType),
        StructField(colName3, LongType),
        StructField(colName4, DecimalType(precision = 2, scale = 2)),
        StructField(colName5, DecimalType.SYSTEM_DEFAULT),
        StructField(colName6, IntegerType),
        StructField(colName7, ShortType),
        StructField(colName8, ByteType),
        StructField(colName9, DoubleType),
        StructField(colName10, FloatType),
        StructField(colName11, LongType),
        StructField(colName12, DecimalType(precision = 2, scale = 2)),
        StructField(colName13, DecimalType.SYSTEM_DEFAULT),
        StructField(colName14, IntegerType),
        StructField(colName15, ShortType),
        StructField(colName16, ByteType)
      )))

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asDouble() shouldEqual colVal1
    objectNode.get(colName2).asDouble() shouldEqual colVal2
    objectNode.get(colName3).asLong() shouldEqual colVal3
    Decimal(objectNode.get(colName4).asDouble()).compareTo(colVal4) shouldEqual 0
    new java.math.BigDecimal(objectNode.get(colName5).asDouble()).compareTo(colVal5) shouldEqual 0
    objectNode.get(colName6).asInt() shouldEqual colVal6
    objectNode.get(colName7).asInt() shouldEqual colVal7.toInt
    objectNode.get(colName8).asInt() shouldEqual colVal8.toInt
    objectNode.get(colName9).asDouble() shouldEqual colVal9
    objectNode.get(colName10).asDouble() shouldEqual colVal10
    objectNode.get(colName11).asLong() shouldEqual colVal11
    Decimal(objectNode.get(colName12).asDouble()).compareTo(colVal12) shouldEqual 0
    new java.math.BigDecimal(objectNode.get(colName13).asDouble()).compareTo(colVal13) shouldEqual 0
    objectNode.get(colName14).asInt() shouldEqual colVal14
    objectNode.get(colName15).asInt() shouldEqual colVal15.toInt
    objectNode.get(colName16).asInt() shouldEqual colVal16.toInt

    objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    objectNode.get(colName1).asDouble() shouldEqual colVal1
    objectNode.get(colName2).asDouble() shouldEqual colVal2
    objectNode.get(colName3).asLong() shouldEqual colVal3
    Decimal(objectNode.get(colName4).asDouble()).compareTo(colVal4) shouldEqual 0
    new java.math.BigDecimal(objectNode.get(colName5).asDouble()).compareTo(colVal5) shouldEqual 0
    objectNode.get(colName6).asInt() shouldEqual colVal6
    objectNode.get(colName7).asInt() shouldEqual colVal7.toInt
    objectNode.get(colName8).asInt() shouldEqual colVal8.toInt
    objectNode.get(colName9).asDouble() shouldEqual colVal9
    objectNode.get(colName10).asDouble() shouldEqual colVal10
    objectNode.get(colName11).asLong() shouldEqual colVal11
    Decimal(objectNode.get(colName12).asDouble()).compareTo(colVal12) shouldEqual 0
    new java.math.BigDecimal(objectNode.get(colName13).asDouble()).compareTo(colVal13) shouldEqual 0
    objectNode.get(colName14).asInt() shouldEqual colVal14
    objectNode.get(colName15).asInt() shouldEqual colVal15.toInt
    objectNode.get(colName16).asInt() shouldEqual colVal16.toInt

    objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    objectNode.get(colName1).asDouble() shouldEqual colVal1
    objectNode.get(colName2).asDouble() shouldEqual colVal2
    objectNode.get(colName3).asLong() shouldEqual colVal3
    Decimal(objectNode.get(colName4).asDouble()).compareTo(colVal4) shouldEqual 0
    new java.math.BigDecimal(objectNode.get(colName5).asDouble()).compareTo(colVal5) shouldEqual 0
    objectNode.get(colName6).asInt() shouldEqual colVal6
    objectNode.get(colName7).asInt() shouldEqual colVal7.toInt
    objectNode.get(colName8).asInt() shouldEqual colVal8.toInt
    objectNode.get(colName9) shouldEqual null
    objectNode.get(colName10) shouldEqual null
    objectNode.get(colName11) shouldEqual null
    objectNode.get(colName12) shouldEqual null
    objectNode.get(colName13) shouldEqual null
    objectNode.get(colName14) shouldEqual null
    objectNode.get(colName15) shouldEqual null
    objectNode.get(colName16) shouldEqual null
  }

  "map in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colVal1: Map[String, String] = Map("x" -> "a", "y" -> null)
    val colVal2: Map[String, Int] = Map("x" -> 20, "y" -> 10)
    val colVal3: Map[String, String] = Map()
    val colVal4: Map[String, Int] = Map()

    val row = new GenericRowWithSchema(
      Array(colVal1, colVal2, colVal3, colVal4),
      StructType(Seq(
        StructField(colName1, MapType(keyType = StringType, valueType = StringType, valueContainsNull = true)),
        StructField(colName2, MapType(keyType = StringType, valueType = IntegerType, valueContainsNull = false)),
        StructField(colName3, MapType(keyType = StringType, valueType = StringType, valueContainsNull = true)),
        StructField(colName4, MapType(keyType = StringType, valueType = IntegerType, valueContainsNull = false)))))

    var objectNode = defaultRowConverter.fromRowToObjectNode(row)
    var node1 = objectNode.get(colName1)
    node1.size() shouldEqual 2
    node1.get("x").asText() shouldEqual colVal1("x")
    node1.get("y").isNull shouldBe true
    var node2 = objectNode.get(colName2)
    node2.size() shouldEqual 2
    node2.get("x").asInt() shouldEqual colVal2("x")
    node2.get("y").asInt() shouldEqual colVal2("y")
    var node3 = objectNode.get(colName3)
    node3.size() shouldEqual 0
    var node4 = objectNode.get(colName4)
    node4.size() shouldEqual 0

    objectNode = rowConverterInclusionNonNull.fromRowToObjectNode(row)
    node1 = objectNode.get(colName1)
    node1.size() shouldEqual 1
    node1.get("x").asText() shouldEqual colVal1("x")
    node1.get("y") shouldBe null
    node2 = objectNode.get(colName2)
    node2.size() shouldEqual 2
    node2.get("x").asInt() shouldEqual colVal2("x")
    node2.get("y").asInt() shouldEqual colVal2("y")
    node3 = objectNode.get(colName3)
    node3.size() shouldEqual 0
    node4 = objectNode.get(colName4)
    node4.size() shouldEqual 0

    objectNode = rowConverterInclusionNonEmpty.fromRowToObjectNode(row)
    node1 = objectNode.get(colName1)
    node1.size() shouldEqual 1
    node1.get("x").asText() shouldEqual colVal1("x")
    node1.get("y") shouldBe null
    node2 = objectNode.get(colName2)
    node2.size() shouldEqual 2
    node2.get("x").asInt() shouldEqual colVal2("x")
    node2.get("y").asInt() shouldEqual colVal2("y")
    objectNode.get(colName3) shouldEqual null
    objectNode.get(colName4) shouldEqual null

    objectNode = rowConverterInclusionNonDefault.fromRowToObjectNode(row)
    node1 = objectNode.get(colName1)
    node1.size() shouldEqual 1
    node1.get("x").asText() shouldEqual colVal1("x")
    node1.get("y") shouldBe null
    node2 = objectNode.get(colName2)
    node2.size() shouldEqual 2
    node2.get("x").asInt() shouldEqual colVal2("x")
    node2.get("y").asInt() shouldEqual colVal2("y")
    objectNode.get(colName3) shouldEqual null
    objectNode.get(colName4) shouldEqual null
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

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
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

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asInt shouldEqual colVal1
    objectNode.get(colName2).asText shouldEqual colVal2
  }

  "rawJson in spark InternalRow" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"
    val sourceObjectNode: ObjectNode = objectMapper.createObjectNode()
    sourceObjectNode.put(colName1, colVal1)
    sourceObjectNode.put(colName2, colVal2)

    val row = InternalRow(sourceObjectNode.toString)

    val objectNode = defaultRowConverter.fromInternalRowToObjectNode(
      row,
      StructType(Seq(StructField(CosmosTableSchemaInferrer.RawJsonBodyAttributeName, StringType))))
    objectNode.get(colName1).asInt shouldEqual colVal1
    objectNode.get(colName2).asText shouldEqual colVal2
  }

  "originRawJson in spark row" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"
    val ts = Random.nextInt(100000)
    val etag = UUID.randomUUID().toString
    val sourceObjectNode: ObjectNode = objectMapper.createObjectNode()
    sourceObjectNode.put(colName1, colVal1)
    sourceObjectNode.put(colName2, colVal2)
    sourceObjectNode.put(CosmosTableSchemaInferrer.TimestampAttributeName, ts)
    sourceObjectNode.put(CosmosTableSchemaInferrer.ETagAttributeName, etag)

    val row = new GenericRowWithSchema(
      Array(sourceObjectNode.toString),
      StructType(Seq(StructField(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName, StringType))))

    val objectNode = defaultRowConverter.fromRowToObjectNode(row)
    objectNode.get(colName1).asInt shouldEqual colVal1
    objectNode.get(colName2).asText shouldEqual colVal2
    objectNode.get(CosmosTableSchemaInferrer.OriginTimestampAttributeName).asInt shouldEqual ts
    objectNode.get(CosmosTableSchemaInferrer.OriginETagAttributeName).asText shouldEqual etag
  }

  "originRawJson in spark InternalRow" should "translate to ObjectNode" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = 8
    val colVal2 = "strVal"
    val ts = Random.nextInt(100000)
    val etag = UUID.randomUUID().toString
    val sourceObjectNode: ObjectNode = objectMapper.createObjectNode()
    sourceObjectNode.put(colName1, colVal1)
    sourceObjectNode.put(colName2, colVal2)
    sourceObjectNode.put(CosmosTableSchemaInferrer.TimestampAttributeName, ts)
    sourceObjectNode.put(CosmosTableSchemaInferrer.ETagAttributeName, etag)

    val row = InternalRow(sourceObjectNode.toString)

    val objectNode = defaultRowConverter.fromInternalRowToObjectNode(
      row,
      StructType(Seq(StructField(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName, StringType))))
    objectNode.get(colName1).asInt shouldEqual colVal1
    objectNode.get(colName2).asText shouldEqual colVal2
    objectNode.get(CosmosTableSchemaInferrer.OriginTimestampAttributeName).asInt shouldEqual ts
    objectNode.get(CosmosTableSchemaInferrer.OriginETagAttributeName).asText shouldEqual etag
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
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  "null for decimal in ObjectNode" should "should not throw when nullable" in {
    val canRun = Platform.canRunTestAccessingDirectByteBuffer
    assume(canRun._1, canRun._2)

    val colName1 = "testCol1"
    val colVal1 = ""

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2), nullable = true)))
    try {
      val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(schema)
      val row = defaultRowConverter.fromRowToInternalRow(
        defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed),
        rowSerializer)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception =>
        fail("Should not throw exception when property is nullable")
    }
  }

  "null for decimal in ObjectNode" should "should throw when not nullable" in {
    val canRun = Platform.canRunTestAccessingDirectByteBuffer
    assume(canRun._1, canRun._2)

    val colName1 = "testCol1"
    val colVal1 = ""

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    val schema = StructType(Seq(
      StructField(colName1, DecimalType(precision = 2, scale = 2), nullable = false)))
    try {
      val rowSerializer: ExpressionEncoder.Serializer[Row] = RowSerializerPool.getOrCreateSerializer(schema)
      defaultRowConverter.fromRowToInternalRow(
        defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed),
        rowSerializer)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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

    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
    val colName5 = "testCol5"
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
    val colVal5 = colVal1.toInt

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    objectNode.put(colName2, colVal2)
    objectNode.put(colName3, colVal3)
    objectNode.put(colName4, colVal4)
    objectNode.put(colName5, colVal5)
    val schema = StructType(Seq(
      StructField(colName1, DateType),
      StructField(colName2, DateType),
      StructField(colName3, DateType),
      StructField(colName4, DateType),
      StructField(colName5, DateType)))
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
    val asStruct = row.getStruct(0)
    asStruct.getString(0) shouldEqual colVal1
    row.getString(1) shouldEqual colVal2

    // with map
    val schemaWithMap = StructType(Seq(
      StructField(colName1, MapType(keyType = StringType, valueType = StringType, valueContainsNull = false)),
      StructField(colName2, StringType)))

    val rowWithMap = defaultRowConverter.fromObjectNodeToRow(schemaWithMap, objectNode, SchemaConversionModes.Relaxed)

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
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
    val rowIncorrectType = defaultRowConverter.fromObjectNodeToRow(schemaIncorrectType,
                                                                  objectNode,
                                                                  SchemaConversionModes.Relaxed)
    rowIncorrectType.getString(0) shouldEqual "12345"

    val schema = StructType(Seq(StructField(CosmosTableSchemaInferrer.LsnAttributeName, LongType)))
    schema.size shouldEqual 1
    schema.head.dataType shouldEqual LongType
    val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
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
      defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Strict)
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
      val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
      row.isNullAt(0) shouldBe true
    }
    catch {
      case _: Exception => fail("Should not throw in Relaxed mode")
    }
  }

  //scalastyle:on null
  //scalastyle:on multiple.string.literals
  //scalastyle:on file.size.limit
}

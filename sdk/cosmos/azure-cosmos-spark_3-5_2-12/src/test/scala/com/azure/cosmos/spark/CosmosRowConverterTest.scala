// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.TimestampNTZType

import java.sql.{Date, Timestamp}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime}

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
// scalastyle:on underscore.import

class CosmosRowConverterTest extends UnitSpec with BasicLoggingTrait {
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


    "date and time and TimestampNTZType in spark row" should "translate to ObjectNode" in {
        val colName1 = "testCol1"
        val colName2 = "testCol2"
        val colName3 = "testCol3"
        val colName4 = "testCol4"
        val currentMillis = System.currentTimeMillis()
        val colVal1 = new Date(currentMillis)
        val timestampNTZType =  "2021-07-01T08:43:28.037"
        val colVal2 = LocalDateTime.parse(timestampNTZType, DateTimeFormatter.ISO_DATE_TIME)
        val colVal3 = currentMillis.toInt

        val row = new GenericRowWithSchema(
            Array(colVal1, colVal2, colVal3, colVal3),
            StructType(Seq(StructField(colName1, DateType),
                StructField(colName2, TimestampNTZType),
                StructField(colName3, DateType),
                StructField(colName4, TimestampType))))

        val objectNode = defaultRowConverter.fromRowToObjectNode(row)
        objectNode.get(colName1).asLong() shouldEqual currentMillis
        objectNode.get(colName2).asText() shouldEqual "2021-07-01T08:43:28.037"
        objectNode.get(colName3).asInt() shouldEqual colVal3
        objectNode.get(colName4).asInt() shouldEqual colVal3
    }

    "time and TimestampNTZType in ObjectNode" should "translate to Row" in {
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
        val colVal4 = "2021-07-01T08:43:28.037"
        val colVal4AsTime = LocalDateTime.parse(colVal4, DateTimeFormatter.ISO_DATE_TIME)

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.put(colName1, colVal1)
        objectNode.put(colName2, colVal2)
        objectNode.put(colName3, colVal3)
        objectNode.put(colName4, colVal4)
        val schema = StructType(Seq(
            StructField(colName1, TimestampType),
            StructField(colName2, TimestampType),
            StructField(colName3, TimestampType),
            StructField(colName4, TimestampNTZType)))
        val row = defaultRowConverter.fromObjectNodeToRow(schema, objectNode, SchemaConversionModes.Relaxed)
        val asTime = row.get(0).asInstanceOf[Timestamp]
        asTime.compareTo(colVal1AsTime) shouldEqual 0
        val asTime2 = row.get(1).asInstanceOf[Timestamp]
        asTime2.compareTo(colVal2AsTime) shouldEqual 0
        val asTime3 = row.get(2).asInstanceOf[Timestamp]
        asTime3.compareTo(colVal3AsTime) shouldEqual 0
        val asTime4 = row.get(3).asInstanceOf[LocalDateTime]
        asTime4.compareTo(colVal4AsTime) shouldEqual 0
    }

    //scalastyle:on null
    //scalastyle:on multiple.string.literals
    //scalastyle:on file.size.limit
}

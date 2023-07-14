// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.SchemaConversionModes.SchemaConversionMode
import com.fasterxml.jackson.annotation.JsonInclude.Include
// scalastyle:off underscore.import
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.catalyst.expressions.{UnsafeMapData}
import org.apache.spark.sql.catalyst.util.ArrayData

import java.sql.{Date, Timestamp}
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import scala.util.{Try, Success, Failure}

// scalastyle:off underscore.import
// scalastyle:on underscore.import

// scalastyle:off
private[cosmos] object CosmosRowConverter {

  // TODO: Expose configuration to handle duplicate fields
  // See: https://github.com/Azure/azure-sdk-for-java/pull/18642#discussion_r558638474
  private val rowConverterMap = new TrieMap[CosmosSerializationConfig, CosmosRowConverter]

  def get(serializationConfig: CosmosSerializationConfig) : CosmosRowConverter = {
    rowConverterMap.get(serializationConfig) match {
      case Some(existingRowConverter) => existingRowConverter
      case None =>
        val newRowConverterCandidate = createRowConverter(serializationConfig)
        rowConverterMap.putIfAbsent(serializationConfig, newRowConverterCandidate) match {
          case Some(existingConcurrentlyCreatedRowConverter) => existingConcurrentlyCreatedRowConverter
          case None => newRowConverterCandidate
        }
    }
  }

  private def createRowConverter(serializationConfig: CosmosSerializationConfig): CosmosRowConverter = {
    val objectMapper = new ObjectMapper()
      import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
      objectMapper.registerModule(new JavaTimeModule)
    serializationConfig.serializationInclusionMode match {
      case SerializationInclusionModes.NonNull => objectMapper.setSerializationInclusion(Include.NON_NULL)
      case SerializationInclusionModes.NonEmpty => objectMapper.setSerializationInclusion(Include.NON_EMPTY)
      case SerializationInclusionModes.NonDefault => objectMapper.setSerializationInclusion(Include.NON_DEFAULT)
      case _ => objectMapper.setSerializationInclusion(Include.ALWAYS)
    }

    new CosmosRowConverter(objectMapper, serializationConfig)
  }
}

private[cosmos] class CosmosRowConverter(private val objectMapper: ObjectMapper, private val serializationConfig: CosmosSerializationConfig)
    extends CosmosRowConverterBase(objectMapper, serializationConfig) {

    private val NTZFormatter = DateTimeFormatter.ISO_DATE_TIME

    override def convertSparkDataTypeToJsonNode(fieldType: DataType, rowData: Any): Option[JsonNode] = {
        if (serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonEmpty ||
            serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonDefault) {

            convertSparkDataTypeToJsonNodeConditionally(fieldType, rowData: Any)
        } else {
            Some(convertSparkDataTypeToJsonNodeNonNull(fieldType, rowData: Any))
        }
    }

    override def convertSparkDataTypeToJsonNodeConditionally
    (
        fieldType: DataType,
        rowData: Any
    ): Option[JsonNode] = {

        fieldType match {
            case StringType =>
                val stringValue = convertRowDataToString(rowData)
                if (isDefaultValue(stringValue)) {
                    None
                } else {
                    Some(objectMapper.convertValue(stringValue, classOf[JsonNode]))
                }
            case BinaryType =>
                val blobValue = rowData.asInstanceOf[Array[Byte]]
                if (isDefaultValue(blobValue)) {
                    None
                } else {
                    Some(objectMapper.convertValue(blobValue, classOf[JsonNode]))
                }
            case BooleanType => convertToJsonNodeConditionally(rowData.asInstanceOf[Boolean])
            case DoubleType => convertToJsonNodeConditionally(rowData.asInstanceOf[Double])
            case IntegerType => convertToJsonNodeConditionally(rowData.asInstanceOf[Int])
            case ShortType => convertToJsonNodeConditionally(rowData.asInstanceOf[Short])
            case ByteType => convertToJsonNodeConditionally(rowData.asInstanceOf[Byte])
            case LongType => convertToJsonNodeConditionally(rowData.asInstanceOf[Long])
            case FloatType => convertToJsonNodeConditionally(rowData.asInstanceOf[Float])
            case DecimalType() if rowData.isInstanceOf[Decimal] =>
                convertToJsonNodeConditionally(rowData.asInstanceOf[Decimal].toJavaBigDecimal)
            case DecimalType() if rowData.isInstanceOf[Long] =>
                convertToJsonNodeConditionally(new java.math.BigDecimal(rowData.asInstanceOf[java.lang.Long]))
            case DecimalType() =>
                convertToJsonNodeConditionally(rowData.asInstanceOf[java.math.BigDecimal])
            case DateType if rowData.isInstanceOf[java.lang.Long] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        convertToJsonNodeConditionally(rowData.asInstanceOf[Long])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone =>
                        convertToJsonNodeConditionally(LocalDate
                            .ofEpochDay(rowData.asInstanceOf[Long])
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC).toEpochMilli)
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val localDate = LocalDate
                            .ofEpochDay(rowData.asInstanceOf[Long])
                            .atStartOfDay()
                        val localTimestampInstant = Timestamp.valueOf(localDate).toInstant

                        convertToJsonNodeConditionally(
                            localDate
                                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(localTimestampInstant)).toEpochMilli)
                }
            case DateType if rowData.isInstanceOf[java.lang.Integer] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        convertToJsonNodeConditionally(rowData.asInstanceOf[java.lang.Integer])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone =>
                        convertToJsonNodeConditionally(LocalDate
                            .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC).toEpochMilli)
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val localDate = LocalDate
                            .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                            .atStartOfDay()
                        val localTimestampInstant = Timestamp.valueOf(localDate).toInstant
                        convertToJsonNodeConditionally(
                            localDate
                                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(localTimestampInstant)).toEpochMilli)
                }
            case DateType => convertToJsonNodeConditionally(rowData.asInstanceOf[Date].getTime)
            case TimestampType if rowData.isInstanceOf[java.lang.Long] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        convertToJsonNodeConditionally(rowData.asInstanceOf[java.lang.Long])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone |
                         SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val microsSinceEpoch = rowData.asInstanceOf[java.lang.Long]
                        convertToJsonNodeConditionally(
                            Instant.ofEpochSecond(
                                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                                TimeUnit.MICROSECONDS.toNanos(
                                    Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                                )
                            ).toEpochMilli)
                }
            case TimestampType if rowData.isInstanceOf[java.lang.Integer] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        convertToJsonNodeConditionally(rowData.asInstanceOf[java.lang.Integer])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone |
                         SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val microsSinceEpoch = rowData.asInstanceOf[java.lang.Integer].longValue()
                        convertToJsonNodeConditionally(
                            Instant.ofEpochSecond(
                                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                                TimeUnit.MICROSECONDS.toNanos(
                                    Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                                )
                            ).toEpochMilli)
                }
            case TimestampType => convertToJsonNodeConditionally(rowData.asInstanceOf[Timestamp].getTime)
            case arrayType: ArrayType if rowData.isInstanceOf[ArrayData] =>
                val arrayDataValue = rowData.asInstanceOf[ArrayData]
                if (isDefaultValue(arrayDataValue)) {
                    None
                }
                else {

                    Some(convertSparkArrayToArrayNode(arrayType.elementType, arrayType.containsNull, arrayDataValue))
                }

            case arrayType: ArrayType =>
                val seqValue = rowData.asInstanceOf[Seq[_]]
                if (isDefaultValue(seqValue)) {
                    None
                } else {
                    Some(convertSparkArrayToArrayNode(arrayType.elementType, arrayType.containsNull, seqValue))
                }

            case structType: StructType => Some(rowTypeRouterToJsonArray(rowData, structType))
            case mapType: MapType =>
                mapType.keyType match {
                    case StringType if rowData.isInstanceOf[Map[_, _]] =>
                        val stringKeyMap = convertToStringKeyMap(rowData)
                        if (isDefaultValue(stringKeyMap)) {
                            None
                        } else {
                            Some(convertSparkMapToObjectNode(
                                mapType.valueType,
                                mapType.valueContainsNull,
                                stringKeyMap))
                        }
                    case StringType if rowData.isInstanceOf[UnsafeMapData] =>
                        val unsafeMapDataValue = rowData.asInstanceOf[UnsafeMapData]

                        if (isDefaultValue(unsafeMapDataValue)) {
                            None
                        } else {
                            Some(convertSparkMapToObjectNode(
                                mapType.valueType,
                                mapType.valueContainsNull,
                                unsafeMapDataValue))
                        }
                    case _ =>
                        throw new Exception(s"Cannot cast $rowData into a Json value. MapTypes "
                            + s"must have keys of StringType for conversion Json")
                }
            case _ =>
                throw new Exception(s"Cannot cast $rowData into a Json value. $fieldType has no matching Json value.")
        }
    }

    override def convertSparkDataTypeToJsonNodeNonNull(fieldType: DataType, rowData: Any): JsonNode = {
        fieldType match {
            case StringType => objectMapper.convertValue(convertRowDataToString(rowData), classOf[JsonNode])
            case BinaryType => objectMapper.convertValue(rowData.asInstanceOf[Array[Byte]], classOf[JsonNode])
            case BooleanType => objectMapper.convertValue(rowData.asInstanceOf[Boolean], classOf[JsonNode])
            case DoubleType => objectMapper.convertValue(rowData.asInstanceOf[Double], classOf[JsonNode])
            case ShortType => objectMapper.convertValue(rowData.asInstanceOf[Short], classOf[JsonNode])
            case ByteType => objectMapper.convertValue(rowData.asInstanceOf[Byte], classOf[JsonNode])
            case IntegerType => objectMapper.convertValue(rowData.asInstanceOf[Int], classOf[JsonNode])
            case LongType => objectMapper.convertValue(rowData.asInstanceOf[Long], classOf[JsonNode])
            case FloatType => objectMapper.convertValue(rowData.asInstanceOf[Float], classOf[JsonNode])
            case DecimalType() if rowData.isInstanceOf[Decimal] => objectMapper.convertValue(rowData.asInstanceOf[Decimal].toJavaBigDecimal, classOf[JsonNode])
            case DecimalType() if rowData.isInstanceOf[Long] => objectMapper.convertValue(new java.math.BigDecimal(rowData.asInstanceOf[java.lang.Long]), classOf[JsonNode])
            case DecimalType() => objectMapper.convertValue(rowData.asInstanceOf[java.math.BigDecimal], classOf[JsonNode])
            case DateType if rowData.isInstanceOf[java.lang.Long] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone =>
                        objectMapper.convertValue(
                            LocalDate
                                .ofEpochDay(rowData.asInstanceOf[java.lang.Long])
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC).toEpochMilli,
                            classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val localDate = LocalDate
                            .ofEpochDay(rowData.asInstanceOf[java.lang.Long])
                            .atStartOfDay()
                        val localTimestampInstant = Timestamp.valueOf(localDate).toInstant
                        objectMapper.convertValue(
                            localDate
                                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(localTimestampInstant)).toEpochMilli,
                            classOf[JsonNode])
                }

            case DateType if rowData.isInstanceOf[java.lang.Integer] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        objectMapper.convertValue(rowData.asInstanceOf[java.lang.Integer], classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone =>
                        objectMapper.convertValue(
                            LocalDate
                                .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC).toEpochMilli,
                            classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val localDate = LocalDate
                            .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                            .atStartOfDay()
                        val localTimestampInstant = Timestamp.valueOf(localDate).toInstant
                        objectMapper.convertValue(
                            localDate
                                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(localTimestampInstant)).toEpochMilli,
                            classOf[JsonNode])
                }
            case DateType => objectMapper.convertValue(rowData.asInstanceOf[Date].getTime, classOf[JsonNode])
            case TimestampType | TimestampNTZType if rowData.isInstanceOf[java.lang.Long] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone |
                         SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val microsSinceEpoch = rowData.asInstanceOf[java.lang.Long]
                        objectMapper.convertValue(
                            Instant.ofEpochSecond(
                                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                                TimeUnit.MICROSECONDS.toNanos(
                                    Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                                )
                            ).toEpochMilli,
                            classOf[JsonNode])
                }
            case TimestampType | TimestampNTZType if rowData.isInstanceOf[java.lang.Integer] =>
                serializationConfig.serializationDateTimeConversionMode match {
                    case SerializationDateTimeConversionModes.Default =>
                        objectMapper.convertValue(rowData.asInstanceOf[java.lang.Integer], classOf[JsonNode])
                    case SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone |
                         SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithSystemDefaultTimezone =>
                        val microsSinceEpoch = rowData.asInstanceOf[java.lang.Integer].longValue()
                        objectMapper.convertValue(
                            Instant.ofEpochSecond(
                                TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
                                TimeUnit.MICROSECONDS.toNanos(
                                    Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
                                )
                            ).toEpochMilli,
                            classOf[JsonNode])
                }
            case TimestampType => objectMapper.convertValue(rowData.asInstanceOf[Timestamp].getTime, classOf[JsonNode])
            case TimestampNTZType => objectMapper.convertValue(rowData.asInstanceOf[java.time.LocalDateTime].toString, classOf[JsonNode])
            case arrayType: ArrayType if rowData.isInstanceOf[ArrayData] => convertSparkArrayToArrayNode(arrayType.elementType, arrayType.containsNull, rowData.asInstanceOf[ArrayData])
            case arrayType: ArrayType => convertSparkArrayToArrayNode(arrayType.elementType, arrayType.containsNull, rowData.asInstanceOf[Seq[_]])
            case structType: StructType => rowTypeRouterToJsonArray(rowData, structType)
            case mapType: MapType =>
                mapType.keyType match {
                    case StringType if rowData.isInstanceOf[Map[_, _]] =>
                        val stringKeyMap = convertToStringKeyMap(rowData)
                        convertSparkMapToObjectNode(
                            mapType.valueType,
                            mapType.valueContainsNull,
                            stringKeyMap)
                    case StringType if rowData.isInstanceOf[UnsafeMapData] =>
                        convertSparkMapToObjectNode(
                            mapType.valueType,
                            mapType.valueContainsNull,
                            rowData.asInstanceOf[UnsafeMapData])
                    case _ =>
                        throw new Exception(s"Cannot cast $rowData into a Json value. MapTypes "
                            + s"must have keys of StringType for conversion Json")
                }
            case _ =>
                throw new Exception(s"Cannot cast $rowData into a Json value. $fieldType has no matching Json value.")
        }
    }

    override def convertToSparkDataType(dataType: DataType,
                                       value: JsonNode,
                                       schemaConversionMode: SchemaConversionMode): Any =
        (value, dataType) match {
            case (_: NullNode, _) | (_, _: NullType) => null
            case (jsonNode: ObjectNode, struct: StructType) =>
                fromObjectNodeToRow(struct, jsonNode, schemaConversionMode)
            case (jsonNode: ObjectNode, map: MapType) =>
                jsonNode.fields().asScala
                    .map(element => (
                        element.getKey,
                        convertToSparkDataType(map.valueType, element.getValue, schemaConversionMode))).toMap
            case (arrayNode: ArrayNode, array: ArrayType) =>
                arrayNode.elements().asScala
                    .map(convertToSparkDataType(array.elementType, _, schemaConversionMode)).toArray
            case (binaryNode: BinaryNode, _: BinaryType) =>
                binaryNode.binaryValue()
            case (arrayNode: ArrayNode, _: BinaryType) =>
                // Assuming the array is of bytes
                objectMapper.convertValue(arrayNode, classOf[Array[Byte]])
            case (_, _: BooleanType) => value.asBoolean()
            case (_, _: StringType) => value.asText()
            case (_, _: DateType) => handleConversionErrors(() => toDate(value), schemaConversionMode)
            case (_, _: TimestampType) => handleConversionErrors(() => toTimestamp(value), schemaConversionMode)
            case (_, _: TimestampNTZType) => handleConversionErrors(() => toTimestampNTZ(value), schemaConversionMode)
            case (isJsonNumber(), DoubleType) => value.asDouble()
            case (isJsonNumber(), DecimalType()) => value.decimalValue()
            case (isJsonNumber(), FloatType) => value.asDouble()
            case (isJsonNumber(), LongType) => value.asLong()
            case (isJsonNumber(), _) => value.asInt()
            case (textNode: TextNode, DoubleType) =>
                handleConversionErrors(() => textNode.asText.toDouble, schemaConversionMode)
            case (textNode: TextNode, DecimalType()) =>
                handleConversionErrors(() => new java.math.BigDecimal(textNode.asText), schemaConversionMode)
            case (textNode: TextNode, FloatType) =>
                handleConversionErrors(() => textNode.asText.toFloat, schemaConversionMode)
            case (textNode: TextNode, LongType) =>
                handleConversionErrors(() => textNode.asText.toLong, schemaConversionMode)
            case (textNode: TextNode, IntegerType) =>
                handleConversionErrors(() => textNode.asText.toInt, schemaConversionMode)
            case _ =>
                if (schemaConversionMode == SchemaConversionModes.Relaxed) {
                    this.logError(s"Unsupported datatype conversion [Value: $value] of ${value.getClass}] to $dataType]")
                    null
                }
                else {
                    throw new IllegalArgumentException(
                        s"Unsupported datatype conversion [Value: $value] of ${value.getClass}] to $dataType]")
                }
        }

        def toTimestampNTZ(value: JsonNode): LocalDateTime = {
            value match {
                case isJsonNumber() =>  LocalDateTime.parse(value.asText())
                case textNode: TextNode =>
                    parseDateTimeNTZFromString(textNode.asText()) match {
                        case Some(odt) => odt
                        case None =>
                            throw new IllegalArgumentException(
                                s"Value '${textNode.asText()} cannot be parsed as LocalDateTime (TIMESTAMP_NTZ).")
                    }
                case _ => LocalDateTime.parse(value.asText())
            }
        }

        private def handleConversionErrors[A] = (conversion: () => A,
                                                 schemaConversionMode: SchemaConversionMode) => {
            Try(conversion()) match {
                case Success(convertedValue) => convertedValue
                case Failure(error) =>
                    if (schemaConversionMode == SchemaConversionModes.Relaxed) {
                        null
                    }
                    else {
                        throw error
                    }
            }
        }

        def parseDateTimeNTZFromString(value: String): Option[LocalDateTime] = {
            try {
                val odt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                Some(odt)
            }
            catch {
                case _: Exception =>
                    try {
                        val odt = LocalDateTime.parse(value, NTZFormatter) //yyyy-MM-ddTHH:mm:ssZ
                        Some(odt)
                    }
                    catch {
                        case _: Exception => None
                    }
            }
        }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{Constants, Utils}
import com.azure.cosmos.spark.CosmosConfigNames.SerializationDateTimeConversionMode
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.SchemaConversionModes.SchemaConversionMode
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.annotation.JsonInclude.Include

import java.sql.{Date, Timestamp}
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, NullNode, ObjectNode, TextNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.{GenericRowWithSchema, UnsafeMapData}
import org.apache.spark.sql.catalyst.util.ArrayData

import java.io.IOException
import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import org.apache.spark.unsafe.types.UTF8String
import scala.util.{Try, Success, Failure}

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
    serializationConfig.serializationInclusionMode match {
      case SerializationInclusionModes.NonNull => objectMapper.setSerializationInclusion(Include.NON_NULL)
      case SerializationInclusionModes.NonEmpty => objectMapper.setSerializationInclusion(Include.NON_EMPTY)
      case SerializationInclusionModes.NonDefault => objectMapper.setSerializationInclusion(Include.NON_DEFAULT)
      case _ => objectMapper.setSerializationInclusion(Include.ALWAYS)
    }

    new CosmosRowConverter(objectMapper, serializationConfig)
  }
}

private[cosmos] class CosmosRowConverter(
                                  private val objectMapper: ObjectMapper,
                                  private val serializationConfig: CosmosSerializationConfig)
    extends BasicLoggingTrait {

    private val skipDefaultValues =
      serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonDefault
    private val FullFidelityChangeFeedMetadataPropertyName = "_metadata"
    private val OperationTypePropertyName = "operationType"
    private val PreviousImagePropertyName = "previousImage"
    private val TimeToLiveExpiredPropertyName = "timeToLiveExpired"

    private val utcFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

    def fromRowToInternalRow(row: Row,
                             rowSerializer: ExpressionEncoder.Serializer[Row]): InternalRow = {
      try {
        rowSerializer.apply(row)
      }
      catch {
        case inner: RuntimeException =>
          throw new Exception(
            s"Cannot convert row into InternalRow",
            inner)
      }
    }

    def ensureObjectNode(jsonNode: JsonNode): ObjectNode = {
      if (jsonNode.isValueNode || jsonNode.isArray) {
        try Utils
          .getSimpleObjectMapper.readTree(s"""{"${Constants.Properties.VALUE}": $jsonNode}""")
          .asInstanceOf[ObjectNode]
        catch {
          case e: IOException =>
            throw new IllegalStateException(s"Unable to parse JSON $jsonNode", e)
        }
      } else {
        jsonNode.asInstanceOf[ObjectNode]
      }
    }

    def fromObjectNodeToRow(schema: StructType,
                            objectNode: ObjectNode,
                            schemaConversionMode: SchemaConversionMode): Row = {
        val values: Seq[Any] = convertStructToSparkDataType(schema, objectNode, schemaConversionMode)
        new GenericRowWithSchema(values.toArray, schema)
    }

    def fromRowToObjectNode(row: Row): ObjectNode = {

      val rawBodyFieldName = if (row.schema.names.contains(CosmosTableSchemaInferrer.RawJsonBodyAttributeName) &&
        row.schema.apply(CosmosTableSchemaInferrer.RawJsonBodyAttributeName).dataType.isInstanceOf[StringType]) {
        Some(CosmosTableSchemaInferrer.RawJsonBodyAttributeName)
      } else if (row.schema.names.contains(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName) &&
        row.schema.apply(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName).dataType.isInstanceOf[StringType]) {
        Some(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName)
      } else {
        None
      }

      if (rawBodyFieldName.isDefined){
        // Special case when the reader read the rawJson
        val rawJson = row.getAs[String](rawBodyFieldName.get)
        convertRawBodyJsonToObjectNode(rawJson, rawBodyFieldName.get)
      } else {
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        row.schema.fields.zipWithIndex.foreach({
          case (field, i) =>
            field.dataType match {
              case _: NullType => putNullConditionally(objectNode, field.name)
              case _ if row.isNullAt(i) => putNullConditionally(objectNode, field.name)
              case _ =>
                val nodeOpt = convertSparkDataTypeToJsonNode(field.dataType, row.get(i))
                if (nodeOpt.isDefined) {
                  objectNode.set(field.name, nodeOpt.get)
                }
            }
        })

        objectNode
      }
    }

    private def convertRawBodyJsonToObjectNode(json: String, rawBodyFieldName: String): ObjectNode = {
      val doc = objectMapper.readTree(json).asInstanceOf[ObjectNode]

      if (rawBodyFieldName == CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName) {
        doc.set(
          CosmosTableSchemaInferrer.OriginETagAttributeName,
          doc.get(CosmosTableSchemaInferrer.ETagAttributeName))
        doc.set(
          CosmosTableSchemaInferrer.OriginTimestampAttributeName,
          doc.get(CosmosTableSchemaInferrer.TimestampAttributeName))
      }

      doc
    }

    def fromInternalRowToObjectNode(row: InternalRow, schema: StructType): ObjectNode = {

      val rawBodyFieldName = if (schema.names.contains(CosmosTableSchemaInferrer.RawJsonBodyAttributeName) &&
        schema.apply(CosmosTableSchemaInferrer.RawJsonBodyAttributeName).dataType.isInstanceOf[StringType]) {
        Some(CosmosTableSchemaInferrer.RawJsonBodyAttributeName)
      } else if (schema.names.contains(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName) &&
        schema.apply(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName).dataType.isInstanceOf[StringType]) {
        Some(CosmosTableSchemaInferrer.OriginRawJsonBodyAttributeName)
      } else {
        None
      }

      if (rawBodyFieldName.isDefined){
        val rawBodyFieldIndex = schema.fieldIndex(rawBodyFieldName.get)
        // Special case when the reader read the rawJson
        val rawJson = convertRowDataToString(row.get(rawBodyFieldIndex, StringType))
        convertRawBodyJsonToObjectNode(rawJson, rawBodyFieldName.get)
      } else {
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        schema.fields.zipWithIndex.foreach({
          case (field, i) =>
            field.dataType match {
              case _: NullType => putNullConditionally(objectNode, field.name)
              case _ if row.isNullAt(i) => putNullConditionally(objectNode, field.name)
              case _ =>
                val nodeOpt = convertSparkDataTypeToJsonNode(field.dataType, row.get(i, field.dataType))
                if (nodeOpt.isDefined) {
                  objectNode.set(field.name, nodeOpt.get)
                }
            }
        })

        objectNode
      }
    }

    private def convertToStringKeyMap(input : Any): Map[String, _] = {
      try {
        input.asInstanceOf[Map[String, _]]
      }
      catch {
        case _: ClassCastException =>
          throw new Exception(
            s"Cannot cast $input into a Json value. MapTypes must have "
              + s"keys of StringType for conversion Json")
      }
    }

    private def convertRowDataToString(rowData: Any) : String = {
      rowData match {
        case str: String =>
          str
        case string: UTF8String =>
          string.toString
        case _ =>
          throw new Exception(s"Cannot cast $rowData into a String.")
      }
    }

    private def convertSparkDataTypeToJsonNode(fieldType: DataType, rowData: Any) : Option[JsonNode] = {
      if (serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonEmpty ||
        serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonDefault) {

        convertSparkDataTypeToJsonNodeConditionally(fieldType, rowData: Any)
      } else {
        Some(convertSparkDataTypeToJsonNodeNonNull(fieldType, rowData: Any))
      }
    }

    private def isDefaultValue(value: Any): Boolean = {
      value match {
        case stringValue: String => stringValue.isEmpty
        case intValue: Int => intValue == 0
        case shortValue: Short => shortValue == 0
        case byteValue: Byte => byteValue == 0
        case longValue: Long => longValue == 0
        case arrayValue: Array[_] => arrayValue.isEmpty
        case booleanValue: Boolean => !booleanValue
        case doubleValue: Double => doubleValue == 0
        case floatValue: Float => floatValue == 0
        case bigDecimalValue : java.math.BigDecimal => bigDecimalValue.compareTo(java.math.BigDecimal.ZERO) == 0
        case arrayDataValue: ArrayData => arrayDataValue.numElements() == 0
        case sequenceValue : Seq[_] => sequenceValue.isEmpty
        case stringMapValue: Map[String,_] => stringMapValue.isEmpty
        case unsafeMapDataValue : UnsafeMapData => unsafeMapDataValue.numElements() == 0
        case _ => throw new Exception("Invalid value type used - can't determine default value")
      }
    }

    private def convertToJsonNodeConditionally[T](value: T) = {
      if (skipDefaultValues && isDefaultValue(value)) {
        None
      } else {
        Some(objectMapper.convertValue(value, classOf[JsonNode]))
      }
    }

    private def convertSparkDataTypeToJsonNodeConditionally
    (
      fieldType: DataType,
      rowData: Any
    ) : Option[JsonNode] = {

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
              convertToJsonNodeConditionally(LocalDate
                .ofEpochDay(rowData.asInstanceOf[Long])
                .atStartOfDay()
                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(Instant.now)).toEpochMilli)
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
              convertToJsonNodeConditionally(LocalDate
                .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                .atStartOfDay()
                .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(Instant.now)).toEpochMilli)
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
                 SerializationDateTimeConversionModes.AlwaysEpochMillisecondsWithUtcTimezone =>
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
          } else {
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

    private def convertSparkDataTypeToJsonNodeNonNull(fieldType: DataType, rowData: Any) : JsonNode = {
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
                  objectMapper.convertValue(
                    LocalDate
                      .ofEpochDay(rowData.asInstanceOf[java.lang.Long])
                      .atStartOfDay()
                      .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(Instant.now)).toEpochMilli,
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
                  objectMapper.convertValue(
                    LocalDate
                      .ofEpochDay(rowData.asInstanceOf[java.lang.Integer].longValue())
                      .atStartOfDay()
                      .toInstant(java.time.ZoneId.systemDefault.getRules().getOffset(Instant.now)).toEpochMilli,
                    classOf[JsonNode])
              }
            case DateType => objectMapper.convertValue(rowData.asInstanceOf[Date].getTime, classOf[JsonNode])
            case TimestampType if rowData.isInstanceOf[java.lang.Long] =>
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
            case TimestampType if rowData.isInstanceOf[java.lang.Integer] =>
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

    private def putNullConditionally(objectNode: ObjectNode, fieldName: String) = {
      if (serializationConfig.serializationInclusionMode == SerializationInclusionModes.Always) {
        objectNode.putNull(fieldName)
      }
    }

    private def convertSparkMapToObjectNode(elementType: DataType, containsNull: Boolean, data: Map[String, Any]) : ObjectNode = {
        val objectNode = objectMapper.createObjectNode()

        data.foreach(x =>
            if (containsNull && x._2 == null) {
                putNullConditionally(objectNode, x._1)
            }
            else {
              val nodeOpt = convertSparkSubItemToJsonNode(elementType, containsNull, x._2)
              if (nodeOpt.isDefined) {
                objectNode.set(x._1, nodeOpt.get)
              }
            })

        objectNode
    }

    private def convertSparkMapToObjectNode(elementType: DataType, containsNull: Boolean, data: UnsafeMapData) : ObjectNode = {
        val objectNode = objectMapper.createObjectNode()

        val keys: Array[String] = data.keyArray().toArray[UTF8String](StringType).map(_.toString)
        val values: Array[AnyRef] = data.valueArray().toObjectArray(elementType)

        keys.zip(values).toMap.foreach(x =>
            if (containsNull && x._2 == null) {
              putNullConditionally(objectNode, x._1)
            }
            else {
              val nodeOpt = convertSparkSubItemToJsonNode(elementType, containsNull, x._2)
              if (nodeOpt.isDefined) {
                objectNode.set(x._1, nodeOpt.get)
              }
            })

        objectNode
    }

    private def convertSparkArrayToArrayNode(elementType: DataType, containsNull: Boolean, data: Seq[Any]): ArrayNode = {
      val arrayNode = objectMapper.createArrayNode()

      data.foreach(value => writeSparkArrayDataToArrayNode(arrayNode, elementType, containsNull, value))

      arrayNode
    }

    private def convertSparkArrayToArrayNode(elementType: DataType, containsNull: Boolean, data: ArrayData): ArrayNode = {
      val arrayNode = objectMapper.createArrayNode()

      data.foreach(elementType, (_, value)
        => writeSparkArrayDataToArrayNode(arrayNode, elementType, containsNull, value))

      arrayNode
    }

    private def writeSparkArrayDataToArrayNode(arrayNode: ArrayNode,
                                               elementType: DataType,
                                               containsNull: Boolean,
                                               value: Any): Unit = {
      if (containsNull && value == null) {
        arrayNode.add(objectMapper.nullNode())
      }
      else {
        val nodeOpt = convertSparkSubItemToJsonNode(elementType, containsNull, value)
        if (nodeOpt.isDefined) {
          arrayNode.add(nodeOpt.get)
        }
      }
    }

    private def convertSparkSubItemToJsonNode
    (
      elementType: DataType,
      containsNull: Boolean,
      data: Any
    ): Option[JsonNode] = {
      if (serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonEmpty ||
        serializationConfig.serializationInclusionMode == SerializationInclusionModes.NonDefault) {

        convertSparkSubItemToJsonNodeConditionally(elementType, containsNull, data: Any)
      } else {
        Some(convertSparkSubItemToJsonNodeNonNull(elementType, containsNull, data: Any))
      }
    }

    private def convertSparkSubItemToJsonNodeNonNull
    (
      elementType: DataType,
      containsNull: Boolean,
      data: Any
    ): JsonNode = {
      elementType match {
        case subDocuments: StructType => rowTypeRouterToJsonArray(data, subDocuments)
        case subArray: ArrayType if data.isInstanceOf[ArrayData]
        => convertSparkArrayToArrayNode(subArray.elementType, containsNull, data.asInstanceOf[ArrayData])
        case subArray: ArrayType
        => convertSparkArrayToArrayNode(subArray.elementType, containsNull, data.asInstanceOf[Seq[_]])
        case _ => convertSparkDataTypeToJsonNodeNonNull(elementType, data)
      }
    }

    private def convertSparkSubItemToJsonNodeConditionally
    (
      elementType: DataType,
      containsNull: Boolean,
      data: Any
    ): Option[JsonNode] = {
        elementType match {
            case subDocuments: StructType => Some(rowTypeRouterToJsonArray(data, subDocuments))
            case subArray: ArrayType if data.isInstanceOf[ArrayData] =>
              val arrayDataValue = data.asInstanceOf[ArrayData]
              if (isDefaultValue(arrayDataValue)) {
                None
              } else {
                Some(convertSparkArrayToArrayNode(subArray.elementType, containsNull, arrayDataValue))
              }
            case subArray: ArrayType =>
              val sequenceData = data.asInstanceOf[Seq[_]]
              if (isDefaultValue(sequenceData)) {
                None
              } else {
                Some(convertSparkArrayToArrayNode(subArray.elementType, containsNull, sequenceData))
              }

            case _ => convertSparkDataTypeToJsonNodeConditionally(elementType, data)
        }
    }

    private def rowTypeRouterToJsonArray(element: Any, schema: StructType) : ObjectNode = {
        element match {
            case e: Row => fromRowToObjectNode(e)
            case e: InternalRow => fromInternalRowToObjectNode(e, schema)
            case _ => throw new Exception(s"Cannot cast $element into a Json value. Struct $element has no matching Json value.")
        }
    }

    private def getFullFidelityMetadata(objectNode: ObjectNode): Option[ObjectNode] = {
      if (objectNode == null) {
        None
      } else {
        val metadata = objectNode.get(FullFidelityChangeFeedMetadataPropertyName)
        if (metadata != null && metadata.isObject) {
          Some(metadata.asInstanceOf[ObjectNode])
        } else {
          None
        }
      }
    }

    private def parsePreviousImage(objectNode: ObjectNode): String = {
      getFullFidelityMetadata(objectNode)
        match {
        case metadataNode: Some[ObjectNode] =>
          metadataNode.get.get(PreviousImagePropertyName) match {
            case previousImageObjectNode: ObjectNode => Option(previousImageObjectNode).map(o => o.toString).orNull
            case _ => null
        }
        case _ => null
      }
    }

  private def parseLsn(objectNode: ObjectNode): Long = {
    objectNode.get(LsnAttributeName)
    match {
      case lsnNode: JsonNode =>
        Option(lsnNode).fold(-1L)(v => v.asLong(-1))
      case _ => -1L
    }
  }

    private def parseTtlExpired(objectNode: ObjectNode): Boolean = {
      getFullFidelityMetadata(objectNode) match {
        case metadataNode: Some[ObjectNode] =>
          metadataNode.get.get(TimeToLiveExpiredPropertyName) match {
            case valueNode: JsonNode =>
              Option(valueNode).fold(false)(v => v.asBoolean(false))
            case _ => false
          }
        case _ => false
      }
    }

    private def parseOperationType(objectNode: ObjectNode): String = {
      getFullFidelityMetadata(objectNode) match {
        case metadataNode: Some[ObjectNode] =>
          metadataNode.get.get(OperationTypePropertyName) match {
            case valueNode: JsonNode =>
              Option(valueNode).fold(null: String)(v => v.asText(null))
            case _ => null
          }
        case _ => null
      }
    }

    private def convertStructToSparkDataType(schema: StructType,
                                             objectNode: ObjectNode,
                                             schemaConversionMode: SchemaConversionMode) : Seq[Any] =
        schema.fields.map {
            case StructField(CosmosTableSchemaInferrer.RawJsonBodyAttributeName, StringType, _, _) =>
                objectNode.toString
            case StructField(CosmosTableSchemaInferrer.PreviousRawJsonBodyAttributeName, StringType, _, _) =>
              parsePreviousImage(objectNode)
            case StructField(CosmosTableSchemaInferrer.OperationTypeAttributeName, StringType, _, _) =>
              parseOperationType(objectNode)
            case StructField(CosmosTableSchemaInferrer.TtlExpiredAttributeName, BooleanType, _, _) =>
              parseTtlExpired(objectNode)
            case StructField(CosmosTableSchemaInferrer.LsnAttributeName, LongType, _, _) =>
              parseLsn(objectNode)
            case StructField(name, dataType, _, _) =>
                Option(objectNode.get(name)).map(convertToSparkDataType(dataType, _, schemaConversionMode)).orNull
        }

    private def convertToSparkDataType(dataType: DataType,
                                       value: JsonNode,
                                       schemaConversionMode: SchemaConversionMode): Any =
      (value, dataType) match {
        case (_ : NullNode, _) | (_, _ : NullType) => null
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

    private def handleConversionErrors[A] = (conversion: () => A,
                                             schemaConversionMode: SchemaConversionMode) => {
      Try(conversion()) match {
        case Success(convertedValue) => convertedValue
        case Failure(error) =>
          if (schemaConversionMode == SchemaConversionModes.Relaxed){
            null
          }
          else {
            throw error
          }
      }
    }

    private def toTimestamp(value: JsonNode): Timestamp = {
        value match {
            case isJsonNumber() => new Timestamp(value.asLong())
            case textNode : TextNode =>
                parseDateTimeFromString(textNode.asText()) match {
                    case Some(odt) => Timestamp.valueOf(odt.toLocalDateTime)
                    case None =>
                      throw new IllegalArgumentException(
                        s"Value '${textNode.asText()} cannot be parsed as Timestamp.")
                }
            case _ => Timestamp.valueOf(value.asText())
        }
    }

    private def toDate(value: JsonNode): Date = {
        value match {
            case isJsonNumber() => new Date(value.asLong())
            case textNode : TextNode =>
                parseDateTimeFromString(textNode.asText()) match {
                    case Some(odt) => Date.valueOf(odt.toLocalDate)
                    case None =>
                      throw new IllegalArgumentException(
                        s"Value '${textNode.asText()} cannot be parsed as Date.")
                }
            case _ => Date.valueOf(value.asText())
        }
    }

    private def parseDateTimeFromString (value: String) : Option[OffsetDateTime] = {
        try {
            val odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME) //yyyy-MM-ddTHH:mm:ss+01:00
            Some(odt)
        }
        catch {
            case _: Exception =>
                try {
                    val odt = OffsetDateTime.parse(value, utcFormatter) //yyyy-MM-ddTHH:mm:ssZ
                    Some(odt)
                }
                catch {
                    case _: Exception => None
                }
        }
    }

    private object isJsonNumber {
        def unapply(x: JsonNode): Boolean = x match {
            case _: com.fasterxml.jackson.databind.node.IntNode
                 | _: com.fasterxml.jackson.databind.node.DecimalNode
                 | _: com.fasterxml.jackson.databind.node.DoubleNode
                 | _: com.fasterxml.jackson.databind.node.FloatNode
                 | _: com.fasterxml.jackson.databind.node.LongNode => true
            case _ => false
        }
    }
}
// scalastyle:on


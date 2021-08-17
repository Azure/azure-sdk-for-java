// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosTableSchemaInferrer.LsnAttributeName
import com.azure.cosmos.spark.SchemaConversionModes.SchemaConversionMode
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.sql.{Date, Timestamp}
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, NullNode, ObjectNode, TextNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.{GenericRowWithSchema, UnsafeMapData}
import org.apache.spark.sql.catalyst.util.ArrayData

import java.time.{OffsetDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import org.apache.spark.unsafe.types.UTF8String
import scala.util.{Try, Success, Failure}

// scalastyle:off multiple.string.literals
// scalastyle:off null
private object CosmosRowConverter
    extends BasicLoggingTrait {

    private val FullFidelityChangeFeedMetadataPropertyName = "_metadata"
    private val OperationTypePropertyName = "operationType"
    private val PreviousImagePropertyName = "previousImage"
    private val TimeToLiveExpiredPropertyName = "timeToLiveExpired"

    // TODO: Expose configuration to handle duplicate fields
    // See: https://github.com/Azure/azure-sdk-for-java/pull/18642#discussion_r558638474
    private val objectMapper = new ObjectMapper()

    private val utcFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

    def fromObjectNodeToInternalRow(schema: StructType,
                                    rowSerializer: ExpressionEncoder.Serializer[Row],
                                    objectNode: ObjectNode,
                                    schemaConversionMode: SchemaConversionMode): InternalRow = {
        val row = fromObjectNodeToRow(schema, objectNode, schemaConversionMode)
        try {
          rowSerializer.apply(row)
        }
        catch {
          case inner: RuntimeException =>
            throw new Exception(
              s"Cannot convert Json '${objectMapper.writeValueAsString(objectNode)}' into InternalRow",
              inner)
        }
    }

    def fromObjectNodeToRow(schema: StructType,
                            objectNode: ObjectNode,
                            schemaConversionMode: SchemaConversionMode): Row = {
        val values: Seq[Any] = convertStructToSparkDataType(schema, objectNode, schemaConversionMode)
        new GenericRowWithSchema(values.toArray, schema)
    }

    def fromRowToObjectNode(row: Row): ObjectNode = {

        if (row.schema.contains(StructField(CosmosTableSchemaInferrer.RawJsonBodyAttributeName, StringType))){
            // Special case when the reader read the rawJson
            val rawJson = row.getAs[String](CosmosTableSchemaInferrer.RawJsonBodyAttributeName)
            objectMapper.readTree(rawJson).asInstanceOf[ObjectNode]
        }
        else {
            val objectNode: ObjectNode = objectMapper.createObjectNode()
            row.schema.fields.zipWithIndex.foreach({
                case (field, i) =>
                    field.dataType match {
                        case _: NullType => objectNode.putNull(field.name)
                        case _ if row.isNullAt(i) => objectNode.putNull(field.name)
                        case _ => objectNode.set(field.name, convertSparkDataTypeToJsonNode(field.dataType, row.get(i)))
                    }
            })

            objectNode
        }
    }

    def fromInternalRowToObjectNode(row: InternalRow, schema: StructType): ObjectNode = {
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        schema.fields.zipWithIndex.foreach({
            case (field, i) =>
                field.dataType match {
                    case _: NullType  => objectNode.putNull(field.name)
                    case _ if row.isNullAt(i) => objectNode.putNull(field.name)
                    case _ => objectNode.set(field.name, convertSparkDataTypeToJsonNode(field.dataType, row.get(i, field.dataType)))
                }
        })

        objectNode
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

    // scalastyle:off
    private def convertSparkDataTypeToJsonNode(fieldType: DataType, rowData: Any) : JsonNode = {
        fieldType match {
            case StringType if rowData.isInstanceOf[String] => objectMapper.convertValue(rowData.asInstanceOf[String], classOf[JsonNode])
            case StringType if rowData.isInstanceOf[UTF8String] => objectMapper.convertValue(rowData.asInstanceOf[UTF8String].toString, classOf[JsonNode])
            case BinaryType => objectMapper.convertValue(rowData.asInstanceOf[Array[Byte]], classOf[JsonNode])
            case BooleanType => objectMapper.convertValue(rowData.asInstanceOf[Boolean], classOf[JsonNode])
            case DoubleType => objectMapper.convertValue(rowData.asInstanceOf[Double], classOf[JsonNode])
            case IntegerType => objectMapper.convertValue(rowData.asInstanceOf[Int], classOf[JsonNode])
            case LongType => objectMapper.convertValue(rowData.asInstanceOf[Long], classOf[JsonNode])
            case FloatType => objectMapper.convertValue(rowData.asInstanceOf[Float], classOf[JsonNode])
            case DecimalType() if rowData.isInstanceOf[Decimal] => objectMapper.convertValue(rowData.asInstanceOf[Decimal].toJavaBigDecimal, classOf[JsonNode])
            case DecimalType() if rowData.isInstanceOf[Long] => objectMapper.convertValue(new java.math.BigDecimal(rowData.asInstanceOf[java.lang.Long]), classOf[JsonNode])
            case DecimalType() => objectMapper.convertValue(rowData.asInstanceOf[java.math.BigDecimal], classOf[JsonNode])
            case DateType if rowData.isInstanceOf[java.lang.Long] => objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
            case DateType => objectMapper.convertValue(rowData.asInstanceOf[Date].getTime, classOf[JsonNode])
            case TimestampType if rowData.isInstanceOf[java.lang.Long] => objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
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
    // scalastyle:on

    private def convertSparkMapToObjectNode(elementType: DataType, containsNull: Boolean, data: Map[String, Any]) : ObjectNode = {
        val objectNode = objectMapper.createObjectNode()

        data.foreach(x =>
            if (containsNull && x._2 == null) {
                objectNode.putNull(x._1)
            }
            else {
                objectNode.set(x._1, convertSparkSubItemToJsonNode(elementType, containsNull, x._2))
            })

        objectNode
    }

    private def convertSparkMapToObjectNode(elementType: DataType, containsNull: Boolean, data: UnsafeMapData) : ObjectNode = {
        val objectNode = objectMapper.createObjectNode()

        val keys: Array[String] = data.keyArray().toArray[UTF8String](StringType).map(_.toString)
        val values: Array[AnyRef] = data.valueArray().toObjectArray(elementType)

        keys.zip(values).toMap.foreach(x =>
            if (containsNull && x._2 == null) {
                objectNode.putNull(x._1)
            }
            else {
                objectNode.set(x._1, convertSparkSubItemToJsonNode(elementType, containsNull, x._2))
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
        arrayNode.add(convertSparkSubItemToJsonNode(elementType, containsNull, value))
      }
    }

    private def convertSparkSubItemToJsonNode(elementType: DataType, containsNull: Boolean, data: Any): JsonNode = {
        elementType match {
            case subDocuments: StructType => rowTypeRouterToJsonArray(data, subDocuments)
            case subArray: ArrayType if data.isInstanceOf[ArrayData]
              => convertSparkArrayToArrayNode(subArray.elementType, containsNull, data.asInstanceOf[ArrayData])
            case subArray: ArrayType
              => convertSparkArrayToArrayNode(subArray.elementType, containsNull, data.asInstanceOf[Seq[_]])
            case _ => convertSparkDataTypeToJsonNode(elementType, data)
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

    // scalastyle:off
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
    // scalastyle:on

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
// scalastyle:on multiple.string.literals
// scalastyle:on null


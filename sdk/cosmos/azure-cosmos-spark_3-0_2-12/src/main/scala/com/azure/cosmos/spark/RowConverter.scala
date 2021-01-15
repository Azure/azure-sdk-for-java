// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.sql.{Date, Timestamp}
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, ObjectNode, NullNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import org.apache.spark.unsafe.types.UTF8String

// scalastyle:off multiple.string.literals
// scalastyle:off null
object CosmosRowConverter
    extends CosmosLoggingTrait {

    val objectMapper = new ObjectMapper()

    def fromObjectNodeToInternalRow(schema: StructType, objectNode: ObjectNode): InternalRow = {
        val row = fromObjectNodeToRow(schema, objectNode)
        RowEncoder(schema).createSerializer().apply(row)
    }

    def fromObjectNodeToRow(schema: StructType, objectNode: ObjectNode): Row = {
        val values: Seq[Any] = convertStructToSparkDataType(schema, objectNode)
        new GenericRowWithSchema(values.toArray, schema)
    }

    def fromRowToObjectNode(row: Row): ObjectNode = {
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        row.schema.fields.zipWithIndex.foreach({
            case (field, i) =>
                field.dataType match {
                    case NullType  | _ if row.isNullAt(i) => objectNode.putNull(field.name)
                    case _ => objectNode.set(field.name, convertSparkDataTypeToJsonNode(field.dataType, row.get(i)))
                }
        })

        objectNode
    }

    def fromInternalRowToObjectNode(row: InternalRow, schema: StructType): ObjectNode = {
        val objectNode: ObjectNode = objectMapper.createObjectNode()
        schema.fields.zipWithIndex.foreach({
            case (field, i) =>
                field.dataType match {
                    case NullType  | _ if row.isNullAt(i) => objectNode.putNull(field.name)
                    case _ => objectNode.set(field.name, convertSparkDataTypeToJsonNode(field.dataType, row.get(i, field.dataType)))
                }
        })

        objectNode
    }

    // scalastyle:off
    private def convertSparkDataTypeToJsonNode(fieldType: DataType, rowData: Any) : JsonNode = {
        fieldType match {
            case StringType if rowData.isInstanceOf[String] => objectMapper.convertValue(rowData.asInstanceOf[String], classOf[JsonNode])
            case StringType if rowData.isInstanceOf[UTF8String] => objectMapper.convertValue(rowData.asInstanceOf[UTF8String], classOf[JsonNode])
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
            case arrayType: ArrayType => convertSparkArrayToArrayNode(arrayType.elementType, arrayType.containsNull, rowData.asInstanceOf[Seq[_]])
            case _: StructType => rowTypeRouterToJsonArray(rowData)
            case mapType: MapType =>
                mapType.keyType match {
                    case StringType =>
                        convertSparkMapToObjectNode(mapType.valueType, mapType.valueContainsNull, rowData.asInstanceOf[Map[String, _]])
                    case _ =>
                        throw new Exception(s"Cannot cast $rowData into a Json value. MapTypes must have keys of StringType for conversion Json")
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

    private def convertSparkArrayToArrayNode(elementType: DataType, containsNull: Boolean, data: Seq[Any]): ArrayNode = {
        val arrayNode = objectMapper.createArrayNode()

        data.foreach(x =>
            if (containsNull && x == null) {
                arrayNode.add(objectMapper.nullNode())
            }
            else {
                arrayNode.add(convertSparkSubItemToJsonNode(elementType, containsNull, x))
            })

        arrayNode
    }

    private def convertSparkSubItemToJsonNode(elementType: DataType, containsNull: Boolean, data: Any): JsonNode = {
        elementType match {
            case _: StructType => rowTypeRouterToJsonArray(data)
            case subArray: ArrayType => convertSparkArrayToArrayNode(subArray.elementType, containsNull, data.asInstanceOf[Seq[_]])
            case _ => convertSparkDataTypeToJsonNode(elementType, data)
        }
    }

    private def rowTypeRouterToJsonArray(element: Any) : ObjectNode = {
        element match {
            case e: Row => fromRowToObjectNode(e)
            case _ => throw new Exception(s"Cannot cast $element into a Json value. Struct $element has no matching Json value.")
        }
    }

    private def convertStructToSparkDataType(schema: StructType, objectNode: ObjectNode) : Seq[Any] =
        schema.fields.map {
            case StructField(name, dataType, _, _) =>
            Option(objectNode.get(name)).map(convertToSparkDataType(dataType, _)).orNull
        }

    // scalastyle:off
    private def convertToSparkDataType(dataType: DataType, value: JsonNode): Any = (value, dataType) match {
        case (_ : NullNode, _) | (_, _ : NullType) => null
        case (jsonNode: ObjectNode, struct: StructType) =>
            fromObjectNodeToRow(struct, jsonNode)
        case (jsonNode: ObjectNode, map: MapType) =>
            jsonNode.fields().asScala
                .map(element => (
                    element.getKey,
                    convertToSparkDataType(map.valueType, element.getValue)))
        case (arrayNode: ArrayNode, array: ArrayType) =>
            arrayNode.elements().asScala.map(convertToSparkDataType(array.elementType, _)).toArray
        case (binaryNode: BinaryNode, _: BinaryType) =>
            binaryNode.binaryValue()
        case (arrayNode: ArrayNode, _: BinaryType) =>
            // Assuming the array is of bytes
            objectMapper.convertValue(arrayNode, classOf[Array[Byte]])
        case (_, _: BooleanType) => value.asBoolean()
        case (_, _: StringType) => value.asText()
        case (_, _: DateType) => toDate(value)
        case (_, _: TimestampType) => toTimestamp(value)
        case (isJsonNumber(), DoubleType) => value.asDouble()
        case (isJsonNumber(), DecimalType()) => value.decimalValue()
        case (isJsonNumber(), FloatType) => value.asDouble()
        case (isJsonNumber(), LongType) => value.asLong()
        case (isJsonNumber(), _) => value.asInt()
        case _ =>
            this.logError(s"Unsupported datatype conversion [Value: $value] of ${value.getClass}] to $dataType]")
            value.asText() // Defaulting to a string representation for values that we cannot convert
    }
    // scalastyle:on

    private def toTimestamp(value: JsonNode): Timestamp = {
        value match {
            case isJsonNumber() => new Timestamp(value.asLong())
            case _ => Timestamp.valueOf(value.asText())
        }
    }

    private def toDate(value: JsonNode): Date = {
        value match {
            case isJsonNumber() => new Date(value.asLong())
            case _ => Date.valueOf(value.asText())
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


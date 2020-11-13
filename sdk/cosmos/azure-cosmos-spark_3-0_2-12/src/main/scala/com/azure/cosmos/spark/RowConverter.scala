// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.sql.{Date, Timestamp}

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.UnsafeMapData
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.types.{BinaryType, BooleanType, DataType, DateType, DecimalType, DoubleType, FloatType, IntegerType, LongType, _}
import org.apache.spark.unsafe.types.UTF8String

/**
 * TODO add more unit tests for this class to CosmosRowConverterSpec.
 */

object CosmosRowConverter
  extends Serializable
    with CosmosLoggingTrait {

  // TODO moderakh make this configurable
  val objectMapper = new ObjectMapper();

  def rowToObjectNode(row: Row): ObjectNode = {
    val objectNode: ObjectNode = objectMapper.createObjectNode();
    row.schema.fields.zipWithIndex.foreach({
      case (field, i) => {
        objectNode.set(field.name, convertToJson(row.get(i), field.dataType, isInternalRow = false))
        objectNode
      }
    })
    objectNode
  }

  def internalRowToObjectNode(internalRow: InternalRow, schema: StructType): ObjectNode = {
    val objectNode: ObjectNode = objectMapper.createObjectNode();
    schema.fields.zipWithIndex.foreach({
      case (field, i) => {
        objectNode.set(field.name, convertToJson(internalRow.get(i, field.dataType), field.dataType, isInternalRow = true))
        objectNode
      }
    })
    objectNode
  }

  private def convertToJson(element: Any, elementType: DataType, isInternalRow: Boolean): JsonNode = {
    elementType match {
      case BinaryType => objectMapper.convertValue(element.asInstanceOf[Array[Byte]], classOf[JsonNode])
      case BooleanType => objectMapper.convertValue(element.asInstanceOf[Boolean], classOf[JsonNode])
      case DateType => objectMapper.convertValue(element.asInstanceOf[Date].getTime, classOf[JsonNode])
      case DoubleType => objectMapper.convertValue(element.asInstanceOf[Double], classOf[JsonNode])
      case IntegerType => objectMapper.convertValue(element.asInstanceOf[Int], classOf[JsonNode])
      case LongType => objectMapper.convertValue(element.asInstanceOf[Long], classOf[JsonNode])
      case FloatType => objectMapper.convertValue(element.asInstanceOf[Float], classOf[JsonNode])
      case NullType => objectMapper.convertValue(null, classOf[JsonNode])
      case DecimalType() => if (element.isInstanceOf[Decimal]) {
        objectMapper.convertValue(element.asInstanceOf[Decimal].toJavaBigDecimal, classOf[JsonNode])
      } else if (element.isInstanceOf[java.lang.Long]) {
        objectMapper.convertValue(new java.math.BigDecimal(element.asInstanceOf[java.lang.Long]), classOf[JsonNode])
      } else {
        objectMapper.convertValue(element.asInstanceOf[java.math.BigDecimal], classOf[JsonNode])
      }
      case StringType =>
        if (isInternalRow) {
          objectMapper.convertValue(element.asInstanceOf[UTF8String].toString, classOf[JsonNode])
        } else {
          objectMapper.convertValue(element.asInstanceOf[String], classOf[JsonNode])
        }
      case TimestampType => if (element.isInstanceOf[java.lang.Long]) {
        objectMapper.convertValue(element.asInstanceOf[java.lang.Long], classOf[JsonNode])
      } else {
        objectMapper.convertValue(element.asInstanceOf[Timestamp].getTime, classOf[JsonNode])
      }
      case arrayType: ArrayType => arrayTypeRouterToJsonArray(arrayType.elementType, element, isInternalRow)

      case mapType: MapType if isInternalRow =>
        mapType.keyType match {
          case StringType =>
            // convert from UnsafeMapData to scala Map
            val unsafeMap = element.asInstanceOf[UnsafeMapData]
            val keys: Array[String] = unsafeMap.keyArray().toArray[UTF8String](StringType).map(_.toString)
            val values: Array[AnyRef] = unsafeMap.valueArray().toObjectArray(mapType.valueType)
            mapTypeToObjectNode(mapType.valueType, keys.zip(values).toMap, isInternalRow)
          case _ => throw new Exception(
            s"Cannot cast $element into a Json value. MapTypes must have keys of StringType for conversion into a Document"
          )
        }

      case structType: StructType => rowTypeRouterToJsonArray(element, structType)
      case _ =>
        throw new Exception(s"Cannot cast $element into a Json value. $elementType has no matching Json value.")
    }
  }

  private def mapTypeToObjectNode(valueType: DataType, data: Map[String, Any], isInternalRow: Boolean): ObjectNode = {
    val jsonObject: ObjectNode = objectMapper.createObjectNode();
    valueType match {
      case subDocuments: StructType => data.map(kv => jsonObject.put(kv._1, rowTypeRouterToJsonArray(kv._2, subDocuments)))
      case subArray: ArrayType => data.map(kv => jsonObject.put(kv._1, arrayTypeRouterToJsonArray(subArray.elementType, kv._2, isInternalRow)))
      case _ => data.map(kv => jsonObject.put(kv._1, convertToJson(kv._2, valueType, isInternalRow)))
    }
    jsonObject
  }

  private def arrayTypeRouterToJsonArray(elementType: DataType, data: Any, isInternalRow: Boolean): ArrayNode = {
    data match {
      case d: Seq[_] => arrayTypeToJsonArray(elementType, d, isInternalRow)
      case d: ArrayData => arrayDataTypeToJsonArray(elementType, d, isInternalRow)
      case _ => throw new Exception(s"Cannot cast $data into a Json value. ArrayType $elementType has no matching Json value.")
    }
  }

  private def arrayTypeToJsonArray(elementType: DataType, data: Seq[Any], isInternalRow: Boolean): ArrayNode = {
    val arrayNode = objectMapper.createArrayNode()

    elementType match {
      case subDocuments: StructType => data.foreach(x => arrayNode.add(rowTypeRouterToJsonArray(x, subDocuments)))
      case subArray: ArrayType => data.foreach(x => arrayNode.add(arrayTypeRouterToJsonArray(subArray.elementType, x, isInternalRow)))
      case _ => data.foreach(x => arrayNode.add(convertToJson(x, elementType, isInternalRow)))
    }
    // When constructing the JSONArray, the internalData should contain JSON-compatible objects in order for the schema to be mantained.
    // Otherwise, the data will be converted into String.
    arrayNode
  }

  private def arrayDataTypeToJsonArray(elementType: DataType, data: ArrayData, isInternalRow: Boolean): ArrayNode = {
    val arrayNode = objectMapper.createArrayNode()

    elementType match {
      case subDocuments: StructType => data.foreach(elementType, (_, x) => arrayNode.add(rowTypeRouterToJsonArray(x, subDocuments)))
      case subArray: ArrayType => data.foreach(elementType, (_, x) => arrayNode.add(arrayTypeRouterToJsonArray(subArray.elementType, x, isInternalRow)))
      case _ => data.foreach(elementType, (_, x) => arrayNode.add(convertToJson(x, elementType, isInternalRow)))
    }
    // When constructing the JSONArray, the internalData should contain JSON-compatible objects in order for the schema to be mantained.
    // Otherwise, the data will be converted into String.
    arrayNode
  }

  private def rowTypeRouterToJsonArray(element: Any, schema: StructType) : ObjectNode = {
    element match {
      case e: Row => rowToObjectNode(e)
      case e: InternalRow => internalRowToObjectNode(e, schema)
      case _ => throw new Exception(s"Cannot cast $element into a Json value. Struct $element has no matching Json value.")
    }
  }
}

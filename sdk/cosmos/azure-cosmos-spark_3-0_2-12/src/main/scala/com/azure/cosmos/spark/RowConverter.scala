// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.sql.{Date, Timestamp}

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.UnsafeMapData
import org.apache.spark.sql.types.{BinaryType, BooleanType, DataType, DateType, DecimalType, DoubleType, FloatType, IntegerType, LongType, _}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.unsafe.types.UTF8String

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * TODO add more unit tests for this class to CosmosRowConverterSpec.
 */

object CosmosRowConverter
  extends Serializable
    with CosmosLoggingTrait {

  // TODO moderakh make this configurable
  val objectMapper = new ObjectMapper();

  def rowToObjectNode(row: Row): ObjectNode = {

    val jsonObject: ObjectNode = objectMapper.createObjectNode();
    row.schema.fields.zipWithIndex.foreach({
      case (field, i) => {
        val jsonValue = convertToJson(row.get(i), field.dataType, isInternalRow = false)
        addJsonPrimitive(jsonValue, field.name, jsonObject)
      }
    })
    jsonObject
  }

  def internalRowToObjectNode(internalRow: InternalRow, schema: StructType): ObjectNode = {
    val jsonObject: ObjectNode = objectMapper.createObjectNode();
    schema.fields.zipWithIndex.foreach({
      case (field, i) => {
        val jsonValue = convertToJson(internalRow.get(i, field.dataType), field.dataType, isInternalRow = true)
        addJsonPrimitive(jsonValue, field.name, jsonObject)
      }
    })
    jsonObject
  }

  private def addJsonPrimitive(jsonValue: Any, fieldName: String, objectNode : ObjectNode) : Unit = {
    jsonValue match {
      case element: Boolean => objectNode.put(fieldName, element.asInstanceOf[Boolean])
      case element: String => objectNode.put(fieldName, element.asInstanceOf[String])
      case element: Double => objectNode.put(fieldName, element.asInstanceOf[Double])
      case element: Float => objectNode.put(fieldName, element.asInstanceOf[Float])
      case element: Long => objectNode.put(fieldName, element.asInstanceOf[Long])
      case element: Int => objectNode.put(fieldName, element.asInstanceOf[Int])
      case element: JsonNode => objectNode.set(fieldName, element.asInstanceOf[JsonNode])
      case _ => objectNode.putNull(fieldName)
    }
  }

  private def convertToJson(element: Any, elementType: DataType, isInternalRow: Boolean): Any = {
    elementType match {
      case BinaryType => element.asInstanceOf[Array[Byte]]
      case BooleanType => element.asInstanceOf[Boolean]
      case DateType => element.asInstanceOf[Date].getTime
      case DoubleType => element.asInstanceOf[Double]
      case IntegerType => element.asInstanceOf[Int]
      case LongType => element.asInstanceOf[Long]
      case FloatType => element.asInstanceOf[Float]
      case NullType => null // TODO: verify how it works for nulls
      case DecimalType() => if (element.isInstanceOf[Decimal]) {
        element.asInstanceOf[Decimal].toJavaBigDecimal
      } else if (element.isInstanceOf[java.lang.Long]) {
        new java.math.BigDecimal(element.asInstanceOf[java.lang.Long])
      } else {
        element.asInstanceOf[java.math.BigDecimal]
      }
      case StringType =>
        if (isInternalRow) {
          new String(element.asInstanceOf[UTF8String].getBytes, "UTF-8")
        } else {
          element.asInstanceOf[String]
        }
      case TimestampType => if (element.isInstanceOf[java.lang.Long]) {
        element.asInstanceOf[java.lang.Long]
      } else {
        element.asInstanceOf[Timestamp].getTime
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

      case structType: StructType => rowTyperouterToJsonArray(element, structType)
      case _ =>
        throw new Exception(s"Cannot cast $element into a Json value. $elementType has no matching Json value.")
    }
  }

  private def mapTypeToObjectNode(valueType: DataType, data: Map[String, Any], isInternalRow: Boolean): ObjectNode = {
    val jsonObject: ObjectNode = objectMapper.createObjectNode();
    val internalData = valueType match {
      case subDocuments: StructType => data.map(kv => jsonObject.put(kv._1, rowTyperouterToJsonArray(kv._2, subDocuments)))
      case subArray: ArrayType => data.map(kv => jsonObject.put(kv._1, arrayTypeRouterToJsonArray(subArray.elementType, kv._2, isInternalRow)))

      // TODO moderakh      case _   => data.map(kv => jsonObject.put(kv._1, convertToJson(kv._2, valueType, isInternalRow)))
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
    val internalData = elementType match {
      case subDocuments: StructType => data.map(x => rowTyperouterToJsonArray(x, subDocuments)).asJava
      case subArray: ArrayType => data.map(x => arrayTypeRouterToJsonArray(subArray.elementType, x, isInternalRow)).asJava
      case _ => data.map(x => convertToJson(x, elementType, isInternalRow)).asJava
    }
    // When constructing the JSONArray, the internalData should contain JSON-compatible objects in order for the schema to be mantained.
    // Otherwise, the data will be converted into String.
    // TODO: moderakh new JSONArray(internalData)
    val arrayNode = objectMapper.createArrayNode()
    arrayNode
  }

  private def arrayDataTypeToJsonArray(elementType: DataType, data: ArrayData, isInternalRow: Boolean): ArrayNode = {
    val listBuffer = ListBuffer.empty[Any]
    elementType match {
      case subDocuments: StructType => data.foreach(elementType, (_, x) => listBuffer.append(rowTyperouterToJsonArray(x, subDocuments)))
      case subArray: ArrayType => data.foreach(elementType, (_, x) => listBuffer.append(arrayTypeRouterToJsonArray(subArray.elementType, x, isInternalRow)))
      case _ => data.foreach(elementType, (_, x) => listBuffer.append(convertToJson(x, elementType, isInternalRow)))
    }
    // When constructing the JSONArray, the internalData should contain JSON-compatible objects in order for the schema to be mantained.
    // Otherwise, the data will be converted into String.
    val arrayNode = objectMapper.createArrayNode()
    // TODO: moderakh new JSONArray(listBuffer)
    arrayNode
  }

  private def rowTyperouterToJsonArray(element: Any, schema: StructType) = element match {
    case e: Row => rowToObjectNode(e)
    case e: InternalRow => internalRowToObjectNode(e, schema)
    case _ => throw new Exception(s"Cannot cast $element into a Json value. Struct $element has no matching Json value.")
  }
}

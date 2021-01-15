// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.sql.{Date, Timestamp}
import java.util
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, ObjectNode, NullNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.microsoft.azure.cosmosdb.spark.schema.JsonSupport
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.catalyst.expressions.{GenericRowWithSchema, UnsafeMapData}
import org.apache.spark.sql.catalyst.util.ArrayData

import scala.collection.immutable.HashMap
// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

import org.apache.spark.unsafe.types.UTF8String

// scalastyle:off multiple.string.literals
// scalastyle:off null
object CosmosRowConverter
  extends JsonSupport
    with CosmosLoggingTrait {

    val objectMapper = new ObjectMapper();

    def fromObjectNodeToRow(schema: StructType, objectNode: ObjectNode): Row = {
        val values: Seq[Any] = convertStructToSparkDataType(schema, objectNode)
        new GenericRowWithSchema(values.toArray, schema)
    }

    def fromRowToObjectNode(row: Row): ObjectNode = {
        val objectNode: ObjectNode = objectMapper.createObjectNode();
        row.schema.fields.zipWithIndex.foreach({
            case (field, i) => {
                field.dataType match {
                    case NullType  | _ if row.isNullAt(i) => objectNode.putNull(field.name)
                    case _ => objectNode.set(field.name, convertSparkDataTypeToJsonNode(field.dataType, row.get(i)))
                }
            }
        })

        objectNode
    }

    // scalastyle:off
    private def convertSparkDataTypeToJsonNode(fieldType: DataType, rowData: Any) : JsonNode = {
        fieldType match {
            case StringType => objectMapper.convertValue(rowData.asInstanceOf[String], classOf[JsonNode])
            case BinaryType => objectMapper.convertValue(rowData.asInstanceOf[Array[Byte]], classOf[JsonNode])
            case BooleanType => objectMapper.convertValue(rowData.asInstanceOf[Boolean], classOf[JsonNode])
            case DoubleType => objectMapper.convertValue(rowData.asInstanceOf[Double], classOf[JsonNode])
            case IntegerType => objectMapper.convertValue(rowData.asInstanceOf[Int], classOf[JsonNode])
            case LongType => objectMapper.convertValue(rowData.asInstanceOf[Long], classOf[JsonNode])
            case FloatType => objectMapper.convertValue(rowData.asInstanceOf[Float], classOf[JsonNode])
            case DecimalType() => if (rowData.isInstanceOf[Decimal]) {
                    objectMapper.convertValue(rowData.asInstanceOf[Decimal].toJavaBigDecimal, classOf[JsonNode])
                } else if (rowData.isInstanceOf[java.lang.Long]) {
                    objectMapper.convertValue(new java.math.BigDecimal(rowData.asInstanceOf[java.lang.Long]), classOf[JsonNode])
                } else {
                    objectMapper.convertValue(rowData.asInstanceOf[java.math.BigDecimal], classOf[JsonNode])
                }
            case DateType => if (rowData.isInstanceOf[java.lang.Long]) {
                objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
            } else {
                objectMapper.convertValue(rowData.asInstanceOf[Date].getTime, classOf[JsonNode])
            }
            case TimestampType => if (rowData.isInstanceOf[java.lang.Long]) {
                    objectMapper.convertValue(rowData.asInstanceOf[java.lang.Long], classOf[JsonNode])
                } else {
                    objectMapper.convertValue(rowData.asInstanceOf[Timestamp].getTime, classOf[JsonNode])
                }
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
                    element.getKey(),
                    convertToSparkDataType(map.valueType, element.getValue())))
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

    // ------------------------------------------------------
    def toInternalRow(schema: StructType, objectNode: ObjectNode): InternalRow = {
        val asMap = documentToMap(objectNode)
        val row = recordAsRow(asMap, schema)

        RowEncoder(schema).createSerializer().apply(row)
    }

  // TODO: moderakh this method requires a rewrite
  // this is borrowed from old OLTP spark connector
  def getMap(objectNode: ObjectNode): java.util.HashMap[String, AnyRef] = {
     objectMapper.convertValue(objectNode, classOf[java.util.HashMap[String, AnyRef]])
  }

  // TODO: moderakh this method requires a rewrite
  // this is borrowed from old OLTP spark connector
  def documentToMap(document: ObjectNode): Map[String, AnyRef] = {
    if (document == null) {
      new HashMap[String, AnyRef]
    } else {
      getMap(document).asScala.toMap
    }
  }

  // TODO: moderakh this method requires a rewrite
  // this is borrowed from old OLTP spark connector
  def recordAsRow(
                   json: Map[String, AnyRef],
                   schema: StructType): Row = {

    val values: Seq[Any] = schema.fields.map {
      case StructField(name, et, _, mdata)
        if mdata.contains("idx") && mdata.contains("colname") =>
        val colName = mdata.getString("colname")
        val idx = mdata.getLong("idx").toInt
        json.get(colName).flatMap(v => Option(v)).map(toSQL(_, ArrayType(et, containsNull = true))).collect {
          case elemsList: Seq[_] if elemsList.indices contains idx => elemsList(idx)
        } orNull
      case StructField(name, dataType, _, _) =>
        json.get(name).flatMap(v => Option(v)).map(toSQL(_, dataType)).orNull
    }
    new GenericRowWithSchema(values.toArray, schema)
  }

  // TODO: moderakh this method requires a rewrite
  // scalastyle:off
  // this is borrowed from old OLTP spark connector
  def toSQL(value: Any, dataType: DataType): Any = {
    Option(value).map { value =>
      (value, dataType) match {
        case (list: List[AnyRef@unchecked], ArrayType(elementType, _)) =>
          null
        case (_, struct: StructType) =>
//          if(JSONObject.NULL.equals(value)) return null // TODO

          val jsonMap: Map[String, AnyRef] = value match {
            case doc: ObjectNode => documentToMap(doc)
            case hm: util.HashMap[_, _] => hm.asInstanceOf[util.HashMap[String, AnyRef]].asScala.toMap
          }
          recordAsRow(jsonMap, struct)
        case (_, map: MapType) =>
          (value match {
            case document: ObjectNode => documentToMap(document)
            case _ => value.asInstanceOf[java.util.HashMap[String, AnyRef]].asScala.toMap
          }).map(element => (toSQL(element._1, map.keyType), toSQL(element._2, map.valueType)))
        case (_, array: ArrayType) =>
          // TODO fixme
          null
//          if(!JSONObject.NULL.equals(value))
//            value.asInstanceOf[java.util.ArrayList[AnyRef]].asScala.map(element => toSQL(element, array.elementType)).toArray
//          else
//            null
        case (_, binaryType: BinaryType) =>
          value.asInstanceOf[java.util.ArrayList[Int]].asScala.map(x => x.toByte).toArray
        case _ =>
          //Assure value is mapped to schema constrained type.
          enforceCorrectType(value, dataType)
      }
    }.orNull
  }
  // scalastyle:on
  ////


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

  // scalastyle:off cyclomatic.complexity
  private def convertToJson(element: Any, elementType: DataType, isInternalRow: Boolean): JsonNode = {
    elementType match {
      case BinaryType => objectMapper.convertValue(element.asInstanceOf[Array[Byte]], classOf[JsonNode])
      case BooleanType => objectMapper.convertValue(element.asInstanceOf[Boolean], classOf[JsonNode])
      case DateType => objectMapper.convertValue(element.asInstanceOf[Date].getTime, classOf[JsonNode])
      case DoubleType => objectMapper.convertValue(element.asInstanceOf[Double], classOf[JsonNode])
      case IntegerType => objectMapper.convertValue(element.asInstanceOf[Int], classOf[JsonNode])
      case LongType => objectMapper.convertValue(element.asInstanceOf[Long], classOf[JsonNode])
      case FloatType => objectMapper.convertValue(element.asInstanceOf[Float], classOf[JsonNode])
      case NullType => objectMapper.nullNode()
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
  // scalastyle:off cyclomatic.complexity

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
// scalastyle:on multiple.string.literals
// scalastyle:on null


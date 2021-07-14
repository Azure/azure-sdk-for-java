// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.JsonNode
import org.apache.spark.sql.catalyst.analysis.TypeCoercion

// scalastyle:off underscore.import
import com.fasterxml.jackson.databind.node._

import org.apache.spark.sql.types._

import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// Infers a schema by reading sample data from a source container.
private object CosmosTableSchemaInferrer
  extends BasicLoggingTrait {

  private[spark] val RawJsonBodyAttributeName = "_rawBody"
  private[spark] val TimestampAttributeName = "_ts"
  private[spark] val IdAttributeName = "id"
  private[spark] val ETagAttributeName = "_etag"
  private[spark] val SelfAttributeName = "_self"
  private[spark] val ResourceIdAttributeName = "_rid"
  private[spark] val AttachmentsAttributeName = "_attachments"
  private[spark] val PreviousRawJsonBodyAttributeName = "_previousRawBody"
  private[spark] val TtlExpiredAttributeName = "_ttlExpired"
  private[spark] val OperationTypeAttributeName = "_operationType"
  private[spark] val LsnAttributeName = "_lsn"

  private val systemProperties = List(
    ETagAttributeName,
    SelfAttributeName,
    ResourceIdAttributeName,
    AttachmentsAttributeName)

  private val notNullableProperties = List(
    IdAttributeName,
    ETagAttributeName,
    SelfAttributeName,
    ResourceIdAttributeName,
    TimestampAttributeName,
    AttachmentsAttributeName)

  private[spark] def inferSchema(
                                  inferredItems: Seq[ObjectNode],
                                  includeSystemProperties: Boolean,
                                  includeTimestamp: Boolean,
                                  allowNullForInferredProperties: Boolean): StructType = {
    if (inferredItems.isEmpty) {
      // No documents to infer from
      StructType(Seq())
    } else {
      // Create a unique map of all distinct properties from documents
      val uniqueStructFields = inferredItems.foldLeft(Map.empty[String, StructField])({
        case (map, item) => inferDataTypeFromObjectNode(
          item, includeSystemProperties, includeTimestamp, allowNullForInferredProperties) match {
          case Some(mappedList) =>
            map ++ mappedList.map(mappedItem => {
              if (map.contains(mappedItem._1) && map(mappedItem._1).dataType != mappedItem._2.dataType) {
                // if any of the 2 mappings is nullable, then the result is nullable
                val isNullable = mappedItem._2.nullable || map(mappedItem._1).nullable

                val commonType = compatibleType(map(mappedItem._1).dataType, mappedItem._2.dataType)

                (mappedItem._1, StructField(mappedItem._1, commonType, nullable=isNullable))
              }
              else {
                mappedItem
              }
            })
          case None => Map.empty[String, StructField]
        }
      })

      StructType(uniqueStructFields.valuesIterator.toSeq)
    }
  }

  private[spark] def inferSchema(client: CosmosAsyncClient,
                                 userConfig: Map[String, String],
                                 defaultSchema: StructType): StructType = {
    val cosmosInferenceConfig = CosmosSchemaInferenceConfig.parseCosmosInferenceConfig(userConfig)
    val cosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(userConfig)
    if (cosmosInferenceConfig.inferSchemaEnabled) {
      val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
      val sourceContainer = ThroughputControlHelper.getContainer(userConfig, cosmosContainerConfig, client)
      val queryOptions = new CosmosQueryRequestOptions()
      queryOptions.setMaxBufferedItemCount(cosmosInferenceConfig.inferSchemaSamplingSize)
      val queryText = cosmosInferenceConfig.inferSchemaQuery match {
        case None =>
          cosmosReadConfig.customQuery match {
            case None => s"select TOP ${cosmosInferenceConfig.inferSchemaSamplingSize} * from c"
            case _ => cosmosReadConfig.customQuery.get.queryText
          }
        case _ => cosmosInferenceConfig.inferSchemaQuery.get
      }

      val pagedFluxResponse =
        sourceContainer.queryItems(queryText, queryOptions, classOf[ObjectNode])

      val feedResponseList = pagedFluxResponse
        .take(cosmosInferenceConfig.inferSchemaSamplingSize)
        .collectList
        .block

      inferSchema(feedResponseList.asScala,
        cosmosInferenceConfig.inferSchemaQuery.isDefined || cosmosInferenceConfig.includeSystemProperties,
        cosmosInferenceConfig.inferSchemaQuery.isDefined || cosmosInferenceConfig.includeTimestamp,
        cosmosInferenceConfig.allowNullForInferredProperties)
    } else {
      defaultSchema
    }
  }

  private def inferDataTypeFromObjectNode
  (
    node: ObjectNode,
    includeSystemProperties: Boolean,
    includeTimestamp: Boolean,
    allowNullForInferredProperties: Boolean
  ): Option[Seq[(String, StructField)]] = {

    Option(node).map(n =>
      n.fields.asScala
        .filter(field => isAllowedPropertyToMap(field.getKey, includeSystemProperties, includeTimestamp))
        .map(field =>
            inferDataTypeFromJsonNode(field.getValue, allowNullForInferredProperties) match {
              case nullType: NullType => field.getKey -> StructField(field.getKey, nullType, nullable=true)
              case anyType: DataType => field.getKey -> StructField(
                field.getKey,
                anyType,
                nullable= !notNullableProperties.contains(field.getKey) && allowNullForInferredProperties)
            })
        .toSeq)
  }

  private def isAllowedPropertyToMap(propertyName: String,
                                     includeSystemProperties: Boolean,
                                     includeTimestamp: Boolean): Boolean = {
    if (includeSystemProperties) {
      true
    }
    else {
      !systemProperties.contains(propertyName) &&
        (includeTimestamp || !TimestampAttributeName.equalsIgnoreCase(propertyName))
    }
  }

  // scalastyle:off
  private def inferDataTypeFromJsonNode(jsonNode: JsonNode, allowNullForInferredProperties: Boolean): DataType = {
    jsonNode match {
      case _: NullNode => NullType
      case _: BinaryNode => BinaryType
      case _: BooleanNode => BooleanType
      case _: TextNode => StringType
      case _: FloatNode => FloatType
      case _: DoubleNode => DoubleType
      case _: LongNode => LongType
      case _: IntNode => IntegerType
      case decimalNode: DecimalNode if decimalNode.isBigDecimal =>
        val asBigDecimal = decimalNode.decimalValue
        val precision = Integer.min(asBigDecimal.precision, DecimalType.MAX_PRECISION)
        val scale = Integer.min(asBigDecimal.scale, DecimalType.MAX_SCALE)
        DecimalType(precision, scale)
      case decimalNode: DecimalNode if decimalNode.isFloat => FloatType
      case decimalNode: DecimalNode if decimalNode.isDouble => DoubleType
      case decimalNode: DecimalNode if decimalNode.isInt => IntegerType
      case arrayNode: ArrayNode => inferDataTypeFromArrayNode(arrayNode, allowNullForInferredProperties) match {
        case Some(valueType) => ArrayType(valueType)
        case None => NullType
      }
      case objectNode: ObjectNode =>
        inferDataTypeFromObjectNode(
          objectNode,includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties) match {
        case Some(mappedList) =>
          val nestedFields = mappedList.map(f => f._2)
          StructType(nestedFields)
        case None => NullType
      }
      case _ =>
        this.logWarning(s"Unsupported document node conversion [${jsonNode.getNodeType}]")
        StringType // Defaulting to a string representation for values that we cannot convert
    }
  }

  // scalastyle:on
  private def inferDataTypeFromArrayNode(node: ArrayNode, allowNullForInferredProperties: Boolean): Option[DataType] = {
    Option(node.get(0)).map(firstElement => inferDataTypeFromJsonNode(firstElement, allowNullForInferredProperties))
  }

  /**
   * It looks for the most compatible type between two given DataTypes.
   * i.e.: {{{
   *   val dataType1 = IntegerType
   *   val dataType2 = DoubleType
   *   assert(compatibleType(dataType1,dataType2)==DoubleType)
   * }}}
   *
   * @param t1 First DataType to compare
   * @param t2 Second DataType to compare
   * @return Compatible type for both t1 and t2
   */
  private def compatibleType(t1: DataType, t2: DataType): DataType = {
    //TypeCoercion.findTightestCommonTypeOfTwo(t1, t2) match {
    TypeCoercion.findTightestCommonType(t1, t2) match {
      case Some(commonType) => commonType

      case None =>
        // t1 or t2 is a StructType, ArrayType, or an unexpected type.
        (t1, t2) match {
          case (other: DataType, NullType) => other
          case (NullType, other: DataType) => other
          case (StructType(fields1), StructType(fields2)) =>
            val newFields = (fields1 ++ fields2)
              .groupBy(field => field.name)
              .map { case (name, fieldTypes) =>
                val dataType = fieldTypes
                  .map(field => field.dataType)
                  .reduce(compatibleType)
                StructField(name, dataType, nullable = true)

              }
            StructType(newFields.toSeq.sortBy(_.name))

          case (ArrayType(elementType1, containsNull1), ArrayType(elementType2, containsNull2)) =>
            ArrayType(
              compatibleType(elementType1, elementType2),
              containsNull1 || containsNull2)

          case (_, _) => StringType
        }
    }
  }
}

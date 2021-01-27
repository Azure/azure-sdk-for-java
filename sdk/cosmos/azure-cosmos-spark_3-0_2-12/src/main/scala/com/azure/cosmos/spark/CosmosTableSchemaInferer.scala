// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, BinaryNode, BooleanNode, DecimalNode, DoubleNode,
    FloatNode, IntNode, LongNode, NullNode, ObjectNode, TextNode}


// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// Infers a schema by reading sample data from a source container.
private object CosmosTableSchemaInferer
    extends CosmosLoggingTrait {
    val RAW_JSON_BODY_ATTRIBUTE_NAME = "_rawBody"
    private val TIMESTAMP_ATTRIBUTE_NAME = "_ts"
    private val ID_ATTRIBUTE_NAME = "id"
    private val defaultSchemaForInferenceDisabled = StructType(Seq(
        StructField(RAW_JSON_BODY_ATTRIBUTE_NAME, StringType),
        StructField(ID_ATTRIBUTE_NAME, StringType),
        StructField(TIMESTAMP_ATTRIBUTE_NAME, LongType)
    ))

    def inferSchema(inferredItems : Seq[ObjectNode]): StructType = {
        if (inferredItems.isEmpty){
            // No documents to infer from
            StructType(Seq())
        }

        // Create a unique map of all distinct properties from documents
        val uniqueStructFields = inferredItems.foldLeft(Map.empty[String, StructField])({
            case (map, item) => inferDataTypeFromObjectNode(item) match {
                case Some(mappedList) =>
                    map ++ mappedList.map(mappedItem => {
                        if (map.contains(mappedItem._1) && map(mappedItem._1).dataType != mappedItem._2.dataType){
                            // If 2 documents contain the same property name but different type, we default to String
                            (mappedItem._1, StructField(mappedItem._1, StringType))
                        }
                        else{
                            mappedItem
                        }
                    })
            }
        })

        StructType(uniqueStructFields.valuesIterator.toSeq)
    }

    def inferSchema(client: CosmosAsyncClient,
                    userConfig: Map[String, String]): StructType = {
        val cosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(userConfig)
        if (cosmosReadConfig.inferSchemaEnabled) {
            val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
            val sourceContainer = client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)

            val queryOptions = new CosmosQueryRequestOptions()
            queryOptions.setMaxBufferedItemCount(cosmosReadConfig.inferSchemaSamplingSize)
            val queryText = s"select TOP ${cosmosReadConfig.inferSchemaSamplingSize} * from c"

            val queryObservable =
                sourceContainer.queryItems(queryText, queryOptions, classOf[ObjectNode])

            val feedResponseList = queryObservable.byPage.collectList.block
            inferSchema(feedResponseList.asScala.flatten(feedResponse => feedResponse.getResults.asScala))
        } else {
            defaultSchemaForInferenceDisabled
        }
    }

    private def inferDataTypeFromObjectNode(node: ObjectNode) : Option[Seq[(String, StructField)]] = {
        Option(node).map(n =>
            n.fields.asScala
                .map(field =>
                    field.getKey ->
                    StructField(field.getKey, inferDataTypeFromJsonNode(field.getValue)))
                .toSeq)
    }

    // scalastyle:off
    private def inferDataTypeFromJsonNode(jsonNode: JsonNode) : DataType = {
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
            case arrayNode: ArrayNode => inferDataTypeFromArrayNode(arrayNode) match {
                case Some(valueType) => ArrayType(valueType)
                case None => NullType
            }
            case objectNode: ObjectNode => inferDataTypeFromObjectNode(objectNode) match {
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

    private def inferDataTypeFromArrayNode(node: ArrayNode) : Option[DataType] = {
        Option(node.get(0)).map(firstElement => inferDataTypeFromJsonNode(firstElement))
    }
}

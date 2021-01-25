// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, NullNode, ObjectNode}


// scalastyle:off underscore.import
import org.apache.spark.sql.types._
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// Infers a schema by reading sample data from a source container.
private object CosmosTableSchemaInferer
    extends CosmosLoggingTrait {

    def inferSchema(inferredItems : Seq[ObjectNode]): StructType = {
        if (inferredItems.isEmpty){
            throw new Exception("Cannot infer schema from an empty source.")
        }


        // Create a unique map of all distinct properties from documents
        // If 2 documents contain the same property name but different type, the last scanned one would define the final
        // type
        val uniqueStructFields = inferredItems.foldLeft(Map.empty[String, StructField])({
            case (map, item) => inferDataTypeFromObjectNode(item) match {
                case Some(mappedList) => map ++ mappedList
            }
        })

        StructType(uniqueStructFields.valuesIterator.toSeq)
    }

    def inferSchema(): StructType = {
        null
    }

    private def inferDataTypeFromObjectNode(node: ObjectNode) : Option[Seq[(String, StructField)]] = {
        Option(node).map(n =>
            n.fields().asScala.map(field =>
                    field.getKey ->
                    StructField(field.getKey, inferDataTypeFromJsonNode(field.getValue)))
                .toSeq)
    }

    private def inferDataTypeFromJsonNode(jsonNode: JsonNode) : DataType = {
        jsonNode match {
            case _: NullNode =>
                NullType
            case objectNode: ObjectNode => inferDataTypeFromObjectNode(objectNode) match {
                case Some(mappedList) =>
                    val nestedFields = mappedList.map(f => f._2)
                    StructType(nestedFields)
                case None =>
                    NullType
                }
            case arrayNode: ArrayNode => inferDataTypeFromArrayNode(arrayNode) match {
                case Some(valueType) => ArrayType(valueType)
                case None => NullType
                }
        }
    }

    private def inferDataTypeFromArrayNode(node: ArrayNode) : Option[DataType] = {
        Option(node.get(0)).map(firstElement => inferDataTypeFromJsonNode(firstElement))
    }
}

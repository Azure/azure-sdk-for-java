// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.spark.sql.types.StructType

import scala.collection.immutable.Map

private[spark] object ViewDefinitionEnvelopeSerializer {
  private val idPropertyName: String = "id"
  private val viewsPropertyName: String = "views"
  private val keyPropertyName: String = "k"
  private val valuePropertyName: String = "v"
  private val databasePropertyName: String = "database"
  private val viewPropertyName: String = "name"
  private val customSchemaPropertyName: String = "customSchema"
  private val optionsPropertyName: String = "options"
  val V1Identifier: String = "spark.cosmos.catalog.viewDefinitions.v1"
  private val objectMapper = new ObjectMapper()

  def fromJson(json: String): Array[ViewDefinition] = {
    val parsedNode = objectMapper.readTree(json)
    if (isValidJson(parsedNode)) {
      val viewArrayNode = parsedNode.get(viewsPropertyName).asInstanceOf[ArrayNode]

      val views = new scala.collection.mutable.ArrayBuffer[ViewDefinition]()
      for (i <- 0 until viewArrayNode.size()) {
        val viewNode = viewArrayNode.get(i)
        val databaseName = viewNode.get(databasePropertyName).asText()
        val name = viewNode.get(viewPropertyName).asText()
        val customSchema: Option[StructType] = if (viewNode.has(customSchemaPropertyName)) {
          Some(StructType.fromDDL(viewNode.get(customSchemaPropertyName).asText))
        } else {
          None
        }
        val options = if (viewNode.has(optionsPropertyName)) {
          val optionsArrayNode = viewNode.get(optionsPropertyName).asInstanceOf[ArrayNode]
          val temp = new scala.collection.mutable.HashMap[String, String]
          for (i <- 0 until optionsArrayNode.size) {
            val kvpNode = optionsArrayNode.get(i)
            temp += (kvpNode.get(keyPropertyName).asText -> kvpNode.get(valuePropertyName).asText)
          }

          temp.toMap
        } else {
          Map.empty[String, String]
        }

        views += ViewDefinition(databaseName, name, customSchema, options)
      }
      views.toArray
    } else {
      val message = s"Unable to deserialize view definitions '$json'."
      throw new IllegalArgumentException(message)
    }
  }

  def toJson(viewDefinitions: Array[ViewDefinition]): String = {
    val root = objectMapper.createObjectNode()
    root.put(idPropertyName, V1Identifier)
    val viewsArray: ArrayNode = root.putArray(viewsPropertyName)

    for (viewDefinition <- viewDefinitions) {

      val view = objectMapper.createObjectNode()
      view.put(databasePropertyName, viewDefinition.databaseName)
      view.put(viewPropertyName, viewDefinition.viewName)

      viewDefinition.userProvidedSchema match {
        case Some(schema) => view.put(customSchemaPropertyName, schema.toDDL)
        case None =>
      }

      if (viewDefinition.options.size > 0) {
        val options = view.putArray(optionsPropertyName)
        viewDefinition.options.foreach((kvp) => {
          val option = objectMapper.createObjectNode()
          option.put(keyPropertyName, kvp._1)
          option.put(valuePropertyName, kvp._2)
          options.add(option)
        })
      }

      viewsArray.add(view)
    }

    objectMapper.writeValueAsString(root)
  }

  private[this] def isValidJson(parsedNode: JsonNode): Boolean = {
    parsedNode != null &&
      parsedNode.isObject &&
      parsedNode.get(idPropertyName) != null &&
      parsedNode.get(idPropertyName).asText("") == V1Identifier &&
      parsedNode.get(viewsPropertyName) != null &&
      parsedNode.get(viewsPropertyName).isArray
  }
}

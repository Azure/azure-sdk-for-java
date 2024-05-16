// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.implementation.patch.{PatchOperationCore, PatchOperationType}
import com.azure.cosmos.implementation.{Constants, ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.models.{CosmosPatchOperations, PartitionKeyDefinition}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty}
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.node.{ArrayNode, BigIntegerNode, DecimalNode, DoubleNode, FloatNode, IntNode, LongNode, MissingNode, ObjectNode, ShortNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import java.io.IOException
import scala.collection.mutable.ListBuffer

private class CosmosPatchHelper(diagnosticsConfig: DiagnosticsConfig,
                        cosmosPatchConfigs: CosmosPatchConfigs) {
 private val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  private val objectMapper = new ObjectMapper()

 private val TimestampAttributeName = "_ts"
 private val IdAttributeName = "id"
 private val ETagAttributeName = "_etag"
 private val SelfAttributeName = "_self"
 private val ResourceIdAttributeName = "_rid"
 private val AttachmentsAttributeName = "_attachments"

 private val systemProperties = List(
  TimestampAttributeName,
  ETagAttributeName,
  SelfAttributeName,
  ResourceIdAttributeName,
  AttachmentsAttributeName)

 private def isAllowedProperty(path: String, partitionKeyDefinition: PartitionKeyDefinition): Boolean = {
  assertNotNullOrEmpty(path, "path")
  assertNotNull(partitionKeyDefinition, "partitionKeyDefinition")

  // There are some properties are immutable, these kind properties include:
  // 1. System properties : _ts, _rid, _etag
  // 2. id, and partitionKeyPath
  if ((path.startsWith("/") && !systemProperties.contains(path.substring(1)) && IdAttributeName != path.substring(1))
   && !StringUtils.join(partitionKeyDefinition.getPaths, "").contains(path)) {
   true
  } else {
   false
  }
 }

  def parseRawJson(jsonNode: JsonNode): JsonNode = {
    if (jsonNode.isValueNode || jsonNode.isArray) {
      try objectMapper.readTree(s"""{"${Constants.Properties.VALUE}": $jsonNode}""")
        .asInstanceOf[ObjectNode]
        .get(Constants.Properties.VALUE)
      catch {
        case e: IOException =>
          throw new IllegalStateException(s"Unable to parse JSON $jsonNode", e)
      }
    } else {
      jsonNode.asInstanceOf[ObjectNode]
    }
  }

 private def addOperationConditionally(cosmosPatchOperations: CosmosPatchOperations,
                                       columnConfig: CosmosPatchColumnConfig,
                                       objectNode: ObjectNode,
                                       condition: Boolean,
                                       message: String): Unit = {
  if (condition) {
    if (columnConfig.operationType == CosmosPatchOperationTypes.Remove) {
      cosmosPatchOperations.remove(columnConfig.mappingPath)
    } else {
      val node = objectNode.get(columnConfig.columnName)
      val effectiveNode = if (columnConfig.isRawJson) {
        objectMapper.readTree(node.asText())
      } else {
        node
      }

      columnConfig.operationType match {
        case CosmosPatchOperationTypes.Add => cosmosPatchOperations.add(columnConfig.mappingPath, effectiveNode)
        case CosmosPatchOperationTypes.Set => cosmosPatchOperations.set(columnConfig.mappingPath, effectiveNode)
        case CosmosPatchOperationTypes.Replace => cosmosPatchOperations.replace(columnConfig.mappingPath, effectiveNode)
        case CosmosPatchOperationTypes.Increment => addIncrementPatchOperation(cosmosPatchOperations, columnConfig, effectiveNode)
        case _ => throw new IllegalArgumentException(s"Patch operation type ${columnConfig.operationType} is not supported")
      }
    }
  } else {
   log.logDebug(
    s" The operation will not be added due to condition checking failed," +
     s" columnName: ${columnConfig.columnName}, opType: ${columnConfig.operationType}, message: $message")
  }
 }

 def createCosmosPatchOperations(itemId: String,
                                 partitionKeyDefinition: PartitionKeyDefinition,
                                 objectNode: ObjectNode): CosmosPatchOperations = {

  val cosmosPatchOperations = CosmosPatchOperations.create()

  cosmosPatchConfigs.columnConfigsMap.values.foreach(columnConfig => {
   if (isAllowedProperty(columnConfig.mappingPath, partitionKeyDefinition)) {
    columnConfig.operationType match {
     case CosmosPatchOperationTypes.Remove => cosmosPatchOperations.remove(columnConfig.mappingPath)
     case CosmosPatchOperationTypes.None => // no-op
     case _ => addOperationConditionally(
      cosmosPatchOperations,
      columnConfig,
      objectNode,
      objectNode.has(columnConfig.columnName),
      s"Object node does not contain ${columnConfig.columnName}")
    }
   }
  })

  if (
   ImplementationBridgeHelpers
    .CosmosPatchOperationsHelper
     .getCosmosPatchOperationsAccessor.getPatchOperations(cosmosPatchOperations).size() == 0) {

   // If we reach here, it means there are no valid operations being included in the patch operation.
   // It could be caused by few reasons:
   // 1. The patch operation type for all columns are None which result in no-op
   // 2. There is no properties which are are allowed for partial updates included (id, partitionKey path, system properties)
   // 3. Due to serialization settings, it could filter out null/empty/default properties
   //
   // As of today, we start with more restrict rules: throw exception if there is no operations being included in the patch operation
   // But in the future, if it is a common scenario that we will reach here,
   // we can consider to relax the rules by adding another config to allow this behavior in patch configs
   throw new IllegalStateException(s"There is no operations included in the patch operation for itemId: $itemId")
  }

  cosmosPatchOperations
 }

 // For increment patch operation, will need to validate the value type
 private[this] def addIncrementPatchOperation(patchOperations: CosmosPatchOperations,
                                              patchColumnConfig: CosmosPatchColumnConfig,
                                              jsonNode: JsonNode): Unit = {
  assertNotNull(patchOperations, "patchOperations")
  assertNotNull(patchColumnConfig, "patchColumnConfig")
  assertNotNull(jsonNode, "jsonNode")

  if (jsonNode.isNumber) {
   jsonNode match {
    case _: ShortNode | _: IntNode | _: LongNode =>
     patchOperations.increment(patchColumnConfig.mappingPath, jsonNode.longValue())
    case _: FloatNode | _: DoubleNode =>
     patchOperations.increment(patchColumnConfig.mappingPath, jsonNode.doubleValue())
    case _: BigIntegerNode if jsonNode.canConvertToLong =>
     patchOperations.increment(patchColumnConfig.mappingPath, jsonNode.longValue())
    case _: BigIntegerNode =>
     throw new IllegalArgumentException(s"BigInteger ${jsonNode.bigIntegerValue()} is too large, cannot be converted to long")
    case _: DecimalNode if jsonNode.canConvertToLong =>
     patchOperations.increment(patchColumnConfig.mappingPath, jsonNode.longValue())
    case _: DecimalNode =>
     throw new IllegalArgumentException(s"Decimal value ${jsonNode.bigIntegerValue()} is not supported ")
    case _ =>
     throw new IllegalArgumentException(s"Increment operation is not supported for type ${jsonNode.getClass}")
   }
  } else {
   throw new IllegalArgumentException(s"Increment operation is not supported for non-numeric type ${jsonNode.getClass}")
  }
 }

  private[spark] def createCosmosPatchBulkUpdateOperations(objectNode: ObjectNode): List[PatchOperationCore[JsonNode]] = {

      val cosmosPatchBulkUpdateOperations = new ListBuffer[PatchOperationCore[JsonNode]]()
      val fieldIterator = objectNode.fields()
      while (fieldIterator.hasNext) {
          val fieldEntry = fieldIterator.next()

          // check whether there is customized mapping path
          cosmosPatchConfigs.columnConfigsMap.get(fieldEntry.getKey) match {
              case Some(userDefinedColumnConfig) =>
                  if (!systemProperties.contains(userDefinedColumnConfig.mappingPath.substring(1))) {
                      val node = fieldEntry.getValue
                      val effectiveNode = if (userDefinedColumnConfig.isRawJson) {
                          objectMapper.readTree(node.asText())
                      } else {
                          node
                      }

                      userDefinedColumnConfig.operationType match {
                          case CosmosPatchOperationTypes.Set =>
                              cosmosPatchBulkUpdateOperations +=
                                  new PatchOperationCore[JsonNode](PatchOperationType.SET, userDefinedColumnConfig.mappingPath, effectiveNode)
                          case _ => throw new RuntimeException(s"Patch operation type is not supported for itemBulkUpdate write strategy")
                      }
                  }

              case None =>
                  if (!systemProperties.contains(fieldEntry.getKey)) {
                      cosmosPatchBulkUpdateOperations +=
                          new PatchOperationCore[JsonNode](PatchOperationType.SET, s"/${fieldEntry.getKey}", fieldEntry.getValue)
                  }
          }
      }

      cosmosPatchBulkUpdateOperations.toList
  }

  private[spark] def patchBulkUpdateItem(
                                            itemToBeUpdatedOpt: Option[ObjectNode],
                                            patchBulkUpdateOperations: List[PatchOperationCore[JsonNode]]): ObjectNode = {

      // Do not do the modification on original objectNode, as it maybe used by other patchBulkUpdate operations
      val rootNode = itemToBeUpdatedOpt match {
          case Some(itemToBeUpdate) => Utils.getSimpleObjectMapper.createObjectNode().setAll(itemToBeUpdate)
          case _ => Utils.getSimpleObjectMapper.createObjectNode()
      }

      patchBulkUpdateOperations.foreach(patchOperation => {
          patchBulkUpdateFields(rootNode, patchOperation.getOperationType, patchOperation.getPath, patchOperation.getResource)
      })

      rootNode
  }

  private[this] def patchBulkUpdateFields(
                                            rootNode: ObjectNode,
                                            patchOperationType: PatchOperationType,
                                            path: String,
                                            pathValue: JsonNode): ObjectNode = {
      patchOperationType match {
          case PatchOperationType.SET =>
              // CosmosDb mapping path: /parent/child1/item
              // Loop through each path component. If the parent node does not exists, create one {}
              var parentNode: JsonNode = rootNode
              val pathArray = path.stripPrefix("/").split("/")

              // get or create parent node
              for (pathIndex <- 0 until pathArray.size - 1) {
                  parentNode = getOrCreateNextParentNode(parentNode, s"/${pathArray(pathIndex)}")
              }

              // after looping through the structure, now set the field value
              val fieldPath = pathArray(pathArray.size - 1)
              parentNode match {
                  case parentIsObjectNode: ObjectNode => parentIsObjectNode.set(fieldPath, pathValue)
                  case parentIsArrayNode: ArrayNode =>
                      val arrayIndex = Integer.parseInt(fieldPath)
                      parentIsArrayNode.set(arrayIndex, pathValue)
                  case _ => throw new RuntimeException(s"Unsupported parent node type ${parentNode.getClass} in bulkUpdateFields") // we should never reach here
              }

              rootNode
          case _ => throw new RuntimeException(s"PatchOperationType $patchOperationType is not supported for ItemBulkUpdate") // we should have never reach here
      }
  }

  private[this] def getOrCreateNextParentNode(parentNode: JsonNode, childPath: String): JsonNode = {

      // Construct a json pointer
      // Probably just a bad naming, but there is no background compiling activities etc, should be light weighted
      // But if in the future we found this part causes perf issue etc, can reconsider using simple string type
      val jsonPath = JsonPointer.compile(childPath)
      val nextParentNode =
          parentNode.at(jsonPath) match {
              case _: MissingNode =>
                  parentNode match {
                      case objectNode: ObjectNode =>
                          objectNode.set(jsonPath.getMatchingProperty, Utils.getSimpleObjectMapper().createObjectNode())
                          objectNode
                      case arrayNode: ArrayNode =>
                          val arrayIndex = Integer.parseInt(jsonPath.getMatchingProperty)
                          arrayNode.insert(arrayIndex, Utils.getSimpleObjectMapper().createObjectNode())
                          arrayNode
                      case _ => throw new RuntimeException(s"Unsupported parent node type ${parentNode.getClass}")
                  }

                  parentNode.at(jsonPath)
              case existingNode: JsonNode => existingNode
          }

      nextParentNode match {
          case _: ObjectNode | _: ArrayNode => nextParentNode
          case _ => throw new RuntimeException(s"Unsupported next parent node type ${nextParentNode.getClass}")
      }
  }
}

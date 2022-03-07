// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.models.{CosmosPatchOperations, PartitionKeyDefinition}
import com.azure.cosmos.spark.CosmosPredicates.{assertNotNull, assertNotNullOrEmpty}
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node._

class CosmosPatchHelper(diagnosticsConfig: DiagnosticsConfig,
                        cosmosPatchConfigs: CosmosPatchConfigs) {
 private val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

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

 def isAllowedProperty(path: String, partitionKeyDefinition: PartitionKeyDefinition): Boolean = {
  assertNotNullOrEmpty(path, "path")
  assertNotNull(partitionKeyDefinition, "partitionKeyDefinition")

  // There are some properties are immutable, these kind properties include:
  // 1. System properties : _ts, _rid, _etag
  // 2. id, and partitionKeyPath
  if ((path.startsWith("/") && !systemProperties.contains(path.substring(1)))
   && IdAttributeName != path
   && !StringUtils.join(partitionKeyDefinition.getPaths, "").contains(path)) {
   true
  } else {
   false
  }
 }

 def addOperationConditionally(cosmosPatchOperations: CosmosPatchOperations,
                               columnConfig: CosmosPatchColumnConfig,
                               objectNode: ObjectNode,
                               condition: Boolean,
                               message: String): Unit = {
  if (condition) {
   columnConfig.operationType match {
    case CosmosPatchOperationTypes.Add => cosmosPatchOperations.add(columnConfig.mappingPath, objectNode.get(columnConfig.columnName))
    case CosmosPatchOperationTypes.Set => cosmosPatchOperations.set(columnConfig.mappingPath, objectNode.get(columnConfig.columnName))
    case CosmosPatchOperationTypes.Replace => cosmosPatchOperations.replace(columnConfig.mappingPath, objectNode.get(columnConfig.columnName))
    case CosmosPatchOperationTypes.Remove => cosmosPatchOperations.remove(columnConfig.mappingPath)
    case CosmosPatchOperationTypes.Increment => addIncrementPatchOperation(cosmosPatchOperations, columnConfig, objectNode.get(columnConfig.columnName))
    case _ => throw new IllegalArgumentException(s"Patch operation type ${columnConfig.operationType} is not supported")
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
}

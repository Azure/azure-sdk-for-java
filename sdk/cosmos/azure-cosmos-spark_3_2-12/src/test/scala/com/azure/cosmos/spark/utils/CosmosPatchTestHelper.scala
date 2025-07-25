// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.utils

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.models.PartitionKeyDefinition
import com.azure.cosmos.spark.{BulkWriter, CosmosPatchColumnConfig, CosmosPatchConfigs, CosmosWriteConfig, DiagnosticsConfig, ItemWriteStrategy, OutputMetricsPublisherTrait, PointWriter}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils
import org.apache.spark.MockTaskContext
import org.apache.spark.sql.types.{ArrayType, BinaryType, BooleanType, ByteType, DecimalType, DoubleType, FloatType, IntegerType, LongType, ShortType, StringType, StructField, StructType}

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

private[spark] object CosmosPatchTestHelper {
 private val objectMapper = new ObjectMapper()
 private val IdAttributeName = "id"

 def getPatchItemWithFullSchema(id: String, partitionKeyPath: String): ObjectNode = {
  getPatchItemWithSchema(id, partitionKeyPath, getPatchFullTestSchema())
 }

def getPatchItemWithFullSchemaSubpartitions(id: String): ObjectNode = {
  getPatchItemWithSchema(id, null, getPatchFullTestSchemaWithSubpartitions())
}

 def getPatchItemWithSchema(id: String, partitionKeyPath: String, schema: StructType): ObjectNode = {
  val objectNode = objectMapper.createObjectNode()
  objectNode.put(IdAttributeName, id)
  if (partitionKeyPath != null && partitionKeyPath != IdAttributeName) {
      objectNode.put(partitionKeyPath, UUID.randomUUID().toString)
  }
  val guid = UUID.randomUUID().toString

  for (field <- schema.fields) {
   field.dataType match {
    case IntegerType =>
     objectNode.put(field.name, RandomUtils.nextInt())
    case LongType =>
     objectNode.put(field.name, RandomUtils.nextLong())
    case FloatType =>
     objectNode.put(field.name, RandomUtils.nextFloat())
    case DoubleType =>
     objectNode.put(field.name, RandomUtils.nextDouble())
    case BooleanType =>
     objectNode.put(field.name, RandomUtils.nextBoolean())
    case StringType =>
        if (!field.name.equals("tenantId") && !field.name.equals("userId") && !field.name.equals("sessionId")) {
            objectNode.put(field.name, guid)
        }
        else if (field.name.equals("tenantId")) {
            objectNode.put(field.name, id)
        }
        else if (field.name.equals("userId")) {
            objectNode.put(field.name, "userId1")
        }
        else if (field.name.equals("sessionId")) {
            objectNode.put(field.name, "sessionId1")
        }
        else {
            objectNode.put(field.name, UUID.randomUUID().toString)
        }
    case _: ArrayType =>
     val arrayNode = objectNode.putArray(field.name)
     arrayNode.add(UUID.randomUUID().toString)
    case _ =>
     throw new IllegalArgumentException(s"${field.dataType} is not supported")
   }
  }

  objectNode
 }

 // Build the node based on the base node field values.
 // The goal here is to create a node with different value compared to base object
 def getPatchItemWithSchema(partitionKeyPath: String,
                            schema: StructType,
                            baseObjectNode: ObjectNode): ObjectNode = {
  val objectNode = objectMapper.createObjectNode()

  val idguid = UUID.randomUUID().toString
  for (field <- schema.fields) {
   field.dataType match {
    case IntegerType =>
     objectNode.put(field.name, baseObjectNode.get(field.name).intValue() + 1)
    case LongType =>
     objectNode.put(field.name, baseObjectNode.get(field.name).longValue() + 1)
    case FloatType =>
     objectNode.put(field.name, baseObjectNode.get(field.name).floatValue() * (-1))
    case DoubleType =>
     objectNode.put(field.name, baseObjectNode.get(field.name).doubleValue() * (-1))
    case BooleanType =>
     objectNode.put(field.name, !baseObjectNode.get(field.name).asBoolean())
    case StringType =>
     if (field.name != IdAttributeName && field.name != partitionKeyPath && !field.name.equals("tenantId") && !field.name.equals("userId") && !field.name.equals("sessionId")) {
      objectNode.put(field.name, idguid)
     }
     else if (field.name.equals("tenantId")) {
         if (baseObjectNode.get("id") != null) {
             objectNode.put(field.name, baseObjectNode.get("id").textValue())
         }
         else {
             objectNode.put(field.name, idguid)
         }
     }
     else if (field.name.equals("userId")) {
         objectNode.put(field.name, "userId1")
     }
     else if (field.name.equals("sessionId")) {
         objectNode.put(field.name, "sessionId1")
     }
     else {
      objectNode.put(field.name, baseObjectNode.get(field.name).textValue())
     }
    case _: ArrayType =>
     val arrayNode = objectNode.putArray(field.name)
     arrayNode.add(UUID.randomUUID().toString)
    case _ =>
     throw new IllegalArgumentException(s"${field.dataType} is not supported")
   }
  }




  // add id and partitionKey
  objectNode.put(IdAttributeName, baseObjectNode.get(IdAttributeName).textValue())
  objectNode
 }

 def getPatchFullTestSchema(): StructType = {
  StructType(Seq(
   StructField("propInt", IntegerType),
   StructField("propLong", LongType),
   StructField("propFloat", FloatType),
   StructField("propDouble", DoubleType),
   StructField("propBoolean", BooleanType),
   StructField("propString", StringType),
   StructField("propArray", ArrayType(StringType))
  ))
 }

def getPatchFullTestSchemaWithSubpartitions(): StructType = {
  StructType(Seq(
   StructField("propInt", IntegerType),
   StructField("propLong", LongType),
   StructField("propFloat", FloatType),
   StructField("propDouble", DoubleType),
   StructField("propBoolean", BooleanType),
   StructField("propString", StringType),
   StructField("tenantId", StringType),
   StructField("userId", StringType),
   StructField("sessionId", StringType),
   StructField("propArray", ArrayType(StringType))
  ))
}

 def getBulkWriterForPatch(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                           container: CosmosAsyncContainer,
                           partitionKeyDefinition: PartitionKeyDefinition,
                           patchPredicateFilter: Option[String] = None,
                           metricsPublisher: OutputMetricsPublisherTrait = new TestOutputMetricsPublisher): BulkWriter = {
  val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
  val writeConfigForPatch = CosmosWriteConfig(
   ItemWriteStrategy.ItemPatch,
   5,
   bulkEnabled = true,
   patchConfigs = Some(patchConfigs))

  new BulkWriter(
    container,
    partitionKeyDefinition,
    writeConfigForPatch,
    DiagnosticsConfig(),
    metricsPublisher,
    1)
 }

 def getBulkWriterForPatchBulkUpdate(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                           container: CosmosAsyncContainer,
                           partitionKeyDefinition: PartitionKeyDefinition,
                           patchPredicateFilter: Option[String] = None): BulkWriter = {
     val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
     val writeConfigForPatch = CosmosWriteConfig(
         ItemWriteStrategy.ItemBulkUpdate,
         5,
         bulkEnabled = true,
         patchConfigs = Some(patchConfigs))

     new BulkWriter(
       container,
       partitionKeyDefinition,
       writeConfigForPatch,
       DiagnosticsConfig(),
       new TestOutputMetricsPublisher,
       1)
 }

 def getPointWriterForPatch(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                            container: CosmosAsyncContainer,
                            partitionKeyDefinition: PartitionKeyDefinition,
                            patchPredicateFilter: Option[String] = None,
                            metricsPublisher: OutputMetricsPublisherTrait = new TestOutputMetricsPublisher): PointWriter = {

  val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
  val writeConfigForPatch = CosmosWriteConfig(
   ItemWriteStrategy.ItemPatch,
   5,
   bulkEnabled = false,
   patchConfigs = Some(patchConfigs))

  new PointWriter(
   container,
    partitionKeyDefinition,
    writeConfigForPatch,
    DiagnosticsConfig(),
    MockTaskContext.mockTaskContext(),
    metricsPublisher)
 }

 def getPointWriterForPatchBulkUpdate(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                                      container: CosmosAsyncContainer,
                                      partitionKeyDefinition: PartitionKeyDefinition,
                                      patchPredicateFilter: Option[String] = None): PointWriter = {

     val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
     val writeConfigForPatch = CosmosWriteConfig(
         ItemWriteStrategy.ItemBulkUpdate,
         5,
         bulkEnabled = false,
         patchConfigs = Some(patchConfigs))

     new PointWriter(
         container,
       partitionKeyDefinition,
       writeConfigForPatch,
       DiagnosticsConfig(),
       MockTaskContext.mockTaskContext(),
       new TestOutputMetricsPublisher)
 }

 def getPatchConfigTestSchema(): StructType = {
  StructType(Seq(
   StructField("byteTypeColumn", ByteType),
   StructField("doubleTypeColumn", DoubleType),
   StructField("floatTypeColumn", FloatType),
   StructField("longTypeColumn", LongType),
   StructField("decimalTypeColumn1", DecimalType(precision = 2, scale = 2)),
   StructField("decimalTypeColumn2", DecimalType.SYSTEM_DEFAULT),
   StructField("integerTypeColumn", IntegerType),
   StructField("shortTypeColumn", ShortType),
   StructField("stringTypeColumn", StringType),
   StructField("arrayTypeColumn", ArrayType(StringType)),
   StructField("binaryTypeColumn", BinaryType),
   StructField("booleanTypeColumn", BooleanType)
  ))
 }

 def getAllPermutationsOfKeyWord(keyword: String, result: String, permutations: ListBuffer[String]): Unit = {

  if (keyword.isEmpty) {
   //has reached to the end
   permutations += result
   return
  }

  val lowerCase = keyword.charAt(0).toLower
  val upperCase = keyword.charAt(0).toUpper
  val newKeyWord = keyword.substring(1)

  getAllPermutationsOfKeyWord(newKeyWord, result + lowerCase, permutations)
  getAllPermutationsOfKeyWord(newKeyWord, result + upperCase, permutations)
 }


 def getColumnConfigString(columnConfig: CosmosPatchColumnConfig): String ={
  var columnConfigString = ""
  columnConfigString += s"col(${columnConfig.columnName})."

  if (s"/${columnConfig.columnName}" != columnConfig.mappingPath) {
   columnConfigString += s"path(${columnConfig.mappingPath})."
  }

  columnConfigString += s"op(${columnConfig.operationType})"
  columnConfigString
 }

 /***
  * Get partition key path without "/" at the beginning
  * @param partitionKeyDefinition the partition key definition
  *
  * @return the partition key path without
  */
  // for hierarchical partitioning this is not used/circumvented, see logic in getPatchItemWithSchema()
 def getStrippedPartitionKeyPath(partitionKeyDefinition: PartitionKeyDefinition): String = {
  StringUtils.join(partitionKeyDefinition.getPaths, "").substring(1)
 }
}

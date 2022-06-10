// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.utils

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.models.PartitionKeyDefinition
import com.azure.cosmos.spark.{BulkWriter, CosmosPatchColumnConfig, CosmosPatchConfigs, CosmosWriteConfig, DiagnosticsConfig, ItemWriteStrategy, PointWriter}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils
import org.apache.spark.MockTaskContext
import org.apache.spark.sql.types.{ArrayType, BinaryType, BooleanType, ByteType, DecimalType, DoubleType, FloatType, IntegerType, LongType, ShortType, StringType, StructField, StructType}

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

object CosmosPatchTestHelper {
 private val objectMapper = new ObjectMapper()

 def getPatchItemWithFullSchema(id: String): ObjectNode = {
  getPatchItemWithSchema(id, getPatchFullTestSchema())
 }

 def getPatchItemWithSchema(id: String, schema: StructType): ObjectNode = {
  val objectNode = objectMapper.createObjectNode()
  objectNode.put("id", id)

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
     objectNode.put(field.name, UUID.randomUUID().toString)
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
 def getPatchItemWithSchema(id: String, schema: StructType, baseObjectNode: ObjectNode): ObjectNode = {
  val objectNode = objectMapper.createObjectNode()
  objectNode.put("id", id)

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
     objectNode.put(field.name, UUID.randomUUID().toString)
    case _: ArrayType =>
     val arrayNode = objectNode.putArray(field.name)
     arrayNode.add(UUID.randomUUID().toString)
    case _ =>
     throw new IllegalArgumentException(s"${field.dataType} is not supported")
   }
  }

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

 def getBulkWriterForPatch(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                           container: CosmosAsyncContainer,
                           partitionKeyDefinition: PartitionKeyDefinition,
                           patchPredicateFilter: Option[String] = None): BulkWriter = {
  val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
  val writeConfigForPatch = CosmosWriteConfig(
   ItemWriteStrategy.ItemPatch,
   5,
   bulkEnabled = true,
   patchConfigs = Some(patchConfigs))

  new BulkWriter(container, partitionKeyDefinition, writeConfigForPatch, DiagnosticsConfig(Option.empty, false, None))
 }

 def getPointWriterForPatch(columnConfigsMap: TrieMap[String, CosmosPatchColumnConfig],
                            container: CosmosAsyncContainer,
                            partitionKeyDefinition: PartitionKeyDefinition,
                            patchPredicateFilter: Option[String] = None): PointWriter = {

  val patchConfigs = CosmosPatchConfigs(columnConfigsMap, patchPredicateFilter)
  val writeConfigForPatch = CosmosWriteConfig(
   ItemWriteStrategy.ItemPatch,
   5,
   bulkEnabled = false,
   patchConfigs = Some(patchConfigs))

  new PointWriter(
   container, partitionKeyDefinition, writeConfigForPatch, DiagnosticsConfig(Option.empty, false, None), MockTaskContext.mockTaskContext())
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
}

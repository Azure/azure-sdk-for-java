// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.cosmos.implementation.HttpConstants
import com.azure.cosmos.models.{CosmosContainerProperties, ExcludedPath, FeedRange, IncludedPath, IndexingMode, IndexingPolicy, ModelBridgeInternal, PartitionKeyDefinition, PartitionKeyDefinitionVersion, SparkModelBridgeInternal, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{ContainerFeedRangesCache, CosmosConstants}
import com.azure.cosmos.{CosmosAsyncClient, CosmosException}

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.util
import java.util.Collections

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO: what is the difference of case class and class
case class CosmosCatalogCosmosSDKClient(cosmosAsyncClient: CosmosAsyncClient)
  extends CosmosCatalogClient
  with BasicLoggingTrait {

  override def close(): Unit = cosmosAsyncClient.close()

  override def readAllDatabases(): Array[Array[String]] =
    cosmosAsyncClient
      .readAllDatabases()
      .toIterable
      .asScala
      .map(database => Array(database.getId))
      .toArray

  override def readDatabase(databaseName: String): Unit = {
    cosmosAsyncClient.getDatabase(databaseName).read().block()
  }

  override def createDatabase(databaseName: String, metaData: Map[String, String]): Unit = {
    try {
        val throughputPropertiesOpt = getThroughputProperties(metaData)
        throughputPropertiesOpt match {
            case Some(throughputProperties) =>
                logDebug(
                    s"creating database $databaseName with shared throughput ${throughputPropertiesOpt.get}")
                cosmosAsyncClient.createDatabase(databaseName, throughputProperties).block()
            case None =>
                logDebug(s"creating database $databaseName")
                cosmosAsyncClient.createDatabase(databaseName).block()
        }
    }  catch {
        case e: CosmosException if e.getStatusCode == HttpConstants.StatusCodes.CONFLICT =>
            throw new CosmosCatalogConflictException(e.toString)
    }

  }

  override def deleteDatabase(databaseName: String): Unit =
    cosmosAsyncClient.getDatabase(databaseName).delete().block()

  override def readAllContainers(databaseName: String): List[String] =
    cosmosAsyncClient
      .getDatabase(databaseName)
      .readAllContainers()
      .toIterable
      .asScala
      .map(container => container.getId)
      .toList

  override def createContainer(databaseName: String, containerName: String, containerProperties: Map[String, String]): Unit = {

    val throughputPropertiesOpt = getThroughputProperties(containerProperties)
    val partitionKeyDefinition = getPartitionKeyDefinition(containerProperties)
    val indexingPolicy = getIndexingPolicy(containerProperties)

    val cosmosContainerProperties = new CosmosContainerProperties(containerName, partitionKeyDefinition)
    cosmosContainerProperties.setIndexingPolicy(indexingPolicy)

    CosmosContainerProperties.getDefaultTtlInSeconds(containerProperties) match {
      case Some(ttl) => cosmosContainerProperties.setDefaultTimeToLiveInSeconds(ttl)
      case None =>
    }

    CosmosContainerProperties.getAnalyticalStoreTtlInSeconds(containerProperties) match {
      case Some(ttl) => cosmosContainerProperties.setAnalyticalStoreTimeToLiveInSeconds(ttl)
      case None =>
    }

    throughputPropertiesOpt match {
      case Some(throughputProperties) =>
        cosmosAsyncClient
          .getDatabase(databaseName)
          .createContainer(cosmosContainerProperties, throughputProperties)
          .block()
      case None =>
        cosmosAsyncClient
          .getDatabase(databaseName)
          .createContainer(cosmosContainerProperties)
          .block()
    }
  }

  private def getIndexingPolicy(containerProperties: Map[String, String]): IndexingPolicy = {
    val indexingPolicySpecification = CosmosContainerProperties.getIndexingPolicy(containerProperties)
    //scalastyle:on multiple.string.literals
    if (CosmosContainerProperties.AllPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
      new IndexingPolicy()
        .setAutomatic(true)
        .setIndexingMode(IndexingMode.CONSISTENT)
        .setIncludedPaths(util.Arrays.asList(new IncludedPath("/*")))
        .setExcludedPaths(util.Arrays.asList(new ExcludedPath(raw"""/"_etag"/?""")))
    } else if (CosmosContainerProperties.OnlySystemPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
      new IndexingPolicy()
        .setAutomatic(true)
        .setIndexingMode(IndexingMode.CONSISTENT)
        .setIncludedPaths(Collections.emptyList())
        .setExcludedPaths(util.Arrays.asList(new ExcludedPath("/*")))
    } else {
      SparkModelBridgeInternal.createIndexingPolicyFromJson(indexingPolicySpecification)
    }
    //scalastyle:off multiple.string.literals
  }

  private def getPartitionKeyDefinition(containerProperties: Map[String, String]): PartitionKeyDefinition = {
    val partitionKeyPath = CosmosContainerProperties.getPartitionKeyPath(containerProperties)

    val partitionKeyDef = new PartitionKeyDefinition
    val paths = new util.ArrayList[String]
    paths.add(partitionKeyPath)
    partitionKeyDef.setPaths(paths)

    CosmosContainerProperties.getPartitionKeyVersion(containerProperties) match {
      case Some(pkVersion) => partitionKeyDef.setVersion(PartitionKeyDefinitionVersion.valueOf(pkVersion))
      case None =>
    }

    partitionKeyDef
  }

  private def getThroughputProperties(containerProperties: Map[String, String]): Option[ThroughputProperties] = {
    CosmosThroughputProperties.getManualThroughput(containerProperties) match {
      case Some(throughput) =>
        Some(ThroughputProperties.createManualThroughput(throughput))
      case None =>
        CosmosThroughputProperties.getAutoScaleMaxThroughput(containerProperties) match {
          case Some(autoScaleMaxThroughput) =>
            Some(ThroughputProperties.createAutoscaledThroughput(
              autoScaleMaxThroughput))
          case None => None
        }
    }
  }

  private def toMap(throughputProperties: ThroughputProperties): Map[String, String] = {
    val props = new util.HashMap[String, String]()
    val manualThroughput = throughputProperties.getManualThroughput
    if (manualThroughput != null) {
      props.put(CosmosThroughputProperties.manualThroughputFieldName, manualThroughput.toString)
    } else {
      val autoScaleMaxThroughput =
        throughputProperties.getAutoscaleMaxThroughput
      props.put(CosmosThroughputProperties.autoScaleMaxThroughputName, autoScaleMaxThroughput.toString)
    }
    props.asScala.toMap
  }

  override def deleteContainer(databaseName: String, containerName: String): Unit = {
      try {
          cosmosAsyncClient
              .getDatabase(databaseName)
              .getContainer(containerName)
              .delete()
              .block()
      } catch {
          case e: CosmosException if isNotFound(e) =>
              throw new CosmosCatalogNotFoundException(e.toString)
      }
  }

  override def readDatabaseThroughput(databaseName: String): Map[String, String] = {
    try {
      // validate whether the database exists
      cosmosAsyncClient.getDatabase(databaseName).read().block()

      val throughput = cosmosAsyncClient.getDatabase(databaseName).readThroughput().block()
      toMap(throughput.getProperties)
    } catch {
        case e: CosmosException if e.getStatusCode == HttpConstants.StatusCodes.NOTFOUND =>
            throw new CosmosCatalogNotFoundException(e.toString)
        case e: CosmosException if e.getStatusCode == HttpConstants.StatusCodes.BADREQUEST => Map[String, String]()
      // not a shared throughput database account
    }
  }

  private def isNotFound(exception: CosmosException) =
    exception.getStatusCode == 404

  override def readContainerMetadata(databaseName: String, containerName: String): Option[util.HashMap[String, String]] = {
    val container = cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName)

    val metaResultOpt = try {
      Some((
        container
          .read()
          .block()
          .getProperties,

        ContainerFeedRangesCache
          .getFeedRanges(container)
          .block(),

        try {
          Some(
            (
              container
                .readThroughput()
                .block()
                .getProperties,
              false
            ))
        } catch {
          case error: CosmosException => {
            if (error.getStatusCode != 400) {
              throw error
            }

            try {
              Some(
                (
                  container
                    .getDatabase
                    .readThroughput()
                    .block()
                    .getProperties,
                  true
                )
              )
            } catch {
              case error: CosmosException => {
                if (error.getStatusCode != 400) {
                  throw error
                }
                None
              }
            }
          }
        }
      ))
    } catch {
      case e: CosmosException if isNotFound(e) =>
        None
    }

    metaResultOpt match {
      case Some(metaDataResult) => Some(generateTblProperties(metaDataResult))
      case None => None
    }
  }

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  private def generateTblProperties
  (
    metadata: (CosmosContainerProperties, List[FeedRange], Option[(ThroughputProperties, Boolean)])
  ): util.HashMap[String, String] = {

    val containerProperties: CosmosContainerProperties = metadata._1
    val feedRanges: List[FeedRange] = metadata._2
    val throughputPropertiesOption: Option[(ThroughputProperties, Boolean)] = metadata._3

    val indexingPolicySnapshotJson = Option.apply(containerProperties.getIndexingPolicy) match {
      case Some(p) => ModelBridgeInternal.getJsonSerializable(p).toJson
      case None => "null"
    }

    val defaultTimeToLiveInSecondsSnapshot = Option.apply(containerProperties.getDefaultTimeToLiveInSeconds) match {
      case Some(defaultTtl) => defaultTtl.toString
      case None => "null"
    }

    val analyticalStoreTimeToLiveInSecondsSnapshot = Option.apply(containerProperties.getAnalyticalStoreTimeToLiveInSeconds) match {
      case Some(analyticalStoreTtl) => analyticalStoreTtl.toString
      case None => "null"
    }

    val lastModifiedSnapshot = ZonedDateTime
      .ofInstant(containerProperties.getTimestamp, ZoneOffset.UTC)
      .format(DateTimeFormatter.ISO_INSTANT)

    val provisionedThroughputSnapshot = throughputPropertiesOption match {
      case Some(throughputPropertiesTuple) =>
        val throughputProperties = throughputPropertiesTuple._1
        val isSharedThroughput = throughputPropertiesTuple._2
        val prefix = if (isSharedThroughput) {
          "Shared."
        } else {
          ""
        }
        val throughputLastModified = ZonedDateTime
          .ofInstant(throughputProperties.getTimestamp, ZoneOffset.UTC)
          .format(DateTimeFormatter.ISO_INSTANT)
        if (throughputProperties.getAutoscaleMaxThroughput == 0) {
          s"${prefix}Manual|${throughputProperties.getManualThroughput}|$throughputLastModified"
        } else {
          // AutoScale|CurrentRU|MaxRU
          s"${prefix}AutoScale|${throughputProperties.getManualThroughput}|" +
            s"${throughputProperties.getAutoscaleMaxThroughput}|" +
            s"$throughputLastModified"
        }
      case None => s"Unknown" // Right now should be serverless  - but because serverless isn't GA
      // yet keeping the contract vague here
    }

    val pkDefinitionJson = ModelBridgeInternal
      .getJsonSerializable(
        containerProperties.getPartitionKeyDefinition)
      .toJson

    val tableProperties = new util.HashMap[String, String]()
    tableProperties.put(
      CosmosConstants.TableProperties.PartitionKeyDefinition,
      s"'$pkDefinitionJson'"
    )

    tableProperties.put(
      CosmosConstants.TableProperties.PartitionCount,
      s"'${feedRanges.size.toString}'"
    )

    tableProperties.put(
      CosmosConstants.TableProperties.ProvisionedThroughput,
      s"'$provisionedThroughputSnapshot'"
    )
    tableProperties.put(
      CosmosConstants.TableProperties.LastModified,
      s"'$lastModifiedSnapshot'"
    )
    tableProperties.put(
      CosmosConstants.TableProperties.DefaultTtlInSeconds,
      s"'$defaultTimeToLiveInSecondsSnapshot'"
    )
    tableProperties.put(
      CosmosConstants.TableProperties.AnalyticalStoreTtlInSeconds,
      s"'$analyticalStoreTimeToLiveInSecondsSnapshot'"
    )
    tableProperties.put(
      CosmosConstants.TableProperties.IndexingPolicy,
      s"'$indexingPolicySnapshotJson'"
    )

    tableProperties
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}

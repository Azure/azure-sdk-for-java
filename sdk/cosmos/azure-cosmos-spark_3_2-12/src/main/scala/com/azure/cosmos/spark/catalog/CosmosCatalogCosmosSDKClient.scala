// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.models.{CosmosContainerProperties, ExcludedPath, FeedRange, IncludedPath, IndexingMode, IndexingPolicy, ModelBridgeInternal, PartitionKeyDefinition, PartitionKeyDefinitionVersion, SparkModelBridgeInternal, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{ContainerFeedRangesCache, CosmosConstants, Exceptions}
import reactor.core.publisher.Mono
import reactor.core.scala.publisher.SMono.{PimpJFlux, PimpJMono}
import reactor.core.scala.publisher.{SFlux, SMono}

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.util
import java.util.Collections

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO: what is the difference of case class and class
private[spark] case class CosmosCatalogCosmosSDKClient(cosmosAsyncClient: CosmosAsyncClient)
    extends CosmosCatalogClient
    with BasicLoggingTrait {

    override def close(): Unit = cosmosAsyncClient.close()

    override def readAllDatabases(): SFlux[String] =
        cosmosAsyncClient.readAllDatabases().asScala.map(_.getId)

    override def createDatabase(databaseName: String, metaData: Map[String, String]): SMono[Unit] = {
        Mono.just(getThroughputProperties(metaData))
            .asScala
            .flatMap(throughputPropertiesOpt => {
                throughputPropertiesOpt match {
                    case Some(throughputProperties) =>
                        logDebug(
                            s"creating database $databaseName with shared throughput ${throughputPropertiesOpt.get}")
                        cosmosAsyncClient.createDatabase(databaseName, throughputProperties).asScala
                    case None =>
                        logDebug(s"creating database $databaseName")
                        cosmosAsyncClient.createDatabase(databaseName).asScala
                }
            })
            .onErrorResume((throwable: Throwable) => {
                if (Exceptions.isResourceExistsException(throwable)) {
                    SMono.error(new CosmosCatalogConflictException(throwable.toString))
                } else {
                    SMono.error(throwable)
                }
            })
            .`then`()
    }

    override def deleteDatabase(databaseName: String): SMono[Unit] =
        cosmosAsyncClient.getDatabase(databaseName).delete().asScala.`then`()

    override def readAllContainers(databaseName: String): SFlux[String] =
        cosmosAsyncClient
            .getDatabase(databaseName)
            .readAllContainers()
            .onErrorResume(throwable => {
                if (Exceptions.isNotFoundException(throwable)) {
                    Mono.error(new CosmosCatalogNotFoundException(throwable.toString))
                } else {
                    Mono.error(throwable)
                }
            })
            .asScala
            .map(properties => properties.getId)

    override def createContainer(
                                    databaseName: String,
                                    containerName: String,
                                    containerProperties: Map[String, String]): SMono[Unit] = {

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
                    .asScala
                    .`then`()
            case None =>
                cosmosAsyncClient
                    .getDatabase(databaseName)
                    .createContainer(cosmosContainerProperties)
                    .asScala
                    .`then`()
        }
    }

    override def deleteContainer(databaseName: String, containerName: String): SMono[Unit] = {
        cosmosAsyncClient
            .getDatabase(databaseName)
            .getContainer(containerName)
            .delete()
            .asScala
            .`then`()
            .onErrorResume(throwable => {
                if (Exceptions.isNotFoundException(throwable)) {
                    SMono.error(new CosmosCatalogNotFoundException(throwable.toString))
                } else {
                    SMono.error(throwable)
                }
            })
    }

    override def readDatabaseThroughput(databaseName: String): SMono[Map[String, String]] = {
        val database = cosmosAsyncClient.getDatabase(databaseName)

        database
            .readThroughput()
            .asScala
            .map(throughputResponse => toMap(throughputResponse.getProperties))
            .onErrorResume((throwable: Throwable) => {
                if (Exceptions.isBadRequestException(throwable)) { // 400 - not a shared throughput database account
                    SMono.just(Map[String, String]())
                } else if (Exceptions.isNotFoundException(throwable)){
                    SMono.error(new CosmosCatalogNotFoundException(throwable.toString))
                } else {
                    SMono.error(throwable)
                }
            })
    }

    override def readDatabase(databaseName: String): SMono[Unit] =
        cosmosAsyncClient.getDatabase(databaseName).read().asScala.`then`()

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

    private def getThroughputProperties(properties: Map[String, String]): Option[ThroughputProperties] = {
        CosmosThroughputProperties.getManualThroughput(properties) match {
            case Some(throughput) =>
                Some(ThroughputProperties.createManualThroughput(throughput))
            case None =>
                CosmosThroughputProperties.getAutoScaleMaxThroughput(properties) match {
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

    override def readContainerMetadata(databaseName: String, containerName: String): SMono[Option[util.HashMap[String, String]]] = {
        val container = cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName)

        SFlux
            .zip3(
                container.read().asScala,
                ContainerFeedRangesCache.getFeedRanges(container),
                readContainerThroughputProperties(databaseName, containerName)
                    .map(Some(_))
                    .onErrorResume((throwable: Throwable) => {
                        if (Exceptions.isBadRequestException(throwable)) {
                            SMono.just(None) // Serverless database account
                        } else {
                            SMono.error(throwable)
                        }
                    }))
            .single()
            .map(result => {
                val metaResultOpt = Some((result._1.getProperties, result._2, result._3))
                metaResultOpt match {
                    case Some(metaDataResult) => Some(generateTblProperties(metaDataResult))
                    case _ => None
                }
            })
            .onErrorResume((throwable: Throwable) => {
                if (Exceptions.isNotFoundException(throwable)) {
                    SMono.just(None)
                } else {
                    SMono.error(throwable)
                }
            })
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

    override def readContainerThroughput(databaseName: String, containerName: String): SMono[Integer] = {
        readContainerThroughputProperties(databaseName, containerName)
            .map(throughputProperties => getMaxThroughput(throughputProperties._1))
    }

    private def getMaxThroughput(throughputProperties: ThroughputProperties): Int =
        Math.max(throughputProperties.getAutoscaleMaxThroughput, throughputProperties.getManualThroughput)

    private def readContainerThroughputProperties(databaseName: String, containerName: String): SMono[(ThroughputProperties, Boolean)] = {
        val database = cosmosAsyncClient.getDatabase(databaseName)
        val container = database.getContainer(containerName)

        container.readThroughput()
            .asScala
            .map(containerThroughputResponse => (containerThroughputResponse.getProperties(), false))
            .onErrorResume((throwable: Throwable) => {
                if (Exceptions.isBadRequestException(throwable)) {
                    database
                        .readThroughput()
                        .asScala
                        .map(databaseThroughputResponse => (databaseThroughputResponse.getProperties(), true))
                } else {
                    SMono.error(throwable)
                }
            })
    }
}

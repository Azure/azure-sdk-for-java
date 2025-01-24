// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.models.{FeedRange, PartitionKeyDefinitionVersion, SparkModelBridgeInternal, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.spark.{ContainerFeedRangesCache, CosmosConstants}
import com.azure.resourcemanager.cosmos.CosmosManager
import com.azure.resourcemanager.cosmos.models.{AutoscaleSettings, AutoscaleSettingsResource, ContainerPartitionKey, CreateUpdateOptions, ExcludedPath, IncludedPath, IndexingMode, IndexingPolicy, SqlContainerCreateUpdateParameters, SqlContainerGetPropertiesResource, SqlContainerResource, SqlDatabaseCreateUpdateParameters, SqlDatabaseResource, ThroughputSettingsGetPropertiesResource, ThroughputSettingsResource, ThroughputSettingsUpdateParameters}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.catalog.{NamespaceChange, TableChange}
import reactor.core.publisher.Mono
import reactor.core.scala.publisher.SMono.{PimpJFlux, PimpJMono}
import reactor.core.scala.publisher.{SFlux, SMono}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util
import java.util.Collections
import scala.collection.mutable.ListBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] case class CosmosCatalogManagementSDKClient(resourceGroupName: String,
                                                           databaseAccountName: String,
                                                           cosmosManager: CosmosManager,
                                                           cosmosAsyncClient: CosmosAsyncClient)
    extends CosmosCatalogClient
      with BasicLoggingTrait {

    private val objectMapper: ObjectMapper = new ObjectMapper()
    objectMapper.setSerializationInclusion(Include.NON_NULL)
    objectMapper.registerModule(
      com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule())
    private val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()

    override def close(): Unit = {}

    override def readAllDatabases(): SFlux[String] = {
        sqlResourcesClient
            .listSqlDatabasesAsync(resourceGroupName, databaseAccountName)
            .asScala
            .map(resultsInner => resultsInner.resource().id())
    }

    override def readDatabase(databaseName: String): SMono[Unit] = {
        sqlResourcesClient
            .getSqlDatabaseAsync(resourceGroupName, databaseAccountName, databaseName)
            .asScala
            .`then`()
    }

    override def createDatabase(databaseName: String, metaData: Map[String, String]): SMono[Unit] = {
        sqlResourcesClient
            .getSqlDatabaseAsync(resourceGroupName, databaseAccountName, databaseName)
            .asScala
            .flatMap(_ => {
                SMono.error(new CosmosCatalogConflictException(s"Database $databaseName already exists"))
            })
            .onErrorResume((throwable: Throwable) => {
                if (ManagementExceptions.isNotFoundException(throwable)) {
                    sqlResourcesClient.createUpdateSqlDatabaseAsync(
                        resourceGroupName,
                        databaseAccountName,
                        databaseName,
                        new SqlDatabaseCreateUpdateParameters()
                            .withResource(new SqlDatabaseResource().withId(databaseName))
                            .withOptions(getThroughputCreateUpdateOption(metaData))
                    ).asScala
                } else {
                    SMono.error(throwable)
                }
            }).`then`()
    }

    override def deleteDatabase(databaseName: String): SMono[Unit] =
        sqlResourcesClient.deleteSqlDatabaseAsync(resourceGroupName, databaseAccountName, databaseName).asScala.`then`()

    override def readAllContainers(databaseName: String): SFlux[String] = {
        sqlResourcesClient.listSqlContainersAsync(resourceGroupName, databaseAccountName, databaseName)
            .onErrorResume(throwable => {
                if (ManagementExceptions.isNotFoundException(throwable)) {
                    Mono.error(new CosmosCatalogNotFoundException(throwable.toString))
                } else {
                    Mono.error(throwable)
                }
            })
            .asScala
            .map(_.resource().id())
    }

    override def createContainer(
                                    databaseName: String,
                                    containerName: String,
                                    containerProperties: Map[String, String]): SMono[Unit] = {

        val sqlContainerResource = new SqlContainerResource()
        sqlContainerResource.withId(containerName)
        sqlContainerResource.withPartitionKey(getPartitionKeyDefinition(containerProperties))
        sqlContainerResource.withIndexingPolicy(getIndexingPolicy(containerProperties))

        // setup ttl
        CosmosContainerProperties.getDefaultTtlInSeconds(containerProperties) match {
            case Some(ttl) => sqlContainerResource.withDefaultTtl(ttl)
            case None =>
        }

        //setup analytical store ttl
        CosmosContainerProperties.getAnalyticalStoreTtlInSeconds(containerProperties) match {
            case Some(ttl) => sqlContainerResource.withAnalyticalStorageTtl(ttl)
            case None =>
        }

        sqlResourcesClient
            .createUpdateSqlContainerAsync(
                resourceGroupName,
                databaseAccountName,
                databaseName,
                containerName,
                new SqlContainerCreateUpdateParameters()
                    .withResource(sqlContainerResource)
                    .withOptions(getThroughputCreateUpdateOption(containerProperties))
            )
            .asScala.`then`()
    }

    override def deleteContainer(databaseName: String, containerName: String): SMono[Unit] = {
        // If the container does not exists, calling the following method will not get exceptions
        sqlResourcesClient
            .deleteSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
            .asScala
            .`then`()
    }

    override def readDatabaseThroughput(databaseName: String): SMono[Map[String, String]] = {
        sqlResourcesClient
            .getSqlDatabaseAsync(resourceGroupName, databaseAccountName, databaseName)
            .asScala
            .flatMap(_ => {
                sqlResourcesClient.getSqlDatabaseThroughputAsync(resourceGroupName, databaseAccountName, databaseName)
                    .asScala
                    .map(resultInner => toMap(resultInner.resource()))
                    .onErrorResume((throwable: Throwable) => {
                        // TODO: Annie : Follow up the contract here, why not 400
                        if (ManagementExceptions.isNotFoundException(throwable)) {
                            SMono.just(Map[String, String]()) // not a shared throughput database account
                        } else {
                            SMono.error(throwable)
                        }
                    })
            })
            .onErrorResume((throwable: Throwable) => {
                if (ManagementExceptions.isNotFoundException(throwable)) {
                    SMono.error(new CosmosCatalogNotFoundException(throwable.toString))
                } else {
                    SMono.error(throwable)
                }
            })
    }

    override def readContainerMetadata(databaseName: String, containerName: String): SMono[Option[util.HashMap[String, String]]] =
    {
        sqlResourcesClient
            .getSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
            .asScala
            .flatMap(containerResultInner => {
                SFlux.zip(
                    ContainerFeedRangesCache.getFeedRanges(cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName)),
                    readContainerThroughputProperties(databaseName, containerName)
                        .map(Some(_))
                        .onErrorResume((throwable: Throwable) => {
                            if (ManagementExceptions.isBadRequestException(throwable)) {
                                SMono.just(None) // Serverless database account
                            } else {
                                SMono.error(throwable)
                            }
                        }))
                    .single()
                    .map(result => {
                        val metaResult = (containerResultInner.resource(), result._1, result._2)
                        Some(generateTblProperties(metaResult))
                    })
            })
            .onErrorResume((throwable: Throwable) => {
                if (ManagementExceptions.isNotFoundException(throwable)) {
                    SMono.just(None)
                } else {
                    SMono.error(throwable)
                }
            })
    }

    private def toMap(throughputPropertiesResource: ThroughputSettingsGetPropertiesResource): Map[String, String] = {
        val props = new util.HashMap[String, String]()
        val autoScaleSettings = throughputPropertiesResource.autoscaleSettings()
        if (autoScaleSettings != null) {
            val autoScaleMaxThroughput = throughputPropertiesResource.autoscaleSettings().maxThroughput()
            props.put(CosmosThroughputProperties.autoScaleMaxThroughputName, autoScaleMaxThroughput.toString)
        } else {
            props.put(CosmosThroughputProperties.manualThroughputFieldName, throughputPropertiesResource.throughput().toString)
        }
        props.asScala.toMap
    }

    private def getPartitionKeyDefinition(containerProperties: Map[String, String]): ContainerPartitionKey = {
        val containerPartitionKey = new ContainerPartitionKey()

        val partitionKeyPath = CosmosContainerProperties.getPartitionKeyPath(containerProperties)
        val pkVersion = CosmosContainerProperties.getPartitionKeyVersion(containerProperties)

        containerPartitionKey.withPaths(util.Arrays.asList(partitionKeyPath))
        if (pkVersion.isDefined) {
            val partitionKeyDefinitionVersion = PartitionKeyDefinitionVersion.valueOf(pkVersion.get)
            partitionKeyDefinitionVersion match {
                case PartitionKeyDefinitionVersion.V1 => containerPartitionKey.withVersion(1)
                case PartitionKeyDefinitionVersion.V2 => containerPartitionKey.withVersion(2)
                case version: PartitionKeyDefinitionVersion => throw new IllegalStateException(s"Partition key version $version is not supported.")
            }
        }

        containerPartitionKey
    }

    private def getIndexingPolicy(containerProperties: Map[String, String]): IndexingPolicy = {
        val indexingPolicySpecification = CosmosContainerProperties.getIndexingPolicy(containerProperties)
        //scalastyle:on multiple.string.literals
        if (CosmosContainerProperties.AllPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
            new IndexingPolicy()
                .withAutomatic(true)
                .withIndexingMode(IndexingMode.CONSISTENT)
                .withIncludedPaths(util.Arrays.asList(new IncludedPath().withPath("/*")))
                .withExcludedPaths(util.Arrays.asList(new ExcludedPath().withPath(raw"""/"_etag"/?""")))
        } else if (CosmosContainerProperties.OnlySystemPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
            new IndexingPolicy()
                .withAutomatic(true)
                .withIndexingMode(IndexingMode.CONSISTENT)
                .withIncludedPaths(Collections.emptyList())
                .withExcludedPaths(util.Arrays.asList(new ExcludedPath().withPath("/*")))
        } else {
            val cosmosIndexingPolicy = SparkModelBridgeInternal.createIndexingPolicyFromJson(indexingPolicySpecification)
            val indexingMode = cosmosIndexingPolicy.getIndexingMode match {
                case com.azure.cosmos.models.IndexingMode.CONSISTENT => IndexingMode.CONSISTENT
                case com.azure.cosmos.models.IndexingMode.LAZY => IndexingMode.LAZY
                case com.azure.cosmos.models.IndexingMode.NONE => IndexingMode.NONE
                case cosmosIndexingMode: com.azure.cosmos.models.IndexingMode =>
                    throw new IllegalStateException(s"Indexing mode $cosmosIndexingMode is not supported")
            }

            val includedPathList = new ListBuffer[IncludedPath]()
            cosmosIndexingPolicy.getIncludedPaths.forEach(includedPath => {
                includedPathList += new IncludedPath().withPath(includedPath.getPath())
            })

            val excludedPathList = new ListBuffer[ExcludedPath]()
            cosmosIndexingPolicy.getExcludedPaths.forEach(excludedPath => {
                excludedPathList += new ExcludedPath().withPath(excludedPath.getPath())
            })

            new IndexingPolicy()
                .withAutomatic(cosmosIndexingPolicy.isAutomatic)
                .withIndexingMode(indexingMode)
                .withIncludedPaths(includedPathList.toList.asJava)
                .withExcludedPaths(excludedPathList.toList.asJava)
        }
        //scalastyle:off multiple.string.literals
    }

    private def getThroughputCreateUpdateOption(properties: Map[String, String]): CreateUpdateOptions = {
        val createUpdateOptions = new CreateUpdateOptions()
        CosmosThroughputProperties.getManualThroughput(properties) match {
            case Some(throughput) =>
                createUpdateOptions.withThroughput(throughput)
            case None =>
                CosmosThroughputProperties.getAutoScaleMaxThroughput(properties) match {
                    case Some(autoScaleMaxThroughput) =>
                        createUpdateOptions.withAutoscaleSettings(new AutoscaleSettings().withMaxThroughput(autoScaleMaxThroughput))
                    case None => createUpdateOptions
                }
        }
    }

    // scalastyle:off cyclomatic.complexity
    // scalastyle:off method.length
    private def generateTblProperties
    (
        metadata: (SqlContainerGetPropertiesResource, List[FeedRange], Option[(ThroughputSettingsGetPropertiesResource, Boolean)])
    ): util.HashMap[String, String] = {

        val containerProperties: SqlContainerGetPropertiesResource = metadata._1
        val feedRanges: List[FeedRange] = metadata._2
        val throughputPropertiesOption: Option[(ThroughputSettingsGetPropertiesResource, Boolean)] = metadata._3

        val indexingPolicySnapshotJson = Option.apply(containerProperties.indexingPolicy()) match {
            case Some(p) => objectMapper.writeValueAsString(p)
            case None => "null"
        }

        val defaultTimeToLiveInSecondsSnapshot = Option.apply(containerProperties.defaultTtl()) match {
            case Some(defaultTtl) => defaultTtl.toString
            case None => "null"
        }

        val analyticalStoreTimeToLiveInSecondsSnapshot = Option.apply(containerProperties.analyticalStorageTtl()) match {
            case Some(analyticalStoreTtl) => analyticalStoreTtl.toString
            case None => "null"
        }

        // TODO: Annie: Is it okie to do this way
        val lastModifiedSnapshot = ZonedDateTime
            .ofInstant(Instant.ofEpochSecond(containerProperties.ts().longValue()), ZoneOffset.UTC)
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

                val throughputSnapshotBuilder = new StringBuilder()
                if (throughputProperties.autoscaleSettings() == null) {
                    throughputSnapshotBuilder.appendAll(s"${prefix}Manual|${throughputProperties.throughput()}|")
                } else {
                    // AutoScale|CurrentRU|MaxRU
                    throughputSnapshotBuilder.appendAll(s"${prefix}AutoScale|${throughputProperties.throughput()}|" +
                        s"${throughputProperties.autoscaleSettings().maxThroughput()}|")
                }

                var throughputLastModified = Option.empty[String]
                // TODO: Annie: The ts() value has already been always returned as null - need to follow up the expected behavior
                if (throughputProperties.ts() != null) {
                    throughputLastModified = Some(ZonedDateTime
                        .ofInstant(Instant.ofEpochSecond(throughputProperties.ts().longValue()), ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                    throughputSnapshotBuilder.appendAll(s"$throughputLastModified")
                }

                throughputSnapshotBuilder.toString
            case None => s"Unknown" // Right now should be serverless  - but because serverless isn't GA
            // yet keeping the contract vague here
        }

        val pkDefinitionJson = objectMapper.writeValueAsString(containerProperties.partitionKey())

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
            .map(throughputPropertiesResource => getMaxThroughput(throughputPropertiesResource._1))
    }

    private def getMaxThroughput(throughputResource: ThroughputSettingsGetPropertiesResource): Integer = {
        if (throughputResource.autoscaleSettings() != null) {
            throughputResource.autoscaleSettings().maxThroughput()
        } else {
            throughputResource.throughput()
        }
    }

    private def readContainerThroughputProperties(databaseName: String, containerName: String): SMono[(ThroughputSettingsGetPropertiesResource, Boolean)] = {
        sqlResourcesClient
            .getSqlContainerThroughputAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
            .asScala
            .map(containerThroughputResultInner => (containerThroughputResultInner.resource(), false))
            .onErrorResume((throwable: Throwable) => {
                if (ManagementExceptions.isNotFoundException(throwable)) {
                    sqlResourcesClient
                        .getSqlDatabaseThroughputAsync(resourceGroupName, databaseAccountName, databaseName)
                        .asScala
                        .map(databaseThroughputResultInner => (databaseThroughputResultInner.resource(), true))
                } else {
                    SMono.error(throwable)
                }
            })
    }

    override def alterContainer
    (
      databaseName: String,
      containerName: String,
      finalThroughputProperty: TableChange.SetProperty
    ): SMono[Boolean] = {

        readContainerThroughputProperties(databaseName, containerName)
            .flatMap(throughPutPropertiesTuple => {
              if (throughPutPropertiesTuple._2) {
                throw new UnsupportedOperationException(
                  "ALTER TABLE cannot be used to modify throughput of a container using shared database throughput.")
              }

              val throughputUpdateParameter = if (CosmosThroughputProperties.manualThroughputFieldName
                .equalsIgnoreCase(finalThroughputProperty.property())) {

                new ThroughputSettingsUpdateParameters()
                  .withResource(new ThroughputSettingsResource().withThroughput(finalThroughputProperty.value().toInt))
              } else {
                new ThroughputSettingsUpdateParameters()
                  .withResource(new ThroughputSettingsResource()
                    .withAutoscaleSettings(
                      new AutoscaleSettingsResource()
                        .withMaxThroughput(finalThroughputProperty.value().toInt)))
              }

              sqlResourcesClient
                .updateSqlContainerThroughputAsync(
                  resourceGroupName,
                  databaseAccountName,
                  databaseName,
                  containerName,
                  throughputUpdateParameter)
                .asScala
                .map(result => {
                  if (result.resource().offerReplacePending() != null &&
                    result.resource().offerReplacePending() != "null" &&
                    !result.resource().offerReplacePending().toBoolean) {

                    logInfo(s"Updated throughput synchronously " +
                      s"(${finalThroughputProperty.property()}: ${finalThroughputProperty.value()}).")
                    true
                  } else {
                    logWarning(s"Throughput will be updated asynchronously " +
                      s"(${finalThroughputProperty.property()}: ${finalThroughputProperty.value()}).")
                    false
                  }
                })
            })
    }

  override def alterDatabase
  (
    databaseName: String,
    finalThroughputProperty: NamespaceChange.SetProperty): SMono[Boolean] = {

    readDatabaseThroughput(databaseName)
      .flatMap(throughPutPropertiesMap => {
        if (throughPutPropertiesMap.size == 0) {
          throw new UnsupportedOperationException(
            "ALTER NAMESPACE can only be used to modify throughput of a database with shared throughput being enabled.")
        }

        val throughputUpdateParameter = if (CosmosThroughputProperties.manualThroughputFieldName
          .equalsIgnoreCase(finalThroughputProperty.property())) {

          new ThroughputSettingsUpdateParameters()
            .withResource(new ThroughputSettingsResource().withThroughput(finalThroughputProperty.value().toInt))
        } else {
          new ThroughputSettingsUpdateParameters()
            .withResource(new ThroughputSettingsResource()
              .withAutoscaleSettings(
                new AutoscaleSettingsResource()
                  .withMaxThroughput(finalThroughputProperty.value().toInt)))
        }

        sqlResourcesClient
          .updateSqlDatabaseThroughputAsync(
            resourceGroupName,
            databaseAccountName,
            databaseName,
            throughputUpdateParameter)
          .asScala
          .map(result => {
            if (!result.resource().offerReplacePending().toBoolean &&
              result.resource().offerReplacePending() != "null" &&
              !result.resource().offerReplacePending().toBoolean) {
              logInfo(s"Updated throughput synchronously " +
                s"(${finalThroughputProperty.property()}: ${finalThroughputProperty.value()}).")
              true
            } else {
              logWarning(s"Throughput will be updated asynchronously " +
                s"(${finalThroughputProperty.property()}: ${finalThroughputProperty.value()}).")
              false
            }
          })
      })
  }
}

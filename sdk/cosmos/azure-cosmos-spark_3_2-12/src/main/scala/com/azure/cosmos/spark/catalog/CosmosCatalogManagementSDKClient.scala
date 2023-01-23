// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.{ContainerFeedRangesCache, CosmosConstants}
import com.azure.resourcemanager.cosmos.CosmosManager
import com.azure.resourcemanager.cosmos.models.{AutoscaleSettings, ContainerPartitionKey, CreateUpdateOptions, ExcludedPath, IncludedPath, IndexingMode, IndexingPolicy, SqlContainerCreateUpdateParameters, SqlContainerGetPropertiesResource, SqlContainerResource, SqlDatabaseCreateUpdateParameters, SqlDatabaseResource, ThroughputSettingsGetPropertiesResource}
import com.fasterxml.jackson.databind.ObjectMapper
import reactor.core.scala.publisher.SMono.{PimpJFlux, PimpJMono}
import reactor.core.scala.publisher.{SFlux, SMono}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util
import java.util.Collections

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO: figure out the error handling
private[spark] case class CosmosCatalogManagementSDKClient(resourceGroupName: String,
                                                           databaseAccountName: String,
                                                           cosmosManager: CosmosManager,
                                                           cosmosAsyncClient: CosmosAsyncClient)
  extends CosmosCatalogClient {

  val objectMapper: ObjectMapper = new ObjectMapper()
  val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()

  override def close(): Unit = {}

  override def readAllDatabases(): SFlux[String] = {
      sqlResourcesClient
          .listSqlDatabasesAsync(resourceGroupName, databaseAccountName)
          .asScala
          .map(resultsInner => resultsInner.id())
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
                val createUpdateOptions = new CreateUpdateOptions()

                CosmosThroughputProperties.getManualThroughput(metaData) match {
                    case Some(throughput) => createUpdateOptions.withThroughput(throughput)
                    case None =>
                        CosmosThroughputProperties.getAutoScaleMaxThroughput(metaData) match {
                            case Some(autoScaleMaxThroughput) =>
                                createUpdateOptions.withAutoscaleSettings(new AutoscaleSettings().withMaxThroughput(autoScaleMaxThroughput))
                            case None =>
                        }
                }

                sqlResourcesClient.createUpdateSqlDatabaseAsync(
                    resourceGroupName,
                    databaseAccountName,
                    databaseName,
                    new SqlDatabaseCreateUpdateParameters()
                        .withResource(new SqlDatabaseResource().withId(databaseName))
                        .withOptions(createUpdateOptions)
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
        .asScala
        .map(_.id())
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

    getThroughputUpdateOption(containerProperties) match {
      case Some(createUpdateOptions) =>
        sqlResourcesClient
            .createUpdateSqlContainerAsync(
                resourceGroupName,
                databaseAccountName,
                databaseName,
                containerName,
                new SqlContainerCreateUpdateParameters()
                  .withResource(sqlContainerResource)
                  .withOptions(createUpdateOptions)
            )
            .asScala.`then`()
      case None =>
        sqlResourcesClient
            .createUpdateSqlContainerAsync(
              resourceGroupName,
              databaseAccountName,
              databaseName,
              containerName,
              new SqlContainerCreateUpdateParameters()
                .withResource(sqlContainerResource)
            )
            .asScala
            .`then`()
    }
  }

  override def deleteContainer(databaseName: String, containerName: String): SMono[Unit] = {
      // TODO: Validate whether SDK will throw the above exception
      sqlResourcesClient.deleteSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
          .asScala
          .onErrorResume((throwable: Throwable) => {
              if (ManagementExceptions.isNotFoundException(throwable)) {
                  SMono.error(new CosmosCatalogNotFoundException(throwable.toString))
              } else {
                  SMono.error(throwable)
              }
          })
          .`then`()
  }

  override def readDatabaseThroughput(databaseName: String): SMono[Map[String, String]] = {
      sqlResourcesClient
          .getSqlDatabaseThroughputAsync(resourceGroupName, databaseAccountName, databaseName)
          .asScala
          .map(resultInner => toMap(resultInner.resource()))
          .onErrorResume((throwable: Throwable) => {
              if (ManagementExceptions.isNotFoundException(throwable)) {
                  SMono.error(new CosmosCatalogNotFoundException(throwable.toString))
              } else if (ManagementExceptions.isBadRequestException(throwable)) {
                  SMono.just(Map[String, String]())        // not a shared throughput database account
              } else {
                  SMono.error(throwable)
              }
          })
  }

  override def readContainerMetadata(databaseName: String, containerName: String): SMono[Option[util.HashMap[String, String]]] =
  {
      SFlux
          .zip3(
              sqlResourcesClient
                  .getSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
                  .asScala,
              ContainerFeedRangesCache
                  .getFeedRanges(cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName)),
              readContainerThroughputProperties(databaseName, containerName)
                  .map(Some(_))
                  .onErrorResume((throwable: Throwable) => {
                      if (ManagementExceptions.isBadRequestException(throwable)) {
                          SMono.just(None)
                      } else {
                          SMono.error(throwable)
                      }
                  }))
          .single()
          .map(result => {
              val metaResultOpt = Some((result._1.resource(), result._2, result._3))
              metaResultOpt match {
                  case Some(metaDataResult) => Some(generateTblProperties(metaDataResult))
                  case _ => None
              }
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
    val manualThroughput = throughputPropertiesResource.throughput()
    if (manualThroughput != null) {
      props.put(CosmosThroughputProperties.manualThroughputFieldName, manualThroughput.toString)
    } else {
      val autoScaleMaxThroughput = throughputPropertiesResource.autoscaleSettings().maxThroughput()
      props.put(CosmosThroughputProperties.autoScaleMaxThroughputName, autoScaleMaxThroughput.toString)
    }
    props.asScala.toMap
  }

  private def getPartitionKeyDefinition(containerProperties: Map[String, String]): ContainerPartitionKey = {
    val containerPartitionKey = new ContainerPartitionKey()

    val partitionKeyPath = CosmosContainerProperties.getPartitionKeyPath(containerProperties)
    val pkVersion = CosmosContainerProperties.getPartitionKeyVersion(containerProperties)

    containerPartitionKey.withPaths(util.Arrays.asList(partitionKeyPath))
    if (pkVersion.isDefined) {
      containerPartitionKey.withVersion(pkVersion.get.toInt)
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
      // TODO: add proper handling
      throw new IllegalStateException("Parsing from json string is not supported")
    }
    //scalastyle:off multiple.string.literals
  }

  private def getThroughputUpdateOption(containerProperties: Map[String, String]): Option[CreateUpdateOptions] = {
    CosmosThroughputProperties.getManualThroughput(containerProperties) match {
      case Some(throughput) =>
        Some(new CreateUpdateOptions().withThroughput(throughput))
      case None =>
        CosmosThroughputProperties.getAutoScaleMaxThroughput(containerProperties) match {
          case Some(autoScaleMaxThroughput) =>
            Some(new CreateUpdateOptions().withAutoscaleSettings(
              new AutoscaleSettings().withMaxThroughput(autoScaleMaxThroughput)))
          case None => None
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
                val throughputLastModified = ZonedDateTime
                    .ofInstant(Instant.ofEpochSecond(throughputProperties.ts().longValue()), ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT)

                if (throughputProperties.autoscaleSettings().maxThroughput() == 0) { // TODO: validate whether it is 0 or null
                    s"${prefix}Manual|${throughputProperties.throughput()}|$throughputLastModified"
                } else {
                    // AutoScale|CurrentRU|MaxRU
                    s"${prefix}AutoScale|${throughputProperties.throughput()}|" +
                        s"${throughputProperties.autoscaleSettings().maxThroughput()}|" +
                        s"$throughputLastModified"
                }
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

    private def getMaxThroughput(throughputResource: ThroughputSettingsGetPropertiesResource): Integer =
        Math.max(throughputResource.throughput(), throughputResource.autoscaleSettings().maxThroughput())

    private def readContainerThroughputProperties(databaseName: String, containerName: String): SMono[(ThroughputSettingsGetPropertiesResource, Boolean)] = {
        sqlResourcesClient
            .getSqlContainerThroughputAsync(resourceGroupName, databaseAccountName, databaseName, containerName)
            .asScala
            .map(containerThroughputResultInner => (containerThroughputResultInner.resource(), false))
            .onErrorResume((throwable: Throwable) => {
                if (ManagementExceptions.isBadRequestException(throwable)) {
                    sqlResourcesClient
                        .getSqlDatabaseThroughputAsync(resourceGroupName, databaseAccountName, databaseName)
                        .asScala
                        .map(databaseThroughputResultInner => (databaseThroughputResultInner.resource(), true))
                } else {
                    SMono.error(throwable)
                }
            })
    }
}

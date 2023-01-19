// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import com.azure.core.management.exception.ManagementException
import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils
import com.azure.cosmos.models.FeedRange
import com.azure.cosmos.spark.{ContainerFeedRangesCache, CosmosConstants}
import com.azure.resourcemanager.cosmos.CosmosManager
import com.azure.resourcemanager.cosmos.fluent.models.ThroughputSettingsGetResultsInner
import com.azure.resourcemanager.cosmos.models.{AutoscaleSettings, ContainerPartitionKey, CreateUpdateOptions, ExcludedPath, IncludedPath, IndexingMode, IndexingPolicy, SqlContainerCreateUpdateParameters, SqlContainerGetPropertiesResource, SqlContainerResource, SqlDatabaseCreateUpdateParameters, SqlDatabaseResource, ThroughputSettingsGetPropertiesResource}
import com.fasterxml.jackson.databind.ObjectMapper

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
  override def close(): Unit = {}

  override def readAllDatabases(): Array[Array[String]] = {
    val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
    sqlResourcesClient.listSqlDatabases(resourceGroupName, databaseAccountName)
      .iterator()
      .asScala
      .toList
      .map(resultInner => Array(resultInner.id()))
      .toArray
  }

  override def readDatabase(databaseName: String): Unit = {
    val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
    sqlResourcesClient.getSqlDatabase(resourceGroupName, databaseAccountName, databaseName)
  }

  override def createDatabase(databaseName: String, metaData: Map[String, String]): Unit = {
    val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()

    try {
        this.readDatabase(databaseName);
        // If we reached here, then it means the database already exists
        throw new CosmosCatalogConflictException(s"Database $databaseName already exists")

    } catch {
                // TODO: Annie: Extract the logic for checking notFound
        case e: ManagementException if isNotFound(e) =>
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
            ).block()
    }
  }

  override def deleteDatabase(databaseName: String): Unit = {
    val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
    sqlResourcesClient.deleteSqlDatabase(resourceGroupName, databaseAccountName, databaseName)
  }

  override def readAllContainers(databaseName: String): List[String] = {
    val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
    sqlResourcesClient.listSqlContainers(resourceGroupName, databaseAccountName, databaseName)
      .iterator()
      .asScala
      .toList
      .map(resultInner => resultInner.id())
  }

  override def createContainer(
                                databaseName: String,
                                containerName: String,
                                containerProperties: Map[String, String]): Unit = {

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
        cosmosManager.serviceClient().getSqlResources().createUpdateSqlContainerAsync(
          resourceGroupName,
          databaseAccountName,
          databaseName,
          containerName,
          new SqlContainerCreateUpdateParameters()
            .withResource(sqlContainerResource)
            .withOptions(createUpdateOptions)
        ).block()
      case None =>
        cosmosManager.serviceClient().getSqlResources().createUpdateSqlContainerAsync(
          resourceGroupName,
          databaseAccountName,
          databaseName,
          containerName,
          new SqlContainerCreateUpdateParameters()
            .withResource(sqlContainerResource)
        ).block()
    }
  }

  override def deleteContainer(databaseName: String, containerName: String): Unit = {
      // TODO: Validate whether SDK will throw the above exception
      try {
          cosmosManager
              .serviceClient()
              .getSqlResources()
              .deleteSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName).block()
      } catch {
          case e: ManagementException if isNotFound(e) =>
              throw new CosmosCatalogNotFoundException(e.toString)
      }
  }

  override def readDatabaseThroughput(databaseName: String): Map[String, String] = {
    try {
        val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
        toMap(
            sqlResourcesClient.getSqlDatabaseThroughputAsync(
                resourceGroupName,
                databaseAccountName,
                databaseName).block())
    } catch {
        case e: ManagementException if isNotFound(e) =>
            throw new CosmosCatalogNotFoundException(e.toString)
        case e: ManagementException if e.getValue != null && isBadRequest(e) =>
            Map[String, String]() // TODO: [Annie] Need to validate whether will get 400 for serverless account
        // not a shared throughput database account
    }
  }

  override def readContainerMetadata(databaseName: String, containerName: String): Option[util.HashMap[String, String]] =
  {
      val sqlResourceClient = cosmosManager.serviceClient().getSqlResources()
      val container = cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName)

      val metaResultOpt = try {
          Some((
              sqlResourceClient.getSqlContainerAsync(
                  resourceGroupName,
                  databaseAccountName,
                  databaseName,
                  containerName
              ).block().resource(),

              ContainerFeedRangesCache
                  .getFeedRanges(container)
                  .block(),

              try {
                  Some(
                      (
                          sqlResourceClient.getSqlContainerThroughputAsync(
                              resourceGroupName,
                              databaseAccountName,
                              databaseName,
                              containerName
                          ).block().resource(),
                          false
                      ))
              } catch {
                  case error: ManagementException => {
                      // TODO: Annie: double check the logic here
                      if (error.getValue != null && error.getValue.getCode != "BadRequest") {
                          throw error
                      }

                      try {
                          Some(
                              (
                                  sqlResourceClient.getSqlDatabaseThroughputAsync(
                                      resourceGroupName,
                                      databaseAccountName,
                                      databaseName
                                  ).block().resource(),
                                  true
                              )
                          )
                      } catch {
                          case error: ManagementException => {
                              if (isBadRequest(error)) {
                                  throw error
                              }
                              None
                          }
                      }
                  }
              }
          ))
      } catch {
          case e: ManagementException if isNotFound(e) =>
              None
      }

      metaResultOpt match {
          case Some(metaDataResult) => Some(generateTblProperties(metaDataResult))
          case None => None
      }
  }

  private def isNotFound(exception: ManagementException) =
        exception.getValue != null && StringUtils.equalsIgnoreCase(exception.getValue.getCode, "NotFound")

  private def isBadRequest(exception: ManagementException) =
      exception.getValue != null && StringUtils.equalsIgnoreCase(exception.getValue.getCode, "BadRequest")

  private def toMap(throughputResult: ThroughputSettingsGetResultsInner): Map[String, String] = {
    val props = new util.HashMap[String, String]()
    val manualThroughput = throughputResult.resource().throughput()
    if (manualThroughput != null) {
      props.put(CosmosThroughputProperties.manualThroughputFieldName, manualThroughput.toString)
    } else {
      val autoScaleMaxThroughput =
        throughputResult.resource().autoscaleSettings().maxThroughput()
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
}

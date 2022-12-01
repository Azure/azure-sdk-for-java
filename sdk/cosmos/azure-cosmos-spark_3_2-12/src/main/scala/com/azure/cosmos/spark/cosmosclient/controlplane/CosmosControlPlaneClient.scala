// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.cosmosclient.controlplane

import com.azure.cosmos.spark.cosmosclient.ICosmosClient
import com.azure.resourcemanager.cosmos.CosmosManager
import com.azure.resourcemanager.cosmos.fluent.models.ThroughputSettingsGetResultsInner
import com.azure.resourcemanager.cosmos.models._

import java.util
import java.util.Collections
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO: figure out the error handling
case class CosmosControlPlaneClient(resourceGroupName: String,
                                    databaseAccountName: String,
                                    cosmosManager: CosmosManager)
    extends ICosmosClient {

    override def close(): Unit = {}

    override def readAllDataBases(): Array[Array[String]] = {
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
        cosmosManager
            .serviceClient()
            .getSqlResources()
            .deleteSqlContainerAsync(resourceGroupName, databaseAccountName, databaseName, containerName).block()
    }

    override def readDatabaseThroughput(databaseName: String): Map[String, String] = {
        val sqlResourcesClient = cosmosManager.serviceClient().getSqlResources()
        toMap(sqlResourcesClient.getSqlDatabaseThroughput(resourceGroupName, databaseAccountName, databaseName))
    }

    override def readContainerMetadata(databaseName: String, containerName: String): Option[util.HashMap[String, String]] = ???


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
}

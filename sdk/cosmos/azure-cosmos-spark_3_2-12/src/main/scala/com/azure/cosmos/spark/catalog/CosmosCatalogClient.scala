// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import reactor.core.scala.publisher.{SFlux, SMono}

import java.util

private[spark] trait CosmosCatalogClient {
    def close()
    def readAllDatabases(): SFlux[String]
    def readDatabase(databaseName: String): SMono[Unit]
    def readDatabaseThroughput(databaseName: String): SMono[Map[String, String]]
    def createDatabase(databaseName: String, metaData: Map[String, String]): SMono[Unit]
    def deleteDatabase(databaseName: String): SMono[Unit]
    def readAllContainers(databaseName: String): SFlux[String]
    def createContainer(databaseName: String, containerName: String, containerProperties: Map[String, String]): SMono[Unit]
    def deleteContainer(databaseName: String, containerName: String): SMono[Unit]
    def readContainerMetadata(databaseName: String, containerName: String): SMono[Option[util.HashMap[String, String]]]
    def readContainerThroughput(databaseName: String, containerName: String): SMono[Integer]

    object CosmosContainerProperties {
        val OnlySystemPropertiesIndexingPolicyName: String = "OnlySystemProperties"
        val AllPropertiesIndexingPolicyName: String = "AllProperties"

        private val partitionKeyPath = "partitionKeyPath"
        private val partitionKeyVersion = "partitionKeyVersion"
        private val indexingPolicy = "indexingPolicy"
        private val defaultTtlPropertyName = "defaultTtlInSeconds"
        private val analyticalStoreTtlPropertyName = "analyticalStoreTtlInSeconds"
        private val defaultPartitionKeyPath = "/id"
        private val defaultIndexingPolicy = AllPropertiesIndexingPolicyName

        def getPartitionKeyPath(properties: Map[String, String]): String = {
            properties.getOrElse(partitionKeyPath, defaultPartitionKeyPath)
        }

        def getPartitionKeyVersion(properties: Map[String, String]): Option[String] = {
            properties.get(partitionKeyVersion)
        }

        def getIndexingPolicy(properties: Map[String, String]): String = {
            properties.getOrElse(indexingPolicy, defaultIndexingPolicy)
        }

        def getDefaultTtlInSeconds(properties: Map[String, String]): Option[Int] = {
            if (properties.contains(defaultTtlPropertyName)) {
                Some(properties(defaultTtlPropertyName).toInt)
            } else {
                None
            }
        }

        def getAnalyticalStoreTtlInSeconds(properties: Map[String, String]): Option[Int] = {
            if (properties.contains(analyticalStoreTtlPropertyName)) {
                Some(properties(analyticalStoreTtlPropertyName).toInt)
            } else {
                None
            }
        }
    }


    object CosmosThroughputProperties {
        val manualThroughputFieldName = "manualThroughput"
        val autoScaleMaxThroughputName = "autoScaleMaxThroughput"

        def getManualThroughput(properties: Map[String, String]): Option[Int] = {
            if (properties.contains(manualThroughputFieldName)) {
                Some(properties(manualThroughputFieldName).toInt)
            } else {
                None
            }
        }

        def getAutoScaleMaxThroughput(properties: Map[String, String]): Option[Int] = {
            if (properties.contains(autoScaleMaxThroughputName)) {
                Some(properties(autoScaleMaxThroughputName).toInt)
            } else {
                None
            }
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import java.util

trait CosmosCatalogClient {
    def close()
    def readAllDatabases(): Array[Array[String]]
    def readDatabase(databaseName: String): Unit
    def readDatabaseThroughput(databaseName:String): Map[String, String]
    def createDatabase(databaseName: String, metaData: Map[String, String]): Unit
    def deleteDatabase(databaseName: String): Unit
    def readAllContainers(databaseName: String): List[String]
    def createContainer(databaseName: String, containerName: String, containerProperties: Map[String, String]): Unit
    def deleteContainer(databaseName: String, containerName: String): Unit
    def readContainerMetadata(databaseName: String, containerName: String): Option[util.HashMap[String, String]]

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

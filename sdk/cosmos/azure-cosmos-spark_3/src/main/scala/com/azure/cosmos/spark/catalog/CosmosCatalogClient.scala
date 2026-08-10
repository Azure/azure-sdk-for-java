// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.catalog

import org.apache.spark.sql.connector.catalog.{NamespaceChange, TableChange}
import reactor.core.scala.publisher.{SFlux, SMono}

import java.util

private[spark] trait CosmosCatalogClient {
    def close()
    def readAllDatabases(): SFlux[String]
    def readDatabase(databaseName: String): SMono[Unit]
    def readDatabaseThroughput(databaseName: String): SMono[Map[String, String]]
    def createDatabase(databaseName: String, metaData: Map[String, String]): SMono[Unit]
    def deleteDatabase(databaseName: String): SMono[Unit]
    def alterDatabase(databaseName: String, finalThroughputProperty: NamespaceChange.SetProperty): SMono[Boolean]
    def readAllContainers(databaseName: String): SFlux[String]
    def createContainer(databaseName: String, containerName: String, containerProperties: Map[String, String]): SMono[Unit]
    def deleteContainer(databaseName: String, containerName: String): SMono[Unit]
    def alterContainer(databaseName: String, containerName: String, finalThroughputProperty: TableChange.SetProperty): SMono[Boolean]
    def readContainerMetadata(databaseName: String, containerName: String): SMono[Option[util.HashMap[String, String]]]
    def readContainerThroughput(databaseName: String, containerName: String): SMono[Integer]
}

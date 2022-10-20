// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ArrayNode

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.util
import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer
// scalastyle:off underscore.import
import com.azure.cosmos.models._
// scalastyle:on underscore.import
import com.azure.cosmos.CosmosException
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.{NamespaceAlreadyExistsException, NonEmptyNamespaceException, NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.connector.catalog.{CatalogPlugin, Identifier, NamespaceChange, SupportsNamespaces, Table, TableCatalog, TableChange}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.streaming.HDFSMetadataLog
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util.Collections
import scala.annotation.tailrec

// scalastyle:off underscore.import
import scala.collection.JavaConverters._

class CosmosCatalog
  extends CosmosCatalogBase
    with SupportsNamespaces {

    override def listNamespaces(): Array[Array[String]] = {
        logDebug("catalog:listNamespaces")

        TransientErrorsRetryPolicy.executeWithRetry(() => listNamespacesImpl())
    }

    @throws(classOf[NoSuchNamespaceException])
    override def listNamespaces(
                                   namespace: Array[String]): Array[Array[String]] = {
        loadNamespaceMetadata(namespace) // throws NoSuchNamespaceException if namespace doesn't exist
        // Cosmos DB only has one single level depth databases
        Array.empty[Array[String]]
    }

    @throws(classOf[NoSuchNamespaceException])
    override def loadNamespaceMetadata(
                                          namespace: Array[String]): util.Map[String, String] = {

        TransientErrorsRetryPolicy.executeWithRetry(() => loadNamespaceMetadataImpl(namespace))
    }

    @throws(classOf[NamespaceAlreadyExistsException])
    override def createNamespace(namespace: Array[String],
                                 metadata: util.Map[String, String]): Unit = {
        TransientErrorsRetryPolicy.executeWithRetry(() => createNamespaceImpl(namespace, metadata))
    }

    @throws(classOf[UnsupportedOperationException])
    override def alterNamespace(namespace: Array[String],
                                changes: NamespaceChange*): Unit = {
        checkNamespace(namespace)
        // TODO: moderakh we can support changing database level throughput?
        throw new UnsupportedOperationException("altering namespace not supported")
    }


    @throws(classOf[NoSuchNamespaceException])
    def dropNamespace(namespace: Array[String]): Boolean = {
        TransientErrorsRetryPolicy.executeWithRetry(() => dropNamespaceImpl(namespace))
    }

    @throws(classOf[NoSuchNamespaceException])
    override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
        TransientErrorsRetryPolicy.executeWithRetry(() => dropNamespaceImpl(namespace))
    }
}
// scalastyle:on multiple.string.literals
// scalastyle:on number.of.methods
// scalastyle:on file.size.limit

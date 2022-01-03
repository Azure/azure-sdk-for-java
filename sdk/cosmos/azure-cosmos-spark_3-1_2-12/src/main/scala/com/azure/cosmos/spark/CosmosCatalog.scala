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
import org.apache.spark.sql.catalyst.analysis.{NamespaceAlreadyExistsException, NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.connector.catalog.{CatalogPlugin, Identifier, NamespaceChange, SupportsNamespaces, Table, TableCatalog, TableChange}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.streaming.HDFSMetadataLog
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util.Collections
import scala.annotation.tailrec

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// CosmosCatalog provides a meta data store for Cosmos database, container control plane
// This will be required for hive integration
// relevant interfaces to implement:
//  - SupportsNamespaces (Cosmos Database and Cosmos Container can be modeled as namespace)
//  - SupportsCatalogOptions // TODO moderakh
//  - CatalogPlugin - A marker interface to provide a catalog implementation for Spark.
//      Implementations can provide catalog functions by implementing additional interfaces
//      for tables, views, and functions.
//  - TableCatalog Catalog methods for working with Tables.

// All Hive keywords are case-insensitive, including the names of Hive operators and functions.
// scalastyle:off multiple.string.literals
// scalastyle:off number.of.methods
// scalastyle:off file.size.limit
class CosmosCatalog
  extends CatalogPlugin
    with SupportsNamespaces
    with TableCatalog
    with BasicLoggingTrait {

  private lazy val sparkSession = SparkSession.active

  // mutable but only expected to be changed from within initialize method
  private var catalogName: String = _
  //private var client: CosmosAsyncClient = _
  private var config: Map[String, String] = _
  private var readConfig: CosmosReadConfig = _
  private var tableOptions: Map[String, String] = _
  private var viewRepository: Option[HDFSMetadataLog[String]] = None

  /**
   * Called to initialize configuration.
   * <p>
   * This method is called once, just after the provider is instantiated.
   *
   * @param name    the name used to identify and load this catalog
   * @param options a case-insensitive string map of configuration
   */
  override def initialize(name: String,
                          options: CaseInsensitiveStringMap): Unit = {
    this.config = CosmosConfig.getEffectiveConfig(
      None,
      None,
      options.asCaseSensitiveMap().asScala.toMap)
    this.readConfig = CosmosReadConfig.parseCosmosReadConfig(config)

    tableOptions = toTableConfig(options)
    this.catalogName = name

    val viewRepositoryConfig = CosmosViewRepositoryConfig.parseCosmosViewRepositoryConfig(config)
    if (viewRepositoryConfig.metaDataPath.isDefined) {
      this.viewRepository = Some(new HDFSMetadataLog[String](
        this.sparkSession,
        viewRepositoryConfig.metaDataPath.get))
    }
  }

  /**
   * Catalog implementations are registered to a name by adding a configuration option to Spark:
   * spark.sql.catalog.catalog-name=com.example.YourCatalogClass.
   * All configuration properties in the Spark configuration that share the catalog name prefix,
   * spark.sql.catalog.catalog-name.(key)=(value) will be passed in the case insensitive
   * string map of options in initialization with the prefix removed.
   * name, is also passed and is the catalog's name; in this case, "catalog-name".
   *
   * @return catalog name
   */
  override def name(): String = catalogName

  /**
   * List top-level namespaces from the catalog.
   * <p>
   * If an object such as a table, view, or function exists, its parent namespaces must also exist
   * and must be returned by this discovery method. For example, if table a.t exists, this method
   * must return ["a"] in the result array.
   *
   * @return an array of multi-part namespace names.
   */
  override def listNamespaces(): Array[Array[String]] = {
    logDebug("catalog:listNamespaces")

    TransientErrorsRetryPolicy.executeWithRetry(() => listNamespacesImpl())
  }

  private[this] def listNamespacesImpl(): Array[Array[String]] = {
    logDebug("catalog:listNamespaces")

    Loan(CosmosClientCache(
      CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
      None,
      s"CosmosCatalog(name $catalogName).listNamespaces"
    ))
      .to(cosmosClientCacheItem => {
        cosmosClientCacheItem
          .client
          .readAllDatabases()
          .toIterable
          .asScala
          .map(database => Array(database.getId))
          .toArray
      })
  }

  /**
   * List namespaces in a namespace.
   * <p>
   * Cosmos supports only single depth database. Hence we always return an empty list of namespaces.
   * or throw if the root namespace doesn't exist
   */
  @throws(classOf[NoSuchNamespaceException])
  override def listNamespaces(
                               namespace: Array[String]): Array[Array[String]] = {
    loadNamespaceMetadata(namespace) // throws NoSuchNamespaceException if namespace doesn't exist
    // Cosmos DB only has one single level depth databases
    Array.empty[Array[String]]
  }

  /**
   * Load metadata properties for a namespace.
   *
   * @param namespace a multi-part namespace
   * @return a string map of properties for the given namespace
   * @throws NoSuchNamespaceException If the namespace does not exist (optional)
   */
  @throws(classOf[NoSuchNamespaceException])
  override def loadNamespaceMetadata(
                                      namespace: Array[String]): util.Map[String, String] = {

    TransientErrorsRetryPolicy.executeWithRetry(() => loadNamespaceMetadataImpl(namespace))
  }

  private[this] def loadNamespaceMetadataImpl(
                                      namespace: Array[String]): util.Map[String, String] = {

    checkNamespace(namespace)

    Loan(CosmosClientCache(
      CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
      None,
      s"CosmosCatalog(name $catalogName).loadNamespaceMetadata([${namespace.mkString(", ")}])"
    ))
      .to(clientCacheItem => {
        val databaseName = toCosmosDatabaseName(namespace.head)
        try {
          clientCacheItem.client.getDatabase(databaseName).read().block()
        } catch {
          case e: CosmosException if isNotFound(e) =>
            throw new NoSuchNamespaceException(namespace)
        }

        try {
          val throughput = clientCacheItem
            .client
            .getDatabase(toCosmosDatabaseName(namespace.head))
            .readThroughput()
            .block()
          CosmosThroughputProperties.toMap(throughput.getProperties).asJava
        } catch {
          case e: CosmosException if e.getStatusCode == 400 =>
            Map[String, String]().asJava
          // not a shared throughput database account
        }
      })
  }

  @throws(classOf[NamespaceAlreadyExistsException])
  override def createNamespace(namespace: Array[String],
                               metadata: util.Map[String, String]): Unit = {
    TransientErrorsRetryPolicy.executeWithRetry(() => createNamespaceImpl(namespace, metadata))
  }

  @throws(classOf[NamespaceAlreadyExistsException])
  private[this] def createNamespaceImpl(namespace: Array[String],
                               metadata: util.Map[String, String]): Unit = {
    checkNamespace(namespace)
    val throughputPropertiesOpt =
      CosmosThroughputProperties.tryGetThroughputProperties(
        metadata.asScala.toMap)
    val databaseName = toCosmosDatabaseName(namespace.head)

    Loan(CosmosClientCache(
      CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
      None,
      s"CosmosCatalog(name $catalogName).createNamespace([${namespace.mkString(", ")}])"
    ))
      .to(cosmosClientCacheItem => {
        try {
          if (throughputPropertiesOpt.isDefined) {
            logDebug(
              s"creating database $databaseName with shared throughput ${throughputPropertiesOpt.get}")
            cosmosClientCacheItem.client
              .createDatabase(databaseName, throughputPropertiesOpt.get)
              .block()
          } else {
            logDebug(s"creating database $databaseName")
            cosmosClientCacheItem.client.createDatabase(databaseName).block()
          }

        } catch {
          case e: CosmosException if alreadyExists(e) =>
            throw new NamespaceAlreadyExistsException(namespace)
        }
      })
  }

  @throws(classOf[UnsupportedOperationException])
  override def alterNamespace(namespace: Array[String],
                              changes: NamespaceChange*): Unit = {
    checkNamespace(namespace)
    // TODO: moderakh we can support changing database level throughput?
    throw new UnsupportedOperationException("altering namespace not supported")
  }

  /**
   * Drop a namespace from the catalog, recursively dropping all objects within the namespace.
   *
   * @param namespace - a multi-part namespace
   * @return true if the namespace was dropped
   */
  @throws(classOf[NoSuchNamespaceException])
  override def dropNamespace(namespace: Array[String]): Boolean = {
    TransientErrorsRetryPolicy.executeWithRetry(() => dropNamespaceImpl(namespace))
  }

  @throws(classOf[NoSuchNamespaceException])
  private[this] def dropNamespaceImpl(namespace: Array[String]): Boolean = {
    checkNamespace(namespace)
    try {
      Loan(CosmosClientCache(
        CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
        None,
        s"CosmosCatalog(name $catalogName).dropNamespace([${namespace.mkString(", ")}])"
      ))
        .to(cosmosClientCacheItem => {
          cosmosClientCacheItem
            .client
            .getDatabase(toCosmosDatabaseName(namespace.head))
            .delete()
            .block()
        })
      true
    } catch {
      case e: CosmosException if isNotFound(e) =>
        throw new NoSuchNamespaceException(namespace)
    }
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    TransientErrorsRetryPolicy.executeWithRetry(() => listTablesImpl(namespace))
  }

  private[this] def listTablesImpl(namespace: Array[String]): Array[Identifier] = {
    checkNamespace(namespace)
    val databaseName = toCosmosDatabaseName(namespace.head)

    try {
      val cosmosTables =
        Loan(CosmosClientCache(
          CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
          None,
          s"CosmosCatalog(name $catalogName).listTables([${namespace.mkString(", ")}])"
        ))
          .to(cosmosClientCacheItem => {
            cosmosClientCacheItem
              .client
              .getDatabase(databaseName)
              .readAllContainers()
              .toIterable
              .asScala
              .map(prop => getContainerIdentifier(namespace.head, prop))
          })

      val tableIdentifiers = this.tryGetViewDefinitions(databaseName) match {
        case Some(viewDefinitions) =>
          cosmosTables ++ viewDefinitions.map(viewDef => getContainerIdentifier(namespace.head, viewDef)).toIterable
        case None => cosmosTables
      }

      tableIdentifiers.toArray
    } catch {
      case e: CosmosException if isNotFound(e) =>
        throw new NoSuchNamespaceException(namespace)
    }
  }

  override def loadTable(ident: Identifier): Table = {
    TransientErrorsRetryPolicy.executeWithRetry(() => loadTableImpl(ident))
  }

  private[this] def loadTableImpl(ident: Identifier): Table = {
    checkNamespace(ident.namespace())
    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())
    logInfo(s"loadTable DB:$databaseName, Container: $containerName")

    this.tryGetContainerMetadata(databaseName, containerName) match {
      case Some(metadata) =>
        val tableProperties: util.HashMap[String, String] = generateTblProperties(metadata)

        new ItemsTable(
          sparkSession,
          Array[Transform](),
          Some(databaseName),
          Some(containerName),
          tableOptions.asJava,
          None,
          tableProperties)
      case None =>
        this.tryGetViewDefinition(databaseName, containerName) match {
          case Some(viewDefinition) =>
            val effectiveOptions = tableOptions ++ viewDefinition.options
            new ItemsReadOnlyTable(
              sparkSession,
              Array[Transform](),
              None,
              None,
              effectiveOptions.asJava,
              viewDefinition.userProvidedSchema)
          case None =>
            throw new NoSuchTableException(ident)
        }
    }
  }

  override def createTable(ident: Identifier,
                           schema: StructType,
                           partitions: Array[Transform],
                           properties: util.Map[String, String]): Table = {

    TransientErrorsRetryPolicy.executeWithRetry(() =>
      createTableImpl(ident, schema, partitions, properties))
  }

  private[this] def createTableImpl(ident: Identifier,
                           schema: StructType,
                           partitions: Array[Transform],
                           properties: util.Map[String, String]): Table = {
    checkNamespace(ident.namespace())

    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())
    val containerProperties = properties.asScala.toMap

    if (CosmosViewRepositoryConfig.isCosmosView(containerProperties)) {
      createViewTable(ident, databaseName, containerName, schema, partitions, containerProperties)
    } else {
      createPhysicalTable(databaseName, containerName, schema, partitions, containerProperties)
    }
  }

  @throws(classOf[UnsupportedOperationException])
  override def alterTable(ident: Identifier, changes: TableChange*): Table = {
    // TODO: moderakh we can support updating indexing policy and throughput
    throw new UnsupportedOperationException
  }

  override def dropTable(ident: Identifier): Boolean = {
    TransientErrorsRetryPolicy.executeWithRetry(() => dropTableImpl(ident))
  }

  private[this] def dropTableImpl(ident: Identifier): Boolean = {
    checkNamespace(ident.namespace())

    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())

    if (deleteViewTable(databaseName, containerName)) {
      true
    } else {
      this.deletePhysicalTable(databaseName, containerName)
    }
  }

  @throws(classOf[UnsupportedOperationException])
  override def renameTable(oldIdent: Identifier, newIdent: Identifier): Unit = {
    throw new UnsupportedOperationException("renaming table not supported")
  }

  //scalastyle:off method.length
  private def createPhysicalTable(databaseName: String,
                                  containerName: String,
                                  schema: StructType,
                                  partitions: Array[Transform],
                                  containerProperties: Map[String, String]): Table = {
     logInfo(s"createPhysicalTable DB:$databaseName, Container: $containerName")

    val throughputPropertiesOpt = CosmosThroughputProperties
      .tryGetThroughputProperties(containerProperties)

    val partitionKeyPath =
      CosmosContainerProperties.getPartitionKeyPath(containerProperties)

    val partitionKeyDef = new PartitionKeyDefinition
    val paths = new util.ArrayList[String]
    paths.add(partitionKeyPath)
    partitionKeyDef.setPaths(paths)

    CosmosContainerProperties.getPartitionKeyVersion(containerProperties) match {
      case Some(pkVersion) => partitionKeyDef.setVersion(pkVersion)
      case None =>
    }

    val indexingPolicy = CosmosContainerProperties.getIndexingPolicy(containerProperties)
    val cosmosContainerProperties = new CosmosContainerProperties(containerName, partitionKeyDef)
    cosmosContainerProperties.setIndexingPolicy(indexingPolicy)

    CosmosContainerProperties.getDefaultTtlInSeconds(containerProperties) match {
      case Some(ttl) => cosmosContainerProperties.setDefaultTimeToLiveInSeconds(ttl)
      case None =>
    }

    CosmosContainerProperties.getAnalyticalStoreTtlInSeconds(containerProperties) match {
      case Some(ttl) => cosmosContainerProperties.setAnalyticalStoreTimeToLiveInSeconds(ttl)
      case None =>
    }

    Loan(CosmosClientCache(
      CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
      None,
      s"CosmosCatalog(name $catalogName).createPhysicalTable($databaseName, $containerName)"
    ))
      .to(cosmosClientCacheItem => {
        if (throughputPropertiesOpt.isDefined) {
          cosmosClientCacheItem
            .client
            .getDatabase(databaseName)
            .createContainer(cosmosContainerProperties,
              throughputPropertiesOpt.get)
            .block()
        } else {
          cosmosClientCacheItem
            .client
            .getDatabase(databaseName)
            .createContainer(cosmosContainerProperties)
            .block()
        }
      })

    val effectiveOptions = tableOptions ++ containerProperties

    new ItemsTable(
      sparkSession,
      partitions,
      Some(databaseName),
      Some(containerName),
      effectiveOptions.asJava,
      Option.apply(schema))
  }
  //scalastyle:on method.length

  //scalastyle:off method.length
  @tailrec
  private def createViewTable(ident: Identifier,
                              databaseName: String,
                              viewName: String,
                              schema: StructType,
                              partitions: Array[Transform],
                              containerProperties: Map[String, String]): Table = {

    logInfo(s"createViewTable DB:$databaseName, View: $viewName")

    this.viewRepository match {
      case Some(viewRepositorySnapshot) =>
        val userProvidedSchema = if (schema != null && schema.length > 0) {
          Some(schema)
        } else {
          None
        }
        val viewDefinition = ViewDefinition(
          databaseName, viewName, userProvidedSchema, redactAuthInfo(containerProperties))
        var lastBatchId = 0L
        val newViewDefinitionsSnapshot = viewRepositorySnapshot.getLatest() match {
          case Some(viewDefinitionsEnvelopeSnapshot) =>
            lastBatchId = viewDefinitionsEnvelopeSnapshot._1
            val alreadyExistingViews = ViewDefinitionEnvelopeSerializer.fromJson(viewDefinitionsEnvelopeSnapshot._2)

            if (alreadyExistingViews.exists(v => v.databaseName.equals(databaseName) &&
              v.viewName.equals(viewName))) {

              throw new IllegalArgumentException(s"View '$viewName' already exists in database '$databaseName'")
            }

            alreadyExistingViews ++ Array(viewDefinition)
          case None => Array(viewDefinition)
        }

        if (viewRepositorySnapshot.add(
          lastBatchId + 1,
          ViewDefinitionEnvelopeSerializer.toJson(newViewDefinitionsSnapshot))) {

          logInfo(s"LatestBatchId: ${viewRepositorySnapshot.getLatestBatchId().getOrElse(-1)}")
          viewRepositorySnapshot.purge(lastBatchId)
          logInfo(s"LatestBatchId: ${viewRepositorySnapshot.getLatestBatchId().getOrElse(-1)}")
          val effectiveOptions = tableOptions ++ viewDefinition.options

          new ItemsReadOnlyTable(
            sparkSession,
            partitions,
            None,
            None,
            effectiveOptions.asJava,
            userProvidedSchema)
        } else {
          createViewTable(ident, databaseName, viewName, schema, partitions, containerProperties)
        }
      case None =>
        throw new IllegalArgumentException(
          s"Catalog configuration for '${CosmosViewRepositoryConfig.MetaDataPathKeyName}' must " +
            "be set when creating views'")
    }
  }
  //scalastyle:on method.length

  private def deletePhysicalTable(databaseName: String, containerName: String): Boolean = {
    try {
      Loan(CosmosClientCache(
        CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
        None,
        s"CosmosCatalog(name $catalogName).deletePhysicalTable($databaseName, $containerName)"
      ))
        .to (cosmosClientCacheItem =>
          cosmosClientCacheItem
            .client
            .getDatabase(databaseName)
            .getContainer(containerName)
            .delete()
            .block())
      true
    } catch {
      case e: CosmosException if isNotFound(e) => false
    }
  }

  @tailrec
  private def deleteViewTable(databaseName: String, viewName: String): Boolean = {
    logInfo(s"deleteViewTable DB:$databaseName, View: $viewName")

    this.viewRepository match {
      case Some(viewRepositorySnapshot) =>
        viewRepositorySnapshot.getLatest() match {
          case Some(viewDefinitionsEnvelopeSnapshot) =>
            val lastBatchId = viewDefinitionsEnvelopeSnapshot._1
            val viewDefinitions = ViewDefinitionEnvelopeSerializer.fromJson(viewDefinitionsEnvelopeSnapshot._2)

            viewDefinitions.find(v => v.databaseName.equals(databaseName) &&
              v.viewName.equals(viewName)) match {
              case Some(existingView) =>
                val updatedViewDefinitionsSnapshot: Array[ViewDefinition] =
                  (ArrayBuffer(viewDefinitions: _*) - existingView).toArray

                if (viewRepositorySnapshot.add(
                  lastBatchId + 1,
                  ViewDefinitionEnvelopeSerializer.toJson(updatedViewDefinitionsSnapshot))) {

                  viewRepositorySnapshot.purge(lastBatchId)
                  true
                } else {
                  deleteViewTable(databaseName, viewName)
                }
              case None => false
            }
          case None => false
        }
      case None =>
        false
    }
  }

  //scalastyle:off method.length
  private def tryGetContainerMetadata
  (
    databaseName: String,
    containerName: String
  ): Option[(CosmosContainerProperties, List[FeedRange], Option[(ThroughputProperties, Boolean)])] = {

    try {
      Some(
        Loan(CosmosClientCache(
          CosmosClientConfiguration(config, readConfig.forceEventualConsistency),
          None,
          s"CosmosCatalog(name $catalogName).tryGetContainerMetadata($databaseName, $containerName)"))
          .to(cosmosClientCacheItem => {

            val container = cosmosClientCacheItem
              .client
              .getDatabase(databaseName)
              .getContainer(containerName)

            (
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
            )
          }))
    } catch {
      case e: CosmosException if isNotFound(e) =>
        None
    }
  }
  //scalastyle:on method.length

  private def tryGetViewDefinition(databaseName: String,
                                   containerName: String): Option[ViewDefinition] = {

    this.tryGetViewDefinitions(databaseName) match {
      case Some(viewDefinitions) =>
        viewDefinitions.find(v => databaseName.equals(v.databaseName) &&
          containerName.equals(v.viewName))
      case None => None
    }
  }

  private def tryGetViewDefinitions(databaseName: String): Option[Array[ViewDefinition]] = {

    this.viewRepository match {
      case Some(viewRepositorySnapshot) =>
        viewRepositorySnapshot.getLatest() match {
          case Some(latestMetadataSnapshot) =>
            val viewDefinitions = ViewDefinitionEnvelopeSerializer.fromJson(latestMetadataSnapshot._2)
              .filter(v => databaseName.equals(v.databaseName))
            if (viewDefinitions.length > 0) {
              Some(viewDefinitions)
            } else {
              None
            }
          case None => None
        }
      case None => None
    }
  }

  private def isNotFound(exception: CosmosException) =
    exception.getStatusCode == 404

  private def alreadyExists(exception: CosmosException) =
    exception.getStatusCode == 409

  private def getContainerIdentifier(
                                      namespaceName: String,
                                      cosmosContainerProperties: CosmosContainerProperties): Identifier = {
    Identifier.of(Array(namespaceName), cosmosContainerProperties.getId)
  }

  private def getContainerIdentifier
  (
    namespaceName: String,
    viewDefinition: ViewDefinition
  ): Identifier = {

    Identifier.of(Array(namespaceName), viewDefinition.viewName)
  }

  private def checkNamespace(namespace: Array[String]): Unit = {
    if (namespace == null || namespace.length != 1) {
      throw new CosmosCatalogException(
        s"invalid namespace ${namespace.mkString("Array(", ", ", ")")}." +
          s" Cosmos DB already support single depth namespace.")
    }
  }

  private def toCosmosDatabaseName(namespace: String): String = {
    namespace
  }

  private def toCosmosContainerName(tableIdent: String): String = {
    tableIdent
  }

  private def toTableConfig(options: CaseInsensitiveStringMap): Map[String, String] = {
    options.asCaseSensitiveMap().asScala.toMap
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

    val indexingPolicySnapshotJson =  Option.apply(containerProperties.getIndexingPolicy) match {
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

  private def redactAuthInfo(cfg: Map[String, String]): Map[String, String] = {
    cfg.filter((kvp) => !CosmosConfigNames.AccountEndpoint.equalsIgnoreCase(kvp._1) &&
      !CosmosConfigNames.AccountKey.equalsIgnoreCase(kvp._1) &&
      !kvp._1.toLowerCase.contains(CosmosConfigNames.AccountEndpoint.toLowerCase()) &&
      !kvp._1.toLowerCase.contains(CosmosConfigNames.AccountKey.toLowerCase())
    )
  }

  private object CosmosContainerProperties {
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

    def getPartitionKeyVersion(properties: Map[String, String]): Option[PartitionKeyDefinitionVersion] = {
      if (properties.contains(partitionKeyVersion)) {
        val pkVersion = properties(partitionKeyVersion).toUpperCase
        Some(PartitionKeyDefinitionVersion.valueOf(pkVersion))
      } else {
        None
      }
    }

    def getIndexingPolicy(properties: Map[String, String]): IndexingPolicy = {
      val indexingPolicySpecification = properties.getOrElse(indexingPolicy, defaultIndexingPolicy)

      //scalastyle:off multiple.string.literals
      if (AllPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
        new IndexingPolicy()
          .setAutomatic(true)
          .setIndexingMode(IndexingMode.CONSISTENT)
          .setIncludedPaths(util.Arrays.asList(new IncludedPath("/*")))
          .setExcludedPaths(util.Arrays.asList(new ExcludedPath(raw"""/"_etag"/?""")))
      } else if (OnlySystemPropertiesIndexingPolicyName.equalsIgnoreCase(indexingPolicySpecification)) {
        new IndexingPolicy()
          .setAutomatic(true)
          .setIndexingMode(IndexingMode.CONSISTENT)
          .setIncludedPaths(Collections.emptyList())
          .setExcludedPaths(util.Arrays.asList(new ExcludedPath("/*")))
      } else {
        SparkModelBridgeInternal.createIndexingPolicyFromJson(indexingPolicySpecification)
      }
      //scalastyle:on multiple.string.literals
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

  private object CosmosThroughputProperties {
    private val manualThroughputFieldName = "manualThroughput"
    private val autoScaleMaxThroughputName = "autoScaleMaxThroughput"

    def tryGetThroughputProperties(
                                    properties: Map[String, String]): Option[ThroughputProperties] = {
      properties
        .get(manualThroughputFieldName)
        .map(
          manualThroughput =>
            ThroughputProperties.createManualThroughput(manualThroughput.toInt)
        )
        .orElse(
          properties
            .get(autoScaleMaxThroughputName)
            .map(
              autoScaleMaxThroughput =>
                ThroughputProperties.createAutoscaledThroughput(
                  autoScaleMaxThroughput.toInt)
            )
        )
    }

    def toMap(
               throughputProperties: ThroughputProperties): Map[String, String] = {
      val props = new util.HashMap[String, String]()
      val manualThroughput = throughputProperties.getManualThroughput
      if (manualThroughput != null) {
        props.put(manualThroughputFieldName, manualThroughput.toString)
      } else {
        val autoScaleMaxThroughput =
          throughputProperties.getAutoscaleMaxThroughput
        props.put(autoScaleMaxThroughputName, autoScaleMaxThroughput.toString)
      }
      props.asScala.toMap
    }
  }
}
// scalastyle:on multiple.string.literals
// scalastyle:on number.of.methods
// scalastyle:on file.size.limit

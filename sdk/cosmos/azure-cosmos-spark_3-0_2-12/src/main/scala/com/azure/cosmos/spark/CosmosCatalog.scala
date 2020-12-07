// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import java.util

import com.azure.cosmos.models.{CosmosContainerProperties, ThroughputProperties}
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import org.apache.spark.sql.catalyst.analysis.{NamespaceAlreadyExistsException, NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.connector.catalog.{CatalogPlugin, Identifier, NamespaceChange, SupportsNamespaces, Table, TableCatalog, TableChange}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

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
class CosmosCatalog extends CatalogPlugin
  with SupportsNamespaces
  with TableCatalog
  with CosmosLoggingTrait {

  private var catalogName: String = _
  private var client: CosmosAsyncClient = _

  var tableOptions: Map[String, String] = _

  /**
    * Called to initialize configuration.
    * <p>
    * This method is called once, just after the provider is instantiated.
    *
    * @param name the name used to identify and load this catalog
    * @param options a case-insensitive string map of configuration
    */
  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = {
    val accountConfig = CosmosAccountConfig.parseCosmosAccountConfig(options.asCaseSensitiveMap().asScala.toMap)
    this.client = new CosmosClientBuilder()
      .key(accountConfig.key)
      .endpoint(accountConfig.endpoint)
      .buildAsyncClient()

    // TODO: moderakh do we need to do config validation here?
    tableOptions = toTableConfig(options)
    this.catalogName = name
  }

  /**
    * Catalog implementations are registered to a name by adding a configuration option to Spark:
    * spark.sql.catalog.catalog-name=com.example.YourCatalogClass.
    * All configuration properties in the Spark configuration that share the catalog name prefix,
    * spark.sql.catalog.catalog-name.(key)=(value) will be passed in the case insensitive
    * string map of options in initialization with the prefix removed.
    * name, is also passed and is the catalog's name; in this case, "catalog-name".
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
    getClient.readAllDatabases()
      .toIterable
      .asScala
      .map(database => Array(database.getId))
      .toArray
  }
  /**
    * List namespaces in a namespace.
    * <p>
    * Cosmos supports only single depth database. Hence we always return an empty list of namespaces.
    * or throw if the root namespace doesn't exist
    */
  @throws(classOf[NoSuchNamespaceException])
  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
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
  override def loadNamespaceMetadata(namespace: Array[String]): util.Map[String, String] = {
    checkNamespace(namespace)
    val databaseName = toCosmosDatabaseName(namespace.head)
    try {
      client.getDatabase(databaseName).read().block()
    } catch {
      case e: CosmosException if isNotFound(e) => throw new NoSuchNamespaceException(namespace)
    }

    try {
      val throughput = client.getDatabase(toCosmosDatabaseName(namespace.head)).readThroughput().block()
      CosmosThroughputProperties.toMap(throughput.getProperties).asJava
    } catch {
      case e: CosmosException if e.getStatusCode == 400 => Map[String, String]().asJava
      // not a shared throughput database account
    }
  }

  @throws(classOf[NamespaceAlreadyExistsException])
  override def createNamespace(namespace: Array[String], metadata: util.Map[String, String]): Unit = {
    checkNamespace(namespace)
    val throughputPropertiesOpt = CosmosThroughputProperties.tryGetThroughputProperties(metadata.asScala.toMap)
    val databaseName = toCosmosDatabaseName(namespace.head)

    try {
      if (throughputPropertiesOpt.isDefined) {
        logDebug(s"creating database $databaseName with shared throughput ${throughputPropertiesOpt.get}")
        getClient.createDatabase(databaseName, throughputPropertiesOpt.get).block()
      } else {
        logDebug(s"creating database $databaseName")
        getClient.createDatabase(databaseName).block()
      }

    } catch {
      case e: CosmosException if alreadyExists(e) =>
        throw new NamespaceAlreadyExistsException(namespace)
    }
  }

  class CosmosCatalogException(msg: String) extends RuntimeException(msg)

  @throws(classOf[UnsupportedOperationException])
  override def alterNamespace(namespace: Array[String], changes: NamespaceChange*): Unit = {
    checkNamespace(namespace)
    // TODO: moderakh we can support changing database level throughput?
    throw new UnsupportedOperationException("altering namespace not supported")
  }

  /**
    * Drop a namespace from the catalog, recursively dropping all objects within the namespace.
    * @param namespace - a multi-part namesp
    * @return true if the namespace was dropped
    */
  @throws(classOf[NoSuchNamespaceException])
  override def dropNamespace(namespace: Array[String]): Boolean = {
    checkNamespace(namespace)
    try {
      getClient.getDatabase(toCosmosDatabaseName(namespace.head)).delete().block()
      true
    } catch {
      case e: CosmosException if isNotFound(e) => throw new NoSuchNamespaceException(namespace)
    }
  }

  private def getClient: CosmosAsyncClient = {
    // TODO moderakh client caching
    this.client
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    checkNamespace(namespace)
    val databaseName = toCosmosDatabaseName(namespace.head)

    try {
      getClient.getDatabase(databaseName).readAllContainers()
        .toIterable
        .asScala
        .map(prop => getContainerIdentifier(namespace.head, prop))
        .toArray
    } catch {
      case e: CosmosException if isNotFound(e) => throw new NoSuchNamespaceException(namespace)
    }
  }

  override def loadTable(ident: Identifier): Table = {
    checkNamespace(ident.namespace())
    getContainerMetadata(ident) // validates that table exists
    // scalastyle:off null
    new CosmosTable(null, null, tableOptions.asJava)
    // scalastyle:off on
  }

  private def getContainerMetadata(ident: Identifier) : CosmosContainerProperties = {
    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())

    try {
      getClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    } catch {
      case e: CosmosException if isNotFound(e) => throw new NoSuchTableException(ident)
    }
  }

  override def createTable(ident: Identifier, schema: StructType, partitions: Array[Transform], properties: util.Map[String, String]): Table = {
    checkNamespace(ident.namespace())

    val containerProperties = properties.asScala.toMap
    val throughputPropertiesOpt = CosmosThroughputProperties
      .tryGetThroughputProperties(containerProperties)

    val partitionKeyPath = CosmosContainerProperties.getPartitionKeyPath(containerProperties)
    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())

    if (throughputPropertiesOpt.isDefined) {
      getClient.getDatabase(databaseName).createContainer(containerName,
        partitionKeyPath,
        throughputPropertiesOpt.get
      ).block()
    } else {
      getClient.getDatabase(databaseName).createContainer(containerName,
        partitionKeyPath
      ).block()
    }
      // TODO: moderakh this needs to be wired up against CosmosTabl
    new CosmosTable(schema, partitions, tableOptions.asJava)
  }

  @throws(classOf[UnsupportedOperationException])
  override def alterTable(ident: Identifier, changes: TableChange*): Table = {
    // TODO: moderakh we can support updating indexing policy and throughput
    throw new UnsupportedOperationException
  }

  override def dropTable(ident: Identifier): Boolean = {
    checkNamespace(ident.namespace())

    val databaseName = toCosmosDatabaseName(ident.namespace().head)
    val containerName = toCosmosContainerName(ident.name())
    try {
      getClient.getDatabase(databaseName).getContainer(containerName).delete().block()
      true
    } catch {
      case e: CosmosException if isNotFound(e) => false
    }
  }

  @throws(classOf[UnsupportedOperationException])
  override def renameTable(oldIdent: Identifier, newIdent: Identifier): Unit = {
    throw new UnsupportedOperationException("renaming table not supported")
  }

  private def isNotFound(exception: CosmosException) = exception.getStatusCode == 404

  private def alreadyExists(exception: CosmosException) = exception.getStatusCode == 409

  private def getContainerIdentifier(namespaceName: String, cosmosContainerProperties: CosmosContainerProperties): Identifier = {
    Identifier.of(Array(namespaceName), cosmosContainerProperties.getId)
  }

  private def checkNamespace(namespace: Array[String]): Unit = {
    if (namespace == null || namespace.length != 1) {
      throw new CosmosCatalogException(s"invalid namespace ${namespace.mkString("Array(", ", ", ")")}." +
        s" Cosmos DB already support single depth namespace.")
    }
  }

  private def toCosmosDatabaseName(namespace: String) : String = {
    namespace
  }

  private def toCosmosContainerName(tableIdent: String) : String = {
    tableIdent
  }

  def toTableConfig(options: CaseInsensitiveStringMap) : Map[String, String] = {
    options.asCaseSensitiveMap().asScala.toMap
  }

  object CosmosContainerProperties {
    private val partitionKeyPath = "partitionKeyPath"
    private val defaultPartitionKeyPath = "/id"
    def getPartitionKeyPath(properties: Map[String, String]) : String = {
      properties.getOrElse(partitionKeyPath, defaultPartitionKeyPath)
    }

    // TODO: add support for other container properties, indexing policy?
  }

  object CosmosThroughputProperties {
    private val manualThroughputFieldName = "manualThroughput"
    private val autoscaleMaxThroughputName = "autoscaleMaxThroughput"

    def tryGetThroughputProperties(properties: Map[String, String]): Option[ThroughputProperties] = {
      properties.get(manualThroughputFieldName).map(
        manualThroughput => ThroughputProperties.createManualThroughput(manualThroughput.toInt)
      ).orElse(
        properties.get(autoscaleMaxThroughputName).map(
          autoscaleMaxThroughput => ThroughputProperties.createAutoscaledThroughput(autoscaleMaxThroughput.toInt)
        )
      )
    }

    def toMap(throughputProperties: ThroughputProperties): Map[String, String] = {
      val props = new util.HashMap[String, String]()
      val manualThroughput = throughputProperties.getManualThroughput
      if (manualThroughput != null) {
        props.put(manualThroughputFieldName, manualThroughput.toString)
      } else {
        val autoscaleMaxThroughput = throughputProperties.getAutoscaleMaxThroughput
        props.put(autoscaleMaxThroughputName, autoscaleMaxThroughput.toString)
      }
      props.asScala.toMap
    }
  }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.catalog.{CosmosCatalogConflictException, CosmosCatalogException, CosmosCatalogNotFoundException, CosmosThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.{NamespaceAlreadyExistsException, NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.connector.catalog.{CatalogPlugin, Identifier, NamespaceChange, Table, TableCatalog, TableChange}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.streaming.HDFSMetadataLog
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

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
class CosmosCatalogBase
    extends CatalogPlugin
        with TableCatalog
        with BasicLoggingTrait {

    private lazy val sparkSession = SparkSession.active
    private lazy val sparkEnvironmentInfo = CosmosClientConfiguration.getSparkEnvironmentInfo(SparkSession.getActiveSession)

    // mutable but only expected to be changed from within initialize method
    private var catalogName: String = _
    //private var client: CosmosAsyncClient = _
    private var config: Map[String, String] = _
    private var readConfig: CosmosReadConfig = _
    private var tableOptions: Map[String, String] = _
    private var viewRepository: Option[HDFSMetadataLog[String]] = None

    /**
     * Called to initialize configuration.
     * <br/>
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
     * <br/>
     * If an object such as a table, view, or function exists, its parent namespaces must also exist
     * and must be returned by this discovery method. For example, if table a.t exists, this method
     * must return ["a"] in the result array.
     *
     * @return an array of multi-part namespace names.
     */
    def listNamespacesBase(): Array[Array[String]] = {
        logDebug("catalog:listNamespaces")

        TransientErrorsRetryPolicy.executeWithRetry(() => listNamespacesImpl())
    }

    private[this] def listNamespacesImpl(): Array[Array[String]] = {
        logDebug("catalog:listNamespaces")

        Loan(
            List[Option[CosmosClientCacheItem]](
                Some(CosmosClientCache(
                    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                    None,
                    s"CosmosCatalog(name $catalogName).listNamespaces"
                ))
            ))
            .to(cosmosClientCacheItems => {
                cosmosClientCacheItems(0)
                    .get
                    .sparkCatalogClient
                    .readAllDatabases()
                    .map(Array(_))
                    .collectSeq()
                    .block()
                    .toArray
            })
    }

    /**
     * List namespaces in a namespace.
     * <br/>
     * Cosmos supports only single depth database. Hence we always return an empty list of namespaces.
     * or throw if the root namespace doesn't exist
     */
    @throws(classOf[NoSuchNamespaceException])
    def listNamespacesBase(namespace: Array[String]): Array[Array[String]] = {
        loadNamespaceMetadataBase(namespace) // throws NoSuchNamespaceException if namespace doesn't exist
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
    def loadNamespaceMetadataBase(namespace: Array[String]): util.Map[String, String] = {

        TransientErrorsRetryPolicy.executeWithRetry(() => loadNamespaceMetadataImpl(namespace))
    }

    private[this] def loadNamespaceMetadataImpl(
                                                   namespace: Array[String]): util.Map[String, String] = {

        checkNamespace(namespace)

        Loan(
            List[Option[CosmosClientCacheItem]](
                Some(CosmosClientCache(
                    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                    None,
                    s"CosmosCatalog(name $catalogName).loadNamespaceMetadata([${namespace.mkString(", ")}])"
                ))
            ))
            .to(clientCacheItems => {
                try {
                    clientCacheItems(0)
                        .get
                        .sparkCatalogClient
                        .readDatabaseThroughput(toCosmosDatabaseName(namespace.head))
                        .block()
                        .asJava
                } catch {
                    case _: CosmosCatalogNotFoundException =>
                        throw new NoSuchNamespaceException(namespace)
                }
            })
    }

    @throws(classOf[NamespaceAlreadyExistsException])
    def createNamespaceBase(namespace: Array[String],
                            metadata: util.Map[String, String]): Unit = {
        TransientErrorsRetryPolicy.executeWithRetry(() => createNamespaceImpl(namespace, metadata))
    }

    @throws(classOf[NamespaceAlreadyExistsException])
    private[this] def createNamespaceImpl(namespace: Array[String],
                                          metadata: util.Map[String, String]): Unit = {
        checkNamespace(namespace)
        val databaseName = toCosmosDatabaseName(namespace.head)

        Loan(
            List[Option[CosmosClientCacheItem]](
                Some(CosmosClientCache(
                    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                    None,
                    s"CosmosCatalog(name $catalogName).createNamespace([${namespace.mkString(", ")}])"
                ))
            ))
            .to(cosmosClientCacheItems => {
                try {
                    cosmosClientCacheItems(0)
                        .get
                        .sparkCatalogClient
                        .createDatabase(databaseName, metadata.asScala.toMap)
                        .block()
                } catch {
                    case _: CosmosCatalogConflictException =>
                        throw new NamespaceAlreadyExistsException(namespace)
                }
            })
    }

    @throws(classOf[UnsupportedOperationException])
    def alterNamespaceBase(namespace: Array[String],
                           changes: Seq[NamespaceChange]): Unit = {
      checkNamespace(namespace)

      if (changes.size > 0) {
        val invalidChangesCount = changes
          .count(change => !CosmosThroughputProperties.isThroughputProperty(change))
        if (invalidChangesCount > 0) {
          throw new UnsupportedOperationException("ALTER NAMESPACE contains unsupported changes.")
        }

        val finalThroughputProperty = changes.last.asInstanceOf[NamespaceChange.SetProperty]

        val databaseName = toCosmosDatabaseName(namespace.head)

        alterNamespaceImpl(databaseName, finalThroughputProperty)
      }
    }

  //scalastyle:off method.length
  private def alterNamespaceImpl(databaseName: String, finalThroughputProperty: NamespaceChange.SetProperty): Unit = {
    logInfo(s"alterNamespace DB:$databaseName")

    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
          None,
          s"CosmosCatalog(name $catalogName).alterNamespace($databaseName)"
        ))
      ))
      .to(cosmosClientCacheItems => {
        cosmosClientCacheItems(0).get
          .sparkCatalogClient
          .alterDatabase(databaseName, finalThroughputProperty)
          .block()
      })
  }
  //scalastyle:on method.length

    /**
     * Drop a namespace from the catalog, recursively dropping all objects within the namespace.
     *
     * @param namespace - a multi-part namespace
     * @return true if the namespace was dropped
     */
    @throws(classOf[NoSuchNamespaceException])
    def dropNamespaceBase(namespace: Array[String]): Boolean = {
        TransientErrorsRetryPolicy.executeWithRetry(() => dropNamespaceImpl(namespace))
    }

    @throws(classOf[NoSuchNamespaceException])
    private[this] def dropNamespaceImpl(namespace: Array[String]): Boolean = {
        checkNamespace(namespace)
        try {
            Loan(
                List[Option[CosmosClientCacheItem]](
                    Some(CosmosClientCache(
                        CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                        None,
                        s"CosmosCatalog(name $catalogName).dropNamespace([${namespace.mkString(", ")}])"
                    ))
                ))
                .to(cosmosClientCacheItems => {
                    cosmosClientCacheItems(0)
                        .get
                        .sparkCatalogClient
                        .deleteDatabase(toCosmosDatabaseName(namespace.head))
                        .block()
                })
            true
        } catch {
            case _: CosmosCatalogNotFoundException =>
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
                Loan(
                    List[Option[CosmosClientCacheItem]](
                        Some(CosmosClientCache(
                            CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                            None,
                            s"CosmosCatalog(name $catalogName).listTables([${namespace.mkString(", ")}])"
                        ))
                    ))
                    .to(cosmosClientCacheItems => {
                        cosmosClientCacheItems(0).get
                            .sparkCatalogClient
                            .readAllContainers(databaseName)
                            .map(containerId => getContainerIdentifier(namespace.head, containerId))
                            .collectSeq()
                            .block()
                            .toList
                    })

            val tableIdentifiers = this.tryGetViewDefinitions(databaseName) match {
                case Some(viewDefinitions) =>
                    cosmosTables ++ viewDefinitions.map(viewDef => getContainerIdentifier(namespace.head, viewDef)).toIterable
                case None => cosmosTables
            }

            tableIdentifiers.toArray
        } catch {
            case _: CosmosCatalogNotFoundException =>
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
            case Some(tableProperties) =>
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
        checkNamespace(ident.namespace())

      if (changes.size > 0) {
        val invalidChangesCount = changes
          .count(change => !CosmosThroughputProperties.isThroughputProperty(change))
        if (invalidChangesCount > 0) {
          throw new UnsupportedOperationException("ALTER TABLE contains unsupported changes.")
        }

        val finalThroughputProperty = changes.last.asInstanceOf[TableChange.SetProperty]

        val tableBeforeModification = loadTableImpl(ident)
        if (!tableBeforeModification.isInstanceOf[ItemsTable]) {
          throw new UnsupportedOperationException("ALTER TABLE cannot be applied to Cosmos views.")
        }

        val databaseName = toCosmosDatabaseName(ident.namespace().head)
        val containerName = toCosmosContainerName(ident.name())

        alterPhysicalTable(databaseName, containerName, finalThroughputProperty)
      }

      loadTableImpl(ident)
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

        Loan(
            List[Option[CosmosClientCacheItem]](
                Some(CosmosClientCache(
                    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                    None,
                    s"CosmosCatalog(name $catalogName).createPhysicalTable($databaseName, $containerName)"
                ))
            ))
            .to(cosmosClientCacheItems => {
                cosmosClientCacheItems(0).get
                    .sparkCatalogClient
                    .createContainer(databaseName, containerName, containerProperties)
                    .block()
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

  //scalastyle:off method.length
  private def alterPhysicalTable(databaseName: String,
                                 containerName: String,
                                 finalThroughputProperty: TableChange.SetProperty): Unit = {
    logInfo(s"alterPhysicalTable DB:$databaseName, Container: $containerName")

    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
          None,
          s"CosmosCatalog(name $catalogName).alterPhysicalTable($databaseName, $containerName)"
        ))
      ))
      .to(cosmosClientCacheItems => {
        cosmosClientCacheItems(0).get
          .sparkCatalogClient
          .alterContainer(databaseName, containerName, finalThroughputProperty)
          .block()
      })
  }
  //scalastyle:on method.length

    private def deletePhysicalTable(databaseName: String, containerName: String): Boolean = {
        try {
            Loan(
                List[Option[CosmosClientCacheItem]](
                    Some(CosmosClientCache(
                        CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                        None,
                        s"CosmosCatalog(name $catalogName).deletePhysicalTable($databaseName, $containerName)"
                    ))
                ))
                .to (cosmosClientCacheItems =>
                    cosmosClientCacheItems(0).get
                        .sparkCatalogClient
                        .deleteContainer(databaseName, containerName))
                .block()
            true
        } catch {
            case _: CosmosCatalogNotFoundException => false
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
    ): Option[util.HashMap[String, String]] = {
        Loan(
            List[Option[CosmosClientCacheItem]](
                Some(CosmosClientCache(
                    CosmosClientConfiguration(config, readConfig.forceEventualConsistency, sparkEnvironmentInfo),
                    None,
                    s"CosmosCatalog(name $catalogName).tryGetContainerMetadata($databaseName, $containerName)"
                ))
            ))
            .to(cosmosClientCacheItems => {
                cosmosClientCacheItems(0)
                    .get
                    .sparkCatalogClient
                    .readContainerMetadata(databaseName, containerName)
                    .block()
            })
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

    private def getContainerIdentifier(
                                          namespaceName: String,
                                          containerId: String): Identifier = {
        Identifier.of(Array(namespaceName), containerId)
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


    private def redactAuthInfo(cfg: Map[String, String]): Map[String, String] = {
        cfg.filter((kvp) => !CosmosConfigNames.AccountEndpoint.equalsIgnoreCase(kvp._1) &&
            !CosmosConfigNames.AccountKey.equalsIgnoreCase(kvp._1) &&
            !kvp._1.toLowerCase.contains(CosmosConfigNames.AccountEndpoint.toLowerCase()) &&
            !kvp._1.toLowerCase.contains(CosmosConfigNames.AccountKey.toLowerCase())
        )
    }
}
// scalastyle:on multiple.string.literals
// scalastyle:on number.of.methods
// scalastyle:on file.size.limit

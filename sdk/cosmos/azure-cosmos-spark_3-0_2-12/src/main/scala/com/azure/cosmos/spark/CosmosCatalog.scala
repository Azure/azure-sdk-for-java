// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.apache.spark.sql.catalog.Catalog
import org.apache.spark.sql.catalyst.catalog.CatalogDatabase
import org.apache.spark.sql.connector.catalog.CatalogPlugin
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// CosmosCatalog provides a meta data store for Cosmos database, container control plane
// This will be required for hive integration
// relevant interfaces to implement:
//  - SupportsNamespaces (Cosmos Database and Cosmos Container can be modeled as namespace)
//  - SupportsCatalogOptions // TODO moderakh
//  - CatalogPlugin - A marker interface to provide a catalog implementation for Spark.
//      Implementations can provide catalog functions by implementing additional interfaces
//      for tables, views, and functions.
//  - TableCatalog Catalog methods for working with Tables.

abstract class CosmosCatalog extends CatalogPlugin {
  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = ???


  /**
    * Catalog implementations are registered to a name by adding a configuration option to Spark:
    * spark.sql.catalog.catalog-name=com.example.YourCatalogClass.
    * All configuration properties in the Spark configuration that share the catalog name prefix,
    * spark.sql.catalog.catalog-name.(key)=(value) will be passed in the case insensitive
    * string map of options in initialization with the prefix removed.
    * name, is also passed and is the catalog's name; in this case, "catalog-name".
    * @return catalog name
    */
  override def name(): String = "cosmos"
}

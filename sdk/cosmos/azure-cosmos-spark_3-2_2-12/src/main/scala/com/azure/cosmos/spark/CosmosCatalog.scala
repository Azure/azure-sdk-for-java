// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import java.util
// scalastyle:off underscore.import
// scalastyle:on underscore.import
import org.apache.spark.sql.catalyst.analysis.{NamespaceAlreadyExistsException, NoSuchNamespaceException}
import org.apache.spark.sql.connector.catalog.{NamespaceChange, SupportsNamespaces}

// scalastyle:off underscore.import

class CosmosCatalog
    extends CosmosCatalogBase
        with SupportsNamespaces {

    override def listNamespaces(): Array[Array[String]] = {
        super.listNamespaces()
    }
    @throws(classOf[NoSuchNamespaceException])
    override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
        super.listNamespaces(namespace)
    }

    @throws(classOf[NoSuchNamespaceException])
    override def loadNamespaceMetadata(namespace: Array[String]): util.Map[String, String] = {
        super.loadNamespaceMetadata(namespace)
    }

    @throws(classOf[NamespaceAlreadyExistsException])
    override def createNamespace(namespace: Array[String],
                                 metadata: util.Map[String, String]): Unit = {
        super.createNamespace(namespace, metadata)
    }

    @throws(classOf[UnsupportedOperationException])
    override def alterNamespace(namespace: Array[String],
                                changes: NamespaceChange*): Unit = {
        super.alterNamespace(namespace, changes)
    }

    @throws(classOf[NoSuchNamespaceException])
    override def dropNamespace(namespace: Array[String]): Boolean = {
        super.dropNamespace(namespace)
    }

}
// scalastyle:on multiple.string.literals
// scalastyle:on number.of.methods
// scalastyle:on file.size.limit

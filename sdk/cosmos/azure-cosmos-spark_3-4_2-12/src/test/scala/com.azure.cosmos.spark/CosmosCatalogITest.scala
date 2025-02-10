// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.NonEmptyNamespaceException

class CosmosCatalogITest
    extends CosmosCatalogITestBase {

    //scalastyle:off magic.number

    // TODO: spark on windows has issue with this test.
    // java.lang.RuntimeException: java.io.IOException: (null) entry in command string: null chmod 0733 D:\tmp\hive;
    // once we move Linux CI re-enable the test:
    it can "drop an empty database" in {
        assume(!Platform.isWindows)

        for (cascade <- Array(true, false)) {
            val databaseName = getAutoCleanableDatabaseName
            spark.catalog.databaseExists(databaseName) shouldEqual false

            createDatabase(spark, databaseName)
            databaseExists(databaseName) shouldEqual true

            dropDatabase(spark, databaseName, cascade)
            spark.catalog.databaseExists(databaseName) shouldEqual false
        }
    }

    it can "drop an non-empty database with cascade true" in {
        assume(!Platform.isWindows)

        val databaseName = getAutoCleanableDatabaseName
        spark.catalog.databaseExists(databaseName) shouldEqual false

        createDatabase(spark, databaseName)
        databaseExists(databaseName) shouldEqual true

        val containerName = RandomStringUtils.randomAlphabetic(5)
        spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

        dropDatabase(spark, databaseName, true)
        spark.catalog.databaseExists(databaseName) shouldEqual false
    }

    "drop an non-empty database with cascade false" should "throw NonEmptyNamespaceException" in {
        assume(!Platform.isWindows)

        try {
            val databaseName = getAutoCleanableDatabaseName
            spark.catalog.databaseExists(databaseName) shouldEqual false

            createDatabase(spark, databaseName)
            databaseExists(databaseName) shouldEqual true

            val containerName = RandomStringUtils.randomAlphabetic(5)
            spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

            dropDatabase(spark, databaseName, false)
            fail("Expected NonEmptyNamespaceException is not thrown")
        }
        catch {
            case expectedError: NonEmptyNamespaceException => {
                logInfo(s"Expected NonEmptyNamespaceException: $expectedError")
                succeed
            }
        }
    }

    it can "list all databases" in {
        val databaseName1 = getAutoCleanableDatabaseName
        val databaseName2 = getAutoCleanableDatabaseName

        // creating those databases ahead of time
        cosmosClient.createDatabase(databaseName1).block()
        cosmosClient.createDatabase(databaseName2).block()

        val databases  = spark.sql("SHOW DATABASES IN testCatalog").collect()
        databases.size should be >= 2
        //validate databases has the above database name1
        databases
         .filter(
             row => row.getAs[String]("namespace").equals(databaseName1)
              || row.getAs[String]("namespace").equals(databaseName2)) should have size 2
    }

    private def dropDatabase(spark: SparkSession, databaseName: String, cascade: Boolean) = {
        if (cascade) {
            spark.sql(s"DROP DATABASE testCatalog.$databaseName CASCADE;")
        } else {
            spark.sql(s"DROP DATABASE testCatalog.$databaseName;")
        }
    }
}

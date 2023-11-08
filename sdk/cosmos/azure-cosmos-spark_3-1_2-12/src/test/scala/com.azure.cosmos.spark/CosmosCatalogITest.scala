// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession

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

            dropDatabase(spark, databaseName)
            spark.catalog.databaseExists(databaseName) shouldEqual false
        }
    }

    it can "drop an non-empty database" in {
        assume(!Platform.isWindows)

        val databaseName = getAutoCleanableDatabaseName
        spark.catalog.databaseExists(databaseName) shouldEqual false

        createDatabase(spark, databaseName)
        databaseExists(databaseName) shouldEqual true

        val containerName = RandomStringUtils.randomAlphabetic(5)
        spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

        dropDatabase(spark, databaseName)
        spark.catalog.databaseExists(databaseName) shouldEqual false
    }

    private def dropDatabase(spark: SparkSession, databaseName: String) = {
        spark.sql(s"DROP DATABASE testCatalog.$databaseName;")
    }
}

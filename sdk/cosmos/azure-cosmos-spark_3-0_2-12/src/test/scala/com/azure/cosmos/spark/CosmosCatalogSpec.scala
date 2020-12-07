// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession
import org.assertj.core.api.Assertions.assertThat
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// TODO  moderakh do proper clean up for spark session, client, etc
// TODO: moderakh should we tag tests at the test class level or test method level?
// TODO: moderakh do we need to recreate spark for each test or should we use a common instance?
// TODO: moderakh rely on the shared database/container for the tests to avoid creating many
// TODO: moderakh develop the proper pattern for proper resource cleanup after test
//       we need to clean up databases after creation.

class CosmosCatalogSpec extends IntegrationSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  it should "create a database with shared throughput" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    spark.conf.set(s"spark.sql.catalog.testCatalog", "com.azure.cosmos.spark.CosmosCatalog")
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountKey", cosmosMasterKey)

    val databaseName = RandomStringUtils.randomAlphabetic(5)

    spark.sql(s"CREATE DATABASE testCatalog.${databaseName} WITH DBPROPERTIES ('manualThroughput' = '1000');")

    client.getDatabase(databaseName).read().block()
    val throughput = client.getDatabase(databaseName).readThroughput().block()
    assertThat(throughput.getProperties.getManualThroughput).isEqualTo(1000)

    client.close()
    spark.close()
  }

  it should "drops a database" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    spark.conf.set(s"spark.sql.catalog.testCatalog", "com.azure.cosmos.spark.CosmosCatalog")
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountKey", cosmosMasterKey)

    val databaseName = RandomStringUtils.randomAlphabetic(6)
    assertThat(databaseExists(client, databaseName)).isEqualTo(false)

    createDatabase(spark, databaseName)
    assertThat(databaseExists(client, databaseName)).isEqualTo(true)

    dropDatabase(spark, databaseName)
    assertThat(spark.catalog.databaseExists(databaseName)).isEqualTo(false)

    client.close()
    spark.close()
  }

  it can "create a table with defaults" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .enableHiveSupport()
      .getOrCreate()

    spark.conf.set(s"spark.sql.catalog.cosmoscatalog", "com.azure.cosmos.spark.CosmosCatalog")
    spark.conf.set(s"spark.sql.catalog.cosmoscatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
    spark.conf.set(s"spark.sql.catalog.cosmoscatalog.spark.cosmos.accountKey", cosmosMasterKey)

    val databaseName = RandomStringUtils.randomAlphabetic(5).toLowerCase
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE cosmoscatalog.${databaseName};")
    spark.sql(s"CREATE TABLE cosmoscatalog.${databaseName}.${containerName} (word STRING, number INT) using cosmos.items;")

    val containerProperties = client.getDatabase(databaseName).getContainer(containerName).read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput

    val throughput = client.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    client.close()
    spark.close()
  }

  it should "create a table with customized properties" taggedAs (RequiresCosmosEndpoint) in {
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    val client = new CosmosClientBuilder()
      .endpoint(cosmosEndpoint)
      .key(cosmosMasterKey)
      .buildAsyncClient()

    val spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .enableHiveSupport()
      .getOrCreate()

    spark.conf.set(s"spark.sql.catalog.cosmoscatalog", "com.azure.cosmos.spark.CosmosCatalog")
    spark.conf.set(s"spark.sql.catalog.cosmoscatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
    spark.conf.set(s"spark.sql.catalog.cosmoscatalog.spark.cosmos.accountKey", cosmosMasterKey)

    val databaseName = RandomStringUtils.randomAlphabetic(5).toLowerCase
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE cosmoscatalog.${databaseName};")
    spark.sql(s"CREATE TABLE cosmoscatalog.${databaseName}.${containerName} (word STRING, number INT) using cosmos.items " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', manualThroughput = '1100')")

    val containerProperties = client.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))

    // validate throughput
    val throughput = client.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 1100

    client.close()
    spark.close()
  }

  private def createDatabase(spark: SparkSession, databaseName: String) = {
    spark.sql(s"CREATE DATABASE testCatalog.${databaseName};")
  }

  private def dropDatabase(spark: SparkSession, databaseName: String) = {
    spark.sql(s"DROP DATABASE testCatalog.${databaseName};")
  }

  private def databaseExists(client: CosmosAsyncClient, databaseName: String) = {
    try {
      client.getDatabase(databaseName).read().block()
      true
    } catch {
      case e: CosmosException if e.getStatusCode == 404 => false
    }
  }
  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

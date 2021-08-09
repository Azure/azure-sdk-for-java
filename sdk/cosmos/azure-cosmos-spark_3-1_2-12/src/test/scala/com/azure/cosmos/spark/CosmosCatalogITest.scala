// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{TestConfigurations, Utils}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession

import java.util.UUID
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosCatalogITest extends IntegrationSpec with CosmosClient with BasicLoggingTrait {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number


  var spark : SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val cosmosEndpoint = TestConfigurations.HOST
    val cosmosMasterKey = TestConfigurations.MASTER_KEY

    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .enableHiveSupport()
      .getOrCreate()

    spark.conf.set(s"spark.sql.catalog.testCatalog", "com.azure.cosmos.spark.CosmosCatalog")
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
    spark.conf.set(s"spark.sql.catalog.testCatalog.spark.cosmos.accountKey", cosmosMasterKey)
    spark.conf.set(
      "spark.sql.catalog.testCatalog.spark.cosmos.views.repositoryPath",
      s"/viewRepository/${UUID.randomUUID().toString}")
    spark.conf.set(
      "spark.sql.catalog.testCatalog.spark.cosmos.read.partitioning.strategy",
      "Restrictive")
  }

  override def afterAll(): Unit = {
    try spark.close()
    finally super.afterAll()
  }

  it can "create a database with shared throughput" in {
    val databaseName = getAutoCleanableDatabaseName()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName WITH DBPROPERTIES ('manualThroughput' = '1000');")

    cosmosClient.getDatabase(databaseName).read().block()
    val throughput = cosmosClient.getDatabase(databaseName).readThroughput().block()

    throughput.getProperties.getManualThroughput shouldEqual 1000
  }

  // TODO: moderakh spark on windows has issue with this test.
  // java.lang.RuntimeException: java.io.IOException: (null) entry in command string: null chmod 0733 D:\tmp\hive;
  // once we move Linux CI re-enable the test:
  it can "drops a database" in {
    assume(!Platform.isWindows())

    val databaseName = getAutoCleanableDatabaseName()
    spark.catalog.databaseExists(databaseName) shouldEqual false

    createDatabase(spark, databaseName)
    databaseExists(databaseName) shouldEqual true

    dropDatabase(spark, databaseName)
    spark.catalog.databaseExists(databaseName) shouldEqual false
  }

  it can "create a table with defaults" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    cleanupDatabaseLater(databaseName)

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp;")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400
  }

  it can "create a table with customized properties" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', manualThroughput = '1100')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))
    // scalastyle:off null
    containerProperties.getDefaultTimeToLiveInSeconds shouldEqual null
    // scalastyle:on null

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 1100
  }

  it can "create a table with well known indexing policy 'AllProperties'" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', manualThroughput = '1100', indexingPolicy = 'AllProperties')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))
    containerProperties
      .getIndexingPolicy
      .getIncludedPaths
      .asScala
      .map(p => p.getPath)
      .toArray should equal(Array("/*"))
    containerProperties
      .getIndexingPolicy
      .getExcludedPaths
      .asScala
      .map(p => p.getPath)
      .toArray should equal(Array(raw"""/"_etag"/?"""))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 1100
  }

  it can "create a table with well known indexing policy 'OnlySystemProperties'" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', manualThroughput = '1100', indexingPolicy = 'ONLYSystemproperties')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.toArray should equal(Array("/mypk"))
    containerProperties
      .getIndexingPolicy
      .getIncludedPaths
      .asScala.map(p => p.getPath)
      .toArray.length shouldEqual 0
    containerProperties
      .getIndexingPolicy
      .getExcludedPaths
      .asScala
      .map(p => p.getPath)
      .toArray should equal(Array("/*", raw"""/"_etag"/?"""))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 1100
  }

  it can "create a table with custom indexing policy" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    val indexPolicyJson = raw"""{"indexingMode":"consistent","automatic":true,"includedPaths":""" +
      raw"""[{"path":"\/helloWorld\/?"},{"path":"\/mypk\/?"}],"excludedPaths":[{"path":"\/*"}]}"""

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', manualThroughput = '1100', indexingPolicy = '$indexPolicyJson')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))
    containerProperties
      .getIndexingPolicy
      .getIncludedPaths
      .asScala
      .map(p => p.getPath)
      .toArray should equal(Array("/helloWorld/?", "/mypk/?"))
    containerProperties
      .getIndexingPolicy
      .getExcludedPaths
      .asScala
      .map(p => p.getPath)
      .toArray should equal(Array("/*", raw"""/"_etag"/?"""))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 1100
  }

  it can "create a table with TTL -1" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', defaultTtlInSeconds = '-1')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))
    containerProperties.getDefaultTimeToLiveInSeconds shouldEqual -1
  }

  it can "create a table with positive TTL" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp " +
      s"TBLPROPERTIES(partitionKeyPath = '/mypk', defaultTtlInSeconds = '5')")

    val containerProperties = cosmosClient.getDatabase(databaseName).getContainer(containerName).read().block().getProperties
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/mypk"))
    containerProperties.getDefaultTimeToLiveInSeconds shouldEqual 5
  }

  it can "select from a catalog table with default TBLPROPERTIES" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    cleanupDatabaseLater(databaseName)

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName (word STRING, number INT) using cosmos.oltp;")

    val container = cosmosClient.getDatabase(databaseName).getContainer(containerName)
    val containerProperties = container.read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's mouse")
      objectNode.put("type", "mouse")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    val dfWithInference = spark.sql(s"SELECT * FROM testCatalog.$databaseName.$containerName")
    val rowsArrayUnfiltered= dfWithInference.collect()
    rowsArrayUnfiltered should have size 2
    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'mouse'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's mouse"
    rowWithInference.getAs[String]("type") shouldEqual "mouse"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false
  }

  it can "select from a catalog Cosmos view" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    val viewName = containerName + "view" + RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

    val container = cosmosClient.getDatabase(databaseName).getContainer(containerName)
    val containerProperties = container.read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's mouse")
      objectNode.put("type", "mouse")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    spark.sql(
      s"CREATE TABLE testCatalog.$databaseName.$viewName using cosmos.oltp " +
        s"TBLPROPERTIES(isCosmosView = 'True') " +
        s"OPTIONS (" +
        s"spark.cosmos.database = '$databaseName', " +
        s"spark.cosmos.container = '$containerName', " +
        "spark.cosmos.read.inferSchema.enabled = 'True', " +
        "spark.cosmos.read.inferSchema.includeSystemProperties = 'True', " +
        "spark.cosmos.read.partitioning.strategy = 'Restrictive');")
    val tables = spark.sql(s"SHOW TABLES in testCatalog.$databaseName;")

    tables.collect() should have size 2

    tables
      .where(s"tableName = '$viewName' and namespace = '$databaseName'")
      .collect() should have size 1

    tables
      .where(s"tableName = '$containerName' and namespace = '$databaseName'")
      .collect() should have size 1

    val dfWithInference = spark.sql(s"SELECT * FROM testCatalog.$databaseName.$viewName")
    val rowsArrayUnfiltered= dfWithInference.collect()
    rowsArrayUnfiltered should have size 2

    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'mouse'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's mouse"
    rowWithInference.getAs[String]("type") shouldEqual "mouse"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe true
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe true
  }

  it can "manage Cosmos view metadata in the catalog" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    val viewNameRaw = containerName +
      "view" +
      RandomStringUtils.randomAlphabetic(6).toLowerCase +
      System.currentTimeMillis()
    val viewNameWithSchemaInference = containerName +
      "view" +
      RandomStringUtils.randomAlphabetic(6).toLowerCase +
      System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

    val container = cosmosClient.getDatabase(databaseName).getContainer(containerName)
    val containerProperties = container.read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's snake")
      objectNode.put("type", "snake")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    spark.sql(
      s"CREATE TABLE testCatalog.$databaseName.$viewNameRaw using cosmos.oltp " +
        s"TBLPROPERTIES(isCosmosView = 'True') " +
        s"OPTIONS (" +
        s"spark.cosmos.database = '$databaseName', " +
        s"spark.cosmos.container = '$containerName', " +
        "spark.cosmos.read.inferSchema.enabled = 'False', " +
        "spark.cosmos.read.partitioning.strategy = 'Restrictive');")

    var tables = spark.sql(s"SHOW TABLES in testCatalog.$databaseName;")
    tables.collect() should have size 2

    spark.sql(
      s"CREATE TABLE testCatalog.$databaseName.$viewNameWithSchemaInference using cosmos.oltp " +
        s"TBLPROPERTIES(isCosmosView = 'True') " +
        s"OPTIONS (" +
        s"spark.cosmos.database = '$databaseName', " +
        s"spark.cosmos.container = '$containerName', " +
        "spark.cosmos.read.inferSchema.enabled = 'True', " +
        "spark.cosmos.read.inferSchema.includeSystemProperties = 'False', " +
        "spark.cosmos.read.partitioning.strategy = 'Restrictive');")

    tables = spark.sql(s"SHOW TABLES in testCatalog.$databaseName;")
    tables.collect() should have size 3

    tables
      .where(s"tableName = '$containerName' and namespace = '$databaseName'")
      .collect() should have size 1
    tables
      .where(s"tableName = '$viewNameRaw' and namespace = '$databaseName'")
      .collect() should have size 1
    tables
      .where(s"tableName = '$viewNameWithSchemaInference' and namespace = '$databaseName'")
      .collect() should have size 1

    val dfRaw = spark.sql(s"SELECT * FROM testCatalog.$databaseName.$viewNameRaw")
    val rowsArrayUnfilteredRaw= dfRaw.collect()
    rowsArrayUnfilteredRaw should have size 2

    val fieldNamesRaw = dfRaw.schema.fields.map(field => field.name)
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.IdAttributeName) shouldBe true
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.RawJsonBodyAttributeName) shouldBe true
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe true
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNamesRaw.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false

    val dfWithInference = spark.sql(s"SELECT * FROM testCatalog.$databaseName.$viewNameWithSchemaInference")
    val rowsArrayUnfiltered= dfWithInference.collect()
    rowsArrayUnfiltered should have size 2

    val rowsArrayWithInference = dfWithInference.where("isAlive = 'true' and type = 'snake'").collect()
    rowsArrayWithInference should have size 1

    val rowWithInference = rowsArrayWithInference(0)
    rowWithInference.getAs[String]("name") shouldEqual "Shrodigner's snake"
    rowWithInference.getAs[String]("type") shouldEqual "snake"
    rowWithInference.getAs[Integer]("age") shouldEqual 20
    rowWithInference.getAs[Boolean]("isAlive") shouldEqual true

    val fieldNames = rowWithInference.schema.fields.map(field => field.name)
    fieldNames.contains(CosmosTableSchemaInferrer.SelfAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.TimestampAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ResourceIdAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.ETagAttributeName) shouldBe false
    fieldNames.contains(CosmosTableSchemaInferrer.AttachmentsAttributeName) shouldBe false

    spark.sql(s"DROP TABLE testCatalog.$databaseName.$viewNameRaw;")
    tables = spark.sql(s"SHOW TABLES in testCatalog.$databaseName;")
    tables.collect() should have size 2

    spark.sql(s"DROP TABLE testCatalog.$databaseName.$viewNameWithSchemaInference;")
    tables = spark.sql(s"SHOW TABLES in testCatalog.$databaseName;")
    tables.collect() should have size 1
  }

  "creating a view without specifying isCosmosView table property" should "throw IllegalArgumentException" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    val viewName = containerName +
      "view" +
      RandomStringUtils.randomAlphabetic(6).toLowerCase +
      System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

    val container = cosmosClient.getDatabase(databaseName).getContainer(containerName)
    val containerProperties = container.read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's snake")
      objectNode.put("type", "snake")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    try {
      spark.sql(
        s"CREATE TABLE testCatalog.$databaseName.$viewName using cosmos.oltp " +
          s"TBLPROPERTIES(isCosmosViewWithTypo = 'True') " +
          s"OPTIONS (" +
          s"spark.cosmos.database = '$databaseName', " +
          s"spark.cosmos.container = '$containerName', " +
          "spark.cosmos.read.inferSchema.enabled = 'False', " +
          "spark.cosmos.read.partitioning.strategy = 'Restrictive');")

      fail("Expected IllegalArgumentException not thrown")
    }
    catch {
      case expectedError: IllegalArgumentException => {
        logInfo(s"Expected IllegaleArgumentException: $expectedError")
        succeed
      }
    }
  }

  "creating a view with specifying isCosmosView==False table property" should "throw IllegalArgumentException" in {
    val databaseName = getAutoCleanableDatabaseName()
    val containerName = RandomStringUtils.randomAlphabetic(6).toLowerCase + System.currentTimeMillis()
    val viewName = containerName +
      "view" +
      RandomStringUtils.randomAlphabetic(6).toLowerCase +
      System.currentTimeMillis()

    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
    spark.sql(s"CREATE TABLE testCatalog.$databaseName.$containerName using cosmos.oltp;")

    val container = cosmosClient.getDatabase(databaseName).getContainer(containerName)
    val containerProperties = container.read().block().getProperties

    // verify default partition key path is used
    containerProperties.getPartitionKeyDefinition.getPaths.asScala.toArray should equal(Array("/id"))

    // validate throughput
    val throughput = cosmosClient.getDatabase(databaseName).getContainer(containerName).readThroughput().block().getProperties
    throughput.getManualThroughput shouldEqual 400

    for (state <- Array(true, false)) {
      val objectNode = Utils.getSimpleObjectMapper.createObjectNode()
      objectNode.put("name", "Shrodigner's snake")
      objectNode.put("type", "snake")
      objectNode.put("age", 20)
      objectNode.put("isAlive", state)
      objectNode.put("id", UUID.randomUUID().toString)
      container.createItem(objectNode).block()
    }

    try {
      spark.sql(
        s"CREATE TABLE testCatalog.$databaseName.$viewName using cosmos.oltp " +
          s"TBLPROPERTIES(isCosmosView = 'False') " +
          s"OPTIONS (" +
          s"spark.cosmos.database = '$databaseName', " +
          s"spark.cosmos.container = '$containerName', " +
          "spark.cosmos.read.inferSchema.enabled = 'False', " +
          "spark.cosmos.read.partitioning.strategy = 'Restrictive');")

      fail("Expected IllegalArgumentException not thrown")
    }
    catch {
      case expectedError: IllegalArgumentException => {
        logInfo(s"Expected IllegaleArgumentException: $expectedError")
        succeed
      }
    }
  }

  private def createDatabase(spark: SparkSession, databaseName: String) = {
    spark.sql(s"CREATE DATABASE testCatalog.$databaseName;")
  }

  private def dropDatabase(spark: SparkSession, databaseName: String) = {
    spark.sql(s"DROP DATABASE testCatalog.$databaseName;")
  }

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

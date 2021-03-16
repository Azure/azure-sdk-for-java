// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID
import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.{ChangeFeedPolicy, CosmosContainerProperties, CosmosItemRequestOptions, ThroughputProperties}
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import java.time.Duration
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter
// scalastyle:off underscore.import

// scalastyle:on underscore.import

// extending class will have a pre-created spark session
trait Spark extends BeforeAndAfterAll {
  this: Suite =>
  //scalastyle:off
  var spark : SparkSession = _

  def getSpark() = spark

  override def beforeAll(): Unit = {
    System.out.println("spark started!!!!!!")
    super.beforeAll()
    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    try {
      System.out.println("shutting down spark!!!!")
      spark.close()
    } // To be stackable, must call super.afterAll
    finally {
      super.afterAll()
    }
  }
}

// extending class will have a pre-created instance of CosmosClient
trait CosmosClient extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>
  //scalastyle:off
  var cosmosClient : CosmosAsyncClient = _

  private val databasesToCleanUp = new ListBuffer[String]()

  override def beforeAll(): Unit = {
    System.out.println("Cosmos Client started!!!!!!")
    super.beforeAll()
    cosmosClient = new CosmosClientBuilder()
      .endpoint(TestConfigurations.HOST)
      .key(TestConfigurations.MASTER_KEY)
      .buildAsyncClient()
  }

  override def afterEach(): Unit = {
    try {
      for (dbName <- databasesToCleanUp) {
        try {
          cosmosClient.getDatabase(dbName).delete().block()
        } catch {
          case e : Exception => None
        }
      }
    }
    finally super.afterEach()
  }

  override def afterAll(): Unit = {
    try {
      System.out.println("shutting down cosmos client!!!!!!")
      cosmosClient.close()
    } // To be stackable, must call super.afterAll
    finally {

      super.afterAll()
    }
  }

  def databaseExists(databaseName: String) = {
    try {
      cosmosClient.getDatabase(databaseName).read().block()
      true
    } catch {
      case e: CosmosException if e.getStatusCode == 404 => false
    }
  }

  def getAutoCleanableDatabaseName() : String = {
    val dbName = RandomStringUtils.randomAlphabetic(5)
    cleanupDatabaseLater(dbName)
    dbName
  }

  def cleanupDatabaseLater(databaseName: String) : Unit = {
    databasesToCleanUp.append(databaseName)
  }
}

trait CosmosDatabase extends CosmosClient {
  this: Suite =>
  //scalastyle:off

  val cosmosDatabase = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    super.beforeAll()
    cosmosClient.createDatabaseIfNotExists(cosmosDatabase).block()
  }

  override def afterAll(): Unit = {
    try cosmosClient.getDatabase(cosmosDatabase).delete().block() // To be stackable, must call super.afterAll
    finally super.afterAll()
  }
}

trait CosmosContainer extends CosmosDatabase {
  this: Suite =>
  //scalastyle:off

  var cosmosContainer = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    super.beforeAll()
    this.createContainerCore()
  }

  def reinitializeContainer(): Unit = {
    val containerIdSnapshot = this.cosmosContainer

    try {
      cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer).delete().block()
    }
    finally {
      this.cosmosContainer = UUID.randomUUID().toString
      this.createContainerCore()
    }
  }

  def createContainerCore(): Unit = {
    val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)
    cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainerIfNotExists(cosmosContainer, "/id", throughputProperties).block()
  }

  override def afterAll(): Unit = {
    try cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer).delete().block()
    finally super.afterAll() // To be stackable, must call super.afterAll
  }

  def queryItems(query: String): List[ObjectNode] = {
    cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      .queryItems(query, classOf[ObjectNode])
      .toIterable
      .asScala
      .toList
  }

  def readAllItems(): List[ObjectNode] = {
    cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      .queryItems("SELECT * FROM r", classOf[ObjectNode])
      .toIterable
      .asScala
      .toList
  }
}

trait CosmosContainerWithRetention extends CosmosContainer {
  this: Suite =>
  //scalastyle:off

  override def createContainerCore(): Unit = {
    val properties: CosmosContainerProperties =
      new CosmosContainerProperties(cosmosContainer, "/id")
    properties.setChangeFeedPolicy(
      ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(10)))

    val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)

    cosmosClient
      .getDatabase(cosmosDatabase)
      .createContainerIfNotExists(properties, throughputProperties).block()
  }
}

trait AutoCleanableCosmosContainer extends CosmosContainer with BeforeAndAfterEach {
  this: Suite =>

  override def afterEach(): Unit = {
    try super.afterEach() // To be stackable, must call super.afterAll
    finally {
      // wait for data to get replicated
      Thread.sleep(1000)
      val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)
      container.queryItems("SELECT * FROM r", classOf[ObjectNode]).flatMap(
        item => container.deleteItem(item, new CosmosItemRequestOptions())
      ).blockLast()
    }
  }
}

private object Defaults {
  val DefaultContainerThroughput = 20000
}

object Platform {
  def isWindows(): Boolean = {
    System.getProperty("os.name").toLowerCase.contains("win")
  }
}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.models.{ChangeFeedPolicy, CosmosBulkOperations, CosmosContainerProperties, CosmosItemOperation, PartitionKey, ThroughputProperties}
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomStringUtils
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import reactor.core.publisher.{EmitterProcessor, Sinks}
import reactor.core.scala.publisher.SMono.PimpJFlux

import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
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
  private val Suffix = "ITest"
  if (!this.getClass.getName.endsWith(Suffix)) {
    throw new RuntimeException(s"All test classes which have dependency" +
      s" on Cosmos DB Endpoint must have $Suffix suffix")
  }

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

  val partitionKeyPath = "/id"

  def getPartitionKeyValue(objectNode: ObjectNode) : Object = {
    // assumes partitionKeyPath being "/id" hence pkValue is always string
    objectNode.get("id").textValue()
  }

  def getId(objectNode: ObjectNode) : String = {
    objectNode.get("id").textValue()
  }

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
      .createContainerIfNotExists(cosmosContainer, partitionKeyPath, throughputProperties).block()
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
      new CosmosContainerProperties(cosmosContainer, partitionKeyPath)
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

    try {
      // wait for data to get replicated
      Thread.sleep(1000)
      System.out.println(s"cleaning the items in container ${cosmosContainer}")
      val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainer)

      try {
        val emitter: Sinks.Many[CosmosItemOperation] = Sinks.many().unicast().onBackpressureBuffer()

        val bulkDeleteFlux = container.executeBulkOperations(emitter.asFlux())

        val cnt = new AtomicInteger(0)
        container.queryItems("SELECT * FROM r", classOf[ObjectNode])
          .asScala
          .doOnNext(item => {
            val operation = CosmosBulkOperations.getDeleteItemOperation(getId(item), new PartitionKey(getPartitionKeyValue(item)))
            cnt.incrementAndGet()
            emitter.tryEmitNext(operation)

          }).doOnComplete(
          () => {
            emitter.tryEmitComplete()
          }).subscribe()

        bulkDeleteFlux.blockLast()

        System.out.println(s"Deleted ${cnt.get()} in container ${cosmosContainer}")
      } catch {
        case e: Exception => {
          System.err.println(s"${this.getClass.getName}#afterEach: failed:" + e.getMessage)
          throw e
        }
      }
    } finally {
      super.afterEach() // To be stackable, must call super.afterEach
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
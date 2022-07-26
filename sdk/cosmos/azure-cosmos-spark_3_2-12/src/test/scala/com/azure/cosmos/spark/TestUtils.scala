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
import reactor.core.publisher.Sinks
import reactor.core.scala.publisher.SMono.PimpJFlux

import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.concurrent.NotThreadSafe
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter
// scalastyle:off underscore.import
// scalastyle:on underscore.import

// extending class will have a pre-created spark session
@NotThreadSafe // marking this as not thread safe because we have to stop Spark Context in some unit tests
// there can only ever be one active Spark Context so running Spark tests in parallel could cause issues
trait Spark extends BeforeAndAfterAll {
  this: Suite =>
  //scalastyle:off
  var spark : SparkSession = _

  def getSpark: SparkSession = spark

  def resetSpark: SparkSession = {
    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    spark
  }

  override def beforeAll(): Unit = {
    System.out.println("spark started!!!!!!")
    super.beforeAll()
    resetSpark
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

trait SparkWithDropwizardAndSlf4jMetrics extends Spark {
  this: Suite =>
  //scalastyle:off

  override def resetSpark: SparkSession = {
    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config("spark.plugins", "com.azure.cosmos.spark.plugins.CosmosMetricsSparkPlugin")
      .getOrCreate()

    spark
  }
}

trait SparkWithJustDropwizardAndNoSlf4jMetrics extends Spark {
  this: Suite =>
  //scalastyle:off

  override def resetSpark: SparkSession = {
    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config("spark.plugins", "com.azure.cosmos.spark.plugins.CosmosMetricsSparkPlugin")
      .config("spark.cosmos.metrics.slf4j.enabled", "false")
      .getOrCreate()

    spark
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
          case _ : Exception =>
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

  def databaseExists(databaseName: String): Boolean = {
    try {
      cosmosClient.getDatabase(databaseName).read().block()
      true
    } catch {
      case e: CosmosException if e.getStatusCode == 404 => false
    }
  }

  def getAutoCleanableDatabaseName: String = {
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

  val cosmosDatabase: String = UUID.randomUUID().toString

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

  var cosmosContainer: String = UUID.randomUUID().toString

  val partitionKeyPath: String = "/id"

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
      System.out.println(s"cleaning the items in container $cosmosContainer")
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

        System.out.println(s"Deleted ${cnt.get()} in container $cosmosContainer")
      } catch {
        case e: Exception =>
          System.err.println(s"${this.getClass.getName}#afterEach: failed:" + e.getMessage)
          throw e
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
  def isWindows: Boolean = {
    System.getProperty("os.name").toLowerCase.contains("win")
  }

  // Indicates whether a test is capable of running when it attempts to access DirectByteBuffer via reflection.
  // Spark 3.1 was written in a way where it attempts to access DirectByteBuffer illegally via reflection in Java 16+.
  def canRunTestAccessingDirectByteBuffer: (Boolean, Any) = {
    val hasSparkVersion = util.Properties.propIsSet("cosmos-spark-version")
    val sparkVersion = util.Properties.propOrElse("cosmos-spark-version", "unknown")

    (!util.Properties.isJavaAtLeast("16") || (hasSparkVersion && !sparkVersion.equals("3.1")),
      s"Test was skipped as it will attempt to reflectively access DirectByteBuffer while using JVM version ${util.Properties.javaSpecVersion} and Spark version $sparkVersion. "
        + "These versions used together will result in an InaccessibleObjectException due to JVM changes on how internal APIs can be accessed by reflection,"
        + " and the Spark version, or unknown version, attempts to access DirectByteBuffer via reflection.")
  }
}
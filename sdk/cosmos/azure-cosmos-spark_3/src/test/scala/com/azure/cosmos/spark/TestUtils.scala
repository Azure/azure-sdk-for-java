// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{Configs, TestConfigurations}
import com.azure.cosmos.models.{ChangeFeedPolicy, CosmosBulkOperations, CosmosContainerProperties, CosmosItemOperation, PartitionKey, PartitionKeyBuilder, PartitionKeyDefinition, PartitionKeyDefinitionVersion, PartitionKind, ThroughputProperties}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.globalmentor.apache.hadoop.fs.{BareLocalFileSystem, NakedLocalFileSystem}
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import org.apache.commons.lang3.RandomStringUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import reactor.core.publisher.Sinks
import reactor.core.scala.publisher.SMono.PimpJFlux

import java.net.URI
import java.time.Duration
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.concurrent.NotThreadSafe
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// extending class will have a pre-created spark session
@NotThreadSafe // marking this as not thread safe because we have to stop Spark Context in some unit tests
// there can only ever be one active Spark Context so running Spark tests in parallel could cause issues
trait Spark extends BeforeAndAfterAll with BasicLoggingTrait {
  this: Suite =>
  //scalastyle:off
  var spark : SparkSession = _

  def getSpark: SparkSession = spark

  def resetSpark: SparkSession = {
    PartitionMetadataCache.clearCache()
    CosmosClientCache.clearCache()

    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .getOrCreate()

    LocalJavaFileSystem.applyToSparkSession(spark)

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

trait SparkWithMetrics extends Spark {
  this: Suite =>
  //scalastyle:off

  override def resetSpark: SparkSession = {
    PartitionMetadataCache.clearCache()
    CosmosClientCache.clearCache()

    val dummyAzMonConnectionString = "InstrumentationKey=12345678-1234-1234-1234-0123456789ab;" +
      "IngestionEndpoint=https://westus2-2.in.applicationinsights.azure.com/;" +
      "LiveEndpoint=https://westus2.livediagnostics.monitor.azure.com/;" +
      "ApplicationId=87654321-1234-1234-1234-0123456789cd"
    var azMonConnectionString = Option
      .apply(Configs.getAzureMonitorConnectionString)
      .getOrElse(dummyAzMonConnectionString)

    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      .config("spark.cosmos.diagnostics.azureMonitor.enabled", "true")
      .config("spark.cosmos.diagnostics.azureMonitor.metrics.intervalInSeconds", "10")
      .config("spark.cosmos.diagnostics.azureMonitor.connectionString", azMonConnectionString)
      .getOrCreate()

    LocalJavaFileSystem.applyToSparkSession(spark)

    spark
  }
}

trait SparkWithJustDropwizardAndNoSlf4jMetrics extends Spark {
  this: Suite =>
  //scalastyle:off

  override def resetSpark: SparkSession = {
    PartitionMetadataCache.clearCache()
    CosmosClientCache.clearCache()
    spark = SparkSession.builder()
      .appName("spark connector sample")
      .master("local")
      // .config("spark.plugins", "com.azure.cosmos.spark.plugins.CosmosMetricsSparkPlugin")
      .config("spark.cosmos.metrics.slf4j.enabled", "false")
      .getOrCreate()

    LocalJavaFileSystem.applyToSparkSession(spark)

    spark
  }
}

trait MetricAssertions extends BasicLoggingTrait with Matchers {
  def assertMetrics(meterRegistry: CompositeMeterRegistry, prefix: String, expectedToFind: Boolean): Unit = {
    meterRegistry.getRegistries.size() > 0 shouldEqual true
    val firstRegistry: MeterRegistry = meterRegistry.getRegistries.toArray()(0).asInstanceOf[MeterRegistry]
    firstRegistry.isClosed shouldEqual false
    val meters = firstRegistry.getMeters.asScala

    if (expectedToFind) {
      if (meters.size <= 0) {
        logError("No meters found")
      }

      meters.nonEmpty shouldEqual true
    }

    val firstMatchedMetersIndex = meters
      .indexWhere(meter => meter.getId.getName.startsWith(prefix))

    if (firstMatchedMetersIndex >= 0 != expectedToFind) {
      logError(s"Matched meter with index $firstMatchedMetersIndex does not reflect expectation $expectedToFind")
    }

    firstMatchedMetersIndex >= 0 shouldEqual expectedToFind
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
    super.beforeAll()
    createClient()
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

  def createClient(): Unit = {
    System.out.println("Cosmos Client started!!!!!!")
    cosmosClient = new CosmosClientBuilder()
      .endpoint(TestConfigurations.HOST)
      .key(TestConfigurations.MASTER_KEY)
      .buildAsyncClient()
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

trait CosmosGatewayClient extends CosmosClient {
  this: Suite =>

  override def createClient(): Unit = {
    System.out.println("Cosmos Gateway Client started!!!!!!")
    cosmosClient = new CosmosClientBuilder()
      .endpoint(TestConfigurations.HOST)
      .key(TestConfigurations.MASTER_KEY)
      .gatewayMode()
      .buildAsyncClient()
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
    queryItems(query, cosmosDatabase, cosmosContainer)
  }

  def queryItems(query: String, databaseName: String, containerName: String): List[ObjectNode] = {
    cosmosClient.getDatabase(databaseName).getContainer(containerName)
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
      ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofMinutes(10)))

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

trait AutoCleanableCosmosContainerWithSubpartitions extends CosmosContainerWithSubpartitions with BeforeAndAfterEach {
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
                        val partitionKey = new PartitionKeyBuilder()
                            .add(item.get("tenantId").textValue())
                            .add(item.get("userId").textValue())
                            .add(item.get("sessionId").textValue())
                            .build()
                        val operation = CosmosBulkOperations.getDeleteItemOperation(getId(item), partitionKey)
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

trait CosmosContainerWithSubpartitions extends CosmosContainer {
    this: Suite =>
    //scalastyle:off

    override def createContainerCore(): Unit = {
        val partitionKeyPaths = new ArrayList[String]
        partitionKeyPaths.add("/tenantId")
        partitionKeyPaths.add("/userId")
        partitionKeyPaths.add("/sessionId")
        val subpartitionKeyDefinition = new PartitionKeyDefinition
        subpartitionKeyDefinition.setPaths(partitionKeyPaths)
        subpartitionKeyDefinition.setKind(PartitionKind.MULTI_HASH)
        subpartitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2)
        var containerProperties = new CosmosContainerProperties(cosmosContainer, subpartitionKeyDefinition)
        val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)

        cosmosClient
            .getDatabase(cosmosDatabase)
            .createContainerIfNotExists(containerProperties, throughputProperties).block()
    }
}

trait CosmosContainersWithPkAsPartitionKey extends CosmosDatabase {
  this: Suite =>
  //scalastyle:off

  var cosmosContainer: String = UUID.randomUUID().toString
  var cosmosContainersWithPkAsPartitionKey: String = UUID.randomUUID().toString

  val idPath: String = "/id"
  val pkPartitionKeyPath: String = "/pk"

  def getPartitionKeyValue(objectNode: ObjectNode, partitionKeyPath: String) : Object = {
    // assumes pkValue is always string
    objectNode.get(partitionKeyPath.stripPrefix("/")).textValue()
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
      cosmosClient.getDatabase(cosmosDatabase).getContainer(cosmosContainersWithPkAsPartitionKey).delete().block()
    }
    finally {
      this.cosmosContainer = UUID.randomUUID().toString
      this.cosmosContainersWithPkAsPartitionKey = UUID.randomUUID().toString
      this.createContainerCore()
    }
  }

  def createContainerCore(): Unit = {
    val throughputProperties = ThroughputProperties.createManualThroughput(Defaults.DefaultContainerThroughput)

    for (containerTuple <- Array((cosmosContainer, idPath), (cosmosContainersWithPkAsPartitionKey, pkPartitionKeyPath))) {
      cosmosClient
        .getDatabase(cosmosDatabase)
        .createContainerIfNotExists(containerTuple._1, containerTuple._2, throughputProperties).block()
    }
  }

  override def afterAll(): Unit = {
    try {
      for (containerName <- Array(cosmosContainer, cosmosContainersWithPkAsPartitionKey)) {
        cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName).delete().block()
      }
    }
    finally super.afterAll() // To be stackable, must call super.afterAll
  }

  def queryItems(query: String, cosmosContainerName: String): List[ObjectNode] = {
    queryItems(query, cosmosDatabase, cosmosContainerName)
  }

  def queryItems(query: String, databaseName: String, containerName: String): List[ObjectNode] = {
    cosmosClient.getDatabase(databaseName).getContainer(containerName)
      .queryItems(query, classOf[ObjectNode])
      .toIterable
      .asScala
      .toList
  }

  def readAllItems(containerName: String): List[ObjectNode] = {
    cosmosClient.getDatabase(cosmosDatabase).getContainer(containerName)
      .queryItems("SELECT * FROM r", classOf[ObjectNode])
      .toIterable
      .asScala
      .toList
  }
}

trait AutoCleanableCosmosContainersWithPkAsPartitionKey extends CosmosContainersWithPkAsPartitionKey with BeforeAndAfterEach {
  this: Suite =>

  override def afterEach(): Unit = {

    try {
      // wait for data to get replicated
      Thread.sleep(1000)
      for (containerTuple <- Array((cosmosContainer, idPath), (cosmosContainersWithPkAsPartitionKey, pkPartitionKeyPath))) {
        System.out.println(s"cleaning the items in container ${containerTuple._1}")
        val container = cosmosClient.getDatabase(cosmosDatabase).getContainer(containerTuple._1)

        try {
          val emitter: Sinks.Many[CosmosItemOperation] = Sinks.many().unicast().onBackpressureBuffer()

          val bulkDeleteFlux = container.executeBulkOperations(emitter.asFlux())

          val cnt = new AtomicInteger(0)
          container.queryItems("SELECT * FROM r", classOf[ObjectNode])
            .asScala
            .doOnNext(item => {
              val operation = CosmosBulkOperations.getDeleteItemOperation(getId(item), new PartitionKey(getPartitionKeyValue(item, containerTuple._2)))
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

class NakedLocalJavaFileSystem() extends NakedLocalFileSystem {

  // The NakedLocalFileSystem requires to use schema file:/// - which conflicts
  // with some spark code paths where this would automatically trigger winutils to be
  // used - overriding the schema here to allow using NakedLocalFileSystem instead of winutils
  override def getUri: URI = {
    LocalJavaFileSystem.NAME
  }

  override def checkPath(path: Path): Unit = {
    super.checkPath(path)
  }
}

// Just a wrapper to allow injecting the NakedLocalFileSystem with modified schema
class BareLocalJavaFileSystem() extends BareLocalFileSystem(new NakedLocalJavaFileSystem()) {
}

object LocalJavaFileSystem {

  val NAME = URI.create("localfs:///")

  def applyToSparkSession(spark: SparkSession) = {
    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "localfs:///")
    spark.sparkContext.hadoopConfiguration.setClass(
      "fs.localfs.impl", classOf[BareLocalJavaFileSystem], classOf[FileSystem])
  }
}

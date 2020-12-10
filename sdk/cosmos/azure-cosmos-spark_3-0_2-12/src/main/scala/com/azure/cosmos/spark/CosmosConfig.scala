// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import java.net.URL
import java.util.Locale

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// each config category will be a case class:
// TODO moderakh more configs
//case class ClientConfig()
//case class CosmosBatchWriteConfig()

case class CosmosAccountConfig(endpoint: String, key: String)

object CosmosConfig {

  def getEffectiveConfig(sparkConf: SparkConf, // spark application config
                         userProvidedOptions: Map[String, String] // user provided config
                        ) : Map[String, String] = {
    val conf = sparkConf.clone()
    conf.setAll(userProvidedOptions).getAll.toMap
  }

  @throws[IllegalStateException] // if there is no active spark session
  def getEffectiveConfig(userProvidedOptions: Map[String, String] = Map().empty) : Map[String, String] = {
    val session = SparkSession.active

    // TODO: moderakh we should investigate how spark sql config should be merged:
    // TODO: session.conf.getAll, // spark sql runtime config
    getEffectiveConfig(
      session.sparkContext.getConf, // spark application config
      userProvidedOptions) // user provided config
  }
}

object CosmosAccountConfig {
  val CosmosAccountEndpointUri = CosmosConfigEntry[String](key = "spark.cosmos.accountEndpoint",
    mandatory = true,
    parseFromStringFunction = accountEndpointUri => {
      new URL(accountEndpointUri)
      accountEndpointUri
    },
    helpMessage = "Cosmos DB Account Endpoint Uri")

  val CosmosKey = CosmosConfigEntry[String](key = "spark.cosmos.accountKey",
    mandatory = true,
    parseFromStringFunction = accountEndpointUri => accountEndpointUri,
    helpMessage = "Cosmos DB Account Key")

  def parseCosmosAccountConfig(cfg: Map[String, String]): CosmosAccountConfig = {
    val endpointOpt = CosmosConfigEntry.parse(cfg, CosmosAccountEndpointUri)
    val key = CosmosConfigEntry.parse(cfg, CosmosKey)

    // parsing above already validated these assertions
    assert(endpointOpt.isDefined)
    assert(key.isDefined)

    CosmosAccountConfig(endpointOpt.get, key.get)
  }
}

case class CosmosContainerConfig(database: String, container: String)

object CosmosContainerConfig {
  val databaseName = CosmosConfigEntry[String](key = "spark.cosmos.database",
    mandatory = true,
    parseFromStringFunction = database => database,
    helpMessage = "Cosmos DB database name")

  val containerName = CosmosConfigEntry[String](key = "spark.cosmos.container",
    mandatory = true,
    parseFromStringFunction = container => container,
    helpMessage = "Cosmos DB container name")

  def parseCosmosContainerConfig(cfg: Map[String, String]): CosmosContainerConfig = {
    val databaseOpt = CosmosConfigEntry.parse(cfg, databaseName)
    val containerOpt = CosmosConfigEntry.parse(cfg, containerName)

    // parsing above already validated this
    assert(databaseOpt.isDefined)
    assert(containerOpt.isDefined)

    CosmosContainerConfig(databaseOpt.get, containerOpt.get)
  }
}

case class CosmosConfigEntry[T](key: String,
                                mandatory: Boolean,
                                defaultValue: Option[String] = Option.empty,
                                parseFromStringFunction: String => T,
                                helpMessage: String) {
  CosmosConfigEntry.configEntriesDefinitions.put(key, this)

  def parse(paramAsString: String) : T = {
    try {
      parseFromStringFunction(paramAsString)
    } catch {
      case e: Exception => throw new RuntimeException(s"invalid configuration for ${key}:${paramAsString}. Config description: ${helpMessage}",  e)
    }
  }
}

// TODO: moderakh how to merge user config with SparkConf application config?
object CosmosConfigEntry {
  private val configEntriesDefinitions = new java.util.HashMap[String, CosmosConfigEntry[_]]()

  def allConfigNames(): Seq[String] = {
    configEntriesDefinitions.keySet().asScala.toSeq
  }

  def parse[T](configuration: Map[String, String], configEntry: CosmosConfigEntry[T]): Option[T] = {
    // TODO moderakh: where should we handle case sensitivity?
    // we are doing this here per config parsing for now
    val opt = configuration.map { case (key, value) => (key.toLowerCase(Locale.ROOT), value) }.get(configEntry.key.toLowerCase(Locale.ROOT))
    if (opt.isDefined) {
      Option.apply(configEntry.parse(opt.get))
    }
    else {
      if (configEntry.mandatory) {
        throw new RuntimeException(s"mandatory option ${configEntry.key} is missing. Config description: ${configEntry.helpMessage}")
      } else {
        Option.empty
      }
    }
  }
}

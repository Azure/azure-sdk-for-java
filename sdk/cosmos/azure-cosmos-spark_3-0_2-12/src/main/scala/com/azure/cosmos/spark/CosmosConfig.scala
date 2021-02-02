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

case class CosmosWriteConfig(upsertEnabled: Boolean, maxRetryCount: Int)

object CosmosWriteConfig {
  val upsertEnabled = CosmosConfigEntry[Boolean](key = "spark.cosmos.write.upsertEnabled",
    mandatory = false,
    defaultValue = Option.apply(true), // TODO: what the default value should be?
    parseFromStringFunction = overwriteEnabled => overwriteEnabled.toBoolean,
    helpMessage = "Cosmos DB Write Upsert Enabled")

  val maxRetryCount = CosmosConfigEntry[Int](key = "spark.cosmos.write.maxRetryCount",
    mandatory = false,
    defaultValue = Option.apply(3),
    parseFromStringFunction = maxRetryAttempt => {
      val cnt = maxRetryAttempt.toInt
      if (cnt < 0) {
        throw new RuntimeException(s"expected a non-negative number")
      }
      cnt
    },
    helpMessage = "Cosmos DB Write Max Retry Attempts on failure")

  def parseWriteConfig(cfg: Map[String, String]): CosmosWriteConfig = {
    val upsertEnabledOpt = CosmosConfigEntry.parse(cfg, upsertEnabled)
    val maxRetryCountOpt = CosmosConfigEntry.parse(cfg, maxRetryCount)

    // parsing above already validated this
    assert(upsertEnabledOpt.isDefined)
    assert(maxRetryCountOpt.isDefined)

    CosmosWriteConfig(upsertEnabledOpt.get, maxRetryCountOpt.get)
  }
}

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

case class CosmosSchemaInferenceConfig(
                                          inferSchemaSamplingSize: Int,
                                          inferSchemaEnabled: Boolean,
                                          inferSchemaQuery: Option[String])

object CosmosSchemaInferenceConfig {
    private val DefaultSampleSize: Int = 1000

    val inferSchemaSamplingSize = CosmosConfigEntry[Int](key = "spark.cosmos.read.inferSchemaSamplingSize",
        mandatory = false,
        parseFromStringFunction = size => size.toInt,
        helpMessage = "Sampling size to use when inferring schema")

    val inferSchemaEnabled = CosmosConfigEntry[Boolean](key = "spark.cosmos.read.inferSchemaEnabled",
        mandatory = false,
        parseFromStringFunction = enabled => enabled.toBoolean,
        helpMessage = "Whether schema inference is enabled or should return raw json")

    val inferSchemaQuery = CosmosConfigEntry[String](key = "spark.cosmos.read.inferSchemaQuery",
        mandatory = false,
        parseFromStringFunction = query => query,
        helpMessage = "When schema inference is enabled, used as custom query to infer it")

    def parseCosmosReadConfig(cfg: Map[String, String]): CosmosSchemaInferenceConfig = {
        val samplingSize = CosmosConfigEntry.parse(cfg, inferSchemaSamplingSize)
        val enabled = CosmosConfigEntry.parse(cfg, inferSchemaEnabled)
        val query = CosmosConfigEntry.parse(cfg, inferSchemaQuery)

        CosmosSchemaInferenceConfig(
            samplingSize.getOrElse(DefaultSampleSize),
            enabled.getOrElse(false),
            query)
    }
}

case class CosmosConfigEntry[T](key: String,
                                mandatory: Boolean,
                                defaultValue: Option[T] = Option.empty,
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
        configEntry.defaultValue.orElse(Option.empty)
      }
    }
  }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.Locale
import scala.collection.JavaConverters.{asScalaIteratorConverter, mapAsJavaMapConverter}

class CosmosViewMetadataPersistenceSpec extends UnitSpec with Spark {
  private val objectMapper = new ObjectMapper()
  private val catalogName = "metadataTestCatalog"
  private val databaseName = "test-database"
  private val containerName = "test-container"
  private val viewName = "test-view"
  private val enabled = "true"
  private val schema = StructType(Seq(StructField("id", StringType, nullable = true)))
  private val primaryEndpoint = "https://synthetic-primary.documents.azure.com:443/"
  private val primaryAccountOptions = Map(
    CosmosConfigNames.AccountEndpoint -> primaryEndpoint,
    CosmosConfigNames.AccountKey -> "SYNTHETIC_PRIMARY_ACCOUNT_KEY")
  private val safeOptions = Map(
    CosmosConfigNames.Database -> databaseName,
    CosmosConfigNames.Container -> containerName,
    CosmosConfigNames.AuthType -> "ServicePrincipal",
    CosmosConfigNames.TenantId -> "11111111-1111-1111-1111-111111111111",
    CosmosConfigNames.ClientId -> "22222222-2222-2222-2222-222222222222",
    CosmosConfigNames.ReadInferSchemaEnabled -> "false",
    CosmosConfigNames.ReadCustomQuery -> "SELECT * FROM c WHERE c.kind = 'test'",
    CosmosConfigNames.ReadMaxItemCount -> "10",
    CosmosConfigNames.ThroughputControlAccountEndpoint -> "https://synthetic-throughput.documents.azure.com:443/")
  private val sensitiveOptions = Map(
    CosmosConfigNames.AccountKey -> primaryAccountOptions(CosmosConfigNames.AccountKey),
    CosmosConfigNames.ClientSecret -> "SYNTHETIC_SERVICE_PRINCIPAL_SECRET",
    CosmosConfigNames.ClientCertPemBase64 -> "SYNTHETIC_CERTIFICATE_PRIVATE_KEY",
    CosmosConfigNames.ThroughputControlAccountKey -> "SYNTHETIC_THROUGHPUT_ACCOUNT_KEY",
    CosmosConfigNames.DiagnosticsAzureMonitorConnectionString -> "InstrumentationKey=SYNTHETIC_DIAGNOSTICS_KEY",
    CosmosConfigNames.MetricsAzureMonitorConnectionString -> "InstrumentationKey=SYNTHETIC_METRICS_KEY")
  private val credentialHeaderOptions = Map(
    CosmosConfigNames.AdditionalHeaders -> """{"Authorization":"Bearer SYNTHETIC_HEADER_TOKEN"}""")
  private val unrecognizedOptions = Map("application.accessToken" -> "SYNTHETIC_UNRECOGNIZED_TOKEN")
  private var repository: Option[Path] = None

  override def beforeEach(): Unit = {
    super.beforeEach()
    repository = Some(Files.createTempDirectory("cosmos-view-metadata-"))
  }

  override def afterEach(): Unit = {
    try {
      repository.foreach { root =>
        val paths = Files.walk(root)
        try {
          paths.iterator().asScala.toVector.sortBy(_.getNameCount).reverse.foreach(Files.delete)
        } finally {
          paths.close()
        }
      }
    } finally {
      repository = None
      super.afterEach()
    }
  }

  "Catalog view metadata" should "persist safe view options without credential values" in {
    val options = safeOptions ++ sensitiveOptions ++ primaryAccountOptions ++ unrecognizedOptions
    createView(options)

    val json = readPersistedEnvelope()
    val views = ViewDefinitionEnvelopeSerializer.fromJson(json)
    views.length shouldEqual 1
    assertViewMetadata(views.head, viewName, Some(schema), safeOptions)
    assertExcluded(json, views.head, sensitiveOptions ++ primaryAccountOptions ++ unrecognizedOptions)
    views.head.options.keySet shouldEqual safeOptions.keySet
  }

  it should "exclude mixed-case credentials and catalog-prefixed or unknown options" in {
    val mixedCaseSafeOptions = uppercaseKeys(safeOptions)
    val mixedCaseSensitiveOptions = uppercaseKeys(sensitiveOptions ++ primaryAccountOptions)
    val prefixedOptions = sensitiveOptions.map { case (key, _) =>
      s"spark.sql.catalog.$catalogName.$key" -> s"SYNTHETIC_PREFIXED_${key.toUpperCase(Locale.ROOT)}"
    }
    val excludedOptions = mixedCaseSensitiveOptions ++ prefixedOptions ++ unrecognizedOptions
    createView(mixedCaseSafeOptions ++ excludedOptions)

    val json = readPersistedEnvelope()
    val views = ViewDefinitionEnvelopeSerializer.fromJson(json)
    views.length shouldEqual 1
    assertViewMetadata(views.head, viewName, Some(schema), mixedCaseSafeOptions)
    assertExcluded(json, views.head, excludedOptions)
    views.head.options.keySet shouldEqual mixedCaseSafeOptions.keySet
  }

  "View snapshot serialization" should "filter existing options without changing the loaded definitions" in {
    val unknownCosmosOptions = Map("spark.cosmos.futureCredential" -> "SYNTHETIC_UNKNOWN_COSMOS_SECRET")
    val excludedOptions = sensitiveOptions ++ credentialHeaderOptions ++ unrecognizedOptions ++ unknownCosmosOptions
    val expectedSafeOptions = safeOptions ++ Map(CosmosConfigNames.AccountEndpoint -> primaryEndpoint)
    val originalOptions = expectedSafeOptions ++ excludedOptions
    val existingViews = ViewDefinitionEnvelopeSerializer.fromJson(legacyEnvelope(originalOptions))
    existingViews.head.options shouldEqual originalOptions

    val emptyViewName = "credential-only-view"
    val snapshot = existingViews ++ Array(ViewDefinition(databaseName, emptyViewName, None, excludedOptions))
    val json = ViewDefinitionEnvelopeSerializer.toJson(snapshot)
    val views = ViewDefinitionEnvelopeSerializer.fromJson(json)

    views.length shouldEqual snapshot.length
    assertViewMetadata(views.head, viewName, Some(schema), expectedSafeOptions)
    assertViewMetadata(views.last, emptyViewName, None, Map.empty[String, String])
    forEvery(views) { view =>
      assertExcluded(json, view, excludedOptions)
    }
    views.head.options shouldEqual expectedSafeOptions
    views.last.options shouldBe empty
    existingViews.head.options shouldEqual originalOptions
  }

  private def createView(options: Map[String, String]): Unit = {
    val catalogOptions = Map(
      CosmosConfigNames.AccountEndpoint -> "https://synthetic-catalog.documents.azure.com:443/",
      CosmosConfigNames.AccountKey -> "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      CosmosConfigNames.ViewsRepositoryPath ->
        LocalJavaFileSystem.NAME.resolve(repository.value.toUri.getRawPath).toString)
    val catalog = new CosmosCatalogBase()
    catalog.initialize(catalogName, new CaseInsensitiveStringMap(catalogOptions.asJava))
    catalog.createTable(
      Identifier.of(Array(databaseName), viewName),
      schema,
      Array.empty[Transform],
      (options + ("isCosmosView" -> enabled)).asJava)
  }

  private def readPersistedEnvelope(): String = {
    val paths = Files.list(repository.value)
    val metadataFiles = try {
      paths.iterator().asScala.filter(_.getFileName.toString.matches("[0-9]+")).toVector
    } finally {
      paths.close()
    }
    metadataFiles should not be empty
    val latest = metadataFiles.maxBy(_.getFileName.toString.toLong)
    val storedJson = new String(Files.readAllBytes(latest), StandardCharsets.UTF_8)
    // HDFSMetadataLog[String] stores the view envelope inside an outer JSON string.
    val envelope = objectMapper.readTree(storedJson)
    envelope.isTextual shouldBe true
    envelope.asText()
  }

  private def assertViewMetadata(view: ViewDefinition,
                                 expectedName: String,
                                 expectedSchema: Option[StructType],
                                 expectedOptions: Map[String, String]): Unit = {
    view.databaseName shouldEqual databaseName
    view.viewName shouldEqual expectedName
    view.userProvidedSchema shouldEqual expectedSchema
    view.options.filter { case (key, _) => expectedOptions.contains(key) } shouldEqual expectedOptions
  }

  private def assertExcluded(json: String,
                             view: ViewDefinition,
                             excludedOptions: Map[String, String]): Unit = {
    val persistedKeys = view.options.keySet.intersect(excludedOptions.keySet)
    val persistedValues = excludedOptions.collect {
      case (key, value) if json.contains(value) => key
    }.toSet
    withClue("Unsafe option names or synthetic values persisted: ") {
      (persistedKeys ++ persistedValues) shouldBe empty
    }
  }

  private def uppercaseKeys(options: Map[String, String]): Map[String, String] = {
    options.map { case (key, value) => key.toUpperCase(Locale.ROOT) -> value }
  }

  private def legacyEnvelope(options: Map[String, String]): String = {
    val root = objectMapper.createObjectNode()
    root.put("id", ViewDefinitionEnvelopeSerializer.V1Identifier)
    val view = root.putArray("views").addObject()
    view.put("database", databaseName)
    view.put("name", viewName)
    view.put("customSchema", schema.toDDL)
    val entries = view.putArray("options")
    options.foreach { case (key, value) =>
      val entry = entries.addObject()
      entry.put("k", key)
      entry.put("v", value)
    }
    objectMapper.writeValueAsString(root)
  }
}

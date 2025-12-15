// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.ReadConsistencyStrategy
import com.azure.cosmos.models.{DedicatedGatewayRequestOptions, PartitionKey, SparkModelBridgeInternal}
import org.apache.spark.sql.sources.{EqualTo, Filter, In}
import reactor.util.concurrent.Queues

class ReadManyFilterAnalyzerSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number
  private[this] val readConfigWithoutCustomQuery =
  new CosmosReadConfig(
    ReadConsistencyStrategy.EVENTUAL,
    SchemaConversionModes.Relaxed,
    100,
    Queues.XS_BUFFER_SIZE,
    new DedicatedGatewayRequestOptions,
    None,
    None,
    false,
    CosmosReadManyFilteringConfig(false, "_itemIdentity"))

  it should "use id as the effective readMany filtering property when id is the partitionKey" in {
    val filters = Array[Filter](
      In("_itemIdentity", Array("id(1).pk([\"1pk\"])", "id(2).pk([\"2pk\"])")),
      In("id", Array("3", "4")),
      In("random", Array("5", "6")),
      EqualTo("physicist", "Schrodinger")
    )

    val pkDefinitionString = "{\"paths\":[\"/id\"],\"kind\":\"Hash\"}"
    val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(pkDefinitionString)
    val readManyFilterAnalyzer = ReadManyFilterAnalyzer(readConfigWithoutCustomQuery, pkDefinition)

    val analyzedFilters = readManyFilterAnalyzer.analyze(filters)

    analyzedFilters.filtersToBePushedDownToCosmos should have size 1
    analyzedFilters.filtersNotSupportedByCosmos should have size 3
    analyzedFilters.filtersToBePushedDownToCosmos should contain(filters(1))
    analyzedFilters.filtersNotSupportedByCosmos should contain allElementsOf (Array(filters(0), filters(2), filters(3)))

    analyzedFilters.readManyFiltersOpt.isDefined shouldBe true
    analyzedFilters.readManyFiltersOpt.get should have size 2

    val expectedReadManyFilters = Array(
      ReadManyFilter(new PartitionKey("3"), "id(3).pk([\"3\"])"),
      ReadManyFilter(new PartitionKey("4"), "id(4).pk([\"4\"])"),
    )
    analyzedFilters.readManyFiltersOpt.get should contain allElementsOf expectedReadManyFilters
  }

  it should "use _itemIdentity as the effective readMany filtering property when id is not the partitionKey" in {
    val filters = Array[Filter](
      In("_itemIdentity", Array("id(1).pk([\"1pk\"])", "id(2).pk([\"2pk\"])")),
      In("id", Array("3", "4")),
      In("random", Array("5", "6")),
      EqualTo("physicist", "Schrodinger")
    )

    val pkDefinitionString = "{\"paths\":[\"/pk\"],\"kind\":\"Hash\"}"
    val pkDefinition = SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(pkDefinitionString)
    val readManyFilterAnalyzer = ReadManyFilterAnalyzer(readConfigWithoutCustomQuery, pkDefinition)

    val analyzedFilters = readManyFilterAnalyzer.analyze(filters)

    analyzedFilters.filtersToBePushedDownToCosmos should have size 1
    analyzedFilters.filtersNotSupportedByCosmos should have size 3
    analyzedFilters.filtersToBePushedDownToCosmos should contain(filters(0))
    analyzedFilters.filtersNotSupportedByCosmos should contain allElementsOf (Array(filters(1), filters(2), filters(3)))

    analyzedFilters.readManyFiltersOpt.isDefined shouldBe true
    analyzedFilters.readManyFiltersOpt.get should have size 2

    val expectedReadManyFilters = Array(
      ReadManyFilter(new PartitionKey("1pk"), "id(1).pk([\"1pk\"])"),
      ReadManyFilter(new PartitionKey("2pk"), "id(2).pk([\"2pk\"])"),
    )
    analyzedFilters.readManyFiltersOpt.get should contain allElementsOf expectedReadManyFilters
  }
}

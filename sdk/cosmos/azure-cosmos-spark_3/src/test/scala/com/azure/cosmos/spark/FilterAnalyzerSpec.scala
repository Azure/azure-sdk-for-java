// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.ReadConsistencyStrategy
import com.azure.cosmos.models.{CosmosParameterizedQuery, DedicatedGatewayRequestOptions, SparkModelBridgeInternal}
import org.apache.spark.sql.sources.{AlwaysFalse, AlwaysTrue, EqualTo, Filter, In, IsNotNull, IsNull, StringContains, StringEndsWith, StringStartsWith}
import org.assertj.core.api.Assertions.assertThat
import reactor.util.concurrent.Queues
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class FilterAnalyzerSpec extends UnitSpec {
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
  private[this] val queryText = "SELECT * FROM c WHERE c.abc='Hello World'"
  private[this] val query = Some(CosmosParameterizedQuery(
    queryText,
    List.empty[String],
    List.empty[Any]))
  private[this] val readConfigWithCustomQuery = new CosmosReadConfig(
    ReadConsistencyStrategy.EVENTUAL,
    SchemaConversionModes.Relaxed,
    100,
    Queues.XS_BUFFER_SIZE,
    new DedicatedGatewayRequestOptions,
    query,
    None,
    true,
    CosmosReadManyFilteringConfig(false, "_itemIdentity"))

  private val pkDefinition = "{\"paths\":[\"/pk\"],\"kind\":\"Hash\"}"
  private val partitionKeyDefinition =
    SparkModelBridgeInternal.createPartitionKeyDefinitionFromJson(pkDefinition)

  private[this] lazy val filterProcessorWithoutCustomQuery = FilterAnalyzer(readConfigWithoutCustomQuery, partitionKeyDefinition)
  private[this] lazy val filterProcessorWithCustomQuery = FilterAnalyzer(readConfigWithCustomQuery, partitionKeyDefinition)

  "many filters" should "be translated to cosmos predicates with AND" in {
    val filters = Array[Filter](
      EqualTo("physicist", "Schrodinger"), In("isCatAlive", Array(true, false)))
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos.toIterable should contain theSameElementsAs filters.toList

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['physicist']=@param0 AND r['isCatAlive'] IN (@param1,@param2)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1", "@param2")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", true, false)
  }

  "in" should "be translated to cosmos in predicate" in {
    val filters = Array[Filter](In("physicist", Array("Schrodinger", "Hawking")))
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['physicist'] IN (@param0,@param1)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", "Hawking")
  }

  "= on number" should "be translated to cosmos = predicate" in {
    val filters = Array[Filter](EqualTo("age", 5))
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['age']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List(5)
  }

  "= on utf8" should "be translated to cosmos = predicate" in {
    val filters = Array[Filter](EqualTo("mathematician", "خوارزمی"))

    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['mathematician']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("خوارزمی")
  }

  "IsNull" should "be translated to cosmos" in {
    val filters = Array[Filter](IsNull("age"))
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE (IS_NULL(r['age']) OR NOT(IS_DEFINED(r['age'])))"
    query.parameterNames shouldBe empty
  }

  "IsNotNull" should "be translated to cosmos" in {
    val filters = Array[Filter](IsNotNull("age"))
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE (NOT(IS_NULL(r['age'])) AND IS_DEFINED(r['age']))"
    query.parameterNames shouldBe empty
  }

  "nested filter" should "be translated to nested cosmos json filter" in {
    val filters = Array[Filter](
      EqualTo("mathematician", "خوارزمی"),
      EqualTo("mathematician.book", "Algorithmo de Numero Indorum"),
      In("mathematician.work", Array("Algebra", "Algorithm")))

    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['mathematician']=@param0" +
      " AND r['mathematician']['book']=@param1" +
      " AND r['mathematician']['work'] IN (@param2,@param3)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1", "@param2", "@param3")
    query.parameterValues should contain theSameElementsInOrderAs
      List("خوارزمی",
      "Algorithmo de Numero Indorum",
      "Algebra",
      "Algorithm")
  }

  "StringStartsWith on utf8" should "be translated to cosmos startswith predicate" in {
    val filters = Array[Filter](StringStartsWith("mathematician.book", "Algorithmo"))

    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE STARTSWITH(r['mathematician']['book'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Algorithmo")
  }

  "StringEndsWith on utf8" should "be translated to cosmos endswith predicate" in {
    val filters = Array[Filter](StringEndsWith("mathematician.book", "Numero Indorum"))

    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE ENDSWITH(r['mathematician']['book'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Numero Indorum")
  }

  "StringContains on utf8" should "be translated to cosmos contains predicate" in {
    // algebra
    val filters = Array[Filter](StringContains("mathematician.work", "gebr"))

    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE CONTAINS(r['mathematician']['work'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("gebr")
  }

  "no filter" should "be translated to cosmos query with no where clause" in {
    val filters = Array[Filter]()
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r"
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter" should "be translated to cosmos predicates with AND" in {
    val filters = Array[Filter](AlwaysTrue)
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryText).isEqualTo("SELECT * FROM r")
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter mixed with supported filter" should "be translated to cosmos predicates with AND" in {
    val filters = Array[Filter](AlwaysTrue, EqualTo("pet", "Schrodinger cat"), AlwaysFalse)
    val analyzedQuery = filterProcessorWithoutCustomQuery.analyze(filters)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).containsExactly(EqualTo("pet", "Schrodinger cat"))
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).containsExactlyElementsOf(Array(AlwaysTrue, AlwaysFalse).toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['pet']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger cat")
  }

  "for custom query predicates" should "not be pushed down" in {
    val filters = Array[Filter](
      EqualTo("physicist", "Schrodinger"), In("isCatAlive", Array(true, false)))
    val analyzedQuery = filterProcessorWithCustomQuery.analyze(filters)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos.toIterable should contain theSameElementsAs filters.toList

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual queryText
    query.parameterNames shouldBe empty
    query.parameterValues shouldBe empty
  }

  "_itemIdentity filter" should "be ignored when readManyFiltering is disabled" in {
    val filters = Array[Filter](
      In("_itemIdentity", Array("id(1).pk(1)", "id(2).pk(2)"))
    )

    val analyzedFilters = filterProcessorWithoutCustomQuery.analyze(filters)
    analyzedFilters.filtersToBePushedDownToCosmos shouldBe empty
    analyzedFilters.filtersNotSupportedByCosmos.toIterable should contain theSameElementsAs filters.toList
    analyzedFilters.cosmosParametrizedQuery.queryText shouldEqual QueryFilterAnalyzer.rootParameterizedQuery.queryText
    analyzedFilters.readManyFiltersOpt.isDefined shouldBe false
  }

  "_itemIdentity filter" should "be parsed into readMany list when readManyFiltering is enabled" in {
    val readConfigWithReadManyFilterEnabled =
      new CosmosReadConfig(
        ReadConsistencyStrategy.EVENTUAL,
        SchemaConversionModes.Relaxed,
        100,
        Queues.XS_BUFFER_SIZE,
        new DedicatedGatewayRequestOptions,
        None,
        None,
        false,
        CosmosReadManyFilteringConfig(true, "_itemIdentity"))

    val filterAnalyzer = FilterAnalyzer(readConfigWithReadManyFilterEnabled, partitionKeyDefinition)
    val itemIdentityOne = CosmosItemIdentityHelper.getCosmosItemIdentityValueString("1", List("1"))
    val itemIdentityTwo = CosmosItemIdentityHelper.getCosmosItemIdentityValueString("2", List("2"))

    val filters = Array[Filter](
      In("_itemIdentity", Array(itemIdentityOne, itemIdentityTwo)),
      In("isCatAlive", Array(true, false)) // add another filter to also test once readMany filter can be applied, query filter will not kick in
    )

    val analyzedFilters = filterAnalyzer.analyze(filters)

    analyzedFilters.filtersToBePushedDownToCosmos.contains(filters(0)) shouldBe true
    analyzedFilters.filtersNotSupportedByCosmos.contains(filters(1)) shouldBe  true
    analyzedFilters.cosmosParametrizedQuery.queryText shouldEqual QueryFilterAnalyzer.rootParameterizedQuery.queryText
    analyzedFilters.readManyFiltersOpt.isDefined shouldBe true
    analyzedFilters.readManyFiltersOpt.get.size shouldBe 2
    analyzedFilters.readManyFiltersOpt.get(0).value shouldEqual itemIdentityOne
    analyzedFilters.readManyFiltersOpt.get(1).value shouldEqual itemIdentityTwo
  }

  // TODO: moderakhs add unit test for all spark filters

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

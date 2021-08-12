// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParameterizedQuery
import org.apache.spark.sql.sources.{AlwaysFalse, AlwaysTrue, EqualTo, Filter, In, StringEndsWith, StringStartsWith, StringContains}
import org.assertj.core.api.Assertions.assertThat
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class FilterAnalyzerSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  private[this] val readConfigWithoutCustomQuery =
    new CosmosReadConfig(true, SchemaConversionModes.Relaxed, 100, None)
  private[this] val queryText = "SELECT * FROM c WHERE c.abc='Hello World'"
  private[this] val query = Some(CosmosParameterizedQuery(
    queryText,
    List.empty[String],
    List.empty[Any]))
  private[this] val readConfigWithCustomQuery = new CosmosReadConfig(
    true,
    SchemaConversionModes.Relaxed,
    100,
    query)

  "many filters" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](
      EqualTo("physicist", "Schrodinger"), In("isCatAlive", Array(true, false)))
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos.toIterable should contain theSameElementsAs filters.toList

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['physicist']=@param0 AND r['isCatAlive'] IN (@param1,@param2)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1", "@param2")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", true, false)
  }

  "in" should "be translated to cosmos in predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](In("physicist", Array("Schrodinger", "Hawking")))
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['physicist'] IN (@param0,@param1)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", "Hawking")
  }

  "= on number" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("age", 5))
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['age']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List(5)
  }

  "= on utf8" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("mathematician", "خوارزمی"))

    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['mathematician']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("خوارزمی")
  }

  "nested filter" should "be translated to nested cosmos json filter" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](
      EqualTo("mathematician", "خوارزمی"),
      EqualTo("mathematician.book", "Algorithmo de Numero Indorum"),
      In("mathematician.work", Array("Algebra", "Algorithm")))

    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
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
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](StringStartsWith("mathematician.book", "Algorithmo"))

    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE STARTSWITH(r['mathematician']['book'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Algorithmo")
  }

  "StringEndsWith on utf8" should "be translated to cosmos endswith predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](StringEndsWith("mathematician.book", "Numero Indorum"))

    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE ENDSWITH(r['mathematician']['book'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Numero Indorum")
  }

  "StringContains on utf8" should "be translated to cosmos contains predicate" in {
    val filterProcessor = FilterAnalyzer()

    // algebra
    val filters = Array[Filter](StringContains("mathematician.work", "gebr"))

    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE CONTAINS(r['mathematician']['work'],@param0)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("gebr")
  }

  "no filter" should "be translated to cosmos query with no where clause" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter]()
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r"
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue)
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryText).isEqualTo("SELECT * FROM r")
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter mixed with supported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue, EqualTo("pet", "Schrodinger cat"), AlwaysFalse)
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithoutCustomQuery)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).containsExactly(EqualTo("pet", "Schrodinger cat"))
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).containsExactlyElementsOf(Array(AlwaysTrue, AlwaysFalse).toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual "SELECT * FROM r WHERE r['pet']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger cat")
  }

  "for custom query predicates" should "not be pushed down" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](
      EqualTo("physicist", "Schrodinger"), In("isCatAlive", Array(true, false)))
    val analyzedQuery = filterProcessor.analyze(filters, readConfigWithCustomQuery)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos.toIterable should contain theSameElementsAs filters.toList

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryText shouldEqual queryText
    query.parameterNames shouldBe empty
    query.parameterValues shouldBe empty
  }
  // TODO: moderakhs add unit test for all spark filters

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

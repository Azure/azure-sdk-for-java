// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.sources.{AlwaysFalse, AlwaysTrue, EqualTo, Filter, In}
import org.assertj.core.api.Assertions.assertThat
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class FilterAnalyzerSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "many filters" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](
      EqualTo("physicist", "Schrodinger"), In("isCatAlive", Array(true, false)))
    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos.toIterable should contain theSameElementsAs filters.toList

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['physicist']=@param0 AND r['isCatAlive'] IN (@param1,@param2)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1", "@param2")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", true, false)
  }

  "in" should "be translated to cosmos in predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](In("physicist", Array("Schrodinger", "Hawking")))
    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['physicist'] IN (@param0,@param1)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger", "Hawking")
  }

  "= on number" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("age", 5))
    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['age']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List(5)
  }

  "= on utf8" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("mathematician", "خوارزمی"))

    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['mathematician']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("خوارزمی")
  }

  "nested filter" should "be translated to nested cosmos json filter" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](
      EqualTo("mathematician", "خوارزمی"),
      EqualTo("mathematician.book", "Algorithmo de Numero Indorum"),
      In("mathematician.work", Array("Algebra", "Algorithm")))

    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty
    analyzedQuery.filtersToBePushedDownToCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['mathematician']=@param0" +
      " AND r['mathematician']['book']=@param1" +
      " AND r['mathematician']['work'] IN (@param2,@param3)"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0", "@param1", "@param2", "@param3")
    query.parameterValues should contain theSameElementsInOrderAs
      List("خوارزمی",
      "Algorithmo de Numero Indorum",
      "Algebra",
      "Algorithm")
  }

  "no filter" should "be translated to cosmos query with no where clause" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter]()
    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos shouldBe empty

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r"
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue)
    val analyzedQuery = filterProcessor.analyze(filters)
    analyzedQuery.filtersToBePushedDownToCosmos shouldBe empty
    analyzedQuery.filtersNotSupportedByCosmos should contain theSameElementsInOrderAs filters

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r")
    query.parameterNames.toArray shouldBe empty
    query.parameterValues.toArray shouldBe empty
  }

  "unsupported filter mixed with supported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue, EqualTo("pet", "Schrodinger cat"), AlwaysFalse)
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).containsExactly(EqualTo("pet", "Schrodinger cat"))
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).containsExactlyElementsOf(Array(AlwaysTrue, AlwaysFalse).toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    query.queryTest shouldEqual "SELECT * FROM r WHERE r['pet']=@param0"
    query.parameterNames should contain theSameElementsInOrderAs List("@param0")
    query.parameterValues should contain theSameElementsInOrderAs List("Schrodinger cat")
  }
  // TODO: moderakhs add unit test for all spark filters

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

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
    assertThat(analyzedQuery.filtersNotSupportedByCosmos).isEmpty()
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos.toIterable.asJava).containsExactlyElementsOf(filters.toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['physicist']=@param0 AND r['isCatAlive'] IN (@param1,@param2)")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0", "@param1", "@param2")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("Schrodinger", true, false)
  }

  "in" should "be translated to cosmos in predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](In("physicist", Array("Schrodinger", "Hawking")))
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersNotSupportedByCosmos).isEmpty()
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos.toIterable.asJava).containsExactlyElementsOf(filters.toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['physicist'] IN (@param0,@param1)")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0", "@param1")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("Schrodinger", "Hawking")
  }

  "= on number" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("age", 5))
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersNotSupportedByCosmos).isEmpty()
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos.toIterable.asJava).containsExactlyElementsOf(filters.toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['age']=@param0")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly(5)
  }

  "= on utf8" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](EqualTo("mathematician", "خوارزمی"))

    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersNotSupportedByCosmos).isEmpty()
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos.toIterable.asJava).containsExactlyElementsOf(filters.toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['mathematician']=@param0")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("خوارزمی")
  }

  "no filter" should "be translated to cosmos query with no where clause" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter]()
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).isEmpty()
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).isEmpty()

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r")
    assertThat(query.parameterNames.toIterable.asJava).isEmpty()
    assertThat(query.parameterValues.toIterable.asJava).isEmpty()
  }

  "unsupported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue)
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).isEmpty()
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).containsExactlyElementsOf(filters.toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r")
    assertThat(query.parameterNames.toIterable.asJava).isEmpty()
    assertThat(query.parameterValues.toIterable.asJava).isEmpty()
  }

  "unsupported filter mixed with supported filter" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterAnalyzer()

    val filters = Array[Filter](AlwaysTrue, EqualTo("pet", "Schrodinger cat"), AlwaysFalse)
    val analyzedQuery = filterProcessor.analyze(filters)
    assertThat(analyzedQuery.filtersToBePushedDownToCosmos).containsExactly(EqualTo("pet", "Schrodinger cat"))
    assertThat(analyzedQuery.filtersNotSupportedByCosmos.toIterable.asJava).containsExactlyElementsOf(Array(AlwaysTrue, AlwaysFalse).toList.asJava)

    val query = analyzedQuery.cosmosParametrizedQuery
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['pet']=@param0")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("Schrodinger cat")
  }
  // TODO: moderakhs add unit test for all spark filters

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

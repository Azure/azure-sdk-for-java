// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.sources.{EqualTo, Filter, In}
import org.assertj.core.api.Assertions.assertThat
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class FilterProcessorSpec extends UnitSpec {
  //scalastyle:off multiple.string.literals
  //scalastyle:off magic.number

  "= on number" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterProcessor()

    val query = filterProcessor.processFilters(Array[Filter](EqualTo("age", 5)))

    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['age']=@param0")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly(5)
  }
  "= on utf8" should "be translated to cosmos = predicate" in {
    val filterProcessor = FilterProcessor()

    val query = filterProcessor.processFilters(Array[Filter](EqualTo("mathematicianName", "خوارزمی")))
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['mathematicianName']=@param0")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("خوارزمی")
  }

  "in" should "be translated to cosmos in predicate" in {
    val filterProcessor = FilterProcessor()

    val query = filterProcessor.processFilters(Array[Filter](In("physicist", Array("Schrödinger", "Hawking"))))
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['physicist'] IN (@param0,@param1)")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0", "@param1")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("Schrödinger", "Hawking")
  }

  "many filters" should "be translated to cosmos predicates with AND" in {
    val filterProcessor = FilterProcessor()

    val query = filterProcessor.processFilters(Array[Filter](
      EqualTo("mathematicianName", "خوارزمی"), In("physicist", Array("Schrödinger", "Hawking"))))
    assertThat(query.queryTest).isEqualTo("SELECT * FROM r WHERE r['mathematicianName']=@param0 AND r['physicist'] IN (@param1,@param2)")
    assertThat(query.parameterNames.toIterable.asJava).containsOnly("@param0", "@param1", "@param2")
    assertThat(query.parameterValues.toIterable.asJava).containsOnly("خوارزمی", "Schrödinger", "Hawking")
  }

  // TODO: moderakhs, once integrated to the query pipeline DataSource V2 we should add unit test for all spark filters

  //scalastyle:on magic.number
  //scalastyle:on multiple.string.literals
}

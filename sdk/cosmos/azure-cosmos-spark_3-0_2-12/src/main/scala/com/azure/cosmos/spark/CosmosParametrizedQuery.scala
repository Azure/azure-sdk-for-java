// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

case class CosmosParametrizedQuery(val queryTest : String,
                                   val parameterNames: List[String],
                                   val parameterValues: List[Any])
  extends Serializable {
  def toSqlQuerySpec() : SqlQuerySpec = {
    new SqlQuerySpec(queryTest, new SqlParameter(parameterNames.asJava.get(0), parameterValues.asJava.get(0)))
  }
}


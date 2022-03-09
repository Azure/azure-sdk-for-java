// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

// SqlQuerySpec is not serializable we need a serializable wrapper
private[cosmos] case class CosmosParameterizedQuery(queryText: String,
                                                    parameterNames: List[String],
                                                    parameterValues: List[Any])
  extends Serializable {
  def toSqlQuerySpec: SqlQuerySpec = {
    new SqlQuerySpec(queryText, parameterNames.zip(parameterValues)
      .map(param => new SqlParameter(param._1, param._2))
      .asJava)
  }
}

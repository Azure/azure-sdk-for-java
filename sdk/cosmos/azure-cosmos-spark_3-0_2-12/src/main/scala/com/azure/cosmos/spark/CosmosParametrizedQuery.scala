// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models

case class CosmosParametrizedQuery(val queryTest: String,
                                   val parameterNames: List[String],
                                   val parameterValues: List[Any])
  extends Serializable {
  def toSqlQuerySpec(): SqlQuerySpec = {
    new SqlQuerySpec(queryTest, new SqlParameter(parameterNames.zip(parameterValues) {
      case (parameterName, parameterValue) => new SqlParameter(parameterName, parameterValue)
    }))
  }
}


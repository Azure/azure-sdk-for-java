// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParametrizedQuery
import org.apache.spark.sql.sources.{
  And, EqualNullSafe, EqualTo, Filter, GreaterThan,
  GreaterThanOrEqual, In, IsNotNull, IsNull, LessThan, LessThanOrEqual, Not, Or,
  StringContains, StringEndsWith, StringStartsWith
}

import scala.collection.mutable.ListBuffer

case class FilterProcessor() {

  def processFilters(filters: Array[Filter]): CosmosParametrizedQuery = {
    val queryBuilder = new StringBuilder
    queryBuilder.append("SELECT * FROM r")
    val list = ListBuffer[(String, Any)]()

    if (filters.size > 0) {
      queryBuilder.append(" WHERE ")
      filters.zipWithIndex.foreach{ case (filter, index) =>
          appendCosmosQueryPredicate(queryBuilder, list, filter)
          if (index < filters.size - 1 ) {
            queryBuilder.append(" AND ")
          }
      }
    }

    CosmosParametrizedQuery(queryBuilder.toString(), list.map(f => f._1).toList, list.map(f => f._2).toList)
  }

  /**
    * Provides Json Field path prefixed by the root. For example: "r['id']
    * @param sparkFilterColumnName
    * @return cosmosFieldpath
    */
  private def canonicalCosmosFieldPath(sparkFilterColumnName: String): String = {
    val result = new StringBuilder(FilterProcessor.rootName)
    sparkFilterColumnName.split('.').foreach(cNamePart => result.append(s"['${normalizedFieldName(cNamePart)}']"))
    result.toString
  }

  /**
    * Parameter name in the parametrized query: e.g. @param1.
    * @param paramNumber
    * @return
    */
  private def paramName(paramNumber: Integer): String = {
    s"@param$paramNumber"
  }

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  // scalastyle:off multiple.string.literals
  private def appendCosmosQueryPredicate(queryBuilder: StringBuilder,
                                         list: scala.collection.mutable.ListBuffer[(String, Any)],
                                         filter: Filter): Unit = {
    val pName = paramName(list.size)
    filter match {
      case EqualTo(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("=").append(pName)
        list.append((pName, value))

      case EqualNullSafe(attr, value) =>
        // TODO moderakh check the difference between EqualTo and EqualNullSafe
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("=").append(pName)
        list.append((pName, value))

      case LessThan(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("<").append(pName)
        list.append((pName, value))

      case GreaterThan(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append(">").append(pName)
        list.append((pName, value))

      case LessThanOrEqual(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("<=").append(pName)
        list.append((pName, value))

      case GreaterThanOrEqual(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append(">=").append(pName)
        list.append((pName, value))

      case In(attr, values) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append(" IN ")
        queryBuilder.append("(")
        queryBuilder.append(
          values.map(
            value => {
              val pName = paramName(list.size)
              list.append((pName, value))
              pName
            }
          ).mkString(","))
        queryBuilder.append(")")

      case StringStartsWith(attr, value: String) =>
        queryBuilder.append("STARTSWITH(").append(canonicalCosmosFieldPath(attr)).append(pName).append(")")
        list.append((pName, value))

      case StringEndsWith(attr, value: String) =>
        queryBuilder.append("ENDSWITH(").append(canonicalCosmosFieldPath(attr)).append(pName).append(")")
        list.append((pName, value))

      case StringContains(attr, value: String) =>
        queryBuilder.append("CONTAINS(").append(canonicalCosmosFieldPath(attr)).append(pName).append(")")
        list.append((pName, value))

      case IsNull(attr) =>
        queryBuilder.append(s"IS_NULL(${canonicalCosmosFieldPath(attr)})")

      case IsNotNull(attr) =>
        queryBuilder.append(s"NOT(IS_NULL(${canonicalCosmosFieldPath(attr)}))")

      case And(leftFilter, rightFilter) =>
        queryBuilder.append(s"(${appendCosmosQueryPredicate(queryBuilder, list, leftFilter)}" +
          s" AND ${appendCosmosQueryPredicate(queryBuilder, list, rightFilter)})")

      case Or(leftFilter, rightFilter) =>
        queryBuilder.append(s"(${appendCosmosQueryPredicate(queryBuilder, list, leftFilter)}" +
          s" OR ${appendCosmosQueryPredicate(queryBuilder, list, rightFilter)})")

      case Not(childFilter) =>
        queryBuilder.append(s"NOT(${appendCosmosQueryPredicate(queryBuilder, list, childFilter)})")

      case _: Filter =>
      // TODO: moderakh we should collect unsupported ones and pass to DataSource v2
      // as unsupported filters, then the filters will be applied by the spark platform itself
    }
  }

  // scalastyle:on multiple.string.literals
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private def normalizedFieldName(jsonFieldName: String): String = {
    // TODO: moderakh what happens if jsonFieldName has "[" in it? we need to escape it?
    jsonFieldName
  }
}

object FilterProcessor {
  private val rootName = "r"
}
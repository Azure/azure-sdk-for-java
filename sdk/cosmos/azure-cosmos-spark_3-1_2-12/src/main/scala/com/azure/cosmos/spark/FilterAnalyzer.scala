// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParameterizedQuery
import org.apache.spark.sql.sources.{
  And, EqualNullSafe, EqualTo, Filter, GreaterThan,
  GreaterThanOrEqual, In, IsNotNull, IsNull, LessThan, LessThanOrEqual, Not, Or,
  StringContains, StringEndsWith, StringStartsWith
}

import scala.collection.mutable.ListBuffer

private case class FilterAnalyzer() {
  // TODO: moderakh it is worth looking at DOM/AST:
  // https://github.com/Azure/azure-cosmos-dotnet-v3/tree/master/Microsoft.Azure.Cosmos/src/SqlObjects
  // https://github.com/Azure/azure-sdk-for-java/pull/17789#discussion_r530574888
  def analyze(filters: Array[Filter], cosmosReadConfig: CosmosReadConfig): AnalyzedFilters = {

    if (cosmosReadConfig.customQuery.isDefined) {
      AnalyzedFilters(
        cosmosReadConfig.customQuery.get,
        Array.empty[Filter],
        filters)
    } else {
      val queryBuilder = new StringBuilder
      queryBuilder.append("SELECT * FROM r")
      val list = ListBuffer[(String, Any)]()

      val filtersToBePushedDownToCosmos = ListBuffer[Filter]()
      val filtersNotSupportedByCosmos = ListBuffer[Filter]()

      val whereClauseBuilder = new StringBuilder

      for (filter <- filters) {
        val filterAsCosmosPredicate = new StringBuilder()
        val canBePushedDownToCosmos = appendCosmosQueryPredicate(filterAsCosmosPredicate, list, filter)
        if (canBePushedDownToCosmos) {
          if (filtersToBePushedDownToCosmos.nonEmpty) {
            whereClauseBuilder.append(" AND ")
          }
          filtersToBePushedDownToCosmos.append(filter)
          whereClauseBuilder.append(filterAsCosmosPredicate)
        } else {
          filtersNotSupportedByCosmos.append(filter)
        }
      }

      if (whereClauseBuilder.nonEmpty) {
        queryBuilder.append(" WHERE ")
        queryBuilder.append(whereClauseBuilder)
      }

      AnalyzedFilters(
        CosmosParameterizedQuery(queryBuilder.toString(), list.map(f => f._1).toList, list.map(f => f._2).toList),
        filtersToBePushedDownToCosmos.toArray,
        filtersNotSupportedByCosmos.toArray)
    }
  }

  /**
    * Provides Json Field path prefixed by the root. For example: "r['id']
    * @param sparkFilterColumnName the column name of the filter
    * @return cosmosFieldpath
    */
  private def canonicalCosmosFieldPath(sparkFilterColumnName: String): String = {
    val result = new StringBuilder(FilterAnalyzer.rootName)
    sparkFilterColumnName.split('.').foreach(cNamePart => result.append(s"['${normalizedFieldName(cNamePart)}']"))
    result.toString
  }

  /**
    * Parameter name in the parametrized query: e.g. @param1.
    * @param paramNumber parameter index
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
                                         filter: Filter): Boolean = {
    val pName = paramName(list.size)
    filter match {
      case EqualTo(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("=").append(pName)
        list.append((pName, value))
        true

      case EqualNullSafe(attr, value) =>
        // TODO moderakh check the difference between EqualTo and EqualNullSafe
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("=").append(pName)
        list.append((pName, value))
        true

      case LessThan(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("<").append(pName)
        list.append((pName, value))
        true

      case GreaterThan(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append(">").append(pName)
        list.append((pName, value))
        true

      case LessThanOrEqual(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append("<=").append(pName)
        list.append((pName, value))
        true

      case GreaterThanOrEqual(attr, value) =>
        queryBuilder.append(canonicalCosmosFieldPath(attr)).append(">=").append(pName)
        list.append((pName, value))
        true

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
        true

      case StringStartsWith(attr, value: String) =>
        queryBuilder.append("STARTSWITH(").append(canonicalCosmosFieldPath(attr)).append(",").append(pName).append(")")
        list.append((pName, value))
        true

      case StringEndsWith(attr, value: String) =>
        queryBuilder.append("ENDSWITH(").append(canonicalCosmosFieldPath(attr)).append(",").append(pName).append(")")
        list.append((pName, value))
        true

      case StringContains(attr, value: String) =>
        queryBuilder.append("CONTAINS(").append(canonicalCosmosFieldPath(attr)).append(",").append(pName).append(")")
        list.append((pName, value))
        true

      case IsNull(attr) =>
        queryBuilder.append(s"(IS_NULL(${canonicalCosmosFieldPath(attr)}) OR NOT(IS_DEFINED(${canonicalCosmosFieldPath(attr)})))")
        true

      case IsNotNull(attr) =>
        queryBuilder.append(s"(NOT(IS_NULL(${canonicalCosmosFieldPath(attr)})) AND IS_DEFINED(${canonicalCosmosFieldPath(attr)}))")
        true

      case And(leftFilter, rightFilter) =>
        queryBuilder.append("(")
        val isLeftCosmosPredicate = appendCosmosQueryPredicate(queryBuilder, list, leftFilter)
        queryBuilder.append(" AND ")
        val isRightCosmosPredicate = appendCosmosQueryPredicate(queryBuilder, list, rightFilter)
        queryBuilder.append(")")
        isLeftCosmosPredicate && isRightCosmosPredicate

      case Or(leftFilter, rightFilter) =>
        queryBuilder.append("(")
        val isLeftCosmosPredicate = appendCosmosQueryPredicate(queryBuilder, list, leftFilter)
        queryBuilder.append(" OR ")
        val isRightCosmosPredicate = appendCosmosQueryPredicate(queryBuilder, list, rightFilter)
        queryBuilder.append(")")
        isLeftCosmosPredicate && isRightCosmosPredicate

      case Not(childFilter) =>
        queryBuilder.append("NOT(")
        val isInnerCosmosPredicate = appendCosmosQueryPredicate(queryBuilder, list, childFilter)
        queryBuilder.append(")")
        isInnerCosmosPredicate

      case _: Filter =>
        // the unsupported filter will be applied by the spark platform itself.
        // TODO: moderakh how count, avg, min, max are pushed down? or orderby?
        // spark 3.0 does not support aggregate push downs, but spark 3.1 will
        // https://issues.apache.org/jira/browse/SPARK-22390
        // https://github.com/apache/spark/pull/29695/files
        false
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

private object FilterAnalyzer {
  private val rootName = "r"
}

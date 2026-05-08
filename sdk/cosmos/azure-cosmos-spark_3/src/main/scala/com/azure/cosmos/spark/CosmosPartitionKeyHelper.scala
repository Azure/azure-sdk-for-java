// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.routing.PartitionKeyInternal
import com.azure.cosmos.implementation.{ImplementationBridgeHelpers, Utils}
import com.azure.cosmos.models.{PartitionKey, PartitionKeyBuilder}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosPartitionKeyHelper extends BasicLoggingTrait {
  private[spark] val HierarchicalPartitionKeyNoneHandlingErrorMessage =
    s"The configuration '${CosmosConfigNames.ReadManyByPkNullHandling}=None' is not supported for " +
      "hierarchical partition keys because PartitionKey.NONE can't be used with multiple paths. " +
      "Use 'Null' for explicit JSON null values, filter out rows with missing partition key " +
      "components, or provide fully-defined hierarchical partition keys."

  private[spark] def validateNoneHandlingForPartitionKeyComponentCount(
    componentCount: Int,
    treatNullAsNone: Boolean): Unit = {
    if (treatNullAsNone && componentCount > 1) {
      throw new IllegalArgumentException(HierarchicalPartitionKeyNoneHandlingErrorMessage)
    }
  }

  // pattern will be recognized
  // pk(partitionKeyValue)
  //
  // (?i) : The whole matching is case-insensitive
  // pk[(](.*)[)]: partitionKey Value
  private val cosmosPartitionKeyStringRegx = """(?i)^pk\((.*)\)$""".r
  private val objectMapper = Utils.getSimpleObjectMapper

  def getCosmosPartitionKeyValueString(partitionKeyValue: List[Object]): String = {
    s"pk(${objectMapper.writeValueAsString(partitionKeyValue.asJava)})"
  }

  def tryParsePartitionKey(cosmosPartitionKeyString: String): Option[PartitionKey] =
    tryParsePartitionKey(cosmosPartitionKeyString, treatNullAsNone = false)

  /**
   * Parses a pk(...) string into a [[PartitionKey]].
   *
   * When treatNullAsNone is true, any JSON null components in the serialized array are mapped to
   * [[PartitionKeyBuilder.addNoneValue()]] (meaning the document field is absent/undefined).
   * When false, they are mapped to [[PartitionKeyBuilder.addNullValue()]] (JSON null value).
   * This matches the spark.cosmos.read.readManyByPk.nullHandling config for the non-UDF column path.
   */
  def tryParsePartitionKey(
                            cosmosPartitionKeyString: String,
                            treatNullAsNone: Boolean): Option[PartitionKey] = {
    require(cosmosPartitionKeyString != null, "Argument 'cosmosPartitionKeyString' must not be null.")
    cosmosPartitionKeyString match {
      case cosmosPartitionKeyStringRegx(pkValue) =>
        scala.util.Try(Utils.parse(pkValue, classOf[Object])).toOption.flatMap {
          case arrayList: util.ArrayList[Object @unchecked] =>
            val components = arrayList.toArray
            if (components.exists(_ == null)) {
              validateNoneHandlingForPartitionKeyComponentCount(components.length, treatNullAsNone)
              // Build via PartitionKeyBuilder so nulls can be disambiguated between
              // JSON-null (addNullValue) and undefined (addNoneValue) based on config.
              val builder = new PartitionKeyBuilder()
              components.foreach {
                case null =>
                  if (treatNullAsNone) builder.addNoneValue() else builder.addNullValue()
                case s: String => builder.add(s)
                case n: java.lang.Number => builder.add(n.doubleValue())
                case b: java.lang.Boolean => builder.add(b.booleanValue())
                case other =>
                  throw new IllegalArgumentException(
                    s"Unsupported partition key component type '${other.getClass.getName}' with value '$other'. " +
                      "Supported types are String, Number (integral or floating-point), Boolean, and null.")
              }
              Some(builder.build())
            } else {
              Some(
                ImplementationBridgeHelpers
                  .PartitionKeyHelper
                  .getPartitionKeyAccessor
                  .toPartitionKey(PartitionKeyInternal.fromObjectArray(components, false)))
            }
          case other => Some(new PartitionKey(other))
        }
      case _ => None
    }
  }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.SchemaConversionModes.SchemaConversionMode
import com.fasterxml.jackson.annotation.JsonInclude.Include
// scalastyle:off underscore.import
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import org.apache.spark.sql.types._
// scalastyle:on underscore.import

import scala.util.{Try, Success, Failure}

// scalastyle:off
private[cosmos] object CosmosRowConverter {

    // TODO: Expose configuration to handle duplicate fields
    // See: https://github.com/Azure/azure-sdk-for-java/pull/18642#discussion_r558638474
    private val rowConverterMap = new TrieMap[CosmosSerializationConfig, CosmosRowConverter]

    def get(serializationConfig: CosmosSerializationConfig): CosmosRowConverter = {
        rowConverterMap.get(serializationConfig) match {
            case Some(existingRowConverter) => existingRowConverter
            case None =>
                val newRowConverterCandidate = createRowConverter(serializationConfig)
                rowConverterMap.putIfAbsent(serializationConfig, newRowConverterCandidate) match {
                    case Some(existingConcurrentlyCreatedRowConverter) => existingConcurrentlyCreatedRowConverter
                    case None => newRowConverterCandidate
                }
        }
    }

    private def createRowConverter(serializationConfig: CosmosSerializationConfig): CosmosRowConverter = {
        val objectMapper = new ObjectMapper()
        import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule)
        serializationConfig.serializationInclusionMode match {
            case SerializationInclusionModes.NonNull => objectMapper.setSerializationInclusion(Include.NON_NULL)
            case SerializationInclusionModes.NonEmpty => objectMapper.setSerializationInclusion(Include.NON_EMPTY)
            case SerializationInclusionModes.NonDefault => objectMapper.setSerializationInclusion(Include.NON_DEFAULT)
            case _ => objectMapper.setSerializationInclusion(Include.ALWAYS)
        }

        new CosmosRowConverter(objectMapper, serializationConfig)
    }
}

private[cosmos] class CosmosRowConverter(private val objectMapper: ObjectMapper, private val serializationConfig: CosmosSerializationConfig)
    extends CosmosRowConverterBase(objectMapper, serializationConfig) {

    override def convertSparkDataTypeToJsonNodeConditionallyForSparkRuntimeSpecificDataType
    (
        fieldType: DataType,
        rowData: Any
    ): Option[JsonNode] = {
        fieldType match {
            case TimestampNTZType if rowData.isInstanceOf[java.time.LocalDateTime] => convertToJsonNodeConditionally(rowData.asInstanceOf[java.time.LocalDateTime].toString)
            case _ =>
                throw new Exception(s"Cannot cast $rowData into a Json value. $fieldType has no matching Json value.")
        }
    }

    override def convertSparkDataTypeToJsonNodeNonNullForSparkRuntimeSpecificDataType(fieldType: DataType, rowData: Any): JsonNode = {
        fieldType match {
            case TimestampNTZType if rowData.isInstanceOf[java.time.LocalDateTime] => objectMapper.convertValue(rowData.asInstanceOf[java.time.LocalDateTime].toString, classOf[JsonNode])
            case _ =>
                throw new Exception(s"Cannot cast $rowData into a Json value. $fieldType has no matching Json value.")
        }
    }

    override def convertToSparkDataTypeForSparkRuntimeSpecificDataType
    (dataType: DataType,
     value: JsonNode,
     schemaConversionMode: SchemaConversionMode): Any =
        (value, dataType) match {
            case (_, _: TimestampNTZType) => handleConversionErrors(() => toTimestampNTZ(value), schemaConversionMode)
            case _ =>
                throw new IllegalArgumentException(
                    s"Unsupported datatype conversion [Value: $value] of ${value.getClass}] to $dataType]")
        }


    def toTimestampNTZ(value: JsonNode): LocalDateTime = {
        value match {
            case isJsonNumber() => LocalDateTime.parse(value.asText())
            case textNode: TextNode =>
                parseDateTimeNTZFromString(textNode.asText()) match {
                    case Some(odt) => odt
                    case None =>
                        throw new IllegalArgumentException(
                            s"Value '${textNode.asText()} cannot be parsed as LocalDateTime (TIMESTAMP_NTZ).")
                }
            case _ => LocalDateTime.parse(value.asText())
        }
    }

    private def handleConversionErrors[A] = (conversion: () => A,
                                             schemaConversionMode: SchemaConversionMode) => {
        Try(conversion()) match {
            case Success(convertedValue) => convertedValue
            case Failure(error) =>
                if (schemaConversionMode == SchemaConversionModes.Relaxed) {
                    null
                }
                else {
                    throw error
                }
        }
    }

    def parseDateTimeNTZFromString(value: String): Option[LocalDateTime] = {
        try {
            val odt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
            Some(odt)
        }
        catch {
            case _: Exception =>
                try {
                    val odt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                    Some(odt) 
                }
                catch {
                    case _: Exception => None
                }
        }
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation
import com.azure.cosmos.implementation.SparkRowDocument.NullObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.Row
import org.slf4j.Logger

import java.nio.ByteBuffer
import java.time.Instant
import java.{lang, util}

case class SparkRowDocument
(
  val row: Row,
  val properties: Array[Option[Any]]
) extends Document(NullObjectNode) {

  def getSparkRow(): Row = {
    row
  }

  override def setResourceId(resourceId: String): Resource = ???

  override def setSelfLink(selfLink: String): Resource = ???

  override def populatePropertyBag(): Unit = ???

  override def remove(propertyName: String): Unit = ???

  override def has(propertyName: String): Boolean = {
    propertyName match {
      case Properties.Id => properties(PropertyIndexes.Id).isDefined
      case Properties.ETag => properties(PropertyIndexes.ETag).isDefined
      case Properties.Timestamp => properties(PropertyIndexes.Timestamp).isDefined
      case Properties.SelfLink => properties(PropertyIndexes.SelfLink).isDefined
      case Properties.ResourceId=> properties(PropertyIndexes.ResourceId).isDefined
      case Properties.LSN=> properties(PropertyIndexes.LSN).isDefined
      case _ => row.schema.names.contains(propertyName)
    }
  }

  private[this] def getProperty[T](propertyName: String): Option[T] = {
    val systemPropertyValue =  propertyName match {
      case Properties.Id => properties(PropertyIndexes.Id).asInstanceOf[Option[T]]
      case Properties.ETag => properties(PropertyIndexes.ETag).asInstanceOf[Option[T]]
      case Properties.Timestamp => properties(PropertyIndexes.Timestamp).asInstanceOf[Option[T]]
      case Properties.SelfLink => properties(PropertyIndexes.SelfLink).asInstanceOf[Option[T]]
      case Properties.ResourceId=> properties(PropertyIndexes.ResourceId).asInstanceOf[Option[T]]
      case Properties.LSN=> properties(PropertyIndexes.LSN).asInstanceOf[Option[T]]
      case _ => None
    }

    if (systemPropertyValue.isDefined) {
      systemPropertyValue
    } else {
      if (row.schema.names.contains(propertyName)) {
        Some(row.get(row.fieldIndex(propertyName)).asInstanceOf[T])
      } else {
        None
      }
    }
  }

  private def getRequiredProperty[T](propertyName: String, propertyIndex: Int): T = {
    properties(propertyIndex)
      .getOrElse(row.get(row.schema.fieldIndex(propertyName)))
        .asInstanceOf[T]
  }

  private def getOptionalProperty[T](propertyName: String, propertyIndex: Int): Option[T] = {
    properties(propertyIndex)
      .getOrElse(
        if (row.schema.names.contains(propertyName)){
          Option(row.get(row.schema.fieldIndex(propertyName)).asInstanceOf[T])
        } else {
          Option.empty[T]
        }).asInstanceOf[Option[T]]
  }


  override def getResourceId: String = getRequiredProperty[String](Properties.ResourceId, PropertyIndexes.ResourceId)

  override def getMap: util.Map[String, AnyRef] = ???

  override def getSelfLink: String = getRequiredProperty[String](Properties.SelfLink, PropertyIndexes.SelfLink)

  override def getInt(propertyName: String): Integer = ???

  override def getString(propertyName: String): String = {
    getProperty[String](propertyName) match {
      case Some(text) => text
      case None => null
    }
  }

  override def getBoolean(propertyName: String): lang.Boolean = ???

  override def getLong(propertyName: String): lang.Long = ???

  override def getMap[T](propertyKey: String): util.Map[String, T] = ???

  override def getTimeToLive: Integer =
    getOptionalProperty[Integer](Properties.SelfLink, PropertyIndexes.SelfLink).getOrElse(null)

  override def getId: String = getRequiredProperty[String](Properties.Id, PropertyIndexes.Id)


  override def serializeJsonToByteBuffer(objectMapper: ObjectMapper): ByteBuffer = ???

  override def containsJsonSerializable[T](c: Class[T]): Boolean = ???

  override def getDouble(propertyName: String): lang.Double = ???

  override def serializeJsonToByteBuffer(): ByteBuffer = ???

  override def setETag(eTag: String): Resource = ???

  override def toJson(formattingPolicy: SerializationFormattingPolicy): String = ???

  override def setAltLink(altLink: String): Resource = ???

  override def setTimestamp(timestamp: Instant): Resource = ???

  override def setTimeToLive(timeToLive: Integer): Unit = ???

  override def getCollection(propertyName: String): util.Collection[ObjectNode] = ???

  override def getObjectByPath(propertyNames: util.List[String]): AnyRef = ???

  override def getObject(propertyName: String): ObjectNode = ???

  override def getPropertyBag: ObjectNode = ???

  override def getTimestamp: Instant = ???

  override def toJson: String = ???

  override def getAltLink: String = ???

  override def getETag: String = getRequiredProperty[String](Properties.ETag, PropertyIndexes.ETag)

  override def setMapper(om: ObjectMapper): Unit = ???


  override def toObject[T](c: Class[T]): T = {
    if (c.equals(classOf[SparkRowDocument])) {
      this.asInstanceOf[T]
    } else {
      ???
    }
  }

  override def getLogger: Logger = super.getLogger

  override def setId(id: String): Document = ???

  override def set[T](propertyName: String, value: T): Unit = ???
}

private object Properties {
  val Id = "id"
  val ETag = "_etag"
  val ResourceId = "_rid"
  val Timestamp = "_ts"
  val SelfLink = "_self"
  val TTL = "_ttl"
  val LSN = "_lsn"
}

private object PropertyIndexes {
  val Id = 2
  val ETag = 3
  val ResourceId = 0
  val Timestamp = 4
  val SelfLink = 1
  val TTL = 5
  val LSN = 6
}

object SparkRowDocument {
  private val NullObjectNode: ObjectNode = null

  def apply(objectNode: ObjectNode, row: Row): SparkRowDocument = {
    val properties = new Array[Option[Any]](6)
    properties(PropertyIndexes.ResourceId) = Option[JsonNode](objectNode.get(Properties.ResourceId)) match {
      case Some(jsonNode) => Some(jsonNode.asText)
      case None => None
    }

    properties(PropertyIndexes.SelfLink) = Option[JsonNode](objectNode.get(Properties.SelfLink)) match {
      case Some(jsonNode) => Some(jsonNode.asText())
      case None => None
    }

    properties(PropertyIndexes.Id) = Option[JsonNode](objectNode.get(Properties.Id)) match {
      case Some(jsonNode) => Some(jsonNode.asText())
      case None => None
    }

    properties(PropertyIndexes.ETag) = Option[JsonNode](objectNode.get(Properties.ETag)) match {
      case Some(jsonNode) => Some(jsonNode.asText())
      case None => None
    }

    properties(PropertyIndexes.Timestamp) = Option[JsonNode](objectNode.get(Properties.Timestamp)) match {
      case Some(jsonNode) => Some(jsonNode.asLong())
      case None => None
    }

    properties(PropertyIndexes.TTL) = Option[JsonNode](objectNode.get(Properties.TTL)) match {
      case Some(jsonNode) => Some(jsonNode.asInt())
      case None => None
    }

    new SparkRowDocument(row, properties)
  }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.fasterxml.jackson.databind.ObjectMapper

import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.sql.types.{
  ArrayType, BinaryType, BooleanType, DecimalType, DoubleType,
  FloatType, LongType, NullType, StringType, StructType, IntegerType
}

class CosmosTableSchemaInferrerSpec extends UnitSpec {
  //scalastyle:off null
  //scalastyle:off multiple.string.literals

  val objectMapper = new ObjectMapper()

  "string properties" should "be detected" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", "text")
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.put("id", "text")
    objectNode2.put("otherProperty", "text")
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 2
    schema.fields(0).name shouldBe "id"
    schema.fields(1).name shouldBe "otherProperty"
    schema.fields(0).dataType shouldBe StringType
    schema.fields(1).dataType shouldBe StringType
    schema.fields(0).nullable shouldBe false
    schema.fields(1).nullable shouldBe false
  }

  "string properties" should "be nullable when allowNullForInferredProperties is set" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", "text")
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.put("id", "text")
    objectNode2.put("otherProperty", "text")
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = true)
    schema.fields should have size 2
    schema.fields(0).name shouldBe "id"
    schema.fields(1).name shouldBe "otherProperty"
    schema.fields(0).dataType shouldBe StringType
    schema.fields(1).dataType shouldBe StringType
    schema.fields(0).nullable shouldBe false
    schema.fields(1).nullable shouldBe true
  }

  "null properties" should "be detected" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.putNull("someProperty")
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe NullType
    schema.fields(0).nullable shouldBe true
  }

  "boolean properties" should "be detected" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("someProperty", true)
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe BooleanType
    schema.fields(0).nullable shouldBe false
  }

  "binary properties" should "be detected" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.set("someProperty", objectNode.binaryNode("test".getBytes()))
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe BinaryType
    schema.fields(0).nullable shouldBe false
  }

  "numeric properties" should "be detected" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colName3 = "testCol3"
    val colName4 = "testCol4"
    val colName5 = "testCol5"

    val colVal1: Double = 3.5
    val colVal2: Float = 1e14f
    val colVal3: Long = 1000000
    val colVal4: java.math.BigDecimal = new java.math.BigDecimal(4.6)
    val colVal5: Int = 10

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put(colName1, colVal1)
    objectNode.put(colName2, colVal2)
    objectNode.put(colName3, colVal3)
    objectNode.put(colName4, colVal4)
    objectNode.put(colName5, colVal5)
    val docs = List[ObjectNode](objectNode)


    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 5
    schema(colName1).dataType shouldBe DoubleType
    schema(colName2).dataType shouldBe FloatType
    schema(colName3).dataType shouldBe LongType
    schema(colName4).dataType shouldBe DecimalType(DecimalType.MAX_PRECISION, DecimalType.MAX_SCALE)
    schema(colName5).dataType shouldBe IntegerType
    schema(colName1).nullable shouldBe false
    schema(colName2).nullable shouldBe false
    schema(colName3).nullable shouldBe false
    schema(colName4).nullable shouldBe false
    schema(colName5).nullable shouldBe false
  }

  "array properties" should "be detected" in {
    val colName1 = "testCol1"
    val colVal1: Array[String] = Array("element1", "element2")
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val arrayObjectNode = objectMapper.createArrayNode()
    colVal1.foreach(elem => arrayObjectNode.add(elem))
    objectNode.set(colName1, arrayObjectNode)
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe ArrayType(StringType)
    schema.fields(0).nullable shouldBe false
  }

  "nested objectNode properties" should "be detected" in {
    val colName1 = "testCol1"
    val colName2 = "testCol2"
    val colVal1 = "testVal1"
    val colVal2 = "testVal2"

    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val nestedObjectNode: ObjectNode = objectNode.putObject(colName1)
    nestedObjectNode.put(colName1, colVal1)
    objectNode.put(colName2, colVal2)
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 2
    schema.fields(0).dataType.asInstanceOf[StructType].fields should have size 1
    schema.fields(0).dataType.asInstanceOf[StructType].fields(0).dataType shouldBe StringType
    schema.fields(1).dataType shouldBe StringType
    schema.fields(0).nullable shouldBe false
    schema.fields(1).nullable shouldBe false
  }

  it should "map duplicate properties with different types to StringType" in {
    val idVal1 = 20
    val idVal2 = true
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.put("id", idVal2)
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe StringType
    schema.fields(0).nullable shouldBe false
  }

  it should "map duplicate properties with same type to original type" in {
    val idVal1 = 20
    val idVal2 = 30
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.put("id", idVal2)
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe IntegerType
    schema.fields(0).nullable shouldBe false
  }

  it should "map duplicate properties when one is nullable" in {
    val idVal1 = 20
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.putNull("id")
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe IntegerType
    schema.fields(0).nullable shouldBe true
  }

  it should "map duplicate properties when the other is nullable" in {
    val idVal1 = 20
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.putNull("id")
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    objectNode2.put("id", idVal1)
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe IntegerType
    schema.fields(0).nullable shouldBe true
  }

  it should "map unsupported properties to StringType" in {
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val initValue = 5
    val colVal = new MyPOJO(initValue)
    val colName = "test"
    objectNode.putPOJO(colName, colVal)
    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe StringType
    schema.fields(0).nullable shouldBe false
  }

  it should "map nested objects properties when objects have different properties" in {
    val nodeValue = 10
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val subNode = objectNode.putObject("id")
    subNode.put("prop1", "test")
    subNode.put("prop3", 10.13)
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    val subNode2 = objectNode2.putObject("id")
    subNode2.put("prop2", "test")
    subNode2.put("prop3", nodeValue)
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)

    schema.fields should have size 1

    val subSchema = schema("id").dataType.asInstanceOf[StructType]
    subSchema.fields should have size 3
    subSchema("prop1").dataType shouldBe StringType
    subSchema("prop2").dataType shouldBe StringType
    subSchema("prop3").dataType shouldBe DoubleType
  }

  it should "map array properties when objects have different arrays" in {
    val nodeValue = 10
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    val subNode = objectNode.putArray("id")
    subNode.add(10.3)
    val objectNode2: ObjectNode = objectMapper.createObjectNode()
    val subNode2 = objectNode2.putArray("id")
    subNode2.add(nodeValue)
    val docs = List[ObjectNode](objectNode, objectNode2)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = true, allowNullForInferredProperties = false)

    schema.fields should have size 1

    val subSchema = schema("id").dataType.asInstanceOf[ArrayType]
    subSchema.elementType shouldBe DoubleType
  }

  it should "include timestamp" in {
    val idVal1 = 20
    val etagVal = "etag"
    val selfVal = "self"
    val ridVal = "rid"
    val attachmentVal = "attachments"
    val tsVal: Long = 1000000000
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    objectNode.put(CosmosTableSchemaInferrer.ETagAttributeName, etagVal)
    objectNode.put(CosmosTableSchemaInferrer.ResourceIdAttributeName, ridVal)
    objectNode.put(CosmosTableSchemaInferrer.SelfAttributeName, selfVal)
    objectNode.put(CosmosTableSchemaInferrer.AttachmentsAttributeName, attachmentVal)
    objectNode.put(CosmosTableSchemaInferrer.TimestampAttributeName, tsVal)

    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = false, includeTimestamp = true, allowNullForInferredProperties = false)
    schema.fields should have size 2
    schema.fields(schema.fieldIndex("id")).dataType shouldBe IntegerType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.TimestampAttributeName)).dataType shouldBe LongType
    schema.fields(schema.fieldIndex("id")).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.TimestampAttributeName)).nullable shouldBe false
  }

  it should "ignore system properties and timestamp" in {
    val idVal1 = 20
    val etagVal = "etag"
    val selfVal = "self"
    val ridVal = "rid"
    val attachmentVal = "attachments"
    val tsVal: Long = 1000000000
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    objectNode.put(CosmosTableSchemaInferrer.ETagAttributeName, etagVal)
    objectNode.put(CosmosTableSchemaInferrer.ResourceIdAttributeName, ridVal)
    objectNode.put(CosmosTableSchemaInferrer.SelfAttributeName, selfVal)
    objectNode.put(CosmosTableSchemaInferrer.AttachmentsAttributeName, attachmentVal)
    objectNode.put(CosmosTableSchemaInferrer.TimestampAttributeName, tsVal)

    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = false, includeTimestamp = false, allowNullForInferredProperties = false)
    schema.fields should have size 1
    schema.fields(0).dataType shouldBe IntegerType
    schema.fields(0).nullable shouldBe false
  }

  it should "not ignore system properties and timestamp" in {
    val idVal1 = 20
    val etagVal = "etag"
    val selfVal = "self"
    val ridVal = "rid"
    val attachmentVal = "attachments"
    val tsVal: Long = 1000000000
    val objectNode: ObjectNode = objectMapper.createObjectNode()
    objectNode.put("id", idVal1)
    objectNode.put(CosmosTableSchemaInferrer.ETagAttributeName, etagVal)
    objectNode.put(CosmosTableSchemaInferrer.ResourceIdAttributeName, ridVal)
    objectNode.put(CosmosTableSchemaInferrer.SelfAttributeName, selfVal)
    objectNode.put(CosmosTableSchemaInferrer.AttachmentsAttributeName, attachmentVal)
    objectNode.put(CosmosTableSchemaInferrer.TimestampAttributeName, tsVal)

    val docs = List[ObjectNode](objectNode)

    val schema = CosmosTableSchemaInferrer.inferSchema(
      docs, includeSystemProperties = true, includeTimestamp = false, allowNullForInferredProperties = false)
    schema.fields should have size 6
    schema.fields(schema.fieldIndex("id")).dataType shouldBe IntegerType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.ETagAttributeName)).dataType shouldBe StringType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.ResourceIdAttributeName)).dataType shouldBe StringType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.SelfAttributeName)).dataType shouldBe StringType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.AttachmentsAttributeName)).dataType shouldBe StringType
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.TimestampAttributeName)).dataType shouldBe LongType
    schema.fields(schema.fieldIndex("id")).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.ETagAttributeName)).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.ResourceIdAttributeName)).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.SelfAttributeName)).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.AttachmentsAttributeName)).nullable shouldBe false
    schema.fields(schema.fieldIndex(CosmosTableSchemaInferrer.TimestampAttributeName)).nullable shouldBe false
  }

  private class MyPOJO(value: Int)

}

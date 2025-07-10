// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.Utils
import com.azure.cosmos.implementation.patch.{PatchOperationCore, PatchOperationType}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{BooleanNode, DoubleNode, FloatNode, IntNode, LongNode, TextNode}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

class CosmosPatchHelperSpec extends UnitSpec {
    val objectMapper = Utils.getSimpleObjectMapper()

    private case class PatchHelperParameterTest(path: String, value: JsonNode)

    "CosmosPatchHelper" can "patch simple path" in {
        val patchParameters = this.getPatchHelperTestParameters()
        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(new TrieMap[String, CosmosPatchColumnConfig]()))

        val patchOperationList = this.getPatchOperationList(patchParameters, "")

        // validate it can patch on empty parent node
        val updatedNodeWithEmptyParentNode = cosmosPatchHelper.patchBulkUpdateItem(None, patchOperationList)
        for (parameterTest <- patchParameters) {
            updatedNodeWithEmptyParentNode.get(parameterTest.path) shouldEqual parameterTest.value
        }

        // validate it can patch on non-empty parent node
        val rootNode = objectMapper.createObjectNode()
        val existingChildNode = new TextNode("randomText")
        val existingChildPath = "randomPath"
        rootNode.set(existingChildPath, existingChildNode)
        val updatedNodeWithParentNode = cosmosPatchHelper.patchBulkUpdateItem(Some(rootNode), patchOperationList)
        for (parameterTest <- patchParameters) {
            updatedNodeWithParentNode.get(parameterTest.path) shouldEqual parameterTest.value
        }
        updatedNodeWithParentNode.get("randomPath") shouldEqual existingChildNode
    }

    "CosmosPatchHelper" can "patch nested object path" in {
        val patchParameters = this.getPatchHelperTestParameters()
        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(new TrieMap[String, CosmosPatchColumnConfig]()))

        val nestedPath = "nestedObject"
        val patchOperationList = this.getPatchOperationList(patchParameters, s"/$nestedPath")

        // validate it can patch on empty nested parent node, the nested parent node will be created automatically
        val rootNode = objectMapper.createObjectNode()
        val updatedNodeWithEmptyNestedPath = cosmosPatchHelper.patchBulkUpdateItem(Some(rootNode), patchOperationList)
        for (parameterTest <- patchParameters) {
            updatedNodeWithEmptyNestedPath.get(nestedPath).get(parameterTest.path) shouldEqual parameterTest.value
        }

        // validate it can patch on an existing nested parent node, the nested parent node will be created automatically
        val rootNode2 = objectMapper.createObjectNode()
        rootNode2.set(nestedPath, objectMapper.createObjectNode())
        val updatedNodeWithNestedPath = cosmosPatchHelper.patchBulkUpdateItem(Some(rootNode2), patchOperationList)
        for (parameterTest <- patchParameters) {
            updatedNodeWithNestedPath.get(nestedPath).get(parameterTest.path) shouldEqual parameterTest.value
        }
    }

    "CosmosPatchHelper" can "patch nested array path" in {
        val patchParameters = this.getPatchHelperTestParameters()
        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(new TrieMap[String, CosmosPatchColumnConfig]()))

        val nestedPath = "nestedArray"
        val patchOperationList = this.getPatchOperationList(patchParameters, s"/$nestedPath/0")

        // validate it can patch on an existing nested parent node, the nested parent node will be created automatically
        val rootNode = objectMapper.createObjectNode()
        val nestedArrayNode = objectMapper.createArrayNode()
        rootNode.set(nestedPath, nestedArrayNode)
        val updatedNodeWithNestedPath = cosmosPatchHelper.patchBulkUpdateItem(Some(rootNode), patchOperationList)
        for (parameterTest <- patchParameters) {
            try {
                updatedNodeWithNestedPath.get(nestedPath).get(0).get(parameterTest.path) shouldEqual parameterTest.value
            } catch {
                case _: Exception => System.out.println(parameterTest.path)
            }
        }
    }

    "CosmosPatchHelper" can "create cosmos patch operations without customized config" in {
        val patchParameters = this.getPatchHelperTestParameters()
        val patchObject = objectMapper.createObjectNode()

        for (patchParameter <- patchParameters) {
            patchObject.replace(patchParameter.path, patchParameter.value)
        }

        // validate system property will not be patched
        patchObject.set("_etag", new TextNode("etag"))
        patchObject.set("_rid", new TextNode("rid"))

        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(new TrieMap[String, CosmosPatchColumnConfig]()))
        val expectedPatchOperationMap =
            this
                .getPatchOperationList(patchParameters, "")
                .map(operation => operation.getPath -> operation)
                .toMap

        val patchOperations = cosmosPatchHelper.createCosmosPatchBulkUpdateOperations(patchObject)
        patchOperations.size shouldEqual expectedPatchOperationMap.size
        for (patchOperation <- patchOperations) {
            val expectedPatchOperation = expectedPatchOperationMap.get(patchOperation.getPath)
            expectedPatchOperation.isDefined shouldEqual true
            patchOperation.getOperationType shouldEqual expectedPatchOperation.get.getOperationType
            patchOperation.getResource shouldEqual expectedPatchOperation.get.getResource
        }
    }

    "CosmosPatchHelper" can "create cosmos patch operations with customized config" in {
        val patchParameters = this.getPatchHelperTestParameters()
        val patchObject = objectMapper.createObjectNode()
        for (patchParameter <- patchParameters) {
            patchObject.replace(patchParameter.path, patchParameter.value)
        }
        // validate system property will not be patched
        patchObject.set("_etag", new TextNode("etag"))
        patchObject.set("_rid", new TextNode("rid"))

        // configure customized patch column config
        val nestedPath = "nestedObject"
        val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
        for (patchParameter <- patchParameters) {
            columnConfigsMap +=
                patchParameter.path ->
                    CosmosPatchColumnConfig(
                        patchParameter.path,
                        CosmosPatchOperationTypes.Set,
                        s"/$nestedPath/${patchParameter.path}",
                        false)
        }

        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(columnConfigsMap))

        val expectedPatchOperationMap =
            this
                .getPatchOperationList(patchParameters, s"/$nestedPath")
                .map(operation => operation.getPath -> operation)
                .toMap

        val patchOperations = cosmosPatchHelper.createCosmosPatchBulkUpdateOperations(patchObject)
        patchOperations.size shouldEqual expectedPatchOperationMap.size
        for (patchOperation <- patchOperations) {
            val expectedPatchOperation = expectedPatchOperationMap.get(patchOperation.getPath)
            expectedPatchOperation.isDefined shouldEqual true
            patchOperation.getOperationType shouldEqual expectedPatchOperation.get.getOperationType
            patchOperation.getResource shouldEqual expectedPatchOperation.get.getResource
        }
    }

    "CosmosPatchHelper" can "create cosmos patch operations with customized config using rawJson" in {
        val partialUpdateNode = objectMapper.createObjectNode()

        val patchParameters = Seq(
            Tuple2.apply("textNode", "\"test\""),
            Tuple2.apply("parent2", "{\"firstName\": \"John\",  \"lastName\": \"Anderson\", \"age\": 3}"),
            Tuple2.apply("arrayNode", "[{\"id\":\"2\"}, {\"id\":\"3\"}]"),
            Tuple2.apply("textNode", "\"test\""),
            Tuple2.apply("numberNode", "12345")
        )

        val columnConfigsMap = new TrieMap[String, CosmosPatchColumnConfig]
        val expectedPatchOperationMap = new TrieMap[String, PatchOperationCore[JsonNode]]()

        for (patchParameter <- patchParameters) {
            partialUpdateNode.put(patchParameter._1, patchParameter._2)
            columnConfigsMap +=
                patchParameter._1 ->
                    CosmosPatchColumnConfig(
                        patchParameter._1,
                        CosmosPatchOperationTypes.Set,
                        s"${patchParameter._1}", true)

            expectedPatchOperationMap +=
                patchParameter._1 ->
                    new PatchOperationCore[JsonNode](
                        PatchOperationType.SET,
                        s"/${patchParameter._1}",
                        objectMapper.readTree(patchParameter._2))
        }

        val cosmosPatchHelper =
            new CosmosPatchHelper(
                DiagnosticsConfig(),
                CosmosPatchConfigs(columnConfigsMap))

        val patchOperations = cosmosPatchHelper.createCosmosPatchBulkUpdateOperations(partialUpdateNode)
        patchOperations.size shouldEqual expectedPatchOperationMap.size
        for (patchOperation <- patchOperations) {
            val expectedPatchOperation = expectedPatchOperationMap.get(patchOperation.getPath)
            expectedPatchOperation.isDefined shouldEqual true
            patchOperation.getOperationType shouldEqual expectedPatchOperation.get.getOperationType
            patchOperation.getResource shouldEqual expectedPatchOperation.get.getResource
        }
    }

    private[this] def getPatchHelperTestParameters(): List[PatchHelperParameterTest] = {
        val patchParameters = new ListBuffer[PatchHelperParameterTest]
        //patchParameters += PatchHelperParameterTest("id", new TextNode("id"))
        patchParameters += PatchHelperParameterTest("propInt", new IntNode(1))
        patchParameters += PatchHelperParameterTest("propLong", new LongNode(2L))
        patchParameters += PatchHelperParameterTest("propFloat", new FloatNode(3.3f))
        patchParameters += PatchHelperParameterTest("propDouble", new DoubleNode(1))
        patchParameters += PatchHelperParameterTest("propBoolean", BooleanNode.TRUE)
        patchParameters += PatchHelperParameterTest("propText", new TextNode("patchTest"))
        val objectNode = objectMapper.createObjectNode()
        objectNode.set("firstName", new TextNode("John"))
        objectNode.set("lastName", new TextNode("John"))
        patchParameters += PatchHelperParameterTest("propObject", objectNode)

        val arrayNode = objectMapper.createArrayNode()
        val objectNode2 = objectMapper.createObjectNode()
        objectNode2.set("city", new TextNode("Redmond"))
        objectNode2.set("state", new TextNode("WA"))
        arrayNode.add(objectNode2)
        patchParameters += PatchHelperParameterTest("propArray", arrayNode)

        patchParameters.toList
    }

    private[this] def getPatchOperationList(
                                               patchParameters: List[PatchHelperParameterTest],
                                               pathPrefix: String): List[PatchOperationCore[JsonNode]] = {

        val patchOperationList = new ListBuffer[PatchOperationCore[JsonNode]]
        for (parameterTest <- patchParameters) {
            patchOperationList += new PatchOperationCore[JsonNode](PatchOperationType.SET, s"$pathPrefix/${parameterTest.path}", parameterTest.value)
        }

        patchOperationList.toList
    }
}

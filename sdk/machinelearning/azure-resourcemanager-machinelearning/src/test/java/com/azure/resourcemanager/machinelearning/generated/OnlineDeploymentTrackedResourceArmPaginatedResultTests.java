// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.machinelearning.fluent.models.OnlineDeploymentInner;
import com.azure.resourcemanager.machinelearning.models.EgressPublicNetworkAccessType;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.OnlineDeploymentProperties;
import com.azure.resourcemanager.machinelearning.models.OnlineDeploymentTrackedResourceArmPaginatedResult;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class OnlineDeploymentTrackedResourceArmPaginatedResultTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        OnlineDeploymentTrackedResourceArmPaginatedResult model =
            BinaryData
                .fromString(
                    "{\"nextLink\":\"rprsnm\",\"value\":[{\"identity\":{\"principalId\":\"4da85e2a-d4e0-4d4c-95ab-041badbdf8b9\",\"tenantId\":\"16df482c-d39e-4be2-9abe-ddba471908e5\",\"type\":\"SystemAssigned,UserAssigned\",\"userAssignedIdentities\":{}},\"kind\":\"lbkpb\",\"properties\":{\"endpointComputeType\":\"OnlineDeploymentProperties\",\"appInsightsEnabled\":false,\"egressPublicNetworkAccess\":\"Enabled\",\"instanceType\":\"jh\",\"model\":\"vechndbnwiehole\",\"modelMountPath\":\"wiuub\",\"provisioningState\":\"Succeeded\",\"description\":\"aqtferr\",\"environmentId\":\"ex\",\"environmentVariables\":{},\"properties\":{}},\"sku\":{\"name\":\"xap\",\"tier\":\"Free\",\"size\":\"qqnobpudcd\",\"family\":\"tqwpwya\",\"capacity\":2036163978},\"location\":\"sqbuc\",\"tags\":{\"sdaultxij\":\"kyexaoguyaipi\",\"qwazlnqnmcjngzq\":\"um\",\"sf\":\"qxtbjwgnyf\"},\"id\":\"svtui\",\"name\":\"zh\",\"type\":\"jqg\"},{\"identity\":{\"principalId\":\"0ddd2460-1f6c-4ca7-a3bb-8be70bc2fc6a\",\"tenantId\":\"a55475da-8d74-41aa-b7bd-fc2075054554\",\"type\":\"UserAssigned\",\"userAssignedIdentities\":{}},\"kind\":\"qryxyn\",\"properties\":{\"endpointComputeType\":\"OnlineDeploymentProperties\",\"appInsightsEnabled\":true,\"egressPublicNetworkAccess\":\"Disabled\",\"instanceType\":\"sovwxznptgoeiyb\",\"model\":\"pfhvfslk\",\"modelMountPath\":\"tjlrigjksky\",\"provisioningState\":\"Creating\",\"description\":\"sxwaabzm\",\"environmentId\":\"rygznmmaxriz\",\"environmentVariables\":{},\"properties\":{}},\"sku\":{\"name\":\"gopxlhslnelxie\",\"tier\":\"Basic\",\"size\":\"llxecwc\",\"family\":\"jphslhcaw\",\"capacity\":224494261},\"location\":\"fdwfmvigorqj\",\"tags\":{\"n\":\"zhraglkafh\"},\"id\":\"juj\",\"name\":\"ickpz\",\"type\":\"cpopmxel\"},{\"identity\":{\"principalId\":\"0f14930d-5df5-4097-9b70-8c82d3a4bfcb\",\"tenantId\":\"bbf6d45e-ad6b-4e4a-97f7-a7c3941dcbf1\",\"type\":\"SystemAssigned\",\"userAssignedIdentities\":{}},\"kind\":\"edexxmlfm\",\"properties\":{\"endpointComputeType\":\"OnlineDeploymentProperties\",\"appInsightsEnabled\":true,\"egressPublicNetworkAccess\":\"Enabled\",\"instanceType\":\"uawxtzx\",\"model\":\"mwabzxrvxc\",\"modelMountPath\":\"hsphaivmxyas\",\"provisioningState\":\"Deleting\",\"description\":\"ywakoihk\",\"environmentId\":\"mjblmljhlny\",\"environmentVariables\":{},\"properties\":{}},\"sku\":{\"name\":\"qyryuzcbmqqvxm\",\"tier\":\"Standard\",\"size\":\"tayx\",\"family\":\"supe\",\"capacity\":2074822274},\"location\":\"qnhcvsqltnzoibg\",\"tags\":{\"qoxwd\":\"nxfyqonm\"},\"id\":\"fdbxiqxeiiqbim\",\"name\":\"tmwwi\",\"type\":\"h\"},{\"identity\":{\"principalId\":\"8b9a4105-dd9b-43ff-91e1-ba1a28c9f57b\",\"tenantId\":\"2a050a31-f610-475f-948f-53b51c0d42ca\",\"type\":\"SystemAssigned\",\"userAssignedIdentities\":{}},\"kind\":\"vwbcblembnkbwv\",\"properties\":{\"endpointComputeType\":\"OnlineDeploymentProperties\",\"appInsightsEnabled\":false,\"egressPublicNetworkAccess\":\"Enabled\",\"instanceType\":\"vqihebwtswbzuwf\",\"model\":\"ragegi\",\"modelMountPath\":\"cjfelisdjubgg\",\"provisioningState\":\"Failed\",\"description\":\"sazgakgacyrcmj\",\"environmentId\":\"spofapvuhry\",\"environmentVariables\":{},\"properties\":{}},\"sku\":{\"name\":\"frzgbzjed\",\"tier\":\"Premium\",\"size\":\"vnlvxbcuiiznktwf\",\"family\":\"snvpdibmi\",\"capacity\":1037622433},\"location\":\"bzbkiw\",\"tags\":{\"ophzfylsgcrp\":\"n\",\"fwyfwlwxjwet\":\"bcunezzceze\",\"zvaylptrsqqw\":\"psihcla\"},\"id\":\"tcmwqkchc\",\"name\":\"waxfewzjkj\",\"type\":\"xfdeqvhpsyl\"}]}")
                .toObject(OnlineDeploymentTrackedResourceArmPaginatedResult.class);
        Assertions.assertEquals("rprsnm", model.nextLink());
        Assertions.assertEquals("sqbuc", model.value().get(0).location());
        Assertions.assertEquals("kyexaoguyaipi", model.value().get(0).tags().get("sdaultxij"));
        Assertions
            .assertEquals(
                ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, model.value().get(0).identity().type());
        Assertions.assertEquals("lbkpb", model.value().get(0).kind());
        Assertions.assertEquals("aqtferr", model.value().get(0).properties().description());
        Assertions.assertEquals("ex", model.value().get(0).properties().environmentId());
        Assertions.assertEquals(false, model.value().get(0).properties().appInsightsEnabled());
        Assertions
            .assertEquals(
                EgressPublicNetworkAccessType.ENABLED, model.value().get(0).properties().egressPublicNetworkAccess());
        Assertions.assertEquals("jh", model.value().get(0).properties().instanceType());
        Assertions.assertEquals("vechndbnwiehole", model.value().get(0).properties().model());
        Assertions.assertEquals("wiuub", model.value().get(0).properties().modelMountPath());
        Assertions.assertEquals("xap", model.value().get(0).sku().name());
        Assertions.assertEquals(SkuTier.FREE, model.value().get(0).sku().tier());
        Assertions.assertEquals("qqnobpudcd", model.value().get(0).sku().size());
        Assertions.assertEquals("tqwpwya", model.value().get(0).sku().family());
        Assertions.assertEquals(2036163978, model.value().get(0).sku().capacity());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        OnlineDeploymentTrackedResourceArmPaginatedResult model =
            new OnlineDeploymentTrackedResourceArmPaginatedResult()
                .withNextLink("rprsnm")
                .withValue(
                    Arrays
                        .asList(
                            new OnlineDeploymentInner()
                                .withLocation("sqbuc")
                                .withTags(
                                    mapOf("sdaultxij", "kyexaoguyaipi", "qwazlnqnmcjngzq", "um", "sf", "qxtbjwgnyf"))
                                .withIdentity(
                                    new ManagedServiceIdentity()
                                        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                                        .withUserAssignedIdentities(mapOf()))
                                .withKind("lbkpb")
                                .withProperties(
                                    new OnlineDeploymentProperties()
                                        .withDescription("aqtferr")
                                        .withEnvironmentId("ex")
                                        .withEnvironmentVariables(mapOf())
                                        .withProperties(mapOf())
                                        .withAppInsightsEnabled(false)
                                        .withEgressPublicNetworkAccess(EgressPublicNetworkAccessType.ENABLED)
                                        .withInstanceType("jh")
                                        .withModel("vechndbnwiehole")
                                        .withModelMountPath("wiuub"))
                                .withSku(
                                    new Sku()
                                        .withName("xap")
                                        .withTier(SkuTier.FREE)
                                        .withSize("qqnobpudcd")
                                        .withFamily("tqwpwya")
                                        .withCapacity(2036163978)),
                            new OnlineDeploymentInner()
                                .withLocation("fdwfmvigorqj")
                                .withTags(mapOf("n", "zhraglkafh"))
                                .withIdentity(
                                    new ManagedServiceIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentities(mapOf()))
                                .withKind("qryxyn")
                                .withProperties(
                                    new OnlineDeploymentProperties()
                                        .withDescription("sxwaabzm")
                                        .withEnvironmentId("rygznmmaxriz")
                                        .withEnvironmentVariables(mapOf())
                                        .withProperties(mapOf())
                                        .withAppInsightsEnabled(true)
                                        .withEgressPublicNetworkAccess(EgressPublicNetworkAccessType.DISABLED)
                                        .withInstanceType("sovwxznptgoeiyb")
                                        .withModel("pfhvfslk")
                                        .withModelMountPath("tjlrigjksky"))
                                .withSku(
                                    new Sku()
                                        .withName("gopxlhslnelxie")
                                        .withTier(SkuTier.BASIC)
                                        .withSize("llxecwc")
                                        .withFamily("jphslhcaw")
                                        .withCapacity(224494261)),
                            new OnlineDeploymentInner()
                                .withLocation("qnhcvsqltnzoibg")
                                .withTags(mapOf("qoxwd", "nxfyqonm"))
                                .withIdentity(
                                    new ManagedServiceIdentity()
                                        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                                        .withUserAssignedIdentities(mapOf()))
                                .withKind("edexxmlfm")
                                .withProperties(
                                    new OnlineDeploymentProperties()
                                        .withDescription("ywakoihk")
                                        .withEnvironmentId("mjblmljhlny")
                                        .withEnvironmentVariables(mapOf())
                                        .withProperties(mapOf())
                                        .withAppInsightsEnabled(true)
                                        .withEgressPublicNetworkAccess(EgressPublicNetworkAccessType.ENABLED)
                                        .withInstanceType("uawxtzx")
                                        .withModel("mwabzxrvxc")
                                        .withModelMountPath("hsphaivmxyas"))
                                .withSku(
                                    new Sku()
                                        .withName("qyryuzcbmqqvxm")
                                        .withTier(SkuTier.STANDARD)
                                        .withSize("tayx")
                                        .withFamily("supe")
                                        .withCapacity(2074822274)),
                            new OnlineDeploymentInner()
                                .withLocation("bzbkiw")
                                .withTags(
                                    mapOf(
                                        "ophzfylsgcrp", "n", "fwyfwlwxjwet", "bcunezzceze", "zvaylptrsqqw", "psihcla"))
                                .withIdentity(
                                    new ManagedServiceIdentity()
                                        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                                        .withUserAssignedIdentities(mapOf()))
                                .withKind("vwbcblembnkbwv")
                                .withProperties(
                                    new OnlineDeploymentProperties()
                                        .withDescription("sazgakgacyrcmj")
                                        .withEnvironmentId("spofapvuhry")
                                        .withEnvironmentVariables(mapOf())
                                        .withProperties(mapOf())
                                        .withAppInsightsEnabled(false)
                                        .withEgressPublicNetworkAccess(EgressPublicNetworkAccessType.ENABLED)
                                        .withInstanceType("vqihebwtswbzuwf")
                                        .withModel("ragegi")
                                        .withModelMountPath("cjfelisdjubgg"))
                                .withSku(
                                    new Sku()
                                        .withName("frzgbzjed")
                                        .withTier(SkuTier.PREMIUM)
                                        .withSize("vnlvxbcuiiznktwf")
                                        .withFamily("snvpdibmi")
                                        .withCapacity(1037622433))));
        model = BinaryData.fromObject(model).toObject(OnlineDeploymentTrackedResourceArmPaginatedResult.class);
        Assertions.assertEquals("rprsnm", model.nextLink());
        Assertions.assertEquals("sqbuc", model.value().get(0).location());
        Assertions.assertEquals("kyexaoguyaipi", model.value().get(0).tags().get("sdaultxij"));
        Assertions
            .assertEquals(
                ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED, model.value().get(0).identity().type());
        Assertions.assertEquals("lbkpb", model.value().get(0).kind());
        Assertions.assertEquals("aqtferr", model.value().get(0).properties().description());
        Assertions.assertEquals("ex", model.value().get(0).properties().environmentId());
        Assertions.assertEquals(false, model.value().get(0).properties().appInsightsEnabled());
        Assertions
            .assertEquals(
                EgressPublicNetworkAccessType.ENABLED, model.value().get(0).properties().egressPublicNetworkAccess());
        Assertions.assertEquals("jh", model.value().get(0).properties().instanceType());
        Assertions.assertEquals("vechndbnwiehole", model.value().get(0).properties().model());
        Assertions.assertEquals("wiuub", model.value().get(0).properties().modelMountPath());
        Assertions.assertEquals("xap", model.value().get(0).sku().name());
        Assertions.assertEquals(SkuTier.FREE, model.value().get(0).sku().tier());
        Assertions.assertEquals("qqnobpudcd", model.value().get(0).sku().size());
        Assertions.assertEquals("tqwpwya", model.value().get(0).sku().family());
        Assertions.assertEquals(2036163978, model.value().get(0).sku().capacity());
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}

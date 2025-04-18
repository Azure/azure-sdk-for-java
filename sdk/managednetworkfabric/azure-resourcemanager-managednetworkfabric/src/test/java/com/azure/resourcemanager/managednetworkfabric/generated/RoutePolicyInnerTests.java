// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.managednetworkfabric.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.managednetworkfabric.fluent.models.RoutePolicyInner;
import com.azure.resourcemanager.managednetworkfabric.models.ActionIpCommunityProperties;
import com.azure.resourcemanager.managednetworkfabric.models.ActionIpExtendedCommunityProperties;
import com.azure.resourcemanager.managednetworkfabric.models.AddressFamilyType;
import com.azure.resourcemanager.managednetworkfabric.models.CommunityActionTypes;
import com.azure.resourcemanager.managednetworkfabric.models.IpCommunityIdList;
import com.azure.resourcemanager.managednetworkfabric.models.IpExtendedCommunityIdList;
import com.azure.resourcemanager.managednetworkfabric.models.RoutePolicyActionType;
import com.azure.resourcemanager.managednetworkfabric.models.RoutePolicyConditionType;
import com.azure.resourcemanager.managednetworkfabric.models.RoutePolicyStatementProperties;
import com.azure.resourcemanager.managednetworkfabric.models.StatementActionProperties;
import com.azure.resourcemanager.managednetworkfabric.models.StatementConditionProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class RoutePolicyInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RoutePolicyInner model = BinaryData.fromString(
            "{\"properties\":{\"networkFabricId\":\"jf\",\"addressFamilyType\":\"IPv4\",\"configurationState\":\"Deprovisioning\",\"provisioningState\":\"Deleting\",\"administrativeState\":\"Enabled\",\"defaultAction\":\"Permit\",\"statements\":[{\"sequenceNumber\":4448738498634238178,\"condition\":{\"type\":\"And\",\"ipPrefixId\":\"ztirjvqxvwkiocxo\",\"ipExtendedCommunityIds\":[\"wbuocqflmnlrlqx\"],\"ipCommunityIds\":[\"ate\",\"yozdbc\",\"qnl\"]},\"action\":{\"localPreference\":2247099392682138883,\"actionType\":\"Deny\",\"ipCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}},\"ipExtendedCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}}},\"annotation\":\"hjjidodnvltc\"},{\"sequenceNumber\":9175709040224729929,\"condition\":{\"type\":\"Or\",\"ipPrefixId\":\"wkupbbnhic\",\"ipExtendedCommunityIds\":[\"zhrcqdfwbif\",\"nhlsforsimtfcqm\",\"ynb\",\"pelpfijtezgxmpe\"],\"ipCommunityIds\":[\"madlerzinfuivcz\",\"tllxsw\",\"dapsmir\"]},\"action\":{\"localPreference\":2955918462156050023,\"actionType\":\"Continue\",\"ipCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}},\"ipExtendedCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}}},\"annotation\":\"vdsp\"},{\"sequenceNumber\":2451432701721525303,\"condition\":{\"type\":\"Or\",\"ipPrefixId\":\"wtblgmkokqoi\",\"ipExtendedCommunityIds\":[\"efwlnm\",\"kffcnuestbsl\"],\"ipCommunityIds\":[\"dnccotelik\",\"iytehhxtzxqdwbym\"]},\"action\":{\"localPreference\":1174159606945652119,\"actionType\":\"Continue\",\"ipCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}},\"ipExtendedCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}}},\"annotation\":\"s\"},{\"sequenceNumber\":804072634972443064,\"condition\":{\"type\":\"Or\",\"ipPrefixId\":\"xdbyhq\",\"ipExtendedCommunityIds\":[\"vimmwc\",\"ozvlfymt\"],\"ipCommunityIds\":[\"v\",\"upqtzckj\"]},\"action\":{\"localPreference\":5472309175848968118,\"actionType\":\"Deny\",\"ipCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}},\"ipExtendedCommunityProperties\":{\"delete\":{},\"set\":{},\"add\":{}}},\"annotation\":\"xncqz\"}],\"annotation\":\"gt\"},\"location\":\"gdobimor\",\"tags\":{\"bmxqfgvz\":\"xosgihtrxue\",\"osecxlngouf\":\"jqswshesgcs\"},\"id\":\"izp\",\"name\":\"mfxzspf\",\"type\":\"vsl\"}")
            .toObject(RoutePolicyInner.class);
        Assertions.assertEquals("gdobimor", model.location());
        Assertions.assertEquals("xosgihtrxue", model.tags().get("bmxqfgvz"));
        Assertions.assertEquals("jf", model.networkFabricId());
        Assertions.assertEquals(AddressFamilyType.IPV4, model.addressFamilyType());
        Assertions.assertEquals(CommunityActionTypes.PERMIT, model.defaultAction());
        Assertions.assertEquals("hjjidodnvltc", model.statements().get(0).annotation());
        Assertions.assertEquals(4448738498634238178L, model.statements().get(0).sequenceNumber());
        Assertions.assertEquals("ate", model.statements().get(0).condition().ipCommunityIds().get(0));
        Assertions.assertEquals(RoutePolicyConditionType.AND, model.statements().get(0).condition().type());
        Assertions.assertEquals("ztirjvqxvwkiocxo", model.statements().get(0).condition().ipPrefixId());
        Assertions.assertEquals("wbuocqflmnlrlqx",
            model.statements().get(0).condition().ipExtendedCommunityIds().get(0));
        Assertions.assertEquals(2247099392682138883L, model.statements().get(0).action().localPreference());
        Assertions.assertEquals(RoutePolicyActionType.DENY, model.statements().get(0).action().actionType());
        Assertions.assertEquals("gt", model.annotation());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RoutePolicyInner model
            = new RoutePolicyInner().withLocation("gdobimor")
                .withTags(mapOf("bmxqfgvz", "xosgihtrxue", "osecxlngouf", "jqswshesgcs"))
                .withNetworkFabricId("jf")
                .withAddressFamilyType(AddressFamilyType.IPV4)
                .withDefaultAction(CommunityActionTypes.PERMIT)
                .withStatements(Arrays.asList(
                    new RoutePolicyStatementProperties().withAnnotation("hjjidodnvltc")
                        .withSequenceNumber(4448738498634238178L)
                        .withCondition(new StatementConditionProperties()
                            .withIpCommunityIds(Arrays.asList("ate", "yozdbc", "qnl"))
                            .withType(RoutePolicyConditionType.AND)
                            .withIpPrefixId("ztirjvqxvwkiocxo")
                            .withIpExtendedCommunityIds(Arrays.asList("wbuocqflmnlrlqx")))
                        .withAction(new StatementActionProperties()
                            .withLocalPreference(2247099392682138883L)
                            .withActionType(RoutePolicyActionType.DENY)
                            .withIpCommunityProperties(
                                new ActionIpCommunityProperties().withAdd(new IpCommunityIdList())
                                    .withDelete(new IpCommunityIdList())
                                    .withSet(new IpCommunityIdList()))
                            .withIpExtendedCommunityProperties(new ActionIpExtendedCommunityProperties()
                                .withAdd(new IpExtendedCommunityIdList())
                                .withDelete(new IpExtendedCommunityIdList())
                                .withSet(new IpExtendedCommunityIdList()))),
                    new RoutePolicyStatementProperties().withAnnotation("vdsp")
                        .withSequenceNumber(9175709040224729929L)
                        .withCondition(new StatementConditionProperties()
                            .withIpCommunityIds(Arrays.asList("madlerzinfuivcz", "tllxsw", "dapsmir"))
                            .withType(RoutePolicyConditionType.OR)
                            .withIpPrefixId("wkupbbnhic")
                            .withIpExtendedCommunityIds(
                                Arrays.asList("zhrcqdfwbif", "nhlsforsimtfcqm", "ynb", "pelpfijtezgxmpe")))
                        .withAction(new StatementActionProperties().withLocalPreference(2955918462156050023L)
                            .withActionType(RoutePolicyActionType.CONTINUE)
                            .withIpCommunityProperties(
                                new ActionIpCommunityProperties().withAdd(new IpCommunityIdList())
                                    .withDelete(new IpCommunityIdList())
                                    .withSet(new IpCommunityIdList()))
                            .withIpExtendedCommunityProperties(
                                new ActionIpExtendedCommunityProperties().withAdd(new IpExtendedCommunityIdList())
                                    .withDelete(new IpExtendedCommunityIdList())
                                    .withSet(new IpExtendedCommunityIdList()))),
                    new RoutePolicyStatementProperties().withAnnotation("s")
                        .withSequenceNumber(2451432701721525303L)
                        .withCondition(new StatementConditionProperties()
                            .withIpCommunityIds(Arrays.asList("dnccotelik", "iytehhxtzxqdwbym"))
                            .withType(RoutePolicyConditionType.OR)
                            .withIpPrefixId("wtblgmkokqoi")
                            .withIpExtendedCommunityIds(Arrays.asList("efwlnm", "kffcnuestbsl")))
                        .withAction(new StatementActionProperties()
                            .withLocalPreference(1174159606945652119L)
                            .withActionType(RoutePolicyActionType.CONTINUE)
                            .withIpCommunityProperties(
                                new ActionIpCommunityProperties().withAdd(new IpCommunityIdList())
                                    .withDelete(new IpCommunityIdList())
                                    .withSet(new IpCommunityIdList()))
                            .withIpExtendedCommunityProperties(new ActionIpExtendedCommunityProperties()
                                .withAdd(new IpExtendedCommunityIdList())
                                .withDelete(new IpExtendedCommunityIdList())
                                .withSet(new IpExtendedCommunityIdList()))),
                    new RoutePolicyStatementProperties().withAnnotation("xncqz")
                        .withSequenceNumber(804072634972443064L)
                        .withCondition(new StatementConditionProperties()
                            .withIpCommunityIds(Arrays.asList("v", "upqtzckj"))
                            .withType(RoutePolicyConditionType.OR)
                            .withIpPrefixId("xdbyhq")
                            .withIpExtendedCommunityIds(Arrays.asList("vimmwc", "ozvlfymt")))
                        .withAction(new StatementActionProperties().withLocalPreference(5472309175848968118L)
                            .withActionType(RoutePolicyActionType.DENY)
                            .withIpCommunityProperties(
                                new ActionIpCommunityProperties().withAdd(new IpCommunityIdList())
                                    .withDelete(new IpCommunityIdList())
                                    .withSet(new IpCommunityIdList()))
                            .withIpExtendedCommunityProperties(
                                new ActionIpExtendedCommunityProperties().withAdd(new IpExtendedCommunityIdList())
                                    .withDelete(new IpExtendedCommunityIdList())
                                    .withSet(new IpExtendedCommunityIdList())))))
                .withAnnotation("gt");
        model = BinaryData.fromObject(model).toObject(RoutePolicyInner.class);
        Assertions.assertEquals("gdobimor", model.location());
        Assertions.assertEquals("xosgihtrxue", model.tags().get("bmxqfgvz"));
        Assertions.assertEquals("jf", model.networkFabricId());
        Assertions.assertEquals(AddressFamilyType.IPV4, model.addressFamilyType());
        Assertions.assertEquals(CommunityActionTypes.PERMIT, model.defaultAction());
        Assertions.assertEquals("hjjidodnvltc", model.statements().get(0).annotation());
        Assertions.assertEquals(4448738498634238178L, model.statements().get(0).sequenceNumber());
        Assertions.assertEquals("ate", model.statements().get(0).condition().ipCommunityIds().get(0));
        Assertions.assertEquals(RoutePolicyConditionType.AND, model.statements().get(0).condition().type());
        Assertions.assertEquals("ztirjvqxvwkiocxo", model.statements().get(0).condition().ipPrefixId());
        Assertions.assertEquals("wbuocqflmnlrlqx",
            model.statements().get(0).condition().ipExtendedCommunityIds().get(0));
        Assertions.assertEquals(2247099392682138883L, model.statements().get(0).action().localPreference());
        Assertions.assertEquals(RoutePolicyActionType.DENY, model.statements().get(0).action().actionType());
        Assertions.assertEquals("gt", model.annotation());
    }

    // Use "Map.of" if available
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

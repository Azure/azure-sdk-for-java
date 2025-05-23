// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.connectedvmware.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.connectedvmware.fluent.models.VirtualNetworkInner;
import com.azure.resourcemanager.connectedvmware.models.ExtendedLocation;
import com.azure.resourcemanager.connectedvmware.models.VirtualNetworksList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class VirtualNetworksListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        VirtualNetworksList model = BinaryData.fromString(
            "{\"nextLink\":\"dlwwqfbumlkxt\",\"value\":[{\"properties\":{\"uuid\":\"fsmlmbtxhwgfw\",\"vCenterId\":\"tawc\",\"moRefId\":\"zbrhubskhudyg\",\"inventoryItemId\":\"okkqfqjbvleo\",\"moName\":\"ml\",\"customResourceName\":\"qtqzfavyv\",\"statuses\":[{\"type\":\"bar\",\"status\":\"uayjkqa\",\"reason\":\"gzslesjcbhernnti\",\"message\":\"djc\",\"severity\":\"quwrbehwag\",\"lastUpdatedAt\":\"2021-10-18T14:47:31Z\"},{\"type\":\"f\",\"status\":\"mrqemvvhmx\",\"reason\":\"rjfut\",\"message\":\"oe\",\"severity\":\"vewzcj\",\"lastUpdatedAt\":\"2021-07-10T06:35:01Z\"},{\"type\":\"cpmguaadraufact\",\"status\":\"hzovaj\",\"reason\":\"iuxxpshneekulfg\",\"message\":\"qubkw\",\"severity\":\"enr\",\"lastUpdatedAt\":\"2021-04-26T16:34:12Z\"}],\"provisioningState\":\"Created\"},\"extendedLocation\":{\"type\":\"a\",\"name\":\"juohminyflnorw\"},\"kind\":\"vwpklvxwmygdxp\",\"location\":\"pqchiszep\",\"tags\":{\"ozauorsukokwb\":\"jcrxgibbdaxcon\",\"wzsoldweyuqdunv\":\"plhlvnuuepzlrp\",\"alywjhhgdn\":\"nnrwrbiork\",\"iqndieuzaofj\":\"xmsivfomiloxggdu\"},\"id\":\"hvcyyysfg\",\"name\":\"otcubi\",\"type\":\"p\"},{\"properties\":{\"uuid\":\"pwoqo\",\"vCenterId\":\"acjekni\",\"moRefId\":\"hqvcimpevfgmblr\",\"inventoryItemId\":\"lbywdxsm\",\"moName\":\"cwrwfs\",\"customResourceName\":\"fnynszqujizdvoqy\",\"statuses\":[{\"type\":\"owbb\",\"status\":\"yavutpthjoxois\",\"reason\":\"ksbpimlqoljx\",\"message\":\"gxxlxsffgcvizq\",\"severity\":\"wlvwlyoupf\",\"lastUpdatedAt\":\"2021-07-15T15:51:01Z\"},{\"type\":\"jub\",\"status\":\"hgkfmin\",\"reason\":\"owzfttsttkt\",\"message\":\"hbq\",\"severity\":\"tx\",\"lastUpdatedAt\":\"2021-10-19T02:59:28Z\"},{\"type\":\"kxitmmqtgqqqxhr\",\"status\":\"rxcpjuisavo\",\"reason\":\"dzf\",\"message\":\"zivj\",\"severity\":\"rqttbajlkatnw\",\"lastUpdatedAt\":\"2021-05-31T00:28:24Z\"},{\"type\":\"pidkqqfkuvscxkdm\",\"status\":\"govibrxkpmloazu\",\"reason\":\"ocbgoorbteoyb\",\"message\":\"jxakv\",\"severity\":\"gslordilmyww\",\"lastUpdatedAt\":\"2021-05-30T05:31:16Z\"}],\"provisioningState\":\"Canceled\"},\"extendedLocation\":{\"type\":\"edabgyvudtjue\",\"name\":\"cihxuuwhcjyx\"},\"kind\":\"bvpa\",\"location\":\"akkud\",\"tags\":{\"tcyohpfkyrk\":\"gwjplmag\",\"nwqjnoba\":\"bdgiogsjk\",\"egfnmntfpmvmemfn\":\"yhddvia\",\"lchpodbzevwrdn\":\"zdwvvbalxl\"},\"id\":\"fukuvsjcswsmystu\",\"name\":\"uqypfcvle\",\"type\":\"chpqbmfpjba\"},{\"properties\":{\"uuid\":\"idfcxsspuunnoxyh\",\"vCenterId\":\"g\",\"moRefId\":\"drihpfhoqcaaewda\",\"inventoryItemId\":\"djvlpj\",\"moName\":\"kzbrmsgeivsiy\",\"customResourceName\":\"kdncj\",\"statuses\":[{\"type\":\"bzo\",\"status\":\"culapzwyrpgogtq\",\"reason\":\"pnylb\",\"message\":\"ajlyjtlvofqzhv\",\"severity\":\"ibyfmo\",\"lastUpdatedAt\":\"2021-08-08T19:27:36Z\"},{\"type\":\"kjpvdwxf\",\"status\":\"iivwzjbhyzsxjrka\",\"reason\":\"trnegvmnvuqeqvld\",\"message\":\"astjbkkdmflvestm\",\"severity\":\"xrrilozapee\",\"lastUpdatedAt\":\"2020-12-23T19:56:30Z\"},{\"type\":\"xlktwkuzi\",\"status\":\"slevufuztc\",\"reason\":\"yhjtqedcgzu\",\"message\":\"mmrqz\",\"severity\":\"rjvpglydzgkrvqee\",\"lastUpdatedAt\":\"2021-04-21T12:17:49Z\"},{\"type\":\"pryu\",\"status\":\"wytpzdmovz\",\"reason\":\"va\",\"message\":\"zqadf\",\"severity\":\"z\",\"lastUpdatedAt\":\"2021-05-05T00:43:37Z\"}],\"provisioningState\":\"Deleting\"},\"extendedLocation\":{\"type\":\"cx\",\"name\":\"t\"},\"kind\":\"kpvzmlq\",\"location\":\"mldgxobfirc\",\"tags\":{\"khyawfvjlboxqv\":\"kciayzri\"},\"id\":\"jlmxhomdynhd\",\"name\":\"digumbnr\",\"type\":\"auzzptjazysd\"}]}")
            .toObject(VirtualNetworksList.class);
        Assertions.assertEquals("dlwwqfbumlkxt", model.nextLink());
        Assertions.assertEquals("pqchiszep", model.value().get(0).location());
        Assertions.assertEquals("jcrxgibbdaxcon", model.value().get(0).tags().get("ozauorsukokwb"));
        Assertions.assertEquals("a", model.value().get(0).extendedLocation().type());
        Assertions.assertEquals("juohminyflnorw", model.value().get(0).extendedLocation().name());
        Assertions.assertEquals("vwpklvxwmygdxp", model.value().get(0).kind());
        Assertions.assertEquals("tawc", model.value().get(0).vCenterId());
        Assertions.assertEquals("zbrhubskhudyg", model.value().get(0).moRefId());
        Assertions.assertEquals("okkqfqjbvleo", model.value().get(0).inventoryItemId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        VirtualNetworksList model = new VirtualNetworksList().withNextLink("dlwwqfbumlkxt")
            .withValue(Arrays.asList(
                new VirtualNetworkInner().withLocation("pqchiszep")
                    .withTags(mapOf("ozauorsukokwb", "jcrxgibbdaxcon", "wzsoldweyuqdunv", "plhlvnuuepzlrp",
                        "alywjhhgdn", "nnrwrbiork", "iqndieuzaofj", "xmsivfomiloxggdu"))
                    .withExtendedLocation(new ExtendedLocation().withType("a").withName("juohminyflnorw"))
                    .withKind("vwpklvxwmygdxp")
                    .withVCenterId("tawc")
                    .withMoRefId("zbrhubskhudyg")
                    .withInventoryItemId("okkqfqjbvleo"),
                new VirtualNetworkInner().withLocation("akkud")
                    .withTags(mapOf("tcyohpfkyrk", "gwjplmag", "nwqjnoba", "bdgiogsjk", "egfnmntfpmvmemfn", "yhddvia",
                        "lchpodbzevwrdn", "zdwvvbalxl"))
                    .withExtendedLocation(new ExtendedLocation().withType("edabgyvudtjue").withName("cihxuuwhcjyx"))
                    .withKind("bvpa")
                    .withVCenterId("acjekni")
                    .withMoRefId("hqvcimpevfgmblr")
                    .withInventoryItemId("lbywdxsm"),
                new VirtualNetworkInner().withLocation("mldgxobfirc")
                    .withTags(mapOf("khyawfvjlboxqv", "kciayzri"))
                    .withExtendedLocation(new ExtendedLocation().withType("cx").withName("t"))
                    .withKind("kpvzmlq")
                    .withVCenterId("g")
                    .withMoRefId("drihpfhoqcaaewda")
                    .withInventoryItemId("djvlpj")));
        model = BinaryData.fromObject(model).toObject(VirtualNetworksList.class);
        Assertions.assertEquals("dlwwqfbumlkxt", model.nextLink());
        Assertions.assertEquals("pqchiszep", model.value().get(0).location());
        Assertions.assertEquals("jcrxgibbdaxcon", model.value().get(0).tags().get("ozauorsukokwb"));
        Assertions.assertEquals("a", model.value().get(0).extendedLocation().type());
        Assertions.assertEquals("juohminyflnorw", model.value().get(0).extendedLocation().name());
        Assertions.assertEquals("vwpklvxwmygdxp", model.value().get(0).kind());
        Assertions.assertEquals("tawc", model.value().get(0).vCenterId());
        Assertions.assertEquals("zbrhubskhudyg", model.value().get(0).moRefId());
        Assertions.assertEquals("okkqfqjbvleo", model.value().get(0).inventoryItemId());
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

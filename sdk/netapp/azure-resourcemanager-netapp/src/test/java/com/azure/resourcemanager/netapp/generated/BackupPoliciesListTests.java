// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.netapp.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.netapp.fluent.models.BackupPolicyInner;
import com.azure.resourcemanager.netapp.models.BackupPoliciesList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class BackupPoliciesListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        BackupPoliciesList model = BinaryData.fromString(
            "{\"value\":[{\"etag\":\"ggicccnxqhue\",\"properties\":{\"backupPolicyId\":\"ktt\",\"provisioningState\":\"tvlz\",\"dailyBackupsToKeep\":798208756,\"weeklyBackupsToKeep\":471536025,\"monthlyBackupsToKeep\":218184123,\"volumesAssigned\":1311146203,\"enabled\":true,\"volumeBackups\":[{\"volumeName\":\"u\",\"volumeResourceId\":\"ypbsfgytguslfead\",\"backupsCount\":358703588,\"policyEnabled\":true},{\"volumeName\":\"yhejhzisxgfp\",\"volumeResourceId\":\"olppvksrpqvujz\",\"backupsCount\":126730684,\"policyEnabled\":false}]},\"location\":\"dw\",\"tags\":{\"dl\":\"swibyr\",\"hfwpracstwit\":\"h\"},\"id\":\"khevxccedc\",\"name\":\"nmdyodnwzxl\",\"type\":\"jc\"},{\"etag\":\"hlt\",\"properties\":{\"backupPolicyId\":\"gcxn\",\"provisioningState\":\"vwxqibyqunyo\",\"dailyBackupsToKeep\":2001486807,\"weeklyBackupsToKeep\":1090974611,\"monthlyBackupsToKeep\":1597116231,\"volumesAssigned\":351776134,\"enabled\":true,\"volumeBackups\":[{\"volumeName\":\"fvpdbo\",\"volumeResourceId\":\"cizsjqlhkrribdei\",\"backupsCount\":1262026177,\"policyEnabled\":false},{\"volumeName\":\"ghvxndzwmkrefa\",\"volumeResourceId\":\"jorwkqnyhgbij\",\"backupsCount\":1741167059,\"policyEnabled\":true},{\"volumeName\":\"zs\",\"volumeResourceId\":\"bibsystawfsdjpvk\",\"backupsCount\":250834954,\"policyEnabled\":true}]},\"location\":\"kzbzkdvncjabudu\",\"tags\":{\"hmouwqlgzrfze\":\"akmokzhjjklf\",\"lbjbsyb\":\"yebizikayuh\"},\"id\":\"qwrvtldgmfp\",\"name\":\"vm\",\"type\":\"ipaslthaqfxssmwu\"},{\"etag\":\"bdsrez\",\"properties\":{\"backupPolicyId\":\"rhneuyowq\",\"provisioningState\":\"wyt\",\"dailyBackupsToKeep\":372277345,\"weeklyBackupsToKeep\":97621763,\"monthlyBackupsToKeep\":1145560346,\"volumesAssigned\":1229797985,\"enabled\":false,\"volumeBackups\":[{\"volumeName\":\"mejzanlfzxia\",\"volumeResourceId\":\"mbzonokix\",\"backupsCount\":115638191,\"policyEnabled\":false},{\"volumeName\":\"gzpfrla\",\"volumeResourceId\":\"zrnw\",\"backupsCount\":409653705,\"policyEnabled\":true},{\"volumeName\":\"pwp\",\"volumeResourceId\":\"lwbtlhf\",\"backupsCount\":901371312,\"policyEnabled\":false}]},\"location\":\"szfjvfbgofelja\",\"tags\":{\"ojnal\":\"mqhldvrii\",\"qhhahhxvrhmzkwpj\":\"hfkvtvsexsowuel\",\"ughftqsx\":\"wws\"},\"id\":\"qxujxukndxd\",\"name\":\"grjguufzd\",\"type\":\"syqtfi\"},{\"etag\":\"hbotzingamvppho\",\"properties\":{\"backupPolicyId\":\"qzudphq\",\"provisioningState\":\"vdkfwynwcvtbvk\",\"dailyBackupsToKeep\":1479821561,\"weeklyBackupsToKeep\":911796600,\"monthlyBackupsToKeep\":1206519159,\"volumesAssigned\":565781188,\"enabled\":true,\"volumeBackups\":[{\"volumeName\":\"wp\",\"volumeResourceId\":\"p\",\"backupsCount\":219155482,\"policyEnabled\":false},{\"volumeName\":\"sgvvsccyajguq\",\"volumeResourceId\":\"wygzlvdnkfxusem\",\"backupsCount\":1095077901,\"policyEnabled\":true}]},\"location\":\"hapfcqdpsqx\",\"tags\":{\"mgccelvezrypq\":\"svuo\",\"kerqwkyh\":\"mfe\",\"pg\":\"ob\"},\"id\":\"edkowepbqpcrfk\",\"name\":\"wccsnjvcdwxlpqek\",\"type\":\"tn\"}]}")
            .toObject(BackupPoliciesList.class);
        Assertions.assertEquals("dw", model.value().get(0).location());
        Assertions.assertEquals("swibyr", model.value().get(0).tags().get("dl"));
        Assertions.assertEquals(798208756, model.value().get(0).dailyBackupsToKeep());
        Assertions.assertEquals(471536025, model.value().get(0).weeklyBackupsToKeep());
        Assertions.assertEquals(218184123, model.value().get(0).monthlyBackupsToKeep());
        Assertions.assertEquals(true, model.value().get(0).enabled());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        BackupPoliciesList model = new BackupPoliciesList().withValue(Arrays.asList(
            new BackupPolicyInner().withLocation("dw")
                .withTags(mapOf("dl", "swibyr", "hfwpracstwit", "h"))
                .withDailyBackupsToKeep(798208756)
                .withWeeklyBackupsToKeep(471536025)
                .withMonthlyBackupsToKeep(218184123)
                .withEnabled(true),
            new BackupPolicyInner().withLocation("kzbzkdvncjabudu")
                .withTags(mapOf("hmouwqlgzrfze", "akmokzhjjklf", "lbjbsyb", "yebizikayuh"))
                .withDailyBackupsToKeep(2001486807)
                .withWeeklyBackupsToKeep(1090974611)
                .withMonthlyBackupsToKeep(1597116231)
                .withEnabled(true),
            new BackupPolicyInner().withLocation("szfjvfbgofelja")
                .withTags(mapOf("ojnal", "mqhldvrii", "qhhahhxvrhmzkwpj", "hfkvtvsexsowuel", "ughftqsx", "wws"))
                .withDailyBackupsToKeep(372277345)
                .withWeeklyBackupsToKeep(97621763)
                .withMonthlyBackupsToKeep(1145560346)
                .withEnabled(false),
            new BackupPolicyInner().withLocation("hapfcqdpsqx")
                .withTags(mapOf("mgccelvezrypq", "svuo", "kerqwkyh", "mfe", "pg", "ob"))
                .withDailyBackupsToKeep(1479821561)
                .withWeeklyBackupsToKeep(911796600)
                .withMonthlyBackupsToKeep(1206519159)
                .withEnabled(true)));
        model = BinaryData.fromObject(model).toObject(BackupPoliciesList.class);
        Assertions.assertEquals("dw", model.value().get(0).location());
        Assertions.assertEquals("swibyr", model.value().get(0).tags().get("dl"));
        Assertions.assertEquals(798208756, model.value().get(0).dailyBackupsToKeep());
        Assertions.assertEquals(471536025, model.value().get(0).weeklyBackupsToKeep());
        Assertions.assertEquals(218184123, model.value().get(0).monthlyBackupsToKeep());
        Assertions.assertEquals(true, model.value().get(0).enabled());
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

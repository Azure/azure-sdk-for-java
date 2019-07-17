// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Undefined;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PartitionKeyHashingTests {

    @Test(groups = "unit")
    public void effectivePartitionKeyHashV1() {
        HashMap<Object, String> keyToEffectivePartitionKeyString = new HashMap<Object, String>() {{
            put("", "05C1CF33970FF80800");
            put("partitionKey", "05C1E1B3D9CD2608716273756A756A706F4C667A00");
            put(new String(new char[1024]).replace("\0", "a"), "05C1EB5921F706086262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626262626200");
            put(null, "05C1ED45D7475601");
            put(NullNode.getInstance(), "05C1ED45D7475601");
            put(Undefined.Value(), "05C1D529E345DC00");
            put(true, "05C1D7C5A903D803");
            put(false, "05C1DB857D857C02");
            put(Byte.MIN_VALUE, "05C1D73349F54C053FA0");
            put(Byte.MAX_VALUE, "05C1DD539DDFCC05C05FE0");
            put(Long.MIN_VALUE, "05C1DB35F33D1C053C20");
            put(Long.MAX_VALUE, "05C1B799AB2DD005C3E0");
            put(Integer.MIN_VALUE, "05C1DFBF252BCC053E20");
            put(Integer.MAX_VALUE, "05C1E1F503DFB205C1DFFFFFFFFC");
            put(Double.MIN_VALUE, "05C1E5C91F4D3005800101010101010102");
            put(Double.MAX_VALUE, "05C1CBE367C53005FFEFFFFFFFFFFFFFFE");
        }};

        for (Map.Entry<Object, String> entry : keyToEffectivePartitionKeyString.entrySet()) {
            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            partitionKeyDef.kind(PartitionKind.HASH);
            partitionKeyDef.paths(Arrays.asList(new String[]{"\\id"}));
            String actualEffectiveKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(new PartitionKey(entry.getKey()).getInternalPartitionKey(),partitionKeyDef, true);
            assertThat(entry.getValue()).isEqualTo(actualEffectiveKeyString);
        }
    }

    @Test(groups = "unit")
    public void effectivePartitionKeyHashV2() {
        HashMap<Object, String> keyToEffectivePartitionKeyString = new HashMap<Object, String>() {{
            put("", "32E9366E637A71B4E710384B2F4970A0");
            put("partitionKey", "013AEFCF77FA271571CF665A58C933F1");
            put(new String(new char[1024]).replace("\0", "a"), "332BDF5512AE49615F32C7D98C2DB86C");
            put(null, "378867E4430E67857ACE5C908374FE16");
            put(NullNode.getInstance(), "378867E4430E67857ACE5C908374FE16");
            put(Undefined.Value(), "11622DAA78F835834610ABE56EFF5CB5");
            put(true, "0E711127C5B5A8E4726AC6DD306A3E59");
            put(false, "2FE1BE91E90A3439635E0E9E37361EF2");
            put(Byte.MIN_VALUE, "01DAEDABF913540367FE219B2AD06148");
            put(Byte.MAX_VALUE, "0C507ACAC853ECA7977BF4CEFB562A25");
            put(Long.MIN_VALUE, "23D5C6395512BDFEAFADAD15328AD2BB");
            put(Long.MAX_VALUE, "2EDB959178DFCCA18983F89384D1629B");
            put(Integer.MIN_VALUE, "0B1660D5233C3171725B30D4A5F4CC1F");
            put(Integer.MAX_VALUE, "2D9349D64712AEB5EB1406E2F0BE2725");
            put(Double.MIN_VALUE, "0E6CBA63A280927DE485DEF865800139");
            put(Double.MAX_VALUE, "31424D996457102634591FF245DBCC4D");
        }};

        for (Map.Entry<Object, String> entry : keyToEffectivePartitionKeyString.entrySet()) {
            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            partitionKeyDef.kind(PartitionKind.HASH);
            partitionKeyDef.version(PartitionKeyDefinitionVersion.V2);
            partitionKeyDef.paths(Arrays.asList(new String[]{"\\id"}));
            String actualEffectiveKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(new PartitionKey(entry.getKey()).getInternalPartitionKey(),partitionKeyDef, true);
            assertThat(entry.getValue()).isEqualTo(actualEffectiveKeyString);
        }
    }

    @Test(groups = "unit")
    public void hashV2PartitionKeyDeserialization() {
        String partitionKeyDefinitionStr = "{\"paths\":[\"/pk\"],\"kind\":\"Hash\",\"version\":2}";
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition(partitionKeyDefinitionStr);
        assertThat(partitionKeyDef.version()).isEqualTo(PartitionKeyDefinitionVersion.V2);
        assertThat(partitionKeyDef.kind()).isEqualTo(PartitionKind.HASH);
        assertThat(partitionKeyDef.paths().toArray()[0]).isEqualTo("/pk");
    }

    @Test(groups = "unit")
    public void hashV1PartitionKeyDeserialization() {
        String partitionKeyDefinitionStr = "{\"paths\":[\"/pk\"],\"kind\":\"Hash\"}";
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition(partitionKeyDefinitionStr);
        assertThat(partitionKeyDef.version()).isNull();
        assertThat(partitionKeyDef.kind()).isEqualTo(PartitionKind.HASH);
        assertThat(partitionKeyDef.paths().toArray()[0]).isEqualTo("/pk");
    }
}

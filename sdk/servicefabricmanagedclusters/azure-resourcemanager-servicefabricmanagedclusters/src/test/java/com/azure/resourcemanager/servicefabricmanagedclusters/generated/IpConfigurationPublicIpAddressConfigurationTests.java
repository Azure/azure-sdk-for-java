// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicefabricmanagedclusters.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpConfigurationPublicIpAddressConfiguration;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpTag;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PublicIpAddressVersion;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class IpConfigurationPublicIpAddressConfigurationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        IpConfigurationPublicIpAddressConfiguration model = BinaryData.fromString(
            "{\"name\":\"ithtywu\",\"ipTags\":[{\"ipTagType\":\"bihwqknfdnt\",\"tag\":\"jchrdgoihxumw\"},{\"ipTagType\":\"ton\",\"tag\":\"zj\"}],\"publicIPAddressVersion\":\"IPv4\"}")
            .toObject(IpConfigurationPublicIpAddressConfiguration.class);
        Assertions.assertEquals("ithtywu", model.name());
        Assertions.assertEquals("bihwqknfdnt", model.ipTags().get(0).ipTagType());
        Assertions.assertEquals("jchrdgoihxumw", model.ipTags().get(0).tag());
        Assertions.assertEquals(PublicIpAddressVersion.IPV4, model.publicIpAddressVersion());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        IpConfigurationPublicIpAddressConfiguration model
            = new IpConfigurationPublicIpAddressConfiguration().withName("ithtywu")
                .withIpTags(Arrays.asList(new IpTag().withIpTagType("bihwqknfdnt").withTag("jchrdgoihxumw"),
                    new IpTag().withIpTagType("ton").withTag("zj")))
                .withPublicIpAddressVersion(PublicIpAddressVersion.IPV4);
        model = BinaryData.fromObject(model).toObject(IpConfigurationPublicIpAddressConfiguration.class);
        Assertions.assertEquals("ithtywu", model.name());
        Assertions.assertEquals("bihwqknfdnt", model.ipTags().get(0).ipTagType());
        Assertions.assertEquals("jchrdgoihxumw", model.ipTags().get(0).tag());
        Assertions.assertEquals(PublicIpAddressVersion.IPV4, model.publicIpAddressVersion());
    }
}

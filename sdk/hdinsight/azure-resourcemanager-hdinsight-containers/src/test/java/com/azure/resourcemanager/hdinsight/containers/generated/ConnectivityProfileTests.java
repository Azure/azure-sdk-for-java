// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.containers.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.hdinsight.containers.models.ConnectivityProfile;
import com.azure.resourcemanager.hdinsight.containers.models.ConnectivityProfileWeb;
import com.azure.resourcemanager.hdinsight.containers.models.SshConnectivityEndpoint;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class ConnectivityProfileTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ConnectivityProfile model = BinaryData.fromString(
            "{\"web\":{\"fqdn\":\"eli\",\"privateFqdn\":\"nr\"},\"ssh\":[{\"endpoint\":\"o\",\"privateSshEndpoint\":\"bnxknalaulppg\"},{\"endpoint\":\"dtpnapnyiropuhp\",\"privateSshEndpoint\":\"vpgylgqgitxmed\"},{\"endpoint\":\"v\",\"privateSshEndpoint\":\"lynqwwncwzzh\"}]}")
            .toObject(ConnectivityProfile.class);
        Assertions.assertEquals("eli", model.web().fqdn());
        Assertions.assertEquals("nr", model.web().privateFqdn());
        Assertions.assertEquals("o", model.ssh().get(0).endpoint());
        Assertions.assertEquals("bnxknalaulppg", model.ssh().get(0).privateSshEndpoint());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ConnectivityProfile model
            = new ConnectivityProfile().withWeb(new ConnectivityProfileWeb().withFqdn("eli").withPrivateFqdn("nr"))
                .withSsh(Arrays.asList(
                    new SshConnectivityEndpoint().withEndpoint("o").withPrivateSshEndpoint("bnxknalaulppg"),
                    new SshConnectivityEndpoint().withEndpoint("dtpnapnyiropuhp")
                        .withPrivateSshEndpoint("vpgylgqgitxmed"),
                    new SshConnectivityEndpoint().withEndpoint("v").withPrivateSshEndpoint("lynqwwncwzzh")));
        model = BinaryData.fromObject(model).toObject(ConnectivityProfile.class);
        Assertions.assertEquals("eli", model.web().fqdn());
        Assertions.assertEquals("nr", model.web().privateFqdn());
        Assertions.assertEquals("o", model.ssh().get(0).endpoint());
        Assertions.assertEquals("bnxknalaulppg", model.ssh().get(0).privateSshEndpoint());
    }
}

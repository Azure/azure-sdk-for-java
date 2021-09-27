// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class MetadataPolicyClientTests extends PurviewAccountClientTestBase {
    private MetadataPolicyClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewMetadataClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildMetadataPolicyClient());
    }

    @Test
    public void testGetAccount() {
        List<BinaryData> response = client.listAll(null).stream().collect(Collectors.toList());
        System.out.println(response);
    }
}

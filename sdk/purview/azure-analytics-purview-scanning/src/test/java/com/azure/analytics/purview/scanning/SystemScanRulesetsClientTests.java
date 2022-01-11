// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemScanRulesetsClientTests extends PurviewScanningClientTestBase {
    private SystemScanRulesetsClient client;

    @Override
    protected void beforeTest() {
        PurviewScanningClientBuilder builder = builderSetUp();
        client = builder.endpoint(getEndpoint()).buildSystemScanRulesetsClient();
    }


    @Test
    public void testListAll() {
        PagedIterable<BinaryData> response = client.listAll(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());

        assertTrue(list.size() > 0);
    }
}

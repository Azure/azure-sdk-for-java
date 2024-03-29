// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.SharePointOnlineListSource;

public final class SharePointOnlineListSourceTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SharePointOnlineListSource model = BinaryData.fromString(
            "{\"type\":\"SharePointOnlineListSource\",\"query\":\"datacnkojy\",\"httpRequestTimeout\":\"datahbtycfj\",\"sourceRetryCount\":\"dataxiapts\",\"sourceRetryWait\":\"datadoybpwzniekedx\",\"maxConcurrentConnections\":\"dataevip\",\"disableMetricsCollection\":\"datazcxqdrqsuve\",\"\":{\"oxqwcusls\":\"datayb\",\"zwybbewjvyrd\":\"datatzq\",\"bwr\":\"dataw\"}}")
            .toObject(SharePointOnlineListSource.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SharePointOnlineListSource model = new SharePointOnlineListSource().withSourceRetryCount("dataxiapts")
            .withSourceRetryWait("datadoybpwzniekedx").withMaxConcurrentConnections("dataevip")
            .withDisableMetricsCollection("datazcxqdrqsuve").withQuery("datacnkojy")
            .withHttpRequestTimeout("datahbtycfj");
        model = BinaryData.fromObject(model).toObject(SharePointOnlineListSource.class);
    }
}

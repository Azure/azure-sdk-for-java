// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientExceptionTest {

    @Test(groups = { "unit" })
    public void headerNotNull1() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull2() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, "dummy");
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull3() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, new RuntimeException());
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull4() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException(0, (CosmosError) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull5() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, 0, (CosmosError) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull6() {
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, (Exception) null, (Map) null, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull7() {
        ImmutableMap<String, String> respHeaders = ImmutableMap.of("key", "value");
        CosmosClientException dce = BridgeInternal.createCosmosClientException((String) null, (Exception) null, respHeaders, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).contains(respHeaders.entrySet().iterator().next());
    }
}

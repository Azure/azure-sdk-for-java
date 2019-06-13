/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientExceptionTest {

    @Test(groups = { "unit" })
    public void headerNotNull1() {
        CosmosClientException dce = new CosmosClientException(0);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull2() {
        CosmosClientException dce = new CosmosClientException(0, "dummy");
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull3() {
        CosmosClientException dce = new CosmosClientException(0, new RuntimeException());
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull4() {
        CosmosClientException dce = new CosmosClientException(0, (Error) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull5() {
        CosmosClientException dce = new CosmosClientException((String) null, 0, (Error) null, (Map) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull6() {
        CosmosClientException dce = new CosmosClientException((String) null, (Exception) null, (Map) null, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void headerNotNull7() {
        ImmutableMap<String, String> respHeaders = ImmutableMap.of("key", "value");
        CosmosClientException dce = new CosmosClientException((String) null, (Exception) null, respHeaders, 0, (String) null);
        assertThat(dce.responseHeaders()).isNotNull();
        assertThat(dce.responseHeaders()).contains(respHeaders.entrySet().iterator().next());
    }
}

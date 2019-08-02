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
package com.microsoft.azure.cosmosdb;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class PermissionTest {

    @Test(groups = {"unit"})
    public void deserialize() {
        String json = "{" +
                "    'id': 'a98eb026-b66b-4cec-8fb9-9b0e10ddab76'," +
                "    'permissionMode': 'read'," +
                "    'resource': 'dbs/AQAAAA==/colls/AQAAAJ0fgTc='," +
                "    'resourcePartitionKey': ['/id']" +
                "}";
        Permission p = new Permission(json);
        assertThat(p.getResourcePartitionKey()).isEqualToComparingFieldByField(new PartitionKey("/id"));
        assertThat(p.getPermissionMode()).isEqualTo(PermissionMode.Read);
    }
}
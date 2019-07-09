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

import com.azure.data.cosmos.internal.Conflict;
import com.azure.data.cosmos.internal.Document;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConflictTests {
    private String conflictAsString;

    @BeforeClass(groups = { "unit" })
    public void setup() throws Exception {
        conflictAsString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("sampleConflict.json"), "UTF-8");
    }

    @Test(groups = { "unit" })
    public void getSourceResourceId() {
        Conflict conf = new Conflict(conflictAsString);
        assertThat(conf.getSourceResourceId()).isEqualTo("k6d9ALgBmD+ChB4AAAAAAA==");
    }

    @Test(groups = { "unit" })
    public void getOperationKind() {
        Conflict conf = new Conflict(conflictAsString);
        assertThat(conf.getOperationKind()).isEqualTo("create");
        conf.getSourceResourceId();
    }

    @Test(groups = { "unit" })
    public void getResourceType() {
        Conflict conf = new Conflict(conflictAsString);
        assertThat(conf.getResouceType()).isEqualTo("document");
        conf.getSourceResourceId();
    }

    @Test(groups = { "unit" })
    public void getResource() {
        Conflict conf = new Conflict(conflictAsString);
        Document doc = conf.getResource(Document.class);
        assertThat(doc.id()).isEqualTo("0007312a-a1c5-4b54-9e39-35de2367fa33");
        assertThat(doc.getInt("regionId")).isEqualTo(2);
        assertThat(doc.resourceId()).isEqualTo("k6d9ALgBmD+ChB4AAAAAAA==");
        assertThat(doc.etag()).isEqualTo("\"00000200-0000-0000-0000-5b6e214b0000\"");
    }
}

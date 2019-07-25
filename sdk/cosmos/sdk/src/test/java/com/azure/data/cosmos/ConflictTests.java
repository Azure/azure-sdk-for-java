// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

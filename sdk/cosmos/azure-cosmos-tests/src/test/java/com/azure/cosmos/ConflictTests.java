// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.ConflictResolutionPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ConflictTests {
    private List<String> confList = new ArrayList<String>();

    @BeforeClass(groups = { "unit" })
    public void before_ConflictTests() throws Exception {
        String conflictAsString = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("sampleConflict.json"), "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode valuesNode = mapper.readTree(conflictAsString).get("conflictList");
        for (JsonNode node : valuesNode) {
            confList.add(node.toString());
        }
    }

    @Test(groups = { "unit" })
    public void getSourceResourceId() {
        Conflict conf = new Conflict(confList.get(0));
        assertThat(conf.getSourceResourceId()).isEqualTo("k6d9ALgBmD+ChB4AAAAAAA==");
    }

    @Test(groups = { "unit" })
    public void getOperationKind() {
        Conflict conf = new Conflict(confList.get(0));
        assertThat(conf.getOperationKind().toString()).isEqualTo("create");
        conf = new Conflict(confList.get(1));
        assertThat(conf.getOperationKind().toString()).isEqualTo("update");
        conf = new Conflict(confList.get(2));
        assertThat(conf.getOperationKind().toString()).isEqualTo("delete");
        conf = new Conflict(confList.get(3));
        assertThat(conf.getOperationKind().toString()).isEqualTo("replace");
        conf = new Conflict(confList.get(4));
        assertThat(conf.getOperationKind().toString()).isEqualTo("unknown");
        conf.getSourceResourceId();
    }

    @Test(groups = { "unit" })
    public void getResourceType() {
        Conflict conf = new Conflict(confList.get(0));
        assertThat(conf.getResourceType()).isEqualTo("document");
        conf.getSourceResourceId();
    }

    @Test(groups = { "unit" })
    public void getResource() {
        Conflict conf = new Conflict(confList.get(0));
        Document doc = conf.getResource(Document.class);
        assertThat(doc.getId()).isEqualTo("0007312a-a1c5-4b54-9e39-35de2367fa33");
        assertThat(doc.getInt("regionId")).isEqualTo(2);
        assertThat(doc.getResourceId()).isEqualTo("k6d9ALgBmD+ChB4AAAAAAA==");
        assertThat(doc.getETag()).isEqualTo("\"00000200-0000-0000-0000-5b6e214b0000\"");
    }

    @Test(groups = {"unit"})
    public void createCustomPolicyWithSproc() {
        try {
            ConflictResolutionPolicy.createCustomPolicy(null, "testColl", "testSproc");
            fail("ConflictResolutionPolicy.createCustomPolicy should fail with IllegalArgumentException as dbName is " +
                "null");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("dbName cannot be null");
        }

        try {
            ConflictResolutionPolicy.createCustomPolicy("testDb", null, "testSproc");
            fail("ConflictResolutionPolicy.createCustomPolicy should fail with IllegalArgumentException as containerName is " +
                "null");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("containerName cannot be null");
        }

        try {
            ConflictResolutionPolicy.createCustomPolicy("testDb", "testColl", null);
            fail("ConflictResolutionPolicy.createCustomPolicy should fail with IllegalArgumentException as sprocName is " +
                "null");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("sprocName cannot be null");
        }
    }
}

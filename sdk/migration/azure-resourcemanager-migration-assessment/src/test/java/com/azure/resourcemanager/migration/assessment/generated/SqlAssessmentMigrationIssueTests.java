// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.migration.assessment.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.migration.assessment.models.SqlAssessmentMigrationIssue;

public final class SqlAssessmentMigrationIssueTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SqlAssessmentMigrationIssue model = BinaryData.fromString(
            "{\"issueId\":\"a\",\"issueCategory\":\"Issue\",\"impactedObjects\":[{\"objectName\":\"h\",\"objectType\":\"usps\"},{\"objectName\":\"sdvlmfwdgzxulucv\",\"objectType\":\"mrsreuzvxurisjnh\"},{\"objectName\":\"txifqj\",\"objectType\":\"xmrhu\"},{\"objectName\":\"wp\",\"objectType\":\"sutrgjup\"}]}")
            .toObject(SqlAssessmentMigrationIssue.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SqlAssessmentMigrationIssue model = new SqlAssessmentMigrationIssue();
        model = BinaryData.fromObject(model).toObject(SqlAssessmentMigrationIssue.class);
    }
}
